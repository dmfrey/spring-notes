import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import { vi, describe, it, expect, beforeEach } from 'vitest'
import ChatPanel from './ChatPanel'
import { streamChat } from './chat.js'

vi.mock('./AuthProvider.jsx', () => ({
  useAuth: () => ({ user: { access_token: 'test-token' } }),
}))

vi.mock('./chat.js', () => ({
  streamChat: vi.fn(),
}))

async function* fakeStream(chunks) {
  for (const chunk of chunks) {
    yield chunk
  }
}

describe('ChatPanel', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('renders a toggle button', () => {
    render(<ChatPanel />)
    expect(screen.getByRole('button', { name: /^chat$/i })).toBeInTheDocument()
  })

  it('opens the panel when the toggle button is clicked', () => {
    render(<ChatPanel />)
    fireEvent.click(screen.getByRole('button', { name: /^chat$/i }))
    expect(screen.getByRole('button', { name: /close chat panel/i })).toBeInTheDocument()
  })

  it('sends a message and streams the assistant response incrementally', async () => {
    streamChat.mockReturnValue(fakeStream(['Hello', ', ', 'world']))

    render(<ChatPanel />)
    fireEvent.click(screen.getByRole('button', { name: /^chat$/i }))

    fireEvent.change(screen.getByLabelText(/chat message/i), { target: { value: 'What did I write?' } })
    fireEvent.click(screen.getByRole('button', { name: /^send$/i }))

    expect(await screen.findByText('What did I write?')).toBeInTheDocument()
    await waitFor(() => expect(screen.getByText('Hello, world')).toBeInTheDocument())

    expect(streamChat).toHaveBeenCalledWith(
      'What did I write?',
      expect.objectContaining({ Authorization: 'Bearer test-token' })
    )
  })

  it('clears the input after sending', async () => {
    streamChat.mockReturnValue(fakeStream(['ok']))

    render(<ChatPanel />)
    fireEvent.click(screen.getByRole('button', { name: /^chat$/i }))

    const input = screen.getByLabelText(/chat message/i)
    fireEvent.change(input, { target: { value: 'hi' } })
    fireEvent.click(screen.getByRole('button', { name: /^send$/i }))

    await waitFor(() => expect(input).toHaveValue(''))
  })

  it('shows an error message when streaming fails', async () => {
    streamChat.mockImplementation(async function* () {
      throw new Error('boom')
    })

    render(<ChatPanel />)
    fireEvent.click(screen.getByRole('button', { name: /^chat$/i }))
    fireEvent.change(screen.getByLabelText(/chat message/i), { target: { value: 'hi' } })
    fireEvent.click(screen.getByRole('button', { name: /^send$/i }))

    expect(await screen.findByRole('alert')).toHaveTextContent('boom')
  })

  it('does not send an empty message', () => {
    render(<ChatPanel />)
    fireEvent.click(screen.getByRole('button', { name: /^chat$/i }))

    expect(screen.getByRole('button', { name: /^send$/i })).toBeDisabled()
  })
})
