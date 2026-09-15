import { ref } from 'vue'

const toasts = ref([])
let uid = 0

export function useToast() {
  function toast(message, type = 'info', duration = 3000) {
    const id = ++uid
    toasts.value.push({ id, message, type })
    if (duration > 0) {
      setTimeout(() => remove(id), duration)
    }
  }

  function remove(id) {
    toasts.value = toasts.value.filter(t => t.id !== id)
  }

  return { toasts, toast, remove }
}
