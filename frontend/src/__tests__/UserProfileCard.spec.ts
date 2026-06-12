import { mount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'
import UserProfileCard from '@/views/h5/components/UserProfileCard.vue'

vi.mock('@/api/h5User', () => ({
  isUserProfileComplete: vi.fn().mockReturnValue(false),
  updateUserProfile: vi.fn(),
}))

vi.mock('vant', () => ({
  showFailToast: vi.fn(),
  showSuccessToast: vi.fn(),
  showToast: vi.fn(),
}))

const PopupStub = {
  props: ['show'],
  template: '<div v-if="show" class="popup-stub"><slot /></div>',
}

describe('UserProfileCard', () => {
  it('keeps the editor available without rendering a profile card', async () => {
    const wrapper = mount(UserProfileCard, {
      props: {
        editorOnly: true,
        submitText: '确认信息并支付',
        user: {
          receiverName: '',
          phone: '',
          school: '',
          province: '',
          city: '',
          district: '',
          detailAddress: '',
        },
      },
      global: {
        stubs: {
          'van-popup': PopupStub,
          'van-button': { template: '<button><slot /></button>' },
          'van-form': { template: '<form><slot /></form>' },
          'van-cell-group': { template: '<div><slot /></div>' },
          'van-field': {
            props: ['modelValue', 'label'],
            template: '<div class="field-stub">{{ label }}：{{ modelValue }}</div>',
          },
          'van-cascader': { template: '<div />' },
        },
      },
    })

    expect(wrapper.find('.profile-card').exists()).toBe(false)
    wrapper.vm.openEditor()
    await wrapper.vm.$nextTick()

    expect(wrapper.find('.popup-stub').exists()).toBe(true)
    expect(wrapper.text()).toContain('确认信息并支付')
    expect(wrapper.text()).toContain('省市区：')
  })
})
