<template>
  <div class="card" style="max-width:520px;margin:40px auto;">
    <h2 style="margin-top:0;">首次登录需修改密码</h2>
    <form @submit.prevent="onSubmit" style="display:grid;gap:10px;">
      <div>
        <label>原密码</label>
        <input class="input" type="password" v-model.trim="oldPassword" required />
      </div>
      <div>
        <label>新密码</label>
        <input class="input" type="password" v-model.trim="newPassword" minlength="8" required />
      </div>
      <button class="btn" :disabled="loading">保存</button>
    </form>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import http from '../api/http'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../store/auth'

const router = useRouter()
const auth = useAuthStore()
const oldPassword = ref('')
const newPassword = ref('')
const loading = ref(false)

async function onSubmit(){
  loading.value = true
  try{
    await http.post('/api/auth/change-password', { oldPassword: oldPassword.value, newPassword: newPassword.value })
    auth.setAuth({ token: auth.token, name: auth.name, roles: auth.roles, mustChangePassword: false })
    router.push('/my')
  }catch(e){
    alert(e?.response?.data?.message || '修改失败')
  }finally{
    loading.value = false
  }
}
</script>