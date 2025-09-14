<template>
  <div style="display:grid; gap:16px;">
    <!-- <RequestForm @created="goDetail" /> -->

    <div class="card">
      <div class="header">
        <h3>我的报销单</h3>
        <button class="btn outline" @click="load()">刷新</button>
      </div>
      <table class="table">
        <thead>
          <tr><th>ID</th><th>标题</th><th>金额</th><th>状态</th><th>创建时间</th><th>操作</th></tr>
        </thead>
        <tbody>
          <tr v-for="it in list" :key="it.id">
            <td>{{ it.id }}</td>
            <td>{{ it.title }}</td>
            <td>¥ {{ Number(it.amount).toFixed(2) }}</td>
            <td><span class="badge">{{ statusBadge(it.status) }}</span></td>
            <td>{{ new Date(it.createdAt).toLocaleString('zh-CN') }}</td>
            <td>
              <router-link :to="`/request/${it.id}`">详情</router-link>
            </td>
          </tr>
          <tr v-if="!list.length"><td colspan="6" style="text-align:center;color:#888;">暂无数据</td></tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import http from '../api/http'
import RequestForm from '../components/RequestForm.vue'
import { statusBadge } from '../utils/format'
import { useRouter } from 'vue-router'

const router = useRouter()
const list = ref([])

function goDetail(id){ router.push(`/request/${id}`) }

async function load(){
  const { data } = await http.get('/api/requests')
  list.value = data
}

onMounted(load)
</script>