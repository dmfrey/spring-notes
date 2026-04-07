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