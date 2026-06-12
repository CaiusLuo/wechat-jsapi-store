import http from './http'
import type { CreateOrderPayload, CreatePayOrderResult, OrderDetail } from './types'

export function createOrderAndPay(payload: CreateOrderPayload) {
  return http.post<unknown, CreatePayOrderResult>('/h5/orders/pay', payload)
}

export function getH5OrderDetail(orderNo: string) {
  return http.get<unknown, OrderDetail>(`/h5/orders/${orderNo}`)
}

export function listH5Orders(params?: { unfinished?: boolean }) {
  return http.get<unknown, OrderDetail[]>('/h5/orders/my', { params })
}

export function cancelH5Order(orderNo: string) {
  return http.post<unknown, void>(`/h5/orders/${orderNo}/cancel`)
}
