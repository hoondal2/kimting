import { useState, useRef, useEffect } from 'react'
import MessageBubble from './MessageBubble'

export default function ChatWindow({ onMemoriesUpdate }) {
  const [messages, setMessages] = useState([])
  const [input, setInput] = useState('')
  const [loading, setLoading] = useState(false)
  const bottomRef = useRef(null)

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages])

  async function send() {
    const text = input.trim()
    if (!text || loading) return

    setInput('')
    setMessages(prev => [...prev, { role: 'user', content: text }])
    setLoading(true)

    try {
      const res = await fetch('/api/chat', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ message: text }),
      })
      const data = await res.json()

      if (!res.ok) {
        setMessages(prev => [...prev, { role: 'assistant', content: `오류: ${data.message ?? res.status}` }])
        return
      }

      setMessages(prev => [...prev, { role: 'assistant', content: data.response }])
      onMemoriesUpdate?.({
        used: data.usedMemories ?? [],
        saved: data.savedMemories ?? [],
      })
    } catch (e) {
      setMessages(prev => [...prev, { role: 'assistant', content: '서버와 연결할 수 없어요.' }])
    } finally {
      setLoading(false)
    }
  }

  function onKeyDown(e) {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault()
      send()
    }
  }

  return (
    <div className="chat-window">
      <div className="messages">
        {messages.length === 0 && (
          <p className="empty-hint">안녕하세요! 무슨 일이 있었는지 이야기해 주세요.</p>
        )}
        {messages.map((m, i) => <MessageBubble key={i} message={m} />)}
        {loading && (
          <div className="bubble-row assistant">
            <div className="bubble loading">···</div>
          </div>
        )}
        <div ref={bottomRef} />
      </div>

      <div className="input-row">
        <textarea
          rows={2}
          value={input}
          onChange={e => setInput(e.target.value)}
          onKeyDown={onKeyDown}
          placeholder="메시지 입력 (Enter: 전송, Shift+Enter: 줄바꿈)"
          disabled={loading}
        />
        <button onClick={send} disabled={loading || !input.trim()}>전송</button>
      </div>
    </div>
  )
}
