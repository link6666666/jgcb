<template>
  <div class="page-content">
    <div class="profile-card">
      <div class="profile-header">
        <div class="avatar-wrapper" @click="triggerUpload">
          <img decoding="async" loading="lazy" v-if="user.avatar" :src="thumbUrl(user.avatar, 256)" class="avatar-img" @error="$event.target.src = user.avatar" />
          <span v-else class="avatar-placeholder">{{ (user.nickname || user.username || '?')[0] }}</span>
          <div class="avatar-overlay">更换头像</div>
        </div>
        <input ref="fileInput" type="file" accept="image/*" style="display:none" @change="handleAvatarChange" />
        <div class="profile-info">
          <h2>{{ user.nickname || user.username }}</h2>
          <p>@{{ user.username }}</p>
          <div class="level-badge">
            <span class="level-icon">⭐</span>
            <span class="level-text">Lv.{{ level }}</span>
            <span v-if="level >= 6" class="level-max">已满级</span>
          </div>
          <div class="exp-bar-wrapper">
            <div class="exp-bar">
              <div class="exp-bar-fill" :style="{ width: expPercent + '%' }"></div>
            </div>
            <span class="exp-text">{{ expCurrent }} / {{ expNext }} EXP</span>
          </div>
        </div>
      </div>
      <div class="profile-detail">
        <div class="detail-row">
          <label>昵称</label>
          <span v-if="!editing">{{ user.nickname || '未设置' }}</span>
          <input v-else v-model="form.nickname" class="profile-input" />
        </div>
        <div class="detail-row">
          <label>邮箱</label>
          <span v-if="!editing">{{ user.email || '未填写' }}</span>
          <input v-else v-model="form.email" class="profile-input" />
        </div>
        <div class="detail-row">
          <label>手机</label>
          <span v-if="!editing">{{ user.phone || '未填写' }}</span>
          <input v-else v-model="form.phone" class="profile-input" />
        </div>
        <div class="detail-row">
          <label>个性签名</label>
          <span v-if="!editing">{{ user.signature || '未填写' }}</span>
          <input v-else v-model="form.signature" class="profile-input" placeholder="写点什么吧~" maxlength="100" />
        </div>
        <div class="profile-actions">
          <button v-if="!editing" class="btn-edit" @click="startEdit">编辑资料</button>
          <button v-if="!editing" class="btn-password" @click="showPwdForm = !showPwdForm">修改密码</button>
          <template v-else>
            <button class="btn-save" @click="saveProfile" :disabled="saving">{{ saving ? '保存中...' : '保存' }}</button>
            <button class="btn-cancel-edit" @click="cancelEdit">取消</button>
          </template>
        </div>

        <div v-if="showPwdForm && !editing" class="password-form">
          <div class="detail-row">
            <label>原密码</label>
            <input v-model="pwdForm.oldPassword" type="password" class="profile-input" placeholder="请输入原密码" />
          </div>
          <div class="detail-row">
            <label>新密码</label>
            <input v-model="pwdForm.newPassword" type="password" class="profile-input" placeholder="至少6位" />
          </div>
          <div class="detail-row">
            <label>确认密码</label>
            <input v-model="pwdForm.confirmPassword" type="password" class="profile-input" placeholder="再次输入新密码" />
          </div>
          <div class="profile-actions">
            <button class="btn-save" @click="handleChangePwd" :disabled="changingPwd">{{ changingPwd ? '修改中...' : '确认修改' }}</button>
            <button class="btn-cancel-edit" @click="showPwdForm = false">取消</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { getUserInfo, updateAvatar, uploadFile, updateProfile, updatePassword } from '../api'
import { useToast } from '../composables/useToast'
import { thumbUrl } from '../utils/image'

const { toast } = useToast()
const fileInput = ref(null)
const user = reactive({})
const editing = ref(false)

const level = computed(() => Math.min(Math.floor((user.exp || 0) / 100) + 1, 6))
const expCurrent = computed(() => level.value >= 6 ? (user.exp || 0) : (user.exp || 0) % 100)
const expNext = computed(() => level.value >= 6 ? (user.exp || 0) : 100)
const expPercent = computed(() => level.value >= 6 ? 100 : ((user.exp || 0) % 100))
const saving = ref(false)
const form = reactive({ nickname: '', email: '', phone: '', signature: '' })
const showPwdForm = ref(false)
const changingPwd = ref(false)
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })

onMounted(async () => {
  try {
    const res = await getUserInfo()
    Object.assign(user, res.data)
    const stored = JSON.parse(localStorage.getItem('user') || '{}')
    localStorage.setItem('user', JSON.stringify({ ...res.data, userId: res.data.id, token: stored.token }))
  } catch (e) { /* ignore */ }
})

function triggerUpload() {
  fileInput.value.click()
}

async function handleAvatarChange(e) {
  const file = e.target.files[0]
  if (!file) return
  try {
    const res = await uploadFile(file)
    const url = res.data.url
    await updateAvatar({ avatarUrl: url })
    user.avatar = url
    const stored = JSON.parse(localStorage.getItem('user') || '{}')
    stored.avatar = url
    localStorage.setItem('user', JSON.stringify(stored))
    toast('头像更新成功', 'success')
  } catch (e) {
    toast('上传失败', 'error')
  }
}

function startEdit() {
  form.nickname = user.nickname || ''
  form.email = user.email || ''
  form.phone = user.phone || ''
  form.signature = user.signature || ''
  editing.value = true
}

function cancelEdit() {
  editing.value = false
}

async function handleChangePwd() {
  if (!pwdForm.oldPassword) {
    toast('请输入原密码', 'info')
    return
  }
  if (!pwdForm.newPassword || pwdForm.newPassword.length < 6) {
    toast('新密码至少6位', 'info')
    return
  }
  if (pwdForm.newPassword !== pwdForm.confirmPassword) {
    toast('两次输入的新密码不一致', 'info')
    return
  }
  changingPwd.value = true
  try {
    await updatePassword({ oldPassword: pwdForm.oldPassword, newPassword: pwdForm.newPassword })
    toast('密码修改成功', 'success')
    showPwdForm.value = false
    pwdForm.oldPassword = ''
    pwdForm.newPassword = ''
    pwdForm.confirmPassword = ''
  } catch (e) {
    toast(e.response?.data?.message || '密码修改失败', 'error')
  }
  changingPwd.value = false
}

async function saveProfile() {
  saving.value = true
  try {
    await updateProfile({ nickname: form.nickname, email: form.email, phone: form.phone, signature: form.signature })
    user.nickname = form.nickname
    user.email = form.email
    user.phone = form.phone
    user.signature = form.signature
    const stored = JSON.parse(localStorage.getItem('user') || '{}')
    stored.nickname = form.nickname
    stored.userId = stored.userId || stored.id
    localStorage.setItem('user', JSON.stringify(stored))
    editing.value = false
    toast('保存成功', 'success')
  } catch (e) {
    toast(e.response?.data?.message || '保存失败', 'error')
  }
  saving.value = false
}
</script>

<style scoped>
.level-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  margin-top: 8px;
  padding: 3px 10px;
  background: linear-gradient(135deg, rgba(99, 102, 241, .15), rgba(139, 92, 246, .15));
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
  color: var(--primary);
}

.level-max {
  font-size: 11px;
  color: #f59e0b;
  margin-left: 4px;
}

.exp-bar-wrapper {
  margin-top: 10px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.exp-bar {
  flex: 1;
  height: 8px;
  background: #e5e7eb;
  border-radius: 4px;
  overflow: hidden;
}

.exp-bar-fill {
  height: 100%;
  background: linear-gradient(90deg, var(--primary), #8b5cf6);
  border-radius: 4px;
  transition: width .5s ease;
}

.exp-text {
  font-size: 12px;
  color: var(--text-secondary, #999);
  white-space: nowrap;
}
</style>
