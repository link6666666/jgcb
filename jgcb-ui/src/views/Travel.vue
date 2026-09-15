<template>
  <div class="page-content">
    <div class="publish-card">
      <input v-model="form.title" placeholder="旅游标题" class="publish-title-input" />
      <input v-model="form.destination" placeholder="旅行目的地" class="publish-title-input" />
      <input v-model.number="form.participantCount" placeholder="旅游人数" type="number" min="1" class="publish-title-input" />
      <div class="travel-date-row">
        <label>开始日期</label>
        <input v-model="form.startDate" type="date" class="travel-date-input" />
        <label>结束日期</label>
        <input v-model="form.endDate" type="date" class="travel-date-input" />
      </div>
      <textarea v-model="form.plan" placeholder="旅游计划..." class="publish-input travel-textarea"></textarea>
      <textarea v-model="form.process" placeholder="旅游过程..." class="publish-input travel-textarea"></textarea>
      <div v-if="previewImages.length" class="preview-row">
        <div v-for="(img, i) in previewImages" :key="i" class="preview-item">
          <img decoding="async" loading="lazy" :src="img" class="preview-img" />
          <span class="preview-remove" @click="removePreview(i)">&times;</span>
        </div>
      </div>
      <div class="publish-bar">
        <span class="publish-add" @click="triggerImageSelect">+ 图片</span>
        <input ref="imageInput" type="file" accept="image/*" multiple style="display:none" @change="handleImagesChange" />
        <button class="btn-publish" @click="handleCreate" :disabled="creating || uploading">
          {{ uploading ? '图片上传中...' : creating ? '发布中...' : '发布' }}
        </button>
      </div>
    </div>

    <div class="section-title">✈️ 旅游记录</div>
    <div v-if="travels.length === 0" class="empty-tip">
      <span class="empty-icon">🗺️</span>
      还没有旅游记录，发起你的第一次旅行吧
    </div>
    <div class="memoir-list">
      <div v-for="t in travels" :key="t.id" class="memoir-card" @click="showDetail(t.id)">
        <div class="memoir-cover" v-if="t.images && t.images.length">
          <img decoding="async" :src="thumbUrl(t.images[0], 600)" loading="lazy" class="memoir-cover-img" @error="$event.target.src = t.images[0]" />
        </div>
        <div class="memoir-body">
          <h4 class="memoir-title">{{ t.title }}</h4>
          <p class="memoir-excerpt">
            <span v-if="t.destination">目的地：{{ t.destination }}</span>
            <span v-if="t.participantCount"> | {{ t.participantCount }}人</span>
            <span v-if="t.startDate"> | {{ t.startDate }} ~ {{ t.endDate }}</span>
          </p>
          <div class="memoir-footer">
            <div class="memoir-author">
              <img decoding="async" loading="lazy" v-if="t.avatar" :src="thumbUrl(t.avatar, 128)" class="memoir-avatar" @error="$event.target.src = t.avatar" />
              <span v-else class="memoir-avatar-placeholder">{{ (t.nickname || '?')[0] }}</span>
              <span class="memoir-nickname">{{ t.nickname }}</span>
            </div>
            <span class="memoir-time">{{ formatTime(t.createdAt) }}</span>
          </div>
        </div>
      </div>
    </div>
    <div v-if="hasMore" class="load-more">
      <button class="btn-more" @click="loadMore" :disabled="loadingMore">加载更多</button>
    </div>

    <div v-if="detailVisible" class="modal-overlay" @click.self="detailVisible = false">
      <div class="modal-card travel-detail-modal">
        <h3>{{ detail.title }}</h3>
        <div class="detail-row">
          <span class="detail-label">目的地</span>
          <span>{{ detail.destination }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">时间</span>
          <span>{{ detail.startDate }} ~ {{ detail.endDate }}</span>
        </div>
        <div class="detail-row" v-if="detail.participantCount">
          <span class="detail-label">人数</span>
          <span>{{ detail.participantCount }}人</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">作者</span>
          <span>{{ detail.nickname }}</span>
        </div>
        <div v-if="detail.plan" class="detail-section">
          <h4>旅游计划</h4>
          <p class="travel-detail-text">{{ detail.plan }}</p>
        </div>
        <div v-if="detail.process" class="detail-section">
          <h4>旅游过程</h4>
          <p class="travel-detail-text">{{ detail.process }}</p>
        </div>
        <div v-if="detail.images && detail.images.length" class="memoir-detail-images">
          <img decoding="async" v-for="(img, i) in detail.images" :key="i" :src="thumbUrl(img, 1600)" loading="lazy" class="memoir-detail-img" @error="$event.target.src = img" />
        </div>
        <div class="detail-actions">
          <button v-if="detail.userId === currentUserId" class="btn-cancel" @click="handleDelete(detail.id)">删除</button>
          <button class="btn-detail-close" @click="detailVisible = false">关闭</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getTravels, createTravel, getTravelDetail, deleteTravel, uploadFile } from '../api'
import { useToast } from '../composables/useToast'
import { thumbUrl } from '../utils/image'

const { toast } = useToast()
const imageInput = ref(null)
const form = reactive({
  title: '',
  destination: '',
  participantCount: null,
  startDate: '',
  endDate: '',
  plan: '',
  process: ''
})
const previewImages = ref([])
const uploadUrls = ref([])
const uploading = ref(false)
const creating = ref(false)
const travels = ref([])
const currentUserId = ref(null)
const currentPage = ref(1)
const totalPages = ref(1)
const hasMore = ref(false)
const loadingMore = ref(false)
const detailVisible = ref(false)
const detail = ref({})

onMounted(async () => {
  const stored = localStorage.getItem('user')
  if (stored) {
    currentUserId.value = JSON.parse(stored).userId || JSON.parse(stored).id
  }
  await loadTravels()
})

async function loadTravels() {
  try {
    const res = await getTravels(currentPage.value)
    travels.value = res.data.records || []
    totalPages.value = res.data.pages || 1
    hasMore.value = currentPage.value < totalPages.value
  } catch (e) { /* ignore */ }
}

async function loadMore() {
  currentPage.value++
  loadingMore.value = true
  try {
    const res = await getTravels(currentPage.value)
    travels.value.push(...(res.data.records || []))
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
  if (!form.title.trim()) {
    toast('请输入标题', 'info')
    return
  }
  if (uploading.value) {
    toast('图片正在上传中，请稍候', 'info')
    return
  }
  creating.value = true
  try {
    await createTravel({ ...form, images: uploadUrls.value })
    toast('发布成功', 'success')
    form.title = ''
    form.destination = ''
    form.participantCount = null
    form.startDate = ''
    form.endDate = ''
    form.plan = ''
    form.process = ''
    previewImages.value = []
    uploadUrls.value = []
    currentPage.value = 1
    await loadTravels()
  } catch (e) {
    toast('发布失败', 'error')
  }
  creating.value = false
}

async function showDetail(id) {
  try {
    const res = await getTravelDetail(id)
    detail.value = res.data
    detailVisible.value = true
  } catch (e) {
    toast('获取详情失败', 'error')
  }
}

async function handleDelete(id) {
  if (!confirm('确定删除？')) return
  try {
    await deleteTravel(id)
    detailVisible.value = false
    travels.value = travels.value.filter(t => t.id !== id)
    toast('删除成功', 'success')
  } catch (e) {
    toast('删除失败', 'error')
  }
}

function formatTime(t) {
  if (!t) return ''
  return new Date(t).toLocaleString('zh-CN')
}
</script>
