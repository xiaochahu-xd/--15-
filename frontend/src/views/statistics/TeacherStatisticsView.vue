<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { listAssignmentsApi } from '@/api/assignment'
import { listMyCoursesApi } from '@/api/course'
import {
  exportCourseGradesApi,
  getAssignmentStatisticsApi,
  getCourseStatisticsApi,
  listCourseGradesApi
} from '@/api/statistics'
import { useAuthStore } from '@/stores/auth'
import type { Assignment, AssignmentStatistics, Course, CourseStatistics, GradeExportRow } from '@/types'
import PageHeader from '@/components/ui/PageHeader.vue'
import StatCard from '@/components/ui/StatCard.vue'
import ChartCard from '@/components/ui/ChartCard.vue'
import DataTableCard from '@/components/ui/DataTableCard.vue'
import StatusTag from '@/components/ui/StatusTag.vue'
import EmptyState from '@/components/ui/EmptyState.vue'

const courses = ref<Course[]>([])
const authStore = useAuthStore()
const assignments = ref<Assignment[]>([])
const grades = ref<GradeExportRow[]>([])
const courseStats = ref<CourseStatistics | null>(null)
const assignmentStats = ref<AssignmentStatistics | null>(null)
const assignmentStatsList = ref<AssignmentStatistics[]>([])
const selectedCourseId = ref<number>()
const selectedAssignmentId = ref<number>()
const semester = ref('2026 春季学期')
const loading = ref(false)
const exporting = ref(false)

const submitChartRef = ref<HTMLElement>()
const lateChartRef = ref<HTMLElement>()
const scoreChartRef = ref<HTMLElement>()
const averageChartRef = ref<HTMLElement>()

let submitChart: echarts.ECharts | null = null
let lateChart: echarts.ECharts | null = null
let scoreChart: echarts.ECharts | null = null
let averageChart: echarts.ECharts | null = null

const passRate = computed(() => {
  const scored = grades.value.filter((row) => row.finalScore !== undefined && row.finalScore !== null)
  if (scored.length === 0) return '0%'
  const passed = scored.filter((row) => Number(row.finalScore) >= 60).length
  return `${Math.round((passed / scored.length) * 100)}%`
})

watch(selectedCourseId, async () => {
  await loadCourseScopedData()
})

watch(selectedAssignmentId, async () => {
  await loadAssignmentStatistics()
  await renderCharts()
})

async function loadCourses() {
  const response = await listMyCoursesApi()
  courses.value = response.data.data.filter((course) => ['ADMIN', 'OWNER', 'TEACHER', 'ASSISTANT'].includes(course.memberRole))
  if (!selectedCourseId.value && courses.value.length > 0) {
    selectedCourseId.value = courses.value[0].id
  }
}

async function loadCourseScopedData() {
  if (!selectedCourseId.value) return
  loading.value = true
  try {
    const [assignmentResponse, courseStatsResponse, gradesResponse] = await Promise.all([
      listAssignmentsApi(selectedCourseId.value),
      getCourseStatisticsApi(selectedCourseId.value),
      listCourseGradesApi(selectedCourseId.value)
    ])
    assignments.value = assignmentResponse.data.data
    courseStats.value = courseStatsResponse.data.data
    grades.value = gradesResponse.data.data
    if (!assignments.value.some((assignment) => assignment.id === selectedAssignmentId.value)) {
      selectedAssignmentId.value = assignments.value[0]?.id
    }
    assignmentStatsList.value = []
    for (const assignment of assignments.value) {
      const response = await getAssignmentStatisticsApi(assignment.id)
      assignmentStatsList.value.push(response.data.data)
    }
    assignmentStats.value = assignmentStatsList.value.find((item) => item.assignmentId === selectedAssignmentId.value) || null
    await renderCharts()
  } finally {
    loading.value = false
  }
}

async function loadAssignmentStatistics() {
  if (!selectedAssignmentId.value) {
    assignmentStats.value = null
    return
  }
  const cached = assignmentStatsList.value.find((item) => item.assignmentId === selectedAssignmentId.value)
  if (cached) {
    assignmentStats.value = cached
    return
  }
  const response = await getAssignmentStatisticsApi(selectedAssignmentId.value)
  assignmentStats.value = response.data.data
}

async function exportGrades() {
  if (!selectedCourseId.value) return
  exporting.value = true
  try {
    const response = await exportCourseGradesApi(selectedCourseId.value)
    const url = URL.createObjectURL(response.data)
    const link = document.createElement('a')
    link.href = url
    link.download = `${courseStats.value?.courseName || '课程'}-成绩.xlsx`
    link.click()
    URL.revokeObjectURL(url)
    ElMessage.success('Excel 已导出')
  } finally {
    exporting.value = false
  }
}

async function renderCharts() {
  await nextTick()
  renderSubmitChart()
  renderLateChart()
  renderScoreChart()
  renderAverageChart()
}

function renderSubmitChart() {
  if (!submitChartRef.value) return
  submitChart = submitChart || echarts.init(submitChartRef.value)
  const series = buildRecentSubmissionSeries()
  submitChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 34, right: 18, top: 26, bottom: 32 },
    xAxis: {
      type: 'category',
      data: series.labels,
      boundaryGap: false,
      axisTick: { show: false }
    },
    yAxis: { type: 'value', minInterval: 1, splitLine: { lineStyle: { color: '#edf2f7' } } },
    series: [{
      name: '提交数',
      type: 'line',
      smooth: true,
      symbolSize: 7,
      data: series.values,
      lineStyle: { width: 3, color: '#2563eb' },
      itemStyle: { color: '#2563eb' },
      areaStyle: { color: 'rgba(37, 99, 235, 0.12)' }
    }]
  })
}

function renderLateChart() {
  if (!lateChartRef.value || !courseStats.value) return
  lateChart = lateChart || echarts.init(lateChartRef.value)
  lateChart.setOption({
    tooltip: { trigger: 'item' },
    color: ['#f59e0b', '#16a34a'],
    series: [{
      type: 'pie',
      radius: ['55%', '78%'],
      data: [
        { name: '迟交', value: courseStats.value.lateCount },
        { name: '按时', value: Math.max(courseStats.value.submissionCount - courseStats.value.lateCount, 0) }
      ]
    }]
  })
}

function renderScoreChart() {
  if (!scoreChartRef.value || !assignmentStats.value) return
  scoreChart = scoreChart || echarts.init(scoreChartRef.value)
  scoreChart.setOption({
    tooltip: {},
    grid: { left: 32, right: 16, top: 24, bottom: 30 },
    xAxis: { type: 'category', data: assignmentStats.value.scoreDistribution.map((item) => item.label), axisTick: { show: false } },
    yAxis: { type: 'value', minInterval: 1, splitLine: { lineStyle: { color: '#edf2f7' } } },
    series: [{
      type: 'bar',
      data: assignmentStats.value.scoreDistribution.map((item) => item.count),
      barMaxWidth: 34,
      itemStyle: { color: '#2563eb', borderRadius: [8, 8, 0, 0] }
    }]
  })
}

function renderAverageChart() {
  if (!averageChartRef.value) return
  averageChart = averageChart || echarts.init(averageChartRef.value)
  averageChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 34, right: 18, top: 26, bottom: 44 },
    xAxis: {
      type: 'category',
      data: assignmentStatsList.value.map((item) => item.assignmentTitle),
      axisTick: { show: false },
      axisLabel: { overflow: 'truncate', width: 88 }
    },
    yAxis: { type: 'value', max: 100, splitLine: { lineStyle: { color: '#edf2f7' } } },
    series: [{
      name: '平均分',
      type: 'bar',
      data: assignmentStatsList.value.map((item) => item.averageScore),
      barMaxWidth: 36,
      itemStyle: { color: '#16a34a', borderRadius: [8, 8, 0, 0] }
    }]
  })
}

function buildRecentSubmissionSeries() {
  const today = new Date()
  const labels: string[] = []
  const values: number[] = []
  for (let index = 6; index >= 0; index -= 1) {
    const day = new Date(today)
    day.setDate(today.getDate() - index)
    const key = formatDayKey(day)
    labels.push(`${day.getMonth() + 1}/${day.getDate()}`)
    values.push(grades.value.filter((row) => formatDayKey(new Date(row.submitTime)) === key).length)
  }
  return { labels, values }
}

function formatDayKey(day: Date) {
  if (Number.isNaN(day.getTime())) return ''
  return `${day.getFullYear()}-${day.getMonth() + 1}-${day.getDate()}`
}

function resizeCharts() {
  submitChart?.resize()
  lateChart?.resize()
  scoreChart?.resize()
  averageChart?.resize()
}

onMounted(async () => {
  await loadCourses()
  await loadCourseScopedData()
  window.addEventListener('resize', resizeCharts)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeCharts)
  submitChart?.dispose()
  lateChart?.dispose()
  scoreChart?.dispose()
  averageChart?.dispose()
})
</script>

<template>
  <section class="page-stack statistics-page" v-loading="loading">
    <PageHeader title="成绩统计" eyebrow="统计与导出" description="按课程、学期和作业查看提交、迟交、得分与高度相似核查情况，并导出完整成绩 Excel。">
      <template #actions>
        <el-select v-model="semester" placeholder="选择学期" class="semester-select">
          <el-option label="2026 春季学期" value="2026 春季学期" />
          <el-option label="2026 秋季学期" value="2026 秋季学期" />
        </el-select>
        <el-select v-model="selectedCourseId" placeholder="选择课程" class="course-select">
          <el-option
            v-for="course in courses"
            :key="course.id"
            :label="`${course.courseName}（${course.courseCode}）`"
            :value="course.id"
          />
        </el-select>
        <el-select v-model="selectedAssignmentId" placeholder="选择作业" class="assignment-select">
          <el-option v-for="assignment in assignments" :key="assignment.id" :label="assignment.title" :value="assignment.id" />
        </el-select>
        <el-button @click="loadCourseScopedData">刷新</el-button>
        <el-button v-if="authStore.user?.roles.includes('ADMIN') || authStore.user?.roles.includes('TEACHER')" type="primary" :loading="exporting" @click="exportGrades">
          导出 Excel
        </el-button>
      </template>
    </PageHeader>

    <div v-if="courseStats" class="stat-grid statistics-stat-grid">
      <StatCard title="提交人数" :value="courseStats.submissionCount" hint="已形成提交记录" icon="Finished" tone="primary" />
      <StatCard title="平均分" :value="courseStats.averageScore" hint="最终成绩均值" icon="DataAnalysis" tone="success" />
      <StatCard title="最高分" :value="courseStats.highestScore" hint="课程最高成绩" icon="DocumentChecked" tone="primary" />
      <StatCard title="及格率" :value="passRate" hint="最终成绩不低于 60 分" icon="TrendCharts" tone="success" />
      <StatCard title="迟交率" :value="`${courseStats.lateRate}%`" hint="迟交提交占比" icon="Calendar" tone="warning" />
      <StatCard title="高度相似" :value="courseStats.duplicateCount" hint="仅提醒核查，不直接判罚" icon="Bell" tone="danger" />
    </div>

    <EmptyState v-else-if="!loading" title="暂无统计数据" description="请选择有作业和提交记录的课程。" />

    <div class="chart-grid">
      <ChartCard title="最近 7 天提交趋势" description="按提交时间汇总课程提交数量">
        <div ref="submitChartRef" class="chart"></div>
      </ChartCard>
      <ChartCard title="迟交率" description="迟交与按时提交占比">
        <div ref="lateChartRef" class="chart"></div>
      </ChartCard>
      <ChartCard title="成绩分布" description="当前作业成绩区间">
        <div ref="scoreChartRef" class="chart"></div>
      </ChartCard>
      <ChartCard title="各作业平均分" description="不同作业的平均成绩对比">
        <div ref="averageChartRef" class="chart"></div>
      </ChartCard>
    </div>

    <DataTableCard title="成绩明细" description="Excel 导出使用同一批处理数据源。">
      <EmptyState v-if="!loading && grades.length === 0" title="暂无成绩记录" description="学生提交并批改后会显示成绩明细。" />
      <el-table v-else :data="grades" border>
        <el-table-column prop="studentNo" label="学号" min-width="120" />
        <el-table-column prop="studentName" label="姓名" min-width="120" />
        <el-table-column prop="assignmentName" label="作业名称" min-width="180" />
        <el-table-column prop="submitTime" label="提交时间" min-width="170" />
        <el-table-column label="迟交" width="90">
          <template #default="{ row }">
            <StatusTag :label="row.late ? '迟交' : '按时'" />
          </template>
        </el-table-column>
        <el-table-column label="相似度核查" width="120">
          <template #default="{ row }">
            <StatusTag :label="row.suspectedDuplicate ? '高度相似' : '正常'" />
          </template>
        </el-table-column>
        <el-table-column label="自动判分" width="100">
          <template #default="{ row }">{{ row.autoScore ?? '-' }}</template>
        </el-table-column>
        <el-table-column label="人工评分" width="100">
          <template #default="{ row }">{{ row.manualScore ?? '-' }}</template>
        </el-table-column>
        <el-table-column label="最终成绩" width="100">
          <template #default="{ row }">{{ row.finalScore ?? '-' }}</template>
        </el-table-column>
        <el-table-column prop="comment" label="评语" min-width="220" show-overflow-tooltip />
        <el-table-column prop="graderName" label="批改人" min-width="120" />
      </el-table>
    </DataTableCard>
  </section>
</template>

<style scoped>
.semester-select {
  width: 180px;
}

.course-select,
.assignment-select {
  width: 240px;
}

.statistics-stat-grid {
  grid-template-columns: repeat(6, minmax(150px, 1fr));
}

.chart-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(320px, 1fr));
  gap: 16px;
}

.chart {
  height: 240px;
}

@media (max-width: 1280px) {
  .statistics-stat-grid {
    grid-template-columns: repeat(3, minmax(180px, 1fr));
  }
}

@media (max-width: 960px) {
  .semester-select,
  .course-select,
  .assignment-select {
    width: 100%;
  }

  .statistics-stat-grid,
  .chart-grid {
    grid-template-columns: 1fr;
  }
}
</style>
