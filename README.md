# spring-notes

[![Uptime](https://kromgo.dmfrey.com/badges/spring_notes_uptime_days)](https://github.com/kashalls/kromgo)&nbsp;&nbsp;
[![Requests](https://kromgo.dmfrey.com/badges/spring_notes_request_rate)](https://github.com/kashalls/kromgo)&nbsp;&nbsp;
[![Error-Rate](https://kromgo.dmfrey.com/badges/spring_notes_error_rate)](https://github.com/kashalls/kromgo)&nbsp;&nbsp;
[![P95-Latency](https://kromgo.dmfrey.com/badges/spring_notes_p95_latency)](https://github.com/kashalls/kromgo)&nbsp;&nbsp;
[![JVM-Heap](https://kromgo.dmfrey.com/badges/spring_notes_jvm_heap)](https://github.com/kashalls/kromgo)&nbsp;&nbsp;
[![DB-Pool](https://kromgo.dmfrey.com/badges/spring_notes_db_pool)](https://github.com/kashalls/kromgo)&nbsp;&nbsp;
[![Restarts](https://kromgo.dmfrey.com/badges/spring_notes_restarts)](https://github.com/kashalls/kromgo)

A notes-taking app built as a Spring Boot / React reference project — event-sourced backend,
OIDC login, GraalVM native image, deployed to Kubernetes via FluxCD. Badges above are live
metrics from the running deployment, served by [kromgo](https://github.com/kashalls/kromgo).

## Tech stack

**Backend**
- Java 25, Spring Boot 4.1.0, compiled to a GraalVM native image
- Spring Data JDBC + Liquibase (PostgreSQL), Spring MVC, event sourcing over RabbitMQ
- OAuth2/OIDC resource server (Authentik in production)
- OpenTelemetry, Micrometer tracing, Prometheus, Dependency-Track for SBOM/vulnerability scanning

**Frontend**
- React + Vite, OIDC login via `oidc-client-ts`
- Vitest (unit) and Playwright (E2E) test suites

**Architecture**: Hexagonal (Ports and Adapters) on the backend — see `CLAUDE.md` for package
conventions and the full local-development, testing, and deployment reference.

## Running it locally

Requires [Podman](https://podman.io/) (not Docker):

```bash
systemctl --user enable --now podman.socket
export DOCKER_HOST=unix:///run/user/$(id -u)/podman/podman.sock
```

Run the backend test suite (spins up Postgres/RabbitMQ/Grafana LGTM via Testcontainers):

```bash
./gradlew test
```

Run the app itself:

```bash
SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
```

Frontend, from `frontend/`:

```bash
npm install
npm run dev        # unit tests: npm test · E2E: npm run test:e2e
```

## Deployment

Deployed to a Kubernetes cluster via a FluxCD `HelmRelease` (config lives in a separate
GitOps repository). Images are published to `ghcr.io/dmfrey/spring-notes` (backend) and
`ghcr.io/dmfrey/spring-notes-frontend` (frontend) on every push to `main` and on release tags.
