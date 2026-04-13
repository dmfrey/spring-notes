import { useState, useEffect, useCallback, useRef } from 'react'
import { useAuth } from './AuthProvider.jsx'

async function fetchNotes(cursor, limit = 25, headers) {
  const params = new URLSearchParams({ limit })
  if (cursor) params.set('cursor', cursor)

  const response = await fetch(`/api/notes?${params}`, { headers })
  if (!response.ok) throw new Error(`Failed to fetch notes: ${response.status}`)
  return response.json()
}

async function postNote(title, content, headers) {
  const response = await fetch('/api/notes', {
    method: 'POST',
    headers: { ...headers, 'Content-Type': 'application/json' },
    body: JSON.stringify({ title, content }),
  })
  if (!response.ok) throw new Error(`Failed to create note: ${response.status}`)
  return response.json()
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
  const [content, setContent] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [createError, setCreateError] = useState(null)

  const dialogRef = useRef(null)

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

  function openModal() {
    setTitle('')
    setContent('')
    setCreateError(null)
    setModalOpen(true)
  }

  function closeModal() {
    setModalOpen(false)
  }

  async function handleCreate(e) {
    e.preventDefault()
    setSubmitting(true)
    setCreateError(null)
    try {
      const note = await postNote(title, content, authHeaders)
      setNotes((prev) => [note, ...prev])
      closeModal()
    } catch (e) {
      setCreateError(e.message)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <main>
      <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
        <h1>Notes</h1>
        <button onClick={openModal}>New Note</button>
      </div>

      {error && <p role="alert">Error: {error}</p>}

      {notes.length === 0 && !loading && !error && (
        <p>No notes yet.</p>
      )}

      <ul>
        {notes.map((note) => (
          <li key={note.id}>
            <h2>{note.title}</h2>
            <p>{note.content}</p>
          </li>
        ))}
      </ul>

      {hasMore && (
        <button onClick={() => loadNotes(cursor)} disabled={loading}>
          {loading ? 'Loading…' : 'Load more'}
        </button>
      )}

      {loading && notes.length === 0 && <p>Loading…</p>}

      <dialog ref={dialogRef} onClose={closeModal}>
        <h2>New Note</h2>
        <form onSubmit={handleCreate}>
          <div>
            <label htmlFor="note-title">Title</label>
            <br />
            <input
              id="note-title"
              type="text"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              required
              autoFocus
            />
          </div>
          <div style={{ marginTop: '0.75rem' }}>
            <label htmlFor="note-content">Content</label>
            <br />
            <textarea
              id="note-content"
              rows={6}
              value={content}
              onChange={(e) => setContent(e.target.value)}
              required
            />
          </div>
          {createError && <p role="alert">Error: {createError}</p>}
          <div style={{ marginTop: '1rem', display: 'flex', gap: '0.5rem' }}>
            <button type="submit" disabled={submitting}>
              {submitting ? 'Saving…' : 'Save'}
            </button>
            <button type="button" onClick={closeModal} disabled={submitting}>
              Cancel
            </button>
          </div>
        </form>
      </dialog>
    </main>
  )
}