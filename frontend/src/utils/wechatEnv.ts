export function isWechatBrowser() {
  if (typeof window === 'undefined') return false
  return /MicroMessenger/i.test(window.navigator.userAgent)
}

export function isWeixinBridgeReady() {
  return typeof window !== 'undefined' && typeof window.WeixinJSBridge !== 'undefined'
}

export function waitForWeixinBridge() {
  return new Promise<void>((resolve, reject) => {
    if (typeof window === 'undefined') {
      reject(new Error('当前环境不支持微信支付'))
      return
    }

    if (isWeixinBridgeReady()) {
      resolve()
      return
    }

    const timer = window.setTimeout(() => {
      document.removeEventListener('WeixinJSBridgeReady', onReady)
      reject(new Error('微信支付环境未就绪，请稍后重试'))
    }, 8000)

    function onReady() {
      window.clearTimeout(timer)
      resolve()
    }

    document.addEventListener('WeixinJSBridgeReady', onReady, { once: true })
  })
}
