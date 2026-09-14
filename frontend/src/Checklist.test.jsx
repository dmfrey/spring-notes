import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import { vi, describe, it, expect, beforeEach } from 'vitest'
import Checklist from './Checklist'

const authHeaders = { 'API-Version': '1', 'Authorization': 'Bearer test-token' }

function makeNote(items) {
  return { id: 'note-1', title: 'Groceries', content: null, type: 'LIST', items }
}

describe('Checklist', () => {
  beforeEach(() => {
    global.fetch = vi.fn()
  })

  it('renders unchecked items before checked items regardless of stored order', () => {
    const note = makeNote([
      { id: 'a', text: 'Eggs', checked: true },
      { id: 'b', text: 'Milk', checked: false },
    ])

    render(<Checklist note={note} authHeaders={authHeaders} onNoteUpdated={vi.fn()} />)

    const texts = screen.getAllByText(/Eggs|Milk/).map((el) => el.textContent)
    expect(texts).toEqual(['Milk', 'Eggs'])
  })

  it('renders checked items with strikethrough styling', () => {
    const note = makeNote([{ id: 'a', text: 'Eggs', checked: true }])

    render(<Checklist note={note} authHeaders={authHeaders} onNoteUpdated={vi.fn()} />)

    expect(screen.getByText('Eggs')).toHaveClass('checklist__item-text--checked')
  })

  it('toggling an unchecked item PATCHes checked=true and calls onNoteUpdated', async () => {
    const note = makeNote([{ id: 'a', text: 'Milk', checked: false }])
    const onNoteUpdated = vi.fn()
    const updated = makeNote([{ id: 'a', text: 'Milk', checked: true }])
    global.fetch.mockResolvedValueOnce({ ok: true, json: () => Promise.resolve(updated) })

    render(<Checklist note={note} authHeaders={authHeaders} onNoteUpdated={onNoteUpdated} />)

    fireEvent.click(screen.getByRole('checkbox'))

    await waitFor(() => {
      expect(global.fetch).toHaveBeenCalledWith('/api/notes/note-1/items/a/toggle', expect.objectContaining({
        method: 'PATCH',
        body: JSON.stringify({ checked: true }),
      }))
      expect(onNoteUpdated).toHaveBeenCalledWith(updated)
    })
  })

  it('removing an item calls DELETE and calls onNoteUpdated', async () => {
    const note = makeNote([{ id: 'a', text: 'Milk', checked: false }])
    const onNoteUpdated = vi.fn()
    const updated = makeNote([])
    global.fetch.mockResolvedValueOnce({ ok: true, json: () => Promise.resolve(updated) })

    render(<Checklist note={note} authHeaders={authHeaders} onNoteUpdated={onNoteUpdated} />)

    fireEvent.click(screen.getByRole('button', { name: /remove item/i }))

    await waitFor(() => {
      expect(global.fetch).toHaveBeenCalledWith('/api/notes/note-1/items/a', expect.objectContaining({ method: 'DELETE' }))
      expect(onNoteUpdated).toHaveBeenCalledWith(updated)
    })
  })

  it('adding an item POSTs the text and clears the input on success', async () => {
    const note = makeNote([])
    const onNoteUpdated = vi.fn()
    const updated = makeNote([{ id: 'a', text: 'Bread', checked: false }])
    global.fetch.mockResolvedValueOnce({ ok: true, json: () => Promise.resolve(updated) })

    render(<Checklist note={note} authHeaders={authHeaders} onNoteUpdated={onNoteUpdated} />)

    const input = screen.getByPlaceholderText('Add item')
    fireEvent.change(input, { target: { value: 'Bread' } })
    fireEvent.click(screen.getByRole('button', { name: /^add$/i }))

    await waitFor(() => {
      expect(global.fetch).toHaveBeenCalledWith('/api/notes/note-1/items', expect.objectContaining({
        method: 'POST',
        body: JSON.stringify({ text: 'Bread' }),
      }))
      expect(onNoteUpdated).toHaveBeenCalledWith(updated)
    })
    expect(input.value).toBe('')
  })

  it('moving an item down PUTs the swapped order', async () => {
    const note = makeNote([
      { id: 'a', text: 'Milk', checked: false },
      { id: 'b', text: 'Eggs', checked: false },
    ])
    const updated = makeNote([
      { id: 'b', text: 'Eggs', checked: false },
      { id: 'a', text: 'Milk', checked: false },
    ])
    global.fetch.mockResolvedValueOnce({ ok: true, json: () => Promise.resolve(updated) })

    render(<Checklist note={note} authHeaders={authHeaders} onNoteUpdated={vi.fn()} />)

    const downButtons = screen.getAllByRole('button', { name: /move down/i })
    fireEvent.click(downButtons[0])

    await waitFor(() => {
      expect(global.fetch).toHaveBeenCalledWith('/api/notes/note-1/items', expect.objectContaining({
        method: 'PUT',
        body: JSON.stringify({ orderedItemIds: ['b', 'a'] }),
      }))
    })
  })

  it('shows an error message when a mutation fails', async () => {
    const note = makeNote([{ id: 'a', text: 'Milk', checked: false }])
    global.fetch.mockResolvedValueOnce({ ok: false, status: 404 })

    render(<Checklist note={note} authHeaders={authHeaders} onNoteUpdated={vi.fn()} />)

    fireEvent.click(screen.getByRole('checkbox'))

    expect(await screen.findByRole('alert')).toHaveTextContent(/error/i)
  })
})
