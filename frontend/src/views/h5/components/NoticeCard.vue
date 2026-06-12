<script setup lang="ts">
import type { H5Config } from '@/api/types'

defineProps<{
  config: H5Config
}>()
</script>

<template>
  <section class="notice-card h5-card">
    <div class="notice-title">
      <span></span>
      <h2>服务说明</h2>
    </div>
    <div class="notice-grid">
      <p>联系电话：{{ config.servicePhone }}</p>
      <p>工作时间：{{ config.workTime }}</p>
      <p>公告：{{ config.noticeText }}</p>
    </div>
  </section>
</template>

<style scoped>
.notice-card {
  width: 100%;
  max-width: 100%;
  min-width: 0;
  padding: 16px;
  overflow: hidden;
  border: 1px solid var(--color-border);
  border-radius: 16px;
  background: #fffcf4;
  box-shadow: var(--shadow-card);
}

.notice-title {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
  margin-bottom: 12px;
}

.notice-title span {
  width: 8px;
  height: 18px;
  border-radius: 99px;
  background: var(--color-accent);
}

.notice-title h2 {
  min-width: 0;
  margin: 0;
  overflow: hidden;
  color: var(--color-text-main);
  font-size: 17px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notice-grid {
  display: grid;
  gap: 8px;
  min-width: 0;
}

.notice-grid p {
  margin: 0;
  color: var(--color-text-body);
  font-size: 13px;
  line-height: 1.55;
  overflow-wrap: anywhere;
}
</style>
