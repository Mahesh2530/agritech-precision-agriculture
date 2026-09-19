import { useEffect, useMemo, useState } from 'react'
import Topbar from '../components/Topbar'
import TelemetryChart from '../components/TelemetryChart'
import { getWindSpeedData } from '../api/external'

const DEFAULTS = {
  format: 'JSON',
  state: 'Andhra Pradesh',
  district: 'KRISHNA',
  agency: 'Andhra Pradesh GW',
}

export default function WindData() {
  const [form, setForm] = useState(DEFAULTS)
  const [records, setRecords] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  const loadData = async (params = form) => {
    setLoading(true)
    setError(null)
    try {
      const data = await getWindSpeedData(params)
      const result = data?.result ?? {}
      const items = Array.isArray(result.records) ? result.records : []
      setRecords(items)
    } catch (requestError) {
      setRecords([])
      setError(requestError?.response?.data?.message || 'Wind data could not be loaded. Check the backend and NWDP resource configuration.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadData()
  }, [])

  const chartData = useMemo(() => records.map((item) => ({
    label: String(item['Data Acquisition Time'] || '').slice(0, 16),
    value: Number(item.windSpeed ?? item['Telemetry Hourly Wind Speed (Km/Hr)'] ?? 0),
  })), [records])

  const averageWind = chartData.length ? (chartData.reduce((sum, item) => sum + item.value, 0) / chartData.length).toFixed(1) : '0.0'

  return (
    <>
      <Topbar
        title="Wind Data"
        subtitle="External NWDP telemetry feed"
      />

      <div className="page-content">
        <div className="card" style={{ marginBottom: 20 }}>
          <div style={{ display: 'grid', gap: 16, gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))' }}>
            <label>
              <div style={{ marginBottom: 8, fontWeight: 600 }}>Format</div>
              <select value={form.format} onChange={(e) => setForm((prev) => ({ ...prev, format: e.target.value }))}>
                <option value="JSON">JSON</option>
              </select>
            </label>

            <label>
              <div style={{ marginBottom: 8, fontWeight: 600 }}>State</div>
              <select value={form.state} onChange={(e) => setForm((prev) => ({ ...prev, state: e.target.value }))}>
                <option value="Andhra Pradesh">Andhra Pradesh</option>
              </select>
            </label>

            <label>
              <div style={{ marginBottom: 8, fontWeight: 600 }}>District</div>
              <select value={form.district} onChange={(e) => setForm((prev) => ({ ...prev, district: e.target.value }))}>
                <option value="KRISHNA">KRISHNA</option>
              </select>
            </label>

            <label>
              <div style={{ marginBottom: 8, fontWeight: 600 }}>Agency</div>
              <select value={form.agency} onChange={(e) => setForm((prev) => ({ ...prev, agency: e.target.value }))}>
                <option value="Andhra Pradesh GW">Andhra Pradesh GW</option>
              </select>
            </label>
          </div>

          <div style={{ marginTop: 18, display: 'flex', justifyContent: 'flex-end' }}>
            <button className="btn btn-primary" onClick={() => loadData()} disabled={loading}>
              {loading ? 'Loading...' : 'Load data'}
            </button>
          </div>
          {error && <div style={{ color: 'var(--accent-critical)', marginTop: 14 }}>{error}</div>}
        </div>

        <div className="grid grid-cols-3" style={{ marginBottom: 20 }}>
          <div className="card">
            <div style={{ fontSize: 11, color: 'var(--text-muted)', letterSpacing: '0.08em', textTransform: 'uppercase' }}>Records</div>
            <div style={{ fontFamily: 'var(--font-mono)', fontSize: 30 }}>{records.length}</div>
          </div>
          <div className="card">
            <div style={{ fontSize: 11, color: 'var(--text-muted)', letterSpacing: '0.08em', textTransform: 'uppercase' }}>Avg wind</div>
            <div style={{ fontFamily: 'var(--font-mono)', fontSize: 30 }}>{averageWind} km/h</div>
          </div>
          <div className="card">
            <div style={{ fontSize: 11, color: 'var(--text-muted)', letterSpacing: '0.08em', textTransform: 'uppercase' }}>State</div>
            <div style={{ fontFamily: 'var(--font-mono)', fontSize: 24 }}>{form.state}</div>
          </div>
        </div>

        <div className="card">
          <div style={{ fontFamily: 'var(--font-mono)', fontSize: 11, color: 'var(--accent-water)', marginBottom: 10, letterSpacing: '0.08em' }}>
            WIND SPEED — HOURLY
          </div>
          <TelemetryChart data={chartData} xKey="label" dataKey="value" color="var(--accent-water)" unit="km/h" />
        </div>
      </div>
    </>
  )
}
