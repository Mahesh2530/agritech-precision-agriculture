import { NavLink } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

const NAV_ITEMS = [
  { to: '/', label: 'Dashboard', icon: '◧' },
  { to: '/farms', label: 'Farms & Fields', icon: '⛶' },
  { to: '/irrigation', label: 'Irrigation', icon: '⌁' },
  { to: '/alerts', label: 'Alerts', icon: '▲' },
  { to: '/analytics', label: 'Analytics', icon: '≋' },
]

export default function Sidebar() {
  const { user, logout } = useAuth()

  return (
    <aside style={styles.sidebar}>
      <div style={styles.brand}>
        <span style={styles.brandMark}>◈</span>
        <div>
          <div style={styles.brandName}>AgriTelemetry</div>
          <div style={styles.brandSub}>Field Ops Console</div>
        </div>
      </div>

      <nav style={styles.nav}>
        {NAV_ITEMS.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            end={item.to === '/'}
            style={({ isActive }) => ({
              ...styles.navItem,
              ...(isActive ? styles.navItemActive : {}),
            })}
          >
            <span style={styles.navIcon}>{item.icon}</span>
            {item.label}
          </NavLink>
        ))}
      </nav>

      <div style={styles.footer}>
        {user && (
          <>
            <div style={styles.userName}>{user.fullName}</div>
            <div style={styles.userRole}>{user.role}</div>
            <button className="btn" style={{ marginTop: 10, width: '100%' }} onClick={logout}>
              Sign out
            </button>
          </>
        )}
      </div>
    </aside>
  )
}

const styles = {
  sidebar: {
    background: 'var(--surface)',
    borderRight: '1px solid var(--border-soft)',
    display: 'flex',
    flexDirection: 'column',
    padding: '22px 16px',
    position: 'sticky',
    top: 0,
    height: '100vh',
  },
  brand: { display: 'flex', alignItems: 'center', gap: 10, marginBottom: 30, padding: '0 6px' },
  brandMark: { fontSize: 22, color: 'var(--accent-growth)' },
  brandName: { fontFamily: 'var(--font-display)', fontWeight: 600, fontSize: 15 },
  brandSub: { fontSize: 11, color: 'var(--text-muted)', fontFamily: 'var(--font-mono)' },
  nav: { display: 'flex', flexDirection: 'column', gap: 3, flex: 1 },
  navItem: {
    display: 'flex', alignItems: 'center', gap: 10,
    padding: '10px 12px', borderRadius: 8, fontSize: 13.5,
    color: 'var(--text-secondary)', textDecoration: 'none', fontWeight: 500,
  },
  navItemActive: {
    background: 'var(--surface-raised)', color: 'var(--text-primary)',
    boxShadow: 'inset 2px 0 0 var(--accent-growth)',
  },
  navIcon: { width: 16, textAlign: 'center', color: 'var(--accent-growth)' },
  footer: { borderTop: '1px solid var(--border-soft)', paddingTop: 14, marginTop: 14 },
  userName: { fontSize: 13, fontWeight: 600 },
  userRole: { fontSize: 11, color: 'var(--text-muted)', fontFamily: 'var(--font-mono)', textTransform: 'uppercase' },
}
