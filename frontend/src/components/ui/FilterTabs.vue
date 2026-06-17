<script setup lang="ts">
defineProps<{
  modelValue: string
  options: Array<{ label: string; value: string }>
}>()

defineEmits<{
  (event: 'update:modelValue', value: string): void
}>()
</script>

<template>
  <div class="filter-tabs">
    <button
      v-for="item in options"
      :key="item.value"
      type="button"
      :class="{ active: item.value === modelValue }"
      @click="$emit('update:modelValue', item.value)"
    >
      {{ item.label }}
    </button>
  </div>
</template>

<style scoped>
.filter-tabs {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.filter-tabs button {
  height: 32px;
  padding: 0 12px;
  color: #475569;
  cursor: pointer;
  background: #fff;
  border: 1px solid var(--app-border);
  border-radius: 8px;
}

.filter-tabs button.active {
  color: #fff;
  background: var(--app-primary);
  border-color: var(--app-primary);
}
</style>
