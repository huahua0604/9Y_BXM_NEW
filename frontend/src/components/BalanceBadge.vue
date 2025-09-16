<template>
  <div class="badge" v-if="loaded">
    <span class="dot"></span>
    当前余额：<b>{{ formatMoney(balance.available) }}</b> 元
    <small class="ts">（更新：{{ fmtTime(balance.updatedAt) }}）</small>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import dayjs from 'dayjs'
import { getBalance } from '@/api/balance'

const balance = ref({ available: 0, updatedAt: null })
const loaded = ref(false)

function formatMoney(n){
  const x = Number(n || 0)
  return x.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}
function fmtTime(t){
  return t ? dayjs(t).format('YYYY-MM-DD HH:mm') : '--'
}

async function load(){
  try{
    balance.value = await getBalance()
  }catch(e){
    // 不是管理员或接口不可达就静默
    return
  }finally{
    loaded.value = true
  }
}

onMounted(load)
</script>

<style scoped>
.badge{
  display:inline-flex; align-items:center; gap:8px;
  background:#f1f5f9; color:#0f172a; border:1px solid #e2e8f0;
  padding:6px 10px; border-radius:999px; font-size:13px;
}
.dot{ width:8px; height:8px; border-radius:50%; background:#10b981; display:inline-block; }
.ts{ color:#64748b; margin-left:4px; }
</style>
