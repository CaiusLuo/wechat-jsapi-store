<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useBookStore } from '@/stores/book'
import { formatMoney } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const bookStore = useBookStore()
const bookId = computed(() => Number(route.params.id))
const book = computed(() => bookStore.findById(bookId.value))

onMounted(async () => {
  if (!bookStore.books.length) {
    await bookStore.fetchBooks()
  }
})

function buy() {
  router.push({ path: '/h5/order/create', query: { bookId: bookId.value } })
}
</script>

<template>
  <main class="detail-page h5-page">
    <div class="detail-shell">
      <van-nav-bar title="书籍详情" left-arrow @click-left="router.back()" />

      <van-empty v-if="!bookStore.loading && !book" description="书籍不存在或已下架" />
      <van-loading v-else-if="bookStore.loading" class="page-loading" />

      <section v-else-if="book" class="book-detail">
        <div class="cover">
          <img v-if="book.coverUrl" :src="book.coverUrl" :alt="book.name" />
          <span v-else>{{ book.name.slice(0, 2) }}</span>
        </div>

        <div class="content h5-card">
          <h1>{{ book.name }}</h1>
          <strong class="price">￥{{ formatMoney(book.price) }}</strong>
        </div>

        <van-action-bar>
          <van-action-bar-button type="primary" text="立即购买" @click="buy" />
        </van-action-bar>
      </section>
    </div>
  </main>
</template>

<style scoped>
.detail-page {
  padding-bottom: 64px;
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

.book-detail {
  padding: 16px;
}

.cover {
  display: grid;
  width: 180px;
  height: 240px;
  place-items: center;
  margin: 6px auto 18px;
  overflow: hidden;
  border-radius: 8px;
  color: #52616f;
  background: #e9eef3;
  font-size: 32px;
  box-shadow: 0 14px 34px rgb(31 41 55 / 10%);
}

.cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.content {
  min-width: 0;
  padding: 16px;
  border-radius: 8px;
  background: #fff;
}

:deep(.van-action-bar) {
  right: auto;
  left: 50%;
  width: 100%;
  max-width: 520px;
  transform: translateX(-50%);
}

h1 {
  margin: 0;
  font-size: 22px;
  line-height: 1.35;
}

.subtitle {
  margin: 8px 0 0;
  color: #5d6b7a;
}

.price {
  display: block;
  margin-top: 14px;
  color: #d9480f;
  font-size: 22px;
}

.intro {
  margin: 16px 0 0;
  color: #46515c;
  font-size: 15px;
  line-height: 1.8;
  white-space: pre-wrap;
}
</style>
