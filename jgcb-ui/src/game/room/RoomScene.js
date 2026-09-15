import { Application, Assets, Container, Graphics, Rectangle, Sprite } from 'pixi.js'
import Player from './Player'
import RoomObject from './RoomObject'
import { WORLD_WIDTH, WORLD_HEIGHT, SPAWN_POINT, OBJECT_ZONES, STATIC_COLLISIONS, pointInPolygon, pointInRect } from './roomConfig'

export default class RoomScene {
  constructor(host, options = {}) {
    this.host = host
    this.options = options
    this.players = new Map()
    this.objects = []
    this.currentUserId = Number(options.currentUserId)
    this.tick = ticker => this.update(ticker.deltaMS)
  }

  async init(room) {
    this.app = new Application()
    await this.app.init({
      resizeTo: this.host,
      background: '#2f312f',
      antialias: true,
      autoDensity: true,
      resolution: Math.min(window.devicePixelRatio || 1, 2),
      powerPreference: 'high-performance'
    })
    this.app.canvas.className = 'room-canvas'
    this.app.canvas.setAttribute('aria-label', '2.5D多人小屋，点击地面移动角色，点击家具互动')
    this.host.appendChild(this.app.canvas)

    this.world = new Container()
    this.world.sortableChildren = true
    this.mapLayer = new Container()
    this.mapLayer.zIndex = 0
    this.objectLayer = new Container()
    this.objectLayer.zIndex = 10
    this.playerLayer = new Container()
    this.playerLayer.zIndex = 20
    this.playerLayer.sortableChildren = true
    this.world.addChild(this.mapLayer, this.objectLayer, this.playerLayer)
    this.app.stage.addChild(this.world)

    const texture = await Assets.load(room.mapImage || '/room-assets/room-map.jpg')
    this.mapSprite = new Sprite(texture)
    this.mapSprite.width = WORLD_WIDTH
    this.mapSprite.height = WORLD_HEIGHT
    this.mapSprite.eventMode = 'static'
    this.mapSprite.cursor = 'crosshair'
    this.mapSprite.hitArea = new Rectangle(0, 0, WORLD_WIDTH, WORLD_HEIGHT)
    this.mapSprite.on('pointertap', event => this.onGroundTap(event))
    this.mapLayer.addChild(this.mapSprite)

    this.marker = new Graphics()
    this.marker.visible = false
    this.marker.zIndex = 50
    this.world.addChild(this.marker)

    this.fitWorld()
    this.resizeObserver = new ResizeObserver(() => this.fitWorld())
    this.resizeObserver.observe(this.host)
    this.app.ticker.add(this.tick)
  }

  fitWorld() {
    if (!this.app || !this.world) return
    const scale = Math.min(this.app.screen.width / WORLD_WIDTH, this.app.screen.height / WORLD_HEIGHT)
    this.world.scale.set(scale)
    this.world.x = (this.app.screen.width - WORLD_WIDTH * scale) / 2
    this.world.y = (this.app.screen.height - WORLD_HEIGHT * scale) / 2
  }

  onGroundTap(event) {
    const point = this.world.toLocal(event.global)
    this.requestMove(point.x, point.y)
  }

  isWalkable(x, y) {
    const point = { x, y }
    if (!pointInPolygon(point)) return false
    if (STATIC_COLLISIONS.some(zone => pointInRect(point, zone))) return false
    return !this.objects.some(object => pointInRect(point, object.zone))
  }

  requestMove(x, y, action = 'walk') {
    const target = { x: Math.round(x), y: Math.round(y) }
    if (!this.isWalkable(target.x, target.y)) {
      this.showMarker(target.x, target.y, false)
      this.options.onBlocked?.()
      return false
    }
    const player = this.players.get(this.currentUserId)
    if (!player) return false
    const direction = target.x < player.x ? 'left' : target.x > player.x ? 'right' : player.direction
    player.setTarget(target.x, target.y, direction, action)
    this.showMarker(target.x, target.y, true)
    this.options.onMove?.({ userId: this.currentUserId, x: target.x, y: target.y, direction, action })
    return true
  }

  moveToObject(type) {
    const zone = OBJECT_ZONES[type]
    if (!zone?.approach) return false
    return this.requestMove(zone.approach.x, zone.approach.y, `interact:${type}`)
  }

  showMarker(x, y, accepted) {
    this.marker.clear()
      .circle(0, 0, accepted ? 12 : 14)
      .stroke({ color: accepted ? 0x6ee7b7 : 0xfb7185, width: 4, alpha: 0.95 })
    this.marker.x = x
    this.marker.y = y
    this.marker.visible = true
    clearTimeout(this.markerTimer)
    this.markerTimer = setTimeout(() => { if (this.marker) this.marker.visible = false }, 650)
  }

  setObjects(items = []) {
    this.objects.forEach(object => object.destroy())
    this.objects = items.map(data => {
      const object = new RoomObject(data, (item, zone) => this.options.onObjectClick?.(item, zone))
      this.objectLayer.addChild(object.container)
      return object
    })
  }

  setPlayers(items = []) {
    const activeIds = new Set(items.map(item => Number(item.userId)))
    for (const [id] of this.players) {
      if (!activeIds.has(id) && id !== this.currentUserId) this.removePlayer(id)
    }
    items.forEach(item => this.upsertPlayer(item, true))
  }

  upsertPlayer(data, snap = false) {
    const id = Number(data.userId)
    if (!Number.isFinite(id)) return
    let player = this.players.get(id)
    if (!player) {
      player = new Player({ ...SPAWN_POINT, ...data }, id === this.currentUserId)
      this.players.set(id, player)
      this.playerLayer.addChild(player.container)
    } else {
      player.sync(data, snap)
    }
    this.options.onPlayerCount?.(this.players.size)
  }

  removePlayer(userId) {
    const id = Number(userId)
    const player = this.players.get(id)
    if (!player) return
    this.playerLayer.removeChild(player.container)
    player.destroy()
    this.players.delete(id)
    this.options.onPlayerCount?.(this.players.size)
  }

  update(deltaMs) {
    this.players.forEach(player => player.update(deltaMs))
  }

  resetCurrentPlayer() {
    this.requestMove(SPAWN_POINT.x, SPAWN_POINT.y)
  }

  destroy() {
    clearTimeout(this.markerTimer)
    this.resizeObserver?.disconnect()
    if (this.app) {
      this.app.ticker.remove(this.tick)
      this.players.forEach(player => { player.destroyed = true })
      this.app.destroy({ removeView: true }, { children: true })
    }
    this.players.clear()
    this.objects = []
    this.app = null
  }
}