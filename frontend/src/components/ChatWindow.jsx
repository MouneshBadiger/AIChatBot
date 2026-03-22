import { useState } from 'react'
import MessageList from './MessageList'
import ChatInput from './ChatInput'

const starterMessages = [
  {
    id: 1,
    role: 'assistant',
    content:
      'Welcome! This starter UI is ready for chat features once you connect your backend APIs.',
  },
]

function ChatWindow({ healthStatus, apiBaseUrl }) {
  const [messages, setMessages] = useState(starterMessages)
  const [isSending, setIsSending] = useState(false)
  const [chatError, setChatError] = useState(null)

  const isBackendReady = healthStatus.state === 'connected'
  const chatEndpoint = `${apiBaseUrl}/api/chat`

  const handleSendMessage = async (content) => {
    setChatError(null)
    setIsSending(true)

    // Optimistically add the user message
    setMessages((currentMessages) => [
      ...currentMessages,
      {
        id: currentMessages.length + 1,
        role: 'user',
        content,
      },
    ])

    try {
      const response = await fetch(chatEndpoint, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ message: content }),
      })

      const data = await response.json().catch(() => null)

      if (!response.ok || data?.error) {
        const message =
          data?.error?.message ||
          (response.ok ? 'Chat request failed.' : `Backend responded with ${response.status}`)
        throw new Error(message)
      }

      setMessages((currentMessages) => [
        ...currentMessages,
        {
          id: currentMessages.length + 1,
          role: 'assistant',
          content: data.reply || '(no reply)',
        },
      ])
    } catch (error) {
      setChatError(error.message || 'Unable to send message.')
      setMessages((currentMessages) => [
        ...currentMessages,
        {
          id: currentMessages.length + 1,
          role: 'assistant',
          content: 'Sorry — I could not get a reply from the AI service. Check the error above.',
        },
      ])
    } finally {
      setIsSending(false)
    }
  }

  return (
    <section className="chat-window">
      <div className="chat-window__meta">
        <div>
          <h2>Chat workspace</h2>
          <p>Backend URL: {apiBaseUrl}</p>
          <p>Chat endpoint: {chatEndpoint}</p>
        </div>
        <p>
          Health check:{' '}
          <strong>{healthStatus.details?.status || healthStatus.state.toUpperCase()}</strong>
        </p>
      </div>

      {chatError ? <p className="chat-error">{chatError}</p> : null}

      <MessageList messages={messages} />
      <ChatInput
        disabled={!isBackendReady || isSending}
        onSend={handleSendMessage}
      />
    </section>
  )
}

export default ChatWindow
