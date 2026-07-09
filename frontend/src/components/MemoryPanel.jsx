export default function MemoryPanel({ memories }) {
  const { used = [], saved = [] } = memories ?? {}

  return (
    <aside className="memory-panel">
      <h2>기억 패널</h2>

      <section>
        <h3>사용된 기억 <span className="badge">{used.length}</span></h3>
        {used.length === 0
          ? <p className="empty">없음</p>
          : used.map(m => <MemoryCard key={m.id} memory={m} />)
        }
      </section>

      <section>
        <h3>저장된 기억 <span className="badge new">{saved.length}</span></h3>
        {saved.length === 0
          ? <p className="empty">없음</p>
          : saved.map(m => <MemoryCard key={m.id} memory={m} highlight />)
        }
      </section>
    </aside>
  )
}

function MemoryCard({ memory, highlight }) {
  return (
    <div className={`memory-card ${highlight ? 'highlight' : ''}`}>
      <span className="memory-type">{memory.type}</span>
      <p className="memory-title">{memory.title}</p>
      {memory.occurredAt && (
        <span className="memory-date">{formatDate(memory.occurredAt)}</span>
      )}
    </div>
  )
}

function formatDate(iso) {
  if (!iso) return ''
  const d = new Date(iso)
  return `${d.getMonth() + 1}/${d.getDate()} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}
