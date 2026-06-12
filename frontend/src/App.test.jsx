import { render, screen, fireEvent, waitFor } from '@testing-library/react'
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

  it('renders a delete button for each note', async () => {
    global.fetch = vi.fn().mockResolvedValue({
      ok: true,
      json: () => Promise.resolve({
        notes: [{ id: '11111111-0000-0000-0000-000000000001', title: 'Note A', content: 'Content A' }],
        nextCursor: null,
      }),
    })

    render(<App />)

    expect(await screen.findByText('Note A')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /delete/i })).toBeInTheDocument()
  })

  it('shows confirmation dialog when delete button is clicked', async () => {
    global.fetch = vi.fn().mockResolvedValue({
      ok: true,
      json: () => Promise.resolve({
        notes: [{ id: '11111111-0000-0000-0000-000000000001', title: 'Note A', content: 'Content A' }],
        nextCursor: null,
      }),
    })

    render(<App />)

    await screen.findByText('Note A')
    fireEvent.click(screen.getByRole('button', { name: /^delete$/i }))

    const confirmText = await screen.findByText(/are you sure/i)
    expect(confirmText.closest('dialog')).toHaveAttribute('open')
  })

  it('removes note from list after confirmed delete', async () => {
    const noteId = '11111111-0000-0000-0000-000000000001'
    global.fetch = vi.fn()
      .mockResolvedValueOnce({
        ok: true,
        json: () => Promise.resolve({
          notes: [{ id: noteId, title: 'Note A', content: 'Content A' }],
          nextCursor: null,
        }),
      })
      .mockResolvedValueOnce({ ok: true })

    render(<App />)

    await screen.findByText('Note A')
    fireEvent.click(screen.getByRole('button', { name: /^delete$/i }))
    await screen.findByText(/are you sure/i)
    fireEvent.click(screen.getByRole('button', { name: /^delete$/i, hidden: true }))

    await waitFor(() => {
      expect(screen.queryByRole('heading', { name: 'Note A' })).not.toBeInTheDocument()
    })
  })

  it('closes confirmation dialog on cancel', async () => {
    global.fetch = vi.fn().mockResolvedValue({
      ok: true,
      json: () => Promise.resolve({
        notes: [{ id: '11111111-0000-0000-0000-000000000001', title: 'Note A', content: 'Content A' }],
        nextCursor: null,
      }),
    })

    render(<App />)

    await screen.findByText('Note A')
    fireEvent.click(screen.getByRole('button', { name: /^delete$/i }))
    await screen.findByText(/are you sure/i)
    fireEvent.click(screen.getByRole('button', { name: /cancel/i }))

    await waitFor(() => {
      expect(screen.getByText(/are you sure/i).closest('dialog')).not.toHaveAttribute('open')
    })
    expect(screen.getByRole('heading', { name: 'Note A' })).toBeInTheDocument()
  })
})