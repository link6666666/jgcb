<template>
  <div class="page-content">
    <div class="section-title">🎤 约唱歌</div>

    <div class="session-actions">
      <button class="btn-publish" @click="showForm = !showForm">
        {{ showForm ? '收起' : '+ 发起唱歌局' }}
      </button>
    </div>

    <div v-if="showForm" class="publish-card">
      <div class="form-item">
        <label>标题</label>
        <input v-model="form.title" placeholder="如：K歌之夜" />
      </div>
      <div class="form-item">
        <label>地点</label>
        <input v-model="form.location" placeholder="KTV地点" />
      </div>
      <div class="form-item">
        <label>时间</label>
        <input v-model="form.sessionTime" type="datetime-local" />
      </div>
      <div class="form-item">
        <label>备注</label>
        <input v-model="form.remark" placeholder="备注" />
      </div>
      <div class="form-item">
        <label>人数上限</label>
        <select v-model="form.maxPlayers">
          <option :value="2">2人</option>
          <option :value="3">3人</option>
          <option :value="4">4人</option>
          <option :value="5">5人</option>
          <option :value="6">6人</option>
          <option :value="7">7人</option>
          <option :value="8">8人</option>
          <option :value="9">9人</option>
          <option :value="10">10人</option>
        </select>
      </div>
      <button class="btn-publish" @click="handleCreate" :disabled="creating" style="width:100%;margin-top:12px">
        {{ creating ? '创建中...' : '创建唱歌局' }}
      </button>
    </div>

    <div v-if="sessions.length === 0" class="empty-tip">
      <span class="empty-icon">🎤</span>
      暂无可加入的唱歌局
    </div>

    <div v-for="s in sessions" :key="s.id" class="session-card" :class="{ cancelled: s.status === 0 }">
      <div class="sess-header">
        <div>
          <span class="sess-title">{{ s.title }}</span>
          <span class="sess-status" :class="statusClass(s.status)">{{ statusText(s.status) }}</span>
        </div>
        <span class="sess-time">{{ formatTime(s.sessionTime) }}</span>
      </div>
      <div class="sess-info">
        <span>📍 {{ s.location || '未定' }}</span>
        <span>👤 发起人：{{ s.creatorName }}</span>
        <span>👥 {{ s.players ? s.players.length : 0 }}/{{ s.maxPlayers }}</span>
      </div>
      <div v-if="s.players && s.players.length" class="sess-players">
        <img decoding="async" loading="lazy" v-for="p in s.players" :key="p.userId" :src="thumbUrl(p.avatar, 128)" class="sess-player-avatar"
          :title="p.nickname"
          @error="$event.target.style.display='none'" />
      </div>
      <div class="sess-actions">
        <button class="btn-detail" @click="showDetail(s.id)">详情</button>
        <button v-if="canCancel(s)" class="btn-cancel" @click="handleCancel(s.id)">取消唱歌局</button>
        <button v-if="canJoin(s)" class="btn-join" @click="handleJoin(s.id)">加入</button>
        <button v-if="canLeave(s)" class="btn-leave" @click="handleLeave(s.id)">退出</button>
      </div>
    </div>

    <div v-if="hasMore" class="load-more">
      <button class="btn-more" @click="loadMore" :disabled="loadingMore">加载更多</button>
    </div>

    <div v-if="detailVisible" class="modal-overlay" @click.self="detailVisible = false">
      <div class="modal-card">
        <h3>{{ detail.title }}</h3>
        <div class="detail-row">
          <span class="detail-label">状态</span>
          <span :class="statusClass(detail.status)">{{ statusText(detail.status) }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">地点</span>
          <span>{{ detail.location || '未定' }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">时间</span>
          <span>{{ formatTime(detail.sessionTime) }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">发起人</span>
          <span>{{ detail.creatorName }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">人数</span>
          <span>{{ detail.players ? detail.players.length : 0 }}/{{ detail.maxPlayers }}</span>
        </div>
        <div v-if="detail.remark" class="detail-row">
          <span class="detail-label">备注</span>
          <span>{{ detail.remark }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">创建时间</span>
          <span>{{ formatTime(detail.createdAt) }}</span>
        </div>
        <div v-if="detail.players && detail.players.length" class="detail-section">
          <div class="detail-label" style="margin-bottom:8px">参与玩家</div>
          <div class="detail-players">
            <div v-for="p in detail.players" :key="p.userId" class="detail-player-item">
              <img decoding="async" loading="lazy" :src="thumbUrl(p.avatar, 128)" class="detail-player-avatar" @error="$event.target.style.display='none'" />
              <span>{{ p.nickname }}</span>
            </div>
          </div>
        </div>
        <div v-if="detail.status === 3 && detail.result && detail.result.length" class="detail-section">
          <div class="detail-label" style="margin-bottom:8px">结算结果</div>
          <div class="settle-results">
            <div v-for="p in detail.result" :key="p.userId" class="settle-result-item" :class="{ winner: p.score > 0, loser: p.score < 0 }">
              <img decoding="async" loading="lazy" :src="thumbUrl(p.avatar, 128)" class="detail-player-avatar" @error="$event.target.style.display='none'" />
              <span class="settle-name">{{ p.nickname }}</span>
              <span class="settle-score">{{ p.score > 0 ? '+' : '' }}{{ p.score }}</span>
            </div>
          </div>
        </div>
        <div class="detail-actions">
          <button v-if="Number(detail.creatorId) === Number(currentUserId) && (detail.status === 1 || detail.status === 2)" class="btn-settle" @click="openSettle">结算</button>
          <button class="btn-detail-close" @click="detailVisible = false">关闭</button>
        </div>
      </div>
    </div>

    <div v-if="settleVisible" class="modal-overlay" @click.self="settleVisible = false">
      <div class="modal-card">
        <h3>唱歌局结算</h3>
        <div v-for="(p, i) in settleForm" :key="p.userId" class="settle-input-row">
          <img decoding="async" loading="lazy" :src="thumbUrl(p.avatar, 128)" class="detail-player-avatar" @error="$event.target.style.display='none'" />
          <span class="settle-name">{{ p.nickname }}</span>
          <input v-model.number="p.score" type="number" class="settle-input" placeholder="输赢分数" />
        </div>
        <div class="detail-actions">
          <button class="btn-settle" @click="submitSettle" :disabled="settling">{{ settling ? '结算中...' : '确认结算' }}</button>
          <button class="btn-detail-close" @click="settleVisible = false">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { createKaraoke, getKaraokes, joinKaraoke, leaveKaraoke, cancelKaraoke, getKaraokeDetail, settleKaraoke } from '../api'
import { useToast } from '../composables/useToast'
import { thumbUrl } from '../utils/image'

const { toast } = useToast()
const showForm = ref(false)
const creating = ref(false)
const sessions = ref([])
const currentUserId = ref(null)
const currentUserNickname = ref('')
const currentUserAvatar = ref('')
const detailVisible = ref(false)
const detail = ref({})
const settleVisible = ref(false)
const settleForm = ref([])
const settling = ref(false)
const currentPage = ref(1)
const totalPages = ref(1)
const hasMore = ref(false)
const loadingMore = ref(false)

const form = reactive({
  title: '',
  location: '',
  sessionTime: '',
  remark: '',
  maxPlayers: 4
})

onMounted(async () => {
  const stored = localStorage.getItem('user')
  if (stored) {
    const user = JSON.parse(stored)
    currentUserId.value = user.userId || user.id
    currentUserNickname.value = user.nickname || user.username || ''
    currentUserAvatar.value = user.avatar || ''
  }
  await loadSessions()
})

async function loadSessions() {
  try {
    const res = await getKaraokes(currentPage.value)
    sessions.value = res.data.records || []
    totalPages.value = res.data.pages || 1
    hasMore.value = currentPage.value < totalPages.value
  } catch (e) { /* ignore */ }
}

async function loadMore() {
  currentPage.value++
  loadingMore.value = true
  try {
    const res = await getKaraokes(currentPage.value)
    sessions.value.push(...(res.data.records || []))
    totalPages.value = res.data.pages || 1
    hasMore.value = currentPage.value < totalPages.value
  } catch (e) { /* ignore */ }
  loadingMore.value = false
}

async function handleCreate() {
  if (!form.title || !form.sessionTime) {
    toast('请填写标题和时间', 'info')
    return
  }
  creating.value = true
  try {
    await createKaraoke({ ...form, maxPlayers: Number(form.maxPlayers) })
    toast('唱歌局创建成功', 'success')
    showForm.value = false
    form.title = ''
    form.location = ''
    form.sessionTime = ''
    form.remark = ''
    form.maxPlayers = 4
    currentPage.value = 1
    await loadSessions()
  } catch (e) {
    toast(e.response?.data?.message || '创建失败', 'error')
  }
  creating.value = false
}

async function handleJoin(id) {
  const s = sessions.value.find(s => s.id === id)
  if (!s || !s.players || s.creatorId === currentUserId.value) return
  const oldStatus = s.status
  if (!s.players.some(p => p.userId === currentUserId.value)) {
    s.players.push({ userId: currentUserId.value, nickname: currentUserNickname.value, avatar: currentUserAvatar.value })
    if (s.players.length >= s.maxPlayers) s.status = 2
  }
  try {
    await joinKaraoke(id)
    toast('加入成功', 'success')
  } catch (e) {
    s.players = s.players.filter(p => p.userId !== currentUserId.value)
    s.status = oldStatus
    toast(e.response?.data?.message || '加入失败', 'error')
  }
}

async function handleLeave(id) {
  const s = sessions.value.find(s => s.id === id)
  if (!s || !s.players) return
  const oldPlayers = [...s.players]
  const oldStatus = s.status
  s.players = s.players.filter(p => p.userId !== currentUserId.value)
  s.status = 1
  try {
    await leaveKaraoke(id)
    toast('已退出', 'info')
  } catch (e) {
    s.players = oldPlayers
    s.status = oldStatus
    toast(e.response?.data?.message || '退出失败', 'error')
  }
}

async function handleCancel(id) {
  if (!confirm('确定取消该唱歌局？')) return
  const s = sessions.value.find(s => s.id === id)
  if (!s) return
  const oldStatus = s.status
  s.status = 0
  try {
    await cancelKaraoke(id)
    toast('唱歌局已取消', 'info')
  } catch (e) {
    s.status = oldStatus
    toast(e.response?.data?.message || '取消失败', 'error')
  }
}

async function showDetail(id) {
  try {
    const res = await getKaraokeDetail(id)
    detail.value = res.data
    detailVisible.value = true
  } catch (e) {
    toast('获取详情失败', 'error')
  }
}

function openSettle() {
  settleForm.value = (detail.value.players || []).map(p => ({
    userId: p.userId,
    nickname: p.nickname,
    avatar: p.avatar,
    score: 0
  }))
  settleVisible.value = true
}

async function submitSettle() {
  settling.value = true
  try {
    await settleKaraoke(detail.value.id, { items: settleForm.value.map(p => ({ userId: p.userId, score: p.score || 0 })) })
    toast('结算成功', 'success')
    settleVisible.value = false
    await showDetail(detail.value.id)
    await loadSessions()
  } catch (e) {
    toast(e.response?.data?.message || '结算失败', 'error')
  }
  settling.value = false
}

function isCreator(s) {
  return s.creatorId != null && currentUserId.value != null && Number(s.creatorId) === Number(currentUserId.value)
}

function canCancel(s) {
  if (!isCreator(s)) return false
  return s.status !== 0
}

function canJoin(s) {
  if (isCreator(s)) return false
  if (s.status !== 1) return false
  return !isInSession(s)
}

function canLeave(s) {
  if (isCreator(s)) return false
  if (s.status !== 1) return false
  return isInSession(s)
}

function isInSession(s) {
  if (!s.players || !s.players.length) return false
  return s.players.some(p => Number(p.userId) === Number(currentUserId.value))
}

function statusText(status) {
  return { 0: '已取消', 1: '进行中', 2: '已满员', 3: '已结算' }[status] || ''
}

function statusClass(status) {
  return { 0: 'ss-cancelled', 1: 'ss-open', 2: 'ss-full', 3: 'ss-settled' }[status] || ''
}

function formatTime(t) {
  if (!t) return ''
  return new Date(t).toLocaleString('zh-CN')
}
</script>
