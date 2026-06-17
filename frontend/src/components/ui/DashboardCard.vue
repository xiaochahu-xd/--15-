<script setup lang="ts">
defineProps<{
  title: string
  description?: string
  compact?: boolean
}>()
</script>

<template>
  <section class="dashboard-card" :class="{ compact }">
    <div class="dashboard-card-head">
      <div>
        <h3>{{ title }}</h3>
        <p v-if="description">{{ description }}</p>
      </div>
      <div v-if="$slots.actions" class="dashboard-card-actions">
        <slot name="actions" />
      </div>
    </div>
    <slot />
  </section>
</template>

<style scoped>
.dashboard-card {
  display: grid;
  gap: 14px;
  padding: 18px;
  background: #fff;
  border: 1px solid var(--app-border);
  border-radius: 14px;
  box-shadow: var(--app-shadow);
}

.dashboard-card.compact {
  gap: 12px;
  padding: 16px;
}

.dashboard-card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.dashboard-card h3 {
  margin: 0;
  font-size: 17px;
}

.dashboard-card p {
  margin: 6px 0 0;
  color: var(--app-muted);
}

.dashboard-card-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>
