<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  Bell,
  Collection,
  DataAnalysis,
  DocumentAdd,
  EditPen,
  Files,
  Finished,
  House,
  Notebook,
  Reading,
  SwitchButton,
  Tickets,
  TrendCharts,
  User
} from '@element-plus/icons-vue'
import { getUnreadNotificationCountApi } from '@/api/notification'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const unreadCount = ref(0)

const iconMap = {
  Bell,
  Collection,
  DataAnalysis,
  DocumentAdd,
  EditPen,
  Files,
  Finished,
  House,
  Notebook,
  Reading,
  SwitchButton,
  Tickets,
  TrendCharts,
  User
}

const titleMap: Array<[RegExp, string]> = [
  [/^\/dashboard$/, '控制台'],
  [/^\/notifications$/, '通知中心'],
  [/^\/courses\/\d+/, '课程详情'],
  [/^\/admin\/courses$/, '课程管理'],
  [/^\/admin\/course-approvals$/, '课程审批'],
  [/^\/admin\/users$/, '用户管理'],
  [/^\/admin\/logs$/, '系统日志'],
  [/^\/teacher\/courses$/, '我的课程'],
  [/^\/teacher\/applications$/, '课程申请'],
  [/^\/teacher\/assignments/, '作业管理'],
  [/^\/teacher\/grading/, '待批改列表'],
  [/^\/teacher\/statistics$/, '成绩统计'],
  [/^\/assistant\/assignments/, '作业查看'],
  [/^\/assistant\/courses$/, '负责课程'],
  [/^\/assistant\/grading/, '待批改列表'],
  [/^\/assistant\/statistics$/, '成绩查看'],
  [/^\/student\/courses$/, '我的课程'],
  [/^\/student\/assignments/, '我的作业'],
  [/^\/student\/submissions/, '我的提交'],
  [/^\/student\/grades$/, '我的成绩']
]

const activePath = computed(() => route.path)
const currentTitle = computed(() => titleMap.find(([pattern]) => pattern.test(route.path))?.[1] || String(route.meta.title || '控制台'))
const todayText = computed(() => new Intl.DateTimeFormat('zh-CN', {
  year: 'numeric',
  month: '2-digit',
  day: '2-digit',
  weekday: 'short'
}).format(new Date()))

const avatarText = computed(() => authStore.user?.realName?.slice(0, 1) || authStore.user?.username?.slice(0, 1) || '用')

async function loadUnreadCount() {
  if (!authStore.token) return
  const response = await getUnreadNotificationCountApi()
  unreadCount.value = response.data.data
}

function goNotifications() {
  router.push('/notifications')
}

function logout() {
  authStore.logout()
  router.replace('/login')
}

watch(() => route.path, () => {
  loadUnreadCount().catch(() => undefined)
})

onMounted(() => {
  loadUnreadCount().catch(() => undefined)
})
</script>

<template>
  <el-container class="app-shell">
    <el-aside class="sidebar" width="220px">
      <div class="brand">
        <div class="brand-mark">作</div>
        <div>
          <div class="brand-title">作业批改系统</div>
          <div class="brand-subtitle">智能课程作业提交与批改管理系统</div>
        </div>
      </div>

      <el-menu :default-active="activePath" router class="menu">
        <el-menu-item index="/dashboard">
          <el-icon><House /></el-icon>
          <span>控制台</span>
        </el-menu-item>
        <el-menu-item v-for="item in authStore.menus" :key="item.path" :index="item.path">
          <el-icon>
            <component :is="iconMap[item.icon as keyof typeof iconMap]" />
          </el-icon>
          <span>{{ item.title }}</span>
        </el-menu-item>
      </el-menu>

      <div class="user-card">
        <div class="avatar">{{ avatarText }}</div>
        <div>
          <strong>{{ authStore.user?.realName || authStore.user?.username }}</strong>
          <span>{{ authStore.primaryRoleName }}</span>
        </div>
        <el-icon><User /></el-icon>
      </div>
    </el-aside>

    <el-container>
      <el-header class="topbar">
        <div>
          <h1>{{ currentTitle }}</h1>
          <p>{{ authStore.user?.realName }} · {{ authStore.roleNames }} · {{ todayText }}</p>
        </div>
        <div class="topbar-actions">
          <span class="role-pill">{{ authStore.primaryRoleName }}</span>
          <el-badge :value="unreadCount" :hidden="unreadCount === 0">
            <el-button :icon="Bell" circle plain @click="goNotifications" />
          </el-badge>
          <el-button :icon="SwitchButton" plain @click="logout">退出</el-button>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.app-shell {
  min-height: 100vh;
  background: var(--app-bg);
}

.sidebar {
  position: sticky;
  top: 0;
  display: flex;
  flex-direction: column;
  height: 100vh;
  padding: 14px 10px;
  background:
    radial-gradient(circle at 20% 0%, rgba(37, 99, 235, 0.35), transparent 28%),
    linear-gradient(180deg, #0f172a 0%, #10213f 100%);
  border-right: 1px solid rgba(255, 255, 255, 0.08);
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  min-height: 70px;
  padding: 0 8px;
  color: #fff;
}

.brand-mark {
  display: grid;
  place-items: center;
  flex: 0 0 38px;
  width: 38px;
  height: 38px;
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  border-radius: 10px;
  font-size: 18px;
  font-weight: 900;
}

.brand-title {
  font-size: 17px;
  font-weight: 800;
}

.brand-subtitle {
  width: 132px;
  margin-top: 4px;
  color: #cbd5e1;
  font-size: 12px;
  line-height: 1.35;
}

.menu {
  flex: 1;
  margin-top: 12px;
  overflow-y: auto;
  background: transparent;
  border-right: 0;
}

.menu :deep(.el-menu-item) {
  height: 44px;
  margin: 5px 2px;
  color: #dbeafe;
  border-radius: 10px;
}

.menu :deep(.el-menu-item:hover) {
  color: #fff;
  background: rgba(37, 99, 235, 0.35);
}

.menu :deep(.el-menu-item.is-active) {
  color: #fff;
  background: linear-gradient(135deg, #2563eb, #1d4ed8);
}

.user-card {
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: 10px;
  align-items: center;
  min-height: 66px;
  padding: 10px;
  color: #fff;
  background: rgba(15, 23, 42, 0.55);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 14px;
}

.avatar {
  display: grid;
  place-items: center;
  width: 38px;
  height: 38px;
  color: #1d4ed8;
  background: #dbeafe;
  border-radius: 999px;
  font-weight: 800;
}

.user-card strong,
.user-card span {
  display: block;
}

.user-card span {
  margin-top: 3px;
  color: #bfdbfe;
  font-size: 12px;
}

.topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 64px;
  padding: 0 24px;
  background: rgba(255, 255, 255, 0.95);
  border-bottom: 1px solid var(--app-border);
  backdrop-filter: blur(12px);
}

.topbar h1 {
  margin: 0;
  font-size: 20px;
  font-weight: 800;
  color: var(--app-text);
}

.topbar p {
  margin: 5px 0 0;
  color: var(--app-muted);
  font-size: 13px;
}

.topbar-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.role-pill {
  padding: 5px 12px;
  color: var(--app-primary);
  font-size: 13px;
  font-weight: 700;
  background: var(--app-primary-soft);
  border-radius: 999px;
}

.main {
  min-width: 0;
  padding: 20px;
  background: var(--app-bg);
}

@media (max-width: 840px) {
  .sidebar {
    width: 72px !important;
  }

  .brand div:last-child,
  .menu span,
  .user-card div:not(.avatar),
  .user-card > .el-icon,
  .role-pill {
    display: none;
  }

  .topbar {
    padding: 0 14px;
  }

  .main {
    padding: 14px;
  }
}
</style>
