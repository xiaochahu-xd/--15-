<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getSubmissionApi } from '@/api/submission'
import type { SubmissionDetail } from '@/types'
import PageHeader from '@/components/ui/PageHeader.vue'
import StatCard from '@/components/ui/StatCard.vue'
import DataTableCard from '@/components/ui/DataTableCard.vue'
import StatusTag from '@/components/ui/StatusTag.vue'
import EmptyState from '@/components/ui/EmptyState.vue'

const route = useRoute()
const router = useRouter()
const submissionId = Number(route.params.submissionId)
const submission = ref<SubmissionDetail | null>(null)
const loading = ref(false)

async function loadData() {
  loading.value = true
  try {
    const response = await getSubmissionApi(submissionId)
    submission.value = response.data.data
  } finally {
    loading.value = false
  }
}

onMounted(loadData)

function percent(value?: number) {
  if (value === undefined || value === null) return '-'
  return `${Math.round(Number(value) * 100)}%`
}
</script>

<template>
  <section class="page-stack submission-detail" v-loading="loading">
    <PageHeader title="提交详情" :description="submission ? `${submission.courseName} · ${submission.assignmentTitle}` : '查看提交内容和规则判定结果。'">
      <template #actions>
        <el-button @click="router.back()">返回</el-button>
      </template>
    </PageHeader>

    <template v-if="submission">
      <div class="stat-grid">
        <StatCard title="学生" :value="submission.studentName" :hint="submission.studentUsername" icon="User" tone="primary" />
        <StatCard title="提交时间" :value="submission.submitTime" :hint="submission.late ? '迟交' : '按时'" icon="Calendar" :tone="submission.late ? 'warning' : 'success'" />
        <StatCard title="自动判分" :value="submission.autoScore ?? '-'" hint="规则系统结果" icon="DocumentChecked" tone="purple" />
        <StatCard title="最终成绩" :value="submission.finalScore ?? '-'" :hint="submission.status" icon="DataAnalysis" tone="success" />
        <StatCard title="相似度核查" :value="submission.hasSimilarityAlert || submission.suspectedDuplicate ? '已进入核查' : '未触发'" :hint="submission.similarityScore ? `最高 ${percent(submission.similarityScore)}` : '仅教师可查看明细'" icon="Bell" :tone="submission.hasSimilarityAlert || submission.suspectedDuplicate ? 'danger' : 'info'" />
      </div>

      <section class="app-card content-block">
        <div class="toolbar-row">
          <h3>提交内容</h3>
          <div class="status-row">
            <StatusTag :label="submission.late ? '迟交' : '按时'" />
            <StatusTag :label="submission.hasSimilarityAlert || submission.suspectedDuplicate ? '已进入教师核查' : '正常'" />
            <StatusTag :status="submission.status" />
          </div>
        </div>
        <pre>{{ submission.content || '无文本内容' }}</pre>
      </section>

      <DataTableCard title="文件记录" description="文件正文会尽量参与同一作业全班范围的相似度匹配；Hash 仅作为完全相同文件的辅助检测。">
        <EmptyState v-if="submission.files.length === 0" title="暂无文件" description="该提交没有上传文件。" />
        <el-table v-else :data="submission.files" border>
          <el-table-column prop="fileName" label="文件名" min-width="180" />
          <el-table-column prop="filePath" label="文件路径" min-width="280" show-overflow-tooltip />
          <el-table-column prop="fileSize" label="大小" width="110" />
          <el-table-column prop="fileHash" label="SHA-256 Hash" min-width="260" show-overflow-tooltip />
          <el-table-column prop="uploadedAt" label="上传时间" min-width="170" />
        </el-table>
      </DataTableCard>
    </template>
  </section>
</template>

<style scoped>
.content-block {
  display: grid;
  gap: 14px;
}

.content-block h3 {
  margin: 0;
  font-size: 18px;
}

.status-row {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.content-block pre {
  min-height: 120px;
  margin: 0;
  padding: 14px;
  white-space: pre-wrap;
  word-break: break-word;
  background: #f8fafc;
  border: 1px solid var(--app-border);
  border-radius: 10px;
  font-family: inherit;
}
</style>
