export interface ApiResult<T> {
  code: number
  message: string
  data: T
}

export interface Book {
  id: number
  name: string
  subtitle?: string
  coverUrl?: string
  originalPrice?: number | string | null
  price: number | string
  intro?: string
  stock: number
  sort: number
  status: 0 | 1
}

export interface BookPayload {
  name: string
  subtitle?: string
  coverUrl?: string
  originalPrice: number | string
  price: number | string
  intro?: string
  stock: number
  sort: number
  status: 0 | 1
}

export interface UploadedFile {
  publicUrl: string
}

export interface H5Config {
  siteTitle: string
  siteSubtitle: string
  serviceWechat: string
  servicePhone: string
  workTime: string
  noticeText: string
}

export interface H5UserProfile {
  openid?: string
  nickname?: string
  avatar?: string
  receiverName: string
  phone: string
  school: string
  province: string
  city: string
  district: string
  detailAddress: string
  profileCompleted?: boolean
}

export type H5UserProfilePayload = Pick<
  H5UserProfile,
  'receiverName' | 'phone' | 'school' | 'province' | 'city' | 'district' | 'detailAddress'
>

export interface OrderBookItem {
  bookId: number
  quantity: number
}

export interface CreateOrderPayload {
  receiverName: string
  phone: string
  school: string
  province: string
  city: string
  district: string
  detailAddress: string
  remark?: string
  items: OrderBookItem[]
}

export interface JsapiPayParams {
  appId: string
  timeStamp: string
  nonceStr: string
  packageValue?: string
  package?: string
  signType: string
  paySign: string
}

export interface CreatePayOrderResult {
  orderNo: string
  payAmount: number | string
  payParams: JsapiPayParams
}

export type OrderStatus = 'CREATED' | 'PAID' | 'DELIVERING' | 'FINISHED' | 'CANCELLED'

export interface OrderItem {
  id?: number
  bookId: number
  bookName: string
  coverUrl?: string
  price: number | string
  quantity: number
  subtotal: number | string
}

export interface OrderDetail {
  id?: number
  orderNo: string
  status: OrderStatus
  statusText?: string
  totalAmount?: number | string
  payAmount: number | string
  receiverName: string
  phone: string
  school?: string
  province?: string
  city?: string
  district?: string
  detailAddress?: string
  remark?: string
  trackingCompany?: string
  trackingNo?: string
  items?: OrderItem[]
  payTime?: string
  createTime?: string
  deliverTime?: string
  finishTime?: string
  updateTime?: string
  payStatus?: number
  payStatusText?: string
  itemSummary?: string
  payment?: PaymentInfo | null
}

export interface PaymentInfo {
  transactionId?: string
  amount: number | string
  payStatus: number
  payStatusText?: string
  payTime?: string
}

export interface PageResult<T> {
  records?: T[]
  list?: T[]
  total: number
  current?: number
  size?: number
}

export interface AdminBookQuery {
  name?: string
  status?: 0 | 1
  page?: number
  size?: number
}

export interface AdminLoginPayload {
  username: string
  password: string
}

export interface AdminLoginResult {
  token: string
  expiresAt?: number | null
  username?: string
}

export interface AdminOrderQuery {
  orderNo?: string
  phone?: string
  receiverName?: string
  status?: OrderStatus
  startTime?: string
  endTime?: string
  page?: number
  size?: number
}

export interface AdminOrderStatusPayload {
  status: OrderStatus
}

export interface AdminDeliverOrderPayload {
  trackingNo?: string
}

export interface WeeklySalesItem {
  date: string
  salesVolume: number
  salesAmount: number | string
}

export interface AdminDashboardOverview {
  todayOrderCount: number
  pendingDeliveryCount: number
  onSaleBookCount: number
  weeklySales: WeeklySalesItem[]
}
