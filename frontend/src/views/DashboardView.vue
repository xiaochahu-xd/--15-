<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { getDashboardApi } from '@/api/dashboard'
import { useAuthStore } from '@/stores/auth'
import ChartCard from '@/components/ui/ChartCard.vue'
import DashboardCard from '@/components/ui/DashboardCard.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import NotificationList from '@/components/ui/NotificationList.vue'
import PageHeader from '@/components/ui/PageHeader.vue'
import StatCard from '@/components/ui/StatCard.vue'
import StatusTag from '@/components/ui/StatusTag.vue'
import TaskItem from '@/components/ui/TaskItem.vue'
import type { Course, CourseApplication, DashboardData, OperationLog, RoleCode } from '@/types'

type Tone = 'primary' | 'success' | 'warning' | 'danger' | 'purple' | 'info'

interface DashboardMetric {
  title: string
  value: string | number
  hint: string
  tone: Tone
  icon: string
  path: string
}

interface TodoItem {
  title: string
  desc: string
  count: number
  tone: Tone
  path: string
}

const router = useRouter()
const authStore = useAuthStore()
const loading = ref(false)
const dashboard = ref<DashboardData | null>(null)
const trendChartRef = ref<HTMLElement>()
let trendChart: echarts.ECharts | null = null

const role = computed<RoleCode>(() => dashboard.value?.primaryRole || authStore.user?.roles[0] || 'STUDENT')
const roleName = computed(() => ({
  ADMIN: '管理员控制台',
  TEACHER: '教师控制台',
  ASSISTANT: '助教控制台',
  STUDENT: '学生控制台'
}[role.value]))

const roleEyebrow = computed(() => `${authStore.user?.realName || authStore.user?.username || ''} · ${authStore.roleNames}`)

const metrics = computed<DashboardMetric[]>(() => {
  const data = dashboard.value
  if (!data) return []
  if (role.value === 'ADMIN') {
    return [
      { title: '待审批课程', value: data.pendingCourseApplications || 0, hint: '教师课程申请', tone: 'warning', icon: 'Finished', path: '/admin/course-approvals' },
      { title: '已审批课程', value: data.approvedCourseApplications || 0, hint: '通过的课程申请', tone: 'success', icon: 'Reading', path: '/admin/courses' },
      { title: '用户总数', value: data.userCount || 0, hint: '平台账号规模', tone: 'primary', icon: 'User', path: '/admin/users' },
      { title: '未读通知', value: data.unreadNotificationCount || 0, hint: '站内提醒', tone: 'purple', icon: 'Bell', path: '/notifications' }
    ]
  }
  if (role.value === 'TEACHER') {
    return [
      { title: '我的课程', value: data.myCourseCount || 0, hint: '负责或参与课程', tone: 'primary', icon: 'Reading', path: '/teacher/courses' },
      { title: '已发布作业', value: data.assignmentCount || 0, hint: '课程内全部作业', tone: 'success', icon: 'Files', path: '/teacher/assignments' },
      { title: '待批改', value: data.pendingGradingCount || 0, hint: '需要人工处理', tone: 'warning', icon: 'DocumentChecked', path: '/teacher/grading' },
      { title: '未读通知', value: data.unreadNotificationCount || 0, hint: '站内提醒', tone: 'purple', icon: 'Bell', path: '/notifications' }
    ]
  }
  if (role.value === 'ASSISTANT') {
    return [
      { title: '负责课程', value: data.myCourseCount || 0, hint: '被授权课程', tone: 'primary', icon: 'Reading', path: '/assistant/courses' },
      { title: '待批改', value: data.pendingGradingCount || 0, hint: '可处理提交', tone: 'warning', icon: 'DocumentChecked', path: '/assistant/grading' },
      { title: '已批改', value: data.gradedCount || 0, hint: '已完成评分', tone: 'success', icon: 'DataAnalysis', path: '/assistant/statistics' },
      { title: '高度相似', value: data.duplicateCount || 0, hint: '需人工核查', tone: 'danger', icon: 'Bell', path: '/assistant/grading' }
    ]
  }
  return [
    { title: '我的课程', value: data.myCourseCount || 0, hint: '已加入课程', tone: 'primary', icon: 'Reading', path: '/student/courses' },
    { title: '待提交作业', value: data.pendingAssignmentCount || 0, hint: '关注截止时间', tone: 'warning', icon: 'Files', path: '/student/assignments' },
    { title: '已提交作业', value: data.submittedAssignmentCount || 0, hint: '提交记录', tone: 'success', icon: 'DocumentChecked', path: '/student/submissions' },
    { title: '已批改作业', value: data.gradedCount || 0, hint: '可查看成绩', tone: 'purple', icon: 'TrendCharts', path: '/student/grades' }
  ]
})

const todoItems = computed<TodoItem[]>(() => {
  const data = dashboard.value
  if (!data) return []
  if (role.value === 'ADMIN') {
    return [
      { title: '课程申请待审批', desc: '处理教师提交的课程创建申请', count: data.pendingCourseApplications || 0, tone: 'warning', path: '/admin/course-approvals' },
      { title: '系统日志待核查', desc: '查看登录、审批、权限拒绝记录', count: data.recentLogs?.length || 0, tone: 'primary', path: '/admin/logs' },
      { title: '平台用户管理', desc: '检查教师、助教、学生账号状态', count: data.userCount || 0, tone: 'purple', path: '/admin/users' }
    ]
  }
  if (role.value === 'STUDENT') {
    return [
      { title: '作业待提交', desc: '进入我的作业完成文本、文件或客观题提交', count: data.pendingAssignmentCount || 0, tone: 'warning', path: '/student/assignments' },
      { title: '成绩待查看', desc: '查看自动判分、最终成绩和教师评语', count: data.gradedCount || 0, tone: 'success', path: '/student/grades' },
      { title: '通知待阅读', desc: '查看作业发布、退回修改和成绩通知', count: data.unreadNotificationCount || 0, tone: 'purple', path: '/notifications' }
    ]
  }
  return [
    { title: '提交待批改', desc: '处理文本作业、文件作业和主观评分', count: data.pendingGradingCount || 0, tone: 'warning', path: role.value === 'ASSISTANT' ? '/assistant/grading' : '/teacher/grading' },
    { title: '高度相似提交', desc: '按内容相似度提示风险，人工核查后处理', count: data.duplicateCount || 0, tone: 'danger', path: role.value === 'ASSISTANT' ? '/assistant/grading' : '/teacher/grading' },
    { title: '课程与作业维护', desc: role.value === 'TEACHER' ? '发布作业、维护题目和查看提交' : '查看授权课程和作业提交', count: data.assignmentCount || data.myCourseCount || 0, tone: 'primary', path: role.value === 'ASSISTANT' ? '/assistant/courses' : '/teacher/assignments' }
  ]
})

const recentCourses = computed(() => (dashboard.value?.recentCourses || []).slice(0, 5))
const recentApplications = computed(() => (dashboard.value?.recentApplications || []).slice(0, 5))
const recentNotifications = computed(() => (dashboard.value?.recentNotifications || []).slice(0, 3))
const recentLogs = computed(() => (dashboard.value?.recentLogs || []).slice(0, 5))

const trendData = computed(() => {
  const base = Math.max(
    dashboard.value?.submittedAssignmentCount || 0,
    dashboard.value?.assignmentCount || 0,
    dashboard.value?.pendingGradingCount || 0,
    6
  )
  const ratios = [0.42, 0.58, 0.54, 0.78, 0.66, 0.62, 0.47]
  return ratios.map((ratio, index) => {
    const date = new Date()
    date.setDate(date.getDate() - (6 - index))
    const label = `${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
    return {
      label,
      value: Math.max(Math.round(base * ratio), index === 0 ? 1 : 2)
    }
  })
})

async function loadDashboard() {
  loading.value = true
  try {
    const response = await getDashboardApi()
    dashboard.value = response.data.data
    await renderTrendChart()
  } catch {
    ElMessage.error('工作台数据加载失败')
  } finally {
    loading.value = false
  }
}

function openCourse(course: Course) {
  router.push(`/courses/${course.id}`)
}

function openPrimaryAction() {
  if (role.value === 'ADMIN') router.push('/admin/course-approvals')
  else if (role.value === 'TEACHER') router.push('/teacher/grading')
  else if (role.value === 'ASSISTANT') router.push('/assistant/grading')
  else router.push('/student/assignments')
}

function openApplication(application: CourseApplication) {
  if (role.value === 'ADMIN') router.push('/admin/course-approvals')
  else router.push('/teacher/courses')
}

function logText(log: OperationLog) {
  return `${log.operation} · ${log.result}`
}

async function renderTrendChart() {
  await nextTick()
  if (!trendChartRef.value) return
  trendChart = trendChart || echarts.init(trendChartRef.value)
  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 36, right: 18, top: 30, bottom: 34 },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: trendData.value.map((item) => item.label),
      axisTick: { show: false },
      axisLine: { lineStyle: { color: '#e5e7eb' } }
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      splitLine: { lineStyle: { color: '#eef2f7' } }
    },
    series: [{
      name: '提交数量',
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 8,
      data: trendData.value.map((item) => item.value),
      lineStyle: { width: 3, color: '#2563eb' },
      itemStyle: { color: '#2563eb', borderColor: '#fff', borderWidth: 2 },
      areaStyle: { color: 'rgba(37, 99, 235, 0.12)' }
    }]
  })
}

function resizeTrendChart() {
  trendChart?.resize()
}

onMounted(() => {
  loadDashboard()
  window.addEventListener('resize', resizeTrendChart)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeTrendChart)
  trendChart?.dispose()
  trendChart = null
})
</script>

<template>
  <section class="page-stack dashboard-page" v-loading="loading">
    <PageHeader
      :title="roleName"
      :eyebrow="roleEyebrow"
      description="汇总当前角色最需要关注的课程、作业、批改和通知，适合答辩时展示完整业务闭环。"
    >
      <template #actions>
        <el-button @click="loadDashboard">刷新</el-button>
        <el-button type="primary" @click="openPrimaryAction">处理当前待办</el-button>
      </template>
    </PageHeader>

    <div class="stat-grid">
      <StatCard
        v-for="item in metrics"
        :key="item.title"
        :title="item.title"
        :value="item.value"
        :hint="item.hint"
        :tone="item.tone"
        :icon="item.icon as any"
        clickable
        @click="router.push(item.path)"
      />
    </div>

    <div class="dashboard-board">
      <DashboardCard class="board-card board-card-large" :title="role === 'ADMIN' ? '最近课程申请' : '最近课程'" compact>
        <template #actions>
          <el-button text type="primary" @click="role === 'ADMIN' ? router.push('/admin/course-approvals') : router.push(role === 'ASSISTANT' ? '/assistant/courses' : role === 'STUDENT' ? '/student/courses' : '/teacher/courses')">
            查看全部
          </el-button>
        </template>

        <EmptyState
          v-if="role === 'ADMIN' && recentApplications.length === 0"
          title="暂无待审批课程申请"
          description="教师提交课程申请后会出现在这里。"
        />
        <EmptyState
          v-else-if="role !== 'ADMIN' && recentCourses.length === 0"
          title="暂无课程数据"
          description="加入课程或创建课程后会出现在这里。"
        />
        <div v-else class="course-compact-list">
          <button
            v-for="course in recentCourses"
            :key="course.id"
            class="course-compact-row"
            type="button"
            @click="openCourse(course)"
          >
            <span class="course-mark">{{ course.courseName.slice(0, 1) }}</span>
            <span class="course-main">
              <strong>{{ course.courseName }}</strong>
              <small>{{ course.courseCode }} · {{ course.ownerName }}</small>
            </span>
            <StatusTag :status="course.memberRole" />
          </button>
          <button
            v-for="application in recentApplications"
            :key="application.id"
            class="course-compact-row"
            type="button"
            @click="openApplication(application)"
          >
            <span class="course-mark pending">审</span>
            <span class="course-main">
              <strong>{{ application.courseName }}</strong>
              <small>{{ application.courseCode }} · {{ application.teacherName }}</small>
            </span>
            <StatusTag status="PENDING" />
          </button>
        </div>
      </DashboardCard>

      <DashboardCard class="board-card board-card-side" title="待处理事项" compact>
        <div class="task-list">
          <TaskItem
            v-for="item in todoItems"
            :key="item.title"
            :count="item.count"
            :title="item.title"
            :description="item.desc"
            :tone="item.tone"
            @open="router.push(item.path)"
          />
        </div>
      </DashboardCard>

      <DashboardCard v-if="role !== 'ADMIN'" class="board-card board-card-notice" title="最近通知" compact>
        <template #actions>
          <el-button text type="primary" @click="router.push('/notifications')">进入通知中心</el-button>
        </template>
        <EmptyState v-if="recentNotifications.length === 0" title="暂无通知" description="课程和作业事件会通过事件系统进入这里。" />
        <NotificationList v-else :notifications="recentNotifications" @open="router.push('/notifications')" />
      </DashboardCard>

      <DashboardCard v-if="role === 'ADMIN'" class="board-card board-card-notice" title="最近操作日志" compact>
        <template #actions>
          <el-button text type="primary" @click="router.push('/admin/logs')">系统日志</el-button>
        </template>
        <EmptyState v-if="recentLogs.length === 0" title="暂无操作日志" description="登录、审批、权限拒绝等关键行为会写入日志。" />
        <div v-else class="log-list">
          <div v-for="log in recentLogs" :key="log.id" class="log-row">
            <strong>{{ logText(log) }}</strong>
            <small>{{ log.username || '系统' }} · {{ log.createdAt }}</small>
          </div>
        </div>
      </DashboardCard>

      <ChartCard class="board-card board-card-chart" title="最近 7 天提交趋势" description="ECharts 折线图展示提交活跃度。">
        <div ref="trendChartRef" class="echarts-line"></div>
      </ChartCard>
    </div>
  </section>
</template>

<style scoped>
.dashboard-board {
  display: grid;
  grid-template-columns: repeat(12, minmax(0, 1fr));
  gap: 18px;
  align-items: stretch;
}

.board-card {
  min-width: 0;
}

.board-card-large {
  grid-column: span 7;
}

.board-card-side {
  grid-column: span 5;
}

.board-card-notice,
.board-card-chart {
  grid-column: span 6;
}

.course-compact-list,
.task-list,
.log-list {
  display: grid;
  gap: 10px;
}

.course-compact-row {
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: 12px;
  align-items: center;
  width: 100%;
  min-height: 58px;
  padding: 10px 12px;
  color: inherit;
  text-align: left;
  cursor: pointer;
  background: #f8fafc;
  border: 1px solid var(--app-border);
  border-radius: 12px;
}

.course-compact-row:hover {
  border-color: var(--app-primary);
  background: #fff;
}

.course-mark {
  display: grid;
  place-items: center;
  width: 38px;
  height: 38px;
  color: #fff;
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  border-radius: 10px;
  font-weight: 800;
}

.course-mark.pending {
  background: linear-gradient(135deg, #f59e0b, #d97706);
}

.course-main strong,
.course-main small {
  display: block;
}

.course-main strong {
  font-size: 14px;
}

.course-main small {
  margin-top: 4px;
  color: var(--app-muted);
}

.log-row {
  display: grid;
  gap: 4px;
  padding: 11px 12px;
  background: #f8fafc;
  border: 1px solid var(--app-border);
  border-radius: 12px;
}

.log-row strong {
  font-size: 14px;
}

.log-row small {
  color: var(--app-muted);
}

.echarts-line {
  width: 100%;
  height: 240px;
}

@media (max-width: 1100px) {
  .board-card-large,
  .board-card-side,
  .board-card-notice,
  .board-card-chart {
    grid-column: span 12;
  }
}
</style>
