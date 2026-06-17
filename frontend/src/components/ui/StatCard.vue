<script setup lang="ts">
import {
  Bell,
  Calendar,
  Collection,
  DataAnalysis,
  DocumentChecked,
  EditPen,
  Files,
  Finished,
  Notebook,
  Reading,
  TrendCharts,
  User
} from '@element-plus/icons-vue'

const props = withDefaults(defineProps<{
  title: string
  value: string | number
  hint?: string
  tone?: 'primary' | 'success' | 'warning' | 'danger' | 'purple' | 'info'
  icon?: keyof typeof iconMap
  clickable?: boolean
}>(), {
  tone: 'primary',
  icon: 'DataAnalysis',
  clickable: false
})

const iconMap = {
  Bell,
  Calendar,
  Collection,
  DataAnalysis,
  DocumentChecked,
  EditPen,
  Files,
  Finished,
  Notebook,
  Reading,
  TrendCharts,
  User
}
</script>

<template>
  <article class="stat-card" :class="[`tone-${props.tone}`, { clickable: props.clickable }]">
    <div class="stat-icon">
      <component :is="iconMap[props.icon]" />
    </div>
    <div>
      <span>{{ title }}</span>
      <strong>{{ value }}</strong>
      <small v-if="hint">{{ hint }}</small>
    </div>
  </article>
</template>

<style scoped>
.stat-card {
  display: flex;
  align-items: center;
  gap: 14px;
  min-height: 96px;
  padding: 16px 18px;
  background: #fff;
  border: 1px solid var(--app-border);
  border-radius: 14px;
  box-shadow: var(--app-shadow);
  transition: transform 0.18s ease, border-color 0.18s ease, box-shadow 0.18s ease;
}

.stat-card.clickable {
  cursor: pointer;
}

.stat-card.clickable:hover {
  border-color: var(--app-primary);
  transform: translateY(-2px);
  box-shadow: 0 14px 30px rgba(15, 23, 42, 0.08);
}

.stat-icon {
  display: grid;
  place-items: center;
  width: 54px;
  height: 54px;
  color: var(--tone-color);
  background: var(--tone-bg);
  border-radius: 14px;
  font-size: 24px;
}

.stat-card span,
.stat-card small {
  display: block;
  color: var(--app-muted);
}

.stat-card strong {
  display: block;
  margin-top: 6px;
  font-size: 28px;
  line-height: 1;
  color: var(--app-text);
}

.stat-card small {
  margin-top: 6px;
  font-size: 12px;
}

.tone-primary { --tone-color: #2563eb; --tone-bg: #dbeafe; }
.tone-success { --tone-color: #16a34a; --tone-bg: #dcfce7; }
.tone-warning { --tone-color: #f59e0b; --tone-bg: #fef3c7; }
.tone-danger { --tone-color: #dc2626; --tone-bg: #fee2e2; }
.tone-purple { --tone-color: #7c3aed; --tone-bg: #ede9fe; }
.tone-info { --tone-color: #475569; --tone-bg: #f1f5f9; }
</style>
