import { useQuery } from '@tanstack/react-query'
import { orderApi } from '@/api/order.api'
import { TERMINAL_STATES } from '@/types/order.types'
import type { OrderResponse } from '@/types/order.types'

export function useOrderPolling(orderId: number) {
  return useQuery<OrderResponse>({
    queryKey: ['orders', orderId],
    queryFn: () => orderApi.getById(orderId),
    refetchInterval: (query) => {
      const estado = query.state.data?.estado
      if (!estado) return 5000
      return TERMINAL_STATES.includes(estado) ? false : 5000
    },
    staleTime: 0,
    enabled: orderId > 0,
  })
}
