import { createOrderAndPay } from '@/api/h5Order'
import type { CreateOrderPayload, CreatePayOrderResult, JsapiPayParams } from '@/api/types'
import { isWechatBrowser, waitForWeixinBridge } from '@/utils/wechatEnv'

declare global {
  interface Window {
    WeixinJSBridge?: {
      invoke: (
        name: string,
        params: Record<string, string>,
        callback: (res: { err_msg?: string }) => void,
      ) => void
    }
  }
}

export class WechatPayError extends Error {
  order?: CreatePayOrderResult

  constructor(message: string, order?: CreatePayOrderResult) {
    super(message)
    this.name = 'WechatPayError'
    this.order = order
  }
}

export function normalizePayParams(params: JsapiPayParams) {
  return {
    appId: params.appId,
    timeStamp: params.timeStamp,
    nonceStr: params.nonceStr,
    package: params.package || params.packageValue || '',
    signType: params.signType,
    paySign: params.paySign,
  }
}

export async function invokeWechatPay(params: JsapiPayParams) {
  if (!isWechatBrowser()) {
    throw new Error('请在微信内打开并完成支付')
  }

  await waitForWeixinBridge()

  return new Promise<void>((resolve, reject) => {
    if (!window.WeixinJSBridge) {
      reject(new Error('微信支付环境未就绪'))
      return
    }

    window.WeixinJSBridge.invoke('getBrandWCPayRequest', normalizePayParams(params), (res) => {
      if (res.err_msg === 'get_brand_wcpay_request:ok') {
        resolve()
        return
      }
      reject(new Error(res.err_msg || '支付未完成'))
    })
  })
}

export async function createOrderAndRequestWechatPay(payload: CreateOrderPayload) {
  const order = await createOrderAndPay(payload)

  try {
    await invokeWechatPay(order.payParams)
  } catch (error) {
    throw new WechatPayError(error instanceof Error ? error.message : '支付未完成', order)
  }

  return order
}

export const chooseWxPay = invokeWechatPay
export { isWechatBrowser }
