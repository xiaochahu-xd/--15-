<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { listUsersApi } from '@/api/admin'
import type { UserSummary } from '@/types'

const users = ref<UserSummary[]>([])
const loading = ref(false)

async function loadUsers() {
  loading.value = true
  try {
    const response = await listUsersApi()
    users.value = response.data.data
  } finally {
    loading.value = false
  }
}

onMounted(loadUsers)
</script>

<template>
  <section class="page-panel">
    <h2 class="page-title">用户管理</h2>
    <el-table v-loading="loading" :data="users" border>
      <el-table-column prop="username" label="用户名" min-width="120" />
      <el-table-column prop="realName" label="姓名" min-width="120" />
      <el-table-column label="角色" min-width="180">
        <template #default="{ row }">
          <el-tag v-for="role in row.roles" :key="role" class="role-tag">{{ role }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="email" label="邮箱" min-width="180" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
    </el-table>
  </section>
</template>

<style scoped>
.role-tag {
  margin-right: 6px;
}
</style>
