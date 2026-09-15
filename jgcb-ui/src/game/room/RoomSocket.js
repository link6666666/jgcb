import { backendWebSocketOrigin } from '../../utils/runtime'

export default class RoomSocket {
  constructor(roomId, handlers = {}) {
    this.roomId = roomId
    this.handlers = handlers
    this.closedByUser = false
    this.retryCount = 0
    this.connectionId = 0
  }

  connect() {
    const connectionId = ++this.connectionId
    this.closedByUser = false
    clearTimeout(this.retryTimer)
    const token = localStorage.getItem('token')
    if (!token) {
      this.handlers.onStatus?.('error')
      return
    }
    const url = backendWebSocketOrigin() + '/ws/room?roomId=' + encodeURIComponent(this.roomId) + '&token=' + encodeURIComponent(token)
    this.handlers.onStatus?.('connecting')
    this.socket = new WebSocket(url)
    this.socket.addEventListener('open', () => {
      if (connectionId !== this.connectionId) return
      this.retryCount = 0
      this.handlers.onStatus?.('connected')
    })
    this.socket.addEventListener('message', event => {
      if (connectionId !== this.connectionId) return
      try { this.handlers.onMessage?.(JSON.parse(event.data)) } catch { /* Ignore malformed server messages. */ }
    })
    this.socket.addEventListener('close', event => {
      if (connectionId !== this.connectionId) return
      this.handlers.onStatus?.(this.closedByUser ? 'closed' : 'reconnecting')
      if (!this.closedByUser && event.code !== 4001) this.scheduleReconnect()
    })
    this.socket.addEventListener('error', () => {
      if (connectionId === this.connectionId) this.handlers.onStatus?.('error')
    })
  }

  scheduleReconnect() {
    clearTimeout(this.retryTimer)
    const delay = Math.min(1000 * 2 ** this.retryCount++, 10000)
    this.retryTimer = setTimeout(() => this.connect(), delay)
  }

  sendMove(player) {
    if (this.socket?.readyState !== WebSocket.OPEN) return false
    this.socket.send(JSON.stringify({ type: 'move', ...player }))
    return true
  }

  close() {
    this.closedByUser = true
    this.connectionId++
    clearTimeout(this.retryTimer)
    const socket = this.socket
    this.socket = null
    socket?.close(1000, 'leave room')
  }
}