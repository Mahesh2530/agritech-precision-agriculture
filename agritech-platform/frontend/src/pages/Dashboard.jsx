import { useEffect, useMemo, useState } from 'react'
import Topbar from '../components/Topbar'
import StatCard from '../components/StatCard'
import TelemetryChart from '../components/TelemetryChart'
import AlertBanner from '../components/AlertBanner'
import { listFarms, listFields } from '../api/farms'
import { getFieldLatest, getFieldHistory } from '../api/telemetry'
import { listAlerts, acknowledgeAlert } from '../api/alerts'
import { useWebSocket } from '../hooks/useWebSocket'

export default function Dashboard() {
  const [farms, setFarms] = useState([])
  const [fields, setFields] = useState([])
  const [selectedFieldId, setSelectedFieldId] = useState(null)
  const [latest, setLatest] = useState([])
  const [history, setHistory] = useState([])
  const [alerts, setAlerts] = useState([])

  useEffect(() => {
    listFarms().then(async (farmsData) => {
      setFarms(farmsData)
      if (farmsData.length > 0) {
        const fieldsData = await listFields(farmsData[0].id)
        setFields(fieldsData)
        if (fieldsData.length > 0) setSelectedFieldId(fieldsData[0].id)
      }
    })
    listAlerts({ unacknowledgedOnly: true }).then(setAlerts)
  }, [])

  useEffect(() => {
    if (!selectedFieldId) return
    getFieldLatest(selectedFieldId).then(setLatest)
    const to = new Date().toISOString()
    const from = new Date(Date.now() - 12 * 3600 * 1000).toISOString()
    getFieldHistory(selectedFieldId, 'SOIL_MOISTURE', from, to).then(setHistory)
  }, [selectedFieldId])

  const topics = useMemo(() => (selectedFieldId ? [`/topic/telemetry/${selectedFieldId}`] : []), [selectedFieldId])
  useWebSocket(topics, (_topic, payload) => {
    setLatest((prev) => {
      const others = prev.filter((p) => p.metricType !== payload.metricType)
      return [...others, payload]
    })
    if (payload.metricType === 'SOIL_MOISTURE') {
      setHistory((prev) => [...prev, payload].slice(-50))
    }
  })

  const moisture = latest.find((l) => l.metricType === 'SOIL_MOISTURE')
  const temp = latest.find((l) => l.metricType === 'TEMPERATURE')
  const selectedField = fields.find((f) => f.id === selectedFieldId)

  return (
    <>
      <Topbar
        title="Live Field Dashboard"
        subtitle={farms[0]?.name || 'No farms yet'}
        right={
          fields.length > 0 && (
            <select value={selectedFieldId || ''} onChange={(e) => setSelectedFieldId(Number(e.target.value))}>
              {fields.map((f) => <option key={f.id} value={f.id}>{f.name}</option>)}
            </select>
          )
        }
      />
      <div className="page-content">
        {fields.length === 0 ? (
          <div className="card">No fields yet — head to <strong>Farms &amp; Fields</strong> to add one.</div>
        ) : (
          <>
            <div className="grid grid-cols-4" style={{ marginBottom: 20 }}>
              <StatCard
                label="Soil moisture"
                value={moisture ? moisture.value.toFixed(1) : '—'}
                unit="%"
                status={moisture && selectedField && moisture.value < selectedField.moistureThreshold ? 'warning' : 'ok'}
                trend={selectedField ? `Threshold ${selectedField.moistureThreshold}%` : ''}
              />
              <StatCard
                label="Temperature"
                value={temp ? temp.value.toFixed(1) : '—'}
                unit="°C"
                status="info"
              />
              <StatCard label="Crop" value={selectedField?.cropType || '—'} />
              <StatCard label="Field area" value={selectedField?.areaHectares ?? '—'} unit="ha" />
            </div>

            <div className="card" style={{ marginBottom: 20 }}>
              <div className="eyebrow" style={{ fontFamily: 'var(--font-mono)', fontSize: 11, color: 'var(--accent-water)', marginBottom: 10, letterSpacing: '0.08em' }}>
                SOIL MOISTURE — LAST 12H
              </div>
              <TelemetryChart data={history} xKey="recordedAt" dataKey="value" color="var(--accent-water)" unit="%" />
            </div>

            <h3 style={{ marginBottom: 10 }}>Active alerts</h3>
            <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
              {alerts.length === 0 && <div className="card" style={{ color: 'var(--text-muted)' }}>No unacknowledged alerts. All clear.</div>}
              {alerts.map((a) => (
                <AlertBanner key={a.id} alert={a} onAcknowledge={async (id) => {
                  await acknowledgeAlert(id)
                  setAlerts((prev) => prev.filter((x) => x.id !== id))
                }} />
              ))}
            </div>
          </>
        )}
      </div>
    </>
  )
}
