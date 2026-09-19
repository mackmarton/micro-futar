# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project overview

Micro-futár is a package delivery platform (sender/tracking, logistics/dispatch, courier) built as
Spring Boot microservices with a React frontend monorepo. Services communicate synchronously via
the API gateway and asynchronously via Kafka, and authenticate through Keycloak (OIDC/JWT).

## Repository layout

- `api_gateway/` — Spring Cloud Gateway (WebFlux). Single entry point; routes `/api/orders/**`,
  `/api/tracking/**`, `/api/logistics/**`, `/api/courier/**` to the corresponding service and
  performs the OAuth2 `TokenRelay` (login happens here via Keycloak).
- `orders/` — sender-facing service: shipment creation, price calculation, order records.
- `logistics/` — dispatcher/admin service: route planning (via an external routing API), depo
  transit, courier assignment.
- `tracking/` — read-optimized status/tracking service consumed by the public tracking UI.
- `courier/` — courier-facing service: assigned routes, delivery status updates.
- `shared/` — plain (non-Spring-Boot) Maven library with the DTOs, enums, and exceptions that are
  serialized onto Kafka topics and shared across services. Every other backend module depends on
  it as `org.bme.micro_futar:shared:1.0.0`, so it must be `mvn install`ed locally before the other
  modules will build.
- `ui/` — npm workspaces monorepo (`apps/*`, `packages/*`):
  - `apps/client-ui` — sender/recipient app (create shipment, track, dashboard).
  - `apps/logistics-ui` — admin/dispatcher app (uses Leaflet for maps).
  - `apps/courier-ui` — courier PWA.
  - `apps/keycloak-theme` — Keycloakify custom theme for the Keycloak login/account UI, built
    separately and mounted into the `keycloak` container as a provider jar.
  - `packages/shared-core` — shared API clients (generated from each service's OpenAPI spec) and
    other framework-agnostic logic, consumed as `@package/shared-core`.
  - `packages/shared-ui` — shared React components (forms, map widgets), consumed as
    `@package/shared-ui`.
- `misc/` — Helm values for shared infra (Kafka, Postgres, Traefik, Keycloak), the
  `micro-futar-routing` Helm chart, External Secrets Operator config, and image-updater config.
- Each backend service also has its own `helm/` chart (with `values-local/dev/prod.yaml`) and
  `Dockerfile` for deployment.
- `docker-compose.yml` — local infra (Postgres, Zookeeper, Kafka, Keycloak) plus the four backend
  services gated behind the `apps` profile.

## Cross-service architecture

Reference/master data does not live in one place: several services keep local, denormalized
copies of entities they need (e.g. `LocationCity`, `PackageSize`, `Depo`, `Shipment`,
`ShipmentRoute`) and stay in sync via Kafka rather than direct DB or REST calls between services.

- One service owns writes for a given entity and exposes REST CRUD for it (e.g. `orders` owns
  `Shipment` creation; `logistics` owns `Courier`, `Vehicle`, `Depo`, `ShipmentRoute`).
- On write, the owning service's `*Producer`/`KafkaProducerService` serializes the entity's DTO
  (from `shared`) to JSON and publishes it to a topic named in `kafka.topics.*`
  (`application.yaml`, topic names resolved from env vars, defaulted in
  `application-local.yaml`).
- Every interested service runs a `*Consumer` (`@KafkaListener`) that deserializes the message and
  upserts its own local copy via a `*Service`, so each service's Postgres schema is an
  eventually-consistent read replica of the entities it needs plus the entities it owns.
- `logistics/controllers/KafkaBulkSenderController` exists to re-publish current state in bulk
  (useful for backfilling a new/rebuilt consumer).
- Each service has its own Postgres database (`orders_db`, `logistics_db`, `courier_db`,
  `tracking_db`, plus `keycloak_db`), created by `init-multiple-databases.sh` — there is no shared
  schema or cross-service foreign keys.
- Auth: the gateway performs the Keycloak login and token relay; each backend service is an OAuth2
  resource server validating the JWT against Keycloak's JWK set
  (`jwt.auth.converter` config maps the `api-gateway-client` JWT claims to Spring Security
  authorities; see `config/auth/JwtAuthConverter.java` in each service).
- Each backend service exposes OpenAPI at build/test time (`springdoc-openapi`); the UI's
  `generate-*-api` scripts run `swagger-typescript-api` against `<service>/target/openapi.json` to
  regenerate the typed clients in `ui/packages/shared-core/api`. Run the relevant service's tests
  (which include `OpenApiGenerationTest`) before regenerating a client so `target/openapi.json` is
  up to date.

## Common commands

### Backend (Java 25, Maven; each service is an independent Maven module — no reactor/parent pom)

Build `shared` first — every other module depends on it:

```bash
cd shared && mvn clean install -DskipTests
```

Then, from a given service directory (`orders`, `logistics`, `courier`, `tracking` have a Maven
wrapper; `api_gateway` and `shared` do not):

```bash
./mvnw clean test                 # run all tests for that service
./mvnw test -Dtest=ShipmentServiceTests            # run a single test class
./mvnw test -Dtest=ShipmentServiceTests#createsShipment   # run a single test method
./mvnw clean package -DskipTests  # build the jar (also writes target/openapi.json)
./mvnw spring-boot:run            # run the service (needs infra + env vars, see below)
```

Tests that extend `EnableTestContainers` spin up real Postgres/Kafka via Testcontainers and need
Docker running locally.

### Frontend (`ui/`, npm workspaces)

```bash
npm install                       # from ui/, installs all apps/packages
npm run dev:client                # apps/client-ui on :5173
npm run dev:logistics             # apps/logistics-ui on :5174
npm run dev:courier               # apps/courier-ui on :5175
```

Per-app (run inside `ui/apps/<app>/`, or via `npm run <script> --workspace=apps/<app>`):

```bash
npm run lint
npm run build                     # tsc -b && vite build
```

`keycloak-theme` is built separately (`npm run build-keycloak-theme` inside
`ui/apps/keycloak-theme`) and its output is mounted into the `keycloak` service in
`docker-compose.yml`.

### Local infra

```bash
docker compose up -d                       # Postgres, Zookeeper, Kafka, Keycloak
docker compose --profile apps up -d --build  # also build/run orders, logistics, courier, tracking
```

Requires a `.env` at the repo root with `POSTGRES_USER`, `POSTGRES_PASSWORD`,
`KEYCLOAK_ADMIN_PASSWORD`. Services themselves are configured entirely through environment
variables (see each `src/main/resources/application.yaml`); `application-local.yaml` in each
service supplies default Kafka topic names for the `local` Spring profile.
