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

      // Handle OIDC redirect callback
      if (window.location.search.includes('code=') && window.location.search.includes('state=')) {
        try {
          const u = await mgr.signinRedirectCallback()
          window.history.replaceState({}, '', '/')
          if (!cancelled) setUser(u)
        } catch (e) {
          console.error('OIDC callback error:', e)
          await mgr.signinRedirect()
        }
        return
      }

      // Use existing session or redirect to login
      const u = await mgr.getUser()
      if (cancelled) return

      if (!u || u.expired) {
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
