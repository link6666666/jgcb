<template>
  <div class="checkin-page">
    <div class="checkin-hero">
      <div class="checkin-btn-wrapper">
        <button
          class="checkin-btn"
          :class="{ checked: checkedInToday }"
          :disabled="checkedInToday"
          @click="handleCheckIn"
        >
          <span v-if="checkedInToday" class="checkin-btn-icon">✅</span>
          <span v-else class="checkin-btn-icon">📅</span>
          <span class="checkin-btn-text">{{ checkedInToday ? '今日已签到' : '今日签到' }}</span>
        </button>
      </div>

      <div class="streak-card" v-if="streak > 0">
        <span class="streak-icon">🔥</span>
        <div class="streak-info">
          <span class="streak-number">{{ streak }}</span>
          <span class="streak-label">连续签到天数</span>
        </div>
      </div>
      <div class="streak-card empty" v-else>
        <span class="streak-icon">💤</span>
        <div class="streak-info">
          <span class="streak-label">还没有签到记录</span>
          <span class="streak-hint">开始你的第一次签到吧</span>
        </div>
      </div>
    </div>

    <div class="calendar-card">
      <div class="calendar-header">
        <button class="calendar-nav" @click="prevMonth">&lt;</button>
        <span class="calendar-title">{{ currentYear }}年 {{ currentMonth }}月</span>
        <button class="calendar-nav" @click="nextMonth">&gt;</button>
      </div>

      <div class="calendar-weekdays">
        <span v-for="wd in weekDays" :key="wd" class="weekday">{{ wd }}</span>
      </div>

      <div class="calendar-grid">
        <div
          v-for="(cell, idx) in calendarCells"
          :key="idx"
          class="calendar-cell"
          :class="{
            'calendar-cell--checked': cell.checked,
            'calendar-cell--today': cell.isToday,
            'calendar-cell--empty': cell.day === null
          }"
        >
          <span v-if="cell.day !== null" class="calendar-day">{{ cell.day }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { doCheckIn, getCheckInStatus, getCheckInCalendar } from '../api'
import { useToast } from '../composables/useToast'

const { toast } = useToast()

const checkedInToday = ref(false)
const streak = ref(0)
const checkedDates = ref([])
const currentYear = ref(new Date().getFullYear())
const currentMonth = ref(new Date().getMonth() + 1)
const weekDays = ['日', '一', '二', '三', '四', '五', '六']

const calendarCells = computed(() => {
  const year = currentYear.value
  const month = currentMonth.value
  const firstDay = new Date(year, month - 1, 1).getDay()
  const daysInMonth = new Date(year, month, 0).getDate()
  const today = new Date()
  const todayStr = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`

  const cells = []
  for (let i = 0; i < firstDay; i++) {
    cells.push({ day: null, checked: false, isToday: false })
  }

  for (let d = 1; d <= daysInMonth; d++) {
    const dateStr = `${year}-${String(month).padStart(2, '0')}-${String(d).padStart(2, '0')}`
    cells.push({
      day: d,
      checked: checkedDates.value.includes(dateStr),
      isToday: dateStr === todayStr
    })
  }

  return cells
})

async function handleCheckIn() {
  try {
    await doCheckIn()
    checkedInToday.value = true
    streak.value++
    await loadCalendar()
    toast.success('签到成功')
  } catch (e) {
    const msg = e?.response?.data?.message || '签到失败'
    toast.error(msg)
  }
}

async function loadStatus() {
  try {
    const res = await getCheckInStatus()
    checkedInToday.value = res.data.checkedInToday
    streak.value = res.data.streak
  } catch (e) { /* ignore */ }
}

async function loadCalendar() {
  try {
    const res = await getCheckInCalendar(currentYear.value, currentMonth.value)
    checkedDates.value = (res.data || []).map(d => {
      const dt = new Date(d)
      const y = dt.getFullYear()
      const m = String(dt.getMonth() + 1).padStart(2, '0')
      const day = String(dt.getDate()).padStart(2, '0')
      return `${y}-${m}-${day}`
    })
  } catch (e) { /* ignore */ }
}

function prevMonth() {
  if (currentMonth.value === 1) {
    currentMonth.value = 12
    currentYear.value--
  } else {
    currentMonth.value--
  }
  loadCalendar()
}

function nextMonth() {
  if (currentMonth.value === 12) {
    currentMonth.value = 1
    currentYear.value++
  } else {
    currentMonth.value++
  }
  loadCalendar()
}

onMounted(() => {
  loadStatus()
  loadCalendar()
})
</script>

<style scoped>
.checkin-page {
  max-width: 480px;
  margin: 0 auto;
  padding: 20px 16px;
}

.checkin-hero {
  text-align: center;
  margin-bottom: 24px;
}

.checkin-btn-wrapper {
  margin-bottom: 20px;
}

.checkin-btn {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 16px 48px;
  font-size: 20px;
  font-weight: 600;
  color: #fff;
  background: linear-gradient(135deg, var(--primary), #8b5cf6);
  border: none;
  border-radius: 16px;
  cursor: pointer;
  transition: all .3s ease;
  box-shadow: 0 4px 20px rgba(99, 102, 241, .35);
}

.checkin-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 28px rgba(99, 102, 241, .5);
}

.checkin-btn:active:not(:disabled) {
  transform: translateY(0);
}

.checkin-btn.checked {
  background: linear-gradient(135deg, #10b981, #34d399);
  box-shadow: 0 4px 20px rgba(16, 185, 129, .35);
  cursor: default;
}

.checkin-btn-icon {
  font-size: 24px;
}

.streak-card {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  padding: 14px 28px;
  background: var(--bg-card);
  border-radius: 12px;
  box-shadow: var(--shadow);
}

.streak-card.empty {
  opacity: .7;
}

.streak-icon {
  font-size: 28px;
}

.streak-info {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.streak-number {
  font-size: 28px;
  font-weight: 700;
  color: var(--primary);
  line-height: 1.1;
}

.streak-label {
  font-size: 13px;
  color: var(--text-secondary, #666);
}

.streak-hint {
  font-size: 12px;
  color: var(--text-secondary, #999);
}

.calendar-card {
  background: var(--bg-card);
  border-radius: 12px;
  padding: 16px;
  box-shadow: var(--shadow);
}

.calendar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}

.calendar-title {
  font-size: 16px;
  font-weight: 600;
}

.calendar-nav {
  width: 32px;
  height: 32px;
  border: 1px solid var(--border);
  border-radius: 8px;
  background: var(--bg-card);
  cursor: pointer;
  font-size: 14px;
  color: var(--text-primary, #333);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all .2s;
}

.calendar-nav:hover {
  background: var(--primary);
  color: #fff;
  border-color: var(--primary);
}

.calendar-weekdays {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  text-align: center;
  margin-bottom: 8px;
}

.weekday {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-secondary, #999);
  padding: 8px 0;
}

.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 4px;
}

.calendar-cell {
  aspect-ratio: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  font-size: 14px;
  transition: all .2s;
}

.calendar-cell--checked {
  background: rgba(99, 102, 241, .15);
  color: var(--primary);
  font-weight: 600;
}

.calendar-cell--today {
  border: 2px solid var(--primary);
}

.calendar-cell--today.calendar-cell--checked {
  background: var(--primary);
  color: #fff;
}
</style>
