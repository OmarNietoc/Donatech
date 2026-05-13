import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Link, useNavigate } from 'react-router-dom'
import { useState } from 'react'
import { authApi } from '@/api/auth.api'

type UserType = 'DONANTE' | 'ORGANIZACION' | 'BENEFICIARIO'

const baseSchema = z.object({
  name: z.string().min(2, 'Nombre requerido'),
  email: z.string().email('Email inválido'),
  password: z.string().min(6, 'Mínimo 6 caracteres'),
  phone: z.string().optional(),
})

type FormValues = z.infer<typeof baseSchema>

const roleIdMap: Record<UserType, number> = {
  DONANTE: 3,
  ORGANIZACION: 5,
  BENEFICIARIO: 4,
}

export default function Register() {
  const [userType, setUserType] = useState<UserType>('DONANTE')
  const [success, setSuccess] = useState(false)
  const navigate = useNavigate()

  const {
    register,
    handleSubmit,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<FormValues>({ resolver: zodResolver(baseSchema) })

  const onSubmit = async (data: FormValues) => {
    try {
      if (userType === 'ORGANIZACION') {
        await authApi.registerOrganization({ ...data, roleId: 5 })
      } else {
        await authApi.register({ ...data, roleId: roleIdMap[userType] })
      }
      setSuccess(true)
      setTimeout(() => navigate('/login'), 2000)
    } catch {
      setError('root', { message: 'Error al registrar. El email puede estar en uso.' })
    }
  }

  if (success) {
    return (
      <div className="min-h-[calc(100vh-52px)] flex items-center justify-center">
        <div className="bg-green-50 border border-green-300 rounded-lg p-8 text-center max-w-md">
          <p className="text-green-800 font-medium text-lg">¡Registro exitoso!</p>
          <p className="text-green-600 mt-1 text-sm">Redirigiendo al login...</p>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-[calc(100vh-52px)] flex items-center justify-center bg-gray-50 py-8">
      <div className="w-full max-w-md bg-white rounded-lg shadow p-8">
        <h1 className="text-2xl font-bold text-primary mb-6">Crear cuenta</h1>

        <div className="flex rounded-md border border-gray-200 mb-6 overflow-hidden">
          {(['DONANTE', 'ORGANIZACION', 'BENEFICIARIO'] as UserType[]).map((type) => (
            <button
              key={type}
              type="button"
              onClick={() => setUserType(type)}
              className={`flex-1 py-2 text-sm font-medium transition ${
                userType === type
                  ? 'bg-primary text-white'
                  : 'text-gray-600 hover:bg-gray-50'
              }`}
            >
              {type === 'DONANTE' ? 'Donante' : type === 'ORGANIZACION' ? 'Organización' : 'Beneficiario'}
            </button>
          ))}
        </div>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Nombre completo</label>
            <input
              {...register('name')}
              className="w-full border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-primary"
            />
            {errors.name && <p className="text-red-600 text-sm mt-1">{errors.name.message}</p>}
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Email</label>
            <input
              {...register('email')}
              type="email"
              className="w-full border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-primary"
            />
            {errors.email && <p className="text-red-600 text-sm mt-1">{errors.email.message}</p>}
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Contraseña</label>
            <input
              {...register('password')}
              type="password"
              className="w-full border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-primary"
            />
            {errors.password && (
              <p className="text-red-600 text-sm mt-1">{errors.password.message}</p>
            )}
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Teléfono <span className="text-gray-400">(opcional)</span>
            </label>
            <input
              {...register('phone')}
              type="tel"
              className="w-full border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-primary"
            />
          </div>

          {errors.root && (
            <p className="text-red-600 text-sm bg-red-50 px-3 py-2 rounded">{errors.root.message}</p>
          )}

          <button
            type="submit"
            disabled={isSubmitting}
            className="w-full bg-primary text-white py-2 rounded-md font-medium hover:bg-primary-dark transition disabled:opacity-50"
          >
            {isSubmitting ? 'Registrando...' : 'Crear cuenta'}
          </button>
        </form>

        <p className="mt-4 text-sm text-gray-600 text-center">
          ¿Ya tienes cuenta?{' '}
          <Link to="/login" className="text-primary hover:underline font-medium">
            Inicia sesión
          </Link>
        </p>
      </div>
    </div>
  )
}
