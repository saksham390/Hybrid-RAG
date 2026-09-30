import { useEffect, useState } from 'react'

function App() {
  const [documents, setDocuments] = useState([])
  const [selectedFile, setSelectedFile] = useState(null)
  const [question, setQuestion] = useState('')
  const [answer, setAnswer] = useState(null)
  const [busy, setBusy] = useState(false)
  const [message, setMessage] = useState('')

  async function loadDocuments() {
    const response = await fetch('/api/documents')
    if (response.ok) setDocuments(await response.json())
  }

  useEffect(() => {
    loadDocuments().catch(() => setMessage('Start the Spring Boot backend to load documents.'))
  }, [])

  async function upload(event) {
    event.preventDefault()
    if (!selectedFile) return
    setBusy(true)
    setMessage('')
    const body = new FormData()
    body.append('file', selectedFile)
    const response = await fetch('/api/documents/upload', { method: 'POST', body })
    const data = await response.json()
    setBusy(false)
    if (!response.ok) return setMessage(data.message || 'Upload failed.')
    setMessage(`${data.filename} is ready. ${data.chunks} chunks indexed.`)
    setSelectedFile(null)
    event.target.reset()
    loadDocuments()
  }

  async function ask(event) {
    event.preventDefault()
    if (!question.trim()) return
    setBusy(true)
    setMessage('')
    const response = await fetch('/api/chat/rag', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ question })
    })
    const data = await response.json()
    setBusy(false)
    if (!response.ok) return setMessage(data.message || 'Question failed.')
    setAnswer(data)
  }

  return (
    <main className="shell">
      <header className="masthead">
        <p className="eyebrow">Enterprise knowledge assistant</p>
        <h1>Ask the documents.</h1>
        <p className="lede">Upload a PDF, then get answers grounded in its pages.</p>
      </header>

      <section className="workspace">
        <aside className="sidebar">
          <div className="section-heading">
            <h2>Library</h2>
            <span>{documents.length}</span>
          </div>
          <form className="upload" onSubmit={upload}>
            <label htmlFor="pdf">PDF document</label>
            <input id="pdf" type="file" accept="application/pdf,.pdf" onChange={(event) => setSelectedFile(event.target.files[0])} />
            <button disabled={busy || !selectedFile}>{busy ? 'Working...' : 'Upload document'}</button>
          </form>
          <div className="document-list">
            {documents.length === 0 && <p className="muted">No documents uploaded yet.</p>}
            {documents.map((document) => (
              <article className="document" key={document.documentId}>
                <strong>{document.filename}</strong>
                <small>Added {new Date(document.uploadedAt).toLocaleDateString()}</small>
              </article>
            ))}
          </div>
        </aside>

        <section className="chat-panel">
          <div className="section-heading">
            <h2>Conversation</h2>
            <span className="status-dot">Ready</span>
          </div>
          {!answer && <div className="empty-state"><span>01</span><h3>Your documents, made searchable.</h3><p>Ask about policies, procedures, or anything in the uploaded PDFs.</p></div>}
          {answer && <div className="answer"><p className="answer-label">Assistant</p><p className="answer-text">{answer.answer}</p><div className="citations"><p className="answer-label">Sources</p>{answer.citations.map((citation) => <div className="citation" key={citation.chunkId}><strong>{citation.document}</strong><span>Page {citation.page}</span></div>)}</div></div>}
          <form className="question-form" onSubmit={ask}>
            <input value={question} onChange={(event) => setQuestion(event.target.value)} placeholder="What would you like to know?" />
            <button disabled={busy || !question.trim()} aria-label="Ask question">Ask</button>
          </form>
          {message && <p className="message">{message}</p>}
        </section>
      </section>
    </main>
  )
}

export default App
