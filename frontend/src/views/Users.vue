<template>
  <div style="max-width:1100px;margin:16px auto;">
    <h2>用户管理</h2>
    <div class="card">
      <h3>新增用户</h3>
      <div class="grid">
        <div class="field">
          <label>工号</label>
          <input v-model.trim="form.employeeId" placeholder="如：100001" />
        </div>
        <div class="field">
          <label>姓名</label>
          <input v-model.trim="form.name" placeholder="张三" />
        </div>
        <div class="field">
          <label>科室</label>
          <input v-model.trim="form.department" placeholder="信息科" />
        </div>
        <div class="field">
          <label>密码(可不填，默认 两遍工号)</label>
          <input v-model.trim="form.password" type="password" placeholder="8-64位" />
        </div>
        <div class="field">
          <label>角色</label>
          <div class="roles">
            <label><input type="checkbox" value="USER" v-model="form.roles"> 用户</label>
            <label><input type="checkbox" value="REVIEWER" v-model="form.roles"> 审核员</label>
            <label><input type="checkbox" value="ADMIN" v-model="form.roles"> 管理员</label>
          </div>
        </div>
        <div class="field">
          <label>首登需改密</label>
          <select v-model="form.mustChangePassword">
            <option :value="true">是</option>
            <option :value="false">否</option>
          </select>
        </div>
      </div>
      <div class="actions">
        <button class="btn primary" @click="onCreate">创建用户</button>
      </div>
      <p v-if="tempPwd" class="hint">已创建，临时密码：<b>{{ tempPwd }}</b></p>
    </div>

    <div class="card" style="margin-top:16px;">
      <h3>全部用户</h3>
      <table class="table">
        <thead>
          <tr>
            <th>ID</th><th>工号</th><th>姓名</th><th>科室</th><th>角色</th><th>首登改密</th><th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="u in users" :key="u.id">
            <td>{{ u.id }}</td>
            <td>{{ u.employeeId }}</td>
            <td>{{ u.name }}</td>
            <td>{{ u.department }}</td>
            <td>{{ u.roles.map(roleLabel).join(', ') }}</td>
            <td>{{ u.mustChangePassword ? '是' : '否' }}</td>
            <td>
              <button class="btn" @click="onReset(u)">重置为默认密码</button>
            </td>
          </tr>
          <tr v-if="!users.length">
            <td colspan="7" class="empty">暂无用户</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminCreateUser, adminListUsers, adminResetPassword } from '@/api/admin'

const form = ref({
  employeeId: '',
  name: '',
  department: '',
  password: '',
  roles: ['USER'],         // 默认 USER
  mustChangePassword: true // 默认首登改密
})

const users = ref([])
const tempPwd = ref('')

async function load(){
  users.value = await adminListUsers()
}

function roleLabel(role) {
  switch (role) {
    case 'ADMIN': return '管理员'
    case 'REVIEWER': return '审核员'
    case 'USER': return '普通用户'
    default: return role
  }
}

async function onCreate(){
  if(!form.value.employeeId?.trim() || !form.value.name?.trim()){
    return alert('请填写工号和姓名')
  }

  // 构造 payload：空的就不传
  const payload = {
    employeeId: form.value.employeeId.trim(),
    name: form.value.name.trim(),
    department: form.value.department?.trim() || undefined,
    roles: (form.value.roles && form.value.roles.length) ? form.value.roles : undefined,
    mustChangePassword: form.value.mustChangePassword // 保证是 boolean，不是字符串
  }
  // 密码为空就不要传，避免触发 @Size 校验
  if (form.value.password && form.value.password.length > 0) {
    if (form.value.password.length < 8 || form.value.password.length > 64) {
      return alert('密码长度需在 8-64 位之间')
    }
    payload.password = form.value.password
  }

  try{
    const res = await adminCreateUser(payload)
    const tmp = res?.tempPassword
    alert(tmp ? `创建成功，临时密码：${tmp}` : '创建成功')
    // 重置表单
    form.value = { employeeId:'', name:'', department:'', password:'', roles:['USER'], mustChangePassword:true }
    await load()
  }catch(e){
    // 把后端的字段错误展示出来
    const errors = e?.response?.data?.errors
    if (errors && typeof errors === 'object') {
      alert('参数校验失败：\n' + Object.entries(errors).map(([k,v])=>`${k}: ${v}`).join('\n'))
    } else {
      alert(e?.response?.data?.message || '创建失败')
    }
  }
}


async function onReset(u){
  if(!confirm(`确定把 ${u.name}（${u.employeeId}） 重置为默认密码？`)) return
  try{
    const res = await adminResetPassword(u.id)
    alert(`已重置，临时密码：${u.employeeId}${u.employeeId}`)
  }catch(e){
    alert(e?.response?.data?.message || '重置失败')
  }
}

onMounted(load)
</script>

<style scoped>
.card{ background:#fff; border:1px solid #e5e7eb; border-radius:10px; padding:16px; }
.grid{ display:grid; grid-template-columns: 1fr 1fr 1fr; gap: 12px 16px; }
.field{ display:flex; flex-direction:column; gap:6px; }
.roles{ display:flex; gap:12px; }
.actions{ margin-top:12px; }
.btn{ padding:6px 10px; border:1px solid #d1d5db; border-radius:8px; background:#2563eb; cursor:pointer; }
.btn.primary{ background:#2563eb; border-color:#2563eb; color:#fff; }
.table{ width:100%; border-collapse:collapse; margin-top:10px; font-size:14px; }
.table th,.table td{ border-bottom:1px solid #e5e7eb; padding:8px 10px; }
.empty{ text-align:center; color:#94a3b8; padding:16px 0; }
.hint{ margin-top:8px; color:#065f46; }
</style>
