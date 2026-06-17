<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import { listOperationLogsApi } from '@/api/admin'
import type { OperationLog } from '@/types'

const logs = ref<OperationLog[]>([])
const loading = ref(false)

async function loadLogs() {
  loading.value = true
  try {
    const response = await listOperationLogsApi(80)
    logs.value = response.data.data
  } finally {
    loading.value = false
  }
}

onMounted(loadLogs)
</script>

<template>
  <section class="page-panel">
    <div class="table-head">
      <h2 class="page-title">系统日志</h2>
      <el-button :icon="Refresh" @click="loadLogs">刷新</el-button>
    </div>
    <el-table v-loading="loading" :data="logs" border>
      <el-table-column prop="createdAt" label="时间" min-width="170" />
      <el-table-column prop="username" label="用户" min-width="110" />
      <el-table-column prop="operation" label="操作" min-width="140" />
      <el-table-column prop="result" label="结果" width="100" />
      <el-table-column prop="ip" label="IP" min-width="120" />
      <el-table-column prop="detail" label="详情" min-width="240" />
    </el-table>
  </section>
</template>

<style scoped>
.table-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}

.table-head .page-title {
  margin-bottom: 0;
}
</style>
