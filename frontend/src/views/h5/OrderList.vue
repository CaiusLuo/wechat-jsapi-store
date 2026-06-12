<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { completeWxOAuthCallback } from '@/api/h5User'
import { listH5Orders } from '@/api/h5Order'
import type { OrderDetail } from '@/api/types'
import { AuthService } from '@/services/authService'
import OrderListCard from './components/OrderListCard.vue'

const router = useRouter()
const orders = ref<OrderDetail[]>([])
const loading = ref(false)
const loadError = ref('')

onMounted(initialize)

async function initialize() {
  const oauthHandled = await completeOAuthIfNeeded()
  if (oauthHandled === false) {
    const loggedIn = await AuthService.ensureLogin({ saveIntent: false, toastWhenNotWechat: false })
    if (!loggedIn) {
      loadError.value = '请在微信内登录后查看订单'
      return
    }
  } else if (oauthHandled === null) {
    return
  }

  await loadOrders()
}

async function completeOAuthIfNeeded() {
  const url = new URL(window.location.href)
  const code = url.searchParams.get('code')
  if (!code) return false

  try {
    await completeWxOAuthCallback(code)
    url.searchParams.delete('code')
    url.searchParams.delete('state')
    window.history.replaceState({}, document.title, `${url.pathname}${url.search}${url.hash}`)
    return true
  } catch {
    loadError.value = '微信登录失败，请重新进入订单页'
    showToast(loadError.value)
    return null
  }
}

async function loadOrders() {
  if (loading.value) return
  loading.value = true
  loadError.value = ''
  try {
    orders.value = await listH5Orders({ unfinished: false })
  } catch (error) {
    orders.value = []
    loadError.value = error instanceof Error ? error.message : '订单加载失败，请稍后重试'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <main class="orders-page h5-page">
    <div class="orders-shell">
      <van-nav-bar title="我的订单" left-arrow @click-left="router.back()" />

      <div class="orders-content">
        <p v-if="loadError" class="load-error">{{ loadError }}</p>
        <OrderListCard :orders="orders" :loading="loading" @refresh="loadOrders" />
      </div>
    </div>
  </main>
</template>

<style scoped>
.orders-page {
  width: 100%;
  min-height: 100vh;
  min-height: 100dvh;
  overflow-x: hidden;
  background: var(--color-bg);
}

.orders-shell {
  width: 100%;
  max-width: 520px;
  min-height: 100vh;
  min-height: 100dvh;
  margin: 0 auto;
  overflow-x: hidden;
}

.orders-content {
  display: grid;
  gap: 10px;
  padding: 14px 16px calc(32px + env(safe-area-inset-bottom));
}

.load-error {
  margin: 0;
  padding: 10px 12px;
  border-radius: 10px;
  color: #a63d1f;
  background: rgb(217 72 15 / 8%);
  font-size: 13px;
  line-height: 1.5;
}

@media (max-width: 360px) {
  .orders-content {
    padding-right: 10px;
    padding-left: 10px;
  }
}
</style>
