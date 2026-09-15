<template>
  <div class="page-content">
    <div class="welcome-card">
      <h1>👋 欢迎回来，{{ user.nickname || user.username }}</h1>
      <p>蕨根糍粑 — 记录我们的每一次相聚</p>
    </div>

    <div class="section-title">✨ 快速入口</div>
    <div class="dashboard-grid">
      <router-link to="/members" class="dash-card">
        <span class="dash-icon">👥</span>
        <span class="dash-label">成员列表</span>
        <span class="dash-sub">查看所有成员</span>
      </router-link>
      <router-link to="/travel" class="dash-card">
        <span class="dash-icon">✈️</span>
        <span class="dash-label">旅游</span>
        <span class="dash-sub">旅行记录与计划</span>
      </router-link>
      <router-link to="/memoirs" class="dash-card">
        <span class="dash-icon">📖</span>
        <span class="dash-label">回忆录</span>
        <span class="dash-sub">珍藏美好回忆</span>
      </router-link>
      <router-link to="/mahjong" class="dash-card">
        <span class="dash-icon">🀄</span>
        <span class="dash-label">约麻将</span>
        <span class="dash-sub">三缺一速来</span>
      </router-link>
      <router-link to="/billiard" class="dash-card">
        <span class="dash-icon">🎱</span>
        <span class="dash-label">约台球</span>
        <span class="dash-sub">一杆清台</span>
      </router-link>
      <router-link to="/boardgame" class="dash-card">
        <span class="dash-icon">🎲</span>
        <span class="dash-label">约桌游</span>
        <span class="dash-sub">狼人杀/剧本杀</span>
      </router-link>
      <router-link to="/karaoke" class="dash-card">
        <span class="dash-icon">🎤</span>
        <span class="dash-label">约唱歌</span>
        <span class="dash-sub">K歌之王</span>
      </router-link>
      <router-link to="/movie" class="dash-card">
        <span class="dash-icon">🎬</span>
        <span class="dash-label">约电影</span>
        <span class="dash-sub">一起看电影</span>
      </router-link>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const user = ref({})

onMounted(() => {
  const stored = localStorage.getItem('user')
  if (stored) {
    try { user.value = JSON.parse(stored) } catch (e) { /* ignore */ }
  }
})
</script>
