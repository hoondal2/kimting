import { TYPE_LABELS, formatMemDate } from '../utils'

export default function MemoryPanel({ usedMemories, recentMemories, onGoMemory }) {
  return (
    <aside className="memory-side-panel">
      <div className="panel-title">기억 패널</div>

      <div className="panel-section-header">
        사용된 기억
        <span className="panel-badge">{usedMemories.length}</span>
      </div>
      <div className="panel-mem-list">
        {usedMemories.length === 0
          ? <div className="panel-empty">없음</div>
          : usedMemories.map(m => <MemCard key={m.id} memory={m} />)
        }
      </div>

      <div className="panel-section-header">
        저장된 기억
        <span className="panel-badge dim">{recentMemories.length}</span>
      </div>
      <div className="panel-timeline">
        {recentMemories.length === 0
          ? <div className="panel-empty">없음</div>
          : recentMemories.slice(0, 5).map(m => <TimelineItem key={m.id} memory={m} />)
        }
      </div>

      <button className="panel-all-btn" onClick={onGoMemory}>
        전체 기억 보기 →
      </button>
    </aside>
  )
}

function MemCard({ memory }) {
  const { date, time } = formatMemDate(memory.occurredAt)
  return (
    <div className="panel-mem-card">
      <div className="panel-mem-meta">{date} · {time}</div>
      <div className="panel-mem-text">{memory.title}</div>
    </div>
  )
}

function TimelineItem({ memory }) {
  const { date } = formatMemDate(memory.occurredAt)
  return (
    <div className="panel-timeline-item">
      <div className="panel-timeline-dot" />
      <div className="panel-mem-meta">{date} · {TYPE_LABELS[memory.type] ?? memory.type}</div>
      <div className="panel-mem-text">{memory.title}</div>
    </div>
  )
}
