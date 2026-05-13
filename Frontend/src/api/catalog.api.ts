import { apiClient } from './client'
import type { Campaign, CampaignRequestDto, CampaignKitDto } from '@/types/campaign.types'
import type { Kit, KitDto } from '@/types/campaign.types'

export const campaignApi = {
  getAll: () =>
    apiClient.get<Campaign[]>('/api/campaigns').then((r) => r.data),

  getActive: () =>
    apiClient.get<Campaign[]>('/api/campaigns/active').then((r) => r.data),

  getById: (id: number) =>
    apiClient.get<Campaign>(`/api/campaigns/${id}`).then((r) => r.data),

  getByBeneficiary: (beneficiaryId: number) =>
    apiClient.get<Campaign[]>(`/api/campaigns/by-beneficiary/${beneficiaryId}`).then((r) => r.data),

  create: (data: CampaignRequestDto) =>
    apiClient.post<{ message: string }>('/api/campaigns', data).then((r) => r.data),

  addKit: (id: number, data: CampaignKitDto) =>
    apiClient.post<{ message: string }>(`/api/campaigns/${id}/kits`, data).then((r) => r.data),

  removeKit: (id: number, kitId: number) =>
    apiClient.delete<{ message: string }>(`/api/campaigns/${id}/kits/${kitId}`).then((r) => r.data),

  close: (id: number) =>
    apiClient.patch<{ message: string }>(`/api/campaigns/${id}/close`).then((r) => r.data),
}

export const kitApi = {
  getAll: () =>
    apiClient.get<Kit[]>('/api/kits').then((r) => r.data),

  getById: (id: number) =>
    apiClient.get<Kit>(`/api/kits/${id}`).then((r) => r.data),

  create: (data: KitDto) =>
    apiClient.post<{ message: string }>('/api/kits', data).then((r) => r.data),

  update: (id: number, data: KitDto) =>
    apiClient.put<{ message: string }>(`/api/kits/${id}`, data).then((r) => r.data),

  delete: (id: number) =>
    apiClient.delete<{ message: string }>(`/api/kits/${id}`).then((r) => r.data),
}
