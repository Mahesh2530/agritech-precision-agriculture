import { useEffect, useState } from 'react'
import Topbar from '../components/Topbar'
import FieldMap from '../components/FieldMap'
import { listFarms, createFarm, listFields, createField } from '../api/farms'

export default function Farms() {
  const [farms, setFarms] = useState([])
  const [selectedFarmId, setSelectedFarmId] = useState(null)
  const [fields, setFields] = useState([])
  const [showFarmForm, setShowFarmForm] = useState(false)
  const [showFieldForm, setShowFieldForm] = useState(false)

  const refreshFarms = () => listFarms().then((data) => {
    setFarms(data)
    if (data.length > 0 && !selectedFarmId) setSelectedFarmId(data[0].id)
  })

  useEffect(() => { refreshFarms() }, [])
  useEffect(() => { if (selectedFarmId) listFields(selectedFarmId).then(setFields) }, [selectedFarmId])

  return (
    <>
      <Topbar title="Farms & Fields" subtitle="Manage your growing sites" right={
        <button className="btn btn-primary" onClick={() => setShowFarmForm((s) => !s)}>+ New farm</button>
      } />
      <div className="page-content">
        {showFarmForm && (
          <FarmForm onCreated={() => { setShowFarmForm(false); refreshFarms() }} />
        )}

        <div className="grid grid-cols-2" style={{ marginBottom: 20 }}>
          <div className="card">
            <div style={{ fontSize: 12, color: 'var(--text-muted)', marginBottom: 10, textTransform: 'uppercase', letterSpacing: '0.06em' }}>Farms</div>
            {farms.map((f) => (
              <div
                key={f.id}
                onClick={() => setSelectedFarmId(f.id)}
                style={{
                  padding: '10px 12px', borderRadius: 8, cursor: 'pointer', marginBottom: 4,
                  background: selectedFarmId === f.id ? 'var(--surface-raised)' : 'transparent',
                }}
              >
                <div style={{ fontWeight: 600, fontSize: 14 }}>{f.name}</div>
                <div style={{ fontSize: 12, color: 'var(--text-muted)' }}>{f.location} · {f.fieldCount} field(s)</div>
              </div>
            ))}
          </div>
          <FieldMap fields={fields} />
        </div>

        <div className="page-header">
          <h3 style={{ margin: 0 }}>Fields</h3>
          <button className="btn" onClick={() => setShowFieldForm((s) => !s)}>+ New field</button>
        </div>

        {showFieldForm && selectedFarmId && (
          <FieldForm farmId={selectedFarmId} onCreated={() => {
            setShowFieldForm(false)
            listFields(selectedFarmId).then(setFields)
          }} />
        )}

        <div className="grid grid-cols-3">
          {fields.map((f) => (
            <div key={f.id} className="card">
              <div style={{ fontWeight: 600 }}>{f.name}</div>
              <div style={{ fontSize: 12.5, color: 'var(--text-secondary)', marginTop: 4 }}>
                {f.cropType} · {f.areaHectares} ha
              </div>
              <div style={{ fontFamily: 'var(--font-mono)', fontSize: 11.5, color: 'var(--text-muted)', marginTop: 8 }}>
                threshold {f.moistureThreshold}% · {f.defaultIrrigationMinutes}min cycle
              </div>
            </div>
          ))}
        </div>
      </div>
    </>
  )
}

function FarmForm({ onCreated }) {
  const [name, setName] = useState('')
  const [location, setLocation] = useState('')

  const submit = async (e) => {
    e.preventDefault()
    await createFarm({ name, location, latitude: null, longitude: null })
    onCreated()
  }

  return (
    <form className="card" onSubmit={submit} style={{ marginBottom: 20, display: 'flex', gap: 12, alignItems: 'flex-end', flexWrap: 'wrap' }}>
      <div>
        <label>Farm name</label>
        <input value={name} onChange={(e) => setName(e.target.value)} required />
      </div>
      <div>
        <label>Location</label>
        <input value={location} onChange={(e) => setLocation(e.target.value)} />
      </div>
      <button className="btn btn-primary" type="submit">Create farm</button>
    </form>
  )
}

function FieldForm({ farmId, onCreated }) {
  const [form, setForm] = useState({ name: '', cropType: '', areaHectares: '', moistureThreshold: 30, defaultIrrigationMinutes: 15 })

  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value })

  const submit = async (e) => {
    e.preventDefault()
    await createField(farmId, {
      ...form,
      areaHectares: Number(form.areaHectares) || null,
      moistureThreshold: Number(form.moistureThreshold),
      defaultIrrigationMinutes: Number(form.defaultIrrigationMinutes),
    })
    onCreated()
  }

  return (
    <form className="card" onSubmit={submit} style={{ marginBottom: 20, display: 'flex', gap: 12, alignItems: 'flex-end', flexWrap: 'wrap' }}>
      <div><label>Field name</label><input value={form.name} onChange={set('name')} required /></div>
      <div><label>Crop</label><input value={form.cropType} onChange={set('cropType')} /></div>
      <div><label>Area (ha)</label><input type="number" value={form.areaHectares} onChange={set('areaHectares')} style={{ width: 90 }} /></div>
      <div><label>Moisture threshold %</label><input type="number" value={form.moistureThreshold} onChange={set('moistureThreshold')} style={{ width: 90 }} /></div>
      <div><label>Irrigation minutes</label><input type="number" value={form.defaultIrrigationMinutes} onChange={set('defaultIrrigationMinutes')} style={{ width: 90 }} /></div>
      <button className="btn btn-primary" type="submit">Create field</button>
    </form>
  )
}
