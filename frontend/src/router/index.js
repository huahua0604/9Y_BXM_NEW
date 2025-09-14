import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../store/auth'

const routes = [
  { path: '/', redirect: '/my' },
  { path: '/login', name: 'login', component: () => import('../views/Login.vue'), meta: { public: true } },
  { path: '/change-password', name: 'changePwd', component: () => import('../views/ChangePassword.vue') },
  { path: '/my', name: 'my', component: () => import('../views/MyRequests.vue') },
  { path: '/request/:id', name: 'detail', component: () => import('../views/RequestDetail.vue') },
  { path: '/review', name: 'review', component: () => import('../views/ReviewQueue.vue'), meta: { roles: ['REVIEWER','ADMIN'] } },
  { path: '/review/my-approvals', name: 'MyApprovals', component: () => import('@/views/MyApprovals.vue'), meta: { requiresAuth: true, roles: ['REVIEWER', 'ADMIN'], title: '我的审批记录' }},
  { path: '/request/new', name: 'new-request', component: () => import('../views/NewRequest.vue') },
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach((to) => {
  const auth = useAuthStore()
  if (to.meta.public) return true
  if (!auth.isAuthed) return { name: 'login', query: { redirect: to.fullPath } }
  if (auth.mustChangePassword && to.name !== 'changePwd') {
    return { name: 'changePwd' }
  }
  if (to.meta.roles) {
    const ok = to.meta.roles.some(r => auth.roles.includes(r))
    if (!ok) return { path: '/my' }
  }
  return true
})

export default router