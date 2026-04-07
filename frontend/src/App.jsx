import { useState, useEffect, useCallback } from 'react'

const API_HEADERS = { 'API-Version': '1' }

async function fetchNotes(cursor, limit = 25) {
  const params = new URLSearchParams({ limit })
  if (cursor) params.set('cursor', cursor)

  const response = await fetch(`/api/notes?${params}`, { headers: API_HEADERS })
  if (!response.ok) throw new Error(`Failed to fetch notes: ${response.status}`)
  return response.json()
}

export default function App() {
  const [notes, setNotes] = useState([])
  const [cursor, setCursor] = useState(null)
  const [hasMore, setHasMore] = useState(false)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  const loadNotes = useCallback(async (nextCursor) => {
    setLoading(true)
    setError(null)
    try {
      const data = await fetchNotes(nextCursor)
      setNotes((prev) => nextCursor ? [...prev, ...data.notes] : data.notes)
      setCursor(data.nextCursor ?? null)
      setHasMore(!!data.nextCursor)
    } catch (e) {
      setError(e.message)
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    loadNotes(null)
  }, [loadNotes])

  return (
    <main>
      <h1>Notes</h1>

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
    </main>
  )
}