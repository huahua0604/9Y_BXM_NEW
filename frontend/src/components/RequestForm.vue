<template>
  <form class="card" @submit.prevent="onSubmit">
    <div class="header"><h3>新建报销单</h3></div>
    <div style="display:grid;grid-template-columns:1fr 1fr;gap:12px;">
      <div>
        <label>标题</label>
        <input class="input" v-model.trim="form.title" required placeholder="如：会议差旅报销" />
      </div>
      <div>
        <label>金额（元）</label>
        <input class="input" v-model.number="form.amount" type="number" step="0.01" min="0.01" required />
      </div>
      <div>
        <label>类别</label>
        <select v-model="form.category" class="input" required>
          <option value="">请选择</option>
          <option>差旅</option>
          <option>办公</option>
          <option>培训</option>
          <option>其他</option>
        </select>
      </div>
      <div>
        <label>发生日期</label>
        <input class="input" type="date" v-model="form.occurDate" required />
      </div>
      <div style="grid-column:1/3;">
        <label>备注</label>
        <textarea class="input" rows="3" v-model.trim="form.remark" maxlength="512" placeholder="可选" />
      </div>
    </div>
    <div style="margin-top:14px;display:flex;gap:10px;">
      <button class="btn" :disabled="loading">保存</button>
      <slot name="extra" />
    </div>
  </form>
</template>

<script setup>
import { ref } from 'vue'
import http from '../api/http'

const emit = defineEmits(['created'])
const loading = ref(false)
const form = ref({ title:'', amount:null, category:'', occurDate:'', remark:'' })

async function onSubmit(){
  loading.value = true
  try{
    const { data:id } = await http.post('/api/requests', form.value)
    emit('created', id)
    form.value = { title:'', amount:null, category:'', occurDate:'', remark:'' }
  }catch(e){
    alert(e?.response?.data?.message || '保存失败')
  }finally{
    loading.value = false
  }
}
</script>