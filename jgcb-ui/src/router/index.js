import { createRouter, createWebHistory } from 'vue-router'
import { isAuthenticated } from '../utils/auth'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue')
  },
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/Home.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/members',
    name: 'Members',
    component: () => import('../views/Members.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/travel',
    name: 'Travel',
    component: () => import('../views/Travel.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('../views/Profile.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/mahjong',
    name: 'Mahjong',
    component: () => import('../views/Mahjong.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/memoirs',
    name: 'Memoirs',
    component: () => import('../views/Memoirs.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/billiard',
    name: 'Billiard',
    component: () => import('../views/Billiard.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/boardgame',
    name: 'BoardGame',
    component: () => import('../views/BoardGame.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/checkin',
    name: 'CheckIn',
    component: () => import('../views/CheckIn.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/karaoke',
    name: 'Karaoke',
    component: () => import('../views/Karaoke.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/movie',
    name: 'Movie',
    component: () => import('../views/Movie.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/pet',
    name: 'Pet',
    component: () => import('../views/Pet.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/room',
    name: 'Room',
    component: () => import('../views/Room.vue'),
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  scrollBehavior(to, from, savedPosition) {
    return savedPosition || { top: 0 }
  },
  routes
})

router.beforeEach((to, from, next) => {
  const authenticated = isAuthenticated()
  if (to.meta.requiresAuth && !authenticated) {
    next('/login')
  } else if ((to.path === '/login' || to.path === '/register') && authenticated) {
    next('/')
  } else {
    next()
  }
})

export default router
