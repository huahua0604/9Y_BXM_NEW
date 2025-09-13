<template>
  <div class="p-6 space-y-4">
    <div class="flex items-end gap-4">
      <div>
        <label class="block text-sm text-gray-500 mb-1">开始日期</label>
        <input v-model="startDate" type="date" class="border rounded px-3 py-2" />
      </div>
      <div>
        <label class="block text-sm text-gray-500 mb-1">结束日期</label>
        <input v-model="endDate" type="date" class="border rounded px-3 py-2" />
      </div>
      <button @click="load" class="bg-blue-600 text-white px-4 py-2 rounded">查询</button>
    </div>

    <div class="border rounded overflow-hidden">
      <table class="min-w-full text-sm">
        <thead class="bg-gray-100 text-left">
          <tr>
            <th class="px-4 py-2">审批时间</th>
            <th class="px-4 py-2">单号(ID)</th>
            <th class="px-4 py-2">标题</th>
            <th class="px-4 py-2">金额</th>
            <th class="px-4 py-2">动作</th>
            <th class="px-4 py-2">审批后状态</th>
            <th class="px-4 py-2">备注</th>
            <th class="px-4 py-2">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="row in list" :key="row.historyId" class="border-t">
            <td class="px-4 py-2">{{ formatTime(row.occurredAt) }}</td>
            <td class="px-4 py-2">#{{ row.requestId }}</td>
            <td class="px-4 py-2">{{ row.title }}</td>
            <td class="px-4 py-2">{{ row.amount.toFixed ? row.amount.toFixed(2) : row.amount }}</td>
            <td class="px-4 py-2">
              <span :class="badgeClass(row.action)">{{ row.action }}</span>
            </td>
            <td class="px-4 py-2">
              <span :class="statusClass(row.requestStatus)">{{ row.requestStatus }}</span>
            </td>
            <td class="px-4 py-2">{{ row.note }}</td>
            <td class="px-4 py-2">
              <router-link :to="`/requests/${row.requestId}`" class="text-blue-600 hover:underline">查看单据</router-link>
            </td>
          </tr>
          <tr v-if="list.length === 0">
            <td class="px-4 py-6 text-gray-500" colspan="8">暂无审批记录</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted } from "vue";
import dayjs from "dayjs";
import { fetchMyApprovals, type ApprovalHistoryDto } from "@/api/review";

const list = ref<ApprovalHistoryDto[]>([]);
const startDate = ref<string>(dayjs().subtract(30, "day").format("YYYY-MM-DD"));
const endDate = ref<string>(dayjs().format("YYYY-MM-DD"));

function formatTime(t: string) {
  return dayjs(t).format("YYYY-MM-DD HH:mm");
}

function badgeClass(action: string) {
  switch (action) {
    case "APPROVE": return "px-2 py-1 rounded bg-green-100 text-green-700";
    case "REJECT":  return "px-2 py-1 rounded bg-red-100 text-red-700";
    case "RETURN":  return "px-2 py-1 rounded bg-amber-100 text-amber-700";
    default: return "px-2 py-1 rounded bg-gray-100 text-gray-700";
  }
}

function statusClass(s: string) {
  switch (s) {
    case "APPROVED": return "px-2 py-1 rounded bg-green-100 text-green-700";
    case "REJECTED": return "px-2 py-1 rounded bg-red-100 text-red-700";
    case "RETURNED": return "px-2 py-1 rounded bg-amber-100 text-amber-700";
    case "SUBMITTED":return "px-2 py-1 rounded bg-blue-100 text-blue-700";
    default: return "px-2 py-1 rounded bg-gray-100 text-gray-700";
  }
}

async function load() {
  list.value = await fetchMyApprovals({ startDate: startDate.value, endDate: endDate.value });
}

onMounted(load);
</script>

<style scoped>
table { border-collapse: collapse; width: 100%; }
th, td { border-color: #eee; }
</style>
