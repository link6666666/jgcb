import { Assets, Container, Graphics, Sprite, Text } from 'pixi.js'
import { thumbUrl } from '../../utils/image'

const OUTFIT_EMOJI = {
  top_hat: '🎩', cap: '🧢', graduate: '🎓', crown: '👑',
  scarf: '🧣', bow: '🎀', shirt: '👕', vest: '🦺', coat: '🧥',
  wand: '🪄', rifle: '🔫'
}

function colorForUser(userId) {
  const colors = [0x5b8def, 0x8b78e6, 0x42a982, 0xe27d60, 0xd69e2e, 0x4c9fbf]
  return colors[Math.abs(Number(userId) || 0) % colors.length]
}

export default class Player {
  constructor(data, current = false) {
    this.userId = Number(data.userId)
    this.current = current
    this.x = Number(data.x) || 240
    this.y = Number(data.y) || 650
    this.targetX = this.x
    this.targetY = this.y
    this.direction = data.direction || 'down'
    this.action = data.action || 'idle'
    this.speed = 190
    this.destroyed = false
    this.avatarVersion = 0

    this.container = new Container()
    this.container.x = this.x
    this.container.y = this.y
    this.container.zIndex = this.y

    this.shadow = new Graphics().ellipse(0, 4, 24, 8).fill({ color: 0x2b1b10, alpha: 0.18 })
    this.figure = new Container()
    this.figure.y = -5
    this.body = new Graphics()
      .roundRect(-14, -38, 28, 34, 10)
      .fill({ color: colorForUser(this.userId) })
      .roundRect(-12, -11, 8, 18, 4)
      .roundRect(4, -11, 8, 18, 4)
      .fill({ color: 0x4b5563 })
    this.head = new Container()
    this.head.y = -55
    this.headBase = new Graphics().circle(0, 0, 20).fill({ color: 0xffead8 }).stroke({ color: current ? 0xffffff : 0xd7c2ae, width: current ? 3 : 2 })
    this.initial = new Text({ text: (data.nickname || '?')[0], style: { fill: 0x5b4635, fontSize: 16, fontWeight: '700' } })
    this.initial.anchor.set(0.5)
    this.head.addChild(this.headBase, this.initial)
    this.outfitLayer = new Container()
    this.figure.addChild(this.body, this.head, this.outfitLayer)

    this.nameText = new Text({
      text: data.nickname || '成员',
      style: { fill: 0x3f2d20, fontSize: 13, fontWeight: '600', stroke: { color: 0xffffff, width: 4 } }
    })
    this.nameText.anchor.set(0.5)
    this.nameText.y = 18

    this.currentRing = new Graphics().ellipse(0, 4, 29, 12).stroke({ color: 0x6366f1, width: 3, alpha: current ? 0.85 : 0 })
    this.container.addChild(this.shadow, this.currentRing, this.figure, this.nameText)
    this.syncAppearance(data)
  }

  sync(data, snap = false) {
    this.nameText.text = data.nickname || this.nameText.text
    this.direction = data.direction || this.direction
    this.action = data.action || this.action
    this.syncAppearance(data)
    const x = Number(data.x)
    const y = Number(data.y)
    if (Number.isFinite(x) && Number.isFinite(y)) {
      if (snap) {
        this.x = x
        this.y = y
        this.container.x = x
        this.container.y = y
      }
      this.targetX = x
      this.targetY = y
    }
  }

  syncAppearance(data) {
    this.drawOutfit(data)
    if (data.avatar && data.avatar !== this.avatar) {
      this.avatar = data.avatar
      this.loadAvatar(data.avatar)
    }
  }

  async loadAvatar(avatar) {
    const version = ++this.avatarVersion
    try {
      const texture = await Assets.load(thumbUrl(avatar, 128))
      if (this.destroyed || version !== this.avatarVersion) return
      if (this.avatarSprite) this.head.removeChild(this.avatarSprite)
      if (this.avatarMask) this.head.removeChild(this.avatarMask)
      const sprite = new Sprite(texture)
      sprite.anchor.set(0.5)
      sprite.width = 38
      sprite.height = 38
      const mask = new Graphics().circle(0, 0, 19).fill(0xffffff)
      sprite.mask = mask
      this.avatarSprite = sprite
      this.avatarMask = mask
      this.initial.visible = false
      this.head.addChildAt(sprite, 1)
      this.head.addChild(mask)
    } catch {
      if (!this.destroyed && version === this.avatarVersion) this.initial.visible = true
    }
  }

  drawOutfit(data) {
    this.outfitLayer.removeChildren().forEach(child => child.destroy())
    const items = [
      { code: data.head, x: 0, y: -82, size: 25 },
      { code: data.neck, x: 0, y: -34, size: 20 },
      { code: data.body, x: 0, y: -25, size: 24 },
      { code: data.accessory, x: 27, y: -20, size: 22 }
    ]
    for (const item of items) {
      const emoji = OUTFIT_EMOJI[item.code]
      if (!emoji) continue
      const text = new Text({ text: emoji, style: { fontSize: item.size } })
      text.anchor.set(0.5)
      text.x = item.x
      text.y = item.y
      this.outfitLayer.addChild(text)
    }
  }

  setTarget(x, y, direction, action = 'walk') {
    this.targetX = x
    this.targetY = y
    this.direction = direction || (x < this.x ? 'left' : x > this.x ? 'right' : this.direction)
    this.action = action
  }

  update(deltaMs) {
    const dx = this.targetX - this.x
    const dy = this.targetY - this.y
    const distance = Math.hypot(dx, dy)
    if (distance > 1) {
      const step = Math.min(distance, this.speed * deltaMs / 1000)
      this.x += dx / distance * step
      this.y += dy / distance * step
      this.action = 'walk'
      this.figure.y = -5 + Math.sin(performance.now() / 85) * 2
      if (Math.abs(dx) > 1) this.figure.scale.x = dx < 0 ? -1 : 1
    } else {
      this.x = this.targetX
      this.y = this.targetY
      this.action = 'idle'
      this.figure.y += (-5 - this.figure.y) * 0.25
    }
    this.container.x = this.x
    this.container.y = this.y
    this.container.zIndex = this.y
  }

  destroy() {
    this.destroyed = true
    this.container.destroy({ children: true })
  }
}