import { defineStore } from 'pinia'
import { listH5Books } from '@/api/book'
import type { Book } from '@/api/types'

export const useBookStore = defineStore('book', {
  state: () => ({
    books: [] as Book[],
    loading: false,
  }),
  getters: {
    findById: (state) => (id: number) => state.books.find((book) => book.id === id),
  },
  actions: {
    async fetchBooks() {
      this.loading = true
      try {
        this.books = await listH5Books()
      } finally {
        this.loading = false
      }
    },
  },
})
