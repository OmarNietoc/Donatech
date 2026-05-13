import { useQuery } from '@tanstack/react-query'
import { useParams, Link } from 'react-router-dom'
import { campaignApi, kitApi } from '@/api/catalog.api'
import { useAuthStore } from '@/store/auth.store'

export default function CampaignDetail() {
  const { id } = useParams<{ id: string }>()
  const campaignId = Number(id)
  const { user } = useAuthStore()

  const { data: campaign, isLoading } = useQuery({
    queryKey: ['campaigns', campaignId],
    queryFn: () => campaignApi.getById(campaignId),
    enabled: campaignId > 0,
  })

  const { data: allKits } = useQuery({
    queryKey: ['kits'],
    queryFn: kitApi.getAll,
    enabled: (campaign?.kits.length ?? 0) > 0,
  })

  if (isLoading) return <div className="p-8 text-gray-500">Cargando campaña...</div>
  if (!campaign) return <div className="p-8 text-red-600">Campaña no encontrada.</div>

  const getKitName = (kitId: number) =>
    allKits?.find((k) => k.id === kitId)?.nombre ?? `Kit #${kitId}`

  return (
    <div className="max-w-3xl mx-auto px-8 py-10">
      <div className="flex items-center gap-3 mb-6">
        <Link to="/campaigns" className="text-gray-400 hover:text-gray-600 text-sm">
          ← Campañas
        </Link>
        <span className="text-gray-300">/</span>
        <span className="text-sm text-gray-600">{campaign.titulo}</span>
      </div>

      <div className="bg-white rounded-lg shadow p-8">
        <div className="flex items-start justify-between mb-4">
          <h1 className="text-2xl font-bold text-gray-800">{campaign.titulo}</h1>
          <span className="text-xs font-medium bg-green-100 text-green-700 px-2 py-1 rounded shrink-0">
            {campaign.estado}
          </span>
        </div>

        <p className="text-gray-600 mb-8 leading-relaxed">{campaign.descripcion}</p>

        {campaign.kits.length > 0 && (
          <div className="mb-8">
            <h2 className="text-lg font-semibold text-gray-700 mb-4">Kits necesarios</h2>
            <div className="space-y-2">
              {campaign.kits.map((ck) => (
                <div
                  key={ck.kitId}
                  className="flex items-center justify-between bg-gray-50 rounded px-4 py-3 text-sm"
                >
                  <span className="font-medium text-gray-700">{getKitName(ck.kitId)}</span>
                  <div className="flex items-center gap-4 text-gray-500">
                    <span>Necesario: {ck.cantidadNecesaria}</span>
                    <span>
                      Entregado:{' '}
                      <span
                        className={
                          ck.cantidadFulfilled >= ck.cantidadNecesaria
                            ? 'text-green-600 font-medium'
                            : 'text-gray-600'
                        }
                      >
                        {ck.cantidadFulfilled}
                      </span>
                    </span>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        {user?.roles.includes('ROLE_DONANTE') && campaign.estado === 'ACTIVA' && (
          <Link
            to={`/orders/new?campaignId=${campaign.id}`}
            className="inline-block bg-primary text-white font-semibold px-6 py-3 rounded-lg hover:bg-primary-dark transition"
          >
            Donar a esta campaña
          </Link>
        )}

        {!user && campaign.estado === 'ACTIVA' && (
          <div className="bg-blue-50 border border-blue-200 rounded-lg p-4 text-sm text-blue-700">
            <Link to="/login" className="font-medium hover:underline">Inicia sesión</Link>
            {' '}como donante para contribuir a esta campaña.
          </div>
        )}
      </div>
    </div>
  )
}
