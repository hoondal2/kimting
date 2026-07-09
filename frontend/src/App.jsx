import { useState } from 'react'
import ChatWindow from './components/ChatWindow'
import MemoryPanel from './components/MemoryPanel'
import './index.css'

export default function App() {
  const [memories, setMemories] = useState({ used: [], saved: [] })

  return (
    <div className="app">
      <header className="app-header">
        <span className="logo">kimting</span>
        <span className="tagline">당신의 기억을 기억하는 AI</span>
      </header>
      <main className="app-body">
        <ChatWindow onMemoriesUpdate={setMemories} />
        <MemoryPanel memories={memories} />
      </main>
    </div>
  )
}
