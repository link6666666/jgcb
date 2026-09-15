<template>
  <div class="page-content">
    <div class="publish-card">
      <input v-model="newTitle" placeholder="回忆标题" class="publish-title-input" />
      <textarea v-model="newContent" placeholder="写下你的回忆..." class="publish-input"></textarea>
      <div v-if="previewImages.length" class="preview-row">
        <div v-for="(item, i) in previewImages" :key="i" class="preview-item">
          <video preload="none" v-if="item.video" :src="item.url" class="preview-img" muted></video>
          <img decoding="async" loading="lazy" v-else :src="item.url" class="preview-img" />
          <span class="preview-remove" @click="removePreview(i)">&times;</span>
        </div>
      </div>
      <div class="publish-bar">
        <span class="publish-add" @click="triggerImageSelect">+ 图片/视频</span>
        <input ref="imageInput" type="file" accept="image/*,video/*" multiple style="display:none" @change="handleImagesChange" />
        <button class="btn-publish" @click="handlePublish" :disabled="publishing || uploading">
          {{ uploading ? '文件上传中...' : publishing ? '发布中...' : '发布' }}
        </button>
      </div>
    </div>

    <div class="section-title">📖 回忆录</div>
    <div v-if="memoirs.length === 0" class="empty-tip">
      <span class="empty-icon">📝</span>
      还没有回忆录，写下你的第一篇吧
    </div>
    <div class="memoir-list">
    <div v-for="m in memoirs" :key="m.id" class="memoir-card" @click="showDetail(m.id)">
      <div class="memoir-cover" v-if="m.images && m.images.length">
        <img decoding="async" :src="thumbUrl(m.images[0], 600)" loading="lazy" class="memoir-cover-img" @error="onCoverError($event, m.images[0])" />
        <span v-if="isVideo(m.images[0])" class="video-play-badge">▶</span>
      </div>
      <div class="memoir-body">
        <h4 class="memoir-title">{{ m.title }}</h4>
        <p class="memoir-excerpt">{{ m.content ? m.content.substring(0, 100) : '' }}</p>
        <div class="memoir-footer">
          <div class="memoir-author">
            <img decoding="async" loading="lazy" v-if="m.avatar" :src="thumbUrl(m.avatar, 128)" class="memoir-avatar" @error="$event.target.src = m.avatar" />
            <span v-else class="memoir-avatar-placeholder">{{ (m.nickname || '?')[0] }}</span>
            <span class="memoir-nickname">{{ m.nickname }}</span>
          </div>
          <div class="memoir-stats">
            <span class="stat-item">❤️ {{ m.likeCount || 0 }}</span>
            <span class="stat-item">💬 {{ m.commentCount || 0 }}</span>
            <span class="memoir-time">{{ formatTime(m.createdAt) }}</span>
          </div>
        </div>
      </div>
    </div>
    </div>
    <div v-if="hasMore" class="load-more">
      <button class="btn-more" @click="loadMore" :disabled="loadingMore">加载更多</button>
    </div>

    <div v-if="detailVisible" class="modal-overlay" @click.self="closeDetail">
      <div class="modal-card memoir-detail">
        <h3>{{ detail.title }}</h3>
        <div class="detail-row">
          <span class="detail-label">作者</span>
          <span>{{ detail.nickname }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">时间</span>
          <span>{{ formatTime(detail.createdAt) }}</span>
        </div>
        <div v-if="detail.content" class="memoir-detail-content">{{ detail.content }}</div>
        <div v-if="detail.images && detail.images.length" class="memoir-detail-images">
          <template v-for="(media, i) in detail.images" :key="i">
            <video preload="none" v-if="isVideo(media)" :src="media" controls class="memoir-detail-img"></video>
            <img decoding="async" v-else :src="thumbUrl(media, 1600)" loading="lazy" class="memoir-detail-img" @error="$event.target.src = media" />
          </template>
        </div>

        <div class="like-bar">
          <button class="btn-like" :class="{ liked: detail.liked }" @click="handleLike">
            {{ detail.liked ? '❤️' : '🤍' }} {{ detail.likeCount || 0 }}
          </button>
        </div>

        <div class="comment-section">
          <h4>评论 ({{ comments.length }})</h4>
          <div v-if="comments.length === 0" class="comment-empty">暂无评论</div>
          <div v-for="c in comments" :key="c.id" class="comment-item">
            <img decoding="async" loading="lazy" v-if="c.avatar" :src="thumbUrl(c.avatar, 128)" class="comment-avatar" @error="$event.target.src = c.avatar" />
            <span v-else class="comment-avatar-placeholder">{{ (c.nickname || '?')[0] }}</span>
            <div class="comment-body">
              <div class="comment-header">
                <span class="comment-nickname">{{ c.nickname }}</span>
                <span class="comment-time">{{ formatTime(c.createdAt) }}</span>
              </div>
              <div class="comment-content">{{ c.content }}</div>
            </div>
            <button v-if="c.userId === currentUserId" class="comment-delete" @click="handleDeleteComment(c.id)">×</button>
          </div>
          <div class="comment-input-row">
            <input v-model="commentText" placeholder="写下你的评论..." class="comment-input" @keyup.enter="handleAddComment" />
            <button class="btn-comment-submit" @click="handleAddComment" :disabled="!commentText.trim() || sendingComment">发送</button>
          </div>
        </div>

        <div class="detail-actions">
          <button v-if="detail.userId === currentUserId" class="btn-cancel" @click="handleDelete(detail.id)">删除</button>
          <button class="btn-detail-close" @click="closeDetail">关闭</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getMemoirs, createMemoir, getMemoirDetail, deleteMemoir, uploadFile, likeMemoir, getMemoirComments, addMemoirComment, deleteMemoirComment } from '../api'
import { useToast } from '../composables/useToast'
import { thumbUrl, isVideo } from '../utils/image'

const { toast } = useToast()
const imageInput = ref(null)
const newTitle = ref('')
const newContent = ref('')
const previewImages = ref([])
const uploadUrls = ref([])
const uploading = ref(false)
const publishing = ref(false)
const memoirs = ref([])
const currentUserId = ref(null)
const currentPage = ref(1)
const totalPages = ref(1)
const hasMore = ref(false)
const loadingMore = ref(false)
const detailVisible = ref(false)
const detail = ref({})
const comments = ref([])
const commentText = ref('')
const sendingComment = ref(false)

onMounted(async () => {
  const stored = localStorage.getItem('user')
  if (stored) {
    currentUserId.value = JSON.parse(stored).userId || JSON.parse(stored).id
  }
  await loadMemoirs()
})

async function loadMemoirs() {
  try {
    const res = await getMemoirs(currentPage.value)
    memoirs.value = res.data.records || []
    totalPages.value = res.data.pages || 1
    hasMore.value = currentPage.value < totalPages.value
  } catch (e) { /* ignore */ }
}

async function loadMore() {
  currentPage.value++
  loadingMore.value = true
  try {
    const res = await getMemoirs(currentPage.value)
    memoirs.value.push(...(res.data.records || []))
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
    previewImages.value.push({ url: URL.createObjectURL(file), video: file.type.startsWith('video/') })
    try {
      const res = await uploadFile(file)
      uploadUrls.value.push(res.data.url)
    } catch (e) {
      toast('文件上传失败', 'error')
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

function onCoverError(e, url) {
  if (isVideo(url)) {
    e.target.style.display = 'none'
  } else {
    e.target.src = url
  }
}

async function handlePublish() {
  if (!newTitle.value.trim()) {
    toast('请输入标题', 'info')
    return
  }
  if (uploading.value) {
    toast('图片正在上传中，请稍候', 'info')
    return
  }
  publishing.value = true
  try {
    await createMemoir({ title: newTitle.value, content: newContent.value, images: uploadUrls.value })
    toast('发布成功', 'success')
    newTitle.value = ''
    newContent.value = ''
    previewImages.value = []
    uploadUrls.value = []
    currentPage.value = 1
    await loadMemoirs()
  } catch (e) {
    toast('发布失败', 'error')
  }
  publishing.value = false
}

async function showDetail(id) {
  try {
    const res = await getMemoirDetail(id)
    detail.value = res.data
    detailVisible.value = true
    await loadComments()
  } catch (e) {
    toast('获取详情失败', 'error')
  }
}

async function loadComments() {
  try {
    const res = await getMemoirComments(detail.value.id)
    comments.value = res.data || []
  } catch (e) { /* ignore */ }
}

async function handleLike() {
  try {
    const res = await likeMemoir(detail.value.id)
    detail.value.liked = res.data.liked
    detail.value.likeCount = res.data.likeCount
    const idx = memoirs.value.findIndex(m => m.id === detail.value.id)
    if (idx >= 0) {
      memoirs.value[idx].likeCount = res.data.likeCount
    }
  } catch (e) {
    toast('操作失败', 'error')
  }
}

async function handleAddComment() {
  if (!commentText.value.trim() || sendingComment.value) return
  sendingComment.value = true
  try {
    await addMemoirComment(detail.value.id, commentText.value.trim())
    commentText.value = ''
    await loadComments()
    detail.value.commentCount = (detail.value.commentCount || 0) + 1
    const idx = memoirs.value.findIndex(m => m.id === detail.value.id)
    if (idx >= 0) {
      memoirs.value[idx].commentCount = detail.value.commentCount
    }
  } catch (e) {
    toast('评论失败', 'error')
  }
  sendingComment.value = false
}

async function handleDeleteComment(commentId) {
  try {
    await deleteMemoirComment(detail.value.id, commentId)
    comments.value = comments.value.filter(c => c.id !== commentId)
    detail.value.commentCount = Math.max(0, (detail.value.commentCount || 1) - 1)
    const idx = memoirs.value.findIndex(m => m.id === detail.value.id)
    if (idx >= 0) {
      memoirs.value[idx].commentCount = detail.value.commentCount
    }
  } catch (e) {
    toast('删除失败', 'error')
  }
}

function closeDetail() {
  detailVisible.value = false
  comments.value = []
  commentText.value = ''
}

async function handleDelete(id) {
  if (!confirm('确定删除？')) return
  try {
    await deleteMemoir(id)
    detailVisible.value = false
    memoirs.value = memoirs.value.filter(m => m.id !== id)
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
