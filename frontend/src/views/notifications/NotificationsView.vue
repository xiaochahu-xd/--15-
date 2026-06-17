<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { listNotificationsApi, markAllNotificationsReadApi, markNotificationReadApi } from '@/api/notification'
import EmptyState from '@/components/ui/EmptyState.vue'
import FilterTabs from '@/components/ui/FilterTabs.vue'
import NotificationList from '@/components/ui/NotificationList.vue'
import PageHeader from '@/components/ui/PageHeader.vue'
import StatCard from '@/components/ui/StatCard.vue'
import { useAuthStore } from '@/stores/auth'
import type { Notification } from '@/types'

const router = useRouter()
const authStore = useAuthStore()
const notifications = ref<Notification[]>([])
const loading = ref(false)
const filter = ref('ALL')

const unreadCount = computed(() => notifications.value.filter((item) => !isRead(item)).length)
const assignmentCount = computed(() => notifications.value.filter((item) => groupOf(item) === 'ASSIGNMENT').length)
const courseCount = computed(() => notifications.value.filter((item) => groupOf(item) === 'COURSE').length)
const importantCount = computed(() => notifications.value.filter((item) => groupOf(item) === 'IMPORTANT').length)

const filterOptions = [
  { label: '全部', value: 'ALL' },
  { label: '作业', value: 'ASSIGNMENT' },
  { label: '系统', value: 'SYSTEM' },
  { label: '课程', value: 'COURSE' },
  { label: '重要', value: 'IMPORTANT' },
  { label: '未读', value: 'UNREAD' },
  { label: '已读', value: 'READ' }
]

const filteredNotifications = computed(() => notifications.value.filter((item) => {
  if (filter.value === 'ALL') return true
  if (filter.value === 'UNREAD') return !isRead(item)
  if (filter.value === 'READ') return isRead(item)
  return groupOf(item) === filter.value
}))

async function loadNotifications() {
  loading.value = true
  try {
    const response = await listNotificationsApi()
    notifications.value = response.data.data
  } finally {
    loading.value = false
  }
}

async function markRead(row: Notification) {
  await markNotificationReadApi(row.id)
  ElMessage.success('已标记为已读')
  await loadNotifications()
}

async function markAllRead() {
  await markAllNotificationsReadApi()
  ElMessage.success('已全部标记为已读')
  await loadNotifications()
}

async function openNotification(row: Notification) {
  if (!isRead(row)) {
    await markNotificationReadApi(row.id)
    await loadNotifications()
  }
  router.push(inferTarget(row))
}

function isRead(row: Notification) {
  return row.read === 1 || row.readStatus === 1
}

function groupOf(row: Notification) {
  const targetType = row.targetType || legacyTargetType(row.type)
  if (targetType === 'ASSIGNMENT' || targetType === 'SUBMISSION' || targetType === 'GRADE') return 'ASSIGNMENT'
  if (targetType === 'COURSE' || targetType === 'COURSE_APPLICATION') return 'COURSE'
  if (targetType === 'DUPLICATE_RECORD' || row.type.includes('RETURNED')) return 'IMPORTANT'
  return 'SYSTEM'
}

function inferTarget(row: Notification) {
  const roles = authStore.user?.roles || []
  const targetType = row.targetType || legacyTargetType(row.type)

  if (targetType === 'COURSE_APPLICATION') {
    return roles.includes('ADMIN') ? '/admin/course-approvals' : '/teacher/courses'
  }
  if (targetType === 'COURSE' && row.targetId) {
    return `/courses/${row.targetId}`
  }
  if (targetType === 'ASSIGNMENT') {
    if (row.courseId) return `/courses/${row.courseId}?tab=assignments`
    if (row.targetId && roles.includes('STUDENT')) return `/student/assignments/${row.targetId}`
    return roles.includes('ASSISTANT') ? '/assistant/assignments' : '/teacher/assignments'
  }
  if (targetType === 'SUBMISSION') {
    if (row.targetId) {
      if (roles.includes('STUDENT')) return `/student/submissions/${row.targetId}`
      if (roles.includes('ASSISTANT')) return `/assistant/submissions/${row.targetId}`
      return `/teacher/submissions/${row.targetId}`
    }
    return roles.includes('STUDENT') ? '/student/submissions' : '/teacher/grading'
  }
  if (targetType === 'GRADE') {
    if (roles.includes('STUDENT')) return '/student/grades'
    return roles.includes('ASSISTANT') ? '/assistant/statistics' : '/teacher/statistics'
  }
  if (targetType === 'DUPLICATE_RECORD') {
    if (row.courseId) return `/courses/${row.courseId}?tab=submissions`
    return roles.includes('ASSISTANT') ? '/assistant/grading' : '/teacher/grading'
  }
  return '/dashboard'
}

function legacyTargetType(type: string) {
  const legacy: Record<string, string> = {
    COURSE_APPLICATION_CREATED: 'COURSE_APPLICATION',
    COURSE_APPLICATION: 'COURSE_APPLICATION',
    COURSE_APPLICATION_APPROVED: 'COURSE',
    COURSE_APPLICATION_REJECTED: 'COURSE_APPLICATION',
    COURSE_APPROVAL: 'COURSE_APPLICATION',
    ASSIGNMENT_PUBLISHED: 'ASSIGNMENT',
    SUBMISSION_CREATED: 'SUBMISSION',
    GRADE_COMPLETED: 'GRADE',
    ASSIGNMENT_RETURNED: 'SUBMISSION',
    DUPLICATE_FILE: 'DUPLICATE_RECORD',
    DUPLICATE_RECORD: 'DUPLICATE_RECORD',
    SIMILARITY_ALERT: 'DUPLICATE_RECORD'
  }
  return legacy[type] || 'SYSTEM'
}

onMounted(loadNotifications)
</script>

<template>
  <section class="page-stack" v-loading="loading">
    <PageHeader title="通知中心" description="集中查看课程申请、作业发布、提交成功、批改完成、退回修改和高度相似提醒。">
      <template #actions>
        <el-button @click="loadNotifications">刷新</el-button>
        <el-button type="primary" :disabled="unreadCount === 0" @click="markAllRead">全部已读</el-button>
      </template>
    </PageHeader>

    <div class="stat-grid">
      <StatCard title="未读通知" :value="unreadCount" tone="danger" icon="Bell" />
      <StatCard title="作业相关" :value="assignmentCount" tone="primary" icon="Files" />
      <StatCard title="课程相关" :value="courseCount" tone="success" icon="Reading" />
      <StatCard title="重要提醒" :value="importantCount" tone="warning" icon="DocumentChecked" />
    </div>

    <section class="app-card">
      <div class="toolbar-row">
        <FilterTabs v-model="filter" :options="filterOptions" />
      </div>
    </section>

    <EmptyState v-if="!loading && filteredNotifications.length === 0" title="暂无通知" description="当前筛选条件下没有通知消息。" />
    <NotificationList v-else :notifications="filteredNotifications" @open="openNotification" @mark-read="markRead" />
  </section>
</template>
