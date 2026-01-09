export interface NotificationSummary {
  id: string
  fechaEnvio: string
  asunto: string
  remitente: string
  mensaje: string
  silenciada: boolean
  tieneRecordatorio: boolean
  fechaRecordatorio?: string
  recordatorioActivo: boolean
}

const API_URL = 'http://localhost:8080/api/notificaciones'

// Obtiene todas las notificaciones
export const fetchNotifications = async (): Promise<NotificationSummary[]> => {
  try {
    const response = await fetch(API_URL)

    if (!response.ok) {
      throw new Error(`Error al obtener notificaciones: ${response.status}`)
    }

    return await response.json()
  } catch (error) {
    console.error('No fue posible cargar las notificaciones', error)
    return []
  }
}

// Obtiene una notificación específica por ID
export const fetchNotificationById = async (id: string): Promise<NotificationSummary | null> => {
  try {
    const response = await fetch(`${API_URL}/${id}`)

    if (!response.ok) {
      return null
    }

    return await response.json()
  } catch (error) {
    console.error(`No fue posible obtener la notificación ${id}`, error)
    return null
  }
}

// Silencia una notificación específica
export const muteNotification = async (id: string): Promise<boolean> => {
  try {
    const response = await fetch(`${API_URL}/${id}/silenciar`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json'
      }
    })

    if (!response.ok) {
      throw new Error(`Error al silenciar notificación: ${response.status}`)
    }

    return true
  } catch (error) {
    console.error(`No fue posible silenciar la notificación ${id}`, error)
    return false
  }
}

// Dessilencia una notificación específica
export const unmuteNotification = async (id: string): Promise<boolean> => {
  try {
    const response = await fetch(`${API_URL}/${id}/dessilenciar`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json'
      }
    })

    if (!response.ok) {
      throw new Error(`Error al dessilenciar notificación: ${response.status}`)
    }

    return true
  } catch (error) {
    console.error(`No fue posible dessilenciar la notificación ${id}`, error)
    return false
  }
}

// Obtiene el conteo de notificaciones no silenciadas
export const fetchNotificationCount = async (): Promise<number> => {
  try {
    const response = await fetch(`${API_URL}/count`)

    if (!response.ok) {
      throw new Error(`Error al obtener conteo de notificaciones: ${response.status}`)
    }

    return await response.json()
  } catch (error) {
    console.error('No fue posible obtener el conteo de notificaciones', error)
    return 0
  }
}

// Elimina todas las notificaciones del usuario
export const deleteAllNotifications = async (): Promise<boolean> => {
  try {
    const response = await fetch(API_URL, {
      method: 'DELETE',
      headers: {
        'Content-Type': 'application/json'
      }
    })

    if (!response.ok) {
      throw new Error(`Error al eliminar notificaciones: ${response.status}`)
    }

    return true
  } catch (error) {
    console.error('No fue posible eliminar las notificaciones', error)
    return false
  }
}

// Establece un recordatorio para una notificación
export const setReminder = async (id: string, fechaRecordatorio: string): Promise<boolean> => {
  try {
    const response = await fetch(`${API_URL}/${id}/recordatorio`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ fechaRecordatorio })
    })

    if (!response.ok) {
      throw new Error(`Error al establecer recordatorio: ${response.status}`)
    }

    return true
  } catch (error) {
    console.error(`No fue posible establecer recordatorio para notificación ${id}`, error)
    return false
  }
}

// Actualiza el recordatorio de una notificación
export const updateReminder = async (id: string, fechaRecordatorio: string): Promise<boolean> => {
  try {
    const response = await fetch(`${API_URL}/${id}/recordatorio`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ fechaRecordatorio })
    })

    if (!response.ok) {
      throw new Error(`Error al actualizar recordatorio: ${response.status}`)
    }

    return true
  } catch (error) {
    console.error(`No fue posible actualizar recordatorio para notificación ${id}`, error)
    return false
  }
}

// Desactiva el recordatorio de una notificación
export const deleteReminder = async (id: string): Promise<boolean> => {
  try {
    const response = await fetch(`${API_URL}/${id}/recordatorio`, {
      method: 'DELETE',
      headers: {
        'Content-Type': 'application/json'
      }
    })

    if (!response.ok) {
      throw new Error(`Error al desactivar recordatorio: ${response.status}`)
    }

    return true
  } catch (error) {
    console.error(`No fue posible desactivar recordatorio para notificación ${id}`, error)
    return false
  }
}

// Obtiene notificaciones con recordatorios pendientes
export const fetchPendingReminders = async (): Promise<NotificationSummary[]> => {
  try {
    const response = await fetch(`${API_URL}/recordatorios/pendientes`)

    if (!response.ok) {
      throw new Error(`Error al obtener recordatorios pendientes: ${response.status}`)
    }

    return await response.json()
  } catch (error) {
    console.error('No fue posible obtener recordatorios pendientes', error)
    return []
  }
}