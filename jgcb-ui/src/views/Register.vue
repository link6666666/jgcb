<template>
  <div class="login-container">
    <div class="login-card">
      <h2>注册账号</h2>
      <div v-if="errorMsg" class="error-msg">{{ errorMsg }}</div>
      <div class="form-item">
        <label>用户名</label>
        <input v-model="form.username" placeholder="3-50位字符" />
      </div>
      <div class="form-item">
        <label>密码</label>
        <input v-model="form.password" type="password" placeholder="6-100位字符" />
      </div>
      <div class="form-item">
        <label>确认密码</label>
        <input v-model="confirmPassword" type="password" placeholder="请再次输入密码" />
      </div>
      <div class="form-item">
        <label>昵称</label>
        <input v-model="form.nickname" placeholder="可选" />
      </div>
      <div class="form-item">
        <label>邮箱</label>
        <input v-model="form.email" placeholder="可选" />
      </div>
      <div class="form-item">
        <label>手机号</label>
        <input v-model="form.phone" placeholder="可选" />
      </div>
      <button class="btn btn-primary" @click="handleRegister" :disabled="loading">
        {{ loading ? '注册中...' : '注 册' }}
      </button>
      <button class="btn btn-default" @click="$router.push('/login')">已有账号？去登录</button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { register } from '../api'
import { useToast } from '../composables/useToast'

const router = useRouter()
const { toast } = useToast()
const form = reactive({ username: '', password: '', nickname: '', email: '', phone: '' })
const confirmPassword = ref('')
const errorMsg = ref('')
const loading = ref(false)

async function handleRegister() {
  if (!form.username || !form.password) {
    errorMsg.value = '用户名和密码为必填项'
    return
  }
  if (form.username.length < 3) {
    errorMsg.value = '用户名至少3位'
    return
  }
  if (form.password.length < 6) {
    errorMsg.value = '密码至少6位'
    return
  }
  if (form.password !== confirmPassword.value) {
    errorMsg.value = '两次输入的密码不一致'
    return
  }
  loading.value = true
  errorMsg.value = ''
  try {
    await register(form)
    toast('注册成功，请登录', 'success')
    router.push('/login')
  } catch (e) {
    errorMsg.value = e.response?.data?.message || '注册失败'
  } finally {
    loading.value = false
  }
}
</script>
