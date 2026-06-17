<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Lock, User } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import loginCampusUrl from '@/assets/login-campus.png'
import type { RoleCode } from '@/types'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const loading = ref(false)
const mode = ref<'login' | 'register'>('login')
const errorMessage = ref('')

const form = reactive({
  username: 'admin',
  password: '123456',
  confirmPassword: '',
  realName: '',
  roleCode: 'STUDENT' as Extract<RoleCode, 'TEACHER' | 'STUDENT'>,
  email: '',
  phone: ''
})

const accounts = [
  { label: '管理员', username: 'admin' },
  { label: '教师', username: 'teacher01' },
  { label: '助教', username: 'assistant01' },
  { label: '学生', username: 'student01' }
]

const title = computed(() => (mode.value === 'login' ? '登录' : '注册账号'))
const submitText = computed(() => (mode.value === 'login' ? '登录系统' : '注册并进入系统'))

function fillAccount(username: string) {
  mode.value = 'login'
  errorMessage.value = ''
  form.username = username
  form.password = '123456'
}

function switchMode(nextMode: 'login' | 'register') {
  mode.value = nextMode
  errorMessage.value = ''
  if (nextMode === 'login') {
    form.username = 'admin'
    form.password = '123456'
    return
  }
  form.username = ''
  form.password = ''
  form.confirmPassword = ''
  form.realName = ''
  form.roleCode = 'STUDENT'
  form.email = ''
  form.phone = ''
}

function getErrorMessage(error: unknown, fallback: string) {
  const responseMessage = (error as { response?: { data?: { message?: string } } }).response?.data?.message
  return responseMessage || fallback
}

async function submit() {
  errorMessage.value = ''
  if (!form.username || !form.password) {
    errorMessage.value = '请输入用户名和密码'
    return
  }
  if (mode.value === 'register') {
    if (!form.realName) {
      errorMessage.value = '请输入姓名'
      return
    }
    if (form.password.length < 6) {
      errorMessage.value = '密码长度至少 6 位'
      return
    }
    if (form.password !== form.confirmPassword) {
      errorMessage.value = '两次输入的密码不一致'
      return
    }
  }
  loading.value = true
  try {
    if (mode.value === 'login') {
      await authStore.login(form.username, form.password)
    } else {
      await authStore.register({
        username: form.username,
        password: form.password,
        realName: form.realName,
        roleCode: form.roleCode,
        email: form.email || undefined,
        phone: form.phone || undefined
      })
    }
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/dashboard'
    router.replace(redirect)
    ElMessage.success(mode.value === 'login' ? '登录成功' : '注册成功')
  } catch (error) {
    errorMessage.value = getErrorMessage(error, mode.value === 'login' ? '用户名或密码错误' : '注册失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <main class="login-page">
    <section class="login-visual">
      <img class="login-visual-image" :src="loginCampusUrl" alt="" />
      <div class="system-name">智能课程作业提交与批改管理系统</div>
      <div class="system-meta">软件体系结构课程实践</div>
    </section>

    <section class="login-panel">
      <div class="login-box">
        <h1>{{ title }}</h1>
        <div class="mode-switch">
          <button :class="{ active: mode === 'login' }" type="button" @click="switchMode('login')">登录</button>
          <button :class="{ active: mode === 'register' }" type="button" @click="switchMode('register')">注册</button>
        </div>
        <el-form label-position="top" @keyup.enter="submit">
          <el-form-item label="用户名">
            <el-input v-model.trim="form.username" :prefix-icon="User" size="large" placeholder="请输入用户名" />
          </el-form-item>
          <el-form-item v-if="mode === 'register'" label="姓名">
            <el-input v-model.trim="form.realName" :prefix-icon="User" size="large" placeholder="请输入真实姓名" />
          </el-form-item>
          <el-form-item v-if="mode === 'register'" label="注册身份">
            <el-radio-group v-model="form.roleCode" class="role-options">
              <el-radio-button label="STUDENT">学生</el-radio-button>
              <el-radio-button label="TEACHER">教师</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="密码">
            <el-input v-model="form.password" :prefix-icon="Lock" type="password" show-password size="large" placeholder="请输入密码" />
          </el-form-item>
          <el-form-item v-if="mode === 'register'" label="确认密码">
            <el-input v-model="form.confirmPassword" :prefix-icon="Lock" type="password" show-password size="large" placeholder="请再次输入密码" />
          </el-form-item>
          <el-form-item v-if="mode === 'register'" label="邮箱">
            <el-input v-model.trim="form.email" size="large" placeholder="可选" />
          </el-form-item>
          <p v-if="errorMessage" class="form-error">{{ errorMessage }}</p>
          <el-button type="primary" size="large" :loading="loading" class="login-button" @click="submit">
            {{ submitText }}
          </el-button>
        </el-form>

        <div v-if="mode === 'login'" class="quick-accounts">
          <el-button v-for="account in accounts" :key="account.username" size="small" @click="fillAccount(account.username)">
            {{ account.label }}
          </el-button>
        </div>
        <button v-else class="plain-link" type="button" @click="switchMode('login')">已有账号，返回登录</button>
      </div>
    </section>
  </main>
</template>

<style scoped>
.login-page {
  display: grid;
  grid-template-columns: minmax(420px, 1fr) 460px;
  width: 100%;
  min-height: 100vh;
  background: #f4f6f8;
}

.login-visual {
  position: relative;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  min-height: 100%;
  padding: 72px;
  color: #fff;
  background: #14243d;
}

.login-visual::before {
  position: absolute;
  inset: 0;
  z-index: 1;
  content: "";
  background:
    linear-gradient(120deg, rgba(12, 24, 43, 0.88), rgba(22, 68, 111, 0.58)),
    linear-gradient(0deg, rgba(12, 24, 43, 0.58), rgba(12, 24, 43, 0.08) 42%);
}

.login-visual-image {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: left center;
  opacity: 0.9;
}

.system-name {
  position: relative;
  z-index: 2;
  max-width: 720px;
  font-size: 44px;
  font-weight: 800;
  line-height: 1.18;
}

.system-meta {
  position: relative;
  z-index: 2;
  margin-top: 18px;
  color: #d8e4f7;
  font-size: 18px;
}

.login-panel {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  background: #fff;
}

.login-box {
  width: 100%;
  max-width: 360px;
}

.login-box h1 {
  margin: 0 0 28px;
  color: #172033;
  font-size: 30px;
}

.mode-switch {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 6px;
  padding: 4px;
  margin-bottom: 22px;
  border: 1px solid #d8e0ed;
  border-radius: 8px;
  background: #f5f7fb;
}

.mode-switch button,
.plain-link {
  border: 0;
  font: inherit;
  cursor: pointer;
}

.mode-switch button {
  height: 34px;
  border-radius: 6px;
  color: #5b667a;
  background: transparent;
}

.mode-switch button.active {
  color: #1f5fe9;
  font-weight: 700;
  background: #fff;
  box-shadow: 0 4px 12px rgba(31, 95, 233, 0.14);
}

.role-options {
  width: 100%;
}

.role-options :deep(.el-radio-button) {
  width: 50%;
}

.role-options :deep(.el-radio-button__inner) {
  width: 100%;
}

.form-error {
  min-height: 22px;
  margin: -2px 0 10px;
  color: #f04438;
  font-size: 14px;
}

.login-button {
  width: 100%;
  margin-top: 8px;
}

.quick-accounts {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
  margin-top: 18px;
}

.quick-accounts .el-button {
  margin-left: 0;
}

.plain-link {
  display: block;
  margin: 18px auto 0;
  color: #1f5fe9;
  background: transparent;
}

@media (max-width: 900px) {
  .login-page {
    grid-template-columns: 1fr;
  }

  .login-visual {
    min-height: 260px;
    padding: 36px;
  }

  .system-name {
    font-size: 30px;
  }

  .login-panel {
    align-items: flex-start;
  }
}
</style>
