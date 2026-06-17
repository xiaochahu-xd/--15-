<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { downloadAssignmentFileApi, getAssignmentApi, listAssignmentFilesApi, listQuestionsApi } from '@/api/assignment'
import type { Assignment, AssignmentFile, AssignmentType, Question } from '@/types'
import PageHeader from '@/components/ui/PageHeader.vue'
import StatCard from '@/components/ui/StatCard.vue'
import DataTableCard from '@/components/ui/DataTableCard.vue'
import StatusTag from '@/components/ui/StatusTag.vue'
import EmptyState from '@/components/ui/EmptyState.vue'

const route = useRoute()
const router = useRouter()
const assignmentId = Number(route.params.assignmentId)
const assignment = ref<Assignment | null>(null)
const questions = ref<Question[]>([])
const assignmentFiles = ref<AssignmentFile[]>([])
const loading = ref(false)

const typeLabels: Record<AssignmentType, string> = {
  TEXT: '文本作业',
  FILE: '文件作业',
  SINGLE_CHOICE: '单选题作业',
  TRUE_FALSE: '判断题作业'
}

const isObjective = computed(() => {
  return assignment.value?.assignmentType === 'SINGLE_CHOICE' || assignment.value?.assignmentType === 'TRUE_FALSE'
})
const isStudentRoute = computed(() => route.path.startsWith('/student'))

async function loadData() {
  loading.value = true
  try {
    const assignmentResponse = await getAssignmentApi(assignmentId)
    assignment.value = assignmentResponse.data.data
    const fileResponse = await listAssignmentFilesApi(assignmentId)
    assignmentFiles.value = fileResponse.data.data
    if (isObjective.value) {
      const questionsResponse = await listQuestionsApi(assignmentId)
      questions.value = questionsResponse.data.data
    }
  } finally {
    loading.value = false
  }
}

async function downloadFile(file: AssignmentFile) {
  const response = await downloadAssignmentFileApi(file.id)
  const url = URL.createObjectURL(response.data)
  const link = document.createElement('a')
  link.href = url
  link.download = file.fileName
  link.click()
  URL.revokeObjectURL(url)
}

function formatSize(size: number) {
  if (size < 1024) return `${size} B`
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`
  return `${(size / 1024 / 1024).toFixed(1)} MB`
}

onMounted(loadData)
</script>

<template>
  <section class="page-stack detail-page" v-loading="loading">
    <PageHeader title="作业详情" :description="assignment?.title || '查看作业要求、截止时间和客观题内容。'">
      <template #actions>
        <el-button v-if="isStudentRoute" type="primary" @click="router.push(`/student/assignments/${assignmentId}/submit`)">
          提交作业
        </el-button>
        <el-button v-if="isStudentRoute" @click="router.push(`/student/assignments/${assignmentId}/submissions/my`)">
          我的提交
        </el-button>
        <el-button @click="router.back()">返回</el-button>
      </template>
    </PageHeader>

    <template v-if="assignment">
      <div class="stat-grid">
        <StatCard title="课程" :value="assignment.courseName" hint="所属课程" icon="Reading" tone="primary" />
        <StatCard title="作业类型" :value="typeLabels[assignment.assignmentType]" hint="提交方式" icon="Files" tone="purple" />
        <StatCard title="满分" :value="assignment.totalScore" hint="评分上限" icon="DataAnalysis" tone="success" />
        <StatCard title="截止时间" :value="assignment.deadline" :hint="assignment.allowLate ? '允许迟交' : '不允许迟交'" icon="Calendar" tone="warning" />
      </div>

      <section class="app-card description-block">
        <div class="toolbar-row">
          <h3>作业说明</h3>
          <StatusTag :status="assignment.status" />
        </div>
        <p>{{ assignment.description || '暂无作业说明。' }}</p>
      </section>

      <DataTableCard v-if="assignment.assignmentType === 'FILE'" title="教师附件" description="教师发布文件作业时上传的要求、模板或参考资料。">
        <EmptyState v-if="assignmentFiles.length === 0" title="暂无附件" description="教师未上传作业附件。" />
        <el-table v-else :data="assignmentFiles" border>
          <el-table-column prop="fileName" label="文件名" min-width="220" show-overflow-tooltip />
          <el-table-column label="大小" width="120">
            <template #default="{ row }">{{ formatSize(row.fileSize) }}</template>
          </el-table-column>
          <el-table-column prop="uploadedByName" label="上传人" width="130" />
          <el-table-column prop="uploadedAt" label="上传时间" min-width="170" />
          <el-table-column label="操作" width="110">
            <template #default="{ row }">
              <el-button type="primary" size="small" @click="downloadFile(row)">下载</el-button>
            </template>
          </el-table-column>
        </el-table>
      </DataTableCard>

      <DataTableCard v-if="isObjective" title="客观题列表" description="单选题和判断题会由规则系统自动判分。">
        <EmptyState v-if="questions.length === 0" title="暂无题目" description="教师添加客观题后会显示在这里。" />
        <el-table v-else :data="questions" border>
          <el-table-column prop="content" label="题干" min-width="240" show-overflow-tooltip />
          <el-table-column prop="options" label="选项" min-width="220" show-overflow-tooltip />
          <el-table-column label="题型" width="110">
            <template #default="{ row }">
              <StatusTag :status="row.questionType" />
            </template>
          </el-table-column>
          <el-table-column prop="score" label="分值" width="90" />
        </el-table>
      </DataTableCard>
    </template>
  </section>
</template>

<style scoped>
.description-block {
  display: grid;
  gap: 12px;
}

.description-block h3 {
  margin: 0;
  font-size: 18px;
}

.description-block p {
  margin: 0;
  color: #334155;
  line-height: 1.7;
}
</style>
