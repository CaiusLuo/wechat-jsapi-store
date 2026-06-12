import { flushPromises, mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import ProfileHome from '@/views/h5/ProfileHome.vue'

const mocks = vi.hoisted(() => ({
  push: vi.fn(),
  listH5Orders: vi.fn(),
  ensureLogin: vi.fn(),
  getCurrentUser: vi.fn(),
}))

vi.mock('vue-router', () => ({
  useRouter: () => ({ push: mocks.push }),
}))

vi.mock('@/api/h5Config', () => ({
  defaultH5Config: {
    siteTitle: '教材订购',
    siteSubtitle: '',
    serviceWechat: '',
    servicePhone: '',
    workTime: '',
    noticeText: '',
  },
  getH5Config: vi.fn().mockResolvedValue({ siteTitle: '教材订购' }),
}))

vi.mock('@/api/h5User', () => ({
  buildDefaultH5UserProfile: () => ({
    openid: '',
    nickname: '',
    avatar: '',
    receiverName: '',
    phone: '',
    school: '',
    province: '',
    city: '',
    district: '',
    detailAddress: '',
    profileCompleted: false,
  }),
  completeWxOAuthCallback: vi.fn(),
}))

vi.mock('@/api/h5Order', () => ({
  listH5Orders: mocks.listH5Orders,
}))

vi.mock('@/services/authService', () => ({
  AuthService: {
    ensureLogin: mocks.ensureLogin,
    getCurrentUser: mocks.getCurrentUser,
  },
}))

vi.mock('@/utils/storage', () => ({
  consumePurchaseIntent: vi.fn().mockReturnValue(null),
}))

vi.mock('@/utils/wechatEnv', () => ({
  isWechatBrowser: vi.fn().mockReturnValue(false),
}))

vi.mock('vant', () => ({
  showToast: vi.fn(),
}))

describe('ProfileHome', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mocks.ensureLogin.mockResolvedValue(true)
    mocks.getCurrentUser.mockResolvedValue({
      receiverName: '',
      phone: '',
      school: '',
      province: '',
      city: '',
      district: '',
      detailAddress: '',
    })
  })

  it('does not load orders on the home page and opens the dedicated order route', async () => {
    const wrapper = mount(ProfileHome, {
      global: {
        stubs: {
          InlinePurchaseSection: { template: '<section class="purchase-stub" />' },
          NoticeCard: { template: '<section class="notice-stub" />' },
          UserProfileCard: { template: '<div />' },
        },
      },
    })
    await flushPromises()

    expect(mocks.listH5Orders).not.toHaveBeenCalled()
    await wrapper.find('.order-entry').trigger('click')
    expect(mocks.push).toHaveBeenCalledWith('/h5/orders')
  })
})
