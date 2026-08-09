import { render, screen } from '@testing-library/react'
import { vi, describe, it, expect, beforeEach, afterEach } from 'vitest'
import { AuthProvider, useAuth } from './AuthProvider.jsx'

vi.mock('./auth.js', () => ({
  loadConfig: vi.fn(),
  createUserManager: vi.fn(),
}))

import { loadConfig, createUserManager } from './auth.js'

function Consumer() {
  const { user } = useAuth()
  return <div>{user ? `logged in as ${user.profile?.sub}` : 'no user'}</div>
}

describe('AuthProvider', () => {
  beforeEach(() => {
    loadConfig.mockResolvedValue({ OIDC_ISSUER: 'https://issuer.example', OIDC_CLIENT_ID: 'client' })
  })

  afterEach(() => {
    window.history.replaceState({}, '', '/')
  })

  it('passes the pre-clear callback URL to signinRedirectCallback', async () => {
    window.history.pushState({}, '', '/?code=abc123&state=xyz789')
    const expectedUrl = window.location.href

    const signinRedirectCallback = vi.fn().mockResolvedValue({ profile: { sub: 'user-1' } })
    createUserManager.mockReturnValue({
      signinRedirectCallback,
      getUser: vi.fn(),
      clearStaleState: vi.fn(),
      signinRedirect: vi.fn(),
    })

    render(
      <AuthProvider>
        <Consumer />
      </AuthProvider>
    )

    await screen.findByText('logged in as user-1')

    // The regression this guards: window.location was cleared via
    // history.replaceState('/') before signinRedirectCallback() ran, so a
    // call with no arguments (falling back to window.location.href) would
    // read code/state-less URL and fail every time.
    expect(signinRedirectCallback).toHaveBeenCalledWith(expectedUrl)
    expect(window.location.search).toBe('')
  })

  it('does not treat a plain visit (no code/state) as a callback', async () => {
    window.history.pushState({}, '', '/')

    const signinRedirectCallback = vi.fn()
    const existingUser = { profile: { sub: 'user-2' }, expired: false }
    createUserManager.mockReturnValue({
      signinRedirectCallback,
      getUser: vi.fn().mockResolvedValue(existingUser),
      clearStaleState: vi.fn(),
      signinRedirect: vi.fn(),
    })

    render(
      <AuthProvider>
        <Consumer />
      </AuthProvider>
    )

    await screen.findByText('logged in as user-2')

    expect(signinRedirectCallback).not.toHaveBeenCalled()
  })
})
