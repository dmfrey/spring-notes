import { render, screen, fireEvent, waitFor, within } from '@testing-library/react'
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
    const confirmDialog = screen.getByRole('heading', { name: /delete note/i }).closest('dialog')
    fireEvent.click(within(confirmDialog).getByRole('button', { name: /^delete$/i }))

    await waitFor(() => {
      expect(screen.queryByRole('heading', { name: 'Note A' })).not.toBeInTheDocument()
    })
  })

  it('renders a checklist for LIST-type notes instead of plain content', async () => {
    global.fetch = vi.fn().mockResolvedValue({
      ok: true,
      json: () => Promise.resolve({
        notes: [{
          id: '11111111-0000-0000-0000-000000000001',
          title: 'Groceries',
          content: null,
          type: 'LIST',
          items: [{ id: 'item-1', text: 'Milk', checked: false }],
        }],
        nextCursor: null,
      }),
    })

    render(<App />)

    expect(await screen.findByText('Groceries')).toBeInTheDocument()
    expect(screen.getByText('Milk')).toBeInTheDocument()
    expect(screen.getByRole('checkbox')).toBeInTheDocument()
  })

  it('switching the create-modal type to Checklist shows item inputs instead of the content textarea', async () => {
    render(<App />)

    fireEvent.click(await screen.findByRole('button', { name: /new note/i }))
    fireEvent.click(screen.getByRole('radio', { name: /checklist/i }))

    expect(screen.queryByLabelText(/content/i)).not.toBeInTheDocument()
    expect(screen.getByPlaceholderText('Item 1')).toBeInTheDocument()
  })

  it('creating a checklist note sends type and non-empty items to the API', async () => {
    global.fetch = vi.fn()
      .mockResolvedValueOnce({
        ok: true,
        json: () => Promise.resolve({ notes: [], nextCursor: null }),
      })
      .mockResolvedValueOnce({
        ok: true,
        json: () => Promise.resolve({
          id: '11111111-0000-0000-0000-000000000002',
          title: 'Groceries',
          content: null,
          type: 'LIST',
          items: [{ id: 'item-1', text: 'Milk', checked: false }],
        }),
      })

    render(<App />)

    fireEvent.click(await screen.findByRole('button', { name: /new note/i }))
    fireEvent.click(screen.getByRole('radio', { name: /checklist/i }))
    fireEvent.change(screen.getByLabelText(/title/i), { target: { value: 'Groceries' } })
    fireEvent.change(screen.getByPlaceholderText('Item 1'), { target: { value: 'Milk' } })
    fireEvent.click(screen.getByRole('button', { name: /^save$/i }))

    await waitFor(() => {
      expect(global.fetch).toHaveBeenCalledWith('/api/notes', expect.objectContaining({
        method: 'POST',
        body: JSON.stringify({ title: 'Groceries', type: 'LIST', items: ['Milk'] }),
      }))
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