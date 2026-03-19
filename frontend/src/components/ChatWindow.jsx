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

  const handleSendMessage = (content) => {
    setMessages((currentMessages) => [
      ...currentMessages,
      {
        id: currentMessages.length + 1,
        role: 'user',
        content,
      },
    ])
  }

  return (
    <section className="chat-window">
      <div className="chat-window__meta">
        <div>
          <h2>Chat workspace</h2>
          <p>Backend URL: {apiBaseUrl}</p>
        </div>
        <p>
          Health check:{' '}
          <strong>{healthStatus.details?.status || healthStatus.state.toUpperCase()}</strong>
        </p>
      </div>

      <MessageList messages={messages} />
      <ChatInput disabled={healthStatus.state === 'loading'} onSend={handleSendMessage} />
    </section>
  )
}

export default ChatWindow
