import { useRef, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { orderApi } from '@/api/order.api'

export default function UploadProof() {
  const { id } = useParams<{ id: string }>()
  const orderId = Number(id)
  const navigate = useNavigate()
  const fileRef = useRef<HTMLInputElement>(null)
  const [uploading, setUploading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    const file = fileRef.current?.files?.[0]
    if (!file) {
      setError('Selecciona un archivo')
      return
    }

    setUploading(true)
    setError(null)
    try {
      await orderApi.uploadTransferProof(orderId, file)
      navigate(`/orders/${orderId}`)
    } catch {
      setError('Error al subir el comprobante. Intenta de nuevo.')
    } finally {
      setUploading(false)
    }
  }

  return (
    <div className="max-w-lg mx-auto px-8 py-10">
      <h1 className="text-2xl font-bold text-gray-800 mb-2">Comprobante de transferencia</h1>
      <p className="text-gray-500 text-sm mb-8">Orden #{orderId}</p>

      <div className="bg-white rounded-lg shadow p-8">
        <div className="bg-blue-50 border border-blue-200 rounded-lg p-4 mb-6 text-sm text-blue-700">
          Adjunta el comprobante de tu transferencia bancaria. Se validará en las próximas horas hábiles.
        </div>

        <form onSubmit={handleSubmit} className="space-y-5">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Comprobante (PDF, imagen)
            </label>
            <input
              ref={fileRef}
              type="file"
              accept=".pdf,.jpg,.jpeg,.png,.webp"
              className="w-full text-sm text-gray-500 file:mr-4 file:py-2 file:px-4 file:rounded file:border-0 file:text-sm file:font-medium file:bg-primary file:text-white hover:file:bg-primary-dark cursor-pointer"
            />
          </div>

          {error && (
            <p className="text-red-600 text-sm bg-red-50 px-3 py-2 rounded">{error}</p>
          )}

          <div className="flex gap-3">
            <button
              type="submit"
              disabled={uploading}
              className="flex-1 bg-primary text-white py-2 rounded-md font-medium hover:bg-primary-dark transition disabled:opacity-50"
            >
              {uploading ? 'Subiendo...' : 'Enviar comprobante'}
            </button>
            <button
              type="button"
              onClick={() => navigate(`/orders/${orderId}`)}
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
