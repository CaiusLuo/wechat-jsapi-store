<script setup lang="ts">
import { computed } from 'vue'
import type { H5UserProfile } from '@/api/h5User'
import type { OrderDetail } from '@/api/types'
import InlinePurchaseSection from './InlinePurchaseSection.vue'

const props = defineProps<{
  show: boolean
  user: H5UserProfile
}>()

const emit = defineEmits<{
  (event: 'update:show', value: boolean): void
  (event: 'order-created', order: OrderDetail): void
  (event: 'pay-success', order: OrderDetail): void
  (event: 'require-profile'): void
}>()

const visible = computed({
  get: () => props.show,
  set: (value: boolean) => emit('update:show', value),
})
</script>

<template>
  <van-popup v-model:show="visible" round position="bottom" class="purchase-popup" safe-area-inset-bottom>
    <section class="purchase-panel">
      <div class="panel-head">
        <div>
          <p>教材购书服务</p>
          <h2>教材订购</h2>
        </div>
        <van-button size="small" plain class="close-button" @click="visible = false">关闭</van-button>
      </div>

      <InlinePurchaseSection
        :user="user"
        @order-created="emit('order-created', $event)"
        @pay-success="emit('pay-success', $event)"
        @require-profile="emit('require-profile')"
      />
    </section>
  </van-popup>
</template>

<style scoped>
.purchase-popup {
  width: 100%;
  height: 92%;
  max-height: 92dvh;
  overflow: hidden;
  background: transparent;
}

.purchase-panel {
  display: flex;
  width: 100%;
  max-width: 520px;
  height: 100%;
  min-width: 0;
  flex-direction: column;
  margin: 0 auto;
  overflow-y: auto;
  border-radius: 24px 24px 0 0;
  background: var(--color-bg);
}

.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 18px 18px 10px;
}

.panel-head p {
  margin: 0 0 3px;
  color: var(--color-accent);
  font-size: 12px;
  font-weight: 600;
}

.panel-head h2 {
  margin: 0;
  color: var(--color-text-main);
  font-size: 22px;
}

.close-button {
  border-color: var(--color-border);
  border-radius: 12px;
  color: var(--color-primary);
}
</style>
