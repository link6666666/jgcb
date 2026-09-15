const TOKEN_KEY = 'token'
const USER_KEY = 'user'

export function getToken() {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token) {
  localStorage.setItem(TOKEN_KEY, token)
}

export function removeToken() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
}

export function getUser() {
  const stored = localStorage.getItem(USER_KEY)
  if (!stored) return null
  try {
    return JSON.parse(stored)
  } catch {
    return null
  }
}

export function setUser(user) {
  localStorage.setItem(USER_KEY, JSON.stringify(user))
}

/**
 * Decode JWT payload with proper UTF-8 support (handles Chinese and other multi-byte chars)
 */
function base64UrlDecode(str) {
  str = str.replace(/-/g, '+').replace(/_/g, '/')
  while (str.length % 4) str += '='
  const binary = atob(str)
  const bytes = new Uint8Array(binary.length)
  for (let i = 0; i < binary.length; i++) {
    bytes[i] = binary.charCodeAt(i)
  }
  return new TextDecoder('utf-8').decode(bytes)
}

export function isAuthenticated() {
  const token = getToken()
  if (!token) return false

  try {
    const parts = token.split('.')
    if (parts.length !== 3) return false

    const payload = JSON.parse(base64UrlDecode(parts[1]))
    if (!payload.exp) return false

    const now = Math.floor(Date.now() / 1000)
    if (payload.exp < now) {
      removeToken()
      return false
    }

    return true
  } catch {
    removeToken()
    return false
  }
}
