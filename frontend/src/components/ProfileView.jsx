export default function ProfileView({ user, dark, onToggleDark, onLogout, memoriesCount }) {
  const initial = user?.name ? user.name.charAt(0).toUpperCase() : '?'

  return (
    <div className="profile-view">
      <div className="profile-view-inner">
        <div className="profile-header">
          <div className="profile-avatar">{initial}</div>
          <div>
            <div className="profile-name">{user?.name ?? '사용자'}</div>
            <div className="profile-email">{user?.email ?? ''}</div>
          </div>
        </div>

        <div className="profile-stats">
          <div className="stat-block">
            <div className="stat-value">{memoriesCount}</div>
            <div className="stat-label">저장된 기억</div>
          </div>
          <div className="stat-block">
            <div className="stat-value neutral">—</div>
            <div className="stat-label">대화 일수</div>
          </div>
          <div className="stat-block">
            <div className="stat-value date-style neutral">—</div>
            <div className="stat-label">가입일</div>
          </div>
        </div>

        <div className="profile-settings">
          <div className="setting-row">
            <div className="setting-label">다크 모드</div>
            <button className="dark-toggle" onClick={onToggleDark} title="테마 전환">
              <div className="dark-toggle-knob" style={{ left: dark ? '22px' : '2px' }} />
            </button>
          </div>
          <div className="setting-row">
            <div className="setting-label">알림</div>
            <div className="setting-value">준비 중</div>
          </div>
          <div className="setting-row">
            <div className="setting-label">이메일 인증</div>
            <div className="setting-value">준비 중</div>
          </div>
          <div className="setting-row">
            <div className="setting-label">비밀번호 변경</div>
            <div className="setting-value">준비 중</div>
          </div>
          <div className="setting-row clickable">
            <div className="setting-label">데이터 내보내기</div>
            <div className="setting-value">→</div>
          </div>
          <div className="setting-row clickable" onClick={onLogout}>
            <div className="setting-label danger">로그아웃</div>
          </div>
        </div>
      </div>
    </div>
  )
}
