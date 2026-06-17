<script setup lang="ts">
defineProps<{
  title: string
  description?: string
}>()
</script>

<template>
  <section class="data-table-card">
    <div class="data-table-head">
      <div>
        <h3>{{ title }}</h3>
        <p v-if="description">{{ description }}</p>
      </div>
      <div v-if="$slots.toolbar" class="data-table-toolbar">
        <slot name="toolbar" />
      </div>
    </div>
    <div class="table-scroll">
      <slot />
    </div>
  </section>
</template>

<style scoped>
.data-table-card {
  display: grid;
  gap: 14px;
  padding: 18px;
  background: #fff;
  border: 1px solid var(--app-border);
  border-radius: 12px;
  box-shadow: var(--app-shadow);
}

.data-table-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.data-table-head h3 {
  margin: 0;
  font-size: 17px;
}

.data-table-head p {
  margin: 6px 0 0;
  color: var(--app-muted);
}

.data-table-toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
</style>
