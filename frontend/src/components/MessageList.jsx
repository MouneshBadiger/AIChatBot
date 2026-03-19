function MessageList({ messages }) {
  return (
    <div className="message-list" aria-live="polite">
      {messages.map((message) => (
        <article
          className={`message-bubble message-bubble--${message.role}`}
          key={message.id}
        >
          <span className="message-role">{message.role}</span>
          <p>{message.content}</p>
        </article>
      ))}
    </div>
  )
}

export default MessageList
