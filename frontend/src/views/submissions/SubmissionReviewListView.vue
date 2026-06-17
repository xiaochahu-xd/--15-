<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getAssignmentApi } from '@/api/assignment'
import { listAssignmentSubmissionsApi } from '@/api/submission'
import type { Assignment, Submission } from '@/types'
import PageHeader from '@/components/ui/PageHeader.vue'
import StatCard from '@/components/ui/StatCard.vue'
import DataTableCard from '@/components/ui/DataTableCard.vue'
import StatusTag from '@/components/ui/StatusTag.vue'
import EmptyState from '@/components/ui/EmptyState.vue'

const route = useRoute()
const router = useRouter()
const assignmentId = Number(route.params.assignmentId)
const assignment = ref<Assignment | null>(null)
const submissions = ref<Submission[]>([])
const loading = ref(false)

const detailPrefix = computed(() => route.path.startsWith('/assistant') ? '/assistant/submissions' : '/teacher/submissions')
const gradingPrefix = computed(() => route.path.startsWith('/assistant') ? '/assistant/grading/submissions' : '/teacher/grading/submissions')
const duplicatePath = computed(() => route.path.startsWith('/assistant') ? `/assistant/assignments/${assignmentId}/duplicates` : `/teacher/assignments/${assignmentId}/duplicates`)

const summary = computed(() => ({
  total: submissions.value.length,
  late: submissions.value.filter((item) => item.late).length,
  duplicate: submissions.value.filter((item) => item.suspectedDuplicate).length,
  graded: submissions.value.filter((item) => item.finalScore !== null && item.finalScore !== undefined).length
}))

async function loadData() {
  loading.value = true
  try {
    const [assignmentResponse, submissionsResponse] = await Promise.all([
      getAssignmentApi(assignmentId),
      listAssignmentSubmissionsApi(assignmentId)
    ])
    assignment.value = assignmentResponse.data.data
    submissions.value = submissionsResponse.data.data
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>

<template>
  <section class="page-stack review-page">
    <PageHeader title="提交列表" :description="assignment?.title || '查看该作业的全部学生提交。'">
      <template #actions>
        <el-button @click="loadData">刷新</el-button>
        <el-button type="warning" @click="router.push(duplicatePath)">相似度核查</el-button>
        <el-button @click="router.back()">返回</el-button>
      </template>
    </PageHeader>

    <div class="stat-grid">
      <StatCard title="提交人数" :value="summary.total" hint="当前作业记录" icon="User" tone="primary" />
      <StatCard title="迟交提交" :value="summary.late" hint="规则系统判断" icon="Calendar" tone="warning" />
      <StatCard title="高度相似" :value="summary.duplicate" hint="内容相似度检测" icon="Bell" tone="danger" />
      <StatCard title="已有成绩" :value="summary.graded" hint="自动或人工评分" icon="DocumentChecked" tone="success" />
    </div>

    <DataTableCard title="学生提交" description="教师和被授权助教可以查看并进入批改。">
      <EmptyState v-if="!loading && submissions.length === 0" title="暂无提交" description="学生提交作业后会显示在这里。" />
      <el-table v-else v-loading="loading" :data="submissions" border>
        <el-table-column prop="studentName" label="学生" min-width="120" />
        <el-table-column prop="studentUsername" label="用户名" min-width="120" />
        <el-table-column prop="submitTime" label="提交时间" min-width="170" />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <StatusTag :status="row.status" />
          </template>
        </el-table-column>
        <el-table-column label="迟交" width="90">
          <template #default="{ row }">
            <StatusTag :label="row.late ? '迟交' : '按时'" />
          </template>
        </el-table-column>
        <el-table-column label="自动判分" width="100">
          <template #default="{ row }">{{ row.autoScore ?? '-' }}</template>
        </el-table-column>
        <el-table-column label="最终成绩" width="100">
          <template #default="{ row }">{{ row.finalScore ?? '-' }}</template>
        </el-table-column>
        <el-table-column label="相似度核查" width="130">
          <template #default="{ row }">
            <StatusTag :label="row.hasSimilarityAlert || row.suspectedDuplicate ? `高度相似 ${row.similarityScore ? Math.round(Number(row.similarityScore) * 100) + '%' : ''}` : '正常'" />
          </template>
        </el-table-column>
        <el-table-column prop="fileCount" label="文件数" width="90" />
        <el-table-column prop="content" label="内容摘要" min-width="260" show-overflow-tooltip />
        <el-table-column label="操作" width="170">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="router.push(`${detailPrefix}/${row.id}`)">详情</el-button>
            <el-button type="success" size="small" @click="router.push(`${gradingPrefix}/${row.id}`)">批改</el-button>
          </template>
        </el-table-column>
      </el-table>
    </DataTableCard>
  </section>
</template>
