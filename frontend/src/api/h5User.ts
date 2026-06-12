import http, { getValidH5Token, saveH5Session } from './http'
import type { H5UserProfile, H5UserProfilePayload } from './types'

export type { H5UserProfile, H5UserProfilePayload }

export interface H5OAuthMe {
  token: string | null
  expiresAt: number
}

export interface WxOAuthUrlParams {
  redirectUri?: string
  state?: string
  scope?: 'snsapi_base' | 'snsapi_userinfo'
}

export function getH5OAuthMe(options: { silent?: boolean } = {}) {
  return http.get<unknown, H5OAuthMe>('/wx/oauth/me', { silent: options.silent }).then((result) => {
    saveH5Session(result.token, result.expiresAt)
    return result
  })
}

export function saveH5Token(token: string, expiresAt?: number | null) {
  saveH5Session(token, expiresAt)
}

export function getH5Token() {
  return getValidH5Token()
}

export function getWxOAuthUrl(params?: WxOAuthUrlParams) {
  return http.get<unknown, string>('/wx/oauth/url', { params })
}

export function completeWxOAuthCallback(code: string) {
  return http.get<unknown, H5OAuthMe>('/wx/oauth/callback', { params: { code } }).then((result) => {
    saveH5Session(result.token, result.expiresAt)
    return result
  })
}

export function getUserProfile(options: { silent?: boolean } = {}) {
  return http
    .get<unknown, Partial<H5UserProfile> | null>('/h5/user/profile', { silent: options.silent })
    .then(normalizeH5UserProfile)
}

export function updateUserProfile(payload: H5UserProfilePayload) {
  return http.put<unknown, Partial<H5UserProfile> | null>('/h5/user/profile', payload).then(normalizeH5UserProfile)
}

export function buildDefaultH5UserProfile(): H5UserProfile {
  return {
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
  }
}

export function normalizeH5UserProfile(profile?: Partial<H5UserProfile> | null): H5UserProfile {
  const fallback = buildDefaultH5UserProfile()
  return {
    openid: String(profile?.openid || fallback.openid || ''),
    nickname: String(profile?.nickname || fallback.nickname || ''),
    avatar: String(profile?.avatar || fallback.avatar || ''),
    receiverName: String(profile?.receiverName || ''),
    phone: String(profile?.phone || ''),
    school: String(profile?.school || ''),
    province: String(profile?.province || ''),
    city: String(profile?.city || ''),
    district: String(profile?.district || ''),
    detailAddress: String(profile?.detailAddress || ''),
    profileCompleted: Boolean(profile?.profileCompleted),
  }
}

export function isUserProfileComplete(profile: H5UserProfile) {
  return Boolean(
    profile.receiverName.trim() &&
      /^1[3-9]\d{9}$/.test(profile.phone.trim()) &&
      profile.school.trim() &&
      profile.province.trim() &&
      profile.city.trim() &&
      profile.district.trim() &&
      profile.detailAddress.trim(),
  )
}
