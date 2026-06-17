<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const title = computed(() => String(route.meta.title || '页面'))
</script>

<template>
  <section class="page-panel placeholder">
    <h2>{{ title }}</h2>
    <p class="muted">该模块将在后续阶段开发。</p>
  </section>
</template>

<style scoped>
.placeholder {
  display: grid;
  place-items: start;
  align-content: start;
}

.placeholder h2 {
  margin: 0 0 8px;
  color: #172033;
}

.placeholder p {
  margin: 0;
}
</style>
