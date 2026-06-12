import http from './http'
import type { H5Config } from './types'

export const defaultH5Config: H5Config = {
  siteTitle: '教材购书服务',
  siteSubtitle: '个人信息与教材订购入口',
  serviceWechat: '请联系管理员',
  servicePhone: '待配置',
  workTime: '09:00 - 18:00',
  noticeText: '下单后请保持电话畅通，配送信息以后续通知为准。',
}

export function getH5Config() {
  return http.get<unknown, H5Config>('/h5/config')
}
