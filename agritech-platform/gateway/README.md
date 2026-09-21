# API Gateway

This module acts as a centralized entry point for the Precision Agriculture Platform.
It routes client requests to the Spring Boot backend, validates JWT tokens, limits request rate,
and exposes a fallback response when backend services are unavailable.

## Run

```bash
cd gateway
mvn spring-boot:run
```

The gateway starts on:
- http://localhost:8085

The backend remains on:
- http://localhost:8080

## Route design

- `/api/auth/**` -> backend authentication APIs
- `/api/**` -> backend business APIs
- `/ws/**` -> backend WebSocket endpoint
- `/h2-console/**` -> backend H2 console (development only)
- `/` -> frontend Vite app on port 5173

## Security design

The gateway validates the `Authorization: Bearer <token>` header before allowing protected routes.
If the token is invalid or expired, the request is rejected with `401 Unauthorized`.

## Resilience design

- Route-level circuit breaker is enabled for backend API calls.
- Request rate limiting is enforced with a gateway filter.
- Fallback endpoint returns a safe response when the upstream service is unhealthy.

## Example requests

```bash
curl http://localhost:8085/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@agritech.dev","password":"admin123"}'

curl http://localhost:8085/api/farms \
  -H "Authorization: Bearer <token>"
```
