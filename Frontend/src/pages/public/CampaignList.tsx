import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { campaignApi } from '@/api/catalog.api'

export default function CampaignList() {
  const { data: campaigns, isLoading, error } = useQuery({
    queryKey: ['campaigns', 'active'],
    queryFn: campaignApi.getActive,
  })

  return (
    <div className="max-w-4xl mx-auto px-8 py-10">
      <h1 className="text-2xl font-bold text-gray-800 mb-6">Campañas activas</h1>

      {isLoading && <p className="text-gray-500">Cargando...</p>}
      {error && <p className="text-red-600">Error al cargar campañas. Intenta de nuevo.</p>}

      {!isLoading && campaigns?.length === 0 && (
        <p className="text-gray-500">No hay campañas activas en este momento.</p>
      )}

      <div className="grid gap-4">
        {campaigns?.map((c) => (
          <Link
            key={c.id}
            to={`/campaigns/${c.id}`}
            className="block bg-white rounded-lg shadow p-6 hover:shadow-md transition border border-gray-100"
          >
            <div className="flex items-start justify-between gap-4">
              <div className="flex-1">
                <h2 className="text-lg font-semibold text-gray-800">{c.titulo}</h2>
                <p className="text-gray-500 text-sm mt-1 line-clamp-2">{c.descripcion}</p>
                <div className="mt-3 flex items-center gap-3 text-sm text-gray-400">
                  <span>{c.kits.length} kit{c.kits.length !== 1 ? 's' : ''} requeridos</span>
                </div>
              </div>
              <span className="shrink-0 text-xs font-medium bg-green-100 text-green-700 px-2 py-1 rounded">
                ACTIVA
              </span>
            </div>
          </Link>
        ))}
      </div>
    </div>
  )
}
