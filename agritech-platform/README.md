# Precision Agriculture Telemetry & Automated Irrigation Platform

A full-stack scaffold: **React (JavaScript) frontend** + **Spring Boot backend**, covering
telemetry ingestion, live dashboards, automated (rule-based, ML-ready) irrigation, alerts,
multi-farm support, and analytics rollups.

```
agritech-platform/
  docs/ARCHITECTURE.md   ← start here: architecture, data model, API surface, roadmap
  backend/                ← Spring Boot 3.3 / Java 17 / Maven
  frontend/               ← React 18 / plain JavaScript / Vite
```

## Quick start

**1. Backend** (needs Java 17 + Maven; uses an in-memory H2 DB seeded with demo data)
```bash
cd backend
mvn spring-boot:run
```
Runs on `http://localhost:8080`. Demo login: `admin@agritech.dev` / `admin123`.

**2. Frontend** (needs Node 18+)
```bash
cd frontend
npm install
npm run dev
```
Runs on `http://localhost:5173` and proxies API/WebSocket calls to the backend.

Full instructions, including how to point at Postgres for production, are in each folder's
`README.md`. The architecture doc (`docs/ARCHITECTURE.md`) explains the whole system, including
what's implemented now vs. what the roadmap section describes (ML strategy, SSO, direct MQTT
subscription, yield correlation, etc.) so you know exactly where to extend it next.

## What's included

- Multi-farm, multi-field, multi-role (JWT-secured) data model
- Sensor telemetry ingestion → live WebSocket push to the dashboard
- Rule-based automated irrigation with a clean interface for swapping in an ML strategy later
- Threshold + sensor-offline alerting
- Nightly analytics rollups (moisture trend, water usage, irrigation cycles) with a summary API
- API Gateway layer with centralized routing, JWT validation, rate limiting, and circuit breaking
- A distinctive "field instrument panel" UI — dark moss/charcoal theme, mono-font telemetry
  readouts, live charts (Recharts), field map (Leaflet)

## API Gateway Layer

A dedicated gateway module is included under `gateway/` to act as the single entry point for the platform:

- Routes `/api/auth/**`, `/api/**`, `/ws/**`, and `/h2-console/**`
- Validates JWT access tokens at the gateway before forwarding protected requests
- Enforces request throttling and circuit breaker protection for upstream backend calls
- Returns a fallback response when the backend is unavailable

Run it with:

```bash
cd gateway
mvn spring-boot:run
```

The gateway listens on `http://localhost:8085` and forwards traffic to the backend at `http://localhost:8080`.

## Note on this build

This was generated as a code scaffold in an environment without access to Maven Central or the
npm registry, so it hasn't been compiled/run in-sandbox — it's been carefully hand-reviewed but
you should treat `mvn spring-boot:run` / `npm install && npm run dev` as the first real build/test
pass. If anything doesn't compile cleanly, it's most likely a small import or version mismatch,
not a structural issue — happy to help debug once you've got real error output.
