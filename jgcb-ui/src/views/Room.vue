<template>
  <div class="page-content room-page">
    <header class="room-header">
      <div>
        <span class="room-eyebrow">2.5D 社区空间</span>
        <h1>{{ room?.name || '糍粑小屋' }}</h1>
        <p>点击地面移动，点击发光的家具进行互动。</p>
      </div>
      <div class="room-presence">
        <span class="room-status-dot" :class="socketStatus"></span>
        <span>{{ statusLabel }}</span>
        <strong>{{ onlineCount }} 人在线</strong>
      </div>
    </header>

    <div v-if="loadError" class="room-error" role="alert">
      <span>小屋暂时没有打开：{{ loadError }}</span>
      <button type="button" @click="loadRoom">重新进入</button>
    </div>

    <div v-else class="room-layout">
      <section class="room-stage-card" aria-label="多人小屋游戏区域">
        <div ref="canvasHost" class="room-canvas-host"></div>
        <div v-if="loading" class="room-loading" role="status">
          <span class="room-loading-icon">🛖</span>
          <strong>正在打开小屋…</strong>
          <small>加载地图、家具和在线成员</small>
        </div>
      </section>

      <aside class="room-controls">
        <section class="room-control-card">
          <h2>游戏控制</h2>
          <button type="button" class="room-primary-button" :disabled="loading" @click="returnToEntrance">回到入口</button>
          <button v-if="socketStatus !== 'connected'" type="button" class="room-secondary-button" :disabled="loading" @click="reconnect">重新连接</button>
        </section>

        <section class="room-control-card">
          <h2>可以互动</h2>
          <button type="button" class="room-object-row" @click="focusObject('mahjong_table')"><span>🀄</span><div><strong>麻将机</strong><small>走到桌边看看</small></div></button>
          <button type="button" class="room-object-row" @click="focusObject('computer')"><span>🖥️</span><div><strong>电脑</strong><small>打开电脑弹窗</small></div></button>
          <button type="button" class="room-object-row" @click="focusObject('sofa')"><span>🛋️</span><div><strong>沙发</strong><small>移动到沙发旁</small></div></button>
          <button type="button" class="room-object-row" @click="focusObject('plant')"><span>🪴</span><div><strong>绿植</strong><small>走近并浇水</small></div></button>
        </section>

        <section class="room-tip">
          <strong>第一阶段</strong>
          <p>当前支持直线移动、家具互动和多人位置同步，暂不包含复杂寻路和麻将玩法。</p>
        </section>
      </aside>
    </div>

    <teleport to="body">
      <div v-if="dialog.open" class="room-dialog-overlay" @click.self="closeDialog">
        <div class="room-dialog" role="dialog" aria-modal="true" :aria-labelledby="'room-dialog-title'">
          <span class="room-dialog-icon">{{ dialog.icon }}</span>
          <h2 id="room-dialog-title">{{ dialog.title }}</h2>
          <p>{{ dialog.message }}</p>
          <button type="button" @click="closeDialog">知道了</button>
        </div>
      </div>
    </teleport>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { getRoom, getRoomPlayers, getUserInfo, saveRoomObjectState, saveRoomPlayerState } from '../api'
import { useToast } from '../composables/useToast'
import RoomScene from '../game/room/RoomScene'
import RoomSocket from '../game/room/RoomSocket'
import { SPAWN_POINT } from '../game/room/roomConfig'

const ROOM_ID = 1
const { toast } = useToast()
const canvasHost = ref(null)
const room = ref(null)
const loading = ref(true)
const loadError = ref('')
const socketStatus = ref('connecting')
const onlineCount = ref(0)
const dialog = reactive({ open: false, icon: '', title: '', message: '' })
let scene
let roomSocket

const storedUser = (() => {
  try { return JSON.parse(localStorage.getItem('user') || '{}') || {} } catch { return {} }
})()
const currentUserId = Number(storedUser.userId || storedUser.id)
const statusLabel = computed(() => ({
  connected: '实时连接正常', connecting: '正在连接', reconnecting: '正在重连', error: '连接异常', closed: '已离开'
}[socketStatus.value] || '正在连接'))

async function loadRoom() {
  loading.value = true
  loadError.value = ''
  roomSocket?.close()
  scene?.destroy()
  scene = null
  try {
    const [roomRes, playersRes, userRes] = await Promise.all([
      getRoom(ROOM_ID),
      getRoomPlayers(ROOM_ID),
      getUserInfo()
    ])
    if (roomRes.code !== 200 || !roomRes.data) throw new Error(roomRes.message || '房间不存在')
    room.value = roomRes.data
    const profile = userRes.data || storedUser
    const players = playersRes.data || []
    if (!players.some(player => Number(player.userId) === currentUserId)) {
      players.push({
        id: currentUserId,
        userId: currentUserId,
        nickname: profile.nickname || profile.username || '我',
        avatar: profile.avatar || '',
        ...SPAWN_POINT,
        direction: 'right',
        action: 'idle',
        online: true
      })
    }

    scene = new RoomScene(canvasHost.value, {
      currentUserId,
      onMove: syncMove,
      onBlocked: () => toast('这里被家具挡住了', 'info'),
      onObjectClick: interactWithObject,
      onPlayerCount: count => { onlineCount.value = count }
    })
    await scene.init(room.value)
    scene.setObjects(room.value.objects || [])
    scene.setPlayers(players)

    roomSocket = new RoomSocket(ROOM_ID, {
      onStatus: status => { socketStatus.value = status },
      onMessage: handleSocketMessage
    })
    roomSocket.connect()
  } catch (error) {
    loadError.value = error?.response?.data?.message || error.message || '加载失败'
    socketStatus.value = 'error'
  } finally {
    loading.value = false
  }
}

function handleSocketMessage(message) {
  if (!scene) return
  if (message.type === 'snapshot') {
    scene.setPlayers(message.players || [])
  } else if (message.type === 'player_joined' || message.type === 'player_moved') {
    scene.upsertPlayer(message.player, message.type === 'player_joined')
  } else if (message.type === 'player_left') {
    scene.removePlayer(message.userId)
  }
}

function syncMove(player) {
  if (!roomSocket?.sendMove(player)) {
    saveRoomPlayerState(ROOM_ID, player).catch(() => {
      socketStatus.value = 'error'
    })
  }
}

function returnToEntrance() {
  scene?.resetCurrentPlayer()
}

function reconnect() {
  roomSocket?.close()
  roomSocket?.connect()
}

function focusObject(type) {
  const item = room.value?.objects?.find(object => object.type === type)
  if (item) interactWithObject(item)
}

async function interactWithObject(object) {
  if (!scene) return
  scene.moveToObject(object.type)
  if (object.type === 'computer') {
    openDialog('🖥️', '小屋电脑', '电脑已经打开。第一阶段先用弹窗验证家具交互，后续可以接入小游戏或社区功能。')
  } else if (object.type === 'sofa') {
    toast('正在走向沙发', 'info')
  } else if (object.type === 'mahjong_table') {
    openDialog('🀄', '自动麻将机', '麻将机已通电。下一阶段可以把现有“约麻将”牌局接入这里。')
  } else if (object.type === 'plant') {
    try {
      const state = JSON.stringify({ wateredAt: new Date().toISOString(), wateredBy: currentUserId })
      const response = await saveRoomObjectState(ROOM_ID, object.id, { state })
      if (response.code !== 200) throw new Error(response.message || '保存失败')
      toast('绿植浇水成功 🌱', 'success')
    } catch (error) {
      toast(error?.response?.data?.message || error.message || '浇水失败', 'error')
    }
  }
}

function openDialog(icon, title, message) {
  Object.assign(dialog, { open: true, icon, title, message })
}
function closeDialog() { dialog.open = false }

onMounted(loadRoom)
onBeforeUnmount(() => {
  roomSocket?.close()
  scene?.destroy()
})
</script>

<style scoped>
.room-page { max-width: 1500px; }
.room-header { display:flex; align-items:flex-end; justify-content:space-between; gap:24px; margin-bottom:20px; }
.room-eyebrow { color:var(--primary); font-size:12px; font-weight:800; letter-spacing:.12em; text-transform:uppercase; }
.room-header h1 { margin:3px 0 4px; font-size:clamp(24px,3vw,34px); line-height:1.2; }
.room-header p { color:var(--text-secondary); font-size:14px; }
.room-presence { display:flex; align-items:center; gap:8px; padding:10px 14px; border:1px solid var(--border); border-radius:14px; background:var(--bg-card); color:var(--text-secondary); font-size:13px; white-space:nowrap; }
.room-presence strong { color:var(--text-primary); padding-left:8px; border-left:1px solid var(--border); }
.room-status-dot { width:9px; height:9px; border-radius:50%; background:#f59e0b; box-shadow:0 0 0 4px rgba(245,158,11,.14); }
.room-status-dot.connected { background:#10b981; box-shadow:0 0 0 4px rgba(16,185,129,.14); }
.room-status-dot.error, .room-status-dot.closed { background:#ef4444; box-shadow:0 0 0 4px rgba(239,68,68,.12); }
.room-layout { display:grid; grid-template-columns:minmax(0,1fr) 260px; gap:18px; align-items:start; }
.room-stage-card { position:relative; overflow:hidden; border:1px solid #d9cab9; border-radius:20px; background:#2f312f; box-shadow:0 18px 50px rgba(56,38,22,.14); aspect-ratio:3/2; min-height:520px; }
.room-canvas-host { width:100%; height:100%; min-height:520px; }
.room-canvas-host :deep(.room-canvas) { display:block; width:100%; height:100%; touch-action:none; }
.room-loading { position:absolute; inset:0; display:flex; flex-direction:column; align-items:center; justify-content:center; gap:8px; background:linear-gradient(135deg,#f6eadb,#e7d4bc); color:#5b3b22; }
.room-loading-icon { font-size:52px; animation:room-float 1.6s ease-in-out infinite; }
.room-loading small { opacity:.7; }
.room-controls { display:flex; flex-direction:column; gap:14px; }
.room-control-card, .room-tip { border:1px solid var(--border); border-radius:16px; background:var(--bg-card); padding:16px; box-shadow:var(--shadow-xs); }
.room-control-card h2 { font-size:15px; margin-bottom:12px; }
.room-primary-button, .room-secondary-button { width:100%; min-height:42px; border-radius:10px; cursor:pointer; font-weight:700; }
.room-primary-button { border:0; background:var(--primary); color:#fff; }
.room-secondary-button { margin-top:8px; border:1px solid var(--border); background:#fff; color:var(--text-primary); }
.room-object-row { width:100%; display:flex; align-items:center; gap:10px; padding:10px 4px; border:0; border-bottom:1px solid var(--border-light); background:transparent; text-align:left; cursor:pointer; color:var(--text-primary); }
.room-object-row:last-child { border-bottom:0; }
.room-object-row > span { display:grid; place-items:center; width:38px; height:38px; border-radius:10px; background:#faf3e8; font-size:20px; }
.room-object-row div { display:flex; flex-direction:column; }
.room-object-row strong { font-size:13px; }
.room-object-row small { color:var(--text-muted); font-size:11px; }
.room-tip { background:#fff9ed; border-color:#f1dfbd; }
.room-tip strong { color:#9a6819; font-size:13px; }
.room-tip p { margin-top:5px; color:#7a674b; font-size:12px; line-height:1.7; }
.room-error { display:flex; justify-content:space-between; align-items:center; gap:16px; padding:20px; border:1px solid #fecaca; border-radius:16px; background:#fff1f2; color:#991b1b; }
.room-error button { padding:9px 15px; border:0; border-radius:9px; background:#be123c; color:#fff; cursor:pointer; }
.room-dialog-overlay { position:fixed; inset:0; z-index:1000; display:grid; place-items:center; padding:20px; background:rgba(37,25,14,.45); backdrop-filter:blur(4px); }
.room-dialog { width:min(390px,100%); padding:28px; border-radius:20px; background:#fff; text-align:center; box-shadow:0 24px 80px rgba(0,0,0,.22); }
.room-dialog-icon { font-size:46px; }
.room-dialog h2 { margin:8px 0; font-size:20px; }
.room-dialog p { color:var(--text-secondary); line-height:1.75; font-size:14px; }
.room-dialog button { margin-top:20px; min-width:120px; padding:10px 18px; border:0; border-radius:10px; background:var(--primary); color:#fff; cursor:pointer; }
@keyframes room-float { 50% { transform:translateY(-8px); } }
@media(max-width:1100px) { .room-layout { grid-template-columns:1fr; } .room-controls { display:grid; grid-template-columns:repeat(2,minmax(0,1fr)); } .room-tip { grid-column:1/-1; } }
@media(max-width:768px) { .room-header { align-items:flex-start; flex-direction:column; } .room-presence { width:100%; } .room-stage-card, .room-canvas-host { min-height:430px; } .room-controls { grid-template-columns:1fr; } .room-tip { grid-column:auto; } }
</style>