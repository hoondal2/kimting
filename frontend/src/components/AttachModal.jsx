import { useRef, useState } from 'react'

function formatSize(bytes) {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return Math.round(bytes / 1024) + ' KB'
  return (bytes / 1024 / 1024).toFixed(1) + ' MB'
}

export default function AttachModal({ onConfirm, onClose }) {
  const [pendingFiles, setPendingFiles] = useState([])
  const fileInputRef = useRef(null)

  function onFilesSelected(e) {
    const files = Array.from(e.target.files || []).map((f, i) => ({
      id: 'f' + Date.now() + i,
      name: f.name,
      size: f.size,
      file: f,
    }))
    setPendingFiles(prev => [...prev, ...files])
    e.target.value = ''
  }

  function removeFile(id) {
    setPendingFiles(prev => prev.filter(f => f.id !== id))
  }

  return (
    <div className="modal-overlay" onClick={e => e.target === e.currentTarget && onClose()}>
      <div className="modal-box">
        <div className="modal-header">
          <div className="modal-title">파일 첨부</div>
          <button className="modal-close" onClick={onClose}>×</button>
        </div>

        <div className="file-drop-zone" onClick={() => fileInputRef.current?.click()}>
          <div className="file-drop-icon">＋</div>
          <div className="file-drop-label">클릭하여 파일을 선택하세요</div>
          <div className="file-drop-hint">PDF · DOCX · TXT 지원</div>
        </div>
        <input
          ref={fileInputRef} type="file" multiple
          onChange={onFilesSelected} style={{ display: 'none' }}
          accept=".pdf,.docx,.txt"
        />

        {pendingFiles.length > 0 && (
          <div className="pending-files">
            {pendingFiles.map(f => (
              <div key={f.id} className="pending-file">
                <div className="pending-file-icon" />
                <div className="pending-file-info">
                  <div className="pending-file-name">{f.name}</div>
                  <div className="pending-file-size">{formatSize(f.size)}</div>
                </div>
                <button className="remove-btn" onClick={() => removeFile(f.id)}>×</button>
              </div>
            ))}
          </div>
        )}

        <div className="modal-actions">
          <button className="modal-cancel-btn" onClick={onClose}>취소</button>
          <button
            className="modal-confirm-btn"
            disabled={pendingFiles.length === 0}
            onClick={() => { onConfirm(pendingFiles); onClose() }}
          >
            첨부하기
          </button>
        </div>
      </div>
    </div>
  )
}
