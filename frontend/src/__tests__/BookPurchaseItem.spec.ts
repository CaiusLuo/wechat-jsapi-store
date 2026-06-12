import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import BookPurchaseItem from '@/views/h5/components/BookPurchaseItem.vue'

describe('BookPurchaseItem', () => {
  it('does not render subtitle, intro, or fallback description', () => {
    const wrapper = mount(BookPurchaseItem, {
      props: {
        book: {
          id: 1,
          name: '测试教材',
          subtitle: '不应展示的副标题',
          intro: '不应展示的简介',
          originalPrice: 100,
          price: 80,
          stock: 10,
          sort: 1,
          status: 1,
        },
        quantity: 0,
      },
      global: {
        stubs: {
          'van-stepper': { template: '<div class="stepper" />' },
        },
      },
    })

    expect(wrapper.text()).toContain('测试教材')
    expect(wrapper.text()).toContain('折扣价')
    expect(wrapper.text()).not.toContain('不应展示的副标题')
    expect(wrapper.text()).not.toContain('不应展示的简介')
    expect(wrapper.text()).not.toContain('适合课堂延伸阅读与家庭共读')
  })
})
