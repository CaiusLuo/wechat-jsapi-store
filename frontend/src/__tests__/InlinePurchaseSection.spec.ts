import { flushPromises, mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import type { Book, OrderDetail } from '@/api/types'
import type { H5UserProfile } from '@/api/h5User'
import InlinePurchaseSection from '@/views/h5/components/InlinePurchaseSection.vue'

const mocks = vi.hoisted(() => ({
  getH5Books: vi.fn(),
  createOrderAndPay: vi.fn(),
  getH5OrderDetail: vi.fn(),
  ensureLogin: vi.fn(),
  pay: vi.fn(),
  showToast: vi.fn(),
}))

vi.mock('@/api/h5Book', () => ({
  getH5Books: mocks.getH5Books,
}))

vi.mock('@/api/h5Order', () => ({
  createOrderAndPay: mocks.createOrderAndPay,
  getH5OrderDetail: mocks.getH5OrderDetail,
}))

vi.mock('@/services/authService', () => ({
  AuthService: {
    ensureLogin: mocks.ensureLogin,
  },
}))

vi.mock('@/services/payService', () => ({
  PayService: {
    pay: mocks.pay,
  },
}))

vi.mock('@/utils/storage', () => ({
  saveLocalOrderDetail: vi.fn(),
  savePayResult: vi.fn(),
}))

vi.mock('vant', () => ({
  showFailToast: vi.fn(),
  showSuccessToast: vi.fn(),
  showToast: mocks.showToast,
}))

const books: Book[] = Array.from({ length: 5 }, (_, index) => ({
  id: index + 1,
  name: `教材 ${index + 1}`,
  subtitle: `副标题 ${index + 1}`,
  intro: `简介 ${index + 1}`,
  originalPrice: 100,
  price: 80,
  stock: 100,
  sort: index + 1,
  status: 1,
}))

const completeProfile: H5UserProfile = {
  openid: 'openid',
  nickname: '微信用户',
  avatar: '',
  receiverName: '测试用户',
  phone: `138${'0'.repeat(8)}`,
  school: '示例学校',
  province: '北京市',
  city: '北京市',
  district: '东城区',
  detailAddress: '示例路 1 号',
  profileCompleted: true,
}

const paidOrder: OrderDetail = {
  orderNo: 'ORDER-1',
  status: 'PAID',
  payAmount: 80,
  receiverName: completeProfile.receiverName,
  phone: completeProfile.phone,
}

const BookStub = {
  props: ['book', 'quantity'],
  emits: ['update:quantity'],
  template: `
    <div class="book-stub">
      <span class="book-name">{{ book.name }}</span>
      <span class="book-quantity">{{ quantity }}</span>
      <button class="increment" @click="$emit('update:quantity', quantity + 1)">+</button>
    </div>
  `,
}

const VanButtonStub = {
  props: ['loading', 'disabled'],
  emits: ['click'],
  template: `
    <button :disabled="disabled" @click="$emit('click', $event)">
      <slot />
    </button>
  `,
}

function mountSection() {
  return mount(InlinePurchaseSection, {
    props: { user: completeProfile },
    global: {
      stubs: {
        BookPurchaseItem: BookStub,
        'van-button': VanButtonStub,
        'van-loading': { template: '<div class="loading" />' },
        'van-empty': { template: '<div class="empty" />' },
      },
    },
  })
}

async function selectFirstBook(wrapper: ReturnType<typeof mountSection>) {
  await wrapper.find('.increment').trigger('click')
}

describe('InlinePurchaseSection', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mocks.getH5Books.mockResolvedValue(books)
    mocks.ensureLogin.mockResolvedValue(true)
    mocks.createOrderAndPay.mockResolvedValue({
      orderNo: 'ORDER-1',
      payAmount: 80,
      payParams: {
        appId: 'app',
        timeStamp: '1',
        nonceStr: 'nonce',
        package: 'prepay_id=1',
        signType: 'RSA',
        paySign: 'sign',
      },
    })
    mocks.pay.mockResolvedValue(undefined)
    mocks.getH5OrderDetail.mockResolvedValue(paidOrder)
  })

  it('shows three books by default and toggles all books without losing quantities', async () => {
    const wrapper = mountSection()
    await flushPromises()

    expect(wrapper.findAll('.book-stub')).toHaveLength(3)
    expect(wrapper.find('.toggle-books').text()).toBe('查看更多书籍')
    expect(wrapper.text()).not.toContain('共 5 本')
    await selectFirstBook(wrapper)
    expect(wrapper.find('.book-quantity').text()).toBe('1')

    await wrapper.find('.toggle-books').trigger('click')
    expect(wrapper.findAll('.book-stub')).toHaveLength(5)
    expect(wrapper.find('.toggle-books').text()).toBe('收起')
    expect(wrapper.find('.book-quantity').text()).toBe('1')

    await wrapper.find('.toggle-books').trigger('click')
    expect(wrapper.findAll('.book-stub')).toHaveLength(3)
    expect(wrapper.find('.toggle-books').text()).toBe('查看更多书籍')
    expect(wrapper.find('.book-quantity').text()).toBe('1')
  })

  it('requests profile confirmation on every checkout attempt after cancellation', async () => {
    const wrapper = mountSection()
    await flushPromises()
    await selectFirstBook(wrapper)

    await wrapper.find('.pay-button').trigger('click')
    await flushPromises()
    expect(wrapper.emitted('require-profile')).toHaveLength(1)

    wrapper.vm.cancelPendingCheckout()
    await wrapper.vm.$nextTick()
    await wrapper.find('.pay-button').trigger('click')
    await flushPromises()
    expect(wrapper.emitted('require-profile')).toHaveLength(2)
  })

  it('keeps selection and does not create an order when profile confirmation is cancelled', async () => {
    const wrapper = mountSection()
    await flushPromises()
    await selectFirstBook(wrapper)

    await wrapper.find('.pay-button').trigger('click')
    await flushPromises()
    wrapper.vm.cancelPendingCheckout()

    expect(mocks.createOrderAndPay).not.toHaveBeenCalled()
    expect(wrapper.text()).toContain('已选 1 本')
  })

  it('creates one order when the confirmed profile is submitted more than once', async () => {
    const wrapper = mountSection()
    await flushPromises()
    await selectFirstBook(wrapper)
    await wrapper.find('.pay-button').trigger('click')
    await flushPromises()

    await Promise.all([
      wrapper.vm.continueCheckout(completeProfile),
      wrapper.vm.continueCheckout(completeProfile),
    ])

    expect(mocks.createOrderAndPay).toHaveBeenCalledTimes(1)
    expect(mocks.pay).toHaveBeenCalledTimes(1)
    expect(mocks.createOrderAndPay).toHaveBeenCalledWith(
      expect.objectContaining({
        receiverName: completeProfile.receiverName,
        detailAddress: completeProfile.detailAddress,
        items: [{ bookId: 1, quantity: 1 }],
      }),
    )
  })
})
