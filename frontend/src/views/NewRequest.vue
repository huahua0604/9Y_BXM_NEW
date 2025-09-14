<template>
  <div class="card">
    <h2>新建报销单</h2>

    <!-- 基础信息 -->
    <div class="grid">
      <div class="field">
        <label>标题</label>
        <input v-model.trim="form.title" placeholder="例如：会议差旅报销" />
      </div>
      <div class="field">
        <label>金额（元）</label>
        <input v-model.number="form.amount" type="number" min="0" step="0.01" placeholder="0.00" />
      </div>
      <div class="field">
        <label>类别</label>
        <select v-model="form.category">
          <option value="" disabled>请选择</option>
          <option value="差旅">差旅</option>
          <option value="餐饮">餐饮</option>
          <option value="耗材">耗材</option>
          <option value="其他">其他</option>
        </select>
      </div>
      <div class="field">
        <label>发生日期</label>
        <input v-model="form.occurDate" type="date" />
      </div>
      <div class="field col-2">
        <label>备注</label>
        <textarea v-model.trim="form.remark" rows="3" placeholder="可选"></textarea>
      </div>
    </div>

    <!-- 凭证文件（支持多选：PDF/图片） -->
    <div class="field" style="margin-top:10px;">
      <label>报销凭证(PDF/JPG/PNG...)</label>
      <input type="file" multiple accept="application/pdf,image/*" @change="onPick" />
      <p v-if="files.length" class="tips">已选择 {{ files.length }} 个文件</p>
    </div>

    <!-- 一键提交 -->
    <div class="actions">
      <button class="btn primary" :disabled="busy" @click="oneClickSubmit">
        {{ busy ? progressText : '提交' }}
      </button>
      <button class="btn" :disabled="busy" @click="resetAll">重置</button>
    </div>

    <!-- 简单结果提示 -->
    <div v-if="result" class="result" :class="result.ok ? 'ok' : 'err'">
      {{ result.msg }}
    </div>
  </div>
</template>

<script setup>
// 若你没有封装 axios，这里可以改为 `import axios from 'axios'`
import axios from '@/utils/axios'
import { ref } from 'vue'

// 基础表单
const form = ref({
  title: '',
  amount: null,
  category: '',
  occurDate: '',
  remark: ''
})

// 选中的文件（多选）
const files = ref([])

function onPick(e){
  files.value = Array.from(e.target.files || [])
}

// 进度/忙碌状态
const busy = ref(false)
const progressText = ref('')
const result = ref(null)

async function oneClickSubmit(){
  if(!form.value.title) return toast('请填写标题')
  if(form.value.amount == null || form.value.amount === '' || isNaN(form.value.amount)) return toast('请填写金额')
  if(!form.value.category) return toast('请选择类别')
  if(!form.value.occurDate) return toast('请选择发生日期')
  if(files.value.length === 0) return toast('请至少选择 1 个凭证文件')

  busy.value = true
  result.value = null

  try {
    // 1) 新建报销单
    progressText.value = '正在创建报销单...'
    const { data: created } = await axios.post('/api/requests', form.value)

    // 关键：兼容两种返回结构
    let requestId = null
    if (created && typeof created === 'object' && 'id' in created) {
      requestId = created.id
    } else if (typeof created === 'number') {
      requestId = created
    }
    if (!requestId) {
      throw new Error('创建报销单失败：后端未返回ID')
    }

    // 2) 逐个上传附件
    for (let i = 0; i < files.value.length; i++){
      progressText.value = `正在上传附件 ${i+1}/${files.value.length}...`
      const fd = new FormData()
      fd.append('file', files.value[i])
      await axios.post(`/api/requests/${requestId}/attachments`, fd)
    }

    // 3) 提交审核
    progressText.value = '正在提交审核...'
    await axios.post(`/api/requests/${requestId}/submit`)

    result.value = { ok: true, msg: `提交成功！单号 #${requestId}` }
    progressText.value = ''
  } catch (e) {
    console.error(e)
    result.value = { ok: false, msg: e?.response?.data?.message || e.message || '提交失败，请稍后重试' }
  } finally {
    busy.value = false
  }
}


function resetAll(){
  form.value = { title:'', amount:null, category:'', occurDate:'', remark:'' }
  files.value = []
  busy.value = false
  progressText.value = ''
  result.value = null
}

function toast(msg){ alert(msg) }
</script>

<style scoped>
.card{
  max-width: 920px;
  margin: 16px auto;
  padding: 16px;
  background:#fff;
  border:1px solid #e5e7eb;
  border-radius: 10px;
}
h2{ margin:0 0 14px; }

.grid{
  display:grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px 16px;
}
.field{ display:flex; flex-direction:column; gap:6px; }
.field.col-2{ grid-column: span 2; }

input, select, textarea{
  border:1px solid #d1d5db; border-radius:8px; padding:8px 10px; outline:none;
}
input:focus, select:focus, textarea:focus{
  border-color:#60a5fa; box-shadow:0 0 0 3px rgba(59,130,246,0.15);
}

.actions{ margin-top:16px; display:flex; gap:10px; }
.btn{ padding:9px 14px; border:1px solid #d1d5db; border-radius:8px; background:#f8fafc; cursor:pointer; }
.btn:hover{ background:#eef2ff; }
.btn.primary{ background:#2563eb; color:#fff; border-color:#2563eb; }
.btn.primary[disabled]{ opacity:.6; cursor:not-allowed; }
.tips{ color:#64748b; margin-top:6px; }

.result{ margin-top:12px; padding:10px; border-radius:8px; }
.result.ok{ background:#ecfdf5; color:#065f46; border:1px solid #a7f3d0; }
.result.err{ background:#fef2f2; color:#991b1b; border:1px solid #fecaca; }
</style>

