import { describe, it, expect, vi } from 'vitest'
import { streamChat } from './chat.js'

function fakeStreamResponse(chunks, ok = true) {
  const encoder = new TextEncoder()
  let i = 0
  return {
    ok,
    status: ok ? 200 : 500,
    body: {
      getReader() {
        return {
          read() {
            if (i >= chunks.length) return Promise.resolve({ done: true, value: undefined })
            const value = encoder.encode(chunks[i++])
            return Promise.resolve({ done: false, value })
          },
          releaseLock() {},
        }
      },
    },
  }
}

describe('streamChat', () => {
  it('yields each data chunk in order', async () => {
    global.fetch = vi.fn().mockResolvedValue(fakeStreamResponse([
      'data:Hello\n\n',
      'data: world\n\n',
    ]))

    const results = []
    for await (const chunk of streamChat('hi', {})) {
      results.push(chunk)
    }

    expect(results).toEqual(['Hello', ' world'])
  })

  // The exact scenario called out in the chat feature plan: a "data:" event split across two
  // ReadableStream read() chunks must still be reassembled correctly, not dropped or garbled.
  it('reassembles an event split across two read() chunks', async () => {
    global.fetch = vi.fn().mockResolvedValue(fakeStreamResponse([
      'data:Hel',
      'lo\n\n',
    ]))

    const results = []
    for await (const chunk of streamChat('hi', {})) {
      results.push(chunk)
    }

    expect(results).toEqual(['Hello'])
  })

  it('reassembles a chunk boundary landing inside the blank-line event separator', async () => {
    global.fetch = vi.fn().mockResolvedValue(fakeStreamResponse([
      'data:Hello\n',
      '\ndata:World\n\n',
    ]))

    const results = []
    for await (const chunk of streamChat('hi', {})) {
      results.push(chunk)
    }

    expect(results).toEqual(['Hello', 'World'])
  })

  it('throws when the response is not ok', async () => {
    global.fetch = vi.fn().mockResolvedValue(fakeStreamResponse([], false))

    await expect(async () => {
      for await (const _chunk of streamChat('hi', {})) { /* noop */ }
    }).rejects.toThrow('Chat request failed: 500')
  })

  it('sends the message body and merges the provided headers', async () => {
    global.fetch = vi.fn().mockResolvedValue(fakeStreamResponse([]))

    for await (const _chunk of streamChat('hello there', { Authorization: 'Bearer token' })) { /* noop */ }

    expect(global.fetch).toHaveBeenCalledWith('/api/chat', expect.objectContaining({
      method: 'POST',
      headers: expect.objectContaining({ Authorization: 'Bearer token', 'Content-Type': 'application/json' }),
      body: JSON.stringify({ message: 'hello there' }),
    }))
  })
})
