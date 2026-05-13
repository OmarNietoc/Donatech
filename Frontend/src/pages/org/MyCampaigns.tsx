import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { campaignApi } from '@/api/catalog.api'
import { useAuthStore } from '@/store/auth.store'
import type { CampaignStatus } from '@/types/campaign.types'

const STATUS_COLOR: Record<CampaignStatus, string> = {
  EN_VALIDACION: 'bg-yellow-100 text-yellow-700',
  ACTIVA: 'bg-green-100 text-green-700',
  INACTIVA: 'bg-red-100 text-red-700',
  FINALIZADA: 'bg-gray-100 text-gray-500',
}

export default function MyCampaigns() {
  const { user } = useAuthStore()

  const { data: campaigns, isLoading } = useQuery({
    queryKey: ['campaigns', 'mine', user?.id],
    queryFn: () => campaignApi.getByBeneficiary(user!.id),
    enabled: !!user,
    refetchInterval: 10_000,
  })

  return (
    <div className="max-w-4xl mx-auto px-8 py-10">
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-gray-800">Mis campañas</h1>
        <Link
          to="/org/campaigns/new"
          className="bg-primary text-white text-sm font-medium px-4 py-2 rounded hover:bg-primary-dark transition"
        >
          + Nueva campaña
        </Link>
      </div>

      {isLoading && <p className="text-gray-500">Cargando...</p>}

      <div className="space-y-3">
        {campaigns?.map((c) => (
          <div key={c.id} className="bg-white rounded-lg shadow p-5 flex items-center justify-between gap-4">
            <div className="flex-1">
              <div className="flex items-center gap-3 mb-1">
                <span className="font-medium text-gray-800">{c.titulo}</span>
                <span className={`text-xs font-medium px-2 py-0.5 rounded ${STATUS_COLOR[c.estado]}`}>
                  {c.estado.replace('_', ' ')}
                </span>
              </div>
              <p className="text-sm text-gray-500 line-clamp-1">{c.descripcion}</p>
              {c.motivoRechazo && (
                <p className="text-xs text-red-600 mt-1">Motivo: {c.motivoRechazo}</p>
              )}
            </div>
            <p className="text-xs text-gray-400 shrink-0">#{c.id}</p>
          </div>
        ))}
      </div>

      {!isLoading && campaigns?.length === 0 && (
        <div className="text-center py-12 text-gray-500">
          <p className="mb-4">No tienes campañas creadas.</p>
          <Link to="/org/campaigns/new" className="text-primary hover:underline font-medium">
            Crear primera campaña →
          </Link>
        </div>
      )}
    </div>
  )
}
