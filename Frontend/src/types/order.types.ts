export type DonationStatus =
  | 'DRAFT'
  | 'INGRESADA'
  | 'EN_VALIDACION_TRANSFERENCIA'
  | 'EN_PREPARACION'
  | 'ASIGNADA_ENVIO'
  | 'EN_CAMINO'
  | 'PENDIENTE_CONFIRMACION'
  | 'ENTREGADA'
  | 'CANCELADA'
  | 'RECHAZADA'

export const TERMINAL_STATES: DonationStatus[] = ['ENTREGADA', 'CANCELADA', 'RECHAZADA']

export interface OrderItem {
  kitId: number
  kitNameSnapshot: string
  quantity: number
  unitPrice: number
  subtotal: number
}

export interface TrackingHistory {
  id: number
  orderId: number
  estadoAnterior: DonationStatus | null
  estadoNuevo: DonationStatus
  changedById: number | null
  fechaCambio: string
  comentario: string | null
}

export interface OrderResponse {
  id: number
  userEmail: string
  campaignId?: number
  estado: DonationStatus
  total: number
  items: OrderItem[]
  createdAt: string
  rejectionReason?: string
}

export interface OrderItemRequestDto {
  kitId: number
  quantity: number
}

export interface OrderDto {
  userEmail: string
  campaignId?: number
  couponCode?: string
  items: OrderItemRequestDto[]
}
