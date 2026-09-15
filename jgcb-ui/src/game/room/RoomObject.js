import { Container, Graphics, Text } from 'pixi.js'
import { OBJECT_ZONES } from './roomConfig'

export default class RoomObject {
  constructor(data, onInteract) {
    this.data = data
    const zone = OBJECT_ZONES[data.type] || { width: 100, height: 100, label: data.type }
    this.zone = { x: Number(data.x), y: Number(data.y), ...zone }
    this.container = new Container()
    this.container.x = this.zone.x
    this.container.y = this.zone.y
    this.container.eventMode = 'static'
    this.container.cursor = 'pointer'
    this.container.hitArea = { contains: (x, y) => Math.abs(x) <= zone.width / 2 && Math.abs(y) <= zone.height / 2 }

    this.highlight = new Graphics()
      .roundRect(-zone.width / 2, -zone.height / 2, zone.width, zone.height, 20)
      .fill({ color: 0xffffff, alpha: 0.001 })
      .stroke({ color: 0xffd166, width: 4, alpha: 0 })
    this.label = new Text({
      text: `点击互动 · ${zone.label}`,
      style: { fill: 0xffffff, fontSize: 14, fontWeight: '700', stroke: { color: 0x5b3b22, width: 5 } }
    })
    this.label.anchor.set(0.5)
    this.label.y = -zone.height / 2 - 14
    this.label.visible = false
    this.container.addChild(this.highlight, this.label)
    this.container.on('pointerover', () => this.setHover(true))
    this.container.on('pointerout', () => this.setHover(false))
    this.container.on('pointertap', event => {
      event.stopPropagation()
      onInteract(data, this.zone)
    })
  }

  setHover(active) {
    this.label.visible = active
    this.highlight.clear()
      .roundRect(-this.zone.width / 2, -this.zone.height / 2, this.zone.width, this.zone.height, 20)
      .fill({ color: 0xffffff, alpha: active ? 0.06 : 0.001 })
      .stroke({ color: 0xffd166, width: 4, alpha: active ? 0.9 : 0 })
  }

  destroy() {
    this.container.destroy({ children: true })
  }
}