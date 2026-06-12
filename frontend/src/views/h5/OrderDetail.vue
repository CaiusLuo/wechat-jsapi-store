<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showConfirmDialog, showFailToast, showSuccessToast } from 'vant'
import { cancelH5Order } from '@/api/h5Order'
import { getH5OrderDetail } from '@/api/order'
import type { OrderDetail } from '@/api/types'
import { formatAddress, formatMoney } from '@/utils/format'
import { formatH5OrderStatus } from '@/utils/h5OrderStatus'
import { removeLocalOrderDetail, saveLocalOrderDetail } from '@/utils/storage'

const route = useRoute()
const router = useRouter()
const orderNo = computed(() => String(route.params.orderNo))
const loading = ref(false)
const cancelling = ref(false)
const order = ref<OrderDetail | null>(null)

onMounted(loadOrder)

async function loadOrder() {
  loading.value = true
  try {
    order.value = await getH5OrderDetail(orderNo.value)
    saveLocalOrderDetail(order.value)
  } catch {
    order.value = null
  } finally {
    loading.value = false
  }
}

async function cancelOrder() {
  if (cancelling.value || !order.value || order.value.status !== 'CREATED') return
  cancelling.value = true
  try {
    await showConfirmDialog({
      title: '取消订单',
      message: '确认取消该订单？',
    })
    await cancelH5Order(order.value.orderNo)
    removeLocalOrderDetail(order.value.orderNo)
    showSuccessToast('订单已取消')
    router.replace('/h5/orders')
  } catch (error) {
    if (error === 'cancel') return
    showFailToast(error instanceof Error ? error.message : '取消失败，请稍后重试')
  } finally {
    cancelling.value = false
  }
}
</script>

<template>
  <main class="detail-page h5-page">
    <div class="detail-shell">
      <van-nav-bar title="订单详情" left-arrow @click-left="router.back()" />

      <van-loading v-if="loading" class="page-loading" />
      <van-empty v-else-if="!order" description="暂未查询到订单详情" />

      <section v-else class="detail-content">
        <van-cell-group inset>
          <van-cell title="订单号" :value="order.orderNo" />
          <van-cell title="订单状态">
            <template #value>
              <van-tag type="primary">{{ formatH5OrderStatus(order.status, order.statusText) }}</van-tag>
            </template>
          </van-cell>
          <van-cell title="支付金额" :value="`￥${formatMoney(order.payAmount)}`" />
          <van-cell title="创建时间" :value="order.createTime || '-'" />
          <van-cell title="支付时间" :value="order.payTime || '-'" />
        </van-cell-group>

        <div v-if="order.status === 'CREATED'" class="detail-actions">
          <van-button block plain type="danger" :loading="cancelling" :disabled="cancelling" @click="cancelOrder">
            取消订单
          </van-button>
        </div>

        <van-cell-group inset title="商品信息">
          <van-cell v-for="item in order.items || []" :key="`${item.bookId}-${item.quantity}`">
            <template #title>
              <div class="order-item">
                <div class="item-cover">
                  <img v-if="item.coverUrl" :src="item.coverUrl" :alt="item.bookName" />
                  <span v-else>{{ item.bookName.slice(0, 2) }}</span>
                </div>
                <div class="item-main">
                  <strong>{{ item.bookName }}</strong>
                  <p>￥{{ formatMoney(item.price) }} × {{ item.quantity }}</p>
                </div>
              </div>
            </template>
            <template #value>￥{{ formatMoney(item.subtotal) }}</template>
          </van-cell>
          <van-cell v-if="!(order.items || []).length" title="商品" value="后端暂未返回商品明细" />
        </van-cell-group>

        <van-cell-group inset title="收货信息">
          <van-cell title="收货人" :value="order.receiverName" />
          <van-cell title="手机号" :value="order.phone" />
          <van-cell title="学校" :value="order.school" />
          <van-cell
            title="地址"
            :label="formatAddress([order.province, order.city, order.district, order.detailAddress]) || '-'"
          />
          <van-cell title="备注" :label="order.remark || '-'" />
        </van-cell-group>
      </section>
    </div>
  </main>
</template>

<style scoped>
.detail-page {
  background: var(--color-bg);
}

.detail-shell {
  width: 100%;
  max-width: 520px;
  min-height: 100vh;
  min-height: 100dvh;
  margin: 0 auto;
  overflow-x: hidden;
  background: #f6f7f9;
}

.page-loading {
  display: flex;
  justify-content: center;
  padding: 60px 0;
}

.detail-content {
  display: grid;
  gap: 12px;
  padding: 12px 0 24px;
}

.detail-actions {
  padding: 0 16px;
}

.order-item {
  display: grid;
  grid-template-columns: 48px minmax(0, 1fr);
  gap: 10px;
  min-width: 0;
  text-align: left;
}

.item-cover {
  display: grid;
  width: 48px;
  height: 64px;
  place-items: center;
  overflow: hidden;
  border-radius: 4px;
  color: #52616f;
  background: #e9eef3;
  font-size: 12px;
}

.item-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.order-item strong {
  display: block;
  min-width: 0;
  overflow-wrap: anywhere;
  color: #1f2933;
  font-size: 14px;
  line-height: 1.4;
}

.item-main {
  min-width: 0;
}

.order-item p {
  margin: 5px 0 0;
  color: #687583;
  font-size: 13px;
}
</style>
