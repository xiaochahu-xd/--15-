<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listSubmissionSimilarityRecordsApi } from '@/api/duplicate'
import { getGradingSubmissionDetailApi, returnSubmissionApi, saveGradeApi } from '@/api/grading'
import { downloadFileRecordApi } from '@/api/submission'
import type { DuplicateRecord, FileRecord, GradingDetail } from '@/types'
import PageHeader from '@/components/ui/PageHeader.vue'
import StatCard from '@/components/ui/StatCard.vue'
import DataTableCard from '@/components/ui/DataTableCard.vue'
import StatusTag from '@/components/ui/StatusTag.vue'
import EmptyState from '@/components/ui/EmptyState.vue'

const route = useRoute()
const router = useRouter()
const submissionId = Number(route.params.submissionId)
const detail = ref<GradingDetail | null>(null)
const similarityRecords = ref<DuplicateRecord[]>([])
const loading = ref(false)
const saving = ref(false)

const form = reactive({
  score: 0,
  comment: '',
  returnReason: ''
})

async function loadData() {
  loading.value = true
  try {
    const response = await getGradingSubmissionDetailApi(submissionId)
    detail.value = response.data.data
    const similarityResponse = await listSubmissionSimilarityRecordsApi(submissionId)
    similarityRecords.value = similarityResponse.data.data
    form.score = Number(detail.value.gradeScore ?? detail.value.finalScore ?? detail.value.autoScore ?? 0)
    form.comment = detail.value.comment || ''
    form.returnReason = ''
  } finally {
    loading.value = false
  }
}

function percent(value?: number) {
  if (value === undefined || value === null) return '-'
  return `${Math.round(Number(value) * 100)}%`
}

async function saveGrade() {
  if (!detail.value) return
  if (form.score < 0) {
    ElMessage.warning('分数不能小于 0')
    return
  }
  saving.value = true
  try {
    await saveGradeApi(submissionId, form.score, form.comment.trim())
    ElMessage.success('批改结果已保存')
    await loadData()
  } finally {
    saving.value = false
  }
}

async function returnSubmission() {
  await ElMessageBox.confirm('确认退回该作业让学生修改吗？', '退回确认', {
    type: 'warning'
  })
  saving.value = true
  try {
    await returnSubmissionApi(submissionId, form.returnReason.trim())
    ElMessage.success('作业已退回修改')
    await loadData()
  } finally {
    saving.value = false
  }
}

async function downloadFile(file: FileRecord) {
  const response = await downloadFileRecordApi(file.id)
  const url = URL.createObjectURL(response.data)
  const link = document.createElement('a')
  link.href = url
  link.download = file.fileName
  link.click()
  URL.revokeObjectURL(url)
}

function gradeStatusText(status: string) {
  const labels: Record<string, string> = {
    PENDING: '待批改',
    AUTO_GRADED: '自动判分',
    GRADED: '已批改',
    RETURNED: '已退回'
  }
  return labels[status] || status
}

onMounted(loadData)
</script>

<template>
  <section class="page-stack grading-detail" v-loading="loading">
    <PageHeader title="批改详情" :description="detail ? `${detail.courseName} · ${detail.assignmentTitle}` : '查看提交内容并保存评分。'">
      <template #actions>
        <el-button @click="router.back()">返回</el-button>
        <el-button type="primary" :loading="saving" @click="saveGrade">保存批改</el-button>
      </template>
    </PageHeader>

    <template v-if="detail">
      <el-alert
        v-if="detail.hasSimilarityAlert || detail.suspectedDuplicate"
        :title="`该提交被系统标记为高度相似提交，最高相似度 ${percent(detail.similarityScore)}。系统只提示风险，不自动判定作弊或判 0 分。`"
        type="warning"
        :closable="false"
        show-icon
      />

      <div class="stat-grid">
        <StatCard title="学生" :value="detail.studentName" :hint="detail.studentUsername" icon="User" tone="primary" />
        <StatCard title="提交时间" :value="detail.submitTime" :hint="detail.late ? '迟交' : '按时'" icon="Calendar" :tone="detail.late ? 'warning' : 'success'" />
        <StatCard title="自动判分" :value="detail.autoScore ?? '-'" hint="客观题规则结果" icon="DocumentChecked" tone="purple" />
        <StatCard title="最终成绩" :value="detail.finalScore ?? '-'" :hint="gradeStatusText(detail.gradeStatus)" icon="DataAnalysis" tone="success" />
        <StatCard title="最高相似度" :value="percent(detail.similarityScore)" :hint="detail.hasSimilarityAlert || detail.suspectedDuplicate ? '已进入教师核查' : '未触发提醒'" icon="Bell" :tone="detail.hasSimilarityAlert || detail.suspectedDuplicate ? 'danger' : 'info'" />
      </div>

      <section class="app-card content-block">
        <div class="toolbar-row">
          <h3>提交内容</h3>
          <div class="status-row">
            <StatusTag :label="detail.late ? '迟交' : '按时'" />
            <StatusTag :label="detail.hasSimilarityAlert || detail.suspectedDuplicate ? `高度相似 ${percent(detail.similarityScore)}` : '正常'" />
            <StatusTag :label="gradeStatusText(detail.gradeStatus)" />
          </div>
        </div>
        <pre>{{ detail.content || '无文本内容' }}</pre>
      </section>

      <DataTableCard title="相似度匹配记录" description="仅教师、助教和管理员可查看匹配学生与相似度明细。">
        <EmptyState v-if="similarityRecords.length === 0" title="暂无相似度记录" description="该提交没有达到核查阈值的相似度记录。" />
        <el-table v-else :data="similarityRecords" border>
          <el-table-column prop="sourceStudentName" label="学生" min-width="120" />
          <el-table-column prop="matchedStudentName" label="匹配学生" min-width="120" />
          <el-table-column label="相似度" width="120">
            <template #default="{ row }">
              <StatusTag :label="percent(row.similarityScore)" />
            </template>
          </el-table-column>
          <el-table-column prop="detectedAt" label="检测时间" min-width="170" />
          <el-table-column label="状态" width="110">
            <template #default="{ row }"><StatusTag :status="row.status" /></template>
          </el-table-column>
        </el-table>
      </DataTableCard>

      <DataTableCard title="文件下载" description="文件作业可在此下载并人工核查。">
        <EmptyState v-if="detail.files.length === 0" title="暂无文件" description="该提交没有上传文件。" />
        <el-table v-else :data="detail.files" border>
          <el-table-column prop="fileName" label="文件名" min-width="180" />
          <el-table-column prop="fileSize" label="大小" width="110" />
          <el-table-column prop="fileHash" label="SHA-256 Hash" min-width="260" show-overflow-tooltip />
          <el-table-column prop="uploadedAt" label="上传时间" min-width="170" />
          <el-table-column label="操作" width="100">
            <template #default="{ row }">
              <el-button type="primary" size="small" @click="downloadFile(row)">下载</el-button>
            </template>
          </el-table-column>
        </el-table>
      </DataTableCard>

      <section class="app-card grade-form-card">
        <h3>批改结果</h3>
        <el-form label-position="top" class="grade-form">
          <el-form-item label="分数">
            <el-input-number v-model="form.score" :min="0" :step="1" class="score-input" />
          </el-form-item>
          <el-form-item label="评语">
            <el-input v-model="form.comment" type="textarea" :rows="5" maxlength="1000" show-word-limit />
          </el-form-item>
          <el-form-item label="退回原因">
            <el-input v-model="form.returnReason" type="textarea" :rows="3" maxlength="1000" show-word-limit placeholder="需要退回修改时填写" />
          </el-form-item>
          <div class="form-actions">
            <el-button type="primary" :loading="saving" @click="saveGrade">保存批改</el-button>
            <el-button type="warning" :loading="saving" @click="returnSubmission">退回修改</el-button>
          </div>
        </el-form>
      </section>
    </template>
  </section>
</template>

<style scoped>
.content-block,
.grade-form-card {
  display: grid;
  gap: 14px;
}

.content-block h3,
.grade-form-card h3 {
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

.score-input {
  width: 220px;
}

.form-actions {
  display: flex;
  gap: 10px;
}
</style>
