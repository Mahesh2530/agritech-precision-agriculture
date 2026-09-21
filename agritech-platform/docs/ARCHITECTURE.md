# Precision Agriculture Telemetry & Automated Irrigation Platform
### Architecture & Design Document — v1.0

---

## 1. Problem & Goals

Farms operate across multiple fields, each instrumented with soil-moisture, temperature,
humidity, and flow sensors, plus electrically actuated irrigation valves/pumps. The platform
must:

- Ingest high-frequency telemetry from distributed field sensors (wired or LPWAN/MQTT gateways).
- Give farm managers a live, multi-farm view of field conditions.
- Automate irrigation decisions from rules today, and pluggable ML models tomorrow.
- Raise alerts (low moisture, sensor offline, pump failure, frost risk) in real time.
- Provide historical analytics — usage, yield correlation, water savings.
- Support multiple farms, multiple fields per farm, and role-based access per organization.

This doc describes a v1 architecture that is fully buildable now, with explicit extension
points for the ML and analytics scope so the codebase doesn't need to be re-architected later.

---

## 2. High-Level Architecture

```
                                   ┌─────────────────────────┐
                                   │   Field Sensor Nodes     │
                                   │ (soil, temp, humidity,   │
                                   │  flow, valve actuators)  │
                                   └────────────┬─────────────┘
                                                │ MQTT / HTTP (gateway)
                                                ▼
                          ┌───────────────────────────────────────┐
                          │        API Gateway (Spring Cloud)       │
                          │  - JWT validation                       │
                          │  - Rate limiting / circuit breaker      │
                          │  - Routing to backend services          │
                          │  - Request tracing / health checks      │
                          └────────────┬────────────────────────────┘
                                       │
                                       ▼
                          ┌───────────────────────────────────────┐
                          │        Spring Boot Backend             │
                          │                                         │
                          │  ┌───────────────┐   ┌───────────────┐ │
                          │  │ Ingestion API  │──▶│ Telemetry Svc │ │
                          │  └───────────────┘   └──────┬────────┘ │
                          │                              │          │
                          │                    ┌─────────▼───────┐ │
                          │                    │ Irrigation Rule  │ │
                          │                    │  / ML Engine     │ │
                          │                    └─────────┬───────┘ │
                          │                              │          │
                          │  ┌───────────────┐  ┌────────▼──────┐  │
                          │  │  Alert Engine  │  │ Device Command │  │
                          │  └───────┬───────┘  │   Dispatcher   │  │
                          │          │          └────────┬──────┘  │
                          │  ┌───────▼──────────────────▼───────┐  │
                          │  │   WebSocket / STOMP Broadcast     │  │
                          │  └────────────────┬──────────────────┘ │
                          │                    │                    │
                          │  ┌─────────────────▼──────────────────┐│
                          │  │  REST API (Farms/Fields/Sensors/   ││
                          │  │  Irrigation/Alerts/Analytics/Auth) ││
                          │  └─────────────────┬──────────────────┘│
                          └────────────────────┼───────────────────┘
                                                │ JPA
                                   ┌────────────▼─────────────┐
                                   │ PostgreSQL + TimescaleDB  │
                                   │ (relational + time-series)│
                                   └───────────────────────────┘
                                                ▲
                                                │ REST + WebSocket (JWT)
                                   ┌────────────┴─────────────┐
                                   │      React Frontend       │
                                   │  Dashboard / Map / Charts │
                                   │  Irrigation Control panel │
                                   └───────────────────────────┘
```

---

## 3. Tech Stack

| Layer | Choice | Why |
|---|---|---|
| Backend | Spring Boot 3.3 (Java 17) | Requested; mature ecosystem for scheduling, security, JPA |
| Frontend | React 18 (JavaScript, Vite) | Requested — plain JS, no TypeScript |
| DB (relational) | PostgreSQL | Multi-tenant relational data (farms, users, devices) |
| DB (time-series) | TimescaleDB extension on Postgres | Sensor readings at scale; falls back to plain Postgres/H2 in dev |
| Realtime | WebSocket (STOMP) + optional MQTT broker (Mosquitto/HiveMQ) for device ingestion | Live dashboard push; device-friendly ingest protocol |
| Auth | JWT (stateless), Spring Security, role-based access | Multi-farm, multi-role tenancy |
| Charts | Recharts | Lightweight, React-native |
| Maps | Leaflet + OpenStreetMap | Field geolocation, free tiles |
| Scheduling | Spring `@Scheduled` (swap for Quartz at scale) | Periodic irrigation evaluation, offline-sensor sweep |
| ML integration | Strategy-pattern `IrrigationDecisionStrategy` interface | Rule engine ships now; a model-backed strategy (calling out to a Python/FastAPI inference service or an embedded ONNX model) plugs in without touching callers |

---

## 4. Data Model (core entities)

```
Organization ─┬─< Farm ─┬─< Field ─┬─< Sensor ─< SensorReading
              │         │          └─< IrrigationDevice ─< IrrigationEvent
              │         └─< Alert
              └─< User (many-to-many via Role: ADMIN, FARM_MANAGER, AGRONOMIST, VIEWER)

IrrigationSchedule (per Field): cron/window + trigger rule reference
AlertRule (per Field or per Org default): metric, operator, threshold, severity
AnalyticsSnapshot (per Field, daily rollup): avg moisture, water used, irrigation cycles
```

Key fields:
- **SensorReading**: `sensorId, timestamp, metricType (SOIL_MOISTURE|TEMP|HUMIDITY|FLOW), value, unit`
- **IrrigationEvent**: `deviceId, startedAt, endedAt, litersUsed, triggeredBy (RULE|ML|MANUAL), status`
- **Alert**: `fieldId, type, severity (INFO|WARNING|CRITICAL), message, acknowledged, createdAt`

---

## 5. Backend Module Breakdown (scaffolded)

| Package | Responsibility |
|---|---|
| `config` | Security, CORS, WebSocket/STOMP, scheduling enablement |
| `domain` | JPA entities |
| `repository` | Spring Data JPA repositories |
| `dto` | Request/response payloads (never expose entities directly) |
| `security` | JWT filter, token util, `UserDetailsService` |
| `service.telemetry` | Ingestion, validation, persistence, real-time broadcast |
| `service.irrigation` | `IrrigationDecisionStrategy` (rule engine now, ML-ready), device command dispatch |
| `service.alert` | Threshold evaluation, alert lifecycle |
| `service.analytics` | Daily rollups, usage/water-savings aggregation endpoints |
| `scheduler` | `@Scheduled` jobs: irrigation evaluation loop, offline-sensor sweep, daily analytics rollup |
| `controller` | REST endpoints, thin — delegate to services |
| `websocket` | STOMP config; topics `/topic/telemetry/{fieldId}`, `/topic/alerts/{farmId}` |
| `exception` | `@ControllerAdvice` global error handling |

### Automated irrigation logic (v1 rule engine → ML-ready)

```java
public interface IrrigationDecisionStrategy {
    IrrigationDecision decide(Field field, List<SensorReading> recentReadings);
}
```
- `ThresholdIrrigationStrategy` (default, active in v1): soil moisture < field's configured
  threshold AND no rain forecast flag → trigger irrigation for computed duration.
- `MlIrrigationStrategy` (stub, disabled by default): calls out to an external inference
  endpoint (`irrigation.ml.endpoint` config) with recent readings + weather features, returns a
  recommended duration/volume. Swapped in via a Spring `@ConditionalOnProperty`, so no controller
  or scheduler code changes when the ML service is ready.

---

## 6. REST API Surface (v1)

| Method | Path | Purpose |
|---|---|---|
| POST | `/api/auth/login` | JWT login |
| POST | `/api/auth/register` | Create user (admin only) |
| GET/POST | `/api/farms` | List / create farms |
| GET/POST | `/api/farms/{farmId}/fields` | List / create fields |
| POST | `/api/telemetry/ingest` | Sensor gateway pushes readings (API-key or device JWT) |
| GET | `/api/fields/{fieldId}/readings?metric=&from=&to=` | Historical readings |
| GET | `/api/fields/{fieldId}/latest` | Latest snapshot per metric |
| POST | `/api/irrigation/devices/{deviceId}/command` | Manual on/off override |
| GET/POST | `/api/irrigation/schedules` | Manage schedules |
| GET | `/api/alerts?farmId=&status=` | List alerts |
| POST | `/api/alerts/{id}/acknowledge` | Acknowledge alert |
| GET | `/api/analytics/fields/{fieldId}/summary?range=` | Water usage / moisture trend summary |
| WS | `/ws` (STOMP) | Subscribe `/topic/telemetry/{fieldId}`, `/topic/alerts/{farmId}` |

---

## 7. Frontend Structure (React + Vite, plain JS)

```
src/
  api/            axios client + per-domain API modules
  context/        AuthContext (JWT + user/role)
  hooks/          useWebSocket, useTelemetry
  components/     Sidebar, Topbar, StatCard, TelemetryChart, FieldMap,
                  SensorCard, IrrigationToggle, AlertBanner
  pages/          Login, Dashboard, Farms, FieldDetail, Irrigation, Alerts, Analytics
  routes/         ProtectedRoute (role-aware)
  styles/         design tokens (tokens.css) + component CSS
```

State: React Context + hooks (no Redux needed at this scale — swap in Redux Toolkit later if
cross-cutting state grows). Real-time telemetry lands via STOMP-over-WebSocket and merges into
local component state, so charts update live without polling.

---

## 8. Multi-Farm / Multi-Tenancy

- `Organization` is the tenancy boundary; every query is scoped by the authenticated user's
  organization (enforced in the service layer, not just the UI).
- Roles: `ADMIN` (org-wide), `FARM_MANAGER` (assigned farms), `AGRONOMIST` (read + advisory
  notes), `VIEWER` (read-only). Enforced via `@PreAuthorize` on controller methods.

---

## 9. Alerts

Evaluated two ways:
1. **Inline** — the ingestion service checks each incoming reading against the field's active
   `AlertRule`s synchronously (cheap threshold check) and raises alerts immediately.
2. **Scheduled sweep** — a `@Scheduled` job every 5 min checks for silent sensors (no reading in
   N minutes → `SENSOR_OFFLINE`) and device faults.

Alerts broadcast over `/topic/alerts/{farmId}` and are also queryable/acknowledgeable via REST.

---

## 10. Analytics & ML Roadmap

**v1 (shipped in scaffold):** daily rollups (avg moisture, min/max temp, liters used, irrigation
cycle count) computed by a nightly `@Scheduled` job into `AnalyticsSnapshot`, exposed via
`/api/analytics/...`.

**Phase 2:** yield correlation (requires yield data import), water-savings vs. baseline,
per-field anomaly detection (z-score on moisture trend).

**Phase 3 (ML):** `MlIrrigationStrategy` calls an external inference service (Python/FastAPI +
scikit-learn or a hosted model) — kept out-of-process so the JVM backend doesn't own model
training/serving. The Spring service only owns the feature payload contract and result contract,
defined in `dto.ml`, so the strategy swap is config-only (`irrigation.strategy=ml`).

---

## 11. Deployment Topology (target)

```
React (static build) → CDN / Nginx
Spring Boot          → containerized, horizontally scalable (stateless, JWT)
PostgreSQL+Timescale → managed instance (RDS/Cloud SQL + Timescale)
MQTT broker          → managed (HiveMQ Cloud) or self-hosted Mosquitto, bridges into
                        `/api/telemetry/ingest` via a lightweight consumer, or backend
                        subscribes directly via Spring Integration MQTT (Phase 2)
```

Dev/local: `docker-compose` with Postgres + backend + frontend (see `README.md` in each folder).

---

## 12. What's in the Scaffold vs. Roadmap

| Included now | Roadmap (structure ready, not implemented) |
|---|---|
| Auth (JWT), roles | SSO/OAuth2 |
| Farms/Fields/Sensors CRUD | Device provisioning UI |
| Telemetry ingestion + REST history | Direct MQTT broker subscription |
| Rule-based irrigation engine + manual override | ML-backed strategy (interface + stub ready) |
| Threshold + offline-sensor alerts | Anomaly detection, notification channels (SMS/email) |
| Daily analytics rollup + summary API | Yield correlation, forecasting |
| Live dashboard via WebSocket | Field boundary drawing / satellite overlays |

This keeps v1 shippable while making every "full platform" feature a clean extension rather
than a rewrite.
