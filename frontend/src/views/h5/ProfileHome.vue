<script setup lang="ts">
import { nextTick, onMounted, ref } from 'vue'
import { showToast } from 'vant'
import {
  buildDefaultH5UserProfile,
  completeWxOAuthCallback,
  type H5UserProfile,
} from '@/api/h5User'
import { defaultH5Config, getH5Config } from '@/api/h5Config'
import type { H5Config } from '@/api/types'
import { consumePurchaseIntent } from '@/utils/storage'
import { AuthService } from '@/services/authService'
import { isWechatBrowser } from '@/utils/wechatEnv'
import { useRouter } from 'vue-router'
import InlinePurchaseSection from './components/InlinePurchaseSection.vue'
import NoticeCard from './components/NoticeCard.vue'
import OrderEntryRow from './components/OrderEntryRow.vue'
import UserProfileCard from './components/UserProfileCard.vue'

const router = useRouter()
const config = ref<H5Config>({ ...defaultH5Config })
const user = ref<H5UserProfile>(buildDefaultH5UserProfile())
const profileEditorRef = ref<InstanceType<typeof UserProfileCard> | null>(null)
const purchaseSectionRef = ref<InstanceType<typeof InlinePurchaseSection> | null>(null)

onMounted(async () => {
  await loadConfig()
  const oauthHandled = await completeOAuthIfNeeded()
  if (oauthHandled) {
    return
  }

  const loggedIn = await AuthService.ensureLogin({ saveIntent: false, toastWhenNotWechat: false })
  if (!loggedIn && isWechatBrowser()) return

  await refreshProfile(true)
})

async function loadConfig() {
  try {
    config.value = { ...defaultH5Config, ...(await getH5Config()) }
  } catch {
    config.value = { ...defaultH5Config }
  }
}

async function completeOAuthIfNeeded() {
  const url = new URL(window.location.href)
  const code = url.searchParams.get('code')
  if (!code) {
    return false
  }

  try {
    await completeWxOAuthCallback(code)
    url.searchParams.delete('code')
    url.searchParams.delete('state')
    window.history.replaceState({}, document.title, `${url.pathname}${url.search}${url.hash}`)
    await refreshProfile()
    await nextTick()
    restorePurchaseIntent()
    return true
  } catch {
    showToast('微信登录失败，请重新点击订购')
    return true
  }
}

async function refreshProfile(silent = false) {
  try {
    user.value = await AuthService.getCurrentUser({ silent })
  } catch {
    user.value = buildDefaultH5UserProfile()
  }
}

async function handleProfileSaved(profile: H5UserProfile) {
  user.value = profile
  await purchaseSectionRef.value?.continueCheckout(profile)
}

async function handleRequireProfile() {
  await refreshProfile(true)
  await nextTick()
  profileEditorRef.value?.openEditor()
}

function handleProfileCancelled() {
  purchaseSectionRef.value?.cancelPendingCheckout()
}

function restorePurchaseIntent() {
  const intent = consumePurchaseIntent()
  if (!intent) return
  void purchaseSectionRef.value?.focusPurchase()
}
</script>

<template>
  <main class="profile-page h5-page">
    <div class="h5-shell">
      <header class="page-hero">
        <h1>{{ config.siteTitle }}</h1>
      </header>

      <InlinePurchaseSection
        ref="purchaseSectionRef"
        :user="user"
        @require-profile="handleRequireProfile"
      />

      <NoticeCard :config="config" />

      <OrderEntryRow @click="router.push('/h5/orders')" />

      <UserProfileCard
        ref="profileEditorRef"
        editor-only
        submit-text="确认信息并支付"
        :user="user"
        @saved="handleProfileSaved"
        @cancel="handleProfileCancelled"
      />
    </div>
  </main>
</template>

<style scoped>
.profile-page {
  width: 100%;
  min-height: 100vh;
  min-height: 100dvh;
  overflow-x: hidden;
  background: var(--color-bg);
}

.h5-shell {
  display: grid;
  width: 100%;
  max-width: 520px;
  min-width: 0;
  gap: 14px;
  margin: 0 auto;
  padding: calc(20px + env(safe-area-inset-top)) 16px calc(40px + env(safe-area-inset-bottom));
}

.page-hero {
  padding: 8px 2px 2px;
}

.page-hero h1 {
  margin: 0;
  white-space: nowrap;
  word-break: keep-all;
  color: var(--color-text-main);
  font-size: clamp(17px, 5.4vw, 25px);
  line-height: 1.2;
  letter-spacing: -0.04em;
}

@media (max-width: 360px) {
  .h5-shell {
    gap: 12px;
    padding-right: 10px;
    padding-left: 10px;
  }
}
</style>
