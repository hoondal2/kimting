import { useState } from 'react'
import { TYPE_LABELS, formatMemDate } from '../utils'

export default function MessageBubble({ message, showInline }) {
  const [inlineOpen, setInlineOpen] = useState(false)
  const isUser = message.role === 'user'
  const usedMems = message.usedMemories ?? []

  return (
    <div className={`msg-row ${isUser ? 'user' : 'assistant'}`}>
      <div className={isUser ? 'msg-bubble-user' : 'msg-bubble-ai'}>
        <div className={`msg-text${isUser ? '' : ' ai'}`}>{message.content}</div>

        {message.files?.length > 0 && (
          <div className="msg-files">
            {message.files.map((f, i) => (
              <div key={i} className="msg-file-chip">
                <div className="file-icon" />
                {f.name}
              </div>
            ))}
          </div>
        )}

        <div className="msg-time">{message.time}</div>

        {!isUser && showInline && usedMems.length > 0 && (
          <>
            <button className="inline-mem-btn" onClick={() => setInlineOpen(o => !o)}>
              연결된 기억 {usedMems.length}개 {inlineOpen ? '▾' : '▸'}
            </button>
            {inlineOpen && (
              <div className="inline-mem-list">
                {usedMems.map(m => {
                  const { date, time } = formatMemDate(m.occurredAt)
                  return (
                    <div key={m.id} className="inline-mem-item">
                      <div className="inline-mem-dot" />
                      <div>
                        <div className="inline-mem-meta">{date} · {time} · {TYPE_LABELS[m.type] ?? m.type}</div>
                        <div className="inline-mem-text">{m.title}</div>
                      </div>
                    </div>
                  )
                })}
              </div>
            )}
          </>
        )}
      </div>
    </div>
  )
}
