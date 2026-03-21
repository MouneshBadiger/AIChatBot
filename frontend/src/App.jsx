import { useEffect, useMemo, useState } from 'react'
import ChatWindow from './components/ChatWindow'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8081'

function App() {
  const [healthStatus, setHealthStatus] = useState({
    state: 'loading',
    message: 'Checking backend connectivity...',
    details: null,
  })

  const healthEndpoint = useMemo(() => `${API_BASE_URL}/api/health`, [])

  useEffect(() => {
    const controller = new AbortController()

    async function fetchHealth() {
      try {
        const response = await fetch(healthEndpoint, { signal: controller.signal })
        if (!response.ok) {
          throw new Error(`Backend responded with status ${response.status}`)
        }

        const data = await response.json()
        setHealthStatus({
          state: 'connected',
          message: data.message || 'Backend is reachable.',
          details: data,
        })
      } catch (error) {
        if (error.name === 'AbortError') {
          return
        }

        setHealthStatus({
          state: 'error',
          message: error.message || 'Unable to reach backend.',
          details: null,
        })
      }
    }

    fetchHealth()

    return () => controller.abort()
  }, [healthEndpoint])

  return (
    <main className="app-shell">
      <header className="app-header">
        <div>
          <p className="eyebrow">AI ChatBot</p>
          <h1>React + Spring Boot starter</h1>
        </div>
        <div className={`status-pill status-pill--${healthStatus.state}`}>
          <span className="status-dot" />
          <span>{healthStatus.message}</span>
        </div>
      </header>

      <ChatWindow healthStatus={healthStatus} apiBaseUrl={API_BASE_URL} />
    </main>
  )
}

export default App
