import { apiClient } from './client'
import type { Soporte, SoporteRequestDto, EstadoSoporte, TipoSoporte } from '@/types/supports.types'

export const supportsApi = {
  getAll: () =>
    apiClient.get<Soporte[]>('/api/supports').then((r) => r.data),

  getById: (id: number) =>
    apiClient.get<Soporte>(`/api/supports/${id}`).then((r) => r.data),

  create: (data: SoporteRequestDto) =>
    apiClient.post<Soporte>('/api/supports', data).then((r) => r.data),

  getByEstado: (estado: EstadoSoporte) =>
    apiClient.get<Soporte[]>('/api/supports/by-estado', { params: { estado } }).then((r) => r.data),

  getByTipo: (tipo: TipoSoporte) =>
    apiClient.get<Soporte[]>('/api/supports/by-tipo', { params: { tipo } }).then((r) => r.data),

  getByDonation: (donationId: number) =>
    apiClient.get<Soporte[]>(`/api/supports/by-donation/${donationId}`).then((r) => r.data),

  assign: (id: number, voluntarioId: number) =>
    apiClient
      .patch<Soporte>(`/api/supports/${id}/assign`, null, { params: { voluntarioId } })
      .then((r) => r.data),

  updateStatus: (id: number, estado: EstadoSoporte) =>
    apiClient.patch<Soporte>(`/api/supports/${id}/status`, { estado }).then((r) => r.data),

  respond: (id: number, respuesta: string, voluntarioId: number) =>
    apiClient
      .patch<Soporte>(`/api/supports/${id}/respond`, { respuesta, voluntarioId })
      .then((r) => r.data),

  validateCampaign: (id: number, approved: boolean, motivo?: string) =>
    apiClient
      .patch<{ message: string }>(`/api/supports/${id}/validate-campaign`, null, {
        params: { approved, motivo: motivo ?? '' },
      })
      .then((r) => r.data),

  validateTransfer: (id: number, approved: boolean, motivo?: string) =>
    apiClient
      .patch<{ message: string }>(`/api/supports/${id}/validate-transfer`, null, {
        params: { approved, motivo: motivo ?? '' },
      })
      .then((r) => r.data),

  delete: (id: number) =>
    apiClient.delete<{ message: string }>(`/api/supports/${id}`).then((r) => r.data),
}
