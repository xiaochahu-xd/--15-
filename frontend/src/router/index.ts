import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import type { RoleCode } from '@/types'

declare module 'vue-router' {
  interface RouteMeta {
    requiresAuth?: boolean
    roles?: RoleCode[]
    title?: string
  }
}

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/LoginView.vue'),
      meta: { title: '登录' }
    },
    {
      path: '/',
      component: () => import('@/layouts/AppLayout.vue'),
      redirect: '/dashboard',
      meta: { requiresAuth: true },
      children: [
        {
          path: 'dashboard',
          name: 'dashboard',
          component: () => import('@/views/DashboardView.vue'),
          meta: { requiresAuth: true, title: '控制台' }
        },
        {
          path: '403',
          name: 'forbidden',
          component: () => import('@/views/ForbiddenView.vue'),
          meta: { requiresAuth: true, title: '无权限' }
        },
        {
          path: 'notifications',
          component: () => import('@/views/notifications/NotificationsView.vue'),
          meta: { requiresAuth: true, roles: ['ADMIN', 'TEACHER', 'ASSISTANT', 'STUDENT'], title: '通知中心' }
        },
        {
          path: 'courses/:courseId',
          component: () => import('@/views/courses/CourseDetailView.vue'),
          meta: { requiresAuth: true, roles: ['ADMIN', 'TEACHER', 'ASSISTANT', 'STUDENT'], title: '课程详情' }
        },
        {
          path: 'admin/courses',
          component: () => import('@/views/courses/MyCoursesView.vue'),
          meta: { requiresAuth: true, roles: ['ADMIN'], title: '课程管理' }
        },
        {
          path: 'admin/course-approvals',
          component: () => import('@/views/admin/CourseApprovalsView.vue'),
          meta: { requiresAuth: true, roles: ['ADMIN'], title: '课程审批' }
        },
        {
          path: 'admin/users',
          component: () => import('@/views/admin/UserManagementView.vue'),
          meta: { requiresAuth: true, roles: ['ADMIN'], title: '用户管理' }
        },
        {
          path: 'admin/logs',
          component: () => import('@/views/admin/OperationLogsView.vue'),
          meta: { requiresAuth: true, roles: ['ADMIN'], title: '系统日志' }
        },
        {
          path: 'admin/courses/:courseId/members',
          component: () => import('@/views/courses/CourseMembersView.vue'),
          meta: { requiresAuth: true, roles: ['ADMIN'], title: '课程成员管理' }
        },
        {
          path: 'teacher/courses',
          component: () => import('@/views/courses/MyCoursesView.vue'),
          meta: { requiresAuth: true, roles: ['TEACHER'], title: '我的课程' }
        },
        {
          path: 'teacher/courses/:courseId/members',
          component: () => import('@/views/courses/CourseMembersView.vue'),
          meta: { requiresAuth: true, roles: ['ADMIN', 'TEACHER'], title: '课程成员管理' }
        },
        {
          path: 'teacher/applications',
          component: () => import('@/views/teacher/CourseApplicationView.vue'),
          meta: { requiresAuth: true, roles: ['TEACHER'], title: '课程申请' }
        },
        {
          path: 'teacher/assignments',
          component: () => import('@/views/assignments/TeacherAssignmentsView.vue'),
          meta: { requiresAuth: true, roles: ['ADMIN', 'TEACHER'], title: '作业管理' }
        },
        {
          path: 'teacher/grading',
          component: () => import('@/views/grading/GradingListView.vue'),
          meta: { requiresAuth: true, roles: ['ADMIN', 'TEACHER'], title: '待批改列表' }
        },
        {
          path: 'teacher/grading/submissions/:submissionId',
          component: () => import('@/views/grading/GradingDetailView.vue'),
          meta: { requiresAuth: true, roles: ['ADMIN', 'TEACHER'], title: '批改详情' }
        },
        {
          path: 'teacher/assignments/:assignmentId/questions',
          component: () => import('@/views/assignments/QuestionEditorView.vue'),
          meta: { requiresAuth: true, roles: ['ADMIN', 'TEACHER'], title: '客观题编辑' }
        },
        {
          path: 'teacher/assignments/:assignmentId/submissions',
          component: () => import('@/views/submissions/SubmissionReviewListView.vue'),
          meta: { requiresAuth: true, roles: ['ADMIN', 'TEACHER'], title: '学生提交列表' }
        },
        {
          path: 'teacher/assignments/:assignmentId/duplicates',
          component: () => import('@/views/submissions/DuplicateRecordsView.vue'),
          meta: { requiresAuth: true, roles: ['ADMIN', 'TEACHER'], title: '相似度核查' }
        },
        {
          path: 'teacher/submissions/:submissionId',
          component: () => import('@/views/submissions/SubmissionDetailView.vue'),
          meta: { requiresAuth: true, roles: ['ADMIN', 'TEACHER'], title: '提交详情' }
        },
        {
          path: 'teacher/statistics',
          component: () => import('@/views/statistics/TeacherStatisticsView.vue'),
          meta: { requiresAuth: true, roles: ['ADMIN', 'TEACHER'], title: '成绩统计' }
        },
        {
          path: 'assistant/assignments',
          component: () => import('@/views/assignments/AssignmentListView.vue'),
          meta: { requiresAuth: true, roles: ['ASSISTANT'], title: '作业查看' }
        },
        {
          path: 'assistant/grading',
          component: () => import('@/views/grading/GradingListView.vue'),
          meta: { requiresAuth: true, roles: ['ASSISTANT'], title: '待批改列表' }
        },
        {
          path: 'assistant/grading/submissions/:submissionId',
          component: () => import('@/views/grading/GradingDetailView.vue'),
          meta: { requiresAuth: true, roles: ['ASSISTANT'], title: '批改详情' }
        },
        {
          path: 'assistant/assignments/:assignmentId',
          component: () => import('@/views/assignments/AssignmentDetailView.vue'),
          meta: { requiresAuth: true, roles: ['ASSISTANT'], title: '作业详情' }
        },
        {
          path: 'assistant/assignments/:assignmentId/submissions',
          component: () => import('@/views/submissions/SubmissionReviewListView.vue'),
          meta: { requiresAuth: true, roles: ['ASSISTANT'], title: '待查看提交' }
        },
        {
          path: 'assistant/assignments/:assignmentId/duplicates',
          component: () => import('@/views/submissions/DuplicateRecordsView.vue'),
          meta: { requiresAuth: true, roles: ['ASSISTANT'], title: '相似度核查' }
        },
        {
          path: 'assistant/submissions/:submissionId',
          component: () => import('@/views/submissions/SubmissionDetailView.vue'),
          meta: { requiresAuth: true, roles: ['ASSISTANT'], title: '提交详情' }
        },
        {
          path: 'assistant/courses',
          component: () => import('@/views/courses/MyCoursesView.vue'),
          meta: { requiresAuth: true, roles: ['ASSISTANT'], title: '负责课程' }
        },
        {
          path: 'assistant/statistics',
          component: () => import('@/views/statistics/TeacherStatisticsView.vue'),
          meta: { requiresAuth: true, roles: ['ASSISTANT'], title: '成绩查看' }
        },
        {
          path: 'student/courses',
          component: () => import('@/views/courses/MyCoursesView.vue'),
          meta: { requiresAuth: true, roles: ['STUDENT'], title: '我的课程' }
        },
        {
          path: 'student/assignments',
          component: () => import('@/views/assignments/AssignmentListView.vue'),
          meta: { requiresAuth: true, roles: ['STUDENT'], title: '我的作业' }
        },
        {
          path: 'student/assignments/:assignmentId',
          component: () => import('@/views/assignments/AssignmentDetailView.vue'),
          meta: { requiresAuth: true, roles: ['STUDENT'], title: '作业详情' }
        },
        {
          path: 'student/assignments/:assignmentId/submit',
          component: () => import('@/views/submissions/StudentSubmitView.vue'),
          meta: { requiresAuth: true, roles: ['STUDENT'], title: '提交作业' }
        },
        {
          path: 'student/assignments/:assignmentId/submissions/my',
          component: () => import('@/views/submissions/MySubmissionsView.vue'),
          meta: { requiresAuth: true, roles: ['STUDENT'], title: '我的提交' }
        },
        {
          path: 'student/submissions/:submissionId',
          component: () => import('@/views/submissions/SubmissionDetailView.vue'),
          meta: { requiresAuth: true, roles: ['STUDENT'], title: '提交详情' }
        },
        {
          path: 'student/submissions',
          component: () => import('@/views/submissions/StudentSubmissionsView.vue'),
          meta: { requiresAuth: true, roles: ['STUDENT'], title: '我的提交' }
        },
        {
          path: 'student/grades',
          component: () => import('@/views/grading/StudentGradesView.vue'),
          meta: { requiresAuth: true, roles: ['STUDENT'], title: '我的成绩' }
        },
        {
          path: 'student/notifications',
          redirect: '/notifications'
        }
      ]
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: '/dashboard'
    }
  ]
})

router.beforeEach(async (to) => {
  const authStore = useAuthStore()
  if (to.path === '/login' && authStore.isAuthenticated) {
    if (!authStore.user) {
      await authStore.fetchMe().catch(() => authStore.logout())
    }
    if (authStore.isAuthenticated) {
      return '/dashboard'
    }
  }

  if (!to.meta.requiresAuth) {
    return true
  }

  if (!authStore.token) {
    return {
      path: '/login',
      query: { redirect: to.fullPath }
    }
  }

  if (!authStore.user) {
    try {
      await authStore.fetchMe()
    } catch {
      authStore.logout()
      return {
        path: '/login',
        query: { redirect: to.fullPath }
      }
    }
  }

  if (!authStore.hasAnyRole(to.meta.roles)) {
    return '/403'
  }

  return true
})

export default router
