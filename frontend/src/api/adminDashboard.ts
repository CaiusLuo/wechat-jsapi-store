import http from './http'
import type { AdminDashboardOverview } from './types'

export function getAdminDashboardOverview() {
  return http.get<unknown, AdminDashboardOverview>('/admin/dashboard/overview')
}
