<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { DocumentAdd, Finished, UserFilled } from '@element-plus/icons-vue'
import { createCourseApplicationApi } from '@/api/course'
import PageHeader from '@/components/ui/PageHeader.vue'
import DashboardCard from '@/components/ui/DashboardCard.vue'

const loading = ref(false)

const form = reactive({
  courseName: '',
  courseCode: '',
  description: ''
})

async function submit() {
  if (!form.courseName.trim() || !form.courseCode.trim()) {
    ElMessage.warning('请填写课程名称和课程代码')
    return
  }
  loading.value = true
  try {
    await createCourseApplicationApi({
      courseName: form.courseName.trim(),
      courseCode: form.courseCode.trim(),
      description: form.description.trim()
    })
    ElMessage.success('课程申请已提交，等待管理员审批')
    form.courseName = ''
    form.courseCode = ''
    form.description = ''
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <section class="page-stack application-page">
    <PageHeader
      title="课程申请"
      eyebrow="教师工作台"
      description="提交课程创建申请后，由管理员审批；审批通过后你将自动成为课程负责人。"
    >
      <template #actions>
        <el-button type="primary" :loading="loading" @click="submit">提交申请</el-button>
      </template>
    </PageHeader>

    <div class="application-grid">
      <section class="app-card form-card">
        <h3>申请信息</h3>
        <el-form label-position="top" class="application-form">
          <el-form-item label="课程名称">
            <el-input v-model="form.courseName" maxlength="100" show-word-limit placeholder="例如：软件体系结构" />
          </el-form-item>
          <el-form-item label="课程代码">
            <el-input v-model="form.courseCode" maxlength="50" show-word-limit placeholder="例如：SWA-2026" />
          </el-form-item>
          <el-form-item label="课程说明">
            <el-input
              v-model="form.description"
              type="textarea"
              maxlength="1000"
              show-word-limit
              :rows="8"
              placeholder="说明课程用途、授课对象或申请理由"
            />
          </el-form-item>
          <div class="form-actions">
            <el-button type="primary" :loading="loading" @click="submit">提交课程申请</el-button>
            <el-button @click="form.courseName = ''; form.courseCode = ''; form.description = ''">清空</el-button>
          </div>
        </el-form>
      </section>

      <DashboardCard title="审批流程" description="课程创建采用申请-审批-成员管理流程">
        <div class="flow-list">
          <div>
            <span><DocumentAdd /></span>
            <strong>教师提交申请</strong>
            <p>填写课程名称、代码和说明，系统记录申请日志。</p>
          </div>
          <div>
            <span><Finished /></span>
            <strong>管理员审批</strong>
            <p>管理员通过或驳回申请，并向教师发送站内通知。</p>
          </div>
          <div>
            <span><UserFilled /></span>
            <strong>课程成员管理</strong>
            <p>审批通过后，教师负责添加学生、助教和其他教师。</p>
          </div>
        </div>
      </DashboardCard>
    </div>
  </section>
</template>

<style scoped>
.application-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.4fr) minmax(320px, 0.8fr);
  gap: 16px;
  align-items: start;
}

.form-card h3 {
  margin: 0 0 16px;
  font-size: 18px;
}

.form-actions {
  display: flex;
  gap: 10px;
}

.flow-list {
  display: grid;
  gap: 14px;
}

.flow-list div {
  display: grid;
  grid-template-columns: auto 1fr;
  gap: 4px 12px;
  align-items: start;
  padding: 14px;
  background: #f8fafc;
  border: 1px solid var(--app-border);
  border-radius: 10px;
}

.flow-list span {
  display: grid;
  place-items: center;
  grid-row: span 2;
  width: 38px;
  height: 38px;
  color: var(--app-primary);
  background: var(--app-primary-soft);
  border-radius: 10px;
  font-size: 18px;
}

.flow-list strong {
  color: var(--app-text);
}

.flow-list p {
  margin: 0;
  color: var(--app-muted);
  line-height: 1.6;
}

@media (max-width: 960px) {
  .application-grid {
    grid-template-columns: 1fr;
  }
}
</style>
