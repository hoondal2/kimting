import { useState } from 'react'

export default function LoginScreen({ onSuccess }) {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const res = await fetch('/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password }),
      })
      if (res.ok) {
        const data = await res.json()
        localStorage.setItem('token', data.token)
        onSuccess({ email, name: data.name ?? email.split('@')[0], token: data.token })
      } else {
        const data = await res.json().catch(() => ({}))
        setError(data.message ?? '로그인에 실패했어요.')
      }
    } catch {
      // auth 미구현 중 — 임시로 앱으로 진입
      onSuccess({ email, name: email.split('@')[0], token: null })
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="login-page">
      <div className="crt-overlay" />
      <div className="login-box">
        <div className="login-logo">
          <div className="login-logo-dot" />
          <div className="login-logo-text">kimting</div>
        </div>
        <div className="login-title">다시 만나서 반가워요</div>
        <div className="login-subtitle">이메일과 비밀번호로 로그인하세요.</div>
        <form className="login-form" onSubmit={handleSubmit}>
          <div>
            <div className="login-field-label">EMAIL</div>
            <input
              type="email" required className="login-input"
              value={email} onChange={e => setEmail(e.target.value)}
              placeholder="you@example.com"
            />
          </div>
          <div>
            <div className="login-field-label">PASSWORD</div>
            <input
              type="password" required className="login-input"
              value={password} onChange={e => setPassword(e.target.value)}
              placeholder="••••••••"
            />
          </div>
          {error && <div className="login-error">{error}</div>}
          <button type="submit" className="login-submit" disabled={loading}>
            {loading ? '로그인 중...' : '로그인'}
          </button>
        </form>
        <div className="login-footer">
          계정이 없으신가요?{' '}
          <a onClick={handleSubmit}>시작하기</a>
        </div>
      </div>
    </div>
  )
}
