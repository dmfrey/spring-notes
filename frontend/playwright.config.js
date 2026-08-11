import { defineConfig, devices } from '@playwright/test'

export default defineConfig({
  testDir: './e2e',
  // Dex runs on a fixed host port (see e2e/fixtures/dex.js) - workers must stay serial so
  // two specs never try to start two Dex containers on the same port at once.
  workers: 1,
  fullyParallel: false,
  reporter: process.env.CI
    ? [['html', { open: 'never' }], ['github'], ['json', { outputFile: 'playwright-report/results.json' }]]
    : 'list',
  webServer: {
    // Runs the actual production build artifact (matches what nginx serves), not the dev
    // server - the whole point of this tier is fidelity to what actually ships.
    command: 'npm run build && npm run preview -- --port 4173 --strictPort',
    url: 'http://localhost:4173',
    reuseExistingServer: !process.env.CI,
    timeout: 60_000,
  },
  use: {
    baseURL: 'http://localhost:4173',
    trace: 'retain-on-failure',
  },
  projects: [{ name: 'chromium', use: { ...devices['Desktop Chrome'] } }],
})
