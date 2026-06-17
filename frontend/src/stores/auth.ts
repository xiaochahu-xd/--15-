import { defineStore } from 'pinia'
import { loginApi, meApi, registerApi, type RegisterPayload } from '@/api/auth'
import type { CurrentUser, MenuItem, RoleCode } from '@/types'

const TOKEN_KEY = 'coursework_token'
const USER_KEY = 'coursework_user'

const roleLabels: Record<RoleCode, string> = {
  ADMIN: '管理员',
  TEACHER: '教师',
  ASSISTANT: '助教',
  STUDENT: '学生'
}

const roleMenus: MenuItem[] = [
  { path: '/admin/courses', title: '课程管理', icon: 'Reading', roles: ['ADMIN'] },
  { path: '/admin/course-approvals', title: '课程审批', icon: 'Finished', roles: ['ADMIN'] },
  { path: '/admin/users', title: '用户管理', icon: 'User', roles: ['ADMIN'] },
  { path: '/admin/logs', title: '系统日志', icon: 'Tickets', roles: ['ADMIN'] },
  { path: '/teacher/courses', title: '我的课程', icon: 'Reading', roles: ['TEACHER'] },
  { path: '/teacher/applications', title: '课程申请', icon: 'DocumentAdd', roles: ['TEACHER'] },
  { path: '/teacher/assignments', title: '作业管理', icon: 'Collection', roles: ['TEACHER'] },
  { path: '/teacher/grading', title: '待批改列表', icon: 'EditPen', roles: ['TEACHER'] },
  { path: '/teacher/statistics', title: '成绩统计', icon: 'DataAnalysis', roles: ['TEACHER'] },
  { path: '/assistant/grading', title: '待批改列表', icon: 'EditPen', roles: ['ASSISTANT'] },
  { path: '/assistant/courses', title: '负责课程', icon: 'Notebook', roles: ['ASSISTANT'] },
  { path: '/assistant/statistics', title: '成绩查看', icon: 'DataAnalysis', roles: ['ASSISTANT'] },
  { path: '/student/courses', title: '我的课程', icon: 'Reading', roles: ['STUDENT'] },
  { path: '/student/assignments', title: '我的作业', icon: 'Files', roles: ['STUDENT'] },
  { path: '/student/submissions', title: '我的提交', icon: 'Notebook', roles: ['STUDENT'] },
  { path: '/student/grades', title: '我的成绩', icon: 'TrendCharts', roles: ['STUDENT'] },
  { path: '/notifications', title: '通知中心', icon: 'Bell', roles: ['ADMIN', 'TEACHER', 'ASSISTANT', 'STUDENT'] }
]

function readUser() {
  const raw = localStorage.getItem(USER_KEY)
  if (!raw) {
    return null
  }
  try {
    return JSON.parse(raw) as CurrentUser
  } catch {
    localStorage.removeItem(USER_KEY)
    return null
  }
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem(TOKEN_KEY) || '',
    user: readUser() as CurrentUser | null
  }),
  getters: {
    isAuthenticated: (state) => Boolean(state.token),
    roleNames: (state) => state.user?.roles.map((role) => roleLabels[role]).join('、') || '',
    primaryRoleName: (state) => roleLabels[state.user?.roles[0] || 'STUDENT'],
    menus: (state) => {
      const roles = state.user?.roles || []
      return roleMenus.filter((item) => item.roles.some((role) => roles.includes(role)))
    }
  },
  actions: {
    async login(username: string, password: string) {
      const response = await loginApi(username, password)
      this.token = response.data.data.token
      this.user = response.data.data.user
      localStorage.setItem(TOKEN_KEY, this.token)
      localStorage.setItem(USER_KEY, JSON.stringify(this.user))
    },
    async register(payload: RegisterPayload) {
      const response = await registerApi(payload)
      this.token = response.data.data.token
      this.user = response.data.data.user
      localStorage.setItem(TOKEN_KEY, this.token)
      localStorage.setItem(USER_KEY, JSON.stringify(this.user))
    },
    async fetchMe() {
      const response = await meApi()
      this.user = response.data.data
      localStorage.setItem(USER_KEY, JSON.stringify(this.user))
      return this.user
    },
    logout() {
      this.token = ''
      this.user = null
      localStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem(USER_KEY)
    },
    hasAnyRole(roles?: RoleCode[]) {
      if (!roles || roles.length === 0) {
        return true
      }
      const currentRoles = this.user?.roles || []
      return roles.some((role) => currentRoles.includes(role))
    }
  }
})
