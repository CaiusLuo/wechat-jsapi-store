import http from './http'
import type { AdminDeliverOrderPayload, AdminOrderQuery, AdminOrderStatusPayload, OrderDetail, PageResult } from './types'

function sanitizeOrderQuery(query: AdminOrderQuery = {}) {
  return {
    ...(query.orderNo?.trim() ? { orderNo: query.orderNo.trim() } : {}),
    ...(query.phone?.trim() ? { phone: query.phone.trim() } : {}),
    ...(query.receiverName?.trim() ? { receiverName: query.receiverName.trim() } : {}),
    ...(query.status ? { status: query.status } : {}),
    ...(query.startTime?.trim() ? { startTime: query.startTime.trim() } : {}),
    ...(query.endTime?.trim() ? { endTime: query.endTime.trim() } : {}),
    ...(query.page ? { page: query.page } : {}),
    ...(query.size ? { size: query.size } : {}),
  }
}

export function listAdminOrders(query: AdminOrderQuery) {
  return http.get<unknown, PageResult<OrderDetail>>('/admin/orders', {
    params: sanitizeOrderQuery(query),
  })
}

export function getAdminOrderDetail(orderNo: string) {
  return http.get<unknown, OrderDetail>(`/admin/orders/${orderNo}`)
}

export function markOrderDelivering(orderNo: string, payload: AdminDeliverOrderPayload = {}, silent = false) {
  return http.post<unknown, void>(`/admin/orders/${orderNo}/deliver`, payload, { silent })
}

export function markOrderFinished(orderNo: string, silent = false) {
  return http.post<unknown, void>(`/admin/orders/${orderNo}/finish`, undefined, { silent })
}

export function cancelAdminOrder(orderNo: string, silent = false) {
  return http.post<unknown, void>(`/admin/orders/${orderNo}/cancel`, undefined, { silent })
}

export function updateAdminOrderStatus(orderNo: string, payload: AdminOrderStatusPayload) {
  return http.put<unknown, void>(`/admin/orders/${orderNo}/status`, payload)
}
