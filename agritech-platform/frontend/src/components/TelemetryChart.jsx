import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts'

export default function TelemetryChart({ data, dataKey = 'value', xKey = 'recordedAt', color = 'var(--accent-growth)', height = 220, unit = '' }) {
  const formatted = (data || []).map((d) => ({
    ...d,
    label: typeof d[xKey] === 'string' ? d[xKey].slice(11, 16) : d[xKey],
  }))

  return (
    <ResponsiveContainer width="100%" height={height}>
      <LineChart data={formatted} margin={{ top: 8, right: 12, left: -12, bottom: 0 }}>
        <CartesianGrid strokeDasharray="3 3" stroke="#2A3222" />
        <XAxis dataKey="label" stroke="#7C8770" fontSize={11} tickLine={false} />
        <YAxis stroke="#7C8770" fontSize={11} tickLine={false} unit={unit} />
        <Tooltip
          contentStyle={{ background: '#1B2016', border: '1px solid #37402C', borderRadius: 8, fontSize: 12 }}
          labelStyle={{ color: '#B7BFA8' }}
        />
        <Line type="monotone" dataKey={dataKey} stroke={color} strokeWidth={2} dot={false} />
      </LineChart>
    </ResponsiveContainer>
  )
}
