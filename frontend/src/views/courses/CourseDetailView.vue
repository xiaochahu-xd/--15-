<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { listAssignmentsApi } from '@/api/assignment'
import { getCourseDetailApi, listCourseMembersApi } from '@/api/course'
import { listCourseNotificationsApi, markNotificationReadApi } from '@/api/notification'
import { listAssignmentSubmissionsApi, listMySubmissionsApi } from '@/api/submission'
import { exportCourseGradesApi, getCourseStatisticsApi, listCourseGradesApi } from '@/api/statistics'
import ChartCard from '@/components/ui/ChartCard.vue'
import DataTableCard from '@/components/ui/DataTableCard.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import FilterTabs from '@/components/ui/FilterTabs.vue'
import NotificationList from '@/components/ui/NotificationList.vue'
import StatCard from '@/components/ui/StatCard.vue'
import StatusTag from '@/components/ui/StatusTag.vue'
import { useAuthStore } from '@/stores/auth'
import type {
  Assignment,
  AssignmentType,
  CourseDetailSummary,
  CourseMember,
  CourseStatistics,
  GradeExportRow,
  Notification,
  Submission
} from '@/types'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const courseId = computed(() => Number(route.params.courseId))

const loading = ref(false)
const activeTab = ref('overview')
const detail = ref<CourseDetailSummary | null>(null)
const members = ref<CourseMember[]>([])
const assignments = ref<Assignment[]>([])
const submissions = ref<Submission[]>([])
const notifications = ref<Notification[]>([])
const grades = ref<GradeExportRow[]>([])
const stats = ref<CourseStatistics | null>(null)
const selectedAssignmentId = ref<number>()
const assignmentTypeFilter = ref('ALL')
const assignmentStatusFilter = ref('ALL')
const submissionStatusFilter = ref('ALL')
const exporting = ref(false)
const statusChartRef = ref<HTMLElement>()
let statusChart: echarts.ECharts | null = null

const isStudent = computed(() => authStore.user?.roles.includes('STUDENT'))
const isTeacher = computed(() => authStore.user?.roles.includes('TEACHER'))
const isAssistant = computed(() => authStore.user?.roles.includes('ASSISTANT'))
const isAdmin = computed(() => authStore.user?.roles.includes('ADMIN'))
const canManage = computed(() => isAdmin.value || detail.value?.myRole === 'OWNER')
const canReview = computed(() => isAdmin.value || isTeacher.value || isAssistant.value)
const canSeeStats = computed(() => isAdmin.value || isTeacher.value || isAssistant.value)

const typeLabels: Record<string, string> = {
  TEXT: '文本作业',
  FILE: '文件作业',
  SINGLE_CHOICE: '单选题',
  TRUE_FALSE: '判断题'
}

const memberRoleLabels: Record<string, string> = {
  OWNER: '负责人',
  TEACHER: '教师',
  ASSISTANT: '助教',
  STUDENT: '学生',
  ADMIN: '管理员'
}

const assignmentTypeOptions = [
  { label: '全部', value: 'ALL' },
  { label: '文本作业', value: 'TEXT' },
  { label: '文件作业', value: 'FILE' },
  { label: '单选题', value: 'SINGLE_CHOICE' },
  { label: '判断题', value: 'TRUE_FALSE' }
]

const assignmentStatusOptions = [
  { label: '全部状态', value: 'ALL' },
  { label: '进行中', value: '进行中' },
  { label: '即将截止', value: '即将截止' },
  { label: '已截止', value: '已截止' }
]

const submissionStatusOptions = [
  { label: '全部', value: 'ALL' },
  { label: '已提交', value: 'SUBMITTED' },
  { label: '迟交', value: 'LATE' },
  { label: '高度相似', value: 'DUPLICATE' },
  { label: '已批改', value: 'GRADED' }
]

const filteredAssignments = computed(() => assignments.value.filter((assignment) => {
  const matchType = assignmentTypeFilter.value === 'ALL' || assignment.assignmentType === assignmentTypeFilter.value
  const matchStatus = assignmentStatusFilter.value === 'ALL' || assignmentStatus(assignment).label === assignmentStatusFilter.value
  return matchType && matchStatus
}))

const filteredSubmissions = computed(() => submissions.value.filter((submission) => {
  if (submissionStatusFilter.value === 'ALL') return true
  if (submissionStatusFilter.value === 'LATE') return submission.late
  if (submissionStatusFilter.value === 'DUPLICATE') return submission.suspectedDuplicate
  if (submissionStatusFilter.value === 'GRADED') return submission.finalScore !== null && submission.finalScore !== undefined
  return submission.status === submissionStatusFilter.value
}))

const recentActivities = computed(() => notifications.value.slice(0, 5))

const assignmentStatusDistribution = computed(() => {
  const result = [
    { label: '进行中', value: 0 },
    { label: '即将截止', value: 0 },
    { label: '已截止', value: 0 }
  ]
  assignments.value.forEach((assignment) => {
    const status = assignmentStatus(assignment).label
    const target = result.find((item) => item.label === status)
    if (target) target.value += 1
  })
  return result
})

watch(selectedAssignmentId, () => {
  loadSubmissions()
})

watch(activeTab, () => {
  if (activeTab.value === 'overview') renderStatusChart()
})

async function loadData() {
  loading.value = true
  try {
    const [detailResponse, memberResponse, assignmentResponse, notificationResponse] = await Promise.all([
      getCourseDetailApi(courseId.value),
      listCourseMembersApi(courseId.value),
      listAssignmentsApi(courseId.value),
      listCourseNotificationsApi(courseId.value)
    ])
    detail.value = detailResponse.data.data
    members.value = memberResponse.data.data
    assignments.value = assignmentResponse.data.data
    notifications.value = notificationResponse.data.data
    selectedAssignmentId.value = assignments.value[0]?.id
    await Promise.all([loadSubmissions(), loadStatistics()])
    await renderStatusChart()
  } finally {
    loading.value = false
  }
}

async function loadSubmissions() {
  if (!selectedAssignmentId.value) {
    submissions.value = []
    return
  }
  const response = isStudent.value
    ? await listMySubmissionsApi(selectedAssignmentId.value)
    : await listAssignmentSubmissionsApi(selectedAssignmentId.value)
  submissions.value = response.data.data
}

async function loadStatistics() {
  if (!canSeeStats.value) return
  const [statsResponse, gradesResponse] = await Promise.all([
    getCourseStatisticsApi(courseId.value),
    listCourseGradesApi(courseId.value)
  ])
  stats.value = statsResponse.data.data
  grades.value = gradesResponse.data.data
}

async function exportGrades() {
  exporting.value = true
  try {
    const response = await exportCourseGradesApi(courseId.value)
    const url = URL.createObjectURL(response.data)
    const link = document.createElement('a')
    link.href = url
    link.download = `课程成绩-${detail.value?.course.courseCode || courseId.value}.xlsx`
    link.click()
    URL.revokeObjectURL(url)
    ElMessage.success('Excel 已导出')
  } finally {
    exporting.value = false
  }
}

async function openNotification(row: Notification) {
  if (!(row.read === 1 || row.readStatus === 1)) {
    await markNotificationReadApi(row.id)
  }
  router.push('/notifications')
}

function assignmentStatus(assignment: Assignment) {
  const deadline = new Date(assignment.deadline).getTime()
  const diff = deadline - Date.now()
  if (diff > 0 && diff <= 24 * 60 * 60 * 1000) return { label: '即将截止' }
  if (diff <= 0) return { label: '已截止' }
  return { label: '进行中' }
}

function openAssignment(row: Assignment) {
  if (isStudent.value) router.push(`/student/assignments/${row.id}`)
  else if (isAssistant.value) router.push(`/assistant/assignments/${row.id}`)
  else router.push(`/teacher/assignments?courseId=${row.courseId}`)
}

function openSubmit(row: Assignment) {
  router.push(`/student/assignments/${row.id}/submit`)
}

function openQuestions(row: Assignment) {
  router.push(`/teacher/assignments/${row.id}/questions`)
}

function openGrading(row: Assignment) {
  const prefix = isAssistant.value ? '/assistant/grading' : '/teacher/grading'
  router.push(`${prefix}?courseId=${row.courseId}&assignmentId=${row.id}`)
}

function openDuplicates(row: Assignment) {
  const prefix = isAssistant.value ? '/assistant' : '/teacher'
  router.push(`${prefix}/assignments/${row.id}/duplicates`)
}

function openSubmission(row: Submission) {
  if (isStudent.value) router.push(`/student/submissions/${row.id}`)
  else if (isAssistant.value) router.push(`/assistant/submissions/${row.id}`)
  else router.push(`/teacher/submissions/${row.id}`)
}

function goMembersManage() {
  router.push(isAdmin.value ? `/admin/courses/${courseId.value}/members` : `/teacher/courses/${courseId.value}/members`)
}

function syncTabFromRoute() {
  const tab = route.query.tab
  if (typeof tab === 'string' && ['overview', 'members', 'assignments', 'submissions', 'statistics', 'notifications'].includes(tab)) {
    activeTab.value = tab
  }
}

async function renderStatusChart() {
  await nextTick()
  if (!statusChartRef.value) return
  statusChart = statusChart || echarts.init(statusChartRef.value)
  statusChart.setOption({
    tooltip: { trigger: 'item' },
    color: ['#2563eb', '#f59e0b', '#94a3b8'],
    legend: { bottom: 0 },
    series: [{
      name: '作业状态',
      type: 'pie',
      radius: ['48%', '70%'],
      center: ['50%', '43%'],
      data: assignmentStatusDistribution.value.map((item) => ({ name: item.label, value: item.value })),
      label: { formatter: '{b}: {c}' }
    }]
  })
}

function resizeCharts() {
  statusChart?.resize()
}

watch(() => route.query.tab, syncTabFromRoute)

onMounted(() => {
  syncTabFromRoute()
  loadData()
  window.addEventListener('resize', resizeCharts)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeCharts)
  statusChart?.dispose()
  statusChart = null
})
</script>

<template>
  <section class="page-stack" v-loading="loading">
    <section class="course-hero">
      <div class="course-icon">{{ detail?.course.courseName?.slice(0, 1) || '课' }}</div>
      <div class="course-hero-main">
        <div class="course-meta-line">
          <span>{{ detail?.course.courseCode || '课程代码' }}</span>
          <StatusTag :status="detail?.myRole" :label="detail ? (memberRoleLabels[detail.myRole] || detail.myRole) : '成员'" />
        </div>
        <h2>{{ detail?.course.courseName || '课程详情' }}</h2>
        <p>{{ detail?.course.description || '课程空间汇总成员、作业、提交、成绩统计和课程通知。' }}</p>
        <small>负责人：{{ detail?.course.ownerName || '-' }}</small>
      </div>
      <div class="course-hero-actions">
        <el-button @click="router.back()">返回</el-button>
        <el-button v-if="canManage" @click="goMembersManage">课程设置</el-button>
        <el-button v-if="canSeeStats" :loading="exporting" @click="exportGrades">导出数据</el-button>
        <el-button v-if="isTeacher || isAdmin" type="primary" @click="router.push(`/teacher/assignments?courseId=${courseId}`)">发布作业</el-button>
      </div>
    </section>

    <el-tabs v-model="activeTab" class="course-tabs">
      <el-tab-pane label="课程概览" name="overview">
        <div class="stat-grid">
          <StatCard title="成员总数" :value="detail?.memberCount ?? 0" hint="教师、助教与学生" tone="primary" icon="User" />
          <StatCard title="作业总数" :value="detail?.assignmentCount ?? 0" hint="当前课程作业" tone="success" icon="Files" />
          <StatCard title="待批改" :value="detail?.pendingGradingCount ?? 0" hint="需要人工处理" tone="warning" icon="DocumentChecked" />
          <StatCard title="平均完成率" :value="`${detail?.submissionRate ?? 0}%`" hint="按提交统计" tone="purple" icon="TrendCharts" />
        </div>

        <div class="content-grid overview-grid">
          <DataTableCard title="最近活动" description="课程提交、批改和通知形成业务闭环。">
            <div class="activity-list">
              <div v-for="notice in recentActivities" :key="notice.id" class="activity-row">
                <span />
                <div>
                  <strong>{{ notice.title }}</strong>
                  <small>{{ notice.content }} · {{ notice.createdAt }}</small>
                </div>
              </div>
              <EmptyState v-if="recentActivities.length === 0" title="暂无最近活动" description="发布作业、提交或批改后会出现活动记录。" />
            </div>
          </DataTableCard>

          <ChartCard title="作业状态分布" description="按截止时间将作业分为进行中、即将截止和已截止。">
            <div ref="statusChartRef" class="status-chart"></div>
          </ChartCard>
        </div>
      </el-tab-pane>

      <el-tab-pane label="成员管理" name="members">
        <DataTableCard title="课程成员" description="教师负责人和管理员可管理成员，学生和助教只读。">
          <template #toolbar>
            <el-button v-if="canManage" type="primary" @click="goMembersManage">进入成员管理</el-button>
          </template>
          <el-table :data="members" border>
            <el-table-column prop="realName" label="姓名" min-width="120" />
            <el-table-column prop="username" label="账号" min-width="120" />
            <el-table-column label="角色" width="120">
              <template #default="{ row }"><StatusTag :status="row.memberRole" /></template>
            </el-table-column>
            <el-table-column prop="joinedAt" label="加入时间" min-width="170" />
            <el-table-column label="状态" width="100">
              <template #default="{ row }"><StatusTag :label="row.status === 1 ? '正常' : '停用'" /></template>
            </el-table-column>
          </el-table>
        </DataTableCard>
      </el-tab-pane>

      <el-tab-pane label="作业列表" name="assignments">
        <DataTableCard title="作业列表" description="按类型和截止状态筛选课程作业。">
          <template #toolbar>
            <FilterTabs v-model="assignmentTypeFilter" :options="assignmentTypeOptions" />
            <el-select v-model="assignmentStatusFilter" class="status-select">
              <el-option v-for="item in assignmentStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </template>
          <EmptyState v-if="filteredAssignments.length === 0" title="暂无作业" description="教师发布作业后会显示在这里。" />
          <el-table v-else :data="filteredAssignments" border>
            <el-table-column prop="title" label="标题" min-width="180" />
            <el-table-column label="类型" width="120">
              <template #default="{ row }"><StatusTag :status="row.assignmentType" :label="typeLabels[row.assignmentType as AssignmentType]" /></template>
            </el-table-column>
            <el-table-column label="状态" width="120">
              <template #default="{ row }"><StatusTag :label="assignmentStatus(row).label" /></template>
            </el-table-column>
            <el-table-column prop="deadline" label="截止时间" min-width="170" />
            <el-table-column prop="totalScore" label="满分" width="90" />
            <el-table-column label="操作" width="380" fixed="right">
              <template #default="{ row }">
                <el-button size="small" @click="openAssignment(row)">详情</el-button>
                <el-button v-if="isStudent" size="small" type="primary" @click="openSubmit(row)">提交</el-button>
                <el-button v-if="canReview" size="small" type="primary" @click="openGrading(row)">
                  {{ row.assignmentType === 'TEXT' || row.assignmentType === 'FILE' ? '批改' : '查看结果' }}
                </el-button>
                <el-button v-if="(isTeacher || isAdmin) && (row.assignmentType === 'SINGLE_CHOICE' || row.assignmentType === 'TRUE_FALSE')" size="small" @click="openQuestions(row)">题目</el-button>
                <el-button v-if="canReview && (row.assignmentType === 'FILE' || row.assignmentType === 'TEXT')" size="small" type="warning" @click="openDuplicates(row)">相似度</el-button>
              </template>
            </el-table-column>
          </el-table>
        </DataTableCard>
      </el-tab-pane>

      <el-tab-pane label="提交情况" name="submissions">
        <DataTableCard title="提交情况" description="查看当前作业下学生提交、迟交、高度相似和自动判分结果。">
          <template #toolbar>
            <el-select v-model="selectedAssignmentId" class="assignment-select" placeholder="选择作业">
              <el-option v-for="assignment in assignments" :key="assignment.id" :label="assignment.title" :value="assignment.id" />
            </el-select>
            <FilterTabs v-model="submissionStatusFilter" :options="submissionStatusOptions" />
          </template>
          <EmptyState v-if="!filteredSubmissions.length" title="暂无提交记录" description="学生提交作业后会显示在这里。" />
          <el-table v-else :data="filteredSubmissions" border>
            <el-table-column prop="studentName" label="学生" min-width="120" />
            <el-table-column prop="assignmentTitle" label="作业" min-width="180" />
            <el-table-column prop="submitTime" label="提交时间" min-width="170" />
            <el-table-column label="状态" width="120">
              <template #default="{ row }"><StatusTag :label="row.finalScore !== null && row.finalScore !== undefined ? '已批改' : '已提交'" /></template>
            </el-table-column>
            <el-table-column label="迟交" width="90">
              <template #default="{ row }"><StatusTag :label="row.late ? '迟交' : '按时'" /></template>
            </el-table-column>
            <el-table-column label="相似度核查" width="130">
              <template #default="{ row }"><StatusTag :label="row.hasSimilarityAlert || row.suspectedDuplicate ? `高度相似 ${row.similarityScore ? Math.round(Number(row.similarityScore) * 100) + '%' : ''}` : '正常'" /></template>
            </el-table-column>
            <el-table-column label="自动判分" width="100">
              <template #default="{ row }">{{ row.autoScore ?? '-' }}</template>
            </el-table-column>
            <el-table-column label="最终成绩" width="100">
              <template #default="{ row }">{{ row.finalScore ?? '-' }}</template>
            </el-table-column>
            <el-table-column label="操作" width="100">
              <template #default="{ row }"><el-button size="small" type="primary" @click="openSubmission(row)">详情</el-button></template>
            </el-table-column>
          </el-table>
        </DataTableCard>
      </el-tab-pane>

      <el-tab-pane label="成绩统计" name="statistics">
        <template v-if="canSeeStats">
          <div class="stat-grid">
            <StatCard title="提交人数" :value="stats?.submissionCount ?? 0" hint="提交记录数" tone="primary" icon="User" />
            <StatCard title="平均分" :value="stats?.averageScore ?? 0" hint="最终成绩均值" tone="success" icon="TrendCharts" />
            <StatCard title="最高分" :value="stats?.highestScore ?? 0" hint="课程最高成绩" tone="purple" icon="DataAnalysis" />
            <StatCard title="迟交率" :value="`${stats?.lateRate ?? 0}%`" hint="迟交占比" tone="warning" icon="Calendar" />
          </div>
          <DataTableCard title="成绩列表" description="教师和管理员可以导出 Excel。">
            <template #toolbar>
              <el-button type="primary" :loading="exporting" @click="exportGrades">导出 Excel</el-button>
              <el-button @click="router.push(isAssistant ? '/assistant/statistics' : '/teacher/statistics')">完整统计图表</el-button>
            </template>
            <el-table :data="grades" border>
              <el-table-column prop="studentNo" label="学号" min-width="110" />
              <el-table-column prop="studentName" label="姓名" min-width="110" />
              <el-table-column prop="assignmentName" label="作业" min-width="180" />
              <el-table-column label="最终成绩" width="100"><template #default="{ row }">{{ row.finalScore ?? '-' }}</template></el-table-column>
              <el-table-column prop="comment" label="评语" min-width="220" show-overflow-tooltip />
            </el-table>
          </DataTableCard>
        </template>
        <EmptyState v-else title="学生端成绩入口" description="学生可在“我的成绩”中查看个人成绩和评语。">
          <template #actions><el-button type="primary" @click="router.push('/student/grades')">我的成绩</el-button></template>
        </EmptyState>
      </el-tab-pane>

      <el-tab-pane label="课程通知" name="notifications">
        <DataTableCard title="课程通知" description="只展示当前课程相关通知，后端按课程权限过滤。">
          <EmptyState v-if="!notifications.length" title="暂无通知" description="课程申请、作业发布、提交、批改等事件会生成通知。" />
          <NotificationList v-else :notifications="notifications" @open="openNotification" @mark-read="openNotification" />
        </DataTableCard>
      </el-tab-pane>
    </el-tabs>
  </section>
</template>

<style scoped>
.course-hero {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto;
  gap: 16px;
  align-items: center;
  padding: 20px;
  background: #fff;
  border: 1px solid var(--app-border);
  border-radius: 14px;
  box-shadow: var(--app-shadow);
}

.course-icon {
  display: grid;
  place-items: center;
  width: 58px;
  height: 58px;
  color: #fff;
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  border-radius: 14px;
  font-size: 24px;
  font-weight: 900;
}

.course-meta-line {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
  color: var(--app-muted);
}

.course-hero-main h2 {
  margin: 0;
  font-size: 22px;
}

.course-hero-main p {
  margin: 8px 0 4px;
  color: #334155;
  line-height: 1.6;
}

.course-hero-main small {
  color: var(--app-muted);
}

.course-hero-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.course-tabs {
  padding: 18px;
  background: #fff;
  border: 1px solid var(--app-border);
  border-radius: 14px;
  box-shadow: var(--app-shadow);
}

.overview-grid {
  margin-top: 18px;
}

.activity-list {
  display: grid;
  gap: 10px;
}

.activity-row {
  display: grid;
  grid-template-columns: auto 1fr;
  gap: 10px;
  align-items: center;
  padding: 12px;
  background: #f8fafc;
  border: 1px solid var(--app-border);
  border-radius: 12px;
}

.activity-row > span {
  width: 8px;
  height: 8px;
  background: var(--app-primary);
  border-radius: 999px;
}

.activity-row strong,
.activity-row small {
  display: block;
}

.activity-row small {
  margin-top: 4px;
  color: var(--app-muted);
}

.status-chart {
  width: 100%;
  height: 240px;
}

.status-select,
.assignment-select {
  width: 220px;
}

@media (max-width: 900px) {
  .course-hero {
    grid-template-columns: 1fr;
  }

  .course-hero-actions {
    justify-content: flex-start;
  }

  .status-select,
  .assignment-select {
    width: 100%;
  }
}
</style>
