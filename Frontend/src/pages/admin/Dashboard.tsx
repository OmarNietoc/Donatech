import { useQuery } from '@tanstack/react-query'
import { orderApi } from '@/api/order.api'

export default function Dashboard() {
  const { data, isLoading } = useQuery({
    queryKey: ['dashboard'],
    queryFn: orderApi.getDashboard,
    refetchInterval: 30_000,
  })

  return (
    <div className="max-w-5xl mx-auto px-8 py-10">
      <h1 className="text-2xl font-bold text-gray-800 mb-8">Dashboard</h1>

      {isLoading && <p className="text-gray-500">Cargando...</p>}

      {data && (
        <pre className="bg-white rounded-lg shadow p-6 text-sm text-gray-700 overflow-auto">
          {JSON.stringify(data, null, 2)}
        </pre>
      )}
    </div>
  )
}
