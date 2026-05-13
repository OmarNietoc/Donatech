export type EstadoSoporte = 'ABIERTO' | 'EN_PROCESO' | 'RESUELTO' | 'CERRADO'

export type TipoSoporte =
  | 'CONSULTA'
  | 'RECLAMO'
  | 'VALIDACION_CAMPAÑA'
  | 'VALIDACION_TRANSFERENCIA'
  | 'STOCK_BAJO'

export interface Soporte {
  id: number
  tipo: TipoSoporte
  titulo?: string
  descripcion: string
  estado: EstadoSoporte
  usuarioId: number
  voluntarioId?: number
  donationId?: number
  campaignId?: number
  recipientEmail?: string
  fechaCreacion: string
  fechaCierre?: string
}

export interface SoporteRequestDto {
  tipo: TipoSoporte
  titulo?: string
  descripcion: string
  usuarioId: number
  donationId?: number
  campaignId?: number
  recipientEmail?: string
}
