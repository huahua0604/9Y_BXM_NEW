<template>
  <div class="card">
    <div class="header">
      <h3>待审列表</h3>
      <button class="btn outline" @click="load">刷新</button>
    </div>
    <table class="table">
      <thead>
        <tr><th>ID</th><th>标题</th><th>金额</th><th>创建时间</th><th>操作</th></tr>
      </thead>
      <tbody>
        <tr v-for="it in list" :key="it.id">
          <td>{{ it.id }}</td>
          <td>{{ it.title }}</td>
          <td>¥ {{ Number(it.amount).toFixed(2) }}</td>
          <td>{{ new Date(it.createdAt).toLocaleString('zh-CN') }}</td>
          <td>
            <router-link :to="`/request/${it.id}`">查看</router-link>
            <button class="btn" style="margin-left:8px;" @click="approve(it.id)">通过</button>
            <button class="btn outline" style="margin-left:6px;" @click="reject(it.id)">驳回</button>
            <button class="btn outline" style="margin-left:6px;" @click="sendBack(it.id)">退回修改</button>
          </td>
        </tr>
        <tr v-if="!list.length"><td colspan="5" style="text-align:center;color:#888;">暂无待审数据</td></tr>
      </tbody>
    </table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import http from '../api/http'

const list = ref([])

async function load(){
  const { data } = await http.get('/api/review/queue')
  list.value = data
}

async function approve(id){
  const note = prompt('通过备注（可空）：') || ''
  await http.post(`/api/review/${id}/approve`, { note })
  await load()
}

async function reject(id){
  const note = prompt('驳回原因（必填）：')
  if(!note) return alert('必须填写驳回原因')
  await http.post(`/api/review/${id}/reject`, { note })
  await load()
}

async function sendBack(id){
  const note = prompt('退回原因（必填）：')
  if(!note) return alert('必须填写退回原因')
  await http.post(`/api/review/${id}/return`, { note })
  await load()
}

onMounted(load)
</script>