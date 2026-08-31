import { useEffect, useState } from 'react'
import Topbar from '../components/Topbar'
import AlertBanner from '../components/AlertBanner'
import { listAlerts, acknowledgeAlert } from '../api/alerts'

export default function Alerts() {
  const [alerts, setAlerts] = useState([])
  const [filter, setFilter] = useState('all')

  useEffect(() => { refresh() }, [])

  const refresh = () => listAlerts().then(setAlerts)

  const filtered = alerts.filter((a) => {
    if (filter === 'unacknowledged') return !a.acknowledged
    if (filter === 'critical') return a.severity === 'CRITICAL'
    return true
  })

  return (
    <>
      <Topbar
        title="Alerts"
        subtitle={`${alerts.filter((a) => !a.acknowledged).length} unacknowledged`}
        right={
          <select value={filter} onChange={(e) => setFilter(e.target.value)}>
            <option value="all">All alerts</option>
            <option value="unacknowledged">Unacknowledged</option>
            <option value="critical">Critical only</option>
          </select>
        }
      />
      <div className="page-content">
        <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
          {filtered.length === 0 && <div className="card" style={{ color: 'var(--text-muted)' }}>No alerts match this filter.</div>}
          {filtered.map((a) => (
            <AlertBanner key={a.id} alert={a} onAcknowledge={async (id) => {
              await acknowledgeAlert(id)
              refresh()
            }} />
          ))}
        </div>
      </div>
    </>
  )
}
