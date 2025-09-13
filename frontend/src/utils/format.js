export function formatDate(d) {
  if (!d) return ''
  const dt = new Date(d)
  return dt.toLocaleString('zh-CN')
}

export function statusBadge(s) {
  const map = {
    DRAFT: '草稿',
    SUBMITTED: '已提交',
    RETURNED: '退回修改',
    APPROVED: '已通过',
    REJECTED: '已驳回'
  }
  return map[s] || s
}