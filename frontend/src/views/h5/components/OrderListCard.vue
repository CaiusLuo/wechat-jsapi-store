<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showConfirmDialog, showFailToast, showSuccessToast } from 'vant'
import { cancelH5Order } from '@/api/h5Order'
import type { OrderDetail, OrderStatus } from '@/api/types'
import { formatMoney } from '@/utils/format'
import { h5OrderFilterTabs, h5OrderStatusText, matchesH5OrderFilter, type H5OrderFilter } from '@/utils/h5OrderStatus'
import { removeLocalOrderDetail } from '@/utils/storage'

const props = defineProps<{
  orders: OrderDetail[]
  loading: boolean
}>()

const emit = defineEmits<{
  (event: 'refresh'): void
}>()

const router = useRouter()
const activeFilter = ref<H5OrderFilter>('ALL')
const cancellingOrderNo = ref<string | null>(null)
const filterTabs = h5OrderFilterTabs

const sortedOrders = computed(() => {
  const priority: Record<OrderStatus, number> = {
    CREATED: 0,
    PAID: 1,
    DELIVERING: 2,
    FINISHED: 3,
    CANCELLED: 4,
  }

  return [...props.orders].sort((a, b) => priority[a.status] - priority[b.status])
})

const visibleOrders = computed(() => {
  return sortedOrders.value.filter((order) => matchesH5OrderFilter(order.status, activeFilter.value))
})

function openOrder(orderNo: string) {
  router.push(`/h5/order/${orderNo}`)
}

function openPay(order: OrderDetail, event: MouseEvent) {
  event.stopPropagation()
  router.push(`/h5/pay/${order.orderNo}`)
}

async function cancelOrder(order: OrderDetail, event: MouseEvent) {
  event.stopPropagation()
  if (cancellingOrderNo.value) return
  cancellingOrderNo.value = order.orderNo
  try {
    await showConfirmDialog({
      title: '取消订单',
      message: '确认取消该订单？',
    })
    await cancelH5Order(order.orderNo)
    removeLocalOrderDetail(order.orderNo)
    showSuccessToast('订单已取消')
    emit('refresh')
  } catch (error) {
    if (error === 'cancel') return
    showFailToast(error instanceof Error ? error.message : '取消失败，请稍后重试')
  } finally {
    cancellingOrderNo.value = null
  }
}

function formatOrderTime(value?: string) {
  if (!value) return '刚刚创建'
  return value.replace('T', ' ').slice(0, 16)
}

function displayStatusText(status: OrderStatus) {
  return h5OrderStatusText[status]
}
</script>

<template>
  <section class="order-card h5-card">
    <div class="card-head">
      <h2>我的订单</h2>
      <van-button size="small" plain :loading="loading" class="refresh-button" @click="emit('refresh')">刷新</van-button>
    </div>

    <div class="filter-tabs" role="tablist" aria-label="订单状态筛选">
      <button
        v-for="tab in filterTabs"
        :key="tab.value"
        type="button"
        :class="['filter-tab', { active: activeFilter === tab.value }]"
        @click="activeFilter = tab.value"
      >
        {{ tab.label }}
      </button>
    </div>

    <van-loading v-if="loading" class="order-loading" />

    <van-empty v-else-if="!visibleOrders.length" image-size="64">
      <template #description>
        <p class="empty-title">暂无订单</p>
        <p class="empty-text">当前筛选下暂无订单</p>
      </template>
    </van-empty>

    <div v-else class="order-list">
      <button
        v-for="order in visibleOrders"
        :key="order.orderNo"
        type="button"
        class="order-item"
        @click="openOrder(order.orderNo)"
      >
        <div class="order-top">
          <strong>订单号：{{ order.orderNo }}</strong>
          <span :class="['status-badge', `status-${order.status.toLowerCase()}`]">
            {{ displayStatusText(order.status) }}
          </span>
        </div>

        <strong class="order-amount">￥{{ formatMoney(order.payAmount) }}</strong>

        <span class="order-time">下单时间：{{ formatOrderTime(order.createTime) }}</span>

        <div v-if="order.status === 'CREATED'" class="order-actions">
          <van-button size="small" type="primary" class="pay-action" @click="openPay(order, $event)">
            继续支付
          </van-button>
          <van-button
            size="small"
            plain
            class="cancel-action"
            :loading="cancellingOrderNo === order.orderNo"
            :disabled="Boolean(cancellingOrderNo)"
            @click="cancelOrder(order, $event)"
          >
            取消订单
          </van-button>
        </div>
      </button>
    </div>
  </section>
</template>

<style scoped>
.order-card {
  width: 100%;
  max-width: 100%;
  min-width: 0;
  box-sizing: border-box;
  padding: 16px;
  overflow: hidden;
  border: 1px solid var(--color-border);
  border-radius: 16px;
  background: var(--color-card);
  box-shadow: var(--shadow-card);
}

.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  width: 100%;
  min-width: 0;
  margin-bottom: 10px;
}

.card-head h2 {
  min-width: 0;
  flex: 1;
  margin: 0;
  overflow: hidden;
  color: var(--color-text-main);
  font-size: 18px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.refresh-button {
  flex: 0 0 auto;
  border-color: var(--color-border);
  border-radius: 12px;
  color: var(--color-primary);
  background: #fff;
}

.filter-tabs {
  display: flex;
  width: 100%;
  max-width: 100%;
  min-width: 0;
  box-sizing: border-box;
  gap: 8px;
  margin-bottom: 12px;
  overflow-x: auto;
  overflow-y: hidden;
  padding-bottom: 4px;
  scrollbar-width: none;
  -webkit-overflow-scrolling: touch;
}

.filter-tabs::-webkit-scrollbar {
  display: none;
}

.filter-tab {
  flex: 0 0 auto;
  height: 30px;
  padding: 0 12px;
  border: 1px solid var(--color-border);
  border-radius: 15px;
  color: var(--color-text-secondary);
  background: #fff;
  font-size: 13px;
  white-space: nowrap;
}

.filter-tab.active {
  border-color: var(--color-primary);
  color: var(--color-primary);
  background: var(--color-primary-soft);
  font-weight: 700;
}

:deep(.van-empty__image) {
  opacity: 0.5;
}

.empty-title,
.empty-text {
  margin: 0;
  text-align: center;
}

.empty-title {
  color: var(--color-text-secondary);
  font-size: 14px;
}

.empty-text {
  margin-top: 4px;
  color: var(--color-text-muted);
  font-size: 12px;
}

.order-loading {
  display: flex;
  justify-content: center;
  padding: 30px 0;
}

.order-list {
  display: grid;
  width: 100%;
  min-width: 0;
  box-sizing: border-box;
  gap: 10px;
}

.order-item {
  display: grid;
  gap: 12px;
  width: 100%;
  min-width: 0;
  box-sizing: border-box;
  padding: 13px;
  overflow: hidden;
  border: 1px solid var(--color-border);
  border-radius: 12px;
  background: #fbfaf6;
  text-align: left;
}

.order-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  min-width: 0;
}

.order-top strong {
  flex: 1 1 auto;
  min-width: 0;
  color: var(--color-text-main);
  font-size: 14px;
  font-weight: 700;
  line-height: 1.4;
  overflow-wrap: anywhere;
}

.status-badge {
  display: flex;
  min-width: 58px;
  height: 24px;
  align-items: center;
  justify-content: center;
  flex: 0 0 auto;
  padding: 0 8px;
  border-radius: 12px;
  color: #fff;
  font-size: 12px;
  font-weight: 700;
  line-height: 24px;
  white-space: nowrap;
}

.status-created {
  background: #8a6f3d;
}

.status-paid {
  background: #1f3a5f;
}

.status-delivering {
  background: #4b6584;
}

.status-finished {
  background: #2f6b4f;
}

.status-cancelled {
  background: #98a2b3;
}

.order-amount {
  color: var(--color-text-main);
  font-size: 18px;
  line-height: 1.2;
}

.order-time {
  min-width: 0;
  color: var(--color-text-secondary);
  font-size: 12px;
  line-height: 1.4;
  overflow-wrap: anywhere;
}

.order-actions {
  display: flex;
  min-width: 0;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 8px;
}

.pay-action,
.cancel-action {
  min-width: 76px;
  height: 30px;
  border-radius: 15px;
}

.cancel-action {
  border-color: var(--color-border);
  color: var(--color-text-secondary);
  background: #fff;
}

@media (max-width: 430px) {
  .order-card {
    padding: 13px;
  }

  .card-head {
    gap: 8px;
  }

  .card-head h2 {
    font-size: 17px;
  }

  .refresh-button {
    --van-button-small-height: 28px;
    --van-button-small-padding: 0 10px;
    font-size: 12px;
  }

  .filter-tabs {
    gap: 7px;
    margin-right: -2px;
    margin-left: -2px;
    padding-right: 2px;
    padding-left: 2px;
  }

  .filter-tab {
    height: 28px;
    padding: 0 10px;
    font-size: 12px;
  }

  .order-item {
    gap: 10px;
    padding: 12px;
  }

  .status-badge {
    min-width: 54px;
    height: 24px;
    padding: 0 7px;
    font-size: 12px;
  }

  .order-actions {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    width: 100%;
  }

  .pay-action,
  .cancel-action {
    width: 100%;
    min-width: 0;
  }
}
</style>
