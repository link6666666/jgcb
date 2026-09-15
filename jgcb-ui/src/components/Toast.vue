<template>
  <div class="toast-container">
    <div
      v-for="t in toasts"
      :key="t.id"
      class="toast-item"
      :class="'toast-' + t.type"
      @click="remove(t.id)"
    >
      <span class="toast-icon">{{ icons[t.type] }}</span>
      <span>{{ t.message }}</span>
    </div>
  </div>
</template>

<script setup>
import { useToast } from '../composables/useToast'

const { toasts, remove } = useToast()

const icons = {
  success: '✅',
  error: '❌',
  info: 'ℹ️'
}
</script>

<style scoped>
.toast-container {
  position: fixed;
  top: 24px;
  right: 24px;
  z-index: 9999;
  display: flex;
  flex-direction: column;
  gap: 10px;
  pointer-events: none;
}

.toast-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 20px;
  border-radius: 10px;
  font-size: 14px;
  color: #fff;
  cursor: pointer;
  pointer-events: auto;
  animation: toast-in 0.35s cubic-bezier(0.21, 1.02, 0.73, 1);
  box-shadow: 0 8px 24px rgba(0,0,0,0.15);
  max-width: 380px;
  word-break: break-all;
  transition: transform 0.2s;
}

.toast-item:hover {
  transform: scale(1.02);
}

.toast-success { background: linear-gradient(135deg, #10b981, #34d399); }
.toast-error { background: linear-gradient(135deg, #ef4444, #f87171); }
.toast-info { background: linear-gradient(135deg, #6366f1, #818cf8); }

@keyframes toast-in {
  from { opacity: 0; transform: translateX(60px); }
  to { opacity: 1; transform: translateX(0); }
}
</style>
