<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  approveCourseApplicationApi,
  listAllCoursesApi,
  listPendingCourseApplicationsApi,
  rejectCourseApplicationApi
} from '@/api/course'
import type { Course, CourseApplication } from '@/types'
import PageHeader from '@/components/ui/PageHeader.vue'
import StatCard from '@/components/ui/StatCard.vue'
import DataTableCard from '@/components/ui/DataTableCard.vue'
import StatusTag from '@/components/ui/StatusTag.vue'
import EmptyState from '@/components/ui/EmptyState.vue'

const pendingApplications = ref<CourseApplication[]>([])
const courses = ref<Course[]>([])
const loading = ref(false)
const router = useRouter()

const summary = computed(() => ({
  pending: pendingApplications.value.length,
  courseCount: courses.value.length,
  active: courses.value.filter((course) => course.status === 'ACTIVE').length,
  teacherCount: new Set(courses.value.map((course) => course.ownerId)).size
}))

async function loadData() {
  loading.value = true
  try {
    const [pendingResponse, coursesResponse] = await Promise.all([
      listPendingCourseApplicationsApi(),
      listAllCoursesApi()
    ])
    pendingApplications.value = pendingResponse.data.data
    courses.value = coursesResponse.data.data
  } finally {
    loading.value = false
  }
}

async function approve(application: CourseApplication) {
  await ElMessageBox.confirm(`确认通过课程“${application.courseName}”的创建申请吗？`, '审批确认', {
    type: 'warning'
  })
  await approveCourseApplicationApi(application.id)
  ElMessage.success('审批通过，课程已创建')
  await loadData()
}

async function reject(application: CourseApplication) {
  const result = await ElMessageBox.prompt(`请输入驳回“${application.courseName}”的原因`, '驳回申请', {
    inputPattern: /\S+/,
    inputErrorMessage: '驳回原因不能为空'
  })
  await rejectCourseApplicationApi(application.id, result.value)
  ElMessage.success('已驳回申请')
  await loadData()
}

onMounted(loadData)
</script>

<template>
  <section class="page-stack approval-page">
    <PageHeader title="课程审批" eyebrow="管理员工作台" description="审核教师课程创建申请，并保留所有课程的最高管理权限。">
      <template #actions>
        <el-button @click="loadData">刷新</el-button>
      </template>
    </PageHeader>

    <div class="stat-grid">
      <StatCard title="待审批申请" :value="summary.pending" hint="需要管理员处理" icon="Finished" tone="warning" />
      <StatCard title="已创建课程" :value="summary.courseCount" hint="平台课程总数" icon="Reading" tone="primary" />
      <StatCard title="正常课程" :value="summary.active" hint="状态可用" icon="Collection" tone="success" />
      <StatCard title="课程负责人" :value="summary.teacherCount" hint="去重统计" icon="User" tone="purple" />
    </div>

    <DataTableCard title="待审批课程申请" description="通过后系统会正式创建课程，并把申请教师设为课程负责人。">
      <template #toolbar>
        <StatusTag :label="`待处理 ${pendingApplications.length}`" />
      </template>
      <EmptyState v-if="!loading && pendingApplications.length === 0" title="暂无待审批申请" description="教师提交课程申请后会显示在这里。" />
      <el-table v-else v-loading="loading" :data="pendingApplications" border>
        <el-table-column prop="courseName" label="课程名称" min-width="150" />
        <el-table-column prop="courseCode" label="课程代码" min-width="130" />
        <el-table-column prop="teacherName" label="申请教师" min-width="120" />
        <el-table-column prop="description" label="说明" min-width="220" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="申请时间" min-width="170" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="approve(row)">通过</el-button>
            <el-button type="danger" size="small" @click="reject(row)">驳回</el-button>
          </template>
        </el-table-column>
      </el-table>
    </DataTableCard>

    <DataTableCard title="已创建课程" description="管理员可进入成员管理，查看或调整任意课程成员。">
      <el-table v-loading="loading" :data="courses" border>
        <el-table-column prop="courseName" label="课程名称" min-width="150" />
        <el-table-column prop="courseCode" label="课程代码" min-width="130" />
        <el-table-column prop="ownerName" label="负责人" min-width="120" />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <StatusTag :status="row.status" />
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" min-width="170" />
        <el-table-column label="操作" width="130">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="router.push(`/admin/courses/${row.id}/members`)">
              成员管理
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </DataTableCard>
  </section>
</template>
