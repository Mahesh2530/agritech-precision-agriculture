import { useEffect, useState } from 'react'
import Topbar from '../components/Topbar'
import IrrigationToggle from '../components/IrrigationToggle'
import { listFarms, listFields } from '../api/farms'
import { listDevices, sendCommand } from '../api/irrigation'
import { useWebSocket } from '../hooks/useWebSocket'

export default function Irrigation() {
  const [fields, setFields] = useState([])
  const [devicesByField, setDevicesByField] = useState({})

  useEffect(() => {
    listFarms().then(async (farms) => {
      if (farms.length === 0) return
      const fieldsData = await listFields(farms[0].id)
      setFields(fieldsData)
      const entries = await Promise.all(fieldsData.map(async (f) => [f.id, await listDevices(f.id)]))
      setDevicesByField(Object.fromEntries(entries))
    })
  }, [])

  const topics = fields.map((f) => `/topic/irrigation/${f.id}`)
  useWebSocket(topics, (topic, payload) => {
    const fieldId = Number(topic.split('/').pop())
    setDevicesByField((prev) => {
      const devices = prev[fieldId] || []
      const updated = devices.map((d) => d.id === payload.deviceId ? { ...d, active: payload.stillRunning } : d)
      return { ...prev, [fieldId]: updated }
    })
  })

  const handleCommand = async (deviceId, activate, durationMinutes) => {
    await sendCommand(deviceId, activate, durationMinutes)
    setDevicesByField((prev) => {
      const next = { ...prev }
      for (const fieldId of Object.keys(next)) {
        next[fieldId] = next[fieldId].map((d) => d.id === deviceId ? { ...d, active: activate } : d)
      }
      return next
    })
  }

  return (
    <>
      <Topbar title="Irrigation Control" subtitle="Manual overrides and live device status" />
      <div className="page-content">
        {fields.map((field) => (
          <div key={field.id} style={{ marginBottom: 26 }}>
            <div className="page-header" style={{ marginBottom: 12 }}>
              <div>
                <span className="eyebrow">{field.cropType || 'Field'}</span>
                <h3 style={{ margin: 0 }}>{field.name}</h3>
              </div>
              <div style={{ fontSize: 12, color: 'var(--text-muted)', fontFamily: 'var(--font-mono)' }}>
                auto-trigger below {field.moistureThreshold}% moisture
              </div>
            </div>
            <div className="grid grid-cols-2">
              {(devicesByField[field.id] || []).map((device) => (
                <IrrigationToggle key={device.id} device={device} onCommand={handleCommand} />
              ))}
              {(devicesByField[field.id] || []).length === 0 && (
                <div className="card" style={{ color: 'var(--text-muted)' }}>No irrigation devices configured for this field.</div>
              )}
            </div>
          </div>
        ))}
      </div>
    </>
  )
}
