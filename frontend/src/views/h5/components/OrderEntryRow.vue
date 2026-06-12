<script setup lang="ts">
defineEmits<{
  (event: 'click'): void
}>()
</script>

<template>
  <button type="button" class="order-entry h5-card" @click="$emit('click')">
    <strong>我的订单</strong>
    <span>查看全部 <i aria-hidden="true">›</i></span>
  </button>
</template>

<style scoped>
.order-entry {
  display: flex;
  width: 100%;
  min-width: 0;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 15px 16px;
  overflow: hidden;
  border: 1px solid var(--color-border);
  border-radius: 16px;
  color: var(--color-text-main);
  background: var(--color-card);
  box-shadow: var(--shadow-card);
  text-align: left;
}

.order-entry strong {
  min-width: 0;
  overflow: hidden;
  font-size: 16px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.order-entry span {
  display: inline-flex;
  flex: 0 0 auto;
  align-items: center;
  gap: 3px;
  color: var(--color-primary);
  font-size: 13px;
}

.order-entry i {
  font-size: 20px;
  font-style: normal;
  line-height: 1;
}
</style>
