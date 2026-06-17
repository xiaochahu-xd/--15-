<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { listMyCoursesApi } from '@/api/course'
import { listStudentSubmissionsApi } from '@/api/submission'
import type { Course, StudentSubmissionAggregate } from '@/types'
import PageHeader from '@/components/ui/PageHeader.vue'
import StatCard from '@/components/ui/StatCard.vue'
import DataTableCard from '@/components/ui/DataTableCard.vue'
import FilterTabs from '@/components/ui/FilterTabs.vue'
import StatusTag from '@/components/ui/StatusTag.vue'
import EmptyState from '@/components/ui/EmptyState.vue'

const router = useRouter()
const loading = ref(false)
const submissions = ref<StudentSubmissionAggregate[]>([])
const courses = ref<Course[]>([])
const filterMode = ref('ALL')
const selectedCourseId = ref<number>()
const keyword = ref('')

const filterOptions = [
  { label: '全部', value: 'ALL' },
  { label: '已提交', value: 'SUBMITTED' },
  { label: '迟交', value: 'LATE' },
  { label: '已批改', value: 'GRADED' },
  { label: '相似度核查', value: 'DUPLICATE' },
  { label: '文本', value: 'TEXT' },
  { label: '文件', value: 'FILE' },
  { label: '客观题', value: 'OBJECTIVE' }
]

const summary = computed(() => ({
  total: submissions.value.length,
  late: submissions.value.filter((item) => item.late).length,
  graded: submissions.value.filter((item) => item.gradeStatus === 'GRADED' || (item.finalScore !== null && item.finalScore !== undefined)).length,
  duplicate: submissions.value.filter((item) => item.duplicate).length
}))

async function loadCourses() {
  const response = await listMyCoursesApi()
  courses.value = response.data.data
}

async function loadData() {
  loading.value = true
  try {
    const params = buildParams()
    const response = await listStudentSubmissionsApi(params)
    submissions.value = response.data.data
  } finally {
    loading.value = false
  }
}

function buildParams() {
  const params: Record<string, string | number | undefined> = {
    courseId: selectedCourseId.value,
    keyword: keyword.value.trim() || undefined
  }
  if (['TEXT', 'FILE', 'OBJECTIVE'].includes(filterMode.value)) {
    params.assignmentType = filterMode.value
  } else if (filterMode.value !== 'ALL') {
    params.status = filterMode.value
  }
  return params
}

function typeLabel(type: string) {
  const labels: Record<string, string> = {
    TEXT: '文本作业',
    FILE: '文件作业',
    SINGLE_CHOICE: '单选题',
    TRUE_FALSE: '判断题'
  }
  return labels[type] || type
}

function statusLabel(row: StudentSubmissionAggregate) {
  if (row.duplicate || row.similarityAlert) return '已进入核查'
  if (row.late) return '迟交'
  if (row.gradeStatus === 'GRADED' || (row.finalScore !== null && row.finalScore !== undefined)) return '已批改'
  return row.status || '已提交'
}

watch([filterMode, selectedCourseId], () => loadData())

onMounted(async () => {
  await loadCourses()
  await loadData()
})
</script>

<template>
  <section class="page-stack submissions-page">
    <PageHeader title="我的提交" description="汇总展示提交时间、迟交状态、自动判分、最终成绩、相似度核查状态与教师评语。">
      <template #actions>
        <el-button @click="loadData">刷新</el-button>
      </template>
    </PageHeader>

    <div class="stat-grid">
      <StatCard title="提交记录" :value="summary.total" hint="当前筛选结果" icon="Notebook" tone="primary" />
      <StatCard title="迟交记录" :value="summary.late" hint="规则系统自动判断" icon="Calendar" tone="warning" />
      <StatCard title="已批改" :value="summary.graded" hint="含自动判分" icon="DocumentChecked" tone="success" />
      <StatCard title="相似度核查" :value="summary.duplicate" hint="仅提示教师核查" icon="Bell" tone="danger" />
    </div>

    <DataTableCard title="提交记录" description="学生只能查看自己的提交记录和成绩。">
      <template #toolbar>
        <FilterTabs v-model="filterMode" :options="filterOptions" />
        <el-select v-model="selectedCourseId" clearable placeholder="全部课程" class="course-select">
          <el-option v-for="course in courses" :key="course.id" :label="course.courseName" :value="course.id" />
        </el-select>
        <el-input v-model="keyword" clearable placeholder="搜索课程、作业或文件" class="keyword-input" @keyup.enter="loadData" />
        <el-button @click="loadData">查询</el-button>
      </template>

      <EmptyState v-if="!loading && submissions.length === 0" title="暂无提交记录" description="完成作业提交后会显示在这里。" />

      <el-table v-else v-loading="loading" :data="submissions" border>
        <el-table-column prop="courseName" label="课程" min-width="140" />
        <el-table-column prop="assignmentTitle" label="作业" min-width="180" />
        <el-table-column label="类型" width="120">
          <template #default="{ row }">{{ typeLabel(row.assignmentType) }}</template>
        </el-table-column>
        <el-table-column prop="submitTime" label="提交时间" min-width="170" />
        <el-table-column prop="deadline" label="截止时间" min-width="170" />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <StatusTag :label="statusLabel(row)" />
          </template>
        </el-table-column>
        <el-table-column label="自动判分" width="100">
          <template #default="{ row }">{{ row.autoScore ?? '-' }}</template>
        </el-table-column>
        <el-table-column label="最终成绩" width="100">
          <template #default="{ row }">{{ row.finalScore ?? '-' }}</template>
        </el-table-column>
        <el-table-column prop="fileName" label="文件" min-width="150" show-overflow-tooltip />
        <el-table-column prop="fileHash" label="文件 Hash" min-width="180" show-overflow-tooltip />
        <el-table-column label="相似度核查" width="130">
          <template #default="{ row }">
            <StatusTag :label="row.duplicate || row.similarityAlert ? '已进入核查' : '正常'" />
          </template>
        </el-table-column>
        <el-table-column prop="comment" label="评语" min-width="180" show-overflow-tooltip />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="router.push(`/student/submissions/${row.submissionId}`)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </DataTableCard>
  </section>
</template>

<style scoped>
.course-select {
  width: 220px;
}

.keyword-input {
  width: 260px;
}

@media (max-width: 900px) {
  .course-select,
  .keyword-input {
    width: 100%;
  }
}
</style>
