<template>
  <div style="max-width:1100px;margin:16px auto;">
    <h2>资金管理</h2>

    <!-- 顶部统计卡 -->
    <div class="card stat">
      <div class="stat-item">
        <div class="stat-title">当前可用余额</div>
        <div class="stat-value">{{ formatMoney(balance.available) }} 元</div>
        <div class="stat-sub">更新于：{{ fmtTime(balance.updatedAt) }}</div>
      </div>
      <div class="stat-actions">
        <div class="field">
          <label>加款金额（元）</label>
          <input v-model="amount" type="number" min="0" step="0.01" placeholder="如：10000" />
        </div>
        <div class="field">
          <label>发生时间</label>
          <div style="display:flex; gap:8px; align-items:center;">
            <input
              v-model="occurredAt"
              type="datetime-local"
              placeholder="选择时间"
            />
            <button class="btn" @click="setNow" type="button">现在</button>
            <button class="btn" @click="clearTime" type="button">清空</button>
          </div>
          <small class="hint">默认则以当前时间记录</small>
        </div>
        <div class="field">
          <label>备注（可选）</label>
          <input v-model.trim="note" placeholder="备注说明" />
        </div>
        <button class="btn primary" :disabled="busy" @click="onAdd">确认加款</button>
      </div>
    </div>

    <!-- 交易列表 -->
    <div class="card">
      <h3>最近交易</h3>
      <table class="table">
        <thead>
          <tr>
            <th style="width:160px;">时间</th>
            <th style="width:90px;">类型</th>
            <th style="width:140px;">金额</th>
            <th style="width:140px;">操作人</th>
            <th style="width:120px;">关联单据</th>
            <th>备注</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="t in tx" :key="t.id">
            <td>{{ fmtTime(t.occurredAt) }}</td>
            <td>
              <span :class="badgeClass(t.type)">
                {{ t.type === 'ADD' ? '加款' : '扣减' }}
              </span>
            </td>
            <td :class="t.type==='ADD' ? 'green' : 'red'">
              {{ (t.type==='ADD' ? '+' : '-') + formatMoney(t.amount) }}
            </td>
            <td>{{ t.operator?.name }}（{{ t.operator?.employeeId }}）</td>
            <td>
              <router-link
                v-if="t.relatedRequest?.id"
                :to="`/request/${t.relatedRequest.id}`"
                class="link"
              >#{{ t.relatedRequest.id }}</router-link>
              <span v-else>—</span>
            </td>
            <td>{{ t.note || '—' }}</td>
          </tr>
          <tr v-if="!tx.length">
            <td class="empty" colspan="6">暂无交易</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import dayjs from 'dayjs'
import { getBalance, addFunds, listTx } from '@/api/balance'

const balance = ref({ available: 0, updatedAt: null })
const amount = ref('')
const note = ref('')
const occurredAt = ref('')
const tx = ref([])
const busy = ref(false)

function formatMoney(n){
  const x = Number(n || 0)
  return x.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}
function fmtTime(t){
  return t ? dayjs(t).format('YYYY-MM-DD HH:mm:ss') : '--'
}

function setNow(){
  occurredAt.value = dayjs().format('YYYY-MM-DDTHH:mm')
}
function clearTime(){
  occurredAt.value = ''
}

function badgeClass(type){
  return type === 'ADD'
    ? 'badge green-badge'
    : 'badge red-badge'
}

async function loadAll(){
  balance.value = await getBalance()
  tx.value = await listTx()
}

async function onAdd(){
  const val = Number(amount.value)
  if (!val || val <= 0) return alert('请输入正确的加款金额')
  try{
    busy.value = true
    await addFunds({ amount: val, note: note.value, occurredAt: occurredAt.value || null  })
    alert('加款成功')
    amount.value = ''
    note.value = ''
    await loadAll()
  }catch(e){
    alert(e?.response?.data?.message || '加款失败')
  }finally{
    busy.value = false
  }
}

onMounted(loadAll)
</script>

<style scoped>
.card{ background:#fff; border:1px solid #e5e7eb; border-radius:10px; padding:16px; margin-top:12px; }
.stat{ display:flex; gap:24px; align-items:flex-end; justify-content:space-between; }
.stat-item{ display:flex; flex-direction:column; gap:6px; }
.stat-title{ color:#64748b; }
.stat-value{ font-size:28px; font-weight:700; }
.stat-sub{ color:#94a3b8; font-size:12px; }

.stat-actions{ display:grid; grid-template-columns: 1fr 2fr auto; gap:12px 16px; align-items:end; }
.field{ display:flex; flex-direction:column; gap:6px; }
.btn{ padding:8px 12px; border:1px solid #fff; border-radius:8px; background:#2563eb; cursor:pointer; }
.btn.primary{ background:#2563eb; border-color:#2563eb; color:#fff; }

.table{ width:100%; border-collapse:collapse; font-size:14px; }
.table th,.table td{ border-bottom:1px solid #e5e7eb; padding:10px 12px; text-align:left; }
.table .empty{ text-align:center; color:#94a3b8; padding:16px 0; }

.badge{ padding:2px 8px; border-radius:999px; font-size:12px; border:1px solid transparent; }
.green-badge{ background:#ecfdf5; color:#065f46; border-color:#a7f3d0; }
.red-badge{ background:#fef2f2; color:#991b1b; border-color:#fecaca; }
.hint{ color:#94a3b8; font-size:12px; }
.green{ color:#065f46; font-weight:600; }
.red{ color:#991b1b; font-weight:600; }
.link{ color:#2563eb; text-decoration:underline; }
</style>
