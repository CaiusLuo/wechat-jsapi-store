import http from './http'
import type { AdminBookQuery, Book, BookPayload, PageResult, UploadedFile } from './types'

function sanitizeBookQuery(query: AdminBookQuery = {}) {
  return {
    ...(query.name?.trim() ? { name: query.name.trim() } : {}),
    ...(query.status === 0 || query.status === 1 ? { status: query.status } : {}),
    ...(query.page ? { page: query.page } : {}),
    ...(query.size ? { size: query.size } : {}),
  }
}

export function listAdminBooks(query: AdminBookQuery = {}) {
  return http.get<unknown, PageResult<Book>>('/admin/books', {
    params: sanitizeBookQuery(query),
  })
}

export function createAdminBook(payload: BookPayload) {
  return http.post<unknown, Book>('/admin/books', payload)
}

export function updateAdminBook(id: number, payload: BookPayload) {
  return http.put<unknown, Book>(`/admin/books/${id}`, payload)
}

export function updateAdminBookStatus(id: number, status: 0 | 1) {
  return http.patch<unknown, Book>(`/admin/books/${id}/status`, { status })
}

export function deleteAdminBook(id: number) {
  return http.delete<unknown, void>(`/admin/books/${id}`)
}

export function uploadAdminFile(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return http.post<unknown, UploadedFile>('/admin/files/upload', formData)
}
