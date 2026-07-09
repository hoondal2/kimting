import { useState, useEffect, useRef } from 'react'

const PHRASE = '기억하고 싶은 내용을 입력해주세요'

export default function OnboardingScreen({ onStart }) {
  const [typedLen, setTypedLen] = useState(0)
  const phaseRef = useRef('typing')
  const counterRef = useRef(0)

  useEffect(() => {
    const timer = setInterval(() => {
      const phase = phaseRef.current
      if (phase === 'typing') {
        setTypedLen(len => {
          const next = len + 1
          if (next >= PHRASE.length) {
            phaseRef.current = 'pause'
            counterRef.current = 0
            return PHRASE.length
          }
          return next
        })
      } else if (phase === 'pause') {
        counterRef.current++
        if (counterRef.current > 16) {
          phaseRef.current = 'erasing'
          counterRef.current = 0
        }
      } else if (phase === 'erasing') {
        setTypedLen(len => {
          const next = len - 1
          if (next <= 0) {
            phaseRef.current = 'pause2'
            counterRef.current = 0
            return 0
          }
          return next
        })
      } else {
        counterRef.current++
        if (counterRef.current > 6) {
          phaseRef.current = 'typing'
          counterRef.current = 0
        }
      }
    }, 90)
    return () => clearInterval(timer)
  }, [])

  return (
    <div className="onboarding">
      <div className="crt-overlay" />
      <div className="onboarding-logo">
        <div className="onboarding-logo-dot" />
        <div className="onboarding-logo-text">kimting</div>
      </div>
      <div className="onboarding-body">
        <div className="onboarding-headline">
          말하는 순간,<br />
          <span className="onboarding-headline-accent">기억이 됩니다.</span>
        </div>
        <div className="terminal-box">
          <div className="terminal-label">KIMTING_TERMINAL v1.0</div>
          <div className="terminal-text">
            &gt; {PHRASE.slice(0, typedLen)}<span className="terminal-cursor">▊</span>
          </div>
        </div>
        <div className="onboarding-ctas">
          <button className="start-btn" onClick={onStart}>시작하기</button>
          <button className="login-link-btn" onClick={onStart}>이미 계정이 있어요</button>
        </div>
      </div>
    </div>
  )
}
