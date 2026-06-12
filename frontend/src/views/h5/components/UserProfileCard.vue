<script setup lang="ts">
import { computed, nextTick, reactive, ref, watch } from 'vue'
import { showFailToast, showSuccessToast, showToast } from 'vant'
import type { CascaderOption } from 'vant'
import { isUserProfileComplete, updateUserProfile, type H5UserProfile } from '@/api/h5User'
import { areaOptions, findAreaCode, findProvinceCode } from '@/utils/area'
import { formatAddress } from '@/utils/format'

const props = defineProps<{
  user: H5UserProfile
  compact?: boolean
  editorOnly?: boolean
  submitText?: string
}>()

const emit = defineEmits<{
  (event: 'saved', user: H5UserProfile): void
  (event: 'cancel'): void
}>()

const editing = ref(false)
const saving = ref(false)
const areaPopup = ref(false)
const areaValue = ref('')
const form = reactive<H5UserProfile>({ ...props.user })
const avatarVisible = ref(true)
const closingAfterSave = ref(false)

watch(
  () => props.user,
  (value) => {
    Object.assign(form, value)
  },
  { deep: true },
)

const displayName = computed(() => props.user.nickname?.trim() || '微信用户')
const avatarUrl = computed(() => props.user.avatar?.trim() || '')
const displayPhone = computed(() => props.user.phone.trim() || '请输入有效的手机号')
const displaySchool = computed(() => props.user.school.trim() || '点击填写学校全称')
const displayAddress = computed(
  () =>
    formatAddress([props.user.province, props.user.city, props.user.district, props.user.detailAddress]) ||
    '点击完善收货地址',
)
const formAreaText = computed(() => formatAddress([form.province, form.city, form.district]) || '')
const profileComplete = computed(() => isUserProfileComplete(props.user))
const compactSummary = computed(() => {
  if (!profileComplete.value) return '请补充收货人真实姓名、手机号、学校全称和完整地址'
  return [
    `${props.user.receiverName.trim()} · ${props.user.phone.trim()}`,
    props.user.school.trim(),
    formatAddress([props.user.province, props.user.city, props.user.district, props.user.detailAddress]),
  ]
    .filter(Boolean)
    .join('\n')
})

watch(
  () => props.user.avatar,
  () => {
    avatarVisible.value = true
  },
)

function openEditor() {
  Object.assign(form, props.user)
  areaValue.value = findAreaCode(form.province, form.city, form.district) || findProvinceCode(form.province)
  closingAfterSave.value = false
  editing.value = true
}

function handleAvatarError() {
  avatarVisible.value = false
}

function validateProfile() {
  if (!form.receiverName.trim()) return '请填写收货人真实姓名'
  if (!/^1[3-9]\d{9}$/.test(form.phone.trim())) return '请输入有效的手机号'
  if (!form.school.trim()) return '请填写学校全称'
  if (!form.province.trim() || !form.city.trim() || !form.district.trim()) return '请选择省市区'
  if (!form.detailAddress.trim()) return '请填写详细地址'
  return ''
}

function handleAreaFinish({ selectedOptions }: { selectedOptions: CascaderOption[] }) {
  const [province, city, district] = selectedOptions
  form.province = String(province?.text || '')
  form.city = String(city?.text || '')
  form.district = String(district?.text || '')
  areaValue.value = String(district?.value || '')
  areaPopup.value = false
}

async function saveProfile() {
  if (saving.value) return
  const message = validateProfile()
  if (message) {
    showToast(message)
    return
  }

  saving.value = true
  try {
    const savedProfile = await updateUserProfile({
      receiverName: form.receiverName.trim(),
      phone: form.phone.trim(),
      school: form.school.trim(),
      province: form.province.trim(),
      city: form.city.trim(),
      district: form.district.trim(),
      detailAddress: form.detailAddress.trim(),
    })
    emit('saved', savedProfile)
    closingAfterSave.value = true
    editing.value = false
    await nextTick()
    closingAfterSave.value = false
    showSuccessToast('用户信息已保存')
  } catch (error) {
    showFailToast(error instanceof Error ? error.message : '保存失败，请稍后重试')
  } finally {
    saving.value = false
  }
}

function cancelEditor() {
  editing.value = false
}

watch(editing, (isOpen, wasOpen) => {
  if (!isOpen && wasOpen && !closingAfterSave.value) {
    emit('cancel')
  }
})

defineExpose({ openEditor })
</script>

<template>
  <section v-if="!editorOnly" :class="['profile-card', 'h5-card', { compact }]">
    <button v-if="compact" class="compact-main" type="button" @click="openEditor">
      <div class="compact-head">
        <div>
          <h2>收货信息</h2>
          <span :class="['profile-status', { complete: profileComplete }]">
            {{ profileComplete ? '已完善' : '未完善' }}
          </span>
        </div>
        <strong>{{ profileComplete ? '修改' : '填写' }}</strong>
      </div>
      <p>{{ compactSummary }}</p>
    </button>

    <button v-else class="profile-main" type="button" @click="openEditor">
      <div class="avatar">
        <img v-if="avatarUrl && avatarVisible" :src="avatarUrl" :alt="displayName" @error="handleAvatarError" />
        <span v-else>购</span>
      </div>
      <div class="profile-text">
        <div class="name-row">
          <h2>{{ displayName }}</h2>
          <span>编辑</span>
        </div>
        <p>{{ displayPhone }}</p>
        <p>{{ displaySchool }}</p>
        <p>{{ displayAddress }}</p>
      </div>
    </button>
  </section>

  <van-popup v-model:show="editing" round position="bottom" class="profile-popup" safe-area-inset-bottom>
    <div class="popup-inner">
      <div class="popup-head">
        <div>
          <p>收货资料</p>
          <h3>完善用户信息</h3>
        </div>
        <van-button size="small" plain round :disabled="saving" @click="cancelEditor">取消</van-button>
      </div>

      <van-form @submit="saveProfile">
        <van-cell-group inset>
          <van-field v-model="form.receiverName" label="收货人真实姓名" placeholder="请输入真实收货人姓名" required />
          <van-field v-model="form.phone" label="手机号" type="tel" placeholder="请输入有效的手机号" required />
          <van-field v-model="form.school" label="学校全称" placeholder="请输入学校完整名称" required />
          <van-field
            :model-value="formAreaText"
            label="省市区"
            placeholder="请选择省市区"
            is-link
            readonly
            required
            @click="areaPopup = true"
          />
          <van-field
            v-model="form.detailAddress"
            label="详细地址"
            type="textarea"
            rows="2"
            autosize
            placeholder="具体到街道、门牌号"
            required
          />
        </van-cell-group>

        <div class="popup-actions">
          <van-button block round type="primary" native-type="submit" :loading="saving" :disabled="saving">
            {{ submitText || '保存信息' }}
          </van-button>
        </div>
      </van-form>
    </div>

    <van-popup v-model:show="areaPopup" round position="bottom">
      <van-cascader
        v-model="areaValue"
        title="选择省市区"
        :options="areaOptions"
        @close="areaPopup = false"
        @finish="handleAreaFinish"
      />
    </van-popup>
  </van-popup>
</template>

<style scoped>
.profile-card {
  width: 100%;
  max-width: 100%;
  min-width: 0;
  overflow: hidden;
  border: 1px solid var(--color-border);
  border-radius: 16px;
  background: var(--color-card);
  box-shadow: var(--shadow-card);
}

.profile-card.compact {
  box-shadow: none;
}

.compact-main {
  display: grid;
  width: 100%;
  min-width: 0;
  gap: 8px;
  padding: 14px;
  border: 0;
  background: transparent;
  text-align: left;
}

.compact-head {
  display: flex;
  min-width: 0;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.compact-head > div {
  display: flex;
  min-width: 0;
  align-items: center;
  gap: 8px;
}

.compact-head h2 {
  margin: 0;
  color: var(--color-text-main);
  font-size: 16px;
  line-height: 1.35;
}

.profile-status {
  display: inline-flex;
  height: 22px;
  align-items: center;
  padding: 0 8px;
  border-radius: 11px;
  color: var(--color-accent);
  background: rgb(217 72 15 / 10%);
  font-size: 12px;
  font-weight: 700;
  white-space: nowrap;
}

.profile-status.complete {
  color: var(--color-primary);
  background: var(--color-primary-soft);
}

.compact-head strong {
  flex: 0 0 auto;
  color: var(--color-primary);
  font-size: 13px;
}

.compact-main p {
  margin: 0;
  white-space: pre-line;
  color: var(--color-text-secondary);
  font-size: 13px;
  line-height: 1.55;
  overflow-wrap: anywhere;
}

.profile-main {
  display: grid;
  width: 100%;
  min-width: 0;
  grid-template-columns: 64px minmax(0, 1fr);
  gap: 14px;
  align-items: center;
  padding: 18px;
  border: 0;
  background: transparent;
  text-align: left;
}

.avatar {
  display: grid;
  width: 64px;
  height: 64px;
  place-items: center;
  overflow: hidden;
  border-radius: 16px;
  color: #fff;
  background: var(--color-primary);
  font-size: 24px;
  font-weight: 700;
  box-shadow: 0 8px 18px rgb(31 58 95 / 12%);
}

.avatar img {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.profile-text {
  min-width: 0;
}

.name-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  min-width: 0;
}

.name-row h2 {
  margin: 0;
  overflow: hidden;
  color: var(--color-text-main);
  font-size: 20px;
  line-height: 1.25;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.name-row span {
  flex: 0 0 auto;
  color: var(--color-primary);
  font-size: 13px;
  font-weight: 700;
}

.profile-text p {
  margin: 7px 0 0;
  color: var(--color-text-secondary);
  font-size: 13px;
  line-height: 1.45;
  overflow-wrap: anywhere;
}

.profile-popup {
  width: 100%;
  max-height: 86vh;
  max-height: 86dvh;
  overflow-y: auto;
  background: transparent;
}

.popup-inner {
  width: 100%;
  max-width: 520px;
  margin: 0 auto;
  overflow-y: auto;
  border-radius: 24px 24px 0 0;
  background: var(--color-bg);
}

.popup-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 18px 18px 10px;
}

.popup-head p {
  margin: 0 0 3px;
  color: var(--color-text-secondary);
  font-size: 12px;
}

.popup-head h3 {
  margin: 0;
  color: var(--color-text-main);
  font-size: 20px;
}

.popup-actions {
  padding: 18px 16px 24px;
}

@media (max-width: 360px) {
  .profile-main {
    grid-template-columns: 52px minmax(0, 1fr);
    gap: 12px;
    padding: 14px;
  }

  .avatar {
    width: 52px;
    height: 52px;
    border-radius: 14px;
    font-size: 20px;
  }

  .name-row h2 {
    font-size: 18px;
  }
}
</style>
