import { apiClient } from './client'
import type { OrderResponse, OrderDto, DonationStatus, TrackingHistory } from '@/types/order.types'

export const orderApi = {
  getAll: () =>
    apiClient.get<OrderResponse[]>('/api/orders').then((r) => r.data),

  getById: (id: number) =>
    apiClient.get<OrderResponse>(`/api/orders/${id}`).then((r) => r.data),

  create: (data: OrderDto) =>
    apiClient.post<OrderResponse>('/api/orders', data).then((r) => r.data),

  updateStatus: (id: number, status: DonationStatus, changedById?: number) =>
    apiClient
      .patch<{ message: string }>(`/api/orders/${id}/status`, null, {
        params: { status, changedById },
      })
      .then((r) => r.data),

  getHistory: (id: number) =>
    apiClient.get<TrackingHistory[]>(`/api/orders/${id}/history`).then((r) => r.data),

  uploadTransferProof: (id: number, file: File) => {
    const formData = new FormData()
    formData.append('file', file)
    return apiClient
      .post<{ message: string }>(`/api/orders/${id}/transfer-proof`, formData)
      .then((r) => r.data)
  },

  uploadDeliveryProof: (id: number, photo?: File, document?: File) => {
    const formData = new FormData()
    if (photo) formData.append('photo', photo)
    if (document) formData.append('document', document)
    return apiClient
      .post<{ message: string }>(`/api/orders/${id}/delivery-proof`, formData)
      .then((r) => r.data)
  },

  confirmDelivery: (id: number, confirmedById: number) =>
    apiClient
      .patch<{ message: string }>(`/api/orders/${id}/confirm-delivery`, null, {
        params: { confirmedById },
      })
      .then((r) => r.data),

  getDashboard: () =>
    apiClient.get('/api/orders/dashboard/summary').then((r) => r.data),

  getByDonor: (email: string) =>
    apiClient.get<OrderResponse[]>('/api/donations/by-donor', { params: { email } }).then((r) => r.data),

  delete: (id: number) =>
    apiClient.delete<{ message: string }>(`/api/orders/${id}`).then((r) => r.data),
}
