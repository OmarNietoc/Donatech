import { useQuery, useMutation } from '@tanstack/react-query'
import { useSearchParams, useNavigate } from 'react-router-dom'
import { useState } from 'react'
import { campaignApi } from '@/api/catalog.api'
import { orderApi } from '@/api/order.api'
import { useAuthStore } from '@/store/auth.store'

export default function NewOrder() {
  const [searchParams] = useSearchParams()
  const campaignId = Number(searchParams.get('campaignId') ?? 0)
  const { user } = useAuthStore()
  const navigate = useNavigate()
  const [quantities, setQuantities] = useState<Record<number, number>>({})
  const [error, setError] = useState<string | null>(null)

  const { data: campaign, isLoading } = useQuery({
    queryKey: ['campaigns', campaignId],
    queryFn: () => campaignApi.getById(campaignId),
    enabled: campaignId > 0,
  })

  const createOrder = useMutation({
    mutationFn: orderApi.create,
    onSuccess: (order) => navigate(`/orders/${order.id}`),
    onError: () => setError('Error al crear la donación. Intenta de nuevo.'),
  })

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    if (!user) return

    const items = Object.entries(quantities)
      .filter(([, qty]) => qty > 0)
      .map(([kitId, quantity]) => ({ kitId: Number(kitId), quantity }))

    if (items.length === 0) {
      setError('Selecciona al menos un kit con cantidad mayor a 0.')
      return
    }

    setError(null)
    createOrder.mutate({
      userEmail: user.email,
      campaignId: campaignId || undefined,
      items,
    })
  }

  if (campaignId > 0 && isLoading) return <div className="p-8 text-gray-500">Cargando campaña...</div>

  return (
    <div className="max-w-2xl mx-auto px-8 py-10">
      <h1 className="text-2xl font-bold text-gray-800 mb-2">Nueva donación</h1>
      {campaign && (
        <p className="text-gray-500 text-sm mb-8">
          Campaña: <strong>{campaign.titulo}</strong>
        </p>
      )}

      <div className="bg-white rounded-lg shadow p-8">
        {(!campaign || campaign.kits.length === 0) && (
          <p className="text-gray-500 text-sm">No hay kits disponibles para esta campaña.</p>
        )}

        {campaign && campaign.kits.length > 0 && (
          <form onSubmit={handleSubmit} className="space-y-5">
            <div>
              <h2 className="font-semibold text-gray-700 mb-3">Selecciona kits y cantidades</h2>
              <div className="space-y-3">
                {campaign.kits.map((ck) => (
                  <div key={ck.kitId} className="flex items-center justify-between bg-gray-50 rounded px-4 py-3">
                    <div>
                      <p className="text-sm font-medium text-gray-700">Kit #{ck.kitId}</p>
                      <p className="text-xs text-gray-400">Necesarios: {ck.cantidadNecesaria}</p>
                    </div>
                    <input
                      type="number"
                      min={0}
                      max={ck.cantidadNecesaria}
                      value={quantities[ck.kitId] ?? 0}
                      onChange={(e) =>
                        setQuantities((prev) => ({ ...prev, [ck.kitId]: Number(e.target.value) }))
                      }
                      className="w-20 border border-gray-300 rounded px-2 py-1 text-sm text-center focus:outline-none focus:ring-2 focus:ring-primary"
                    />
                  </div>
                ))}
              </div>
            </div>

            {error && (
              <p className="text-red-600 text-sm bg-red-50 px-3 py-2 rounded">{error}</p>
            )}

            <button
              type="submit"
              disabled={createOrder.isPending}
              className="w-full bg-primary text-white py-2.5 rounded-md font-medium hover:bg-primary-dark transition disabled:opacity-50"
            >
              {createOrder.isPending ? 'Creando donación...' : 'Crear donación'}
            </button>
          </form>
        )}
      </div>
    </div>
  )
}
