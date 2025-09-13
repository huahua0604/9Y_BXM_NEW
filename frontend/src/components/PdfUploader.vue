<template>
  <div class="card">
    <div class="header"><h3>上传报销凭证(PDF)</h3></div>
    <input ref="fileRef" type="file" accept="application/pdf,image/*" @change="onFile" />
    <div style="margin-top:10px;">
      <button class="btn" :disabled="!file || uploading" @click="upload">上传</button>
      <span v-if="file" style="margin-left:10px;">{{ file.name }} ({{ (file.size/1024/1024).toFixed(2) }} MB)</span>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import http from '../api/http'

const props = defineProps({ requestId: { type: Number, required: true } })
const emit = defineEmits(['uploaded'])

const fileRef = ref(null)
const file = ref(null)
const uploading = ref(false)

function onFile(e){ file.value = e.target.files[0] }

async function upload(){
  if(!file.value) return
  try{
    uploading.value = true
    const fd = new FormData()
    fd.append('file', file.value)
    await http.post(`/api/requests/${props.requestId}/attachments`, fd, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    emit('uploaded')
    fileRef.value.value = ''
    file.value = null
  }catch(e){
    alert(e?.response?.data?.message || '上传失败')
  }finally{
    uploading.value = false
  }
}
</script>