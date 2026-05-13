import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { supportsApi } from '@/api/supports.api'
import type { EstadoSoporte, TipoSoporte } from '@/types/supports.types'
import { useState } from 'react'

const ESTADO_COLOR: Record<EstadoSoporte, string> = {
  ABIERTO: 'bg-blue-100 text-blue-700',
  EN_PROCESO: 'bg-yellow-100 text-yellow-700',
  RESUELTO: 'bg-green-100 text-green-700',
  CERRADO: 'bg-gray-100 text-gray-500',
}

const TIPO_LABEL: Record<TipoSoporte, string> = {
  CONSULTA: 'Consulta',
  RECLAMO: 'Reclamo',
  VALIDACION_CAMPAÑA: 'Validar campaña',
  VALIDACION_TRANSFERENCIA: 'Validar transferencia',
  STOCK_BAJO: 'Stock bajo',
}

export default function TicketList() {
  const [filter, setFilter] = useState<EstadoSoporte | ''>('')

  const { data: tickets, isLoading } = useQuery({
    queryKey: ['supports', filter],
    queryFn: () =>
      filter ? supportsApi.getByEstado(filter) : supportsApi.getAll(),
    refetchInterval: 15000,
  })

  return (
    <div className="max-w-5xl mx-auto px-8 py-10">
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-gray-800">Tickets de soporte</h1>
        <select
          value={filter}
          onChange={(e) => setFilter(e.target.value as EstadoSoporte | '')}
          className="border border-gray-300 rounded-md px-3 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary"
        >
          <option value="">Todos los estados</option>
          <option value="ABIERTO">Abierto</option>
          <option value="EN_PROCESO">En proceso</option>
          <option value="RESUELTO">Resuelto</option>
          <option value="CERRADO">Cerrado</option>
        </select>
      </div>

      {isLoading && <p className="text-gray-500">Cargando tickets...</p>}

      <div className="space-y-3">
        {tickets?.map((t) => (
          <div
            key={t.id}
            className="bg-white rounded-lg shadow p-5 flex items-center justify-between gap-4"
          >
            <div className="flex-1 min-w-0">
              <div className="flex items-center gap-3 mb-1 flex-wrap">
                <span className="font-medium text-gray-800">#{t.id}</span>
                <span className="text-xs font-medium bg-indigo-100 text-indigo-700 px-2 py-0.5 rounded">
                  {TIPO_LABEL[t.tipo]}
                </span>
                <span className={`text-xs font-medium px-2 py-0.5 rounded ${ESTADO_COLOR[t.estado]}`}>
                  {t.estado}
                </span>
              </div>
              <p className="text-sm text-gray-600 truncate">{t.descripcion}</p>
              {t.campaignId && (
                <p className="text-xs text-gray-400 mt-1">Campaña #{t.campaignId}</p>
              )}
              {t.donationId && (
                <p className="text-xs text-gray-400 mt-1">Donación #{t.donationId}</p>
              )}
            </div>

            {(t.tipo === 'VALIDACION_CAMPAÑA' || t.tipo === 'VALIDACION_TRANSFERENCIA') &&
              t.estado === 'ABIERTO' && (
                <Link
                  to={`/admin/tickets/${t.id}/validate`}
                  className="shrink-0 bg-primary text-white text-sm font-medium px-4 py-2 rounded hover:bg-primary-dark transition"
                >
                  Validar
                </Link>
              )}
          </div>
        ))}
      </div>

      {!isLoading && tickets?.length === 0 && (
        <p className="text-gray-500 text-center py-8">No hay tickets en este estado.</p>
      )}
    </div>
  )
}
