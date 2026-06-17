<script setup lang="ts">
import { ArrowRight } from '@element-plus/icons-vue'

withDefaults(defineProps<{
  count: string | number
  title: string
  description?: string
  tone?: 'primary' | 'success' | 'warning' | 'danger' | 'purple' | 'info'
}>(), {
  tone: 'primary'
})

defineEmits<{
  (event: 'open'): void
}>()
</script>

<template>
  <button type="button" class="task-item" :class="`tone-${tone}`" @click="$emit('open')">
    <span class="task-count">{{ count }}</span>
    <span class="task-main">
      <strong>{{ title }}</strong>
      <small v-if="description">{{ description }}</small>
    </span>
    <el-icon class="task-arrow"><ArrowRight /></el-icon>
  </button>
</template>

<style scoped>
.task-item {
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: 12px;
  align-items: center;
  width: 100%;
  padding: 12px;
  color: inherit;
  text-align: left;
  cursor: pointer;
  background: #f8fafc;
  border: 1px solid var(--app-border);
  border-radius: 12px;
  transition: border-color 0.18s ease, transform 0.18s ease, background 0.18s ease;
}

.task-item:hover {
  background: #fff;
  border-color: var(--tone-color);
  transform: translateY(-1px);
}

.task-count {
  display: grid;
  place-items: center;
  width: 34px;
  height: 34px;
  color: var(--tone-color);
  background: var(--tone-bg);
  border-radius: 999px;
  font-size: 15px;
  font-weight: 800;
}

.task-main strong,
.task-main small {
  display: block;
}

.task-main strong {
  font-size: 14px;
  color: var(--app-text);
}

.task-main small {
  margin-top: 4px;
  color: var(--app-muted);
  line-height: 1.45;
}

.task-arrow {
  color: #94a3b8;
}

.tone-primary { --tone-color: #2563eb; --tone-bg: #dbeafe; }
.tone-success { --tone-color: #16a34a; --tone-bg: #dcfce7; }
.tone-warning { --tone-color: #f59e0b; --tone-bg: #fef3c7; }
.tone-danger { --tone-color: #dc2626; --tone-bg: #fee2e2; }
.tone-purple { --tone-color: #7c3aed; --tone-bg: #ede9fe; }
.tone-info { --tone-color: #475569; --tone-bg: #f1f5f9; }
</style>
