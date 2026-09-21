# Microservices Overview

This folder contains the domain services for the Precision Agriculture Platform.

## Service responsibilities

- `auth-service`: login, registration, token validation, user management
- `farm-service`: farms, fields, devices, and organization metadata
- `telemetry-service`: sensor ingestion, stream updates, and alerts
- `analytics-service`: reporting, summaries, and historical analysis

## Communication model

- The `gateway` module acts as the single front door for all external traffic.
- Services expose their own REST endpoints on unique ports.
- Future improvements may include service discovery, message brokers, and event-driven communication.

## Ports

- Auth service: 8081
- Farm service: 8082
- Telemetry service: 8083
- Analytics service: 8084
