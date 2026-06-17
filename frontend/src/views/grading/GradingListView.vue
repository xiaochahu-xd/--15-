<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { listAssignmentsApi } from '@/api/assignment'
import { listMyCoursesApi } from '@/api/course'
import { getGradingProgressApi, listGradingListApi } from '@/api/grading'
import DataTableCard from '@/components/ui/DataTableCard.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import FilterTabs from '@/components/ui/FilterTabs.vue'
import PageHeader from '@/components/ui/PageHeader.vue'
import StatCard from '@/components/ui/StatCard.vue'
import StatusTag from '@/components/ui/StatusTag.vue'
import type { Assignment, Course, GradingItem, GradingProgress } from '@/types'

const route = useRoute()
const router = useRouter()
const courses = ref<Course[]>([])
const assignments = ref<Assignment[]>([])
const gradingItems = ref<GradingItem[]>([])
const progress = ref<GradingProgress | null>(null)
const selectedCourseId = ref<number>()
const selectedAssignmentId = ref<number>()
const loading = ref(false)
const listLoading = ref(false)
const statusFilter = ref('ALL')
const keyword = ref('')

const detailPrefix = computed(() => route.path.startsWith('/assistant') ? '/assistant/grading/submissions' : '/teacher/grading/submissions')

const filterOptions = [
  { label: '全部', value: 'ALL' },
  { label: '未批改', value: 'PENDING' },
  { label: '已批改', value: 'GRADED' },
  { label: '迟交', value: 'LATE' },
  { label: '高度相似', value: 'DUPLICATE' },
  { label: '文本作业', value: 'TEXT' },
  { label: '文件作业', value: 'FILE' },
  { label: '客观题', value: 'OBJECTIVE' },
  { label: '自动判分', value: 'AUTO_GRADED' }
]

const filteredItems = computed(() => {
  const kw = keyword.value.trim().toLowerCase()
  return gradingItems.value.filter((item) => {
    if (statusFilter.value === 'LATE' && !item.late) return false
    if (statusFilter.value === 'DUPLICATE' && !(item.hasSimilarityAlert || item.suspectedDuplicate)) return false
    if (statusFilter.value === 'TEXT' && item.assignmentType !== 'TEXT') return false
    if (statusFilter.value === 'FILE' && item.assignmentType !== 'FILE') return false
    if (statusFilter.value === 'OBJECTIVE' && item.assignmentType !== 'SINGLE_CHOICE' && item.assignmentType !== 'TRUE_FALSE') return false
    if (!['ALL', 'LATE', 'DUPLICATE', 'TEXT', 'FILE', 'OBJECTIVE'].includes(statusFilter.value) && item.gradeStatus !== statusFilter.value) return false
    if (!kw) return true
    return `${item.studentName} ${item.studentUsername} ${item.assignmentTitle}`.toLowerCase().includes(kw)
  })
})

const lateCount = computed(() => gradingItems.value.filter((item) => item.late).length)
const duplicateCount = computed(() => gradingItems.value.filter((item) => item.hasSimilarityAlert || item.suspectedDuplicate).length)

watch(selectedCourseId, async () => {
  await loadAssignments()
  await loadProgress()
})

watch(selectedAssignmentId, () => {
  loadGradingList()
})

async function loadCourses() {
  const response = await listMyCoursesApi()
  courses.value = response.data.data
  const queryCourseId = Number(route.query.courseId)
  const matched = courses.value.find((item) => item.id === queryCourseId)
  if (!selectedCourseId.value && courses.value.length > 0) {
    selectedCourseId.value = matched?.id || courses.value[0].id
  }
}

async function loadAssignments() {
  if (!selectedCourseId.value) {
    assignments.value = []
    selectedAssignmentId.value = undefined
    return
  }
  const response = await listAssignmentsApi(selectedCourseId.value)
  assignments.value = response.data.data
  const queryAssignmentId = Number(route.query.assignmentId)
  const matched = assignments.value.find((item) => item.id === queryAssignmentId)
  selectedAssignmentId.value = matched?.id || assignments.value[0]?.id
}

async function loadProgress() {
  if (!selectedCourseId.value) {
    progress.value = null
    return
  }
  const response = await getGradingProgressApi(selectedCourseId.value)
  progress.value = response.data.data
}

async function loadGradingList() {
  if (!selectedAssignmentId.value) {
    gradingItems.value = []
    return
  }
  listLoading.value = true
  try {
    const response = await listGradingListApi(selectedAssignmentId.value)
    gradingItems.value = response.data.data
  } finally {
    listLoading.value = false
  }
}

async function loadData() {
  loading.value = true
  try {
    await loadCourses()
    await loadAssignments()
    await loadProgress()
    await loadGradingList()
  } finally {
    loading.value = false
  }
}

function openDetail(row: GradingItem) {
  router.push(`${detailPrefix.value}/${row.submissionId}`)
}

function gradeStatusText(status: string) {
  const labels: Record<string, string> = {
    PENDING: '待批改',
    AUTO_GRADED: '自动判分',
    GRADED: '已批改',
    RETURNED: '已退回'
  }
  return labels[status] || status
}

function assignmentTypeText(type: string) {
  const labels: Record<string, string> = {
    TEXT: '文本作业',
    FILE: '文件作业',
    SINGLE_CHOICE: '单选题',
    TRUE_FALSE: '判断题'
  }
  return labels[type] || type
}

onMounted(loadData)
</script>

<template>
  <section class="page-stack" v-loading="loading">
    <PageHeader title="待批改列表" description="集中处理文本作业、文件作业和客观题自动判分结果，高度相似提交只提醒人工核查。">
      <template #actions>
        <el-select v-model="selectedCourseId" placeholder="选择课程" class="course-select">
          <el-option v-for="course in courses" :key="course.id" :label="`${course.courseName}（${course.courseCode}）`" :value="course.id" />
        </el-select>
        <el-select v-model="selectedAssignmentId" placeholder="选择作业" class="assignment-select">
          <el-option v-for="assignment in assignments" :key="assignment.id" :label="assignment.title" :value="assignment.id" />
        </el-select>
        <el-button @click="loadData">刷新</el-button>
      </template>
    </PageHeader>

    <div class="stat-grid">
      <StatCard title="提交总数" :value="progress?.submissionCount ?? 0" tone="primary" icon="Files" />
      <StatCard title="已批改" :value="progress?.gradedCount ?? 0" tone="success" icon="DocumentChecked" />
      <StatCard title="迟交" :value="lateCount" tone="warning" icon="Calendar" />
      <StatCard title="高度相似" :value="duplicateCount" tone="danger" icon="Bell" />
    </div>

    <DataTableCard title="批改任务" :description="progress ? `当前课程批改率 ${progress.gradingRate}%` : '请选择课程和作业查看提交列表。'">
      <template #toolbar>
        <FilterTabs v-model="statusFilter" :options="filterOptions" />
        <el-input v-model="keyword" clearable placeholder="搜索学生或作业" class="search-input" />
      </template>

      <EmptyState v-if="!listLoading && filteredItems.length === 0" title="暂无待处理提交" description="学生提交作业后会显示在这里。" />

      <div v-else v-loading="listLoading" class="grading-task-list">
        <article v-for="row in filteredItems" :key="row.submissionId" class="grading-task-card">
          <div class="student-avatar">{{ row.studentName?.slice(0, 1) || '学' }}</div>
          <div class="task-main">
            <div class="task-title-row">
              <strong>{{ row.studentName }} · {{ row.assignmentTitle }}</strong>
              <div class="task-tags">
                <StatusTag :status="row.assignmentType" :label="assignmentTypeText(row.assignmentType)" />
                <StatusTag :status="row.gradeStatus" :label="gradeStatusText(row.gradeStatus)" />
                <StatusTag :label="row.late ? '迟交' : '按时'" />
                <StatusTag :label="row.hasSimilarityAlert || row.suspectedDuplicate ? `高度相似 ${row.similarityScore ? Math.round(Number(row.similarityScore) * 100) + '%' : ''}` : '正常'" />
              </div>
            </div>
            <div class="task-meta-grid">
              <span><em>账号</em>{{ row.studentUsername }}</span>
              <span><em>提交时间</em>{{ row.submitTime }}</span>
              <span><em>自动分</em>{{ row.autoScore ?? '-' }}</span>
              <span><em>最终分</em>{{ row.finalScore ?? '-' }}</span>
              <span><em>最高相似度</em>{{ row.similarityScore ? Math.round(Number(row.similarityScore) * 100) + '%' : '-' }}</span>
            </div>
          </div>
          <div class="task-actions">
            <el-button type="primary" size="small" @click="openDetail(row)">
              {{ row.gradeStatus === 'GRADED' ? '查看批改' : row.assignmentType === 'TEXT' || row.assignmentType === 'FILE' ? '批改' : '查看结果' }}
            </el-button>
          </div>
        </article>
      </div>
    </DataTableCard>
  </section>
</template>

<style scoped>
.course-select,
.assignment-select {
  width: 260px;
}

.search-input {
  width: 240px;
}

.grading-task-list {
  display: grid;
  gap: 12px;
}

.grading-task-card {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto;
  gap: 14px;
  align-items: center;
  padding: 14px;
  background: #f8fafc;
  border: 1px solid var(--app-border);
  border-radius: 14px;
}

.student-avatar {
  display: grid;
  place-items: center;
  width: 42px;
  height: 42px;
  color: #fff;
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  border-radius: 999px;
  font-weight: 800;
}

.task-main {
  display: grid;
  gap: 10px;
  min-width: 0;
}

.task-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.task-title-row strong {
  overflow: hidden;
  font-size: 15px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.task-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.task-meta-grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(120px, 1fr));
  gap: 8px;
}

.task-meta-grid span {
  display: grid;
  gap: 3px;
  min-height: 44px;
  padding: 8px 10px;
  background: #fff;
  border: 1px solid var(--app-border);
  border-radius: 10px;
}

.task-meta-grid em {
  color: var(--app-muted);
  font-size: 12px;
  font-style: normal;
}

.task-actions {
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 860px) {
  .course-select,
  .assignment-select,
  .search-input {
    width: 100%;
  }

  .grading-task-card,
  .task-title-row,
  .task-meta-grid {
    grid-template-columns: 1fr;
    display: grid;
  }

  .task-tags,
  .task-actions {
    justify-content: flex-start;
  }
}
</style>
