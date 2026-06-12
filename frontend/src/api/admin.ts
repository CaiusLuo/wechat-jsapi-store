import http, { clearAdminSession, getValidAdminToken, saveAdminSession } from './http'
import type { AdminLoginPayload, AdminLoginResult } from './types'

export function adminLogin(payload: AdminLoginPayload) {
  return http.post<unknown, AdminLoginResult>('/admin/login', payload)
}

export function saveAdminToken(token: string) {
  saveAdminSession(token)
}

export function getAdminToken() {
  return getValidAdminToken()
}

export function clearAdminToken() {
  clearAdminSession()
}

export function saveAdminLoginResult(result: AdminLoginResult) {
  saveAdminSession(result.token, result.expiresAt, result.username)
}
