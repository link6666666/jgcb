export function backendHttpOrigin() {
  const configured = import.meta.env.VITE_BACKEND_ORIGIN?.trim()
  if (configured) return configured.replace(/\/$/, '')

  const { protocol, hostname, port, origin } = window.location
  if (protocol === 'http:' && (!port || port === '80')) {
    return 'http://' + hostname + ':8080'
  }
  return origin
}

export function backendWebSocketOrigin() {
  const url = new URL(backendHttpOrigin())
  const protocol = url.protocol === 'https:' ? 'wss:' : 'ws:'
  return protocol + '//' + url.host
}