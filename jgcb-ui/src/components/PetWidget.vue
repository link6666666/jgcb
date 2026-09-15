<template>
  <div
    v-if="visible"
    class="pet-widget"
    :style="{ transform: `translate3d(${pos.x}px, ${pos.y}px, 0)` }"
    @pointerdown="startDrag"
    @pointermove="onDrag" @pointerup="endDrag" @pointercancel="cancelDrag" @lostpointercapture="cancelDrag"
  >
    <div class="pet-widget-bubble" v-if="showBubble" @pointerdown.stop>
      <span class="pet-widget-bubble-text">{{ bubbleText }}</span>
      <router-link to="/pet" class="pet-widget-bubble-link" @click="showBubble = false">装扮糍粑 →</router-link>
    </div>

    <div class="pet-widget-body" role="button" tabindex="0" aria-label="和糍粑互动" :aria-expanded="showBubble" @keydown.enter.prevent="onTap" @keydown.space.prevent="onTap" @click="onTap">
      <PetFigure
        :avatar="avatar"
        :nickname="nickname"
        :outfit="outfit"
        :decorations="decorations"
      />
      <span class="pet-widget-level" v-if="level > 0">Lv.{{ level }}</span>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { getPetStatus } from '../api'
import PetFigure from './PetFigure.vue'

const avatar = ref('')
const nickname = ref('')
const outfit = ref({})
const decorations = ref([])
const level = ref(0)
const visible = ref(false)
const showBubble = ref(false)
const pos = ref({ x: Math.max(0, window.innerWidth - 110), y: Math.max(0, window.innerHeight - 200) })
const bubbleText = ref('')
const BUBBLES = ['今天也要加油哦~', '你好呀！', '戳我看看装扮~', '记得签到哦', '一起玩吧！']
let disposed = false
let revision = 0
let bubbleTimer
let frame = 0
let pendingPosition = null
let pointer = null
let moved = false
let moveTimer

function applyStatus(d) {
  level.value = d.level || 1
  outfit.value = { head: d.head || '', neck: d.neck || '', body: d.body || '', accessory: d.accessory || '' }
  decorations.value = d.decorations || []
  visible.value = true
}
function bubble() {
  clearTimeout(bubbleTimer)
  bubbleText.value = BUBBLES[Math.floor(Math.random() * BUBBLES.length)]
  showBubble.value = true
  bubbleTimer = setTimeout(() => { showBubble.value = false }, 4000)
}
async function loadStatus() {
  const current = ++revision
  try {
    const res = await getPetStatus()
    if (disposed || current !== revision || res.code !== 200 || !res.data) return
    applyStatus(res.data)
    bubble()
  } catch { /* The page remains usable if the companion cannot load. */ }
}
function onOutfitUpdated(event) {
  if (event.detail) { ++revision; applyStatus(event.detail); bubble() }
  else loadStatus()
}
function onTap() {
  if (moved) return
  if (showBubble.value) { clearTimeout(bubbleTimer); showBubble.value = false }
  else bubble()
}
function clampPosition(x, y) {
  return { x: Math.max(16, Math.min(x, window.innerWidth - 110)), y: Math.max(16, Math.min(y, window.innerHeight - 200)) }
}
function resize() { pos.value = clampPosition(pos.value.x, pos.value.y) }
function startDrag(event) {
  if (!event.isPrimary || (event.pointerType === 'mouse' && event.button !== 0)) return
  clearTimeout(moveTimer)
  moved = false
  pointer = { id: event.pointerId, x: event.clientX, y: event.clientY, startX: pos.value.x, startY: pos.value.y }
  // Capture on the tapped child so a tap still reaches its click handler.
  event.target.setPointerCapture(event.pointerId)
}
function onDrag(event) {
  if (!pointer || event.pointerId !== pointer.id) return
  const dx = event.clientX - pointer.x
  const dy = event.clientY - pointer.y
  if (!moved && Math.hypot(dx, dy) < 5) return
  moved = true
  pendingPosition = clampPosition(pointer.startX + dx, pointer.startY + dy)
  if (!frame) frame = requestAnimationFrame(() => { pos.value = pendingPosition; frame = 0 })
}
function endDrag() {
  if (!pointer) return
  pointer = null
  cancelAnimationFrame(frame)
  frame = 0
  if (pendingPosition) pos.value = pendingPosition
  pendingPosition = null
  moveTimer = setTimeout(() => { moved = false }, 0)
}
function cancelDrag() { endDrag() }
onMounted(() => {
  try {
    const u = JSON.parse(localStorage.getItem('user') || '{}') || {}
    avatar.value = u.avatar || ''
    nickname.value = u.nickname || u.username || ''
  } catch { /* Use the default avatar when local profile data is unavailable. */ }
  window.addEventListener('pet-outfit-updated', onOutfitUpdated)
  window.addEventListener('resize', resize)
  resize()
  loadStatus()
})
onBeforeUnmount(() => {
  disposed = true
  clearTimeout(bubbleTimer)
  clearTimeout(moveTimer)
  cancelAnimationFrame(frame)
  window.removeEventListener('pet-outfit-updated', onOutfitUpdated)
  window.removeEventListener('resize', resize)
})
</script>

<style scoped>
.pet-widget {
  left: 0;
  top: 0;
  touch-action: none;
  position: fixed;
  z-index: 500;
  cursor: grab;
  user-select: none;
  -webkit-user-select: none;
}

.pet-widget:active {
  cursor: grabbing;
}

.pet-widget-body {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.pet-widget-level {
  margin-top: -6px;
  padding: 1px 8px;
  background: var(--primary);
  color: #fff;
  border-radius: 10px;
  font-size: 10px;
  font-weight: 700;
  z-index: 4;
}

.pet-widget-bubble {
  position: absolute;
  bottom: 100%;
  left: 50%;
  transform: translateX(-50%);
  margin-bottom: 8px;
  background: #fff;
  border-radius: 14px;
  padding: 10px 14px;
  box-shadow: var(--shadow-lg);
  white-space: nowrap;
  z-index: 10;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.pet-widget-bubble::after {
  content: '';
  position: absolute;
  top: 100%;
  left: 50%;
  transform: translateX(-50%);
  border: 6px solid transparent;
  border-top-color: #fff;
}

.pet-widget-bubble-text {
  font-size: 13px;
  color: var(--text-primary);
  font-weight: 500;
}

.pet-widget-bubble-link {
  font-size: 12px;
  color: var(--primary);
  text-decoration: none;
  font-weight: 600;
}

.pet-widget-bubble-link:hover {
  color: var(--primary-dark);
}
</style>
