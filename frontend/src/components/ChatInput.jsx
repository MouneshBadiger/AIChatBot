import { useState } from 'react'

function ChatInput({ disabled, onSend }) {
  const [value, setValue] = useState('')

  const handleSubmit = (event) => {
    event.preventDefault()
    const trimmedValue = value.trim()

    if (!trimmedValue) {
      return
    }

    onSend(trimmedValue)
    setValue('')
  }

  return (
    <form className="chat-input" onSubmit={handleSubmit}>
      <label className="sr-only" htmlFor="chat-message">
        Type your message
      </label>
      <input
        id="chat-message"
        type="text"
        placeholder="Ask something..."
        value={value}
        onChange={(event) => setValue(event.target.value)}
        disabled={disabled}
      />
      <button type="submit" disabled={disabled || !value.trim()}>
        Send
      </button>
    </form>
  )
}

export default ChatInput
