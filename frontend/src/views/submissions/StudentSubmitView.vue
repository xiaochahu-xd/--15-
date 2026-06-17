<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { downloadAssignmentFileApi, getAssignmentApi, listAssignmentFilesApi, listQuestionsApi } from '@/api/assignment'
import { submitFileApi, submitObjectiveApi, submitTextApi } from '@/api/submission'
import type { Assignment, AssignmentFile, Question } from '@/types'
import PageHeader from '@/components/ui/PageHeader.vue'
import StatCard from '@/components/ui/StatCard.vue'
import StatusTag from '@/components/ui/StatusTag.vue'
import DataTableCard from '@/components/ui/DataTableCard.vue'
import EmptyState from '@/components/ui/EmptyState.vue'

const route = useRoute()
const router = useRouter()
const assignmentId = Number(route.params.assignmentId)
const assignment = ref<Assignment | null>(null)
const questions = ref<Question[]>([])
const assignmentFiles = ref<AssignmentFile[]>([])
const loading = ref(false)
const submitting = ref(false)
const selectedFile = ref<File | null>(null)

const form = reactive({
  content: '',
  answers: {} as Record<string, string>
})

const isObjective = computed(() => {
  return assignment.value?.assignmentType === 'SINGLE_CHOICE' || assignment.value?.assignmentType === 'TRUE_FALSE'
})

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

function parseOptions(options?: string) {
  return (options || '')
    .split(/\r?\n/)
    .map((item) => item.trim())
    .filter(Boolean)
}

function onFileChange(event: Event) {
  const input = event.target as HTMLInputElement
  selectedFile.value = input.files && input.files.length > 0 ? input.files[0] : null
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

async function submit() {
  if (!assignment.value) {
    return
  }
  submitting.value = true
  try {
    if (assignment.value.assignmentType === 'TEXT') {
      if (!form.content.trim()) {
        ElMessage.warning('请填写文本内容')
        return
      }
      await submitTextApi(assignmentId, form.content.trim())
    } else if (assignment.value.assignmentType === 'FILE') {
      if (!selectedFile.value) {
        ElMessage.warning('请选择上传文件')
        return
      }
      await submitFileApi(assignmentId, selectedFile.value, form.content.trim())
    } else {
      const missing = questions.value.some((question) => !form.answers[String(question.id)])
      if (missing) {
        ElMessage.warning('请完成所有题目')
        return
      }
      await submitObjectiveApi(assignmentId, form.answers)
    }
    ElMessage.success('提交成功')
    router.push(`/student/assignments/${assignmentId}/submissions/my`)
  } finally {
    submitting.value = false
  }
}

onMounted(loadData)
</script>

<template>
  <section class="page-stack submit-page" v-loading="loading">
    <PageHeader title="提交作业" :description="assignment?.title || '填写答案并提交，系统会保存提交时间和规则判定结果。'">
      <template #actions>
        <el-button @click="router.back()">返回</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">提交</el-button>
      </template>
    </PageHeader>

    <template v-if="assignment">
      <div class="stat-grid">
        <StatCard title="课程" :value="assignment.courseName" hint="所属课程" icon="Reading" tone="primary" />
        <StatCard title="作业类型" :value="assignment.assignmentType" hint="文本 / 文件 / 客观题" icon="Files" tone="purple" />
        <StatCard title="截止时间" :value="assignment.deadline" :hint="assignment.allowLate ? '允许迟交' : '不允许迟交'" icon="Calendar" tone="warning" />
        <StatCard title="满分" :value="assignment.totalScore" hint="自动或人工批改" icon="DataAnalysis" tone="success" />
      </div>

      <DataTableCard v-if="assignment.assignmentType === 'FILE'" title="教师附件" description="请先下载查看作业要求、模板或参考资料，再提交自己的文件。">
        <EmptyState v-if="assignmentFiles.length === 0" title="暂无附件" description="教师未上传作业附件。" />
        <el-table v-else :data="assignmentFiles" border>
          <el-table-column prop="fileName" label="文件名" min-width="220" show-overflow-tooltip />
          <el-table-column label="大小" width="120">
            <template #default="{ row }">{{ formatSize(row.fileSize) }}</template>
          </el-table-column>
          <el-table-column prop="uploadedByName" label="上传人" width="130" />
          <el-table-column label="操作" width="110">
            <template #default="{ row }">
              <el-button type="primary" size="small" @click="downloadFile(row)">下载</el-button>
            </template>
          </el-table-column>
        </el-table>
      </DataTableCard>

      <section class="app-card">
        <el-form label-position="top" class="submit-form">
          <el-form-item v-if="assignment.assignmentType === 'TEXT'" label="文本内容">
            <el-input v-model="form.content" type="textarea" :rows="10" maxlength="10000" show-word-limit />
          </el-form-item>

          <template v-else-if="assignment.assignmentType === 'FILE'">
            <el-form-item label="补充说明">
              <el-input v-model="form.content" type="textarea" :rows="4" maxlength="10000" show-word-limit />
            </el-form-item>
            <el-form-item label="上传文件">
              <div class="upload-box">
                <input class="file-input" type="file" @change="onFileChange" />
                <span v-if="selectedFile" class="muted">{{ selectedFile.name }} · {{ selectedFile.size }} bytes</span>
                <span v-else class="muted">文件提交会尽量提取正文并参与同一作业全班范围的相似度检测，Hash 仅作为辅助检测。</span>
              </div>
            </el-form-item>
          </template>

          <template v-else-if="isObjective">
            <DataTableCard title="客观题答题" description="提交后系统会按标准答案自动判分。">
              <div class="question-list">
                <div v-for="(question, index) in questions" :key="question.id" class="question-item">
                  <div class="question-title">
                    <strong>{{ index + 1 }}. {{ question.content }}</strong>
                    <StatusTag :status="question.questionType" />
                  </div>
                  <el-radio-group
                    v-if="assignment.assignmentType === 'TRUE_FALSE'"
                    v-model="form.answers[String(question.id)]"
                  >
                    <el-radio label="TRUE">正确</el-radio>
                    <el-radio label="FALSE">错误</el-radio>
                  </el-radio-group>
                  <el-radio-group v-else v-model="form.answers[String(question.id)]" class="choice-group">
                    <el-radio v-for="option in parseOptions(question.options)" :key="option" :label="option.charAt(0).toUpperCase()">
                      {{ option }}
                    </el-radio>
                  </el-radio-group>
                </div>
              </div>
            </DataTableCard>
          </template>

          <div class="form-actions">
            <el-button type="primary" :loading="submitting" @click="submit">提交作业</el-button>
            <el-button @click="router.back()">取消</el-button>
          </div>
        </el-form>
      </section>
    </template>
  </section>
</template>

<style scoped>
.submit-form {
  display: grid;
  gap: 8px;
}

.upload-box {
  display: grid;
  gap: 8px;
  width: 100%;
  padding: 18px;
  background: #f8fafc;
  border: 1px dashed #cbd5e1;
  border-radius: 12px;
}

.file-input {
  display: block;
}

.question-list {
  display: grid;
  gap: 14px;
}

.question-item {
  display: grid;
  gap: 10px;
  padding: 14px;
  background: #f8fafc;
  border: 1px solid var(--app-border);
  border-radius: 10px;
}

.question-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.choice-group {
  display: grid;
  gap: 8px;
}

.form-actions {
  display: flex;
  gap: 10px;
}
</style>
