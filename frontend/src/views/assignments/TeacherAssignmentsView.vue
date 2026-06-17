<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { UploadFile, UploadFiles, UploadUserFile } from 'element-plus'
import {
  createAssignmentApi,
  deleteAssignmentApi,
  listAssignmentsApi,
  updateAssignmentApi,
  uploadAssignmentFileApi,
  type AssignmentPayload
} from '@/api/assignment'
import { listMyCoursesApi } from '@/api/course'
import DataTableCard from '@/components/ui/DataTableCard.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import FilterTabs from '@/components/ui/FilterTabs.vue'
import PageHeader from '@/components/ui/PageHeader.vue'
import StatCard from '@/components/ui/StatCard.vue'
import StatusTag from '@/components/ui/StatusTag.vue'
import type { Assignment, AssignmentType, Course } from '@/types'

const route = useRoute()
const router = useRouter()
const courses = ref<Course[]>([])
const assignments = ref<Assignment[]>([])
const selectedCourseId = ref<number>()
const loading = ref(false)
const saving = ref(false)
const editingAssignment = ref<Assignment | null>(null)
const typeFilter = ref('ALL')
const materialFile = ref<File | null>(null)
const materialFileList = ref<UploadUserFile[]>([])

const form = reactive<AssignmentPayload>({
  title: '',
  description: '',
  assignmentType: 'TEXT',
  deadline: defaultDeadline(),
  totalScore: 100,
  allowLate: false
})

const assignmentTypes: Array<{ label: string; value: AssignmentType }> = [
  { label: '文本作业', value: 'TEXT' },
  { label: '文件作业', value: 'FILE' },
  { label: '单选题作业', value: 'SINGLE_CHOICE' },
  { label: '判断题作业', value: 'TRUE_FALSE' }
]

const typeLabels: Record<AssignmentType, string> = {
  TEXT: '文本作业',
  FILE: '文件作业',
  SINGLE_CHOICE: '单选题',
  TRUE_FALSE: '判断题'
}

const filterOptions = [
  { label: '全部', value: 'ALL' },
  { label: '文本作业', value: 'TEXT' },
  { label: '文件作业', value: 'FILE' },
  { label: '单选题', value: 'SINGLE_CHOICE' },
  { label: '判断题', value: 'TRUE_FALSE' }
]

const editableCourses = computed(() => {
  return courses.value.filter((course) => ['ADMIN', 'OWNER', 'TEACHER'].includes(course.memberRole))
})

const filteredAssignments = computed(() => {
  if (typeFilter.value === 'ALL') return assignments.value
  return assignments.value.filter((assignment) => assignment.assignmentType === typeFilter.value)
})

const objectiveCount = computed(() => assignments.value.filter((item) => item.assignmentType === 'SINGLE_CHOICE' || item.assignmentType === 'TRUE_FALSE').length)
const expiredCount = computed(() => assignments.value.filter((item) => assignmentStatus(item).label === '已截止').length)

watch(selectedCourseId, () => {
  loadAssignments()
})

watch(() => form.assignmentType, (type) => {
  if (type !== 'FILE') {
    clearMaterialFile()
  }
})

async function loadCourses() {
  const response = await listMyCoursesApi()
  courses.value = response.data.data
  if (!selectedCourseId.value && editableCourses.value.length > 0) {
    const queryCourseId = Number(route.query.courseId)
    const matched = editableCourses.value.find((course) => course.id === queryCourseId)
    selectedCourseId.value = matched?.id || editableCourses.value[0].id
  }
}

async function loadAssignments() {
  if (!selectedCourseId.value) {
    assignments.value = []
    return
  }
  loading.value = true
  try {
    const response = await listAssignmentsApi(selectedCourseId.value)
    assignments.value = response.data.data
  } finally {
    loading.value = false
  }
}

function resetForm() {
  editingAssignment.value = null
  form.title = ''
  form.description = ''
  form.assignmentType = 'TEXT'
  form.deadline = defaultDeadline()
  form.totalScore = 100
  form.allowLate = false
  clearMaterialFile()
}

function editAssignment(row: Assignment) {
  editingAssignment.value = row
  form.title = row.title
  form.description = row.description || ''
  form.assignmentType = row.assignmentType
  form.deadline = row.deadline
  form.totalScore = Number(row.totalScore)
  form.allowLate = row.allowLate
  clearMaterialFile()
}

function handleMaterialFileChange(file: UploadFile, fileList: UploadFiles) {
  materialFile.value = file.raw as File
  materialFileList.value = fileList.slice(-1)
}

function handleMaterialFileRemove() {
  clearMaterialFile()
}

function clearMaterialFile() {
  materialFile.value = null
  materialFileList.value = []
}

async function saveAssignment() {
  if (!selectedCourseId.value) {
    ElMessage.warning('请选择课程')
    return
  }
  if (!form.title.trim() || !form.deadline || !form.totalScore) {
    ElMessage.warning('请填写标题、截止时间和满分')
    return
  }
  saving.value = true
  try {
    const payload = {
      ...form,
      title: form.title.trim(),
      description: form.description?.trim()
    }
    let savedAssignment: Assignment
    if (editingAssignment.value) {
      const response = await updateAssignmentApi(editingAssignment.value.id, payload)
      savedAssignment = response.data.data
    } else {
      const response = await createAssignmentApi(selectedCourseId.value, payload)
      savedAssignment = response.data.data
    }
    if (form.assignmentType === 'FILE' && materialFile.value) {
      await uploadAssignmentFileApi(savedAssignment.id, materialFile.value)
      ElMessage.success(editingAssignment.value ? '作业已修改，附件已上传' : '作业已发布，附件已上传')
    } else {
      ElMessage.success(editingAssignment.value ? '作业已修改' : '作业已发布')
    }
    resetForm()
    await loadAssignments()
  } finally {
    saving.value = false
  }
}

async function removeAssignment(row: Assignment) {
  await ElMessageBox.confirm(`确认删除作业“${row.title}”吗？`, '删除确认', { type: 'warning' })
  await deleteAssignmentApi(row.id)
  ElMessage.success('作业已删除')
  await loadAssignments()
}

function openQuestions(row: Assignment) {
  router.push(`/teacher/assignments/${row.id}/questions`)
}

function assignmentStatus(row: Assignment) {
  const deadline = new Date(row.deadline).getTime()
  const diff = deadline - Date.now()
  if (diff > 0 && diff <= 24 * 60 * 60 * 1000) return { label: '即将截止' }
  if (diff <= 0) return { label: '已截止' }
  return { label: '进行中' }
}

function canDelete() {
  const course = editableCourses.value.find((item) => item.id === selectedCourseId.value)
  return course?.memberRole === 'ADMIN' || course?.memberRole === 'OWNER'
}

function defaultDeadline() {
  const value = new Date(Date.now() + 7 * 24 * 60 * 60 * 1000)
  return formatLocalDateTime(value)
}

function formatLocalDateTime(value: Date) {
  const pad = (num: number) => String(num).padStart(2, '0')
  return `${value.getFullYear()}-${pad(value.getMonth() + 1)}-${pad(value.getDate())}T${pad(value.getHours())}:${pad(value.getMinutes())}:00`
}

onMounted(async () => {
  await loadCourses()
  await loadAssignments()
})
</script>

<template>
  <section class="page-stack">
    <PageHeader title="作业管理" description="发布文本、文件、单选题和判断题作业，并按类型管理题目、批改和相似度核查。">
      <template #actions>
        <el-select v-model="selectedCourseId" placeholder="选择课程" class="course-select">
          <el-option v-for="course in editableCourses" :key="course.id" :label="`${course.courseName}（${course.courseCode}）`" :value="course.id" />
        </el-select>
        <el-button @click="loadAssignments">刷新</el-button>
      </template>
    </PageHeader>

    <EmptyState
      v-if="editableCourses.length === 0"
      title="暂无可发布作业的课程"
      description="只有课程负责人、课程教师或管理员可以发布作业。"
    />

    <template v-else>
      <div class="stat-grid">
        <StatCard title="全部作业" :value="assignments.length" tone="primary" icon="Files" />
        <StatCard title="文本作业" :value="assignments.filter((item) => item.assignmentType === 'TEXT').length" tone="success" icon="Notebook" />
        <StatCard title="文件作业" :value="assignments.filter((item) => item.assignmentType === 'FILE').length" tone="purple" icon="Collection" />
        <StatCard title="客观题作业" :value="objectiveCount" tone="warning" icon="DocumentChecked" />
      </div>

      <section class="app-card publish-card">
        <div class="toolbar-row">
          <div>
            <h3>发布新作业</h3>
            <p class="muted">先选择课程，再填写标题、类型、截止时间、满分和作业说明。文件作业可上传本地附件或模板。</p>
          </div>
          <StatusTag :label="editingAssignment ? '编辑中' : '新建作业'" />
        </div>
        <el-form label-position="top" class="assignment-form">
          <div class="form-grid">
            <el-form-item label="作业标题">
              <el-input v-model="form.title" maxlength="120" show-word-limit placeholder="请输入作业标题" />
            </el-form-item>
            <el-form-item label="作业类型">
              <el-select v-model="form.assignmentType" class="full-input">
                <el-option v-for="item in assignmentTypes" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="截止时间">
              <el-date-picker v-model="form.deadline" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" format="YYYY-MM-DD HH:mm" class="full-input" />
            </el-form-item>
            <el-form-item label="满分">
              <el-input-number v-model="form.totalScore" :min="0.1" :step="5" class="full-input" />
            </el-form-item>
          </div>

          <el-form-item v-if="form.assignmentType === 'FILE'" label="作业附件">
            <el-upload
              class="assignment-material-upload"
              drag
              :auto-upload="false"
              :limit="1"
              :file-list="materialFileList"
              :on-change="handleMaterialFileChange"
              :on-remove="handleMaterialFileRemove"
            >
              <div class="upload-drop">
                <strong>点击或拖拽本地文件到这里</strong>
                <span>可上传作业要求、模板、数据集或参考资料，发布后学生可下载查看。</span>
              </div>
            </el-upload>
          </el-form-item>

          <el-form-item label="作业说明">
            <el-input v-model="form.description" type="textarea" :rows="4" maxlength="2000" show-word-limit placeholder="请输入作业说明" />
          </el-form-item>
          <div class="toolbar-row">
            <el-switch v-model="form.allowLate" active-text="允许迟交" inactive-text="不允许迟交" />
            <div class="form-actions">
              <el-button type="primary" :loading="saving" @click="saveAssignment">{{ editingAssignment ? '保存修改' : '发布作业' }}</el-button>
              <el-button @click="resetForm">保存草稿</el-button>
            </div>
          </div>
        </el-form>
      </section>

      <DataTableCard title="作业列表" :description="`当前课程共 ${assignments.length} 个作业，已截止 ${expiredCount} 个。`">
        <template #toolbar>
          <FilterTabs v-model="typeFilter" :options="filterOptions" />
        </template>
        <el-table v-loading="loading" :data="filteredAssignments" border>
          <el-table-column prop="title" label="标题" min-width="180" />
          <el-table-column label="类型" width="130">
            <template #default="{ row }"><StatusTag :status="row.assignmentType" :label="typeLabels[row.assignmentType as AssignmentType]" /></template>
          </el-table-column>
          <el-table-column label="状态" width="120">
            <template #default="{ row }"><StatusTag :label="assignmentStatus(row).label" /></template>
          </el-table-column>
          <el-table-column prop="deadline" label="截止时间" min-width="170" />
          <el-table-column prop="totalScore" label="满分" width="90" />
          <el-table-column label="迟交" width="100">
            <template #default="{ row }"><StatusTag :label="row.allowLate ? '允许' : '不允许'" /></template>
          </el-table-column>
          <el-table-column label="操作" width="460" fixed="right">
            <template #default="{ row }">
              <el-button size="small" @click="editAssignment(row)">编辑</el-button>
              <el-button v-if="row.assignmentType === 'SINGLE_CHOICE' || row.assignmentType === 'TRUE_FALSE'" size="small" type="primary" @click="openQuestions(row)">题目</el-button>
              <el-button size="small" type="primary" @click="router.push(`/teacher/grading?courseId=${row.courseId}&assignmentId=${row.id}`)">
                {{ row.assignmentType === 'TEXT' || row.assignmentType === 'FILE' ? '批改' : '查看结果' }}
              </el-button>
              <el-button v-if="row.assignmentType === 'FILE' || row.assignmentType === 'TEXT'" size="small" type="warning" @click="router.push(`/teacher/assignments/${row.id}/duplicates`)">相似度</el-button>
              <el-button v-if="canDelete()" size="small" type="danger" @click="removeAssignment(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </DataTableCard>
    </template>
  </section>
</template>

<style scoped>
.course-select {
  width: 320px;
}

.publish-card {
  display: grid;
  gap: 16px;
}

.publish-card h3 {
  margin: 0;
  font-size: 17px;
}

.publish-card p {
  margin: 6px 0 0;
}

.form-grid {
  display: grid;
  grid-template-columns: minmax(220px, 1fr) 180px 220px 140px;
  gap: 14px;
}

.full-input {
  width: 100%;
}

.assignment-material-upload {
  width: 100%;
}

.assignment-material-upload :deep(.el-upload) {
  width: 100%;
}

.assignment-material-upload :deep(.el-upload-dragger) {
  width: 100%;
  padding: 18px;
  border-radius: 12px;
}

.upload-drop {
  display: grid;
  gap: 6px;
  color: var(--app-text);
}

.upload-drop span {
  color: var(--app-muted);
  font-size: 13px;
}

.form-actions {
  display: flex;
  gap: 10px;
}

@media (max-width: 1050px) {
  .form-grid {
    grid-template-columns: repeat(2, minmax(180px, 1fr));
  }
}

@media (max-width: 720px) {
  .course-select,
  .form-grid {
    width: 100%;
    grid-template-columns: 1fr;
  }
}
</style>
