import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useNavigate } from 'react-router-dom'
import { campaignApi } from '@/api/catalog.api'
import { useAuthStore } from '@/store/auth.store'

const schema = z.object({
  titulo: z.string().min(3, 'Mínimo 3 caracteres'),
  descripcion: z.string().min(10, 'Mínimo 10 caracteres'),
  observaciones: z.string().optional(),
})

type FormValues = z.infer<typeof schema>

export default function CreateCampaign() {
  const { user } = useAuthStore()
  const navigate = useNavigate()

  const {
    register,
    handleSubmit,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<FormValues>({ resolver: zodResolver(schema) })

  const onSubmit = async (data: FormValues) => {
    if (!user) return
    try {
      await campaignApi.create({ ...data, beneficiarioId: user.id })
      navigate('/org/campaigns')
    } catch {
      setError('root', { message: 'Error al crear la campaña. Intenta de nuevo.' })
    }
  }

  return (
    <div className="max-w-2xl mx-auto px-8 py-10">
      <h1 className="text-2xl font-bold text-gray-800 mb-2">Crear campaña</h1>
      <p className="text-gray-500 text-sm mb-8">
        La campaña quedará en estado <strong>EN_VALIDACION</strong> hasta ser aprobada por un administrador.
      </p>

      <div className="bg-white rounded-lg shadow p-8">
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Título</label>
            <input
              {...register('titulo')}
              className="w-full border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-primary"
              placeholder="Ej: Kits de abrigo para zona sur"
            />
            {errors.titulo && <p className="text-red-600 text-sm mt-1">{errors.titulo.message}</p>}
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Descripción</label>
            <textarea
              {...register('descripcion')}
              rows={4}
              className="w-full border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-primary"
              placeholder="Describe el propósito de la campaña y a quiénes ayuda..."
            />
            {errors.descripcion && (
              <p className="text-red-600 text-sm mt-1">{errors.descripcion.message}</p>
            )}
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Observaciones <span className="text-gray-400">(opcional)</span>
            </label>
            <textarea
              {...register('observaciones')}
              rows={2}
              className="w-full border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-primary"
              placeholder="Información adicional para el equipo de validación..."
            />
          </div>

          {errors.root && (
            <p className="text-red-600 text-sm bg-red-50 px-3 py-2 rounded">{errors.root.message}</p>
          )}

          <div className="flex gap-3 pt-2">
            <button
              type="submit"
              disabled={isSubmitting}
              className="flex-1 bg-primary text-white py-2.5 rounded-md font-medium hover:bg-primary-dark transition disabled:opacity-50"
            >
              {isSubmitting ? 'Creando...' : 'Crear campaña'}
            </button>
            <button
              type="button"
              onClick={() => navigate('/org/campaigns')}
              className="px-4 py-2 border border-gray-300 rounded-md text-gray-600 hover:bg-gray-50 transition"
            >
              Cancelar
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
