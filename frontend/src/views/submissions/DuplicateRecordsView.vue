<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getAssignmentApi } from '@/api/assignment'
import { confirmSimilarityRecordApi, ignoreSimilarityRecordApi, listSimilarityRecordsApi } from '@/api/duplicate'
import type { Assignment, DuplicateRecord } from '@/types'
import PageHeader from '@/components/ui/PageHeader.vue'
import StatCard from '@/components/ui/StatCard.vue'
import DataTableCard from '@/components/ui/DataTableCard.vue'
import StatusTag from '@/components/ui/StatusTag.vue'
import EmptyState from '@/components/ui/EmptyState.vue'

const route = useRoute()
const router = useRouter()
const assignmentId = Number(route.params.assignmentId)
const assignment = ref<Assignment | null>(null)
const records = ref<DuplicateRecord[]>([])
const loading = ref(false)

const highSimilarityCount = computed(() => records.value.filter((item) => Number(item.similarityScore || 0) >= 0.9).length)
const involvedSubmissionCount = computed(() => {
  const ids = new Set<number>()
  records.value.forEach((item) => {
    if (item.sourceSubmissionId) ids.add(item.sourceSubmissionId)
    if (item.matchedSubmissionId) ids.add(item.matchedSubmissionId)
    item.submissionIdList?.forEach((id) => ids.add(id))
  })
  return ids.size
})

async function loadData() {
  loading.value = true
  try {
    const [assignmentResponse, recordsResponse] = await Promise.all([
      getAssignmentApi(assignmentId),
      listSimilarityRecordsApi(assignmentId)
    ])
    assignment.value = assignmentResponse.data.data
    records.value = recordsResponse.data.data
  } finally {
    loading.value = false
  }
}

function openSubmission(submissionId?: number) {
  if (!submissionId) return
  const prefix = route.path.startsWith('/assistant') ? '/assistant/submissions' : '/teacher/submissions'
  router.push(`${prefix}/${submissionId}`)
}

async function confirmRecord(row: DuplicateRecord) {
  await confirmSimilarityRecordApi(row.id)
  await loadData()
}

async function ignoreRecord(row: DuplicateRecord) {
  await ignoreSimilarityRecordApi(row.id)
  await loadData()
}

function percent(value?: number) {
  if (value === undefined || value === null) return '-'
  return `${Math.round(Number(value) * 100)}%`
}

function detectionLabel(type?: string) {
  if (type === 'TEXT_SIMILARITY') return '文本相似度'
  if (type === 'EXACT_HASH') return '完全相同文件'
  return type || '-'
}

onMounted(loadData)
</script>

<template>
  <section class="page-stack duplicate-page">
    <PageHeader title="相似度核查" :description="assignment?.title || '按同一作业全班范围进行内容相似度匹配。'">
      <template #actions>
        <el-button @click="loadData">刷新</el-button>
        <el-button @click="router.back()">返回</el-button>
      </template>
    </PageHeader>

    <el-alert
      title="系统只标记高度相似提交并提醒核查，不直接判定作弊，也不会自动判 0 分。"
      type="warning"
      :closable="false"
      show-icon
    />

    <div class="stat-grid">
      <StatCard title="核查记录" :value="records.length" hint="相似度与完全相同文件" icon="Bell" tone="danger" />
      <StatCard title="高度相似" :value="highSimilarityCount" hint="相似度不低于 90%" icon="DocumentChecked" tone="warning" />
      <StatCard title="涉及提交" :value="involvedSubmissionCount" hint="相关提交数量" icon="Files" tone="primary" />
      <StatCard title="处理方式" value="人工核查" hint="确认或忽略提醒" icon="EditPen" tone="purple" />
    </div>

    <DataTableCard title="相似度核查记录" description="同一作业内不同学生提交内容相似度达到阈值时生成记录。">
      <EmptyState v-if="!loading && records.length === 0" title="暂无相似度提醒" description="没有检测到达到阈值的高度相似提交。" />
      <el-table v-else v-loading="loading" :data="records" border>
        <el-table-column label="学生" min-width="160">
          <template #default="{ row }">
            {{ row.sourceStudentName || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="匹配学生" min-width="160">
          <template #default="{ row }">
            {{ row.matchedStudentName || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="相似度" width="120">
          <template #default="{ row }">
            <StatusTag :label="Number(row.similarityScore || 0) >= 0.9 ? `高度相似 ${percent(row.similarityScore)}` : percent(row.similarityScore)" />
          </template>
        </el-table-column>
        <el-table-column label="检测类型" width="130">
          <template #default="{ row }">{{ detectionLabel(row.detectionType) }}</template>
        </el-table-column>
        <el-table-column prop="detectedAt" label="检测时间" min-width="170" />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <StatusTag :status="row.status" />
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="说明" min-width="180" show-overflow-tooltip />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openSubmission(row.sourceSubmissionId || row.submissionIdList?.[0])">查看</el-button>
            <el-button size="small" type="primary" @click="confirmRecord(row)">确认</el-button>
            <el-button size="small" type="warning" @click="ignoreRecord(row)">忽略</el-button>
          </template>
        </el-table-column>
      </el-table>
    </DataTableCard>
  </section>
</template>
