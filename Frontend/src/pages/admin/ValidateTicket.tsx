import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useParams, useNavigate } from 'react-router-dom'
import { useState } from 'react'
import { supportsApi } from '@/api/supports.api'

export default function ValidateTicket() {
  const { id } = useParams<{ id: string }>()
  const ticketId = Number(id)
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const [motivo, setMotivo] = useState('')
  const [error, setError] = useState<string | null>(null)

  const { data: ticket, isLoading } = useQuery({
    queryKey: ['supports', ticketId],
    queryFn: () => supportsApi.getById(ticketId),
  })

  const isCampaign = ticket?.tipo === 'VALIDACION_CAMPAÑA'
  const isTransfer = ticket?.tipo === 'VALIDACION_TRANSFERENCIA'

  const validate = useMutation({
    mutationFn: ({ approved }: { approved: boolean }) => {
      if (isCampaign) {
        return supportsApi.validateCampaign(ticketId, approved, motivo)
      }
      return supportsApi.validateTransfer(ticketId, approved, motivo)
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['supports'] })
      navigate('/admin/tickets')
    },
    onError: () => {
      setError('Error al procesar la validación. Intenta de nuevo.')
    },
  })

  if (isLoading) return <div className="p-8 text-gray-500">Cargando ticket...</div>
  if (!ticket) return <div className="p-8 text-red-600">Ticket no encontrado.</div>
  if (!isCampaign && !isTransfer) {
    return <div className="p-8 text-gray-500">Este ticket no requiere validación.</div>
  }

  return (
    <div className="max-w-2xl mx-auto px-8 py-10">
      <h1 className="text-2xl font-bold text-gray-800 mb-2">
        {isCampaign ? 'Validar campaña' : 'Validar transferencia'}
      </h1>
      <p className="text-gray-500 text-sm mb-8">Ticket #{ticket.id}</p>

      <div className="bg-white rounded-lg shadow p-8 space-y-6">
        <div className="grid grid-cols-2 gap-4 text-sm">
          <div>
            <span className="text-gray-500">Tipo</span>
            <p className="font-medium text-gray-800">{ticket.tipo.replace('_', ' ')}</p>
          </div>
          {ticket.campaignId && (
            <div>
              <span className="text-gray-500">Campaña</span>
              <p className="font-medium text-gray-800">#{ticket.campaignId}</p>
            </div>
          )}
          {ticket.donationId && (
            <div>
              <span className="text-gray-500">Donación</span>
              <p className="font-medium text-gray-800">#{ticket.donationId}</p>
            </div>
          )}
          {ticket.recipientEmail && (
            <div>
              <span className="text-gray-500">Destinatario email</span>
              <p className="font-medium text-gray-800">{ticket.recipientEmail}</p>
            </div>
          )}
        </div>

        <div>
          <p className="text-sm text-gray-500 mb-1">Descripción del ticket</p>
          <p className="text-gray-700 bg-gray-50 rounded px-3 py-2 text-sm">{ticket.descripcion}</p>
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Motivo / observación{' '}
            <span className="text-gray-400 font-normal">(requerido si rechazas)</span>
          </label>
          <textarea
            value={motivo}
            onChange={(e) => setMotivo(e.target.value)}
            rows={3}
            placeholder="Describe el motivo de la decisión..."
            className="w-full border border-gray-300 rounded-md px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary"
          />
        </div>

        {error && (
          <p className="text-red-600 text-sm bg-red-50 px-3 py-2 rounded">{error}</p>
        )}

        <div className="flex gap-3">
          <button
            onClick={() => validate.mutate({ approved: true })}
            disabled={validate.isPending}
            className="flex-1 bg-green-600 text-white py-2.5 rounded-md font-medium hover:bg-green-700 transition disabled:opacity-50"
          >
            {validate.isPending ? 'Procesando...' : '✓ Aprobar'}
          </button>
          <button
            onClick={() => {
              if (!motivo.trim()) {
                setError('Debes ingresar un motivo para rechazar.')
                return
              }
              setError(null)
              validate.mutate({ approved: false })
            }}
            disabled={validate.isPending}
            className="flex-1 bg-red-600 text-white py-2.5 rounded-md font-medium hover:bg-red-700 transition disabled:opacity-50"
          >
            {validate.isPending ? 'Procesando...' : '✕ Rechazar'}
          </button>
        </div>

        <button
          onClick={() => navigate('/admin/tickets')}
          className="w-full text-sm text-gray-500 hover:text-gray-700 transition"
        >
          ← Volver a tickets
        </button>
      </div>
    </div>
  )
}
