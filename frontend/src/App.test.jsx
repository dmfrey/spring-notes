import { render, screen } from '@testing-library/react'
import { vi, describe, it, expect, beforeEach } from 'vitest'
import App from './App'

vi.mock('./AuthProvider.jsx', () => ({
  useAuth: () => ({ user: { access_token: 'test-token' } }),
}))

describe('App', () => {
  beforeEach(() => {
    global.fetch = vi.fn().mockResolvedValue({
      ok: true,
      json: () => Promise.resolve({ notes: [], nextCursor: null }),
    })
  })

  it('renders the notes heading', async () => {
    render(<App />)
    expect(await screen.findByRole('heading', { name: /notes/i })).toBeInTheDocument()
  })

  it('shows empty state when no notes', async () => {
    render(<App />)
    expect(await screen.findByText('No notes yet.')).toBeInTheDocument()
  })
})