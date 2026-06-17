<template>
  <section class="page-panel forbidden">
    <h2>无权限</h2>
    <p class="muted">当前账号不能访问该页面。</p>
    <el-button type="primary" @click="$router.replace('/dashboard')">返回控制台</el-button>
  </section>
</template>

<style scoped>
.forbidden {
  display: grid;
  place-items: start;
  align-content: center;
  justify-content: center;
  gap: 14px;
  text-align: center;
}

.forbidden h2,
.forbidden p {
  margin: 0;
}
</style>
