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
SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
```

The `local` profile (`application-local.yaml`) enables Spring Boot Docker Compose with `podman-compose`. Without it, Docker Compose is disabled (required for CI/AOT). Service definitions live in `compose.yaml` at the project root.

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
- Feature-scoped `@Configuration` only
- `@ComponentScan` scoped to the feature's root package — picks up `@Service`, `@Repository`, etc. within this feature only
- `@EnableJdbcRepositories` scoped to the feature's persistence package — limits Spring Data JDBC repository scanning to this feature

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

### GraalVM reflection gaps (known issue class)

This app has hit GraalVM's native-image reflection gap three times: once for protobuf/OTLP
(`MetricsConfiguration.ProtobufRuntimeHints`), once for Liquibase
(`LiquibaseConfiguration.ChangeRuntimeHints`, added 2026-08-06 to fix a production outage), and
once for the notes event records (`NotesConfiguration.NoteEventRuntimeHints`, added 2026-08-08,
also a production outage). The pattern: something reflectively invokes a method or accessor
GraalVM's default reachability metadata doesn't cover, and the native image throws an
`UnsupportedFeatureError`/`Cannot reflectively invoke method '...'` at *runtime* instead of
failing to build — CI's `test` job never catches this, since it runs on the JVM, not the native
image. Fix by adding a `RuntimeHintsRegistrar` (`@ImportRuntimeHints`) registering the missing
type/method; see any of the three classes for the pattern.

**The general lesson from both outages: verifying a reflection-hint fix means booting the real
native image *and exercising the specific code path* that performs the reflective call** — a
successful app boot proves nothing on its own if the reflective call only happens later, in
response to a request. Both production incidents passed a boot-only smoke test before shipping;
neither would have passed a test that also exercised the actual behavior (a second boot against
an already-migrated database, or an actual note creation through the API).

**Liquibase specifically**: it reflectively calls each `Change`/config class's bean-property
getters to compute a changeset's checksum, and does this on *every* startup to re-validate
already-applied changesets against `DATABASECHANGELOG` — not just when a changeset first runs.
A missing reflection hint can therefore pass a first deploy cleanly (fresh migration takes a
different code path) and then crash-loop on every restart afterward. This is exactly how the
2026-08-06 incident slipped past both CI and the first production deploy.

**When testing a Liquibase reflection fix, you must restart the app at least twice against the
same already-migrated database** — testing only a fresh/first boot will not catch this class of
bug:

```bash
./gradlew bootBuildImage          # builds a local image, no push (omit -Pregistry* / --publishImage)
podman compose up -d postgres     # or your local Postgres, from compose.yaml
podman run --rm --network host --env SPRING_DATASOURCE_URL=... spring-notes:<tag>   # first boot: applies migrations
# stop it, then run the exact same command again
podman run --rm --network host --env SPRING_DATASOURCE_URL=... spring-notes:<tag>   # second boot: re-validates checksums - this is the one that actually exercises the bug
```

`LiquibaseConfiguration.ChangeRuntimeHints` currently covers every `Change`/config class this
app's changelogs use (`CreateTableChange`, `AddColumnChange`, `AddUniqueConstraintChange`,
`CreateIndexChange`, `ColumnConfig`, `AddColumnConfig`, `ConstraintsConfig`). A new changeset
using a different Liquibase `Change` type not in that list may need a new entry.

**NoteEvent records specifically**: `NoteCreated`/`NoteUpdated`/`NoteDeleted` are serialized by
Jackson via a plain `ObjectMapper` call inside `NoteEventStoreAdapter` (event store persistence)
and `NoteEventPublisherAdapter` (RabbitMQ publish) — not an MVC controller signature. Spring
AOT's binding-hint inference only traces JSON types reachable from `@RequestMapping`/
`@ResponseBody` signatures, so it never discovers these, and GraalVM's default reachability
metadata doesn't cover record component accessors for arbitrary application records. The crash
(`Record components not available ... must be included in the reflection configuration`) only
happens the first time one of these records is actually serialized — a boot-only test never
creates a note, so it never hits this path, which is exactly how the 2026-08-08 incident slipped
past verification of the *previous* (Liquibase) fix.

`NotesConfiguration.NoteEventRuntimeHints` uses `BindingReflectionHintsRegistrar` to cover
`NoteCreated`, `NoteUpdated`, and `NoteDeleted`. A new `NoteEvent` subtype, or any other record
serialized via a raw `ObjectMapper` call outside an MVC signature, needs the same treatment.
Verify by booting the real native image and actually creating (or updating/deleting) a note
through the REST API — not just checking that the app starts.

### CI/CD

GitHub Actions (`.github/workflows/build.yml`):
- **test** job: runs on every push and PR against `main`
- **build-native-image** job: runs on push to `main` only; builds and pushes to GHCR as both `:<git-sha>` and `:latest`

Images are published to `ghcr.io/dmfrey/spring-notes`.

## Deployment

Planned deployment to a Kubernetes cluster via FluxCD HelmRelease. FluxCD configuration lives in a separate repository.