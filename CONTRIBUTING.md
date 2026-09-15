# Contributing

spring-notes is a personal reference project — a working example of Spring Boot 4 /
GraalVM native image / event sourcing / hexagonal architecture, run as a live deployment.
It's maintained by one person in their spare time, so please set expectations accordingly:
issues and PRs are welcome, but review may not be fast.

## Reporting bugs / suggesting features

Open an issue using the appropriate template. For bugs, include steps to reproduce and
what you expected instead. For anything security-related, see [SECURITY.md](SECURITY.md)
instead of opening a public issue.

## Making changes

1. Fork the repo and create a branch off `main`.
2. Read [CLAUDE.md](CLAUDE.md) first — it documents the hexagonal architecture conventions
   (package layout, port/adapter naming, how to add a feature) that PRs are expected to
   follow, plus local dev setup (Podman, Testcontainers) and known GraalVM native-image
   gotchas worth knowing before touching anything AOT- or reflection-related.
3. Run the relevant test suite before opening a PR:
   - Backend: `./gradlew test`
   - Frontend: `npm test` (unit) / `npm run test:e2e` (from `frontend/`)
4. Open a PR against `main`. CI runs the backend test suite, a Dependency-Track
   vulnerability scan, and (for frontend changes) the frontend build/E2E suite — all must
   pass before a PR can merge, along with CodeQL static analysis.
5. Keep PRs focused. A bug fix doesn't need a refactor riding along with it.

## Code style

- Backend: standard Java conventions, hexagonal architecture as documented in CLAUDE.md.
  No inline comments explaining *what* code does — only *why*, when genuinely non-obvious.
- Frontend: React functional components, no class components.
- Commit messages: [Conventional Commits](https://www.conventionalcommits.org/) style
  (`feat:`, `fix:`, `ci:`, etc.) — this repo uses release-please, which generates the
  changelog and version bumps directly from commit prefixes.

## Questions

Open a [discussion-style issue](https://github.com/dmfrey/spring-notes/issues/new) if
something in CLAUDE.md or the codebase itself doesn't answer it.
