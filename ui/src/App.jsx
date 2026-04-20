import { useState, useEffect, useCallback } from 'react'
import './App.css'

const API_BASE = '/xg-tsla-svc/api/v1/price'

function fmt(val, decimals = 2) {
  if (val == null) return '—'
  return Number(val).toFixed(decimals)
}

function fmtTime(ts) {
  return new Date(ts).toLocaleString('en-US', {
    timeZone: 'America/Los_Angeles',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
    hour12: false,
  })
}

export default function App() {
  const [prices, setPrices] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [view, setView] = useState('today')
  const [lastUpdated, setLastUpdated] = useState(null)

  const fetchPrices = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const url = view === 'today'
        ? `${API_BASE}/today`
        : `${API_BASE}/latest?limit=60`
      const res = await fetch(url)
      if (!res.ok) throw new Error(`HTTP ${res.status}`)
      const data = await res.json()
      setPrices(data)
      setLastUpdated(new Date())
    } catch (e) {
      setError(e.message)
    } finally {
      setLoading(false)
    }
  }, [view])

  useEffect(() => {
    fetchPrices()
    const interval = setInterval(fetchPrices, 60_000)
    return () => clearInterval(interval)
  }, [fetchPrices])

  const latest = prices.length > 0
    ? (view === 'today' ? prices[prices.length - 1] : prices[0])
    : null

  const sorted = [...prices].reverse()

  return (
    <div className="app">
      <header className="header">
        <div className="ticker">
          <span className="symbol">TSLA</span>
          {latest && (
            <>
              <span className="price">${fmt(latest.close)}</span>
              <span className="meta">VWAP ${fmt(latest.vwap)}</span>
            </>
          )}
        </div>
        <div className="controls">
          <div className="tabs">
            <button className={view === 'today' ? 'active' : ''} onClick={() => setView('today')}>Today</button>
            <button className={view === 'latest' ? 'active' : ''} onClick={() => setView('latest')}>Latest 60</button>
          </div>
          <button className="refresh-btn" onClick={fetchPrices} disabled={loading}>
            {loading ? '...' : 'Refresh'}
          </button>
        </div>
      </header>

      {lastUpdated && (
        <div className="updated">
          Updated {lastUpdated.toLocaleTimeString('en-US', { timeZone: 'America/Los_Angeles', hour12: false })} LA · auto-refreshes every 60s
        </div>
      )}

      {error && <div className="error">Error: {error}</div>}

      <div className="table-wrap">
        <table>
          <thead>
            <tr>
              <th>Time (LA)</th>
              <th>Open</th>
              <th>High</th>
              <th>Low</th>
              <th>Close</th>
              <th>Volume</th>
              <th>VWAP</th>
            </tr>
          </thead>
          <tbody>
            {sorted.map((q) => (
              <tr key={q.id}>
                <td>{fmtTime(q.timestamp)}</td>
                <td>{fmt(q.open)}</td>
                <td className="high">{fmt(q.high)}</td>
                <td className="low">{fmt(q.low)}</td>
                <td className="close">{fmt(q.close)}</td>
                <td>{q.volume?.toLocaleString() ?? '—'}</td>
                <td>{fmt(q.vwap)}</td>
              </tr>
            ))}
          </tbody>
        </table>
        {!loading && prices.length === 0 && !error && (
          <div className="empty">No data available</div>
        )}
      </div>
    </div>
  )
}
