import { apiClient } from './client'

export interface User {
  id: number
  name: string
  email: string
  phone?: string
  status: number
  roles: string[]
}

export const usersApi = {
  getAll: () =>
    apiClient.get<User[]>('/api/users').then((r) => r.data),

  getById: (id: number) =>
    apiClient.get<User>(`/api/users/${id}`).then((r) => r.data),

  getByEmail: (email: string) =>
    apiClient.get<User>('/api/users/by-email', { params: { email } }).then((r) => r.data),

  updateStatus: (id: number, status: 0 | 1) =>
    apiClient.patch(`/api/users/${id}/status`, null, { params: { status } }).then((r) => r.data),
}
