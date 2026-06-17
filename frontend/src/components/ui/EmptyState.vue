<script setup lang="ts">
import { Box } from '@element-plus/icons-vue'

withDefaults(defineProps<{
  title?: string
  description?: string
}>(), {
  title: '暂无数据',
  description: '当前没有符合条件的记录'
})
</script>

<template>
  <div class="empty-state">
    <div class="empty-icon"><Box /></div>
    <strong>{{ title }}</strong>
    <p>{{ description }}</p>
    <div v-if="$slots.actions" class="empty-actions">
      <slot name="actions" />
    </div>
  </div>
</template>

<style scoped>
.empty-state {
  display: grid;
  place-items: center;
  min-height: 220px;
  padding: 24px;
  color: var(--app-muted);
  text-align: center;
  background: #fff;
  border: 1px dashed #cbd5e1;
  border-radius: 12px;
}

.empty-icon {
  display: grid;
  place-items: center;
  width: 64px;
  height: 64px;
  margin-bottom: 12px;
  color: #94a3b8;
  background: #f8fafc;
  border-radius: 16px;
  font-size: 28px;
}

.empty-state strong {
  color: var(--app-text);
}

.empty-state p {
  max-width: 420px;
  margin: 8px 0 0;
}

.empty-actions {
  margin-top: 14px;
}
</style>
