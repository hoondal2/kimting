export const TYPE_LABELS = {
  SCHEDULE: '일정', TODO: '할일', EMOTION: '감정',
  REFLECTION: '생각', PREFERENCE: '선호', RELATIONSHIP: '관계',
  FACT: '사실', CONVERSATION: '대화', EVENT: '이벤트',
}

export function formatMemDate(iso) {
  if (!iso) return { date: '', time: '' }
  const d = new Date(iso)
  const date = `${d.getFullYear()}.${String(d.getMonth() + 1).padStart(2, '0')}.${String(d.getDate()).padStart(2, '0')}`
  const time = `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
  return { date, time }
}

export function nowTime() {
  const d = new Date()
  return `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

export function todayLabel() {
  const d = new Date()
  return `${d.getFullYear()}.${String(d.getMonth() + 1).padStart(2, '0')}.${String(d.getDate()).padStart(2, '0')}`
}
