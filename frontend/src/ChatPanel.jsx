import { useEffect, useRef, useState } from 'react'
import { useAuth } from './AuthProvider.jsx'
import { streamChat } from './chat.js'

export default function ChatPanel() {
  const { user } = useAuth()
  const [open, setOpen] = useState(false)
  const [messages, setMessages] = useState([])
  const [input, setInput] = useState('')
  const [sending, setSending] = useState(false)
  const [error, setError] = useState(null)
  const messagesEndRef = useRef(null)

  const authHeaders = {
    'API-Version': '1',
    'Authorization': `Bearer ${user.access_token}`,
  }

  useEffect(() => {
    // jsdom (used by the component test suite) doesn't implement scrollIntoView.
    messagesEndRef.current?.scrollIntoView?.({ behavior: 'smooth' })
  }, [messages])

  async function handleSend(e) {
    e.preventDefault()
    const text = input.trim()
    if (!text || sending) return

    setInput('')
    setError(null)
    setMessages((prev) => [...prev, { role: 'user', text }, { role: 'assistant', text: '' }])
    setSending(true)

    try {
      for await (const chunk of streamChat(text, authHeaders)) {
        setMessages((prev) => {
          const next = [...prev]
          next[next.length - 1] = { role: 'assistant', text: next[next.length - 1].text + chunk }
          return next
        })
      }
    } catch (err) {
      setError(err.message)
    } finally {
      setSending(false)
    }
  }

  return (
    <>
      <button className="chat-toggle" onClick={() => setOpen((o) => !o)}>
        {open ? 'Close chat' : 'Chat'}
      </button>

      <aside className={`chat-panel${open ? ' chat-panel--open' : ''}`} aria-hidden={!open}>
        <div className="chat-panel__header">
          <h2>Chat about your notes</h2>
          <button onClick={() => setOpen(false)} aria-label="Close chat panel">×</button>
        </div>

        <div className="chat-panel__messages">
          {messages.length === 0 && <p className="chat-panel__empty">Ask a question about your notes.</p>}
          {messages.map((m, i) => (
            <div key={i} className={`chat-message chat-message--${m.role}`}>
              {m.text}
            </div>
          ))}
          <div ref={messagesEndRef} />
        </div>

        {error && <p role="alert" className="chat-panel__error">Error: {error}</p>}

        <form className="chat-panel__form" onSubmit={handleSend}>
          <input
            type="text"
            value={input}
            onChange={(e) => setInput(e.target.value)}
            placeholder="Ask about your notes…"
            disabled={sending}
            aria-label="Chat message"
          />
          <button type="submit" disabled={sending || !input.trim()}>
            {sending ? 'Sending…' : 'Send'}
          </button>
        </form>
      </aside>
    </>
  )
}
