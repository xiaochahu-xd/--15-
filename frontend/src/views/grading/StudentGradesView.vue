<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { listAssignmentsApi } from '@/api/assignment'
import { listMyCoursesApi } from '@/api/course'
import { getSubmissionGradeApi } from '@/api/grading'
import { listMySubmissionsApi } from '@/api/submission'
import type { Course, GradingDetail } from '@/types'
import PageHeader from '@/components/ui/PageHeader.vue'
import StatCard from '@/components/ui/StatCard.vue'
import DataTableCard from '@/components/ui/DataTableCard.vue'
import StatusTag from '@/components/ui/StatusTag.vue'
import EmptyState from '@/components/ui/EmptyState.vue'

const router = useRouter()
const courses = ref<Course[]>([])
const grades = ref<GradingDetail[]>([])
const selectedCourseId = ref<number | 'ALL'>('ALL')
const loading = ref(false)

const filteredGrades = computed(() => {
  if (selectedCourseId.value === 'ALL') {
    return grades.value
  }
  return grades.value.filter((item) => item.courseId === selectedCourseId.value)
})

const summary = computed(() => ({
  total: filteredGrades.value.length,
  graded: filteredGrades.value.filter((item) => item.gradeStatus === 'GRADED').length,
  auto: filteredGrades.value.filter((item) => item.gradeStatus === 'AUTO_GRADED').length,
  average: averageScore(filteredGrades.value)
}))

async function loadData() {
  loading.value = true
  try {
    const courseResponse = await listMyCoursesApi()
    courses.value = courseResponse.data.data
    const assignmentResponses = await Promise.all(courses.value.map((course) => listAssignmentsApi(course.id)))
    const assignments = assignmentResponses.flatMap((response) => response.data.data)
    const submissionResponses = await Promise.all(assignments.map((assignment) => listMySubmissionsApi(assignment.id)))
    const submissions = submissionResponses.flatMap((response) => response.data.data)
    const gradeResponses = await Promise.all(submissions.map((submission) => getSubmissionGradeApi(submission.id)))
    grades.value = gradeResponses.map((response) => response.data.data)
  } finally {
    loading.value = false
  }
}

function averageScore(rows: GradingDetail[]) {
  const values = rows.map((item) => item.finalScore).filter((score): score is number => score !== null && score !== undefined)
  if (values.length === 0) return '-'
  return (values.reduce((sum, score) => sum + score, 0) / values.length).toFixed(1)
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

onMounted(loadData)
</script>

<template>
  <section class="page-stack grades-page">
    <PageHeader title="我的成绩" description="查看每次提交的自动判分、人工评分、最终成绩与教师评语。">
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
      <StatCard title="成绩记录" :value="summary.total" hint="当前筛选结果" icon="TrendCharts" tone="primary" />
      <StatCard title="人工批改" :value="summary.graded" hint="教师或助教评分" icon="EditPen" tone="success" />
      <StatCard title="自动判分" :value="summary.auto" hint="单选题 / 判断题" icon="DocumentChecked" tone="purple" />
      <StatCard title="平均分" :value="summary.average" hint="按最终成绩计算" icon="DataAnalysis" tone="warning" />
    </div>

    <DataTableCard title="成绩与评语" description="学生只能查看自己的成绩记录。">
      <EmptyState v-if="!loading && filteredGrades.length === 0" title="暂无成绩" description="提交被批改或自动判分后会显示在这里。" />
      <el-table v-else v-loading="loading" :data="filteredGrades" border>
        <el-table-column prop="courseName" label="课程" min-width="150" />
        <el-table-column prop="assignmentTitle" label="作业" min-width="180" />
        <el-table-column prop="submitTime" label="提交时间" min-width="170" />
        <el-table-column label="迟交" width="90">
          <template #default="{ row }">
            <StatusTag :label="row.late ? '迟交' : '按时'" />
          </template>
        </el-table-column>
        <el-table-column label="自动判分" width="100">
          <template #default="{ row }">{{ row.autoScore ?? '-' }}</template>
        </el-table-column>
        <el-table-column label="最终成绩" width="100">
          <template #default="{ row }">{{ row.finalScore ?? '-' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <StatusTag :label="gradeStatusText(row.gradeStatus)" />
          </template>
        </el-table-column>
        <el-table-column prop="comment" label="评语" min-width="260" show-overflow-tooltip />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button size="small" @click="router.push(`/student/submissions/${row.submissionId}`)">提交详情</el-button>
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
