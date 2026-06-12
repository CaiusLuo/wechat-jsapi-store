import { describe, expect, it } from 'vitest'
import router from '@/router'

describe('router document title', () => {
  it('uses the exact full title for the H5 home page', async () => {
    await router.push('/h5')

    expect(document.title).toBe('示例教辅资料订购系统 - 微信 JSAPI 商城')
  })

  it('keeps the existing title composition for other routes', async () => {
    await router.push('/h5/orders')

    expect(document.title).toBe('我的订单 - 示例教辅资料订购系统')
  })
})
