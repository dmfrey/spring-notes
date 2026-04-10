import { UserManager } from 'oidc-client-ts'

let _userManager = null

export async function loadConfig() {
  const res = await fetch('/config.json')
  if (!res.ok) throw new Error('Failed to load /config.json')
  return res.json()
}

export function createUserManager(config) {
  _userManager = new UserManager({
    authority: config.OIDC_ISSUER,
    client_id: config.OIDC_CLIENT_ID,
    redirect_uri: window.location.origin + '/',
    scope: config.OIDC_SCOPE ?? 'openid profile email',
    response_type: 'code',
  })
  return _userManager
}

export function getUserManager() {
  return _userManager
}
