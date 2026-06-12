import type { OrderStatus } from '@/api/types'

export type H5OrderFilter = 'ALL' | 'CREATED' | 'SHIPPING' | 'DONE'

export const h5OrderStatusText: Record<OrderStatus, string> = {
  CREATED: '待支付',
  PAID: '配送中',
  DELIVERING: '配送中',
  FINISHED: '已完成',
  CANCELLED: '已取消',
}

export const h5OrderFilterTabs: Array<{ label: string; value: H5OrderFilter }> = [
  { label: '全部', value: 'ALL' },
  { label: '待支付', value: 'CREATED' },
  { label: '配送中', value: 'SHIPPING' },
  { label: '结束订单', value: 'DONE' },
]

export function formatH5OrderStatus(status?: OrderStatus | string, statusText?: string) {
  if (!status) return '-'
  return h5OrderStatusText[status as OrderStatus] || statusText || '未知状态'
}

export function matchesH5OrderFilter(status: OrderStatus, filter: H5OrderFilter) {
  if (filter === 'ALL') return true
  if (filter === 'SHIPPING') return status === 'PAID' || status === 'DELIVERING'
  if (filter === 'DONE') return status === 'FINISHED' || status === 'CANCELLED'
  return status === filter
}
