# Changelog

## [3.4.0](https://github.com/dmfrey/spring-notes/compare/v3.3.0...v3.4.0) (2026-08-11)


### Features

* **ci:** add backend coverage and E2E result summaries ([e482902](https://github.com/dmfrey/spring-notes/commit/e482902d1fd4660a53a66a16b7c20abd049ec4d2))
* **ci:** add backend coverage and E2E result summaries ([4d84690](https://github.com/dmfrey/spring-notes/commit/4d846902f6c1d1623d34189a3b1a7e7ed86c4050))
* **ci:** submit SBOM to Dependency-Track on push to main/tags ([bffe43f](https://github.com/dmfrey/spring-notes/commit/bffe43fc5e8b756abb0ebf0d7be1f164c2d0be8b))
* **ci:** submit SBOM to Dependency-Track on push to main/tags ([8214adc](https://github.com/dmfrey/spring-notes/commit/8214adc9ce9bd97da7873d053a79174e8ab6fe70))
* **ci:** wait for Dependency-Track processing, gate on Critical findings ([600db1a](https://github.com/dmfrey/spring-notes/commit/600db1ad4331fb5aa13274c5118051f3d3f6a05b))
* **ci:** wait for Dependency-Track processing, gate on Critical findings ([bdaff8f](https://github.com/dmfrey/spring-notes/commit/bdaff8fb64f11b0f3cba1b72a097c1439335f8e8))


### Bug Fixes

* **deps:** bump logback, netty, and postgresql past 3 High CVEs ([15995fa](https://github.com/dmfrey/spring-notes/commit/15995fad9e515aedc6159a7ac536c01b512b5164))

## [3.3.0](https://github.com/dmfrey/spring-notes/compare/v3.2.3...v3.3.0) (2026-08-10)


### Features

* **gradle:** update gradle ( 9.6.1 ➔ 9.7.0 ) ([6f9c7f7](https://github.com/dmfrey/spring-notes/commit/6f9c7f79154233c20bbc9348931bf3292785d27d))
* **gradle:** update gradle ( 9.6.1 ➔ 9.7.0 ) ([7267012](https://github.com/dmfrey/spring-notes/commit/7267012b229af820d5c94f04cce55b8f1d70806d))
* **gradle:** update plugin org.cyclonedx.bom ( 3.3.0 ➔ 3.4.0 ) ([64237d4](https://github.com/dmfrey/spring-notes/commit/64237d4dbac64a91927c1a32f59fa049ae831441))
* **gradle:** update plugin org.cyclonedx.bom ( 3.3.0 ➔ 3.4.0 ) ([64f4073](https://github.com/dmfrey/spring-notes/commit/64f407385b681538e06ebab49abc4461548df3d2))


### Bug Fixes

* **frontend:** exclude e2e/ from vitest's own test discovery ([cf31443](https://github.com/dmfrey/spring-notes/commit/cf31443608ebbca99567bb69bb697c4d1795eb02))
* **gradle:** update dependency org.openrewrite.recipe:rewrite-spring ( 6.36.0 ➔ 6.36.1 ) ([3feb708](https://github.com/dmfrey/spring-notes/commit/3feb7086db3805ae19dfb84a656e78ddb14fb5d8))
* **gradle:** update plugin org.graalvm.buildtools.native ( 1.1.7 ➔ 1.1.8 ) ([0908ccd](https://github.com/dmfrey/spring-notes/commit/0908ccd784bbaaa2054c06283a2058ef490abe9f))
* **npm:** update dependency jsdom ( 30.0.0 ➔ 30.0.1 ) ([f2f25cd](https://github.com/dmfrey/spring-notes/commit/f2f25cd579399e8362a3f81b8990261dba02a741))
* **npm:** update dependency jsdom ( 30.0.0 ➔ 30.0.1 ) ([301c547](https://github.com/dmfrey/spring-notes/commit/301c547f723ad82cd6e7106c9643c9e0fca15e19))

## [3.2.3](https://github.com/dmfrey/spring-notes/compare/v3.2.2...v3.2.3) (2026-08-09)


### Bug Fixes

* **frontend:** pass captured URL to signinRedirectCallback ([c848219](https://github.com/dmfrey/spring-notes/commit/c848219117fc2dd547c4c45f4ca2e4cd6829f92d))
* **frontend:** pass captured URL to signinRedirectCallback ([0ea0725](https://github.com/dmfrey/spring-notes/commit/0ea07253b8328110dfba5ce6263c81c027201b7d))

## [3.2.2](https://github.com/dmfrey/spring-notes/compare/v3.2.1...v3.2.2) (2026-08-09)


### Bug Fixes

* register GraalVM reflection hints for NoteEvent records ([cd844e3](https://github.com/dmfrey/spring-notes/commit/cd844e3fc9b768de0bdeb4f902af5b9a067e1ff4))
* register GraalVM reflection hints for NoteEvent records ([0757def](https://github.com/dmfrey/spring-notes/commit/0757def63bb202b28bb3de662e6fe3a8426dbd1b))

## [3.2.1](https://github.com/dmfrey/spring-notes/compare/v3.2.0...v3.2.1) (2026-08-07)


### Bug Fixes

* **github-action:** update action renovatebot/github-action ( v46.2.0 ➔ v46.2.1 ) ([54010da](https://github.com/dmfrey/spring-notes/commit/54010da84704b7c03fa032141906ad73cc4fb256))
* **gradle:** bind resolved DOCKER_HOST socket into bootBuildImage lifecycle containers ([3cb117a](https://github.com/dmfrey/spring-notes/commit/3cb117a7e6fc320ebbe27d9eea8ec133685c79b6))
* register GraalVM reflection hints for Liquibase Change classes ([#51](https://github.com/dmfrey/spring-notes/issues/51)) ([c285772](https://github.com/dmfrey/spring-notes/commit/c285772d450f382bd11909b46b07eeb8f5315704))

## [3.2.0](https://github.com/dmfrey/spring-notes/compare/v3.1.0...v3.2.0) (2026-08-05)


### Features

* **notes:** backfill NoteCreated events for pre-migration notes ([#48](https://github.com/dmfrey/spring-notes/issues/48)) ([f17d0e1](https://github.com/dmfrey/spring-notes/commit/f17d0e1ffa5ab0b21c5e81f784aa5247399e2ca9))

## [3.1.0](https://github.com/dmfrey/spring-notes/compare/v3.0.0...v3.1.0) (2026-08-05)


### Features

* **notes:** migrate notes to event sourcing, add Update, publish events via RabbitMQ ([#44](https://github.com/dmfrey/spring-notes/issues/44)) ([e0695ea](https://github.com/dmfrey/spring-notes/commit/e0695eadbdb70bb4226d9fd666dcdd95b8dc46bf))

## [3.0.0](https://github.com/dmfrey/spring-notes/compare/v2.0.0...v3.0.0) (2026-08-04)


### ⚠ BREAKING CHANGES

* **npm:** Update dependency jsdom ( 29.1.1 ➔ 30.0.0 ) ([#42](https://github.com/dmfrey/spring-notes/issues/42))
* **github-action:** Update action actions/setup-node ( v6.5.0 ➔ v7.0.0 ) ([#39](https://github.com/dmfrey/spring-notes/issues/39))

### Features

* **github-action:** Update action actions/setup-node ( v6.5.0 ➔ v7.0.0 ) ([#39](https://github.com/dmfrey/spring-notes/issues/39)) ([1e74922](https://github.com/dmfrey/spring-notes/commit/1e74922d626dec05e220a8972d4a4d5d445e6cea))
* **github-action:** update action renovatebot/github-action ( v46.1.21 ➔ v46.2.0 ) ([d4d0475](https://github.com/dmfrey/spring-notes/commit/d4d04750cd0345a95010eadbeaa93655e8888f20))
* **gradle:** update dependency org.openrewrite.recipe:rewrite-java-dependencies ( 1.55.3 ➔ 1.60.1 ) ([#29](https://github.com/dmfrey/spring-notes/issues/29)) ([4a80ccb](https://github.com/dmfrey/spring-notes/commit/4a80ccb227e845edaf1b01f599b26852e7d7c53f))
* **gradle:** update dependency org.openrewrite.recipe:rewrite-spring ( 6.32.1 ➔ 6.36.0 ) ([#30](https://github.com/dmfrey/spring-notes/issues/30)) ([9c52906](https://github.com/dmfrey/spring-notes/commit/9c529063a51c19d418beba90f6f421744ab52c35))
* **gradle:** update gradle ( 9.5.1 ➔ 9.6.1 ) ([#31](https://github.com/dmfrey/spring-notes/issues/31)) ([91ef10a](https://github.com/dmfrey/spring-notes/commit/91ef10a945f369e7c553b0f4b3d3435bf88af264))
* **gradle:** update plugin com.gradle.develocity ( 4.4.3 ➔ 4.5.0 ) ([#37](https://github.com/dmfrey/spring-notes/issues/37)) ([f1c5ce1](https://github.com/dmfrey/spring-notes/commit/f1c5ce15fd0dbf35814a63c1debe7bb1bbb15e49))
* **gradle:** update plugin org.cyclonedx.bom ( 3.2.4 ➔ 3.3.0 ) ([#40](https://github.com/dmfrey/spring-notes/issues/40)) ([7935bd5](https://github.com/dmfrey/spring-notes/commit/7935bd5a37965cb90e9f73b7be06a7e2974ec140))
* **gradle:** update plugin org.openrewrite.rewrite ( 7.34.0 ➔ 7.38.0 ) ([#32](https://github.com/dmfrey/spring-notes/issues/32)) ([272affb](https://github.com/dmfrey/spring-notes/commit/272affb7a39d51c66ccd3b7ec5e231db0e05e21c))
* **npm:** Update dependency jsdom ( 29.1.1 ➔ 30.0.0 ) ([#42](https://github.com/dmfrey/spring-notes/issues/42)) ([fe82b98](https://github.com/dmfrey/spring-notes/commit/fe82b984e394538de899ddf4916368772db12fdd))
* **npm:** update opentelemetry-frontend group ([#38](https://github.com/dmfrey/spring-notes/issues/38)) ([115818a](https://github.com/dmfrey/spring-notes/commit/115818ad67586a10c4d3514909f0b32053dd903e))
* **npm:** update vite-vitest group ([#35](https://github.com/dmfrey/spring-notes/issues/35)) ([9eef7ce](https://github.com/dmfrey/spring-notes/commit/9eef7ce82a40233e1e8a67efcf6b6aef5374ebce))


### Bug Fixes

* **github-action:** update action actions/checkout ( v7.0.0 ➔ v7.0.1 ) ([a5c6b9b](https://github.com/dmfrey/spring-notes/commit/a5c6b9b97d734b17b5250bb435708351eb02b1e6))
* **github-action:** update action renovatebot/github-action ( v46.1.15 ➔ v46.1.16 ) ([6f954ff](https://github.com/dmfrey/spring-notes/commit/6f954ff613c8f4e36b165077c53fa0c47e10d103))
* **github-action:** update action renovatebot/github-action ( v46.1.16 ➔ v46.1.17 ) ([b44cb37](https://github.com/dmfrey/spring-notes/commit/b44cb375eaef0d5a0da20e79ea5fef9de583b77f))
* **github-action:** update action renovatebot/github-action ( v46.1.17 ➔ v46.1.18 ) ([f7a471b](https://github.com/dmfrey/spring-notes/commit/f7a471bbfebca2df2e6da7f93654a27815e56792))
* **github-action:** update action renovatebot/github-action ( v46.1.18 ➔ v46.1.19 ) ([1457b6f](https://github.com/dmfrey/spring-notes/commit/1457b6f6831b196dfd64c71340a9caf677a7c45f))
* **github-action:** update action renovatebot/github-action ( v46.1.19 ➔ v46.1.20 ) ([f2316d4](https://github.com/dmfrey/spring-notes/commit/f2316d4f159ef2fe16d156c12d4fe0ec6e7d1b5b))
* **github-action:** update action renovatebot/github-action ( v46.1.20 ➔ v46.1.21 ) ([b6ec1b7](https://github.com/dmfrey/spring-notes/commit/b6ec1b76719a5014e47efaf8a5f7f3d0fc401f8b))
* **gradle:** update plugin org.graalvm.buildtools.native ( 1.1.2 ➔ 1.1.7 ) ([#36](https://github.com/dmfrey/spring-notes/issues/36)) ([cca7f05](https://github.com/dmfrey/spring-notes/commit/cca7f0559e1f685358be9ca732972fa0e34be62e))
* **npm:** update react monorepo ( 19.2.7 ➔ 19.2.8 ) ([#41](https://github.com/dmfrey/spring-notes/issues/41)) ([d3b35c8](https://github.com/dmfrey/spring-notes/commit/d3b35c8424940da31b3cfeb8d223a574146d7ab5))

## [2.0.0](https://github.com/dmfrey/spring-notes/compare/v1.1.0...v2.0.0) (2026-06-18)


### ⚠ BREAKING CHANGES

* **npm:** Update vite-vitest group ([#27](https://github.com/dmfrey/spring-notes/issues/27))
* **npm:** Update dependency jsdom ( 26.1.0 ➔ 29.1.1 ) ([#26](https://github.com/dmfrey/spring-notes/issues/26))
* **github-action:** Update GitHub Artifact Actions ( v4.6.2 ➔ v7.0.1 ) ([#25](https://github.com/dmfrey/spring-notes/issues/25))
* **github-action:** Update action peter-evans/create-pull-request ( v7.0.11 ➔ v8.1.1 ) ([#24](https://github.com/dmfrey/spring-notes/issues/24))
* **github-action:** Update action node ( 22.23.0 ➔ 24.17.0 ) ([#23](https://github.com/dmfrey/spring-notes/issues/23))
* **github-action:** Update action gradle/actions ( v4.4.4 ➔ v6.2.0 ) ([#22](https://github.com/dmfrey/spring-notes/issues/22))
* **github-action:** Update action dorny/test-reporter ( v1.9.1 ➔ v3.0.0 ) ([#20](https://github.com/dmfrey/spring-notes/issues/20))
* **github-action:** Update action docker/login-action ( v3.7.0 ➔ v4.2.0 ) ([#19](https://github.com/dmfrey/spring-notes/issues/19))
* **github-action:** Update action docker/build-push-action ( v6.19.2 ➔ v7.2.0 ) ([#18](https://github.com/dmfrey/spring-notes/issues/18))
* **github-action:** Update action actions/checkout to v7.0.0 ([#15](https://github.com/dmfrey/spring-notes/issues/15))
* **github-action:** Update action googleapis/release-please-action ( v4.4.1 ➔ v5.0.0 ) ([#21](https://github.com/dmfrey/spring-notes/issues/21))
* **github-action:** Update action actions/setup-node ( v4.4.0 ➔ v6.4.0 ) ([#17](https://github.com/dmfrey/spring-notes/issues/17))
* **github-action:** Update action actions/setup-java ( v4.8.0 ➔ v5.3.0 ) ([#16](https://github.com/dmfrey/spring-notes/issues/16))

### Features

* **github-action:** Update action actions/checkout to v7.0.0 ([#15](https://github.com/dmfrey/spring-notes/issues/15)) ([28bbc4c](https://github.com/dmfrey/spring-notes/commit/28bbc4ca4dd17249f8e7e06daf9653ab75bc7681))
* **github-action:** Update action actions/setup-java ( v4.8.0 ➔ v5.3.0 ) ([#16](https://github.com/dmfrey/spring-notes/issues/16)) ([a5ded50](https://github.com/dmfrey/spring-notes/commit/a5ded505053107663781c9f12acef510a25abf62))
* **github-action:** Update action actions/setup-node ( v4.4.0 ➔ v6.4.0 ) ([#17](https://github.com/dmfrey/spring-notes/issues/17)) ([9422936](https://github.com/dmfrey/spring-notes/commit/9422936f0c8feab27e69fd174fefb8dba5f378c9))
* **github-action:** Update action docker/build-push-action ( v6.19.2 ➔ v7.2.0 ) ([#18](https://github.com/dmfrey/spring-notes/issues/18)) ([03e35c7](https://github.com/dmfrey/spring-notes/commit/03e35c73628f70e0628f200b73f8611899766dda))
* **github-action:** Update action docker/login-action ( v3.7.0 ➔ v4.2.0 ) ([#19](https://github.com/dmfrey/spring-notes/issues/19)) ([d2177ff](https://github.com/dmfrey/spring-notes/commit/d2177ff3e1a4a9245c9fe0f63a176318abae44a9))
* **github-action:** Update action dorny/test-reporter ( v1.9.1 ➔ v3.0.0 ) ([#20](https://github.com/dmfrey/spring-notes/issues/20)) ([9b328ac](https://github.com/dmfrey/spring-notes/commit/9b328acb016e15afa84a28ed08322a9e159ab286))
* **github-action:** Update action googleapis/release-please-action ( v4.4.1 ➔ v5.0.0 ) ([#21](https://github.com/dmfrey/spring-notes/issues/21)) ([b8d0b93](https://github.com/dmfrey/spring-notes/commit/b8d0b9345121fa0e09141a659477120e451c9855))
* **github-action:** Update action gradle/actions ( v4.4.4 ➔ v6.2.0 ) ([#22](https://github.com/dmfrey/spring-notes/issues/22)) ([41c6448](https://github.com/dmfrey/spring-notes/commit/41c64487d92060e76db75bf21f488f0beb5c5e79))
* **github-action:** Update action node ( 22.23.0 ➔ 24.17.0 ) ([#23](https://github.com/dmfrey/spring-notes/issues/23)) ([3c001d7](https://github.com/dmfrey/spring-notes/commit/3c001d7b1abe751dd2667e58dc5f61fbd44f1024))
* **github-action:** Update action peter-evans/create-pull-request ( v7.0.11 ➔ v8.1.1 ) ([#24](https://github.com/dmfrey/spring-notes/issues/24)) ([46442cf](https://github.com/dmfrey/spring-notes/commit/46442cfb0e1f5c381679057d48ce101f54957859))
* **github-action:** Update GitHub Artifact Actions ( v4.6.2 ➔ v7.0.1 ) ([#25](https://github.com/dmfrey/spring-notes/issues/25)) ([d33ba43](https://github.com/dmfrey/spring-notes/commit/d33ba4390c4a5cab32e90fd5fb6151ad3a97c4dd))
* **gradle:** update gradle ( 9.4.1 ➔ 9.5.1 ) ([#13](https://github.com/dmfrey/spring-notes/issues/13)) ([8be3da5](https://github.com/dmfrey/spring-notes/commit/8be3da547a51fa9419a9f7de4e4ef7a863d76685))
* **npm:** Update dependency jsdom ( 26.1.0 ➔ 29.1.1 ) ([#26](https://github.com/dmfrey/spring-notes/issues/26)) ([f5e7a71](https://github.com/dmfrey/spring-notes/commit/f5e7a71e166382ceb3085d050d581fafe744e31d))
* **npm:** update opentelemetry-frontend group ([#14](https://github.com/dmfrey/spring-notes/issues/14)) ([8ac9186](https://github.com/dmfrey/spring-notes/commit/8ac9186cb5cbb70511e1bf30ed64244a13208fb6))
* **npm:** Update vite-vitest group ([#27](https://github.com/dmfrey/spring-notes/issues/27)) ([348b436](https://github.com/dmfrey/spring-notes/commit/348b436426b855618e96bb99d0edbbb9161ba947))


### Bug Fixes

* correct Renovate hourly limit option name ([5579dfa](https://github.com/dmfrey/spring-notes/commit/5579dfad69f11cff67399e4cfbc915b631214c1b))
* **frontend:** stop OIDC redirect loop on callback failure ([0ea68a9](https://github.com/dmfrey/spring-notes/commit/0ea68a90cdd060fc3bb6b8175feb6d0adbed2d74))
* move allowedUnsafeExecutions to workflow global config ([981c8f8](https://github.com/dmfrey/spring-notes/commit/981c8f8951f13032ae44d62dad20123e96fcaec5))
* **npm:** update react monorepo ( 19.2.4 ➔ 19.2.7 ) ([#10](https://github.com/dmfrey/spring-notes/issues/10)) ([5ff0c09](https://github.com/dmfrey/spring-notes/commit/5ff0c092ddded6575c8223cb3e4645570ce62a11))
* **npm:** update vite-vitest group ([#11](https://github.com/dmfrey/spring-notes/issues/11)) ([3fcda7b](https://github.com/dmfrey/spring-notes/commit/3fcda7ba9eb3f8573d71c0aedccb341a93c60710))

## [1.1.0](https://github.com/dmfrey/spring-notes/compare/v1.0.3...v1.1.0) (2026-06-12)


### Features

* **notes:** add Micrometer observations to domain services ([79d5570](https://github.com/dmfrey/spring-notes/commit/79d557091ad9d1f1638415520e0074762c334a7a))
* **notes:** implement delete note with confirmation dialog ([9a15aed](https://github.com/dmfrey/spring-notes/commit/9a15aeda017f0890fe60fd7226b5a3d1bdc05bdd))


### Bug Fixes

* **frontend:** scope confirm-dialog delete click with within() ([d4a461f](https://github.com/dmfrey/spring-notes/commit/d4a461fc989032f08a7e8a05ead49918b26c32d1))
* **frontend:** tighten delete confirmation dialog test assertions ([88108fc](https://github.com/dmfrey/spring-notes/commit/88108fc8390e145d2f2e93348838380cedc1c215))
* register protobuf reflection hint for GraalVM native image ([eb5d5be](https://github.com/dmfrey/spring-notes/commit/eb5d5bea276455bb8e4e4c95c36fdde7576b892d))

## [1.0.3](https://github.com/dmfrey/spring-notes/compare/v1.0.2...v1.0.3) (2026-04-16)


### Bug Fixes

* correct OpenRewrite recipe names ([64b2d77](https://github.com/dmfrey/spring-notes/commit/64b2d77111927165df6ea5325c4fd5343f1f84b6))

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
