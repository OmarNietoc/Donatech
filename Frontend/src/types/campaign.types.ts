export type CampaignStatus = 'EN_VALIDACION' | 'ACTIVA' | 'INACTIVA' | 'FINALIZADA'

export interface CampaignKit {
  kitId: number
  cantidadNecesaria: number
  cantidadFulfilled: number
}

export interface Campaign {
  id: number
  titulo: string
  descripcion: string
  beneficiarioId: number
  estado: CampaignStatus
  regionId?: number
  comunaId?: number
  observaciones?: string
  motivoRechazo?: string
  fechaCreacion: string
  fechaActivacion?: string
  kits: CampaignKit[]
}

export interface CampaignRequestDto {
  titulo: string
  descripcion: string
  beneficiarioId: number
  regionId?: number
  comunaId?: number
  observaciones?: string
}

export interface CampaignKitDto {
  kitId: number
  cantidadNecesaria: number
}

export interface Kit {
  id: number
  nombre: string
  descripcion?: string
  precioEstimado: number
  activo: boolean
}

export interface KitDto {
  nombre: string
  descripcion?: string
  precioEstimado: number
  activo: boolean
}
