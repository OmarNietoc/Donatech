import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { campaignApi } from '@/api/catalog.api'

export default function Landing() {
  const { data: campaigns, isLoading } = useQuery({
    queryKey: ['campaigns', 'active'],
    queryFn: campaignApi.getActive,
  })

  return (
    <div>
      <section className="bg-primary text-white py-16 px-8 text-center">
        <h1 className="text-4xl font-bold mb-4">Donatech</h1>
        <p className="text-xl opacity-90 mb-8 max-w-2xl mx-auto">
          Hub logístico de donaciones ante catástrofes en Chile.
          Conectamos donantes con campañas humanitarias activas.
        </p>
        <Link
          to="/campaigns"
          className="inline-block bg-white text-primary font-semibold px-6 py-3 rounded-lg hover:bg-blue-50 transition"
        >
          Ver campañas activas
        </Link>
      </section>

      <section className="max-w-4xl mx-auto px-8 py-12">
        <h2 className="text-2xl font-bold text-gray-800 mb-6">Campañas activas</h2>

        {isLoading ? (
          <div className="text-gray-500">Cargando campañas...</div>
        ) : campaigns?.length === 0 ? (
          <p className="text-gray-500">No hay campañas activas en este momento.</p>
        ) : (
          <div className="grid gap-4 sm:grid-cols-2">
            {campaigns?.slice(0, 4).map((c) => (
              <Link
                key={c.id}
                to={`/campaigns/${c.id}`}
                className="block bg-white rounded-lg shadow p-5 hover:shadow-md transition border border-gray-100"
              >
                <h3 className="font-semibold text-gray-800 mb-2">{c.titulo}</h3>
                <p className="text-gray-500 text-sm line-clamp-2">{c.descripcion}</p>
                <span className="inline-block mt-3 text-xs font-medium bg-green-100 text-green-700 px-2 py-0.5 rounded">
                  Activa
                </span>
              </Link>
            ))}
          </div>
        )}

        {(campaigns?.length ?? 0) > 4 && (
          <div className="mt-6 text-center">
            <Link to="/campaigns" className="text-primary hover:underline font-medium">
              Ver todas las campañas →
            </Link>
          </div>
        )}
      </section>
    </div>
  )
}
