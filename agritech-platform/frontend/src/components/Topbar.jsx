export default function Topbar({ title, subtitle, right }) {
  return (
    <header style={styles.wrap}>
      <div>
        <h2 style={{ margin: 0, fontSize: 18 }}>{title}</h2>
        {subtitle && <div style={{ fontSize: 12.5, color: 'var(--text-muted)' }}>{subtitle}</div>}
      </div>
      {right && <div>{right}</div>}
    </header>
  )
}

const styles = {
  wrap: {
    display: 'flex', justifyContent: 'space-between', alignItems: 'center',
    padding: '18px 32px', borderBottom: '1px solid var(--border-soft)',
    background: 'var(--bg)', position: 'sticky', top: 0, zIndex: 5,
  },
}
