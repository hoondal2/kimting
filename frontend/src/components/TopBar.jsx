export default function TopBar({ subview, onSubview, dark, onToggleDark, user }) {
  const initial = user?.name ? user.name.charAt(0).toUpperCase() : '?'

  return (
    <div className="topbar">
      <div className="topbar-left">
        <div className="topbar-logo">
          <div className="topbar-logo-dot" />
          <div className="topbar-logo-text">kimting</div>
        </div>
        <nav className="topbar-nav">
          {['chat', 'memory', 'profile'].map(v => (
            <button
              key={v}
              className={`topbar-nav-btn${subview === v ? ' active' : ''}`}
              onClick={() => onSubview(v)}
            >
              {v === 'chat' ? '채팅' : v === 'memory' ? '기억' : '프로필'}
            </button>
          ))}
        </nav>
      </div>
      <div className="topbar-right">
        <button className="dark-toggle" onClick={onToggleDark} title={dark ? '라이트 모드' : '다크 모드'}>
          <div className="dark-toggle-knob" style={{ left: dark ? '22px' : '2px' }} />
        </button>
        <button className="avatar-btn" onClick={() => onSubview('profile')} title="프로필">
          {initial}
        </button>
      </div>
    </div>
  )
}
