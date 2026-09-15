export const WORLD_WIDTH = 1200
export const WORLD_HEIGHT = 800
export const SPAWN_POINT = { x: 240, y: 650 }

export const FLOOR_POLYGON = [
  { x: 30, y: 285 },
  { x: 420, y: 70 },
  { x: 1170, y: 180 },
  { x: 1170, y: 650 },
  { x: 870, y: 760 },
  { x: 380, y: 760 },
  { x: 35, y: 630 }
]

export const OBJECT_ZONES = {
  computer: { width: 220, height: 170, approach: { x: 455, y: 285 }, label: '电脑' },
  sofa: { width: 330, height: 220, approach: { x: 865, y: 390 }, label: '沙发' },
  mahjong_table: { width: 320, height: 280, approach: { x: 615, y: 650 }, label: '麻将机' },
  plant: { width: 180, height: 230, approach: { x: 955, y: 590 }, label: '绿植' }
}

export const STATIC_COLLISIONS = [
  { x: 40, y: 275, width: 190, height: 160 }
]

export function pointInPolygon(point, polygon = FLOOR_POLYGON) {
  let inside = false
  for (let i = 0, j = polygon.length - 1; i < polygon.length; j = i++) {
    const a = polygon[i]
    const b = polygon[j]
    const crosses = ((a.y > point.y) !== (b.y > point.y)) &&
      point.x < ((b.x - a.x) * (point.y - a.y)) / (b.y - a.y) + a.x
    if (crosses) inside = !inside
  }
  return inside
}

export function pointInRect(point, rect, padding = 12) {
  return point.x >= rect.x - rect.width / 2 - padding &&
    point.x <= rect.x + rect.width / 2 + padding &&
    point.y >= rect.y - rect.height / 2 - padding &&
    point.y <= rect.y + rect.height / 2 + padding
}