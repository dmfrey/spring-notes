# spring-notes

Spring Boot 4.0.5 application targeting Kubernetes deployment via FluxCD HelmRelease.

## Tech Stack

- **Java 25** (toolchain) — temporarily on 25; upgrade to 26 when `native-image-svm 26` is available in the Paketo BellSoft Liberica buildpack ([track here](https://github.com/paketo-buildpacks/bellsoft-liberica/releases))
- **Spring Boot 4.0.5** with GraalVM native image (`org.graalvm.buildtools.native`)
- **Spring Data JDBC** + **Liquibase** (PostgreSQL)
- **Spring MVC** (webmvc)
- **Observability**: OpenTelemetry, Micrometer tracing (Brave bridge), Prometheus, datasource-micrometer
- **Testcontainers**: PostgreSQL + Grafana LGTM stack

## Local Development

### Prerequisites

This project uses **Podman** (not Docker). Before running tests or the app locally:

```bash
# Enable Podman socket (once)
systemctl --user enable --now podman.socket

# Set DOCKER_HOST (add to ~/.bashrc)
export DOCKER_HOST=unix:///run/user/$(id -u)/podman/podman.sock

# Install podman-compose (once)
sudo apt install podman-compose
```

### Running Tests

```bash
./gradlew test
```

Testcontainers is configured with Ryuk disabled (`src/test/resources/testcontainers.properties`) for compatibility with rootless Podman.

### Running the App

```bash
./gradlew bootRun
```

Spring Boot Docker Compose is configured to use `podman-compose` (`spring.docker.compose.executable`). Place service definitions in `compose.yaml` at the project root.

## Architecture

This application follows **Hexagonal Architecture** (Ports and Adapters). Features are the primary unit of organisation — each feature is a self-contained module under the base package `com.broadcom.springconsulting.spring_notes`.

### Package Structure

```
com.broadcom.springconsulting.spring_notes
├── configuration/                        ← cross-cutting Spring configuration
└── <feature>/                            ← e.g. notes
    ├── adapter/
    │   ├── in/
    │   │   └── endpoint/                 ← REST controllers (other types: messaging, graphql, …)
    │   └── out/
    │       └── persistence/              ← DB adapters (other types: messaging, external APIs, …)
    ├── application/
    │   ├── domain/
    │   │   ├── model/                    ← domain model (Java Records; may evolve)
    │   │   └── service/                  ← one service class per use case
    │   └── port/
    │       ├── in/                       ← input port interfaces (UseCase + inner Command record)
    │       └── out/                      ← output port interfaces
    └── configuration/                    ← feature-scoped Spring configuration
```

### Conventions

**Input Ports** (`application/port/in/`):
- One interface per use case, named `<Verb><Feature>UseCase` (e.g., `CreateNoteUseCase`)
- Single method: `execute(Command command)`
- `Command` is an inner record on the interface itself

```java
public interface CreateNoteUseCase {
    Note execute(CreateNoteCommand command);

    record CreateNoteCommand(String title, String content) {}
}
```

The verb-prefixed `Command` name (e.g., `CreateNoteCommand`) keeps commands identifiable when they cross boundaries — important if the system evolves toward event-driven messaging.

**Output Ports** (`application/port/out/`):
- One interface per operation, verb-first, ending with `Port` (e.g., `LoadNotePort`, `SaveNotePort`)

**Domain Services** (`application/domain/service/`):
- One service class per use case, implementing the corresponding interface (e.g., `CreateNoteService implements CreateNoteUseCase`)

**Domain Model** (`application/domain/model/`):
- Java Records to start; may gain behaviour as requirements evolve
- Named after the real-world concept, singular, no suffix (e.g., `Note` not `NoteModel`)

**Input Adapters** (`adapter/in/`):
- Thin — delegate all work to input port interfaces; contain no business logic
- Current type: `endpoint` (REST via Spring MVC)

**Output Adapters** (`adapter/out/`):
- Implement output port interfaces; encapsulate the output technology
- Current type: `persistence` (Spring Data JDBC)

**Feature Configuration** (`<feature>/configuration/`):
- Feature-scoped `@Configuration` classes only — e.g., Spring Data JDBC repository scanning limited to this feature's packages

**Root Configuration** (`com.broadcom.springconsulting.spring_notes.configuration/`):
- Cross-cutting concerns only (security, observability config, etc.)

### Adding a New Feature

1. Create the package tree under `com.broadcom.springconsulting.spring_notes.<feature>`
2. Define domain model records in `application/domain/model/`
3. Define input port interfaces (with inner `Command` records) in `application/port/in/`
4. Define output port interfaces in `application/port/out/`
5. Implement one service per use case in `application/domain/service/`
6. Implement input adapter(s) in `adapter/in/endpoint/` (thin — call the port)
7. Implement output adapter(s) in `adapter/out/persistence/` (implement the port)
8. Wire everything in `<feature>/configuration/`
9. Add Liquibase changeset(s) in `src/main/resources/db/changelog/`

## Database Migrations

Liquibase changelogs live in `src/main/resources/db/changelog/`. The master changelog is `db.changelog-master.yaml`. Add new changesets as separate files and include them from the master.

## Build

### Native Image (CI)

The CI workflow builds a native container image via Cloud Native Buildpacks:

```bash
./gradlew bootBuildImage
```

Registry credentials are passed as Gradle properties (`-PregistryUrl`, `-PregistryUsername`, `-PregistryPassword`).

### CI/CD

GitHub Actions (`.github/workflows/build.yml`):
- **test** job: runs on every push and PR against `main`
- **build-native-image** job: runs on push to `main` only; builds and pushes to GHCR as both `:<git-sha>` and `:latest`

Images are published to `ghcr.io/dmfrey/spring-notes`.

## Deployment

Planned deployment to a Kubernetes cluster via FluxCD HelmRelease. FluxCD configuration lives in a separate repository.