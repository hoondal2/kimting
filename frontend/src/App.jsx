import { useState, useEffect, useCallback } from 'react'
import OnboardingScreen from './components/OnboardingScreen'
import LoginScreen from './components/LoginScreen'
import TopBar from './components/TopBar'
import ChatWindow from './components/ChatWindow'
import MemoryView from './components/MemoryView'
import ProfileView from './components/ProfileView'

export default function App() {
  const [screen, setScreen] = useState('onboarding')
  const [subview, setSubview] = useState('chat')
  const [dark, setDark] = useState(true)
  const [user, setUser] = useState(null)
  const [allMemories, setAllMemories] = useState([])
  const [memoriesLoading, setMemoriesLoading] = useState(false)

  useEffect(() => {
    document.documentElement.setAttribute('data-theme', dark ? 'dark' : 'light')
  }, [dark])

  const fetchMemories = useCallback(async () => {
    setMemoriesLoading(true)
    try {
      const headers = {}
      const token = localStorage.getItem('token')
      if (token) headers['Authorization'] = `Bearer ${token}`
      const res = await fetch('/api/memories', { headers })
      if (res.ok) {
        const data = await res.json()
        setAllMemories(Array.isArray(data) ? data : data.content ?? [])
      }
    } catch {
      // ignore — no connection or auth not yet implemented
    } finally {
      setMemoriesLoading(false)
    }
  }, [])

  useEffect(() => {
    if (screen === 'app') fetchMemories()
  }, [screen, fetchMemories])

  async function deleteMemory(id) {
    setAllMemories(prev => prev.filter(m => m.id !== id))
    try {
      const headers = {}
      const token = localStorage.getItem('token')
      if (token) headers['Authorization'] = `Bearer ${token}`
      await fetch(`/api/memories/${id}`, { method: 'DELETE', headers })
    } catch {
      // local optimistic delete already applied
    }
  }

  function handleLogin(userData) {
    setUser(userData)
    setScreen('app')
    setSubview('chat')
  }

  function handleLogout() {
    setUser(null)
    localStorage.removeItem('token')
    setScreen('login')
    setAllMemories([])
  }

  if (screen === 'onboarding') {
    return <OnboardingScreen onStart={() => setScreen('login')} />
  }

  if (screen === 'login') {
    return <LoginScreen onSuccess={handleLogin} />
  }

  return (
    <div className="app">
      <div className="crt-overlay" />
      <TopBar
        subview={subview}
        onSubview={setSubview}
        dark={dark}
        onToggleDark={() => setDark(d => !d)}
        user={user}
      />
      {subview === 'chat' && (
        <ChatWindow
          recentMemories={allMemories.slice(0, 5)}
          onMemoriesSaved={fetchMemories}
          onGoMemory={() => setSubview('memory')}
        />
      )}
      {subview === 'memory' && (
        <MemoryView
          memories={allMemories}
          loading={memoriesLoading}
          onDelete={deleteMemory}
        />
      )}
      {subview === 'profile' && (
        <ProfileView
          user={user}
          dark={dark}
          onToggleDark={() => setDark(d => !d)}
          onLogout={handleLogout}
          memoriesCount={allMemories.length}
        />
      )}
    </div>
  )
}
