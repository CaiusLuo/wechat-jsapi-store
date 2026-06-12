<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { useBookStore } from '@/stores/book'
import { formatMoney } from '@/utils/format'

const router = useRouter()
const bookStore = useBookStore()

onMounted(async () => {
  if (!bookStore.books.length) {
    await bookStore.fetchBooks()
  }
})

function buy(bookId: number) {
  router.push({ path: '/h5/order/create', query: { bookId } })
}

function openDetail(bookId: number) {
  router.push(`/h5/books/${bookId}`)
}

function onImageError(event: Event) {
  const image = event.target as HTMLImageElement
  image.style.display = 'none'
  showToast('部分封面加载失败')
}
</script>

<template>
  <main class="h5-page">
    <div class="h5-shell">
      <section class="page-head">
        <div>
          <h1>公众号购书</h1>
        </div>
      </section>

      <van-loading v-if="bookStore.loading" class="page-loading" />

      <van-empty v-else-if="!bookStore.books.length" description="暂无可购买书籍" />

      <section v-else class="book-list">
        <article v-for="book in bookStore.books" :key="book.id" class="book-card h5-card">
          <button class="cover" type="button" @click="openDetail(book.id)">
            <img v-if="book.coverUrl" :src="book.coverUrl" :alt="book.name" @error="onImageError" />
            <span v-else>{{ book.name.slice(0, 2) }}</span>
          </button>

          <div class="book-main">
            <button class="book-title" type="button" @click="openDetail(book.id)">
              {{ book.name }}
            </button>
            <div class="book-actions">
              <strong>￥{{ formatMoney(book.price) }}</strong>
              <van-button size="small" type="primary" round @click="buy(book.id)">购买</van-button>
            </div>
          </div>
        </article>
      </section>
    </div>
  </main>
</template>

<style scoped>
.h5-page {
  background: var(--color-bg);
}

.page-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
}

.eyebrow {
  margin: 0 0 4px;
  color: #5d6b7a;
  font-size: 13px;
}

h1 {
  margin: 0;
  color: #17212b;
  font-size: 24px;
  line-height: 1.2;
}

.head-note {
  color: #6b7682;
  font-size: 12px;
  white-space: nowrap;
}

.page-loading {
  display: flex;
  justify-content: center;
  padding: 60px 0;
}

.book-list {
  display: grid;
  gap: 12px;
  min-width: 0;
}

.book-card {
  display: grid;
  grid-template-columns: 96px minmax(0, 1fr);
  gap: 12px;
  padding: 12px;
  border: 1px solid #edf0f3;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 8px 22px rgb(31 41 55 / 5%);
}

.cover {
  display: grid;
  width: 96px;
  height: 128px;
  place-items: center;
  padding: 0;
  overflow: hidden;
  border: 0;
  border-radius: 6px;
  color: #52616f;
  background: #e9eef3;
  font-size: 22px;
}

.cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.book-main {
  display: flex;
  min-width: 0;
  flex-direction: column;
}

.book-title {
  padding: 0;
  border: 0;
  color: #17212b;
  background: transparent;
  font-size: 17px;
  font-weight: 700;
  line-height: 1.35;
  text-align: left;
}

.book-subtitle {
  margin: 4px 0 0;
  color: #5d6b7a;
  font-size: 13px;
}

.book-intro {
  display: -webkit-box;
  margin: 8px 0 10px;
  overflow: hidden;
  color: #66717d;
  font-size: 13px;
  line-height: 1.55;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 3;
}

.book-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-top: auto;
}

.book-actions strong {
  color: #d9480f;
  font-size: 18px;
}
</style>
