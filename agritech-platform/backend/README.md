# Backend — Precision Agriculture Platform (Spring Boot)

Java 17, Spring Boot 3.3, Maven.

## Run (dev — in-memory H2, seeded with demo data)

```bash
cd backend
mvn spring-boot:run
```

API starts on `http://localhost:8080`. H2 console: `http://localhost:8080/h2-console`
(JDBC URL `jdbc:h2:mem:agritech`, user `sa`, empty password).

Seeded login: `admin@agritech.dev` / `admin123`

`DataSeeder` (in `config/`) populates the empty dev DB with a full demo dataset on first run:
1 org, 1 farm, 2 fields, 4 sensors, ~24h of mock soil-moisture/temperature history per field
(50 readings each), 2 irrigation devices with event history (one completed cycle, one still
running), 3 sample alerts, 2 alert rules, and 7 days of analytics rollups per field — so the
dashboard, alerts, irrigation, and analytics pages all show real-looking data immediately,
with no need to POST telemetry manually first. It only runs when the database is empty, so it
never clobbers real data, and uses a fixed random seed so the demo numbers are reproducible.

## Run against Postgres (prod profile)

```bash
export SPRING_PROFILES_ACTIVE=prod
export DB_URL=jdbc:postgresql://localhost:5432/agritech
export DB_USERNAME=agritech
export DB_PASSWORD=agritech
mvn spring-boot:run
```

Create the DB first: `createdb agritech` (optionally with the TimescaleDB extension enabled
for the `sensor_readings` table at scale — see ARCHITECTURE.md §3).

## Try it end-to-end

```bash
# 1. Login
TOKEN=$(curl -s -X POST localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@agritech.dev","password":"admin123"}' | python3 -c "import sys,json;print(json.load(sys.stdin)['token'])")

# 2. List farms
curl -s localhost:8080/api/farms -H "Authorization: Bearer $TOKEN"

# 3. Push a sensor reading (no auth required — device endpoint)
curl -s -X POST localhost:8080/api/telemetry/ingest \
  -H "Content-Type: application/json" \
  -d '{"deviceCode":"SM-A1","value":22.5}'
# -> this reading is below Field A's 35% threshold, so it will:
#    - raise a LOW_MOISTURE alert
#    - auto-trigger Field A's irrigation valve (VALVE-A1)
#    - push both events over WebSocket to /topic/telemetry/{fieldId},
#      /topic/alerts/{farmId}, /topic/irrigation/{fieldId}

# 4. Check alerts
curl -s "localhost:8080/api/alerts" -H "Authorization: Bearer $TOKEN"
```

## Security notes for production

- The `/api/telemetry/ingest` endpoint is intentionally unauthenticated in this scaffold so
  field gateways can push data without a user session. In production, replace with a per-device
  API key or mTLS, validated in a `DeviceAuthFilter` before it reaches the controller.
- Rotate `app.jwt.secret` via an env var / secrets manager — do not commit a real secret.

## Where to plug in the ML irrigation strategy

Set `app.irrigation.strategy=ml` and `app.irrigation.ml-endpoint=<your inference service URL>`.
`MlIrrigationStrategy` (in `service/irrigation`) will then be the active bean — no other code
changes needed. See ARCHITECTURE.md §5 and §10.
