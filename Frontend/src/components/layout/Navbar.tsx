import { Link, useNavigate } from 'react-router-dom'
import { useAuthStore } from '@/store/auth.store'

export default function Navbar() {
  const { user, token, logout } = useAuthStore()
  const navigate = useNavigate()

  const has = (role: string) => user?.roles.includes(role) ?? false

  const handleLogout = () => {
    logout()
    navigate('/')
  }

  return (
    <nav className="bg-primary text-white px-6 py-3 flex items-center justify-between shadow-md">
      <div className="flex items-center gap-6">
        <Link to="/" className="font-bold text-lg tracking-tight">Donatech</Link>
        <Link to="/campaigns" className="text-sm hover:text-blue-200 transition">
          Campañas
        </Link>
        {has('ROLE_DONANTE') && (
          <Link to="/orders/new" className="text-sm hover:text-blue-200 transition">
            Donar
          </Link>
        )}
        {(has('ROLE_ORGANIZACION') || has('ROLE_BENEFICIARIO')) && (
          <Link to="/org/campaigns" className="text-sm hover:text-blue-200 transition">
            Mis campañas
          </Link>
        )}
        {(has('ROLE_ADMIN') || has('ROLE_VOLUNTARIO')) && (
          <Link to="/admin/tickets" className="text-sm hover:text-blue-200 transition">
            Tickets
          </Link>
        )}
        {has('ROLE_ADMIN') && (
          <Link to="/admin/dashboard" className="text-sm hover:text-blue-200 transition">
            Dashboard
          </Link>
        )}
      </div>

      <div className="flex items-center gap-3">
        {token ? (
          <>
            <span className="text-sm opacity-75">{user?.email}</span>
            <button
              onClick={handleLogout}
              className="text-sm bg-white text-primary px-3 py-1 rounded font-medium hover:bg-blue-50 transition"
            >
              Salir
            </button>
          </>
        ) : (
          <Link
            to="/login"
            className="text-sm bg-white text-primary px-3 py-1 rounded font-medium hover:bg-blue-50 transition"
          >
            Ingresar
          </Link>
        )}
      </div>
    </nav>
  )
}
