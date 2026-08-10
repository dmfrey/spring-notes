import { readFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import { GenericContainer, Wait } from 'testcontainers'

const CONFIG_PATH = fileURLToPath(new URL('../dex/config.yaml', import.meta.url))
const DEX_PORT = 5556

export const dexCredentials = {
  clientId: 'spring-notes-e2e',
  username: 'e2e@example.com',
  password: 'e2e-test-password',
}

// Fixed host port, not a Testcontainers-assigned random one: Dex's `issuer:` URL in
// config.yaml has to be baked in before the container starts, which is a chicken-and-egg
// problem with random ports. This means two instances of this suite can't run in parallel
// on the same host - not a concern for CI (serial job) or typical local dev.
export async function startDex() {
  const config = readFileSync(CONFIG_PATH, 'utf-8')

  const container = await new GenericContainer(`dexidp/dex:v2.45.1`)
    .withExposedPorts({ container: DEX_PORT, host: DEX_PORT })
    .withCopyContentToContainer([{ content: config, target: '/etc/dex/config.docker.yaml' }])
    .withWaitStrategy(Wait.forLogMessage(/listening on/))
    .start()

  return {
    issuerUrl: `http://localhost:${DEX_PORT}/dex`,
    ...dexCredentials,
    async stop() {
      await container.stop()
    },
  }
}
