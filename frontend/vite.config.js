import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: './src/test/setup.js',
    // e2e/ holds Playwright specs, a separate test runner (npm run test:e2e) - vitest's
    // default glob would otherwise also pick up e2e/*.spec.js as its own test files.
    // First two entries are vitest's own defaultExclude - a custom `exclude` replaces them
    // entirely rather than extending them, so they're repeated here.
    exclude: ['**/node_modules/**', '**/.git/**', 'e2e/**'],
    coverage: {
      provider: 'v8',
      reporter: ['text', 'json', 'json-summary', 'html'],
      reportsDirectory: 'coverage',
    },
  },
  server: {
    proxy: {
      // Mirrors the Envoy strip-prefix routing in production:
      // /api/* → backend (prefix stripped before forwarding)
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '')
      },
      '/otlp': {
        target: 'http://localhost:4318',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/otlp/, '')
      }
    }
  }
})