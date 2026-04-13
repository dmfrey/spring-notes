# Changelog

## [1.0.2](https://github.com/dmfrey/spring-notes/compare/v1.0.1...v1.0.2) (2026-04-13)


### Bug Fixes

* polyfill HTMLDialogElement in jsdom test environment ([946ef25](https://github.com/dmfrey/spring-notes/commit/946ef2587e71e0ebc28cc365798946f8511fc54b))

## [1.0.1](https://github.com/dmfrey/spring-notes/compare/v1.0.0...v1.0.1) (2026-04-12)


### Bug Fixes

* **ci:** set JWT issuer URI during native image AOT build ([9a96048](https://github.com/dmfrey/spring-notes/commit/9a9604892a5c11ffc779591b0584b84a8de7addf))
* suppress JvmGcMetrics in native image to eliminate GC warning ([a4c6b0a](https://github.com/dmfrey/spring-notes/commit/a4c6b0aef17f4d098fb7b72c07c9083cbe8c9c92))

## 1.0.0 (2026-04-10)


### Features

* **frontend:** add OIDC authentication via Authentik using oidc-client-ts ([d349c1f](https://github.com/dmfrey/spring-notes/commit/d349c1f075c29425fb2d4aab019eeee287f357ea))
* **frontend:** add OpenTelemetry browser tracing ([c292ade](https://github.com/dmfrey/spring-notes/commit/c292ade55957a742075ab7ac2aac17347cfa8f7a))


### Bug Fixes

* **otel:** use spanProcessors constructor option for OTel SDK v2 compatibility ([ed75390](https://github.com/dmfrey/spring-notes/commit/ed75390067749e00f4fc08a02e6c2de3736457dd))
