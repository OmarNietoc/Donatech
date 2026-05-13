import { createBrowserRouter, Navigate, Outlet } from 'react-router-dom'
import { useAuthStore } from '@/store/auth.store'
import Layout from '@/components/layout/Layout'

function RequireAuth({ allowedRoles }: { allowedRoles?: string[] }) {
  const { token, user } = useAuthStore()
  if (!token) return <Navigate to="/login" replace />
  if (allowedRoles && !user?.roles.some((r) => allowedRoles.includes(r))) {
    return <Navigate to="/" replace />
  }
  return <Outlet />
}

function GuestOnly() {
  const token = useAuthStore((s) => s.token)
  return token ? <Navigate to="/" replace /> : <Outlet />
}

export const router = createBrowserRouter([
  {
    element: <Layout />,
    children: [
      // Rutas públicas
      {
        path: '/',
        lazy: () => import('@/pages/public/Landing').then((m) => ({ Component: m.default })),
      },
      {
        path: '/campaigns',
        lazy: () => import('@/pages/public/CampaignList').then((m) => ({ Component: m.default })),
      },
      {
        path: '/campaigns/:id',
        lazy: () => import('@/pages/public/CampaignDetail').then((m) => ({ Component: m.default })),
      },

      // Auth (solo sin token)
      {
        element: <GuestOnly />,
        children: [
          {
            path: '/login',
            lazy: () => import('@/pages/auth/Login').then((m) => ({ Component: m.default })),
          },
          {
            path: '/register',
            lazy: () => import('@/pages/auth/Register').then((m) => ({ Component: m.default })),
          },
        ],
      },

      // Donante
      {
        element: <RequireAuth allowedRoles={['ROLE_DONANTE']} />,
        children: [
          {
            path: '/orders/new',
            lazy: () => import('@/pages/donante/NewOrder').then((m) => ({ Component: m.default })),
          },
          {
            path: '/orders/:id',
            lazy: () => import('@/pages/donante/OrderTracking').then((m) => ({ Component: m.default })),
          },
          {
            path: '/orders/:id/proof',
            lazy: () => import('@/pages/donante/UploadProof').then((m) => ({ Component: m.default })),
          },
        ],
      },

      // Org / Beneficiario
      {
        element: <RequireAuth allowedRoles={['ROLE_ORGANIZACION', 'ROLE_BENEFICIARIO']} />,
        children: [
          {
            path: '/org/campaigns',
            lazy: () => import('@/pages/org/MyCampaigns').then((m) => ({ Component: m.default })),
          },
          {
            path: '/org/campaigns/new',
            lazy: () => import('@/pages/org/CreateCampaign').then((m) => ({ Component: m.default })),
          },
        ],
      },

      // Admin / Voluntario
      {
        element: <RequireAuth allowedRoles={['ROLE_ADMIN', 'ROLE_VOLUNTARIO']} />,
        children: [
          {
            path: '/admin/tickets',
            lazy: () => import('@/pages/admin/TicketList').then((m) => ({ Component: m.default })),
          },
          {
            path: '/admin/tickets/:id/validate',
            lazy: () =>
              import('@/pages/admin/ValidateTicket').then((m) => ({ Component: m.default })),
          },
        ],
      },

      // Solo Admin
      {
        element: <RequireAuth allowedRoles={['ROLE_ADMIN']} />,
        children: [
          {
            path: '/admin/dashboard',
            lazy: () => import('@/pages/admin/Dashboard').then((m) => ({ Component: m.default })),
          },
        ],
      },

      // 404
      {
        path: '*',
        element: <Navigate to="/" replace />,
      },
    ],
  },
])
