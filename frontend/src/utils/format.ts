import type { OrderStatus } from '@/api/types'

export const orderStatusText: Record<OrderStatus, string> = {
  CREATED: '待支付',
  PAID: '待发货',
  DELIVERING: '配送中',
  FINISHED: '已完成',
  CANCELLED: '已取消',
}

export const orderStatusType: Record<OrderStatus, 'primary' | 'success' | 'warning' | 'danger' | 'info'> = {
  CREATED: 'warning',
  PAID: 'primary',
  DELIVERING: 'primary',
  FINISHED: 'success',
  CANCELLED: 'info',
}

export const payStatusText: Record<number, string> = {
  0: '未支付',
  1: '已支付',
  2: '已关闭',
}

export function formatOrderStatus(status?: OrderStatus | string | number, statusText?: string) {
  if (statusText) return statusText
  if (!status) return '-'
  return orderStatusText[status as OrderStatus] || '未知状态'
}

export function formatPayStatus(status?: number | string, statusText?: string) {
  if (statusText) return statusText
  if (status === undefined || status === null || status === '') return '-'
  const normalized = Number(status)
  return payStatusText[normalized] || '未知状态'
}

export function formatMoney(value?: number | string) {
  const amount = Number(value || 0)
  return amount.toFixed(2)
}

export function formatAddress(parts: Array<string | undefined>) {
  return parts.filter((part) => Boolean(part?.trim())).join('')
}

export function normalizeList<T>(value: { records?: T[]; list?: T[] } | T[] | undefined) {
  if (!value) return []
  if (Array.isArray(value)) return value
  return value.records || value.list || []
}
