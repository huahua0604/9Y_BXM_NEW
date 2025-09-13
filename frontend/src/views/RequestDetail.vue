<template>
  <div style="display:grid; gap:16px;">
    <div class="card" v-if="detail">
      <div class="header">
        <h3>报销详情 #{{ detail.id }}</h3>
        <div>
          <span class="badge">{{ statusBadge(detail.status) }}</span>
        </div>
      </div>
      <div style="display:grid;grid-template-columns:1fr 1fr;gap:10px;">
        <div><strong>标题：</strong>{{ detail.title }}</div>
        <div><strong>金额：</strong>¥ {{ Number(detail.amount).toFixed(2) }}</div>
        <div><strong>类别：</strong>{{ detail.category }}</div>
        <div><strong>发生日期：</strong>{{ detail.occurDate }}</div>
        <div style="grid-column:1/3;"><strong>备注：</strong>{{ detail.remark || '—' }}</div>
      </div>
      <div style="margin-top:12px;display:flex;gap:10px;">
        <button v-if="['DRAFT','RETURNED'].includes(detail.status)" class="btn" @click="submit">提交审核</button>
      </div>
    </div>

    <PdfUploader :request-id="id" @uploaded="load" />

    <div class="card">
      <div class="header"><h3>附件</h3></div>
      <ul>
        <li v-for="a in (detail?.attachments||[])" :key="a.id" style="margin:6px 0;display:flex;align-items:center;gap:10px;">
          <span>📄 {{ a.filename }}（{{ (a.size/1024).toFixed(0) }} KB）</span>
          <button class="btn outline" @click="download(a.id, a.filename)">下载/预览</button>
        </li>
        <li v-if="!(detail?.attachments||[]).length" style="color:#888;">暂无附件</li>
      </ul>
    </div>

    <div class="card">
      <div class="header"><h3>流转历史</h3></div>
      <table class="table">
        <thead><tr><th>时间</th><th>操作</th><th>备注</th></tr></thead>
        <tbody>
          <tr v-for="h in history" :key="h.id">
            <td>{{ new Date(h.occurredAt).toLocaleString('zh-CN') }}</td>
            <td>{{ actionMap[h.action] || h.action }}</td>
            <td>{{ h.note || '—' }}</td>
          </tr>
          <tr v-if="!history.length"><td colspan="3" style="text-align:center;color:#888;">暂无历史</td></tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import http from '../api/http'
import PdfUploader from '../components/PdfUploader.vue'
import { statusBadge } from '../utils/format'

const route = useRoute()
const id = computed(() => Number(route.params.id))
const detail = ref(null)
const history = ref([])

const actionMap = {
  CREATE: '创建草稿', UPDATE: '更新', SUBMIT: '提交审核', RETURN: '退回修改', APPROVE: '审核通过', REJECT: '审核驳回', UPLOAD_ATTACHMENT: '上传附件'
}

async function load(){
  const { data } = await http.get(`/api/requests/${id.value}`)
  detail.value = data
  const h = await http.get(`/api/requests/${id.value}/history`)
  history.value = h.data
}

async function submit(){
  if(!confirm('确认提交该报销单至审核？')) return
  await http.post(`/api/requests/${id.value}/submit`)
  await load()
}

async function download(attId, filename){
  try{
    const resp = await http.get(`/api/attachments/${attId}/download`, { responseType: 'blob' })
    const blob = new Blob([resp.data], { type: 'application/pdf' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = filename || 'attachment.pdf'
    document.body.appendChild(a)
    a.click()
    a.remove()
    URL.revokeObjectURL(url)
  }catch(e){
    alert('下载失败')
  }
}

onMounted(load)
</script>