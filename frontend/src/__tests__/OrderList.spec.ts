import { flushPromises, mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import type { OrderDetail } from '@/api/types'
import OrderList from '@/views/h5/OrderList.vue'

const mocks = vi.hoisted(() => ({
  listH5Orders: vi.fn(),
  ensureLogin: vi.fn(),
  routerBack: vi.fn(),
  routerPush: vi.fn(),
}))

vi.mock('vue-router', () => ({
  useRouter: () => ({
    back: mocks.routerBack,
    push: mocks.routerPush,
  }),
}))

vi.mock('@/api/h5User', () => ({
  completeWxOAuthCallback: vi.fn(),
}))

vi.mock('@/api/h5Order', () => ({
  listH5Orders: mocks.listH5Orders,
  cancelH5Order: vi.fn(),
}))

vi.mock('@/services/authService', () => ({
  AuthService: {
    ensureLogin: mocks.ensureLogin,
  },
}))

vi.mock('@/utils/storage', () => ({
  removeLocalOrderDetail: vi.fn(),
}))

vi.mock('vant', () => ({
  showToast: vi.fn(),
  showConfirmDialog: vi.fn(),
  showFailToast: vi.fn(),
  showSuccessToast: vi.fn(),
}))

const orders: OrderDetail[] = [
  {
    orderNo: 'ORDER-CREATED',
    status: 'CREATED',
    payAmount: 80,
    receiverName: '测试用户',
    phone: '00000000000',
    createTime: '2026-06-09T10:00:00',
  },
  {
    orderNo: 'ORDER-PAID',
    status: 'PAID',
    payAmount: 90,
    receiverName: '测试用户',
    phone: '00000000000',
    createTime: '2026-06-09T11:00:00',
  },
  {
    orderNo: 'ORDER-DELIVERING',
    status: 'DELIVERING',
    payAmount: 100,
    receiverName: '测试用户',
    phone: '00000000000',
    createTime: '2026-06-09T12:00:00',
  },
]

const VanButtonStub = {
  props: ['loading', 'disabled'],
  emits: ['click'],
  template: `
    <button :disabled="disabled" @click="$emit('click', $event)">
      <slot />
    </button>
  `,
}

describe('OrderList', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    window.history.replaceState({}, '', '/h5/orders')
    mocks.ensureLogin.mockResolvedValue(true)
    mocks.listH5Orders.mockResolvedValue(orders)
  })

  it('loads, refreshes, and filters orders on the dedicated page', async () => {
    const wrapper = mount(OrderList, {
      global: {
        stubs: {
          'van-nav-bar': { template: '<nav>我的订单</nav>' },
          'van-button': VanButtonStub,
          'van-loading': { template: '<div class="loading" />' },
          'van-empty': { template: '<div class="empty"><slot name="description" /></div>' },
        },
      },
    })
    await flushPromises()

    expect(mocks.listH5Orders).toHaveBeenCalledTimes(1)
    expect(wrapper.text()).toContain('ORDER-CREATED')
    expect(wrapper.text()).toContain('ORDER-PAID')
    expect(wrapper.text()).toContain('ORDER-DELIVERING')

    const shippingTab = wrapper.findAll('.filter-tab').find((tab) => tab.text() === '配送中')
    await shippingTab?.trigger('click')
    expect(wrapper.text()).not.toContain('ORDER-CREATED')
    expect(wrapper.text()).toContain('ORDER-PAID')
    expect(wrapper.text()).toContain('ORDER-DELIVERING')

    await wrapper.find('.refresh-button').trigger('click')
    await flushPromises()
    expect(mocks.listH5Orders).toHaveBeenCalledTimes(2)
  })
})
