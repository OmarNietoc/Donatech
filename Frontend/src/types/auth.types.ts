export interface LoginRequest {
  email: string
  password: string
}

export interface RegisterRequest {
  name: string
  email: string
  password: string
  roleId?: number
  phone?: string
  regionId?: number
  comunaId?: number
}

export interface RegisterBeneficiaryRequest {
  name: string
  email: string
  password: string
  phone?: string
  regionId?: number
  comunaId?: number
  rut: string
  direccionEntrega: string
  observaciones?: string
}

export interface JwtResponse {
  token: string
  id: number
  email: string
  roles: string[]
}
