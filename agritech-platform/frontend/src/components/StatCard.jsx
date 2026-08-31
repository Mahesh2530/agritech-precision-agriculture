/**
 * The signature "instrument readout" card: large mono-font digits like a field
 * probe's LCD display, a coloured status edge, and a small trend note.
 */
export default function StatCard({ label, value, unit, status = 'ok', trend }) {
  const statusColor = {
    ok: 'var(--accent-growth)',
    warning: 'var(--accent-alert)',
    critical: 'var(--accent-critical)',
    info: 'var(--accent-water)',
  }[status]

  return (
    <div className="card" style={{ borderLeft: `3px solid ${statusColor}`, position: 'relative' }}>
      <div style={{ fontSize: 11, color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.08em', marginBottom: 8 }}>
        {label}
      </div>
      <div style={{ display: 'flex', alignItems: 'baseline', gap: 6 }}>
        <span style={{ fontFamily: 'var(--font-mono)', fontSize: 32, fontWeight: 600, color: 'var(--text-primary)' }}>
          {value ?? '—'}
        </span>
        {unit && <span style={{ fontFamily: 'var(--font-mono)', fontSize: 14, color: 'var(--text-muted)' }}>{unit}</span>}
      </div>
      {trend && (
        <div style={{ marginTop: 8, fontSize: 12, color: 'var(--text-secondary)' }}>{trend}</div>
      )}
    </div>
  )
}
