# Frontend — Precision Agriculture Platform (React, JavaScript)

Plain JavaScript (no TypeScript), Vite + React 18.

## Run

```bash
cd frontend
npm install
npm run dev
```

Opens on `http://localhost:5173`. API and WebSocket calls are proxied to the backend at
`http://localhost:8080` (see `vite.config.js`) — run the backend first.

Login with the seeded demo account: `admin@agritech.dev` / `admin123`.

## Structure

```
src/
  api/         axios client + one module per backend domain
  context/     AuthContext (JWT session)
  hooks/       useWebSocket — STOMP-over-SockJS live telemetry/alerts/irrigation
  components/  Sidebar, Topbar, StatCard, TelemetryChart, FieldMap, AlertBanner, IrrigationToggle
  pages/       Login, Dashboard, Farms, Irrigation, Alerts, Analytics
  routes/      ProtectedRoute
  styles/      tokens.css (design tokens) + global.css
```

## Design system

The UI uses an "field instrument panel" visual language — a dark moss/charcoal surface with a
`JetBrains Mono` readout style for telemetry numbers (soil moisture %, temperature), a
chlorophyll-green accent for healthy readings, irrigation-blue for water, and amber/rust for
warning/critical alerts. Tokens live in `src/styles/tokens.css` — change them there to re-theme
the whole app.

## Real-time data

`useWebSocket(topics, onMessage)` connects once per mount to `/ws` (SockJS) and subscribes to the
STOMP topics you pass in:
- `/topic/telemetry/{fieldId}` — new sensor readings
- `/topic/irrigation/{fieldId}` — irrigation start/stop events
- `/topic/alerts/{farmId}` — new alerts

## Build

```bash
npm run build
```

Outputs static assets to `dist/` — serve behind Nginx/CDN per ARCHITECTURE.md §11.
