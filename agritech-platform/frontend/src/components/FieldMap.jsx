import { MapContainer, TileLayer, Marker, Popup } from 'react-leaflet'

export default function FieldMap({ fields, height = 320 }) {
  const withCoords = (fields || []).filter((f) => f.latitude && f.longitude)
  const center = withCoords.length > 0
    ? [withCoords[0].latitude, withCoords[0].longitude]
    : [16.3067, 80.4365]

  return (
    <div style={{ height, borderRadius: 'var(--radius-lg)', overflow: 'hidden', border: '1px solid var(--border-soft)' }}>
      <MapContainer center={center} zoom={13} style={{ height: '100%', width: '100%' }}>
        <TileLayer
          attribution='&copy; OpenStreetMap contributors'
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />
        {withCoords.map((f) => (
          <Marker key={f.id} position={[f.latitude, f.longitude]}>
            <Popup>
              <strong>{f.name}</strong><br />
              {f.cropType} · {f.areaHectares} ha
            </Popup>
          </Marker>
        ))}
      </MapContainer>
    </div>
  )
}
