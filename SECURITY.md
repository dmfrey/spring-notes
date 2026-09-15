# Security Policy

## Reporting a vulnerability

Please **do not** open a public GitHub issue for a suspected vulnerability.

Use GitHub's private reporting instead: go to the
[Security tab](https://github.com/dmfrey/spring-notes/security) → **Report a vulnerability**.
This opens a private draft advisory visible only to the maintainer, so the issue isn't
public until there's a fix. If you'd rather not use GitHub, email dmfrey@gmail.com.

Please include:
- What you found and where (endpoint, file, dependency)
- Steps to reproduce, or a proof of concept
- The potential impact as you see it

This is a one-person side project, not a funded security effort — there's no bug bounty
and no formal SLA, but reports are taken seriously and acknowledged as soon as possible.

## What's already in place

This repo runs continuous automated scanning, for context on what's already covered before
reporting something:

- **[Dependency-Track](https://dtrack.dmfrey.com)** scans every dependency change (PR and
  push) for known CVEs in third-party libraries, gated as a required check — a PR
  introducing a CRITICAL/HIGH vulnerability can't merge.
- **[CodeQL](https://github.com/dmfrey/spring-notes/security/code-scanning)** runs static
  analysis on both the Java backend and the React frontend on every PR and weekly.
- **[Renovate](https://github.com/dmfrey/spring-notes/pulls?q=is%3Apr+label%3Arenovate)**
  keeps dependencies current automatically.
- **GitGuardian** scans every push for committed secrets.
- The GitHub [dependency graph](https://github.com/dmfrey/spring-notes/network/dependencies)
  is submitted from the real resolved Gradle graph, not just parsed manifests.

A vulnerability in one of this project's *own* third-party dependencies is most useful
reported upstream to that project directly; open an issue here only if it's specific to how
spring-notes uses it, or if you're unsure where it belongs.

## Supported versions

Only the latest release is supported. This is a live-deployed reference project with a
single running instance, not a library with multiple maintained version lines.
