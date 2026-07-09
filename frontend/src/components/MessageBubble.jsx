export default function MessageBubble({ message }) {
  const isUser = message.role === 'user'
  return (
    <div className={`bubble-row ${isUser ? 'user' : 'assistant'}`}>
      <div className="bubble">
        {message.content}
      </div>
    </div>
  )
}
