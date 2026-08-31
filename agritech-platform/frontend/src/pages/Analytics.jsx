import { useEffect, useState } from 'react'
import Topbar from '../components/Topbar'
import StatCard from '../components/StatCard'
import TelemetryChart from '../components/TelemetryChart'
import { listFarms, listFields } from '../api/farms'
import { getFieldSummary } from '../api/analytics'

export default function Analytics() {
  const [fields, setFields] = useState([])
  const [selectedFieldId, setSelectedFieldId] = useState(null)
  const [summary, setSummary] = useState(null)
  const [days, setDays] = useState(14)

  useEffect(() => {
    listFarms().then(async (farms) => {
      if (farms.length === 0) return
      const fieldsData = await listFields(farms[0].id)
      setFields(fieldsData)
      if (fieldsData.length > 0) setSelectedFieldId(fieldsData[0].id)
    })
  }, [])

  useEffect(() => {
    if (selectedFieldId) getFieldSummary(selectedFieldId, days).then(setSummary)
  }, [selectedFieldId, days])

  return (
    <>
      <Topbar
        title="Analytics"
        subtitle="Daily rollups — water usage, moisture trend, irrigation cycles"
        right={
          <div style={{ display: 'flex', gap: 8 }}>
            <select value={selectedFieldId || ''} onChange={(e) => setSelectedFieldId(Number(e.target.value))}>
              {fields.map((f) => <option key={f.id} value={f.id}>{f.name}</option>)}
            </select>
            <select value={days} onChange={(e) => setDays(Number(e.target.value))}>
              <option value={7}>Last 7 days</option>
              <option value={14}>Last 14 days</option>
              <option value={30}>Last 30 days</option>
            </select>
          </div>
        }
      />
      <div className="page-content">
        {!summary || summary.series.length === 0 ? (
          <div className="card" style={{ color: 'var(--text-muted)' }}>
            No rollup data yet. The nightly analytics job populates this once a day of telemetry has been collected —
            see ARCHITECTURE.md for the rollup schedule.
          </div>
        ) : (
          <>
            <div className="grid grid-cols-3" style={{ marginBottom: 20 }}>
              <StatCard label="Total water used" value={summary.totalWaterUsedLiters?.toFixed(0)} unit="L" status="info" />
              <StatCard label="Avg soil moisture" value={summary.avgMoisture?.toFixed(1)} unit="%" status="ok" />
              <StatCard label="Field" value={summary.fieldName} />
            </div>

            <div className="card" style={{ marginBottom: 20 }}>
              <div style={{ fontFamily: 'var(--font-mono)', fontSize: 11, color: 'var(--accent-growth)', marginBottom: 10, letterSpacing: '0.08em' }}>
                AVG SOIL MOISTURE — DAILY
              </div>
              <TelemetryChart data={summary.series} xKey="date" dataKey="avgSoilMoisture" color="var(--accent-growth)" unit="%" />
            </div>

            <div className="card">
              <div style={{ fontFamily: 'var(--font-mono)', fontSize: 11, color: 'var(--accent-water)', marginBottom: 10, letterSpacing: '0.08em' }}>
                WATER USED — DAILY (LITERS)
              </div>
              <TelemetryChart data={summary.series} xKey="date" dataKey="totalLitersUsed" color="var(--accent-water)" unit="L" />
            </div>
          </>
        )}
      </div>
    </>
  )
}
