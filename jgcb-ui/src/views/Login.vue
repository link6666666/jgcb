<template>
  <div class="login-container">
    <div class="login-card">
      <h2>蕨根糍粑 管理系统</h2>
      <div v-if="errorMsg" class="error-msg">{{ errorMsg }}</div>
      <div class="form-item">
        <label>用户名</label>
        <input v-model="form.username" placeholder="请输入用户名" @keyup.enter="handleLogin" />
      </div>
      <div class="form-item">
        <label>密码</label>
        <input v-model="form.password" type="password" placeholder="请输入密码" @keyup.enter="handleLogin" />
      </div>
      <button class="btn btn-primary" @click="handleLogin" :disabled="loading">
        {{ loading ? '登录中...' : '登 录' }}
      </button>
      <button class="btn btn-default" @click="$router.push('/register')">没有账号？去注册</button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { login } from '../api'
import { setToken, setUser } from '../utils/auth'

const router = useRouter()
const form = reactive({ username: '', password: '' })
const errorMsg = ref('')
const loading = ref(false)

async function handleLogin() {
  if (!form.username || !form.password) {
    errorMsg.value = '请输入用户名和密码'
    return
  }
  loading.value = true
  errorMsg.value = ''
  try {
    const res = await login(form)
    setToken(res.data.token)
    setUser(res.data)
    router.push('/')
  } catch (e) {
    errorMsg.value = e.response?.data?.message || '登录失败，请检查网络连接'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  justify-content: flex-end;
  padding: clamp(24px, 6vw, 96px);
  background:
    linear-gradient(90deg, rgba(45, 28, 12, 0.04) 0%, rgba(45, 28, 12, 0.02) 48%, rgba(45, 28, 12, 0.14) 100%),
    url('/login-background.jpg') center center / cover no-repeat;
}

.login-card {
  margin-right: clamp(0px, 4vw, 72px);
  background: rgba(255, 252, 247, 0.94);
  border: 1px solid rgba(255, 255, 255, 0.72);
  box-shadow: 0 24px 70px rgba(69, 42, 18, 0.22);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
}

@media (max-width: 768px) {
  .login-container {
    justify-content: center;
    padding: 20px;
    background-position: 32% center;
  }

  .login-card {
    margin-right: 0;
    background: rgba(255, 252, 247, 0.96);
  }
}
</style>