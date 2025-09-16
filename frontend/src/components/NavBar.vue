<template>
  <header style="background:#0f172a; color:#fff;">
    <div style="max-width:980px;margin:0 auto;padding:10px 12px;display:flex;align-items:center;gap:16px;">
      <strong style="letter-spacing:0.5px;">医院科内报销平台</strong>
      <router-link to="/my" style="color:#fff;">我的报销</router-link>
      <router-link to="/request/new" style="color:#fff;">新建报销</router-link>
      <router-link v-if="hasRole('REVIEWER') || hasRole('ADMIN')" to="/review" style="color:#fff;">审核队列</router-link>
      <router-link v-if="hasRole('REVIEWER') || hasRole('ADMIN')" to="/review/my-approvals" style="color:#fff;">我的审批记录</router-link>
      <router-link v-if="hasRole('ADMIN')" to="/admin/users" style="color:#fff;">用户管理</router-link>
      <router-link v-if="hasRole('ADMIN')" to="/admin/balance" style="color:#fff;">资金管理</router-link>
      <div style="margin-left:auto;display:flex;align-items:center;gap:10px;">
        <span v-if="name">👋 {{ name }}</span>
        <button v-if="isAuthed" class="btn outline" @click="onLogout">退出</button>
        <router-link v-else class="btn outline" to="/login">登录</router-link>
      </div>
    </div>
  </header>
</template>

<script setup>
import { storeToRefs } from 'pinia'
import { useAuthStore } from '../store/auth'
import { useRouter } from 'vue-router'

const router = useRouter()
const auth = useAuthStore()
const { name, isAuthed } = storeToRefs(auth)
const hasRole = auth.hasRole

function onLogout(){
  auth.logout()
  router.push('/login')
}
</script>