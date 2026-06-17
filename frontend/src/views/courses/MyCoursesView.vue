<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getCourseDetailApi, listMyCoursesApi } from '@/api/course'
import CourseCard from '@/components/ui/CourseCard.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import PageHeader from '@/components/ui/PageHeader.vue'
import StatCard from '@/components/ui/StatCard.vue'
import { useAuthStore } from '@/stores/auth'
import type { Course, CourseDetailSummary } from '@/types'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const courses = ref<Course[]>([])
const details = ref<Record<number, CourseDetailSummary>>({})
const loading = ref(false)

const pageTitle = computed(() => {
  if (route.path.startsWith('/admin')) return '课程管理'
  if (route.path.startsWith('/assistant')) return '负责课程'
  return '我的课程'
})

const pageDescription = computed(() => {
  if (route.path.startsWith('/admin')) return '管理员可以查看并进入所有课程，保留最高管理权限。'
  if (route.path.startsWith('/assistant')) return '查看你被授权负责的课程、作业和待批改提交。'
  return '查看你负责或参与的课程，进入课程空间处理作业、提交、批改、统计和通知。'
})

const canApplyCourse = computed(() => authStore.user?.roles.includes('TEACHER'))
const totalAssignments = computed(() => Object.values(details.value).reduce((sum, item) => sum + (item.assignmentCount || 0), 0))
const pendingGrading = computed(() => Object.values(details.value).reduce((sum, item) => sum + (item.pendingGradingCount || 0), 0))
const duplicateCount = computed(() => Object.values(details.value).reduce((sum, item) => sum + (item.duplicateCount || 0), 0))

async function loadCourses() {
  loading.value = true
  try {
    const response = await listMyCoursesApi()
    courses.value = response.data.data
    const pairs = await Promise.all(courses.value.map(async (course) => {
      try {
        const detailResponse = await getCourseDetailApi(course.id)
        return [course.id, detailResponse.data.data] as const
      } catch {
        return [course.id, null] as const
      }
    }))
    details.value = pairs.reduce<Record<number, CourseDetailSummary>>((acc, [id, detail]) => {
      if (detail) acc[id] = detail
      return acc
    }, {})
  } finally {
    loading.value = false
  }
}

function canManage(course: Course) {
  return authStore.user?.roles.includes('ADMIN')
    || (authStore.user?.roles.includes('TEACHER') && course.ownerId === authStore.user.id)
}

function goMembers(course: Course) {
  router.push(authStore.user?.roles.includes('ADMIN') ? `/admin/courses/${course.id}/members` : `/teacher/courses/${course.id}/members`)
}

function goDetail(course: Course) {
  router.push(`/courses/${course.id}`)
}

onMounted(loadCourses)
</script>

<template>
  <section class="page-stack" v-loading="loading">
    <PageHeader :title="pageTitle" :description="pageDescription">
      <template #actions>
        <el-button @click="loadCourses">刷新</el-button>
        <el-button v-if="canApplyCourse" type="primary" @click="router.push('/teacher/applications')">申请新课程</el-button>
      </template>
    </PageHeader>

    <div class="stat-grid">
      <StatCard title="课程数量" :value="courses.length" tone="primary" icon="Reading" />
      <StatCard title="作业总数" :value="totalAssignments" tone="success" icon="Files" />
      <StatCard title="待批改" :value="pendingGrading" tone="warning" icon="DocumentChecked" />
      <StatCard title="高度相似" :value="duplicateCount" tone="danger" icon="Bell" />
    </div>

    <EmptyState
      v-if="!loading && courses.length === 0"
      title="暂无课程"
      description="教师可以先申请创建课程，学生和助教需要等待教师添加到课程。"
    >
      <template #actions>
        <el-button v-if="canApplyCourse" type="primary" @click="router.push('/teacher/applications')">申请新课程</el-button>
      </template>
    </EmptyState>

    <div v-else class="course-grid">
      <CourseCard
        v-for="(course, index) in courses"
        :key="course.id"
        :course="course"
        :detail="details[course.id]"
        :index="index"
        :manageable="canManage(course)"
        @enter="goDetail"
        @members="goMembers"
      />
    </div>
  </section>
</template>
