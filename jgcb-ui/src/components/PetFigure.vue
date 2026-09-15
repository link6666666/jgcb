<template>
  <div class="pet-figure" :class="{ 'pet-figure--large': large }">
    <div class="pet-head-wrap">
      <span v-if="headEmoji" class="pet-deco pet-deco--head">{{ headEmoji }}</span>
      <img
        v-if="avatar && !imageFailed"
        :src="imageSource" decoding="async" :alt="nickname || '糍粑头像'"
        class="pet-head"
        @error="onImageError"
      />
      <span v-else class="pet-head pet-head--placeholder">{{ initial }}</span>
    </div>

    <span v-if="neckEmoji" class="pet-deco pet-deco--neck">{{ neckEmoji }}</span>

    <div class="pet-stick">
      <span class="pet-line pet-arm--left"></span>
      <span class="pet-line pet-arm--right"></span>
      <div class="pet-torso">
        <span v-if="bodyEmoji" class="pet-deco pet-deco--body">{{ bodyEmoji }}</span>
      </div>
      <span class="pet-line pet-leg--left"></span>
      <span class="pet-line pet-leg--right"></span>
    </div>

    <span v-if="accessoryEmoji" class="pet-deco pet-deco--accessory">{{ accessoryEmoji }}</span>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { thumbUrl } from '../utils/image'

const props = defineProps({
  avatar: { type: String, default: '' },
  nickname: { type: String, default: '' },
  outfit: { type: Object, default: () => ({}) },
  decorations: { type: Array, default: () => [] },
  large: { type: Boolean, default: false }
})

const imageFailed = ref(false)
const useOriginal = ref(false)
const imageSource = computed(() => useOriginal.value ? props.avatar : thumbUrl(props.avatar, props.large ? 256 : 128))
watch(() => props.avatar, () => { imageFailed.value = false; useOriginal.value = false })
function onImageError() {
  if (!useOriginal.value) useOriginal.value = true
  else imageFailed.value = true
}
const initial = computed(() => (props.nickname || '?')[0])

const emojiMap = computed(() => {
  const m = {}
  props.decorations.forEach(d => { m[d.code] = d.emoji })
  return m
})

const headEmoji = computed(() => props.outfit.head ? emojiMap.value[props.outfit.head] : '')
const neckEmoji = computed(() => props.outfit.neck ? emojiMap.value[props.outfit.neck] : '')
const bodyEmoji = computed(() => props.outfit.body ? emojiMap.value[props.outfit.body] : '')
const accessoryEmoji = computed(() => props.outfit.accessory ? emojiMap.value[props.outfit.accessory] : '')
</script>

<style scoped>
.pet-figure {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 84px;
}

.pet-head-wrap {
  position: relative;
  width: 64px;
  height: 64px;
  z-index: 3;
}

.pet-head {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  object-fit: cover;
  background: var(--border);
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.12);
  display: block;
}

.pet-head--placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--primary), var(--primary-light));
  color: #fff;
  font-size: 28px;
  font-weight: 700;
}

.pet-deco {
  position: absolute;
  line-height: 1;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.18));
  pointer-events: none;
  z-index: 5;
}

.pet-deco--head {
  top: -22px;
  left: 50%;
  transform: translateX(-50%);
  font-size: 26px;
}

.pet-deco--neck {
  position: static;
  margin-top: -4px;
  font-size: 24px;
  z-index: 2;
}

/* 火柴人身体 */
.pet-stick {
  position: relative;
  width: 70px;
  height: 58px;
  margin-top: -2px;
  z-index: 2;
}

.pet-torso {
  position: absolute;
  left: 50%;
  top: 0;
  transform: translateX(-50%);
  width: 6px;
  height: 28px;
  background: #4b5563;
  border-radius: 3px;
}

.pet-line {
  position: absolute;
  width: 6px;
  background: #4b5563;
  border-radius: 3px;
  transform-origin: top center;
}

.pet-arm--left {
  height: 24px;
  left: 50%;
  top: 2px;
  transform: translateX(-50%) rotate(30deg);
}

.pet-arm--right {
  height: 24px;
  left: 50%;
  top: 2px;
  transform: translateX(-50%) rotate(-30deg);
}

.pet-leg--left {
  height: 26px;
  left: 50%;
  top: 28px;
  transform: translateX(-50%) rotate(26deg);
}

.pet-leg--right {
  height: 26px;
  left: 50%;
  top: 28px;
  transform: translateX(-50%) rotate(-26deg);
}

.pet-deco--body {
  position: absolute;
  top: 14px;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 28px;
  z-index: 6;
}

.pet-deco--accessory {
  position: absolute;
  font-size: 26px;
  z-index: 4;
  right: -18px;
  bottom: 4px;
}

/* ---------- 大尺寸（独立页面） ---------- */
.pet-figure--large {
  width: 150px;
}

.pet-figure--large .pet-head-wrap,
.pet-figure--large .pet-head {
  width: 120px;
  height: 120px;
}

.pet-figure--large .pet-head--placeholder {
  font-size: 52px;
}

.pet-figure--large .pet-deco {
  filter: drop-shadow(0 3px 6px rgba(0, 0, 0, 0.2));
}

.pet-figure--large .pet-deco--head { top: -38px; font-size: 46px; }
.pet-figure--large .pet-deco--neck { font-size: 42px; margin-top: -8px; }
.pet-figure--large .pet-deco--body { font-size: 54px; }
.pet-figure--large .pet-deco--accessory { font-size: 46px; right: -34px; bottom: 8px; }

.pet-figure--large .pet-stick {
  width: 126px;
  height: 104px;
  margin-top: -4px;
}

.pet-figure--large .pet-torso {
  width: 10px;
  height: 48px;
  border-radius: 5px;
}

.pet-figure--large .pet-line {
  width: 10px;
  border-radius: 5px;
}

.pet-figure--large .pet-arm--left,
.pet-figure--large .pet-arm--right {
  height: 44px;
  top: 4px;
}

.pet-figure--large .pet-leg--left,
.pet-figure--large .pet-leg--right {
  height: 46px;
  top: 48px;
}

.pet-figure--large .pet-deco--body {
  top: 24px;
  font-size: 50px;
}
</style>
