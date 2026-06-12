<script setup lang="ts">
import { computed, nextTick, onMounted, ref } from 'vue'
import { showFailToast, showSuccessToast, showToast } from 'vant'
import type { H5UserProfile } from '@/api/h5User'
import { getH5Books } from '@/api/h5Book'
import { createOrderAndPay, getH5OrderDetail } from '@/api/h5Order'
import type { Book, CreateOrderPayload, CreatePayOrderResult, OrderDetail, OrderStatus } from '@/api/types'
import { formatMoney } from '@/utils/format'
import { saveLocalOrderDetail, savePayResult } from '@/utils/storage'
import { AuthService } from '@/services/authService'
import { PayService } from '@/services/payService'
import BookPurchaseItem from './BookPurchaseItem.vue'

const DEFAULT_VISIBLE_BOOK_COUNT = 3

defineProps<{
  user: H5UserProfile
}>()

const emit = defineEmits<{
  (event: 'order-created', order: OrderDetail): void
  (event: 'pay-success', order: OrderDetail): void
  (event: 'require-profile'): void
}>()

const sectionRef = ref<HTMLElement | null>(null)
const books = ref<Book[]>([])
const loading = ref(false)
const submitting = ref(false)
const requestingCheckout = ref(false)
const expanded = ref(false)
const quantities = ref<Record<number, number>>({})
const pendingCheckout = ref(false)
const continuingAfterProfile = ref(false)
const lastPayResult = ref<CreatePayOrderResult | null>(null)
const lastLocalOrder = ref<OrderDetail | null>(null)

const visibleBooks = computed(() =>
  expanded.value ? books.value : books.value.slice(0, DEFAULT_VISIBLE_BOOK_COUNT),
)
const canToggleBooks = computed(() => books.value.length > DEFAULT_VISIBLE_BOOK_COUNT)

const selectedItems = computed(() =>
  books.value
    .map((book) => ({
      book,
      quantity: quantities.value[book.id] || 0,
    }))
    .filter((item) => item.quantity > 0),
)

const selectedCount = computed(() => selectedItems.value.reduce((sum, item) => sum + item.quantity, 0))
const totalAmount = computed(() =>
  selectedItems.value.reduce((sum, item) => sum + Number(item.book.price || 0) * item.quantity, 0),
)
const checkoutDisabled = computed(
  () =>
    loading.value ||
    submitting.value ||
    requestingCheckout.value ||
    pendingCheckout.value ||
    continuingAfterProfile.value,
)

onMounted(loadBooks)

async function loadBooks() {
  if (loading.value) return
  loading.value = true
  try {
    books.value = await getH5Books()
    const nextQuantities: Record<number, number> = {}
    for (const book of books.value) {
      nextQuantities[book.id] = quantities.value[book.id] || 0
    }
    quantities.value = nextQuantities
  } catch {
    books.value = []
    quantities.value = {}
  } finally {
    loading.value = false
  }
}

function setQuantity(bookId: number, value: number) {
  quantities.value = {
    ...quantities.value,
    [bookId]: Math.max(0, value),
  }
}

function validateUser(profile: H5UserProfile) {
  if (!profile.receiverName.trim()) return '请先完善真实收货人姓名'
  if (!/^1[3-9]\d{9}$/.test(profile.phone.trim())) return '请填写正确手机号'
  if (!profile.school.trim()) return '请先完善学校全称'
  if (
    !profile.province.trim() ||
    !profile.city.trim() ||
    !profile.district.trim() ||
    !profile.detailAddress.trim()
  ) {
    return '请先完善收货地址'
  }
  return ''
}

function buildPayload(profile: H5UserProfile): CreateOrderPayload {
  return {
    receiverName: profile.receiverName.trim(),
    phone: profile.phone.trim(),
    school: profile.school.trim(),
    province: profile.province.trim(),
    city: profile.city.trim(),
    district: profile.district.trim(),
    detailAddress: profile.detailAddress.trim(),
    items: selectedItems.value.map((item) => ({
      bookId: item.book.id,
      quantity: item.quantity,
    })),
  }
}

function buildLocalOrder(
  result: CreatePayOrderResult,
  profile: H5UserProfile,
  status: OrderStatus = 'CREATED',
): OrderDetail {
  return {
    orderNo: result.orderNo,
    payAmount: result.payAmount,
    totalAmount: result.payAmount,
    status,
    receiverName: profile.receiverName,
    phone: profile.phone,
    school: profile.school,
    province: profile.province,
    city: profile.city,
    district: profile.district,
    detailAddress: profile.detailAddress,
    createTime: new Date().toISOString(),
    items: selectedItems.value.map((item) => ({
      bookId: item.book.id,
      bookName: item.book.name,
      coverUrl: item.book.coverUrl,
      price: item.book.price,
      quantity: item.quantity,
      subtotal: Number(item.book.price || 0) * item.quantity,
    })),
  }
}

function persistOrder(
  result: CreatePayOrderResult,
  profile: H5UserProfile,
  status: OrderStatus = 'CREATED',
) {
  const order = buildLocalOrder(result, profile, status)
  savePayResult(result)
  saveLocalOrderDetail(order)
  lastPayResult.value = result
  lastLocalOrder.value = order
  return order
}

function resetSelection() {
  const nextQuantities: Record<number, number> = {}
  for (const book of books.value) {
    nextQuantities[book.id] = 0
  }
  quantities.value = nextQuantities
}

async function requestCheckout() {
  if (checkoutDisabled.value) return
  if (!selectedCount.value) {
    showToast('请至少选择一本书')
    return
  }

  requestingCheckout.value = true
  try {
    const loggedIn = await AuthService.ensureLogin()
    if (!loggedIn) return

    pendingCheckout.value = true
    emit('require-profile')
  } finally {
    requestingCheckout.value = false
  }
}

async function createAndPay(profile: H5UserProfile) {
  if (submitting.value) return
  submitting.value = true
  try {
    const reusablePayResult =
      lastLocalOrder.value?.status === 'CREATED' && lastPayResult.value ? lastPayResult.value : null
    const result = reusablePayResult || (await createOrderAndPay(buildPayload(profile)))
    const order =
      reusablePayResult && lastLocalOrder.value
        ? lastLocalOrder.value
        : persistOrder(result, profile)
    if (!reusablePayResult) {
      emit('order-created', order)
    }

    await PayService.pay(order.orderNo, result.payParams)

    try {
      const remoteOrder = await getH5OrderDetail(order.orderNo)
      saveLocalOrderDetail(remoteOrder)
      lastLocalOrder.value = remoteOrder
      emit('pay-success', remoteOrder)
    } catch {
      emit('pay-success', order)
    }
    showSuccessToast('支付已提交，请在我的订单中确认状态')
    resetSelection()
    lastPayResult.value = null
    lastLocalOrder.value = null
  } catch (error) {
    if (error === 'cancel') return
    showFailToast(error instanceof Error ? error.message : '下单失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

async function continueCheckout(profile: H5UserProfile) {
  if (!pendingCheckout.value || continuingAfterProfile.value || submitting.value) return
  const profileMessage = validateUser(profile)
  if (profileMessage) {
    showToast(profileMessage)
    return
  }

  pendingCheckout.value = false
  continuingAfterProfile.value = true
  try {
    await createAndPay(profile)
  } finally {
    continuingAfterProfile.value = false
  }
}

function cancelPendingCheckout() {
  pendingCheckout.value = false
}

async function focusPurchase() {
  if (!books.value.length) {
    await loadBooks()
  }
  await nextTick()
  sectionRef.value?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

defineExpose({ cancelPendingCheckout, continueCheckout, focusPurchase })
</script>

<template>
  <section ref="sectionRef" class="inline-purchase h5-card">
    <div class="section-head">
      <h2>选择教辅资料</h2>
    </div>

    <van-loading v-if="loading" class="book-loading" />
    <van-empty v-else-if="!books.length" image-size="80" description="暂无可购买书籍" />
    <template v-else>
      <div class="book-list">
        <BookPurchaseItem
          v-for="book in visibleBooks"
          :key="book.id"
          :book="book"
          :quantity="quantities[book.id] || 0"
          @update:quantity="setQuantity(book.id, $event)"
        />
      </div>

      <button v-if="canToggleBooks" type="button" class="toggle-books" @click="expanded = !expanded">
        {{ expanded ? '收起' : '查看更多书籍' }}
      </button>
    </template>

    <div class="purchase-footer">
      <div>
        <p>已选 {{ selectedCount }} 本</p>
        <strong>合计：￥{{ formatMoney(totalAmount) }}</strong>
      </div>
      <van-button
        type="primary"
        :loading="submitting || requestingCheckout"
        :disabled="checkoutDisabled"
        class="pay-button"
        @click="requestCheckout"
      >
        确认下单
      </van-button>
    </div>
  </section>
</template>

<style scoped>
.inline-purchase {
  display: grid;
  width: 100%;
  min-width: 0;
  gap: 12px;
  box-sizing: border-box;
  padding: 16px 14px 0;
  overflow: hidden;
  border: 1px solid var(--color-border);
  border-radius: 16px;
  background: var(--color-card);
  box-shadow: var(--shadow-card);
}

.section-head {
  display: flex;
  min-width: 0;
}

.section-head h2 {
  margin: 0;
  color: var(--color-text-main);
  font-size: 18px;
}

.book-loading {
  display: flex;
  justify-content: center;
  padding: 36px 0;
}

.book-list {
  display: grid;
  width: 100%;
  min-width: 0;
  gap: 10px;
}

.toggle-books {
  width: 100%;
  height: 40px;
  border: 1px solid var(--color-border);
  border-radius: 12px;
  color: var(--color-primary);
  background: #fff;
  font-size: 14px;
  font-weight: 700;
}

.purchase-footer {
  position: sticky;
  bottom: 0;
  z-index: 2;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin: 2px -14px 0;
  padding: 12px 14px calc(16px + env(safe-area-inset-bottom));
  border-top: 1px solid var(--color-border);
  background: rgb(255 255 255 / 96%);
  backdrop-filter: blur(10px);
}

.purchase-footer > div {
  min-width: 0;
}

.purchase-footer p {
  margin: 0 0 4px;
  color: var(--color-text-secondary);
  font-size: 12px;
}

.purchase-footer strong {
  display: block;
  overflow-wrap: anywhere;
  color: var(--color-accent);
  font-size: 18px;
}

.pay-button {
  min-width: 116px;
  border-radius: 14px;
  background: var(--color-primary);
}

@media (max-width: 360px) {
  .inline-purchase {
    padding-right: 10px;
    padding-left: 10px;
  }

  .purchase-footer {
    align-items: stretch;
    flex-direction: column;
    margin-right: -10px;
    margin-left: -10px;
    padding-right: 10px;
    padding-left: 10px;
  }

  .pay-button {
    width: 100%;
  }
}
</style>
