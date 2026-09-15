import { useState } from 'react'
import { ArrowUpIcon, ArrowDownIcon, CloseIcon } from './icons.jsx'

async function addItem(noteId, text, headers) {
  const response = await fetch(`/api/notes/${noteId}/items`, {
    method: 'POST',
    headers: { ...headers, 'Content-Type': 'application/json' },
    body: JSON.stringify({ text }),
  })
  if (!response.ok) throw new Error(`Failed to add item: ${response.status}`)
  return response.json()
}

async function removeItem(noteId, itemId, headers) {
  const response = await fetch(`/api/notes/${noteId}/items/${itemId}`, { method: 'DELETE', headers })
  if (!response.ok) throw new Error(`Failed to remove item: ${response.status}`)
  return response.json()
}

async function toggleItem(noteId, itemId, checked, headers) {
  const response = await fetch(`/api/notes/${noteId}/items/${itemId}/toggle`, {
    method: 'PATCH',
    headers: { ...headers, 'Content-Type': 'application/json' },
    body: JSON.stringify({ checked }),
  })
  if (!response.ok) throw new Error(`Failed to toggle item: ${response.status}`)
  return response.json()
}

async function reorderItems(noteId, orderedItemIds, headers) {
  const response = await fetch(`/api/notes/${noteId}/items`, {
    method: 'PUT',
    headers: { ...headers, 'Content-Type': 'application/json' },
    body: JSON.stringify({ orderedItemIds }),
  })
  if (!response.ok) throw new Error(`Failed to reorder items: ${response.status}`)
  return response.json()
}

// Checked items render after unchecked ones, purely at render time - toggling an item never
// reorders the underlying list, only its checked flag, so the up/down reorder buttons (which
// operate on the note's actual stored order) stay a cleanly separate concern from "moves to the
// bottom when checked."
export default function Checklist({ note, authHeaders, onNoteUpdated }) {
  const [newItemText, setNewItemText] = useState('')
  const [error, setError] = useState(null)
  const [busy, setBusy] = useState(false)

  const unchecked = note.items.filter((item) => !item.checked)
  const checked = note.items.filter((item) => item.checked)

  async function runMutation(fn) {
    setBusy(true)
    setError(null)
    try {
      const updated = await fn()
      onNoteUpdated(updated)
      return updated
    } catch (e) {
      setError(e.message)
      return null
    } finally {
      setBusy(false)
    }
  }

  function handleToggle(item) {
    runMutation(() => toggleItem(note.id, item.id, !item.checked, authHeaders))
  }

  function handleRemove(item) {
    runMutation(() => removeItem(note.id, item.id, authHeaders))
  }

  async function handleAdd(e) {
    e.preventDefault()
    if (!newItemText.trim()) return
    const updated = await runMutation(() => addItem(note.id, newItemText, authHeaders))
    if (updated) setNewItemText('')
  }

  function handleMove(item, direction) {
    const currentIds = note.items.map((i) => i.id)
    const index = currentIds.indexOf(item.id)
    const swapWith = index + direction
    if (swapWith < 0 || swapWith >= currentIds.length) return
    const reordered = [...currentIds]
    ;[reordered[index], reordered[swapWith]] = [reordered[swapWith], reordered[index]]
    runMutation(() => reorderItems(note.id, reordered, authHeaders))
  }

  return (
    <div className="checklist">
      <ul className="checklist__items">
        {[...unchecked, ...checked].map((item) => (
          <li key={item.id} className="checklist__item">
            <input
              type="checkbox"
              className="checklist__checkbox"
              checked={item.checked}
              disabled={busy}
              onChange={() => handleToggle(item)}
            />
            <span className={`checklist__item-text${item.checked ? ' checklist__item-text--checked' : ''}`}>
              {item.text}
            </span>
            <div className="checklist__item-actions">
              <button type="button" className="btn--icon-sm" onClick={() => handleMove(item, -1)} disabled={busy} aria-label="Move up">
                <ArrowUpIcon />
              </button>
              <button type="button" className="btn--icon-sm" onClick={() => handleMove(item, 1)} disabled={busy} aria-label="Move down">
                <ArrowDownIcon />
              </button>
              <button type="button" className="btn--icon-sm" onClick={() => handleRemove(item)} disabled={busy} aria-label="Remove item">
                <CloseIcon size={13} />
              </button>
            </div>
          </li>
        ))}
      </ul>
      {error && <p role="alert" className="checklist__error">Error: {error}</p>}
      <form className="checklist__add-form" onSubmit={handleAdd}>
        <input
          type="text"
          className="checklist__add-input"
          value={newItemText}
          onChange={(e) => setNewItemText(e.target.value)}
          placeholder="Add item"
          disabled={busy}
        />
        <button type="submit" className="checklist__add-btn" disabled={busy || !newItemText.trim()}>Add</button>
      </form>
    </div>
  )
}
