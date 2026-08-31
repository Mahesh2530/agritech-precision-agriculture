import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function Login() {
  const [email, setEmail] = useState('admin@agritech.dev')
  const [password, setPassword] = useState('admin123')
  const [error, setError] = useState(null)
  const [busy, setBusy] = useState(false)
  const { login } = useAuth()
  const navigate = useNavigate()

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError(null)
    setBusy(true)
    try {
      await login(email, password)
      navigate('/')
    } catch (err) {
      setError(err?.response?.data?.message || 'Login failed. Check your credentials.')
    } finally {
      setBusy(false)
    }
  }

  return (
    <div style={styles.wrap}>
      <form className="card" style={styles.card} onSubmit={handleSubmit}>
        <div style={{ marginBottom: 22 }}>
          <span style={{ fontFamily: 'var(--font-mono)', fontSize: 11, letterSpacing: '0.14em', color: 'var(--accent-growth)', textTransform: 'uppercase' }}>
            Field Ops Console
          </span>
          <h1 style={{ fontSize: 22, marginTop: 6 }}>AgriTelemetry</h1>
          <div style={{ fontSize: 13, color: 'var(--text-muted)' }}>Precision irrigation, live from the field.</div>
        </div>

        <label htmlFor="email">Email</label>
        <input id="email" type="email" value={email} onChange={(e) => setEmail(e.target.value)} style={{ width: '100%', marginBottom: 14 }} required />

        <label htmlFor="password">Password</label>
        <input id="password" type="password" value={password} onChange={(e) => setPassword(e.target.value)} style={{ width: '100%', marginBottom: 18 }} required />

        {error && <div style={{ color: 'var(--accent-critical)', fontSize: 13, marginBottom: 14 }}>{error}</div>}

        <button className="btn btn-primary" type="submit" disabled={busy} style={{ width: '100%' }}>
          {busy ? 'Signing in…' : 'Sign in'}
        </button>

        <div style={{ marginTop: 16, fontSize: 11.5, color: 'var(--text-muted)', fontFamily: 'var(--font-mono)' }}>
          demo: admin@agritech.dev / admin123
        </div>
      </form>
    </div>
  )
}

const styles = {
  wrap: {
    minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center',
    background: 'radial-gradient(circle at 20% 20%, #1B2016 0%, #12150F 60%)',
  },
  card: { width: 360, padding: 32 },
}
