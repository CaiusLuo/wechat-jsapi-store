import http from './http'
import type { Book } from './types'

export function getH5Books() {
  return http.get<unknown, Book[]>('/h5/books')
}
