import { useState } from 'react'
import { TYPE_LABELS, formatMemDate } from '../utils'

const FILTERS = [
  { key: 'all', label: '전체' },
  { key: 'SCHEDULE', label: '일정' },
  { key: 'TODO', label: '할일' },
  { key: 'EMOTION', label: '감정' },
  { key: 'REFLECTION', label: '생각' },
  { key: 'PREFERENCE', label: '선호' },
]

export default function MemoryView({ memories, loading, onDelete }) {
  const [search, setSearch] = useState('')
  const [filter, setFilter] = useState('all')

  const filtered = memories
    .filter(m => filter === 'all' || m.type === filter)
    .filter(m => {
      const q = search.trim().toLowerCase()
      return !q || (m.title ?? '').toLowerCase().includes(q) || (m.content ?? '').toLowerCase().includes(q)
    })

  return (
    <div className="memory-view">
      <div className="memory-view-inner">
        <div className="view-title">기억 관리</div>
        <div className="view-subtitle">kimting이 저장한 모든 기억을 검색하고 정리하세요.</div>

        <input
          className="memory-search"
          value={search}
          onChange={e => setSearch(e.target.value)}
          placeholder="기억 검색..."
        />

        <div className="filter-row">
          {FILTERS.map(f => (
            <button
              key={f.key}
              className={`filter-btn${filter === f.key ? ' active' : ''}`}
              onClick={() => setFilter(f.key)}
            >
              {f.label}
            </button>
          ))}
        </div>

        {loading ? (
          <div className="memory-loading">불러오는 중...</div>
        ) : (
          <div className="memory-list">
            {filtered.length === 0 ? (
              <div className="memory-empty">일치하는 기억이 없어요.</div>
            ) : (
              filtered.map(m => (
                <MemoryListItem key={m.id} memory={m} onDelete={onDelete} />
              ))
            )}
          </div>
        )}
      </div>
    </div>
  )
}

function MemoryListItem({ memory, onDelete }) {
  const { date, time } = formatMemDate(memory.occurredAt)
  return (
    <div className="memory-list-item">
      <div className="mem-dot" />
      <div className="mem-body">
        <div className="mem-meta">{date} · {time}</div>
        <div className="mem-content">{memory.title}</div>
      </div>
      <div className="mem-category-tag">{TYPE_LABELS[memory.type] ?? memory.type}</div>
      <button className="mem-delete-btn" onClick={() => onDelete(memory.id)} title="삭제">×</button>
    </div>
  )
}
