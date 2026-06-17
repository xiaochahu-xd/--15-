<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Lock, User } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import loginCampusUrl from '@/assets/login-campus.png'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const loading = ref(false)

const form = reactive({
  username: 'admin',
  password: '123456'
})

const accounts = [
  { label: '管理员', username: 'admin' },
  { label: '教师', username: 'teacher01' },
  { label: '助教', username: 'assistant01' },
  { label: '学生', username: 'student01' }
]

function fillAccount(username: string) {
  form.username = username
  form.password = '123456'
}

async function submit() {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  loading.value = true
  try {
    await authStore.login(form.username, form.password)
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/dashboard'
    router.replace(redirect)
    ElMessage.success('登录成功')
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
        <h1>登录</h1>
        <el-form label-position="top" @keyup.enter="submit">
          <el-form-item label="用户名">
            <el-input v-model="form.username" :prefix-icon="User" size="large" />
          </el-form-item>
          <el-form-item label="密码">
            <el-input v-model="form.password" :prefix-icon="Lock" type="password" show-password size="large" />
          </el-form-item>
          <el-button type="primary" size="large" :loading="loading" class="login-button" @click="submit">
            登录系统
          </el-button>
        </el-form>

        <div class="quick-accounts">
          <el-button v-for="account in accounts" :key="account.username" size="small" @click="fillAccount(account.username)">
            {{ account.label }}
          </el-button>
        </div>
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
