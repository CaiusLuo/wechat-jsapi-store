import http from './http'
import type { H5Config } from './types'

export function getAdminH5Config() {
  return http.get<unknown, H5Config>('/admin/h5-config')
}

export function updateAdminH5Config(payload: H5Config) {
  return http.put<unknown, H5Config>('/admin/h5-config', payload)
}
