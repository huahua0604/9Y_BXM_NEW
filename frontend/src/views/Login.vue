<template>
  <div class="card" style="max-width:420px;margin:60px auto;">
    <h2 style="margin-top:0;">登录</h2>
    <form @submit.prevent="onLogin" style="display:grid;gap:10px;">
      <div>
        <label>工号</label>
        <input class="input" v-model.trim="employeeId" required autofocus />
      </div>
      <div>
        <label>密码</label>
        <input class="input" v-model.trim="password" type="password" required />
      </div>
      <button class="btn" :disabled="loading">登录</button>
    </form>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import http from '../api/http'
import { useAuthStore } from '../store/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const employeeId = ref('')
const password = ref('')
const loading = ref(false)

async function onLogin(){
  loading.value = true
  try{
    const { data } = await http.post('/api/auth/login', { employeeId: employeeId.value, password: password.value })
    auth.setAuth({ token: data.token, name: data.name, roles: data.roles || [], mustChangePassword: data.mustChangePassword })
    const redirect = route.query.redirect || (auth.mustChangePassword ? '/change-password' : '/my')
    router.push(String(redirect))
  }catch(e){
    alert(e?.response?.data?.message || '登录失败')
  }finally{
    loading.value = false
  }
}
</script>