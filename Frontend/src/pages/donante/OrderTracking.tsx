import { useParams, Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { orderApi } from '@/api/order.api'
import { useOrderPolling } from '@/hooks/useOrderPolling'
import { TERMINAL_STATES } from '@/types/order.types'
import type { DonationStatus, TrackingHistory } from '@/types/order.types'

const STATUS_LABELS: Record<DonationStatus, string> = {
  DRAFT: 'Borrador',
  INGRESADA: 'Ingresada',
  EN_VALIDACION_TRANSFERENCIA: 'Validando transferencia',
  EN_PREPARACION: 'En preparación',
  ASIGNADA_ENVIO: 'Asignada a envío',
  EN_CAMINO: 'En camino',
  PENDIENTE_CONFIRMACION: 'Pendiente confirmación',
  ENTREGADA: 'Entregada',
  CANCELADA: 'Cancelada',
  RECHAZADA: 'Rechazada',
}

const STATUS_COLOR: Record<DonationStatus, string> = {
  DRAFT: 'bg-gray-100 text-gray-600',
  INGRESADA: 'bg-blue-100 text-blue-700',
  EN_VALIDACION_TRANSFERENCIA: 'bg-yellow-100 text-yellow-700',
  EN_PREPARACION: 'bg-indigo-100 text-indigo-700',
  ASIGNADA_ENVIO: 'bg-purple-100 text-purple-700',
  EN_CAMINO: 'bg-orange-100 text-orange-700',
  PENDIENTE_CONFIRMACION: 'bg-amber-100 text-amber-700',
  ENTREGADA: 'bg-green-100 text-green-700',
  CANCELADA: 'bg-gray-100 text-gray-500',
  RECHAZADA: 'bg-red-100 text-red-700',
}

function formatDate(iso: string) {
  return new Date(iso).toLocaleString('es-CL', {
    day: '2-digit', month: '2-digit', year: 'numeric',
    hour: '2-digit', minute: '2-digit',
  })
}

export default function OrderTracking() {
  const { id } = useParams<{ id: string }>()
  const orderId = Number(id)

  const { data: order, isLoading } = useOrderPolling(orderId)
  const isPolling = order && !TERMINAL_STATES.includes(order.estado)

  const { data: history } = useQuery<TrackingHistory[]>({
    queryKey: ['orders', orderId, 'history'],
    queryFn: () => orderApi.getHistory(orderId),
    enabled: orderId > 0,
    refetchInterval: isPolling ? 5000 : false,
  })

  if (isLoading) return <div className="p-8 text-gray-500">Cargando orden...</div>
  if (!order) return <div className="p-8 text-red-600">Orden no encontrada.</div>

  return (
    <div className="max-w-3xl mx-auto px-8 py-10">
      <div className="flex items-center gap-3 mb-6">
        <h1 className="text-2xl font-bold text-gray-800">Orden #{order.id}</h1>
        <span className={`text-xs font-semibold px-3 py-1 rounded-full ${STATUS_COLOR[order.estado]}`}>
          {STATUS_LABELS[order.estado]}
        </span>
        {isPolling && (
          <span className="text-xs text-gray-400 animate-pulse">• actualizando...</span>
        )}
      </div>

      <div className="bg-white rounded-lg shadow p-6 mb-6">
        <div className="grid grid-cols-2 gap-4 text-sm">
          <div>
            <span className="text-gray-500">Donante</span>
            <p className="font-medium text-gray-800">{order.userEmail}</p>
          </div>
          {order.campaignId && (
            <div>
              <span className="text-gray-500">Campaña</span>
              <p className="font-medium text-gray-800">#{order.campaignId}</p>
            </div>
          )}
          <div>
            <span className="text-gray-500">Total</span>
            <p className="font-medium text-gray-800">${order.total.toLocaleString('es-CL')}</p>
          </div>
        </div>

        {order.estado === 'INGRESADA' && (
          <div className="mt-4 pt-4 border-t border-gray-100">
            <Link
              to={`/orders/${order.id}/proof`}
              className="inline-block bg-primary text-white text-sm font-medium px-4 py-2 rounded hover:bg-primary-dark transition"
            >
              Subir comprobante de transferencia →
            </Link>
          </div>
        )}

        {order.rejectionReason && (
          <div className="mt-4 pt-4 border-t border-gray-100">
            <p className="text-sm text-red-700 bg-red-50 rounded px-3 py-2">
              <strong>Motivo de rechazo:</strong> {order.rejectionReason}
            </p>
          </div>
        )}
      </div>

      <div className="bg-white rounded-lg shadow p-6 mb-6">
        <h2 className="font-semibold text-gray-700 mb-4">Kits donados</h2>
        <div className="space-y-2">
          {order.items.map((item, i) => (
            <div key={i} className="flex justify-between text-sm text-gray-600">
              <span>{item.kitNameSnapshot} × {item.quantity}</span>
              <span>${item.subtotal.toLocaleString('es-CL')}</span>
            </div>
          ))}
        </div>
      </div>

      {history && history.length > 0 && (
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="font-semibold text-gray-700 mb-4">Historial de estados</h2>
          <ol className="relative border-l border-gray-200 space-y-4 ml-3">
            {history.map((h) => (
              <li key={h.id} className="ml-4">
                <span className="absolute -left-1.5 w-3 h-3 rounded-full bg-primary border-2 border-white" />
                <div className="flex items-center gap-2 flex-wrap">
                  <span className={`text-xs font-medium px-2 py-0.5 rounded ${STATUS_COLOR[h.estadoNuevo]}`}>
                    {STATUS_LABELS[h.estadoNuevo]}
                  </span>
                  <span className="text-xs text-gray-400">{formatDate(h.fechaCambio)}</span>
                </div>
                {h.comentario && (
                  <p className="text-sm text-gray-500 mt-1">{h.comentario}</p>
                )}
              </li>
            ))}
          </ol>
        </div>
      )}
    </div>
  )
}
