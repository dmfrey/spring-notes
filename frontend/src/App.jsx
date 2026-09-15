import { useState, useEffect, useCallback, useRef } from 'react'
import { useAuth } from './AuthProvider.jsx'
import ChatPanel from './ChatPanel.jsx'
import Checklist from './Checklist.jsx'
import { PlusIcon, TrashIcon, LogoIcon, DocIcon, ListIcon, CloseIcon } from './icons.jsx'

function NoteBadge({ type }) {
  return (
    <div className={`note-badge ${type === 'LIST' ? 'note-badge--list' : 'note-badge--text'}`}>
      {type === 'LIST' ? <ListIcon /> : <DocIcon />}
    </div>
  )
}

async function fetchNotes(cursor, limit = 25, headers) {
  const params = new URLSearchParams({ limit })
  if (cursor) params.set('cursor', cursor)

  const response = await fetch(`/api/notes?${params}`, { headers })
  if (!response.ok) throw new Error(`Failed to fetch notes: ${response.status}`)
  return response.json()
}

async function postNote(title, type, content, items, headers) {
  const body = type === 'LIST' ? { title, type, items } : { title, type, content }
  const response = await fetch('/api/notes', {
    method: 'POST',
    headers: { ...headers, 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  })
  if (!response.ok) throw new Error(`Failed to create note: ${response.status}`)
  return response.json()
}

async function deleteNote(id, headers) {
  const response = await fetch(`/api/notes/${id}`, { method: 'DELETE', headers })
  if (!response.ok) throw new Error(`Failed to delete note: ${response.status}`)
}

export default function App() {
  const { user } = useAuth()
  const [notes, setNotes] = useState([])
  const [cursor, setCursor] = useState(null)
  const [hasMore, setHasMore] = useState(false)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  const [modalOpen, setModalOpen] = useState(false)
  const [title, setTitle] = useState('')
  const [type, setType] = useState('TEXT')
  const [content, setContent] = useState('')
  const [itemDrafts, setItemDrafts] = useState([''])
  const [submitting, setSubmitting] = useState(false)
  const [createError, setCreateError] = useState(null)

  const dialogRef = useRef(null)

  const [noteToDelete, setNoteToDelete] = useState(null)
  const [deleting, setDeleting] = useState(false)
  const [deleteError, setDeleteError] = useState(null)
  const confirmDialogRef = useRef(null)

  const authHeaders = {
    'API-Version': '1',
    'Authorization': `Bearer ${user.access_token}`,
  }

  const loadNotes = useCallback(async (nextCursor) => {
    setLoading(true)
    setError(null)
    try {
      const data = await fetchNotes(nextCursor, 25, authHeaders)
      setNotes((prev) => nextCursor ? [...prev, ...data.notes] : data.notes)
      setCursor(data.nextCursor ?? null)
      setHasMore(!!data.nextCursor)
    } catch (e) {
      setError(e.message)
    } finally {
      setLoading(false)
    }
  }, [user.access_token])

  useEffect(() => {
    loadNotes(null)
  }, [loadNotes])

  useEffect(() => {
    const dialog = dialogRef.current
    if (!dialog) return
    if (modalOpen) {
      dialog.showModal()
    } else {
      dialog.close()
    }
  }, [modalOpen])

  useEffect(() => {
    const dialog = confirmDialogRef.current
    if (!dialog) return
    if (noteToDelete) {
      dialog.showModal()
    } else {
      dialog.close()
    }
  }, [noteToDelete])

  function openModal() {
    setTitle('')
    setType('TEXT')
    setContent('')
    setItemDrafts([''])
    setCreateError(null)
    setModalOpen(true)
  }

  function closeModal() {
    setModalOpen(false)
  }

  function openConfirmDelete(note) {
    setDeleteError(null)
    setNoteToDelete(note)
  }

  function cancelDelete() {
    setNoteToDelete(null)
  }

  async function handleDeleteConfirm() {
    setDeleting(true)
    setDeleteError(null)
    try {
      await deleteNote(noteToDelete.id, authHeaders)
      setNotes((prev) => prev.filter((n) => n.id !== noteToDelete.id))
      setNoteToDelete(null)
    } catch (e) {
      setDeleteError(e.message)
    } finally {
      setDeleting(false)
    }
  }

  function updateItemDraft(index, text) {
    setItemDrafts((prev) => prev.map((t, i) => (i === index ? text : t)))
  }

  function addItemDraftRow() {
    setItemDrafts((prev) => [...prev, ''])
  }

  function removeItemDraftRow(index) {
    setItemDrafts((prev) => prev.filter((_, i) => i !== index))
  }

  async function handleCreate(e) {
    e.preventDefault()
    setSubmitting(true)
    setCreateError(null)
    try {
      const items = itemDrafts.map((t) => t.trim()).filter((t) => t.length > 0)
      const note = await postNote(title, type, content, items, authHeaders)
      setNotes((prev) => [note, ...prev])
      closeModal()
    } catch (e) {
      setCreateError(e.message)
    } finally {
      setSubmitting(false)
    }
  }

  function handleNoteUpdated(updatedNote) {
    setNotes((prev) => prev.map((n) => (n.id === updatedNote.id ? updatedNote : n)))
  }

  return (
    <>
    <header className="app-header">
      <div className="app-header__brand">
        <div className="app-logo"><LogoIcon /></div>
        <h1>Notes</h1>
      </div>
      <div className="app-header__actions">
        <button className="btn btn--primary" onClick={openModal}>
          <PlusIcon />
          New Note
        </button>
      </div>
    </header>

    <main className="notes-main">
      {error && <p role="alert" className="error-message">Error: {error}</p>}

      {notes.length === 0 && !loading && !error && (
        <p className="state-message">No notes yet.</p>
      )}

      <ul className="notes-grid">
        {notes.map((note) => (
          <li key={note.id} className="note-card">
            <div className="note-card__head">
              <NoteBadge type={note.type} />
              <h2 className="note-card__title">{note.title}</h2>
              <button className="btn--icon-sm" onClick={() => openConfirmDelete(note)} aria-label="Delete">
                <TrashIcon />
              </button>
            </div>
            {note.type === 'LIST' ? (
              <Checklist note={note} authHeaders={authHeaders} onNoteUpdated={handleNoteUpdated} />
            ) : (
              <p className="note-card__body">{note.content}</p>
            )}
          </li>
        ))}
      </ul>

      {hasMore && (
        <button className="btn btn--ghost" onClick={() => loadNotes(cursor)} disabled={loading}>
          {loading ? 'Loading…' : 'Load more'}
        </button>
      )}

      {loading && notes.length === 0 && <p className="state-message">Loading…</p>}

      <dialog ref={confirmDialogRef} onClose={cancelDelete} className="app-dialog">
        <div className="app-dialog__head">
          <h2>Delete note?</h2>
        </div>
        <p>Are you sure you want to delete &quot;{noteToDelete?.title}&quot;? This cannot be undone.</p>
        {deleteError && <p role="alert" className="error-message">Error: {deleteError}</p>}
        <div className="dialog__actions">
          <button className="btn btn--danger-solid" onClick={handleDeleteConfirm} disabled={deleting}>
            {deleting ? 'Deleting…' : 'Delete'}
          </button>
          <button className="btn btn--ghost" onClick={cancelDelete} disabled={deleting}>Cancel</button>
        </div>
      </dialog>

      <dialog ref={dialogRef} onClose={closeModal} className="app-dialog">
        <div className="app-dialog__head">
          <h2>New Note</h2>
          <button type="button" className="btn--icon-sm" onClick={closeModal} aria-label="Close">
            <CloseIcon />
          </button>
        </div>
        <form onSubmit={handleCreate}>
          <div className="form-field">
            <label htmlFor="note-title" className="form-label">Title</label>
            <input
              id="note-title"
              type="text"
              className="text-input"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              required
              autoFocus
            />
          </div>
          <div className="form-field">
            <label className="form-label">Type</label>
            <div className="type-toggle">
              <input
                type="radio"
                id="note-type-text"
                name="note-type"
                value="TEXT"
                checked={type === 'TEXT'}
                onChange={() => setType('TEXT')}
              />
              <label htmlFor="note-type-text">Note</label>
              <input
                type="radio"
                id="note-type-list"
                name="note-type"
                value="LIST"
                checked={type === 'LIST'}
                onChange={() => setType('LIST')}
              />
              <label htmlFor="note-type-list">Checklist</label>
            </div>
          </div>
          {type === 'TEXT' ? (
            <div className="form-field">
              <label htmlFor="note-content" className="form-label">Content</label>
              <textarea
                id="note-content"
                className="textarea-input"
                rows={6}
                value={content}
                onChange={(e) => setContent(e.target.value)}
                required
              />
            </div>
          ) : (
            <div className="form-field">
              <label className="form-label">Items</label>
              {itemDrafts.map((text, index) => (
                <div key={index} className="item-draft-row">
                  <input
                    type="text"
                    className="text-input"
                    value={text}
                    onChange={(e) => updateItemDraft(index, e.target.value)}
                    placeholder={`Item ${index + 1}`}
                  />
                  <button
                    type="button"
                    className="btn--icon-sm"
                    onClick={() => removeItemDraftRow(index)}
                    disabled={itemDrafts.length === 1}
                    aria-label="Remove item"
                  >
                    <CloseIcon size={15} />
                  </button>
                </div>
              ))}
              <button type="button" className="add-item-btn" onClick={addItemDraftRow}>
                <PlusIcon size={15} />
                Add item
              </button>
            </div>
          )}
          {createError && <p role="alert" className="error-message">Error: {createError}</p>}
          <div className="dialog__actions">
            <button type="submit" className="btn btn--primary" disabled={submitting}>
              {submitting ? 'Saving…' : 'Save'}
            </button>
            <button type="button" className="btn btn--ghost" onClick={closeModal} disabled={submitting}>
              Cancel
            </button>
          </div>
        </form>
      </dialog>
    </main>
    <ChatPanel />
    </>
  )
}
