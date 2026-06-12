<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showFailToast, showSuccessToast } from 'vant'
import { getH5OrderDetail } from '@/api/order'
import type { JsapiPayParams, OrderDetail } from '@/api/types'
import { formatMoney } from '@/utils/format'
import { h5OrderStatusText } from '@/utils/h5OrderStatus'
import { getPayResult, saveLocalOrderDetail } from '@/utils/storage'
import { PayService } from '@/services/payService'

const route = useRoute()
const router = useRouter()
const orderNo = computed(() => String(route.params.orderNo))
const loading = ref(false)
const paying = ref(false)
const order = ref<OrderDetail | null>(null)
const payParams = ref<JsapiPayParams | null>(null)

onMounted(async () => {
  await loadOrder()
  const cached = getPayResult(orderNo.value)
  if (cached?.payParams) {
    payParams.value = cached.payParams
  }
})

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

async function ensurePayParams() {
  if (payParams.value) return payParams.value
  const cached = getPayResult(orderNo.value)
  if (cached?.payParams) {
    payParams.value = cached.payParams
    return payParams.value
  }
  throw new Error('未找到支付参数，请返回下单页重新提交订单')
}

async function pay() {
  if (paying.value) return

  paying.value = true
  try {
    const params = await ensurePayParams()
    await PayService.pay(orderNo.value, params)
    showSuccessToast('支付请求已完成')
    await router.replace(`/h5/order/${orderNo.value}`)
  } catch (error) {
    if (error === 'cancel') return
    showFailToast(error instanceof Error ? error.message : '支付失败')
  } finally {
    paying.value = false
  }
}

</script>

<template>
  <main class="pay-page h5-page">
    <div class="pay-shell">
      <van-nav-bar title="订单支付" left-arrow @click-left="router.back()" />

      <van-loading v-if="loading" class="page-loading" />
      <section v-else class="pay-card h5-card">
        <p class="label">订单号</p>
        <h1>{{ orderNo }}</h1>

        <div class="amount">¥{{ formatMoney(order?.payAmount) }}</div>
        <van-tag v-if="order" size="large" type="warning">{{ h5OrderStatusText[order.status] }}</van-tag>
        <p class="tip">最终订单状态以订单查询结果为准。</p>

        <van-button block type="primary" :loading="paying" :disabled="paying || order?.status !== 'CREATED'" @click="pay">
          立即支付
        </van-button>
        <van-button block plain class="detail-button" @click="router.push(`/h5/order/${orderNo}`)">查看订单</van-button>
      </section>
    </div>
  </main>
</template>

<style scoped>
.pay-page {
  background: var(--color-bg);
}

.pay-shell {
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

.pay-card {
  margin: 16px;
  padding: 22px 16px;
  border-radius: 8px;
  background: #fff;
  text-align: center;
}

.label {
  margin: 0;
  color: #687583;
  font-size: 13px;
}

h1 {
  margin: 8px 0 18px;
  overflow-wrap: anywhere;
  font-size: 18px;
}

.amount {
  margin-bottom: 12px;
  color: #d9480f;
  font-size: 34px;
  font-weight: 700;
}

.tip {
  margin: 18px 0;
  color: #697582;
  font-size: 13px;
  line-height: 1.6;
}

.mock-button,
.detail-button {
  margin-top: 10px;
}
</style>
