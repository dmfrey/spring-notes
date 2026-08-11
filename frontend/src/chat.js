// Native EventSource can't send the Authorization header this app needs, so this hand-rolls
// SSE parsing over a fetch() ReadableStream instead of using EventSource.
export async function* streamChat(message, headers) {
  const response = await fetch('/api/chat', {
    method: 'POST',
    headers: { ...headers, 'Content-Type': 'application/json' },
    body: JSON.stringify({ message }),
  })

  if (!response.ok) throw new Error(`Chat request failed: ${response.status}`)
  if (!response.body) throw new Error('Chat response has no body')

  const reader = response.body.getReader()
  const decoder = new TextDecoder()
  let buffer = ''

  try {
    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      buffer += decoder.decode(value, { stream: true })

      // SSE events are separated by a blank line ("\n\n"). A read() chunk boundary can land
      // in the middle of an event, so only complete events (split on "\n\n") are parsed here -
      // whatever's left after the last one is held back in buffer for the next read().
      const events = buffer.split('\n\n')
      buffer = events.pop() ?? ''

      for (const event of events) {
        for (const line of event.split('\n')) {
          if (line.startsWith('data:')) {
            yield line.slice('data:'.length)
          }
        }
      }
    }
  } finally {
    reader.releaseLock()
  }
}
