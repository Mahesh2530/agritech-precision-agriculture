import { useState } from 'react'

export default function IrrigationToggle({ device, onCommand }) {
  const [duration, setDuration] = useState(15)
  const [busy, setBusy] = useState(false)

  const handle = async (activate) => {
    setBusy(true)
    try {
      await onCommand(device.id, activate, duration)
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="card" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 12 }}>
      <div>
        <div style={{ fontWeight: 600, fontSize: 14 }}>{device.name}</div>
        <div style={{ fontFamily: 'var(--font-mono)', fontSize: 11.5, color: 'var(--text-muted)' }}>{device.deviceCode}</div>
        <span className={`badge ${device.active ? 'badge-ok' : 'badge-info'}`} style={{ marginTop: 6 }}>
          {device.active ? 'RUNNING' : 'IDLE'}
        </span>
      </div>
      <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
        {!device.active && (
          <input
            type="number"
            min={1}
            max={120}
            value={duration}
            onChange={(e) => setDuration(Number(e.target.value))}
            style={{ width: 70 }}
            aria-label="Duration in minutes"
          />
        )}
        {device.active ? (
          <button className="btn btn-danger" disabled={busy} onClick={() => handle(false)}>Stop</button>
        ) : (
          <button className="btn btn-primary" disabled={busy} onClick={() => handle(true)}>Start ({duration}m)</button>
        )}
      </div>
    </div>
  )
}
