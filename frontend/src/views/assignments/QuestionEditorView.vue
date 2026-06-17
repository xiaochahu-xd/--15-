<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { addQuestionApi, getAssignmentApi, listQuestionsApi } from '@/api/assignment'
import type { Assignment, Question } from '@/types'

const route = useRoute()
const router = useRouter()
const assignmentId = Number(route.params.assignmentId)
const assignment = ref<Assignment | null>(null)
const questions = ref<Question[]>([])
const loading = ref(false)
const saving = ref(false)

const form = reactive({
  content: '',
  options: 'A. \nB. \nC. \nD. ',
  standardAnswer: 'A',
  score: 10
})

const isObjective = computed(() => {
  return assignment.value?.assignmentType === 'SINGLE_CHOICE' || assignment.value?.assignmentType === 'TRUE_FALSE'
})

const questionType = computed(() => assignment.value?.assignmentType as 'SINGLE_CHOICE' | 'TRUE_FALSE')

async function loadData() {
  loading.value = true
  try {
    const [assignmentResponse, questionsResponse] = await Promise.all([
      getAssignmentApi(assignmentId),
      listQuestionsApi(assignmentId)
    ])
    assignment.value = assignmentResponse.data.data
    questions.value = questionsResponse.data.data
  } finally {
    loading.value = false
  }
}

async function addQuestion() {
  if (!assignment.value || !isObjective.value) {
    return
  }
  if (!form.content.trim() || !form.standardAnswer.trim() || !form.score) {
    ElMessage.warning('请填写题干、标准答案和分值')
    return
  }
  saving.value = true
  try {
    await addQuestionApi(assignmentId, {
      questionType: questionType.value,
      content: form.content.trim(),
      options: questionType.value === 'SINGLE_CHOICE' ? form.options.trim() : '',
      standardAnswer: form.standardAnswer.trim(),
      score: form.score
    })
    ElMessage.success('题目已添加')
    form.content = ''
    form.options = 'A. \nB. \nC. \nD. '
    form.standardAnswer = questionType.value === 'TRUE_FALSE' ? 'TRUE' : 'A'
    form.score = 10
    await loadData()
  } finally {
    saving.value = false
  }
}

onMounted(loadData)
</script>

<template>
  <section class="page-panel question-page">
    <div class="section-head">
      <div>
        <h2 class="page-title">客观题编辑</h2>
        <p class="muted">{{ assignment?.title }}</p>
      </div>
      <el-button @click="router.back()">返回</el-button>
    </div>

    <el-alert
      v-if="assignment && !isObjective"
      title="该作业不是单选题或判断题作业，不需要编辑客观题。"
      type="info"
      :closable="false"
    />

    <el-form v-if="assignment && isObjective" label-position="top" class="question-form">
      <el-form-item label="题干">
        <el-input v-model="form.content" type="textarea" :rows="3" maxlength="2000" show-word-limit />
      </el-form-item>
      <el-form-item v-if="questionType === 'SINGLE_CHOICE'" label="选项">
        <el-input v-model="form.options" type="textarea" :rows="5" maxlength="2000" show-word-limit />
      </el-form-item>
      <div class="form-grid">
        <el-form-item label="标准答案">
          <el-select v-if="questionType === 'TRUE_FALSE'" v-model="form.standardAnswer" class="full-input">
            <el-option label="正确" value="TRUE" />
            <el-option label="错误" value="FALSE" />
          </el-select>
          <el-input v-else v-model="form.standardAnswer" class="full-input" placeholder="例如：A" />
        </el-form-item>
        <el-form-item label="分值">
          <el-input-number v-model="form.score" :min="0.1" :step="1" class="full-input" />
        </el-form-item>
        <el-form-item label="操作">
          <el-button type="primary" :loading="saving" @click="addQuestion">添加题目</el-button>
        </el-form-item>
      </div>
    </el-form>

    <el-table v-loading="loading" :data="questions" border>
      <el-table-column prop="content" label="题干" min-width="240" show-overflow-tooltip />
      <el-table-column prop="options" label="选项" min-width="220" show-overflow-tooltip />
      <el-table-column prop="standardAnswer" label="标准答案" width="120" />
      <el-table-column prop="score" label="分值" width="90" />
    </el-table>
  </section>
</template>

<style scoped>
.question-page {
  display: grid;
  gap: 16px;
  align-content: start;
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.section-head .page-title,
.section-head p {
  margin-bottom: 0;
}

.question-form {
  padding-bottom: 16px;
  border-bottom: 1px solid #e5e7eb;
}

.form-grid {
  display: grid;
  grid-template-columns: 220px 180px 120px;
  gap: 16px;
}

.full-input {
  width: 100%;
}

@media (max-width: 760px) {
  .form-grid {
    grid-template-columns: 1fr;
  }
}
</style>
