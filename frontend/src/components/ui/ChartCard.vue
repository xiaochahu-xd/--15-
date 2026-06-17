<script setup lang="ts">
defineProps<{
  title: string
  description?: string
}>()
</script>

<template>
  <section class="chart-card">
    <div class="chart-head">
      <div>
        <h3>{{ title }}</h3>
        <p v-if="description">{{ description }}</p>
      </div>
      <div v-if="$slots.actions" class="chart-actions">
        <slot name="actions" />
      </div>
    </div>
    <slot />
  </section>
</template>

<style scoped>
.chart-card {
  min-height: 260px;
  padding: 18px;
  background: #fff;
  border: 1px solid var(--app-border);
  border-radius: 14px;
  box-shadow: var(--app-shadow);
}

.chart-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.chart-head h3 {
  margin: 0;
  font-size: 17px;
}

.chart-head p {
  margin: 6px 0 0;
  color: var(--app-muted);
}

.chart-actions {
  display: flex;
  gap: 8px;
}
</style>
