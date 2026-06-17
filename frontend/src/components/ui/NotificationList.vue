<script setup lang="ts">
import { Bell, CircleCheck, Document, Warning } from '@element-plus/icons-vue'
import type { Notification } from '@/types'
import StatusTag from './StatusTag.vue'

defineProps<{
  notifications: Notification[]
}>()

defineEmits<{
  (event: 'open', notification: Notification): void
  (event: 'mark-read', notification: Notification): void
}>()

function isRead(row: Notification) {
  return row.read === 1 || row.readStatus === 1
}

function typeLabel(type: string) {
  const labels: Record<string, string> = {
    COURSE_APPLICATION: '课程申请通知',
    COURSE_APPLICATION_CREATED: '课程申请通知',
    COURSE_APPROVAL: '审批结果通知',
    COURSE_APPLICATION_APPROVED: '审批结果通知',
    COURSE_APPLICATION_REJECTED: '审批结果通知',
    ASSIGNMENT_PUBLISHED: '作业发布通知',
    SUBMISSION_CREATED: '提交成功通知',
    GRADE_COMPLETED: '批改完成通知',
    ASSIGNMENT_RETURNED: '退回修改通知',
    DUPLICATE_FILE: '相似度核查提醒',
    DUPLICATE_RECORD: '相似度核查提醒',
    SIMILARITY_ALERT: '高度相似提醒',
    SYSTEM: '系统通知',
    DEMO_DATA_READY: '系统通知'
  }
  return labels[type] || type
}

function iconFor(type: string) {
  if (type.includes('DUPLICATE')) return Warning
  if (type.includes('GRADE') || type.includes('APPROVAL')) return CircleCheck
  if (type.includes('ASSIGNMENT') || type.includes('SUBMISSION')) return Document
  return Bell
}
</script>

<template>
  <div class="notification-list">
    <button
      v-for="notice in notifications"
      :key="notice.id"
      type="button"
      class="notice-item"
      :class="{ unread: !isRead(notice) }"
      @click="$emit('open', notice)"
    >
      <span class="notice-dot" />
      <span class="notice-icon">
        <component :is="iconFor(notice.type)" />
      </span>
      <span class="notice-main">
        <strong>{{ notice.title }}</strong>
        <em>{{ notice.content }}</em>
        <small>{{ notice.createdAt }}</small>
      </span>
      <span class="notice-side">
        <StatusTag :label="typeLabel(notice.type)" />
        <el-button v-if="!isRead(notice)" size="small" text @click.stop="$emit('mark-read', notice)">标记已读</el-button>
      </span>
    </button>
  </div>
</template>

<style scoped>
.notification-list {
  display: grid;
  gap: 10px;
}

.notice-item {
  position: relative;
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: 12px;
  align-items: center;
  width: 100%;
  padding: 14px 16px 14px 18px;
  color: inherit;
  text-align: left;
  cursor: pointer;
  background: #fff;
  border: 1px solid var(--app-border);
  border-radius: 12px;
}

.notice-item:hover {
  border-color: var(--app-primary);
}

.notice-dot {
  position: absolute;
  top: 18px;
  left: 8px;
  width: 7px;
  height: 7px;
  background: transparent;
  border-radius: 999px;
}

.notice-item.unread .notice-dot {
  background: var(--app-danger);
}

.notice-icon {
  display: grid;
  place-items: center;
  width: 38px;
  height: 38px;
  color: var(--app-primary);
  background: var(--app-primary-soft);
  border-radius: 10px;
  font-size: 18px;
}

.notice-main strong,
.notice-main em,
.notice-main small {
  display: block;
}

.notice-main em {
  margin-top: 5px;
  color: #334155;
  font-style: normal;
}

.notice-main small {
  margin-top: 5px;
  color: var(--app-muted);
}

.notice-side {
  display: grid;
  justify-items: end;
  gap: 6px;
}

@media (max-width: 720px) {
  .notice-item {
    grid-template-columns: auto 1fr;
  }

  .notice-side {
    grid-column: 2;
    justify-items: start;
  }
}
</style>
