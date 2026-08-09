import { test, expect } from '@playwright/test'
import { startDex } from './fixtures/dex.js'

let dex

test.beforeAll(async () => {
  dex = await startDex()
})

test.afterAll(async () => {
  await dex.stop()
})

test.beforeEach(async ({ page }) => {
  // Mirrors how this file is actually served in production: a runtime-fetched JSON config
  // (see AuthProvider.jsx / auth.js), injected here rather than by nginx/a ConfigMap.
  await page.route('**/config.json', (route) =>
    route.fulfill({
      json: {
        OIDC_ISSUER: dex.issuerUrl,
        OIDC_CLIENT_ID: dex.clientId,
        OIDC_SCOPE: 'openid profile email',
      },
    })
  )

  // Auth-flow-only scope: notes CRUD against the real backend is already covered by the
  // backend's own test suite and the frontend's mocked-fetch component tests.
  await page.route('**/api/notes**', (route) =>
    route.fulfill({ json: { notes: [], nextCursor: null } })
  )
})

async function loginThroughDex(page) {
  // No session yet - AuthProvider's init() redirects straight to Dex.
  await page.waitForURL(/^http:\/\/localhost:5556\/dex\/auth/)

  await page.locator('#login').fill(dex.username)
  await page.locator('#password').fill(dex.password)
  await page.locator('#submit-login').click()

  // Dex redirects back to the app with ?code=...&state=...; AuthProvider exchanges it and
  // clears the query string via history.replaceState once the exchange succeeds.
  await page.waitForURL((url) => url.origin === 'http://localhost:4173' && url.search === '')
}

test('logs in through a real OIDC authorization-code redirect round-trip', async ({ page }) => {
  await page.goto('/')
  await loginThroughDex(page)

  // Reaching the authenticated view proves AuthProvider's callback handling completed the
  // token exchange and didn't fall through to another signinRedirect() - the regression fixed
  // in PR #58 (history.replaceState clearing window.location before signinRedirectCallback()
  // could read code/state from it) made every callback fail locally and loop back here.
  await expect(page.getByRole('heading', { name: /notes/i })).toBeVisible()
})

test('does not loop back to Dex after a successful callback', async ({ page }) => {
  let authEntryHits = 0
  page.on('request', (request) => {
    // Match only the exact entry point signinRedirect() navigates to, not its downstream
    // /dex/auth/local and /dex/auth/local/login sub-steps, which happen once per login
    // attempt regardless of whether a loop occurred.
    if (new URL(request.url()).pathname === '/dex/auth') {
      authEntryHits += 1
    }
  })

  await page.goto('/')
  await loginThroughDex(page)
  await expect(page.getByRole('heading', { name: /notes/i })).toBeVisible()

  // A regression that caused even a brief loop (a few extra round-trips before eventually
  // recovering) would still pass the test above but fail this stricter assertion.
  expect(authEntryHits).toBe(1)
})
