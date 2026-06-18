import { createContext, useContext, useEffect, useState } from 'react'
import { loadConfig, createUserManager } from './auth.js'

const AuthContext = createContext(null)

export function useAuth() {
  return useContext(AuthContext)
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(undefined) // undefined = initializing

  useEffect(() => {
    let cancelled = false

    async function init() {
      const config = await loadConfig()
      const mgr = createUserManager(config)

      // Handle OIDC redirect callback — clean URL first so a failed
      // exchange doesn't re-trigger this branch on the next render
      if (window.location.search.includes('code=') && window.location.search.includes('state=')) {
        window.history.replaceState({}, '', '/')
        try {
          const u = await mgr.signinRedirectCallback()
          if (!cancelled) setUser(u)
          return
        } catch (e) {
          console.error('OIDC callback error:', e)
          // Fall through to getUser() — don't redirect immediately or
          // we create a redirect loop when Authentik is briefly unavailable
        }
      }

      // Use existing session or redirect to login
      const u = await mgr.getUser()
      if (cancelled) return

      if (!u || u.expired) {
        await mgr.clearStaleState()
        await mgr.signinRedirect()
        // browser will navigate away; no state update needed
      } else {
        setUser(u)
      }
    }

    init().catch(console.error)
    return () => { cancelled = true }
  }, [])

  // Render nothing while initializing or redirecting to Authentik
  if (user === undefined) return null

  return (
    <AuthContext.Provider value={{ user }}>
      {children}
    </AuthContext.Provider>
  )
}
