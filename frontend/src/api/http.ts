import axios, { AxiosError, type InternalAxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import { showToast } from 'vant'
import type { ApiResult } from './types'

declare module 'axios' {
  export interface AxiosRequestConfig {
    silent?: boolean
  }
}

const ADMIN_TOKEN_KEY = 'admin_token'
const ADMIN_TOKEN_EXPIRE_AT_KEY = 'admin_token_expire_at'
const ADMIN_USER_KEY = 'admin_user'
const H5_TOKEN_KEY = 'h5_token'
const H5_USER_EXPIRE_AT_KEY = 'h5_user_expire_at'
const ADMIN_TOKEN_HEADER = 'X-Admin-Token'

function normalizeExpireAt(expiresAt?: number | string | null) {
  const value = Number(expiresAt || 0)
  if (!value) return 0
  return value < 10_000_000_000 ? value * 1000 : value
}

function isExpired(expiresAt?: string | null) {
  const value = Number(expiresAt || 0)
  return Boolean(value && Date.now() >= value)
}

function isAdminPage() {
  return typeof window !== 'undefined' && window.location.pathname.startsWith('/admin')
}

function notifyError(message: string) {
  if (isAdminPage()) {
    ElMessage.error(message)
    return
  }
  showToast(message)
}

function redirectAdminLogin() {
  clearAdminSession()
  if (isAdminPage()) {
    window.location.replace('/admin/login')
  }
}

function clearH5Session() {
  localStorage.removeItem(H5_TOKEN_KEY)
  localStorage.removeItem(H5_USER_EXPIRE_AT_KEY)
}

function clearAdminSession() {
  localStorage.removeItem(ADMIN_TOKEN_KEY)
  localStorage.removeItem(ADMIN_TOKEN_EXPIRE_AT_KEY)
  localStorage.removeItem(ADMIN_USER_KEY)
}

function getValidAdminToken() {
  if (isExpired(localStorage.getItem(ADMIN_TOKEN_EXPIRE_AT_KEY))) {
    clearAdminSession()
    return null
  }
  return localStorage.getItem(ADMIN_TOKEN_KEY)
}

function getValidH5Token() {
  if (isExpired(localStorage.getItem(H5_USER_EXPIRE_AT_KEY))) {
    clearH5Session()
    return null
  }
  return localStorage.getItem(H5_TOKEN_KEY)
}

function saveAdminSession(token: string, expiresAt?: number | null, username?: string) {
  localStorage.setItem(ADMIN_TOKEN_KEY, token)
  if (expiresAt) {
    localStorage.setItem(ADMIN_TOKEN_EXPIRE_AT_KEY, String(normalizeExpireAt(expiresAt)))
  }
  if (username) {
    localStorage.setItem(ADMIN_USER_KEY, username)
  }
}

function saveH5Session(token: string | null | undefined, expiresAt?: number | null) {
  if (token) {
    localStorage.setItem(H5_TOKEN_KEY, token)
  }
  if (expiresAt) {
    localStorage.setItem(H5_USER_EXPIRE_AT_KEY, String(normalizeExpireAt(expiresAt)))
  }
}

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 15000,
  withCredentials: true,
})

http.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const url = config.url || ''
  const adminToken = getValidAdminToken()
  const h5Token = getValidH5Token()

  if (url.startsWith('/admin') && adminToken) {
    config.headers.set(ADMIN_TOKEN_HEADER, adminToken)
  } else if (h5Token) {
    config.headers.set('Authorization', `Bearer ${h5Token}`)
  }

  return config
})

http.interceptors.response.use(
  (response) => {
    const body = response.data as ApiResult<unknown> | unknown
    if (body && typeof body === 'object' && 'code' in body && 'message' in body) {
      const result = body as ApiResult<unknown>
      if (Number(result.code) !== 0) {
        const message = result.message || '请求失败'
        if (Number(result.code) === 401) {
          if (isAdminPage()) {
            redirectAdminLogin()
          } else {
            clearH5Session()
          }
        }
        if (!response.config.silent) {
          notifyError(message)
        }
        return Promise.reject(new Error(message))
      }
      return result.data
    }

    return response.data
  },
  (error: AxiosError<ApiResult<unknown>>) => {
    const status = error.response?.status
    const message = error.response?.data?.message || error.message || '网络请求失败'

    if (status === 401) {
      if (isAdminPage()) {
        redirectAdminLogin()
      } else {
        clearH5Session()
      }
    }

    if (!error.config?.silent) {
      notifyError(message)
    }
    return Promise.reject(error)
  },
)

export {
  ADMIN_TOKEN_KEY,
  ADMIN_TOKEN_EXPIRE_AT_KEY,
  ADMIN_USER_KEY,
  H5_TOKEN_KEY,
  H5_USER_EXPIRE_AT_KEY,
  ADMIN_TOKEN_HEADER,
  clearAdminSession,
  clearH5Session,
  getValidAdminToken,
  getValidH5Token,
  saveAdminSession,
  saveH5Session,
}
export default http
