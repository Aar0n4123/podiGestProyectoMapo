import { ref } from 'vue'
import { fetchNotificationCount } from '../services/notificationsService'

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
      // Usar el nuevo endpoint que cuenta solo notificaciones no silenciadas
      const count = await fetchNotificationCount()
      notificationCount.value = count
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

  const decrementCount = () => {
    if (notificationCount.value > 0) {
      notificationCount.value--
    }
  }

  const incrementCount = () => {
    notificationCount.value++
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
    decrementCount,
    incrementCount,
    toggleMute,
    setMuted
  }
}