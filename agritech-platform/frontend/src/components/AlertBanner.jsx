const SEVERITY_CLASS = { INFO: 'badge-info', WARNING: 'badge-warning', CRITICAL: 'badge-critical' }

export default function AlertBanner({ alert, onAcknowledge }) {
  return (
    <div className="card" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '14px 18px' }}>
      <div style={{ display: 'flex', gap: 12, alignItems: 'center' }}>
        <span className={`badge ${SEVERITY_CLASS[alert.severity] || 'badge-info'}`}>{alert.severity}</span>
        <div>
          <div style={{ fontSize: 13.5, fontWeight: 500 }}>{alert.message}</div>
          <div style={{ fontSize: 11.5, color: 'var(--text-muted)', fontFamily: 'var(--font-mono)' }}>
            {alert.fieldName} · {new Date(alert.createdAt).toLocaleString()}
          </div>
        </div>
      </div>
      {!alert.acknowledged && (
        <button className="btn" onClick={() => onAcknowledge(alert.id)}>Acknowledge</button>
      )}
      {alert.acknowledged && <span style={{ fontSize: 12, color: 'var(--text-muted)' }}>Acknowledged</span>}
    </div>
  )
}
