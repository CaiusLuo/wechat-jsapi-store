import { showToast } from 'vant'
import { getValidH5Token } from '@/api/http'
import { getH5OAuthMe, getUserProfile, getWxOAuthUrl, type H5UserProfile } from '@/api/h5User'
import { isWechatBrowser } from '@/utils/wechatEnv'
import { savePurchaseIntent } from '@/utils/storage'

interface EnsureWechatLoginOptions {
  saveIntent?: boolean
  toastWhenNotWechat?: boolean
}

async function ensureWechatLogin(options: EnsureWechatLoginOptions = {}) {
  const { saveIntent = true, toastWhenNotWechat = true } = options

  try {
    await getH5OAuthMe({ silent: true })
    return true
  } catch {
    if (!isWechatBrowser()) {
      if (toastWhenNotWechat) {
        showToast('请在微信内打开后完成登录与支付')
      }
      return false
    }

    try {
      if (saveIntent) {
        savePurchaseIntent()
      }
      const oauthUrl = await getWxOAuthUrl({
        redirectUri: window.location.href,
        state: 'h5',
        scope: 'snsapi_userinfo',
      })
      if (oauthUrl) {
        window.location.replace(oauthUrl)
      }
    } catch {
      showToast('微信登录初始化失败')
    }
    return false
  }
}

export const AuthService = {
  async getCurrentUser(options: { silent?: boolean } = {}): Promise<H5UserProfile> {
    return getUserProfile(options)
  },

  async ensureLogin(options?: EnsureWechatLoginOptions) {
    return ensureWechatLogin(options)
  },

  hasToken() {
    return Boolean(getValidH5Token())
  },
}
