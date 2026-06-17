<script setup lang="ts">
import { computed } from 'vue'
import { Connection, DataBoard, Files, Lock, Reading } from '@element-plus/icons-vue'
import type { Course, CourseDetailSummary } from '@/types'
import StatusTag from './StatusTag.vue'

const props = defineProps<{
  course: Course
  detail?: CourseDetailSummary
  index?: number
  manageable?: boolean
}>()

defineEmits<{
  (event: 'enter', course: Course): void
  (event: 'members', course: Course): void
}>()

const iconColors = ['blue', 'green', 'purple', 'orange', 'teal']
const color = computed(() => iconColors[(props.index || 0) % iconColors.length])
</script>

<template>
  <article class="course-card" @click="$emit('enter', course)">
    <div class="course-top">
      <div class="course-icon" :class="`color-${color}`">
        <Reading />
      </div>
      <div>
        <h3>{{ course.courseName }}</h3>
        <p>{{ course.courseCode }}</p>
      </div>
      <StatusTag :status="course.memberRole" />
    </div>

    <p class="course-desc">{{ course.description || '暂无课程说明。' }}</p>

    <div class="owner-row">
      <span>负责人</span>
      <strong>{{ course.ownerName || '未设置' }}</strong>
    </div>

    <div class="course-meta">
      <span><Connection />成员 <strong>{{ detail?.memberCount ?? '-' }}</strong></span>
      <span><Files />作业 <strong>{{ detail?.assignmentCount ?? '-' }}</strong></span>
      <span><DataBoard />待批改 <strong>{{ detail?.pendingGradingCount ?? '-' }}</strong></span>
      <span><Lock />相似 <strong>{{ detail?.duplicateCount ?? '-' }}</strong></span>
    </div>

    <div class="course-actions" @click.stop>
      <el-button type="primary" plain @click="$emit('enter', course)">进入课程</el-button>
      <el-button v-if="manageable" @click="$emit('members', course)">成员管理</el-button>
    </div>
  </article>
</template>

<style scoped>
.course-card {
  display: grid;
  gap: 16px;
  padding: 17px;
  cursor: pointer;
  background: #fff;
  border: 1px solid var(--app-border);
  border-radius: 12px;
  box-shadow: var(--app-shadow);
  transition: border-color 0.2s, transform 0.2s;
}

.course-card:hover {
  border-color: var(--app-primary);
  transform: translateY(-2px);
}

.course-top {
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: 12px;
  align-items: flex-start;
}

.course-icon {
  display: grid;
  place-items: center;
  width: 48px;
  height: 48px;
  color: #fff;
  border-radius: 10px;
  font-size: 23px;
}

.color-blue { background: #3b82f6; }
.color-green { background: #16a34a; }
.color-purple { background: #7c3aed; }
.color-orange { background: #f59e0b; }
.color-teal { background: #0f9f9a; }

.course-top h3 {
  margin: 0;
  font-size: 17px;
}

.course-top p,
.course-desc {
  margin: 6px 0 0;
  color: var(--app-muted);
}

.course-desc {
  min-height: 44px;
  line-height: 1.6;
}

.owner-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  min-height: 32px;
  padding: 8px 10px;
  background: #f8fafc;
  border: 1px solid var(--app-border);
  border-radius: 10px;
}

.owner-row span {
  color: var(--app-muted);
  font-size: 13px;
}

.owner-row strong {
  color: var(--app-text);
  font-size: 13px;
}

.course-meta {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
  padding-top: 12px;
  border-top: 1px solid var(--app-border);
}

.course-meta span {
  display: flex;
  align-items: center;
  gap: 5px;
  color: #475569;
  font-size: 13px;
}

.course-meta strong {
  color: var(--app-text);
}

.course-actions {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 8px;
}

@media (max-width: 520px) {
  .course-meta,
  .course-actions {
    grid-template-columns: 1fr 1fr;
  }
}
</style>
