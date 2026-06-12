<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import { createPayOrder } from '@/api/order'
import type { OrderDetail } from '@/api/types'
import { useBookStore } from '@/stores/book'
import { formatMoney } from '@/utils/format'
import { saveLocalOrderDetail, savePayResult } from '@/utils/storage'

const route = useRoute()
const router = useRouter()
const bookStore = useBookStore()
const submitting = ref(false)
const bookId = computed(() => Number(route.query.bookId))
const book = computed(() => bookStore.findById(bookId.value))

const form = reactive({
  receiverName: '',
  phone: '',
  school: '',
  province: '',
  city: '',
  district: '',
  detailAddress: '',
  remark: '',
  quantity: 1,
})

const totalAmount = computed(() => Number(book.value?.price || 0) * form.quantity)

onMounted(async () => {
  if (!bookStore.books.length) {
    await bookStore.fetchBooks()
  }
})

function validate() {
  if (!book.value) return '请选择书籍'
  if (!form.receiverName.trim()) return '请填写收货人'
  if (!/^1[3-9]\d{9}$/.test(form.phone)) return '请填写正确手机号'
  if (!form.province.trim() || !form.city.trim() || !form.district.trim() || !form.detailAddress.trim()) {
    return '请填写完整收货地址'
  }
  if (form.quantity <= 0) return '数量必须大于 0'
  return ''
}

async function submit() {
  if (submitting.value) return
  const message = validate()
  if (message) {
    showToast(message)
    return
  }

  submitting.value = true
  try {
    const result = await createPayOrder({
      receiverName: form.receiverName.trim(),
      phone: form.phone.trim(),
      school: form.school.trim(),
      province: form.province.trim(),
      city: form.city.trim(),
      district: form.district.trim(),
      detailAddress: form.detailAddress.trim(),
      remark: form.remark.trim(),
      items: [{ bookId: bookId.value, quantity: form.quantity }],
    })

    savePayResult(result)
    const localOrder: OrderDetail = {
      orderNo: result.orderNo,
      payAmount: result.payAmount,
      totalAmount: result.payAmount,
      status: 'CREATED',
      receiverName: form.receiverName,
      phone: form.phone,
      school: form.school,
      province: form.province,
      city: form.city,
      district: form.district,
      detailAddress: form.detailAddress,
      remark: form.remark,
      createTime: new Date().toISOString(),
      items: book.value
        ? [
            {
              bookId: book.value.id,
              bookName: book.value.name,
              coverUrl: book.value.coverUrl,
              price: book.value.price,
              quantity: form.quantity,
              subtotal: totalAmount.value,
            },
          ]
        : [],
    }
    saveLocalOrderDetail(localOrder)
    router.replace(`/h5/pay/${result.orderNo}`)
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <main class="order-page h5-page">
    <div class="order-shell">
      <van-nav-bar title="填写订单" left-arrow @click-left="router.back()" />

      <van-empty v-if="!bookStore.loading && !book" description="请从书籍列表选择要购买的书" />
      <van-loading v-else-if="bookStore.loading" class="page-loading" />

      <section v-else-if="book" class="order-content">
        <article class="book-summary h5-card">
          <div class="cover">
            <img v-if="book.coverUrl" :src="book.coverUrl" :alt="book.name" />
            <span v-else>{{ book.name.slice(0, 2) }}</span>
          </div>
          <div class="book-main">
            <h2>{{ book.name }}</h2>
            <p>{{ book.subtitle || book.intro || '推荐阅读书籍' }}</p>
            <strong>￥{{ formatMoney(book.price) }}</strong>
          </div>
        </article>

        <van-form @submit="submit">
          <van-cell-group inset title="收货信息">
            <van-field v-model="form.receiverName" label="收货人" placeholder="请输入姓名" required />
            <van-field v-model="form.phone" label="手机号" type="tel" placeholder="请输入手机号" required />
            <van-field v-model="form.school" label="学校" placeholder="可选" />
            <van-field v-model="form.province" label="省份" placeholder="例如：广东省" required />
            <van-field v-model="form.city" label="城市" placeholder="例如：深圳市" required />
            <van-field v-model="form.district" label="区县" placeholder="例如：南山区" required />
            <van-field
              v-model="form.detailAddress"
              label="详细地址"
              type="textarea"
              rows="2"
              autosize
              placeholder="街道、门牌号等"
              required
            />
            <van-field v-model="form.remark" label="备注" type="textarea" rows="2" autosize placeholder="可选" />
          </van-cell-group>

          <van-cell-group inset title="购买数量">
            <van-cell title="数量">
              <template #right-icon>
                <van-stepper v-model="form.quantity" :min="1" integer />
              </template>
            </van-cell>
            <van-cell title="合计" :value="`￥${formatMoney(totalAmount)}`" />
          </van-cell-group>

          <div class="submit-bar">
            <van-button block type="primary" native-type="submit" :loading="submitting" :disabled="submitting">
              提交订单
            </van-button>
          </div>
        </van-form>
      </section>
    </div>
  </main>
</template>

<style scoped>
.order-page {
  background: var(--color-bg);
}

.order-shell {
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

.order-content {
  padding: 12px 0 24px;
}

.book-summary {
  display: grid;
  grid-template-columns: 72px minmax(0, 1fr);
  gap: 12px;
  margin: 0 16px 12px;
  padding: 12px;
  border-radius: 8px;
  background: #fff;
}

.book-main {
  min-width: 0;
}

.cover {
  display: grid;
  width: 72px;
  height: 96px;
  place-items: center;
  overflow: hidden;
  border-radius: 6px;
  color: #52616f;
  background: #e9eef3;
}

.cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

h2 {
  margin: 0;
  font-size: 17px;
}

.book-summary p {
  display: -webkit-box;
  margin: 6px 0 10px;
  overflow: hidden;
  color: #66717d;
  font-size: 13px;
  line-height: 1.5;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.book-summary strong {
  color: #d9480f;
  font-size: 18px;
}

.submit-bar {
  padding: 18px 16px 0;
}
</style>
