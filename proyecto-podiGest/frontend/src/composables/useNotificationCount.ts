import { ref } from 'vue'
import { fetchNotifications } from '../services/notificationsService'

// Estado global compartido
const notificationCount = ref(0)
const isLoading = ref(false)
const isMuted = ref(false)

// Cargar el estado de silenciado desde localStorage al iniciar
const loadMutedState = () => {
  const savedState = localStorage.getItem('notificationsMuted')
  isMuted.value = savedState === 'true'
}

// Inicializar el estado
loadMutedState()

export function useNotificationCount() {
  const loadNotificationCount = async () => {
    isLoading.value = true
    try {
      const notifications = await fetchNotifications()
      notificationCount.value = notifications.length
    } catch (error) {
      console.error('Error al cargar el conteo de notificaciones:', error)
      notificationCount.value = 0
    } finally {
      isLoading.value = false
    }
  }

  const resetCount = () => {
    notificationCount.value = 0
  }

  const toggleMute = () => {
    isMuted.value = !isMuted.value
    // Guardar el estado en localStorage para persistencia
    localStorage.setItem('notificationsMuted', isMuted.value.toString())
  }

  const setMuted = (value: boolean) => {
    isMuted.value = value
    localStorage.setItem('notificationsMuted', value.toString())
  }

  return {
    notificationCount,
    isLoading,
    isMuted,
    loadNotificationCount,
    resetCount,
    toggleMute,
    setMuted
  }
}