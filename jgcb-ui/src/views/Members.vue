<template>
  <div class="page-content">
    <div class="section-title">👥 成员列表</div>
    <div v-if="members.length === 0" class="empty-tip">
      <span class="empty-icon">👤</span>
      暂无成员
    </div>
    <div class="member-grid">
      <div v-for="m in members" :key="m.id" class="member-card" :class="{ disabled: m.status === 0 }" @click="openMember(m)">
        <div class="member-avatar-wrapper">
          <img decoding="async" loading="lazy" v-if="m.avatar" :src="thumbUrl(m.avatar, 128)" class="member-avatar"
            @error="$event.target.src = m.avatar" />
          <span v-if="!m.avatar" class="member-avatar-placeholder">{{ (m.nickname || m.username || '?')[0] }}</span>
        </div>
        <div class="member-info">
          <span class="member-nickname">
            {{ m.nickname || m.username }}
            <span class="member-level-badge">Lv.{{ levelOf(m.exp) }}</span>
            <span v-if="m.status === 0" class="member-status-badge">已禁用</span>
          </span>
          <span class="member-username">@{{ m.username }}</span>
        </div>
        <span class="member-time">{{ formatTime(m.createdAt) }}</span>
      </div>
    </div>

    <teleport to="body">
      <div v-if="showModal" class="member-modal-overlay" @click.self="closeModal">
        <div class="member-modal">
          <button class="member-modal-close" @click="closeModal">✕</button>

          <div class="member-modal-header">
            <img decoding="async" loading="lazy" v-if="detail.avatar" :src="thumbUrl(detail.avatar, 128)" class="member-modal-avatar"
              @error="$event.target.src = detail.avatar" />
            <span v-else class="member-modal-avatar member-modal-avatar--placeholder">{{ (detail.nickname || detail.username || '?')[0] }}</span>
            <div class="member-modal-id">
              <h3>{{ detail.nickname || detail.username }}</h3>
              <p>@{{ detail.username }}</p>
              <span class="member-level-badge">Lv.{{ levelOf(detail.exp) }}</span>
            </div>
          </div>

          <div class="member-modal-signature">
            <span class="sig-label">个性签名</span>
            <span class="sig-text">{{ detail.signature || '这个人很懒，什么都没写~' }}</span>
          </div>

          <div class="member-modal-contact">
            <div class="contact-row">
              <span class="contact-label">邮箱</span>
              <span class="contact-text">{{ detail.email || '未填写' }}</span>
            </div>
            <div class="contact-row">
              <span class="contact-label">手机</span>
              <span class="contact-text">{{ detail.phone || '未填写' }}</span>
            </div>
          </div>

          <div class="member-modal-pet">
            <PetFigure
              :avatar="detail.avatar"
              :nickname="detail.nickname || detail.username"
              :outfit="petOutfit"
              :decorations="petDecorations"
              large
            />
          </div>
        </div>
      </div>
    </teleport>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getMembers, getUserProfile, getPetStatusOf } from '../api'
import { thumbUrl } from '../utils/image'
import PetFigure from '../components/PetFigure.vue'

const members = ref([])
const showModal = ref(false)
const detail = reactive({ id: 0, username: '', nickname: '', avatar: '', signature: '', email: '', phone: '', exp: 0 })
const petOutfit = reactive({ head: '', neck: '', body: '', accessory: '' })
const petDecorations = ref([])

onMounted(async () => {
  try {
    const res = await getMembers()
    members.value = res.data || []
  } catch (e) { /* ignore */ }
})

async function openMember(m) {
  showModal.value = true
  Object.assign(detail, {
    id: m.id,
    username: m.username || '',
    nickname: m.nickname || '',
    avatar: m.avatar || '',
    signature: '',
    email: '',
    phone: '',
    exp: m.exp || 0
  })
  petOutfit.head = ''
  petOutfit.neck = ''
  petOutfit.body = ''
  petOutfit.accessory = ''
  petDecorations.value = []
  await Promise.allSettled([
    getUserProfile(m.id).then(userRes => {
      if (!showModal.value || detail.id !== m.id) return
      const u = userRes.data || {}
      if (u.nickname) detail.nickname = u.nickname
      if (u.username) detail.username = u.username
      if (u.avatar) detail.avatar = u.avatar
      detail.signature = u.signature || ''
      detail.email = u.email || ''
      detail.phone = u.phone || ''
      detail.exp = u.exp || 0
    }),
    getPetStatusOf(m.id).then(petRes => {
      if (!showModal.value || detail.id !== m.id) return
      const p = petRes.data || {}
      petOutfit.head = p.head || ''
      petOutfit.neck = p.neck || ''
      petOutfit.body = p.body || ''
      petOutfit.accessory = p.accessory || ''
      petDecorations.value = p.decorations || []
    })
  ])
}
function closeModal() {
  showModal.value = false
}

function formatTime(t) {
  if (!t) return ''
  return new Date(t).toLocaleString('zh-CN')
}

function levelOf(exp) {
  return Math.min(Math.floor((exp || 0) / 100) + 1, 6)
}
</script>

<style scoped>
.member-card {
  cursor: pointer;
}

.member-level-badge {
  display: inline-block;
  padding: 1px 6px;
  background: linear-gradient(135deg, var(--primary), #8b5cf6);
  color: #fff;
  border-radius: 8px;
  font-size: 11px;
  font-weight: 600;
  vertical-align: middle;
}

.member-modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.member-modal {
  position: relative;
  background: var(--bg-card);
  border-radius: var(--radius);
  padding: 24px;
  width: 100%;
  max-width: 380px;
  box-shadow: var(--shadow-lg);
  max-height: 85vh;
  overflow-y: auto;
}

.member-modal-close {
  position: absolute;
  top: 12px;
  right: 12px;
  width: 28px;
  height: 28px;
  border: none;
  background: var(--bg-page);
  border-radius: 50%;
  cursor: pointer;
  font-size: 14px;
  color: var(--text-muted);
}

.member-modal-header {
  display: flex;
  align-items: center;
  gap: 14px;
}

.member-modal-avatar {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  object-fit: cover;
  background: var(--border);
}

.member-modal-avatar--placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--primary), var(--primary-light));
  color: #fff;
  font-size: 26px;
  font-weight: 700;
}

.member-modal-id h3 {
  font-size: 17px;
  color: var(--text-primary);
  margin-bottom: 2px;
}

.member-modal-id p {
  font-size: 13px;
  color: var(--text-muted);
  margin-bottom: 6px;
}

.member-modal-signature {
  margin: 18px 0;
  padding: 12px 14px;
  background: var(--bg-page);
  border-radius: var(--radius-sm);
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.sig-label {
  font-size: 11px;
  color: var(--text-muted);
}

.sig-text {
  font-size: 14px;
  color: var(--text-secondary);
  word-break: break-all;
}

.member-modal-contact {
  margin: 18px 0;
  padding: 12px 14px;
  background: var(--bg-page);
  border-radius: var(--radius-sm);
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.contact-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.contact-label {
  flex-shrink: 0;
  width: 40px;
  font-size: 11px;
  color: var(--text-muted);
}

.contact-text {
  font-size: 14px;
  color: var(--text-secondary);
  word-break: break-all;
}

.member-modal-pet {
  display: flex;
  justify-content: center;
  padding: 12px 0 4px;
}
</style>
