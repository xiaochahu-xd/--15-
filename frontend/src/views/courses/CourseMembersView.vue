<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { addCourseMemberApi, listCourseMembersApi, removeCourseMemberApi } from '@/api/course'
import { listSelectableUsersApi } from '@/api/users'
import type { CourseMember, RoleCode, UserSummary } from '@/types'
import PageHeader from '@/components/ui/PageHeader.vue'
import StatCard from '@/components/ui/StatCard.vue'
import DataTableCard from '@/components/ui/DataTableCard.vue'
import StatusTag from '@/components/ui/StatusTag.vue'

const route = useRoute()
const router = useRouter()
const courseId = Number(route.params.courseId)
const loading = ref(false)
const adding = ref(false)
const members = ref<CourseMember[]>([])
const users = ref<UserSummary[]>([])

const form = reactive<{
  memberRole: 'STUDENT' | 'ASSISTANT' | 'TEACHER'
  userId?: number
}>({
  memberRole: 'STUDENT',
  userId: undefined
})

const roleLabels: Record<string, string> = {
  OWNER: '负责人',
  TEACHER: '教师',
  ASSISTANT: '助教',
  STUDENT: '学生'
}

const summary = computed(() => ({
  total: members.value.length,
  teachers: members.value.filter((item) => ['OWNER', 'TEACHER'].includes(item.memberRole)).length,
  assistants: members.value.filter((item) => item.memberRole === 'ASSISTANT').length,
  students: members.value.filter((item) => item.memberRole === 'STUDENT').length
}))

const selectableUsers = computed(() => {
  const memberIds = new Set(members.value.map((item) => item.userId))
  return users.value.filter((user) => {
    return user.status === 1 && user.roles.includes(form.memberRole as RoleCode) && !memberIds.has(user.id)
  })
})

const selectableHint = computed(() => {
  const roleName = roleLabels[form.memberRole] || form.memberRole
  const roleUsers = users.value.filter((user) => user.status === 1 && user.roles.includes(form.memberRole as RoleCode))
  if (roleUsers.length === 0) {
    return `系统中暂无可添加的${roleName}账号。学生和教师可在登录页注册；助教账号需由管理员创建或分配助教角色。`
  }
  return `当前课程中未加入的${roleName}账号为空。已有账号若已在成员列表中，不会重复显示。`
})

watch(
  () => form.memberRole,
  () => {
    form.userId = undefined
  }
)

async function loadData() {
  loading.value = true
  try {
    const [membersResponse, usersResponse] = await Promise.all([
      listCourseMembersApi(courseId),
      listSelectableUsersApi()
    ])
    members.value = membersResponse.data.data
    users.value = usersResponse.data.data
  } finally {
    loading.value = false
  }
}

async function addMember() {
  if (!form.userId) {
    ElMessage.warning('请选择要添加的用户')
    return
  }
  adding.value = true
  try {
    await addCourseMemberApi(courseId, {
      userId: form.userId,
      memberRole: form.memberRole
    })
    ElMessage.success('成员已添加')
    form.userId = undefined
    await loadData()
  } finally {
    adding.value = false
  }
}

async function removeMember(member: CourseMember) {
  await ElMessageBox.confirm(`确认移除成员“${member.realName}”吗？`, '移除确认', {
    type: 'warning'
  })
  await removeCourseMemberApi(courseId, member.userId)
  ElMessage.success('成员已移除')
  await loadData()
}

onMounted(loadData)
</script>

<template>
  <section class="page-stack members-page">
    <PageHeader title="课程成员管理" description="教师管理自己负责课程的成员；管理员可管理全部课程成员。">
      <template #actions>
        <el-button @click="router.back()">返回</el-button>
      </template>
    </PageHeader>

    <div class="stat-grid">
      <StatCard title="成员总数" :value="summary.total" hint="当前课程" icon="User" tone="primary" />
      <StatCard title="教师" :value="summary.teachers" hint="含课程负责人" icon="Reading" tone="purple" />
      <StatCard title="助教" :value="summary.assistants" hint="可协助批改" icon="EditPen" tone="warning" />
      <StatCard title="学生" :value="summary.students" hint="可提交作业" icon="Notebook" tone="success" />
    </div>

    <section class="app-card member-form-card">
      <h3>添加成员</h3>
      <div class="member-form">
        <el-select v-model="form.memberRole" class="role-select" placeholder="成员角色">
          <el-option label="学生" value="STUDENT" />
          <el-option label="助教" value="ASSISTANT" />
          <el-option label="教师" value="TEACHER" />
        </el-select>
        <el-select v-model="form.userId" class="user-select" filterable placeholder="选择用户">
          <el-option
            v-for="user in selectableUsers"
            :key="user.id"
            :label="`${user.realName}（${user.username}）`"
            :value="user.id"
          />
          <template #empty>
            <div class="select-empty">
              {{ selectableHint }}
            </div>
          </template>
        </el-select>
        <el-button type="primary" :loading="adding" @click="addMember">添加成员</el-button>
      </div>
      <p class="member-tip">
        候选用户来自系统用户库，只显示拥有所选角色且尚未加入当前课程的账号。
      </p>
    </section>

    <DataTableCard title="成员列表" description="课程负责人不可被移除，成员变更由后端权限校验。">
      <el-table v-loading="loading" :data="members" border>
        <el-table-column prop="realName" label="姓名" min-width="130" />
        <el-table-column prop="username" label="用户名" min-width="130" />
        <el-table-column label="课程身份" width="120">
          <template #default="{ row }">
            <StatusTag :status="row.memberRole" :label="roleLabels[row.memberRole] || row.memberRole" />
          </template>
        </el-table-column>
        <el-table-column prop="joinedAt" label="加入时间" min-width="170" />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button
              v-if="row.memberRole !== 'OWNER'"
              type="danger"
              size="small"
              @click="removeMember(row)"
            >
              移除
            </el-button>
            <span v-else class="muted">不可移除</span>
          </template>
        </el-table-column>
      </el-table>
    </DataTableCard>
  </section>
</template>

<style scoped>
.member-form-card {
  display: grid;
  gap: 14px;
}

.member-form-card h3 {
  margin: 0;
  font-size: 18px;
}

.member-form {
  display: grid;
  grid-template-columns: 150px minmax(240px, 420px) 110px;
  gap: 12px;
  align-items: center;
  max-width: 720px;
}

.role-select,
.user-select {
  width: 100%;
}

.member-tip {
  margin: 0;
  color: #667085;
  font-size: 13px;
}

.select-empty {
  padding: 12px 16px;
  color: #8a94a6;
  line-height: 1.6;
  white-space: normal;
}

@media (max-width: 760px) {
  .member-form {
    grid-template-columns: 1fr;
  }
}
</style>
