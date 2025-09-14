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

    <div class="table-container">
      <table class="approval-table">
        <colgroup>
          <col style="width:16%;" /> <!-- 审批时间 -->
          <col style="width:8%;"  /> <!-- 单号 -->
          <col style="width:26%;" /> <!-- 标题 -->
          <col style="width:10%;" /> <!-- 金额 -->
          <col style="width:10%;" /> <!-- 动作 -->
          <col style="width:12%;" /> <!-- 审批后状态 -->
          <col style="width:12%;" /> <!-- 备注 -->
          <col style="width:6%;"  /> 
        </colgroup>

        <thead>
          <tr>
            <th>审批时间</th>
            <th>单号(ID)</th>
            <th>标题</th>
            <th>金额</th>
            <th>动作</th>
            <th>审批后状态</th>
            <th>备注</th>
            <th>详情</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="row in list" :key="row.historyId">
            <td>{{ formatTime(row.occurredAt) }}</td>
            <td>#{{ row.requestId }}</td>
            <td class="truncate">{{ row.title }}</td>
            <td class="money">{{ row.amount.toFixed ? row.amount.toFixed(2) : row.amount }}</td>
            <td><span :class="badgeClass(row.action)">{{ actionLabel(row.action) }}</span></td>
            <td><span :class="statusClass(row.requestStatus)">{{ statusLabel(row.requestStatus) }}</span></td>
            <td class="truncate">{{ row.note }}</td>
            <td><router-link :to="`/request/${row.requestId}`" class="link">查看</router-link></td>
          </tr>
          <tr v-if="list.length === 0">
            <td colspan="8" class="empty">暂无审批记录</td>
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
    case "APPROVE": return "px-2 py-1 rounded bg-green-100 text-green-700";;
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

function actionLabel(action: string) {
  switch (action) {
    case "APPROVE": return "同意"
    case "REJECT":  return "拒绝"
    case "RETURN":  return "退回"
    default: return action
  }
}

function statusLabel(s: string) {
  switch (s) {
    case "APPROVED": return "已同意"
    case "REJECTED": return "已拒绝"
    case "RETURNED": return "已退回"
    case "SUBMITTED": return "已提交"
    default: return s
  }
}


async function load() {
  list.value = await fetchMyApprovals({ startDate: startDate.value, endDate: endDate.value });
}

onMounted(load);
</script>

<style scoped>
  .table-container{
    margin:20px auto;
    max-width: 1200px;    /* 想更宽可调到 1400 / 或者干脆删掉这行就铺满页面 */
    background:#fff;
    border:1px solid #e5e7eb;
    border-radius:8px;
    box-shadow:0 2px 6px rgba(0,0,0,0.05);
    overflow:hidden;
  }

  .approval-table{
    width:100%;
    border-collapse:collapse;
    table-layout:fixed;   /* 配合 colgroup 才能按百分比分配列宽 */
    font-size:14px;
  }

  .approval-table th,
  .approval-table td{
    padding:10px 12px;
    border-bottom:1px solid #e5e7eb;
    vertical-align:middle;
  }

  /* 对齐策略 */
  .approval-table th:nth-child(1),
  .approval-table td:nth-child(1),
  .approval-table th:nth-child(2),
  .approval-table td:nth-child(2),
  .approval-table th:nth-child(3),
  .approval-table td:nth-child(3),
  .approval-table th:nth-child(4),
  .approval-table td:nth-child(4),
  .approval-table th:nth-child(5),
  .approval-table td:nth-child(5),
  .approval-table th:nth-child(6),
  .approval-table td:nth-child(6),
  .approval-table th:nth-child(7),
  .approval-table td:nth-child(7),
  .approval-table th:nth-child(8),
  .approval-table td:nth-child(8){
    text-align:center;
  }

  /* .approval-table th:nth-child(4),
  .approval-table td:nth-child(4){
    text-align:right;
    font-family:monospace;
  } */

  .approval-table thead{ background:#f9fafb; font-weight:600; }
  .approval-table tbody tr:hover{ background:#f3f4f6; }

  /* 溢出省略号：标题/备注列用 */
  .truncate{
    white-space:nowrap;
    overflow:hidden;
    text-overflow:ellipsis;
  }

  /* 小标签样式（如果已存在可忽略） */
  .badge{ display:inline-block; padding:2px 6px; border-radius:4px; font-size:12px; font-weight:500; white-space:nowrap; }
  .badge-approve{ background:#dcfce7; color:#166534; }
  .badge-reject{  background:#fee2e2; color:#991b1b; }
  .badge-return{  background:#fef9c3; color:#92400e; }
  .badge-default{ background:#f3f4f6; color:#374151; }

  .link{ color:#2563eb; text-decoration:none; }
  .link:hover{ text-decoration:underline; }

  .empty{ text-align:center; color:#9ca3af; padding:20px; }
</style>