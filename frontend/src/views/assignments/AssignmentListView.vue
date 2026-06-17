<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { listAssignmentsApi } from '@/api/assignment'
import { listMyCoursesApi } from '@/api/course'
import type { Assignment, AssignmentType, Course } from '@/types'
import PageHeader from '@/components/ui/PageHeader.vue'
import StatCard from '@/components/ui/StatCard.vue'
import DataTableCard from '@/components/ui/DataTableCard.vue'
import FilterTabs from '@/components/ui/FilterTabs.vue'
import StatusTag from '@/components/ui/StatusTag.vue'
import EmptyState from '@/components/ui/EmptyState.vue'

const route = useRoute()
const router = useRouter()
const courses = ref<Course[]>([])
const assignments = ref<Assignment[]>([])
const selectedCourseId = ref<number | 'ALL'>('ALL')
const typeFilter = ref('ALL')
const loading = ref(false)

const pageTitle = computed(() => route.path.startsWith('/assistant') ? '作业查看' : '我的作业')
const pageDesc = computed(() => route.path.startsWith('/assistant') ? '查看被授权课程中的作业，并进入待批改列表。' : '查看已加入课程中的作业，按类型筛选后进入提交。')
const detailPrefix = computed(() => route.path.startsWith('/assistant') ? '/assistant/assignments' : '/student/assignments')
const isStudentView = computed(() => route.path.startsWith('/student'))
const isAssistantView = computed(() => route.path.startsWith('/assistant'))

const typeFilters = [
  { label: '全部', value: 'ALL' },
  { label: '文本作业', value: 'TEXT' },
  { label: '文件作业', value: 'FILE' },
  { label: '客观题', value: 'OBJECTIVE' }
]

const typeLabels: Record<AssignmentType, string> = {
  TEXT: '文本作业',
  FILE: '文件作业',
  SINGLE_CHOICE: '单选题作业',
  TRUE_FALSE: '判断题作业'
}

const visibleAssignments = computed(() => {
  return assignments.value.filter((item) => {
    const matchCourse = selectedCourseId.value === 'ALL' || item.courseId === selectedCourseId.value
    const matchType = typeFilter.value === 'ALL'
      || item.assignmentType === typeFilter.value
      || (typeFilter.value === 'OBJECTIVE' && ['SINGLE_CHOICE', 'TRUE_FALSE'].includes(item.assignmentType))
    return matchCourse && matchType
  })
})

const summary = computed(() => ({
  total: assignments.value.length,
  text: assignments.value.filter((item) => item.assignmentType === 'TEXT').length,
  file: assignments.value.filter((item) => item.assignmentType === 'FILE').length,
  objective: assignments.value.filter((item) => ['SINGLE_CHOICE', 'TRUE_FALSE'].includes(item.assignmentType)).length
}))

async function loadData() {
  loading.value = true
  try {
    const courseResponse = await listMyCoursesApi()
    courses.value = courseResponse.data.data
    const assignmentResponses = await Promise.all(
      courses.value.map((course) => listAssignmentsApi(course.id))
    )
    assignments.value = assignmentResponses.flatMap((response) => response.data.data)
  } finally {
    loading.value = false
  }
}

function openDetail(row: Assignment) {
  router.push(`${detailPrefix.value}/${row.id}`)
}

function openSubmit(row: Assignment) {
  router.push(`/student/assignments/${row.id}/submit`)
}

function openMySubmissions(row: Assignment) {
  router.push(`/student/assignments/${row.id}/submissions/my`)
}

function openReview(row: Assignment) {
  router.push(`/assistant/grading?courseId=${row.courseId}&assignmentId=${row.id}`)
}

onMounted(loadData)
</script>

<template>
  <section class="page-stack assignment-list-page">
    <PageHeader :title="pageTitle" :description="pageDesc">
      <template #actions>
        <el-select v-model="selectedCourseId" class="course-select">
          <el-option label="全部课程" value="ALL" />
          <el-option
            v-for="course in courses"
            :key="course.id"
            :label="`${course.courseName}（${course.courseCode}）`"
            :value="course.id"
          />
        </el-select>
        <el-button @click="loadData">刷新</el-button>
      </template>
    </PageHeader>

    <div class="stat-grid">
      <StatCard title="全部作业" :value="summary.total" hint="当前可见课程" icon="Files" tone="primary" />
      <StatCard title="文本作业" :value="summary.text" hint="主观文本提交" icon="Notebook" tone="success" />
      <StatCard title="文件作业" :value="summary.file" hint="支持文本提取与相似度检测" icon="Collection" tone="warning" />
      <StatCard title="客观题" :value="summary.objective" hint="单选题 / 判断题" icon="DocumentChecked" tone="purple" />
    </div>

    <DataTableCard title="作业列表" description="学生只能看到自己加入课程的已发布作业；助教只能看到被授权课程作业。">
      <template #toolbar>
        <FilterTabs v-model="typeFilter" :options="typeFilters" />
      </template>
      <EmptyState v-if="!loading && visibleAssignments.length === 0" title="暂无作业" description="教师发布作业后会显示在这里。" />
      <el-table v-else v-loading="loading" :data="visibleAssignments" border>
        <el-table-column prop="title" label="作业标题" min-width="170" />
        <el-table-column prop="courseName" label="课程" min-width="150" />
        <el-table-column label="类型" width="130">
          <template #default="{ row }">
            <StatusTag :status="row.assignmentType" :label="typeLabels[row.assignmentType as AssignmentType]" />
          </template>
        </el-table-column>
        <el-table-column prop="deadline" label="截止时间" min-width="170" />
        <el-table-column prop="totalScore" label="满分" width="90" />
        <el-table-column label="迟交设置" width="110">
          <template #default="{ row }">
            <StatusTag :label="row.allowLate ? '允许迟交' : '不允许迟交'" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="270" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="openDetail(row)">查看</el-button>
            <el-button v-if="isStudentView" type="success" size="small" @click="openSubmit(row)">提交</el-button>
            <el-button v-if="isStudentView" size="small" @click="openMySubmissions(row)">我的提交</el-button>
            <el-button v-if="isAssistantView" type="success" size="small" @click="openReview(row)">批改</el-button>
          </template>
        </el-table-column>
      </el-table>
    </DataTableCard>
  </section>
</template>

<style scoped>
.course-select {
  width: 320px;
}

@media (max-width: 820px) {
  .course-select {
    width: 100%;
  }
}
</style>
