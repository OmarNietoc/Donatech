import { apiClient } from './client'
import type { JwtResponse, LoginRequest, RegisterRequest, RegisterBeneficiaryRequest } from '@/types/auth.types'

export const authApi = {
  login: (data: LoginRequest) =>
    apiClient.post<JwtResponse>('/api/auth/login', data).then((r) => r.data),

  register: (data: RegisterRequest) =>
    apiClient.post<{ message: string }>('/api/auth/register', data).then((r) => r.data),

  registerBeneficiary: (data: RegisterBeneficiaryRequest) =>
    apiClient.post<{ message: string }>('/api/auth/register/beneficiary', data).then((r) => r.data),

  registerOrganization: (data: RegisterRequest) =>
    apiClient.post<{ message: string }>('/api/auth/register/organization', data).then((r) => r.data),

  refresh: () =>
    apiClient.post<JwtResponse>('/api/auth/refresh').then((r) => r.data),

  validate: () =>
    apiClient.get('/api/auth/validate'),
}
