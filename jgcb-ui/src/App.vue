<template>
  <div class="layout">
    <button v-if="showSidebar" type="button" aria-label="切换导航菜单" aria-controls="main-navigation" :aria-expanded="sidebarOpen" class="sidebar-toggle" :class="{ open: sidebarOpen }" @click="toggleSidebar">
      {{ sidebarOpen ? '✕' : '☰' }}
    </button>
    <div class="sidebar-overlay" :class="{ show: sidebarOpen }" @click="closeSidebar"></div>

    <aside id="main-navigation" class="sidebar" :class="{ open: sidebarOpen }" v-if="showSidebar">
      <div class="sidebar-header">
        <img src="/logo.png" alt="JGCB" class="sidebar-logo" />
        <span class="sidebar-brand">蕨根糍粑</span>
      </div>

      <div class="sidebar-user">
        <img v-if="user.avatar" :src="thumbUrl(user.avatar, 128)" class="sidebar-user-avatar" @error="$event.target.src = user.avatar" />
        <span v-else class="sidebar-user-placeholder">{{ (user.nickname || user.username || '?')[0] }}</span>
        <span class="sidebar-user-name">{{ user.nickname || user.username }}</span>
        <span class="sidebar-user-level" v-if="userLevel > 0">Lv.{{ userLevel }}</span>
      </div>

      <nav class="sidebar-nav">
        <span class="nav-section">主菜单</span>
        <router-link to="/" class="nav-item" @click="closeSidebar">
          <span class="nav-icon">🏠</span> 首页
        </router-link>
        <router-link to="/members" class="nav-item" @click="closeSidebar">
          <span class="nav-icon">👥</span> 成员列表
        </router-link>
        <router-link to="/profile" class="nav-item" @click="closeSidebar">
          <span class="nav-icon">👤</span> 个人主页
        </router-link>
        <router-link to="/checkin" class="nav-item" @click="closeSidebar">
          <span class="nav-icon">📅</span> 每日签到
        </router-link>
        <router-link to="/pet" class="nav-item" @click="closeSidebar">
          <span class="nav-icon">🐾</span> 我的糍粑
        </router-link>

        <span class="nav-section">活动</span>
        <router-link to="/travel" class="nav-item" @click="closeSidebar">
          <span class="nav-icon">✈️</span> 旅游
        </router-link>
        <router-link to="/memoirs" class="nav-item" @click="closeSidebar">
          <span class="nav-icon">📖</span> 回忆录
        </router-link>
        <router-link to="/mahjong" class="nav-item" @click="closeSidebar">
          <span class="nav-icon">🀄</span> 约麻将
        </router-link>
        <router-link to="/billiard" class="nav-item" @click="closeSidebar">
          <span class="nav-icon">🎱</span> 约台球
        </router-link>
        <router-link to="/boardgame" class="nav-item" @click="closeSidebar">
          <span class="nav-icon">🎲</span> 约桌游
        </router-link>
        <router-link to="/karaoke" class="nav-item" @click="closeSidebar">
          <span class="nav-icon">🎤</span> 约唱歌
        </router-link>
        <router-link to="/movie" class="nav-item" @click="closeSidebar">
          <span class="nav-icon">🎬</span> 约电影
        </router-link>
      </nav>

      <div class="sidebar-footer">
        <span class="nav-item logout" @click="handleLogout">🚪 退出登录</span>
      </div>
    </aside>

    <main class="main-content" :class="{ 'no-sidebar': !showSidebar }">
      <router-view v-slot="{ Component }">
        <transition name="fade-slide" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>

    <Toast />
    <PetWidget v-if="showPet" />
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Toast from './components/Toast.vue'
import PetWidget from './components/PetWidget.vue'
import { removeToken } from './utils/auth'
import { thumbUrl } from './utils/image'

const route = useRoute()
const router = useRouter()
const sidebarOpen = ref(false)
const user = ref({})

const showSidebar = computed(() => {
  return !['/login', '/register'].includes(route.path)
})

const showPet = computed(() => {
  return !['/login', '/register'].includes(route.path)
})

const userLevel = computed(() => {
  const exp = user.value.exp || 0
  return Math.min(Math.floor(exp / 100) + 1, 6)
})

function loadUser() {
  const stored = localStorage.getItem('user')
  if (stored) {
    try { user.value = JSON.parse(stored) } catch (e) { /* ignore */ }
  } else {
    user.value = {}
  }
}

loadUser()

watch(() => route.path, () => {
  closeSidebar()
  if (showSidebar.value) loadUser()
})

function onNavigationKey(event) {
  if (event.key === 'Escape' && sidebarOpen.value) {
    closeSidebar()
    document.querySelector('.sidebar-toggle')?.focus()
  }
}
onMounted(() => window.addEventListener('keydown', onNavigationKey))
onUnmounted(() => window.removeEventListener('keydown', onNavigationKey))

function toggleSidebar() {
  sidebarOpen.value = !sidebarOpen.value
}

function closeSidebar() {
  sidebarOpen.value = false
}

function handleLogout() {
  removeToken()
  router.push('/login')
}
</script>

<style scoped>
.sidebar-user-level {
  display: inline-block;
  margin-top: 4px;
  padding: 1px 8px;
  background: var(--primary);
  color: #fff;
  border-radius: 10px;
  font-size: 11px;
  font-weight: 600;
}
</style>
