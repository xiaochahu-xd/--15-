<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  status?: string
  label?: string
}>()

const text = computed(() => props.label || statusLabel(props.status || ''))
const kind = computed(() => statusKind(props.status || props.label || ''))

function statusLabel(status: string) {
  const labels: Record<string, string> = {
    ACTIVE: '正常',
    PENDING: '待审批',
    PENDING_REVIEW: '待核查',
    CONFIRMED: '已确认',
    IGNORED: '已忽略',
    APPROVED: '已通过',
    REJECTED: '已驳回',
    PUBLISHED: '已发布',
    SUBMITTED: '已提交',
    AUTO_GRADED: '自动判分',
    GRADED: '已批改',
    RETURNED: '已退回',
    LATE: '迟交',
    DUPLICATE: '高度相似',
    SIMILARITY_ALERT: '高度相似',
    TEXT_SIMILARITY: '文本相似度',
    EXACT_HASH: '完全相同文件',
    TEXT: '文本作业',
    FILE: '文件作业',
    SINGLE_CHOICE: '单选题',
    TRUE_FALSE: '判断题',
    OWNER: '负责人',
    TEACHER: '教师',
    ASSISTANT: '助教',
    STUDENT: '学生',
    ADMIN: '管理员'
  }
  return labels[status] || status || '未知'
}

function statusKind(status: string) {
  if (['GRADED', 'APPROVED', 'ACTIVE', 'SUBMITTED', 'CONFIRMED', '已批改', '已通过', '已提交', '正常', '已确认'].includes(status)) return 'success'
  if (['PENDING', 'PENDING_REVIEW', 'LATE', '待截止', '待审批', '待批改', '迟交', '审核中', '待核查'].includes(status)) return 'warning'
  if (['REJECTED', 'RETURNED', 'DUPLICATE', 'SIMILARITY_ALERT', '高度相似', '已驳回', '已退回'].includes(status)) return 'danger'
  if (['AUTO_GRADED', 'SINGLE_CHOICE', 'TRUE_FALSE', '自动判分', '单选题', '判断题'].includes(status)) return 'purple'
  if (['TEXT', 'FILE', 'TEXT_SIMILARITY', 'EXACT_HASH', '进行中', '已发布', '文本相似度', '完全相同文件'].includes(status)) return 'primary'
  return 'info'
}
</script>

<template>
  <span class="status-tag" :class="`kind-${kind}`">{{ text }}</span>
</template>

<style scoped>
.status-tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 24px;
  padding: 0 9px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
  white-space: nowrap;
}

.kind-primary { color: #1d4ed8; background: #dbeafe; }
.kind-success { color: #15803d; background: #dcfce7; }
.kind-warning { color: #b45309; background: #fef3c7; }
.kind-danger { color: #b91c1c; background: #fee2e2; }
.kind-purple { color: #6d28d9; background: #ede9fe; }
.kind-info { color: #475569; background: #f1f5f9; }
</style>
