<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import type { Book } from '@/api/types'
import { formatMoney } from '@/utils/format'
import { displayOriginalPrice, formatDiscount } from '@/utils/pricing'

const props = defineProps<{
  book: Book
  quantity: number
}>()

const emit = defineEmits<{
  (event: 'update:quantity', value: number): void
}>()

const subtotal = computed(() => Number(props.book.price || 0) * props.quantity)
const originalPrice = computed(() => displayOriginalPrice(props.book))
const discountText = computed(() => formatDiscount(props.book.price, props.book.originalPrice))
const coverVisible = ref(Boolean(props.book.coverUrl))

watch(
  () => props.book.coverUrl,
  (coverUrl) => {
    coverVisible.value = Boolean(coverUrl)
  },
)

function updateQuantity(value: number | string) {
  emit('update:quantity', Number(value || 0))
}

function handleCoverError() {
  coverVisible.value = false
}
</script>

<template>
  <article :class="['book-item', { selected: quantity > 0 }]">
    <div class="cover">
      <img v-if="book.coverUrl && coverVisible" :src="book.coverUrl" :alt="book.name" @error="handleCoverError" />
      <span v-else>{{ book.name.slice(0, 2) }}</span>
    </div>

    <div class="book-body">
      <div class="title-row">
        <span v-if="quantity > 0" class="selected-mark" aria-label="已选择">✓</span>
        <h3>{{ book.name }}</h3>
      </div>
      <div class="book-foot">
        <div class="price-area">
          <div class="price-meta">
            <span class="original-price">原价 ¥{{ formatMoney(originalPrice) }}</span>
            <span v-if="discountText" class="discount-badge">{{ discountText }}</span>
          </div>
          <strong>折扣价 ¥{{ formatMoney(book.price) }}</strong>
          <small v-if="quantity > 0">小计 ¥{{ formatMoney(subtotal) }}</small>
        </div>
        <div class="quantity-control">
          <van-stepper
            :model-value="quantity"
            :min="0"
            integer
            button-size="24"
            @update:model-value="updateQuantity"
          />
        </div>
      </div>
    </div>
  </article>
</template>

<style scoped>
.book-item {
  display: grid;
  grid-template-columns: 72px minmax(0, 1fr);
  gap: 12px;
  padding: 12px;
  border: 1px solid var(--color-border);
  border-radius: 14px;
  background: var(--color-card);
}

.book-item.selected {
  border-color: var(--color-primary);
  box-shadow: 0 8px 18px rgb(31 58 95 / 8%);
}

.cover {
  display: grid;
  width: 72px;
  height: 96px;
  place-items: center;
  overflow: hidden;
  border-radius: 10px;
  color: var(--color-primary);
  background: var(--color-primary-soft);
  font-size: 17px;
  font-weight: 700;
}

.cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.book-body {
  min-width: 0;
}

.title-row {
  display: flex;
  min-width: 0;
  align-items: flex-start;
  gap: 6px;
}

.selected-mark {
  display: grid;
  width: 18px;
  height: 18px;
  flex: 0 0 auto;
  place-items: center;
  border-radius: 50%;
  color: #fff;
  background: var(--color-primary);
  font-size: 13px;
  font-weight: 800;
  line-height: 1;
}

.book-body h3 {
  min-width: 0;
  margin: 0;
  flex: 1;
  color: var(--color-text-main);
  font-size: 16px;
  line-height: 1.35;
  overflow-wrap: anywhere;
}

.book-foot {
  margin-top: 10px;
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 10px;
}

.price-area {
  flex: 1 1 auto;
  min-width: 0;
}

.quantity-control {
  flex: 0 0 auto;
  min-width: max-content;
  white-space: nowrap;
}

.quantity-control :deep(.van-stepper) {
  display: inline-flex;
  flex-wrap: nowrap;
  align-items: center;
}

.quantity-control :deep(.van-stepper__minus),
.quantity-control :deep(.van-stepper__input),
.quantity-control :deep(.van-stepper__plus) {
  flex: 0 0 auto;
}

.book-foot strong {
  display: block;
  color: var(--color-accent);
  font-size: 17px;
}

.price-meta {
  display: flex;
  min-width: 0;
  align-items: center;
  gap: 6px;
  margin-bottom: 3px;
  flex-wrap: wrap;
}

.original-price {
  color: var(--color-text-muted);
  font-size: 11px;
  text-decoration: line-through;
}

.discount-badge {
  display: inline-flex;
  height: 18px;
  align-items: center;
  padding: 0 6px;
  border-radius: 9px;
  color: var(--color-accent);
  background: rgb(217 72 15 / 10%);
  font-size: 11px;
  font-weight: 700;
}

.book-foot small {
  display: block;
  margin-top: 3px;
  color: var(--color-text-muted);
  font-size: 11px;
}

@media (max-width: 390px) {
  .book-item {
    grid-template-columns: 56px minmax(0, 1fr);
    gap: 8px;
    padding: 10px;
  }

  .cover {
    width: 56px;
    height: 76px;
    border-radius: 8px;
  }

  .book-foot {
    gap: 6px;
  }

  .book-foot strong {
    font-size: 15px;
  }
}
</style>
