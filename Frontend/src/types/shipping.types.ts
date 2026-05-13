export type DeliveryStatus = 'PENDING' | 'DISPATCHED' | 'DELIVERED' | 'FAILED'
export type RouteStatus = 'PLANNED' | 'IN_PROGRESS' | 'COMPLETED'

export interface ShipmentDTO {
  id: string
  orderId: number
  trackingNumber: string
  deliveryStatus: DeliveryStatus
  recipientAddress?: string
  latitude?: number
  longitude?: number
}

export interface RouteDTO {
  id: string
  companyId: string
  carrierId: string
  originAddress: string
  status: RouteStatus
  shipments: ShipmentDTO[]
  optimizedPathJson?: string
  createdAt: string
}

export interface RouteCreationRequestDto {
  companyId: string
  carrierId: string
  originAddress: string
  shipmentIds: string[]
  optimizeRoute: boolean
}
