<template>
  <div class="page-content">
    <header class="pet-heading"><div><h1>我的糍粑</h1><p>换个装扮，一起出发。</p></div><span class="pet-collection" v-if="!loading && !loadError">已解锁 {{ unlockedCount }} / {{ decorations.length }}</span></header>
    <div v-if="loading" class="pet-state" role="status">正在打开糍粑衣橱…</div>
    <div v-else-if="loadError" class="pet-state" role="alert"><p>{{ loadError }}</p><button type="button" @click="loadStatus">重新加载</button></div>
    <div v-else class="pet-page">
      <section class="pet-stage" aria-label="装扮预览">
        <span class="pet-preview-label">{{ nickname || '你的糍粑' }} · 装扮预览</span>
        <div class="pet-preview"><PetFigure :avatar="avatar" :nickname="nickname" :outfit="outfit" :decorations="decorations" large /></div>
        <div class="pet-level-badge">⭐ Lv.{{ level }} <span v-if="level >= 6" class="pet-level-max">已满级</span></div>
        <div class="pet-exp-bar" role="progressbar" aria-label="升级进度" :aria-valuenow="expPercent" aria-valuemin="0" aria-valuemax="100"><div class="pet-exp-fill" :style="{ width: expPercent + '%' }"></div></div>
        <span class="pet-exp-text">{{ level >= 6 ? '已达到最高等级' : `再获得 ${100 - expCurrent} EXP 升级` }}</span>
        <router-link v-if="level < 6" to="/checkin" class="pet-checkin">去签到，积累经验 →</router-link>
      </section>
      <section class="pet-panel" aria-label="糍粑衣橱" :aria-busy="saving">
        <div class="pet-wardrobe-header"><h2>糍粑衣橱</h2><button type="button" class="pet-secondary" @click="randomize" :disabled="saving">随机搭配</button></div>
        <div class="pet-tabs" aria-label="装饰分类">
          <button v-for="group in slotGroups" :key="group.slot" type="button" :aria-pressed="activeSlot === group.slot" :class="{ active: activeSlot === group.slot }" @click="activeSlot = group.slot">{{ group.label }}</button>
        </div>
        <p class="pet-hint">点击试穿，满意后保存装扮。</p>
        <div class="pet-deco-grid">
          <button type="button" v-for="d in activeItems" :key="d.code" class="pet-deco-item" :class="{ 'is-locked': d.unlockLevel > level, 'is-selected': outfit[activeSlot] === d.code }" :disabled="saving || d.unlockLevel > level" :aria-pressed="outfit[activeSlot] === d.code" :aria-label="`${d.name}${d.unlockLevel > level ? '，等级 ' + d.unlockLevel + ' 解锁' : ''}`" @click="select(activeSlot, d)">
            <span class="pet-deco-emoji" aria-hidden="true">{{ d.emoji }}</span><span class="pet-deco-name">{{ d.name }}</span><span v-if="d.unlockLevel > level" class="pet-deco-lock">🔒 Lv.{{ d.unlockLevel }}</span><span v-else-if="outfit[activeSlot] === d.code" class="pet-deco-check">✓</span>
          </button>
          <button type="button" class="pet-deco-item pet-deco-item--none" :class="{ 'is-selected': !outfit[activeSlot] }" :aria-pressed="!outfit[activeSlot]" :disabled="saving" @click="select(activeSlot, null)"><span class="pet-deco-emoji" aria-hidden="true">—</span><span class="pet-deco-name">不穿戴</span></button>
        </div>
        <div class="pet-save-bar"><p role="status" :class="{ 'pet-save-error': saveError }">{{ saveError || (saving ? '正在保存…' : dirty ? '有未保存的试穿装扮' : '装扮已同步') }}</p><div class="pet-actions"><button type="button" class="pet-secondary" :disabled="!dirty || saving" @click="restore">还原</button><button type="button" class="pet-save" :disabled="!dirty || saving" @click="save">{{ saving ? '保存中…' : '保存装扮' }}</button></div></div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount } from 'vue'
import { onBeforeRouteLeave } from 'vue-router'
import { getPetStatus, updatePetOutfit } from '../api'
import { useToast } from '../composables/useToast'
import PetFigure from '../components/PetFigure.vue'

const { toast } = useToast()
const avatar = ref('')
const nickname = ref('')
const level = ref(0)
const exp = ref(0)
const decorations = ref([])
const loading = ref(true)
const saving = ref(false)
const loadError = ref('')
const saveError = ref('')
const activeSlot = ref('head')
const outfit = reactive({ head: '', neck: '', body: '', accessory: '' })
const saved = ref({ ...outfit })
const SLOT_LABELS = { head: '头部', neck: '颈部', body: '身体', accessory: '手持' }
const slots = Object.keys(SLOT_LABELS)
const slotGroups = computed(() => slots.map(slot => ({ slot, label: SLOT_LABELS[slot] })))
const activeItems = computed(() => decorations.value.filter(d => d.slot === activeSlot.value))
const unlockedCount = computed(() => decorations.value.filter(d => d.unlockLevel <= level.value).length)
const dirty = computed(() => slots.some(slot => outfit[slot] !== saved.value[slot]))
const expCurrent = computed(() => Math.max(0, exp.value) % 100)
const expPercent = computed(() => level.value >= 6 ? 100 : expCurrent.value)

async function loadStatus() {
  loading.value = true
  loadError.value = ''
  try {
    const res = await getPetStatus()
    if (res.code !== 200 || !res.data) throw new Error(res.message || '加载失败')
    const d = res.data
    level.value = d.level
    exp.value = d.exp || 0
    decorations.value = d.decorations || []
    for (const slot of slots) outfit[slot] = d[slot] || ''
    saved.value = { ...outfit }
  } catch (e) {
    loadError.value = '糍粑暂时没有加载出来，请重试。'
  } finally { loading.value = false }
}
onMounted(() => {
  try {
    const u = JSON.parse(localStorage.getItem('user') || '{}') || {}
    avatar.value = u.avatar || ''
    nickname.value = u.nickname || u.username || ''
  } catch { /* The outfit can still load when local profile data is unavailable. */ }
  window.addEventListener('beforeunload', beforeUnload)
  loadStatus()
})
function select(slot, d) {
  if (saving.value || loading.value || (d && d.unlockLevel > level.value)) return
  outfit[slot] = d ? d.code : ''
  saveError.value = ''
}
function randomize() {
  if (saving.value) return
  for (const slot of slots) {
    const available = decorations.value.filter(d => d.slot === slot && d.unlockLevel <= level.value)
    outfit[slot] = available.length ? available[Math.floor(Math.random() * available.length)].code : ''
  }
  saveError.value = ''
}
function restore() { Object.assign(outfit, saved.value); saveError.value = '' }
async function save() {
  if (!dirty.value || saving.value) return
  saving.value = true
  saveError.value = ''
  const snapshot = { ...outfit }
  try {
    const res = await updatePetOutfit(snapshot)
    if (res.code !== 200) throw new Error(res.message || '保存失败')
    saved.value = snapshot
    window.dispatchEvent(new CustomEvent('pet-outfit-updated', { detail: { ...snapshot, level: level.value, decorations: decorations.value } }))
    toast('装扮已保存', 'success')
  } catch (e) {
    saveError.value = e?.response?.data?.message || e.message || '保存失败，请重试'
  } finally { saving.value = false }
}
function beforeUnload(event) {
  if (dirty.value || saving.value) { event.preventDefault(); event.returnValue = '' }
}
onBeforeRouteLeave(() => {
  if (saving.value) { toast('正在保存，请稍候', 'info'); return false }
  return !dirty.value || window.confirm('试穿装扮尚未保存，确定离开吗？')
})
onBeforeUnmount(() => window.removeEventListener('beforeunload', beforeUnload))
</script>
<style scoped>
.pet-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.pet-stage {
  background: var(--bg-card);
  border-radius: var(--radius);
  padding: 40px 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  border: 1px solid var(--border-light);
  box-shadow: var(--shadow-xs);
}

.pet-level-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 14px;
  background: linear-gradient(135deg, rgba(99, 102, 241, .15), rgba(139, 92, 246, .15));
  border-radius: 20px;
  font-size: 15px;
  font-weight: 700;
  color: var(--primary);
}

.pet-level-max {
  font-size: 11px;
  color: #f59e0b;
  font-weight: 600;
}

.pet-exp-bar {
  width: 220px;
  height: 8px;
  background: #e5e7eb;
  border-radius: 4px;
  overflow: hidden;
}

.pet-exp-fill {
  height: 100%;
  background: linear-gradient(90deg, var(--primary), #8b5cf6);
  border-radius: 4px;
  transition: width .5s ease;
}

.pet-exp-text {
  font-size: 12px;
  color: var(--text-muted);
}

.pet-panel {
  background: var(--bg-card);
  border-radius: var(--radius);
  padding: 20px;
  border: 1px solid var(--border-light);
  box-shadow: var(--shadow-xs);
}

.pet-panel-title {
  font-size: 15px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 14px;
}

.pet-deco-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(96px, 1fr));
  gap: 10px;
}

.pet-deco-item {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 14px 8px;
  border-radius: var(--radius-sm);
  border: 1.5px solid var(--border);
  background: var(--bg-page);
  cursor: pointer;
  transition: all var(--transition);
}

.pet-deco-item:hover {
  border-color: var(--primary-light);
  transform: translateY(-2px);
}

.pet-deco-item.is-selected {
  border-color: var(--primary);
  background: var(--primary-bg);
  box-shadow: 0 0 0 2px rgba(99, 102, 241, .15);
}

.pet-deco-item.is-locked {
  opacity: .5;
  cursor: not-allowed;
}

.pet-deco-item.is-locked:hover {
  transform: none;
  border-color: var(--border);
}

.pet-deco-emoji {
  font-size: 30px;
  line-height: 1;
}

.pet-deco-name {
  font-size: 12px;
  color: var(--text-secondary);
  font-weight: 500;
}

.pet-deco-lock {
  font-size: 11px;
  color: var(--text-muted);
}

.pet-deco-check {
  position: absolute;
  top: 6px;
  right: 8px;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: var(--primary);
  color: #fff;
  font-size: 11px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.pet-heading { display:flex; align-items:center; justify-content:space-between; gap:16px; margin-bottom:24px; }
.pet-heading h1 { font-size:26px; }
.pet-heading p, .pet-hint { color:var(--text-secondary); font-size:14px; }
.pet-collection { font-size:13px; padding:6px 12px; border-radius:20px; background:var(--primary-bg); color:var(--primary); white-space:nowrap; }
.pet-page { display:grid; grid-template-columns:minmax(240px, 310px) minmax(0, 1fr); align-items:start; }
.pet-stage { position:sticky; top:24px; background:radial-gradient(ellipse at top, #eef2ff, #fff 75%); padding:28px 20px; }
.pet-preview-label { color:var(--text-secondary); font-size:13px; }
.pet-preview { padding:46px 36px 24px; }
.pet-checkin { color:var(--primary); font-size:13px; text-decoration:none; }
.pet-panel { min-width:0; padding:24px; }
.pet-wardrobe-header { display:flex; align-items:center; justify-content:space-between; gap:12px; }
.pet-wardrobe-header h2 { font-size:18px; }
.pet-tabs { display:flex; gap:6px; padding:5px; background:var(--bg-page); border-radius:12px; margin:22px 0 12px; }
.pet-tabs button { flex:1; border:0; background:transparent; color:var(--text-secondary); border-radius:8px; min-height:42px; cursor:pointer; }
.pet-tabs button.active { background:var(--bg-card); color:var(--primary); box-shadow:var(--shadow-sm); font-weight:600; }
.pet-hint { margin-bottom:18px; }
.pet-deco-item { min-height:104px; }
.pet-save-bar { border-top:1px solid var(--border); margin-top:24px; padding-top:18px; display:flex; flex-wrap:wrap; align-items:center; justify-content:space-between; gap:12px; }
.pet-save-bar p { color:var(--text-secondary); font-size:13px; }
.pet-save-bar .pet-save-error { color:var(--danger); }
.pet-actions { display:flex; gap:8px; }
.pet-save, .pet-secondary, .pet-state button { border:1px solid var(--border); border-radius:10px; padding:10px 16px; cursor:pointer; background:var(--bg-card); color:var(--text-primary); min-height:44px; }
.pet-save { background:var(--primary); color:#fff; border-color:var(--primary); }
button:disabled { opacity:.55; cursor:not-allowed; }
.pet-state { text-align:center; padding:64px 20px; background:var(--bg-card); border-radius:16px; }
.pet-state button { margin-top:16px; }
@media(max-width:1000px) { .pet-page { grid-template-columns:1fr; } .pet-stage { position:static; } }
@media(max-width:480px) { .pet-heading { flex-wrap:wrap; } .pet-panel { padding:16px; } .pet-deco-grid { grid-template-columns:repeat(3,minmax(0,1fr)); } .pet-exp-bar { max-width:100%; } }
</style>
