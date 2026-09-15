<template>
  <div class="page-content">
    <div class="section-title">🎬 约电影</div>

    <div class="session-actions">
      <button class="btn-publish" @click="showForm = !showForm">
        {{ showForm ? '收起' : '+ 发起电影局' }}
      </button>
    </div>

    <div v-if="showForm" class="publish-card">
      <div class="form-item">
        <label>标题</label>
        <input v-model="form.title" placeholder="如：周末看电影" />
      </div>
      <div class="form-item">
        <label>电影名称</label>
        <input v-model="form.movieName" placeholder="想看哪部电影" />
      </div>
      <div class="form-item">
        <label>地点</label>
        <input v-model="form.location" placeholder="电影院/地点" />
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
      <div v-if="previewImages.length" class="preview-row">
        <div v-for="(img, i) in previewImages" :key="i" class="preview-item">
          <img decoding="async" loading="lazy" :src="img" class="preview-img" />
          <span class="preview-remove" @click="removePreview(i)">×</span>
        </div>
      </div>
      <div class="publish-bar">
        <span class="publish-add" @click="triggerImageSelect">+ 上传照片</span>
        <input ref="imageInput" type="file" accept="image/*" multiple style="display:none" @change="handleImagesChange" />
        <button class="btn-publish" @click="handleCreate" :disabled="creating || uploading">
          {{ uploading ? '图片上传中...' : creating ? '创建中...' : '创建电影局' }}
        </button>
      </div>
    </div>

    <div v-if="sessions.length === 0" class="empty-tip">
      <span class="empty-icon">🎬</span>
      暂无可加入的电影局
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
        <span v-if="s.movieName">🎞️ {{ s.movieName }}</span>
        <span>📍 {{ s.location || '未定' }}</span>
        <span>👤 发起人：{{ s.creatorName }}</span>
        <span>👥 {{ s.players ? s.players.length : 0 }}/{{ s.maxPlayers }}</span>
      </div>
      <div v-if="s.images && s.images.length" class="post-images" style="margin-bottom:10px">
        <img decoding="async" loading="lazy" v-for="(img, i) in s.images" :key="i" :src="thumbUrl(img, 600)" class="post-img" @error="$event.target.onerror = null; $event.target.src = img" />
      </div>
      <div v-if="s.players && s.players.length" class="sess-players">
        <img decoding="async" loading="lazy" v-for="p in s.players" :key="p.userId" :src="thumbUrl(p.avatar, 128)" class="sess-player-avatar"
          :title="p.nickname"
          @error="$event.target.style.display='none'" />
      </div>
      <div class="sess-actions">
        <button class="btn-detail" @click="showDetail(s.id)">详情</button>
        <button v-if="canCancel(s)" class="btn-cancel" @click="handleCancel(s.id)">取消电影局</button>
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
        <div v-if="detail.movieName" class="detail-row">
          <span class="detail-label">电影</span>
          <span>{{ detail.movieName }}</span>
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
        <div v-if="detail.images && detail.images.length" class="detail-section">
          <div class="detail-label" style="margin-bottom:8px">电影照片</div>
          <div class="post-images">
            <img decoding="async" loading="lazy" v-for="(img, i) in detail.images" :key="i" :src="thumbUrl(img, 600)" class="post-img" @error="$event.target.onerror = null; $event.target.src = img" />
          </div>
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
        <div v-if="isInDetailSession && (detail.status === 1 || detail.status === 2)" class="detail-section">
          <div class="detail-label" style="margin-bottom:8px">上传照片</div>
          <div class="detail-upload-row">
            <input ref="detailImagesInput" type="file" accept="image/*" multiple style="display:none" @change="handleDetailImagesChange" />
            <button class="btn-publish" @click="detailImagesInput.click()" :disabled="uploadingDetailImages">
              {{ uploadingDetailImages ? '上传中...' : '选择照片上传' }}
            </button>
          </div>
        </div>
        <div class="detail-actions">
          <button v-if="Number(detail.creatorId) === Number(currentUserId) && (detail.status === 1 || detail.status === 2)" class="btn-settle" @click="handleFinish(detail.id)">完结</button>
          <button class="btn-detail-close" @click="detailVisible = false">关闭</button>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { createMovie, getMovies, joinMovie, leaveMovie, cancelMovie, getMovieDetail, uploadMovieImages, finishMovie, uploadFile } from '../api'
import { useToast } from '../composables/useToast'
import { thumbUrl } from '../utils/image'

const { toast } = useToast()
const imageInput = ref(null)
const showForm = ref(false)
const creating = ref(false)
const sessions = ref([])
const currentUserId = ref(null)
const currentUserNickname = ref('')
const currentUserAvatar = ref('')
const detailVisible = ref(false)
const detail = ref({})
const detailImagesInput = ref(null)
const uploadingDetailImages = ref(false)
const currentPage = ref(1)
const totalPages = ref(1)
const hasMore = ref(false)
const loadingMore = ref(false)
const previewImages = ref([])
const uploadUrls = ref([])
const uploading = ref(false)

const form = reactive({
  title: '',
  movieName: '',
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
    const res = await getMovies(currentPage.value)
    sessions.value = res.data.records || []
    totalPages.value = res.data.pages || 1
    hasMore.value = currentPage.value < totalPages.value
  } catch (e) { /* ignore */ }
}

async function loadMore() {
  currentPage.value++
  loadingMore.value = true
  try {
    const res = await getMovies(currentPage.value)
    sessions.value.push(...(res.data.records || []))
    totalPages.value = res.data.pages || 1
    hasMore.value = currentPage.value < totalPages.value
  } catch (e) { /* ignore */ }
  loadingMore.value = false
}

function triggerImageSelect() {
  imageInput.value.click()
}

async function handleImagesChange(e) {
  const files = Array.from(e.target.files)
  if (files.length === 0) return
  uploading.value = true
  for (const file of files) {
    previewImages.value.push(URL.createObjectURL(file))
    try {
      const res = await uploadFile(file)
      uploadUrls.value.push(res.data.url)
    } catch (e) {
      toast('图片上传失败', 'error')
      previewImages.value.pop()
    }
  }
  uploading.value = false
  imageInput.value.value = ''
}

function removePreview(i) {
  previewImages.value.splice(i, 1)
  uploadUrls.value.splice(i, 1)
}

async function handleCreate() {
  if (!form.title || !form.sessionTime) {
    toast('请填写标题和时间', 'info')
    return
  }
  if (uploading.value) {
    toast('图片正在上传中，请稍候', 'info')
    return
  }
  creating.value = true
  try {
    await createMovie({ ...form, maxPlayers: Number(form.maxPlayers), images: uploadUrls.value })
    toast('电影局创建成功', 'success')
    showForm.value = false
    form.title = ''
    form.movieName = ''
    form.location = ''
    form.sessionTime = ''
    form.remark = ''
    form.maxPlayers = 4
    previewImages.value = []
    uploadUrls.value = []
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
    await joinMovie(id)
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
    await leaveMovie(id)
    toast('已退出', 'info')
  } catch (e) {
    s.players = oldPlayers
    s.status = oldStatus
    toast(e.response?.data?.message || '退出失败', 'error')
  }
}

async function handleCancel(id) {
  if (!confirm('确定取消该电影局？')) return
  const s = sessions.value.find(s => s.id === id)
  if (!s) return
  const oldStatus = s.status
  s.status = 0
  try {
    await cancelMovie(id)
    toast('电影局已取消', 'info')
  } catch (e) {
    s.status = oldStatus
    toast(e.response?.data?.message || '取消失败', 'error')
  }
}

async function showDetail(id) {
  try {
    const res = await getMovieDetail(id)
    detail.value = res.data
    detailVisible.value = true
  } catch (e) {
    toast('获取详情失败', 'error')
  }
}

async function handleFinish(id) {
  if (!confirm('确定完结该电影局？完结后不能上传照片。')) return
  try {
    await finishMovie(id)
    detail.value.status = 3
    toast('电影局已完结', 'success')
    await loadSessions()
  } catch (e) {
    toast(e.response?.data?.message || '完结失败', 'error')
  }
}

async function handleDetailImagesChange(e) {
  const files = Array.from(e.target.files)
  if (files.length === 0) return
  uploadingDetailImages.value = true
  const urls = []
  for (const file of files) {
    try {
      const res = await uploadFile(file)
      urls.push(res.data.url)
    } catch (e) {
      toast('图片上传失败', 'error')
    }
  }
  if (urls.length > 0) {
    await uploadMovieImages(detail.value.id, urls)
    if (!detail.value.images) detail.value.images = []
    detail.value.images.push(...urls)
    toast('照片上传成功', 'success')
    await loadSessions()
  }
  uploadingDetailImages.value = false
  detailImagesInput.value.value = ''
}


const isInDetailSession = computed(() => {
  if (!detail.value.players || !detail.value.players.length) return false
  return detail.value.players.some(p => Number(p.userId) === Number(currentUserId.value))
})

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
  return { 0: '已取消', 1: '进行中', 2: '已满员', 3: '已完结' }[status] || ''
}

function statusClass(status) {
  return { 0: 'ss-cancelled', 1: 'ss-open', 2: 'ss-full', 3: 'ss-settled' }[status] || ''
}

function formatTime(t) {
  if (!t) return ''
  return new Date(t).toLocaleString('zh-CN')
}
</script>
