import type { JsapiPayParams } from '@/api/types'
import { chooseWxPay } from '@/utils/wechatPay'

export const PayService = {
  async pay(_orderNo: string, payParams: JsapiPayParams) {
    await chooseWxPay(payParams)
  },
}
