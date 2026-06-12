import type { CreatePayOrderResult, OrderDetail } from '@/api/types'

const PAY_RESULT_PREFIX = 'wechat_store_pay_result_'
const ORDER_DETAIL_PREFIX = 'wechat_store_order_detail_'
const ORDER_LIST_KEY = 'wechat_store_h5_order_list'
const PURCHASE_INTENT_KEY = 'wechat_store_h5_purchase_intent'

export interface PurchaseIntent {
  openPurchase: boolean
  createdAt: number
}

export function savePayResult(result: CreatePayOrderResult) {
  localStorage.setItem(`${PAY_RESULT_PREFIX}${result.orderNo}`, JSON.stringify(result))
}

export function getPayResult(orderNo: string) {
  const raw = localStorage.getItem(`${PAY_RESULT_PREFIX}${orderNo}`)
  if (!raw) return null
  try {
    return JSON.parse(raw) as CreatePayOrderResult
  } catch {
    return null
  }
}

export function saveLocalOrderDetail(order: OrderDetail) {
  localStorage.setItem(`${ORDER_DETAIL_PREFIX}${order.orderNo}`, JSON.stringify(order))
  const orderNos = listLocalOrderNos().filter((orderNo) => orderNo !== order.orderNo)
  localStorage.setItem(ORDER_LIST_KEY, JSON.stringify([order.orderNo, ...orderNos].slice(0, 20)))
}

export function removeLocalOrderDetail(orderNo: string) {
  localStorage.removeItem(`${ORDER_DETAIL_PREFIX}${orderNo}`)
  localStorage.setItem(ORDER_LIST_KEY, JSON.stringify(listLocalOrderNos().filter((item) => item !== orderNo)))
}

export function getLocalOrderDetail(orderNo: string) {
  const raw = localStorage.getItem(`${ORDER_DETAIL_PREFIX}${orderNo}`)
  if (!raw) return null
  try {
    return JSON.parse(raw) as OrderDetail
  } catch {
    return null
  }
}

export function listLocalOrderNos() {
  const raw = localStorage.getItem(ORDER_LIST_KEY)
  if (!raw) return []
  try {
    const value = JSON.parse(raw)
    return Array.isArray(value) ? value.filter((item): item is string => typeof item === 'string') : []
  } catch {
    return []
  }
}

export function listLocalOrderDetails() {
  return listLocalOrderNos()
    .map((orderNo) => getLocalOrderDetail(orderNo))
    .filter((order): order is OrderDetail => Boolean(order))
}

export function savePurchaseIntent(intent: PurchaseIntent = { openPurchase: true, createdAt: Date.now() }) {
  sessionStorage.setItem(PURCHASE_INTENT_KEY, JSON.stringify(intent))
}

export function consumePurchaseIntent() {
  const raw = sessionStorage.getItem(PURCHASE_INTENT_KEY)
  sessionStorage.removeItem(PURCHASE_INTENT_KEY)
  if (!raw) return null
  try {
    const value = JSON.parse(raw) as PurchaseIntent
    return value.openPurchase ? value : null
  } catch {
    return null
  }
}
