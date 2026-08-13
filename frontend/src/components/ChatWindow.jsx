import { useState, useRef, useEffect } from 'react'
import MessageBubble from './MessageBubble'
import MemoryPanel from './MemoryPanel'
import AttachModal from './AttachModal'
import { nowTime, todayLabel } from '../utils'

const WELCOME = {
  id: 'init',
  role: 'assistant',
  content: '안녕하세요! 오늘 하루 있었던 일들을 편하게 이야기해 주세요. 기억해두고 필요할 때 알려드릴게요.',
  time: nowTime(),
  files: [],
  usedMemories: [],
}

export default function ChatWindow({ recentMemories, onMemoriesSaved, onGoMemory }) {
  const [messages, setMessages] = useState([WELCOME])
  const [composer, setComposer] = useState('')
  const [composerFiles, setComposerFiles] = useState([])
  const [loading, setLoading] = useState(false)
  const [showPanel, setShowPanel] = useState(true)
  const [attachOpen, setAttachOpen] = useState(false)
  const [lastUsedMemories, setLastUsedMemories] = useState([])
  const bottomRef = useRef(null)

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages, loading])

  async function send() {
    const text = composer.trim()
    if ((!text && composerFiles.length === 0) || loading) return

    const userMsg = {
      id: Date.now(),
      role: 'user',
      content: text || '(첨부 파일)',
      time: nowTime(),
      files: composerFiles,
      usedMemories: [],
    }
    setComposer('')
    setComposerFiles([])
    setMessages(prev => [...prev, userMsg])
    setLoading(true)

    try {
      const token = localStorage.getItem('token')
      const res = await fetch('/api/chat', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          ...(token ? { 'Authorization': `Bearer ${token}` } : {}),
        },
        body: JSON.stringify({ message: text }),
      })
      const data = await res.json()
      const usedMems = data.usedMemories ?? []
      const savedMems = data.savedMemories ?? []

      if (!res.ok) {
        setMessages(prev => [...prev, {
          id: Date.now(), role: 'assistant',
          content: `오류: ${data.message ?? res.status}`,
          time: nowTime(), files: [], usedMemories: [],
        }])
        return
      }

      setMessages(prev => [...prev, {
        id: Date.now(), role: 'assistant',
        content: data.response,
        time: nowTime(), files: [], usedMemories: usedMems,
      }])
      setLastUsedMemories(usedMems)
      if (savedMems.length > 0) onMemoriesSaved?.()
    } catch {
      setMessages(prev => [...prev, {
        id: Date.now(), role: 'assistant',
        content: '서버와 연결할 수 없어요.',
        time: nowTime(), files: [], usedMemories: [],
      }])
    } finally {
      setLoading(false)
    }
  }

  function onKeyDown(e) {
    if (e.key === 'Enter' && !e.shiftKey) { e.preventDefault(); send() }
  }

  function removeComposerFile(id) {
    setComposerFiles(prev => prev.filter(f => f.id !== id))
  }

  const panelActive = showPanel

  return (
    <div className="chat-view">
      <div className="chat-main">
        <div className="chat-date-bar">
          <span className="chat-date-text">{todayLabel()} — 오늘의 대화</span>
          <button
            className="panel-toggle-btn"
            onClick={() => setShowPanel(p => !p)}
            title="기억 패널 열기/닫기"
            style={{ background: panelActive ? 'var(--accent)' : 'var(--surface2)' }}
          >
            <div
              className="panel-toggle-icon"
              style={{ border: `1.5px solid ${panelActive ? 'var(--accent-text)' : 'var(--muted)'}` }}
            >
              <div
                className="panel-toggle-bar"
                style={{ background: panelActive ? 'var(--accent-text)' : 'var(--muted)' }}
              />
            </div>
          </button>
        </div>

        <div className="messages-list">
          {messages.map(m => (
            <MessageBubble
              key={m.id}
              message={m}
              showInline={!showPanel}
            />
          ))}
          {loading && (
            <div className="msg-row assistant">
              <div className="msg-bubble-ai">
                <div className="msg-loading">···</div>
              </div>
            </div>
          )}
          <div ref={bottomRef} />
        </div>

        <div className="composer-area">
          {composerFiles.length > 0 && (
            <div className="composer-files">
              {composerFiles.map(f => (
                <div key={f.id} className="composer-file-chip">
                  <div className="file-icon" />
                  <span className="composer-file-name">{f.name}</span>
                  <button className="remove-btn" onClick={() => removeComposerFile(f.id)}>×</button>
                </div>
              ))}
            </div>
          )}
          <div className="composer-row">
            <button className="attach-btn" onClick={() => setAttachOpen(true)} title="파일 첨부">＋</button>
            <textarea
              className="composer-textarea"
              rows={2}
              value={composer}
              onChange={e => setComposer(e.target.value)}
              onKeyDown={onKeyDown}
              placeholder="메시지 입력 (Enter: 전송, Shift+Enter: 줄바꿈)"
              disabled={loading}
            />
            <button
              className="send-btn"
              onClick={send}
              disabled={loading || (!composer.trim() && composerFiles.length === 0)}
              title="전송"
            >
              →
            </button>
          </div>
        </div>
      </div>

      {showPanel && (
        <MemoryPanel
          usedMemories={lastUsedMemories}
          recentMemories={recentMemories}
          onGoMemory={onGoMemory}
        />
      )}

      {attachOpen && (
        <AttachModal
          onConfirm={files => setComposerFiles(prev => [...prev, ...files])}
          onClose={() => setAttachOpen(false)}
        />
      )}
    </div>
  )
}
