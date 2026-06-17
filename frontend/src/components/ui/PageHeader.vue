<script setup lang="ts">
defineProps<{
  title: string
  description?: string
  eyebrow?: string
}>()
</script>

<template>
  <header class="page-header">
    <div>
      <p v-if="eyebrow" class="page-eyebrow">{{ eyebrow }}</p>
      <h2>{{ title }}</h2>
      <p v-if="description">{{ description }}</p>
    </div>
    <div v-if="$slots.actions" class="page-actions">
      <slot name="actions" />
    </div>
  </header>
</template>

<style scoped>
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 20px;
  background: #fff;
  border: 1px solid var(--app-border);
  border-radius: 12px;
  box-shadow: var(--app-shadow);
}

.page-header h2 {
  margin: 0;
  font-size: 22px;
  font-weight: 800;
  color: var(--app-text);
}

.page-header p {
  margin: 8px 0 0;
  color: var(--app-muted);
}

.page-eyebrow {
  margin: 0 0 8px !important;
  color: var(--app-primary) !important;
  font-size: 13px;
  font-weight: 700;
}

.page-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

@media (max-width: 760px) {
  .page-header,
  .page-actions {
    display: grid;
    align-items: stretch;
  }
}
</style>
