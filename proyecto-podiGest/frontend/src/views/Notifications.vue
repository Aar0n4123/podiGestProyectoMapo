<script setup lang="ts">
import { onMounted, ref } from 'vue'
import SideBar from '../components/SideBar.vue'
import { ArrowPathIcon } from '@heroicons/vue/24/solid'
import {
  fetchNotificationById,
  fetchNotifications,
  muteNotification,
  unmuteNotification,
  deleteAllNotifications,
  setReminder,
  updateReminder,
  deleteReminder,
  type NotificationSummary
} from '../services/notificationsService'
import { useNotificationCount } from '../composables/useNotificationCount'

const isCollapsed = ref(false)
const notifications = ref<NotificationSummary[]>([])
const loading = ref(true)
const errorMessage = ref('')
const selectedNotification = ref<NotificationSummary | null>(null)
const loadingDetails = ref(false)
const showDetailView = ref(false)
const mutingNotificationId = ref<string | null>(null)
const unmutingNotificationId = ref<string | null>(null)
const showDeleteConfirmation = ref(false)
const deletingAllNotifications = ref(false)
const showReminderModal = ref(false)
const reminderNotificationId = ref<string | null>(null)
const reminderDateTime = ref('')
const settingReminder = ref(false)
const { loadNotificationCount, decrementCount, incrementCount, isMuted, toggleMute } = useNotificationCount()

const toggleSidebar = () => {
  isCollapsed.value = !isCollapsed.value
}

const loadNotifications = async () => {
  loading.value = true
  errorMessage.value = ''
  const response = await fetchNotifications()
  if (!response.length) {
    errorMessage.value =
      'No hay notificaciones disponibles en este momento. Intentar recargar más tarde.'
  }
  notifications.value = response
  loading.value = false
  loadNotificationCount()
}

const openNotification = async (id: string) => {
  loadingDetails.value = true
  errorMessage.value = ''
  const notification = await fetchNotificationById(id)
  if (notification) {
    selectedNotification.value = notification
    showDetailView.value = true
  } else {
    errorMessage.value = 'No se pudo cargar la información de la notificación seleccionada.'
  }
  loadingDetails.value = false
}

const backToList = () => {
  showDetailView.value = false
  selectedNotification.value = null
  errorMessage.value = ''
}

const handleMuteNotification = async (id: string, event: Event) => {
  event.stopPropagation()

  mutingNotificationId.value = id
  errorMessage.value = ''

  try {
    const success = await muteNotification(id)

    if (success) {
      const notification = notifications.value.find(n => n.id === id)
      if (notification) {
        notification.silenciada = true
      }

      decrementCount()

      console.log(`Notificación ${id} silenciada exitosamente`)
    } else {
      errorMessage.value = 'No se pudo silenciar la notificación. Intente nuevamente.'
    }
  } catch (error) {
    console.error('Error al silenciar notificación:', error)
    errorMessage.value = 'Ocurrió un error al silenciar la notificación.'
  } finally {
    mutingNotificationId.value = null
  }
}

const handleUnmuteNotification = async (id: string, event: Event) => {
  event.stopPropagation()

  unmutingNotificationId.value = id
  errorMessage.value = ''

  try {
    const success = await unmuteNotification(id)

    if (success) {
      const notification = notifications.value.find(n => n.id === id)
      if (notification) {
        notification.silenciada = false
      }

      incrementCount()

      console.log(`Notificación ${id} dessilenciada exitosamente`)
    } else {
      errorMessage.value = 'No se pudo dessilenciar la notificación. Intente nuevamente.'
    }
  } catch (error) {
    console.error('Error al dessilenciar notificación:', error)
    errorMessage.value = 'Ocurrió un error al dessilenciar la notificación.'
  } finally {
    unmutingNotificationId.value = null
  }
}

const openDeleteConfirmation = () => {
  showDeleteConfirmation.value = true
  errorMessage.value = ''
}

const closeDeleteConfirmation = () => {
  showDeleteConfirmation.value = false
}

const handleDeleteAllNotifications = async () => {
  deletingAllNotifications.value = true
  errorMessage.value = ''

  try {
    const success = await deleteAllNotifications()

    if (success) {
      notifications.value = []
      showDeleteConfirmation.value = false
      loadNotificationCount()
      console.log('Todas las notificaciones han sido eliminadas exitosamente')
    } else {
      errorMessage.value = 'No se pudieron eliminar las notificaciones. Intente nuevamente.'
    }
  } catch (error) {
    console.error('Error al eliminar notificaciones:', error)
    errorMessage.value = 'Ocurrió un error al eliminar las notificaciones.'
  } finally {
    deletingAllNotifications.value = false
  }
}

const openReminderModal = (id: string, event: Event) => {
  event.stopPropagation()
  reminderNotificationId.value = id
  const notif = notifications.value.find(n => n.id === id)
  reminderDateTime.value = notif?.fechaRecordatorio || ''
  showReminderModal.value = true
  errorMessage.value = ''
}

const closeReminderModal = () => {
  showReminderModal.value = false
  reminderNotificationId.value = null
  reminderDateTime.value = ''
}

const handleSetReminder = async () => {
  if (!reminderNotificationId.value || !reminderDateTime.value) {
    errorMessage.value = 'Por favor selecciona una fecha y hora válida.'
    return
  }

  settingReminder.value = true
  errorMessage.value = ''

  try {
    const notification = notifications.value.find(n => n.id === reminderNotificationId.value)
    let success = false

    if (notification && notification.tieneRecordatorio) {
      success = await updateReminder(reminderNotificationId.value, reminderDateTime.value)
    } else {
      success = await setReminder(reminderNotificationId.value, reminderDateTime.value)
    }

    if (success) {
      if (notification) {
        notification.tieneRecordatorio = true
        notification.fechaRecordatorio = reminderDateTime.value
        notification.recordatorioActivo = true
      }
      if (selectedNotification.value && selectedNotification.value.id === reminderNotificationId.value) {
        selectedNotification.value.tieneRecordatorio = true
        selectedNotification.value.fechaRecordatorio = reminderDateTime.value
        selectedNotification.value.recordatorioActivo = true
      }
      closeReminderModal()
      console.log(`Recordatorio establecido para notificación ${reminderNotificationId.value}`)
    } else {
      errorMessage.value = 'No se pudo establecer el recordatorio. Intente nuevamente.'
    }
  } catch (error) {
    console.error('Error al establecer recordatorio:', error)
    errorMessage.value = 'Ocurrió un error al establecer el recordatorio.'
  } finally {
    settingReminder.value = false
  }
}

const handleDeleteReminder = async (id: string, event: Event) => {
  event.stopPropagation()
  errorMessage.value = ''

  try {
    const success = await deleteReminder(id)

    if (success) {
      const notification = notifications.value.find(n => n.id === id)
      if (notification) {
        notification.recordatorioActivo = false
      }
      if (selectedNotification.value && selectedNotification.value.id === id) {
        selectedNotification.value.recordatorioActivo = false
      }
      console.log(`Recordatorio desactivado para notificación ${id}`)
    } else {
      errorMessage.value = 'No se pudo desactivar el recordatorio. Intente nuevamente.'
    }
  } catch (error) {
    console.error('Error al desactivar recordatorio:', error)
    errorMessage.value = 'Ocurrió un error al desactivar el recordatorio.'
  }
}

onMounted(() => {
  loadNotifications()
})
</script>

<template>
  <div class="flex">
    <SideBar :is-collapsed="isCollapsed" @toggle="toggleSidebar" />
    <main :class="[
      'transition-all duration-300 p-6 overflow-auto bg-blue-150 min-h-screen w-full',
      isCollapsed ? 'ml-20' : 'ml-64'
    ]">
      <!-- VISTA DE LISTA DE NOTIFICACIONES -->
      <section v-if="!showDetailView" class="max-w-6xl mx-auto space-y-6">
        <header class="bg-white shadow-sm rounded-lg p-6 border border-blue-200">
          <h1 class="text-3xl font-bold text-blue-500  mb-2">Notificaciones</h1>
          <p class="text-gray-600">
            Consulta los recordatorios y avisos importantes enviados por la institución.
          </p>
        </header>

        <div v-if="isMuted" class="bg-gray-100 border border-gray-300 rounded-lg p-4 flex items-center gap-3">
          <svg class="w-6 h-6 text-gray-600 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          <div class="flex-1">
            <p class="text-gray-800 font-medium">Las alertas de notificaciones están silenciadas</p>
            <p class="text-gray-600 text-sm">No verás el badge rojo en el icono de notificaciones hasta que las actives nuevamente.</p>
          </div>
        </div>

        <article class="bg-white rounded-lg shadow border border-blue-200">
          <h2 class="text-2xl font-bold text-blue-500 mt-4">Bandeja de Entrada</h2>
          <div class="flex items-center justify-end px-6 py-4 border-b border-blue-100">
            <div class="flex items-center gap-3">
              <button @click="openDeleteConfirmation" :class="[
                'flex items-center hover:-translate-y-0.5 gap-2 px-4 py-2 rounded-lg font-medium text-sm transition-all duration-200',
                notifications.length === 0
                  ? 'bg-gray-200 text-gray-500 cursor-not-allowed'
                  : 'bg-orange-100 text-orange-700 hover:bg-orange-200'
              ]" :disabled="notifications.length === 0" title="Eliminar todas las notificaciones">
                <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                    d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                </svg>
                <span>Eliminar registro</span>
              </button>

              <button @click="toggleMute" :class="[
                'flex items-center hover:-translate-y-0.5 gap-2 px-4 py-2 rounded-lg font-medium text-sm transition-all duration-200',
                isMuted
                  ? 'bg-gray-200 text-gray-700 hover:bg-gray-300'
                  : 'bg-red-100 text-red-700 hover:bg-red-200'
              ]" :title="isMuted ? 'Activar alertas de notificaciones' : 'Silenciar alertas de notificaciones'">
                <svg v-if="!isMuted" class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                    d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
                </svg>
                <svg v-else class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                    d="M5.586 15H4a1 1 0 01-.707-1.707l1.586-1.586a1 1 0 01.707-.293h3.172a1 1 0 00.707-.293l2.414-2.414A1 1 0 0113.586 8H15m5.586 7H22a1 1 0 00.707-1.707l-1.586-1.586a1 1 0 00-.707-.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 0110.414 8H9M3 3l18 18" />
                </svg>
                <span>{{ isMuted ? 'Alertas silenciadas' : 'Silenciar alertas' }}</span>
              </button>

              <button class="text-sm flex items-center hover:-translate-y-0.5 text-blue-600 hover:text-blue-800 font-medium transition"
                @click="loadNotifications">               
                <ArrowPathIcon class="ml-2 mr-2 w-6 h-6 text-blue-500" />
                <span>Recargar</span>
              </button>
            </div>
          </div>

          <div v-if="loading" class="p-6 text-gray-500 text-center">
            <div class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
            <p class="mt-2">Cargando notificaciones...</p>
          </div>

          <div v-else-if="!notifications.length" class="p-8 text-center text-gray-500">
            <svg class="mx-auto h-12 w-12 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4" />
            </svg>
            <p class="mt-2">No hay notificaciones disponibles.</p>
          </div>

          <ul v-else class="divide-y divide-blue-100">
            <li v-for="notification in notifications" :key="notification.id" :class="[
              'px-6 py-4 transition-colors duration-200',
              notification.silenciada ? 'bg-gray-50 opacity-60' : 'cursor-pointer hover:bg-blue-50'
            ]" @click="!notification.silenciada && openNotification(notification.id)">
              <div class="flex items-start justify-between gap-4">
                <div class="flex-1 min-w-0">
                  <div class="flex items-center gap-2 mb-1">
                    <p class="text-xs text-gray-500">
                      {{ new Date(notification.fechaEnvio).toLocaleString('es-ES', {
                        year: 'numeric',
                        month: 'long',
                        day: 'numeric',
                        hour: '2-digit',
                        minute: '2-digit'
                      }) }}
                    </p>
                    <span v-if="notification.silenciada"
                      class="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-medium bg-gray-200 text-gray-700">
                      <svg class="w-3 h-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                          d="M5.586 15H4a1 1 0 01-.707-1.707l1.586-1.586a1 1 0 01.707-.293h3.172a1 1 0 00.707-.293l2.414-2.414A1 1 0 0113.586 8H15m5.586 7H22a1 1 0 00.707-1.707l-1.586-1.586a1 1 0 00-.707-.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 0110.414 8H9M3 3l18 18" />
                      </svg>
                      Silenciada
                    </span>
                    <span v-if="notification.recordatorioActivo"
                      class="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-medium bg-yellow-100 text-yellow-700">
                      <svg class="w-3 h-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                          d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                      </svg>
                      Recordatorio activo
                    </span>
                  </div>
                  <h3 :class="[
                    'text-lg font-semibold mb-1',
                    notification.silenciada ? 'text-gray-500' : 'text-gray-800'
                  ]">
                    {{ notification.asunto }}
                  </h3>
                  <p :class="[
                    'text-sm',
                    notification.silenciada ? 'text-gray-400' : 'text-gray-600'
                  ]">
                    <span class="font-medium">Remitente:</span> {{ notification.remitente }}
                  </p>
                </div>
                <div class="flex items-center gap-2 shrink-0">
                  <button v-if="!notification.silenciada" @click="openReminderModal(notification.id, $event)"
                    :class="[
                      'p-2 rounded-lg transition-all duration-200',
                      notification.recordatorioActivo
                        ? 'hover:bg-yellow-50 text-yellow-600'
                        : 'hover:bg-blue-50 text-gray-500 hover:text-blue-600'
                    ]" :title="notification.recordatorioActivo ? 'Editar recordatorio' : 'Programar notificación'">
                    <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                        d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                    </svg>
                  </button>

                  <button v-if="notification.recordatorioActivo" @click="handleDeleteReminder(notification.id, $event)"
                    :class="[
                      'p-2 rounded-lg transition-all duration-200',
                      'hover:bg-red-50 text-yellow-600 hover:text-red-600'
                    ]" :title="'Eliminar recordatorio'">
                    <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                        d="M6 18L18 6M6 6l12 12" />
                    </svg>
                  </button>

                  <button v-if="!notification.silenciada" @click="handleMuteNotification(notification.id, $event)"
                    :disabled="mutingNotificationId === notification.id" :class="[
                      'p-2 rounded-lg transition-all duration-200',
                      mutingNotificationId === notification.id
                        ? 'bg-gray-200 cursor-not-allowed'
                        : 'hover:bg-red-50 text-gray-500 hover:text-red-600'
                    ]" :title="mutingNotificationId === notification.id ? 'Silenciando...' : 'Silenciar notificación'">
                    <svg v-if="mutingNotificationId === notification.id" class="w-5 h-5 animate-spin" fill="none"
                      viewBox="0 0 24 24">
                      <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                      <path class="opacity-75" fill="currentColor"
                        d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z">
                      </path>
                    </svg>
                    <svg v-else class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                        d="M5.586 15H4a1 1 0 01-.707-1.707l1.586-1.586a1 1 0 01.707-.293h3.172a1 1 0 00.707-.293l2.414-2.414A1 1 0 0113.586 8H15m5.586 7H22a1 1 0 00.707-1.707l-1.586-1.586a1 1 0 00-.707-.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 0110.414 8H9M3 3l18 18" />
                    </svg>
                  </button>

                  <button v-if="notification.silenciada" @click="handleUnmuteNotification(notification.id, $event)"
                    :disabled="unmutingNotificationId === notification.id" :class="[
                      'p-2 rounded-lg transition-all duration-200',
                      unmutingNotificationId === notification.id
                        ? 'bg-gray-200 cursor-not-allowed'
                        : 'hover:bg-green-50 text-gray-500 hover:text-green-600'
                    ]" :title="unmutingNotificationId === notification.id ? 'Activando...' : 'Activar notificación'">
                    <svg v-if="unmutingNotificationId === notification.id" class="w-5 h-5 animate-spin" fill="none"
                      viewBox="0 0 24 24">
                      <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                      <path class="opacity-75" fill="currentColor"
                        d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z">
                      </path>
                    </svg>
                    <svg v-else class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                        d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
                    </svg>
                  </button>

                  <svg v-if="!notification.silenciada" class="w-6 h-6 text-blue-600" fill="none" viewBox="0 0 24 24"
                    stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
                  </svg>
                </div>
              </div>
            </li>
          </ul>

          <p v-if="errorMessage" class="p-6 text-sm text-red-600 bg-red-50 border-t border-red-200">
            ⚠️ {{ errorMessage }}
          </p>
        </article>
      </section>

      <!-- VISTA DETALLADA DE NOTIFICACIÓN -->
      <section v-else class="max-w-4xl mx-auto space-y-6">
        <div>
          <button @click="backToList"
            class="inline-flex items-center gap-2 px-4 py-2 text-blue-700 hover:text-blue-900 hover:bg-blue-50 rounded-lg transition-colors duration-200 font-medium">
            <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
            </svg>
            Volver a notificaciones
          </button>
        </div>

        <div v-if="loadingDetails" class="bg-white rounded-lg shadow border border-blue-200 p-12 text-center">
          <div class="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
          <p class="mt-4 text-gray-600">Cargando notificación...</p>
        </div>

        <article v-else-if="selectedNotification"
          class="bg-white rounded-lg shadow border border-blue-200 overflow-hidden">
          <header class="bg-linear-to-r from-blue-50 to-blue-100 px-8 py-6 border-b border-blue-200">
            <div class="flex items-start gap-4">
              <div class="shrink-0 w-12 h-12 bg-blue-600 rounded-full flex items-center justify-center">
                <svg class="w-6 h-6 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                    d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
                </svg>
              </div>
              <div class="flex-1">
                <h1 class="text-2xl font-bold text-gray-900 mb-2">
                  {{ selectedNotification.asunto }}
                </h1>
                <div class="flex flex-wrap gap-4 text-sm text-gray-600">
                  <div class="flex items-center gap-1">
                    <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                        d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                    </svg>
                    <span><strong>De:</strong> {{ selectedNotification.remitente }}</span>
                  </div>
                  <div class="flex items-center gap-1">
                    <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                        d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                    </svg>
                    <span>{{ new Date(selectedNotification.fechaEnvio).toLocaleString('es-ES', {
                      year: 'numeric',
                      month: 'long',
                      day: 'numeric',
                      hour: '2-digit',
                      minute: '2-digit'
                    }) }}</span>
                  </div>
                </div>
              </div>
            </div>
          </header>

          <div class="px-8 py-8">
            <div class="prose max-w-none">
              <div class="bg-gray-50 rounded-lg p-6 border border-gray-200">
                <p class="text-gray-800 whitespace-pre-line leading-relaxed text-base">
                  {{ selectedNotification.mensaje }}
                </p>
              </div>
            </div>
          </div>

          <div v-if="selectedNotification.recordatorioActivo" class="bg-yellow-50 border-t border-yellow-200 px-8 py-4">
            <p class="text-sm text-yellow-700">
              <strong>Recordatorio programado:</strong> {{ new Date(selectedNotification.fechaRecordatorio).toLocaleString('es-ES', {
                year: 'numeric',
                month: 'long',
                day: 'numeric',
                hour: '2-digit',
                minute: '2-digit'
              }) }}
            </p>
          </div>

          <footer class="bg-gray-50 px-8 py-4 border-t border-gray-200 flex justify-between items-center">
            <p class="text-sm text-gray-500">
              ID: {{ selectedNotification.id }}
            </p>
            <div class="flex gap-2">
              <button v-if="!selectedNotification.silenciada" @click="openReminderModal(selectedNotification.id, $event)"
                :class="[
                  'px-4 py-2 rounded-lg font-medium text-sm transition-colors duration-200 flex items-center gap-2',
                  selectedNotification.recordatorioActivo
                    ? 'bg-yellow-100 text-yellow-700 hover:bg-yellow-200'
                    : 'bg-blue-100 text-blue-700 hover:bg-blue-200'
                ]">
                <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                    d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                {{ selectedNotification.recordatorioActivo ? 'Editar recordatorio' : 'Programar notificación' }}
              </button>
              <button @click="backToList"
                class="px-6 py-2 bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-lg transition-colors duration-200">
                Cerrar
              </button>
            </div>
          </footer>
        </article>

        <p v-if="errorMessage" class="p-6 text-sm text-red-600 bg-red-50 rounded-lg border border-red-200">
          ⚠️ {{ errorMessage }}
        </p>
      </section>

      <!-- MODAL DE CONFIRMACIÓN PARA ELIMINAR TODAS LAS NOTIFICACIONES -->
      <div v-if="showDeleteConfirmation" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
        <div class="bg-white rounded-lg shadow-lg p-6 max-w-sm w-full mx-4">
          <div class="flex items-center gap-4 mb-4">
            <div class="flex-shrink-0 w-12 h-12 bg-orange-100 rounded-full flex items-center justify-center">
              <svg class="w-6 h-6 text-orange-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M12 9v2m0 4v2m0 4v2m0-12.5V3m0 18v0M3 12a9 9 0 1118 0 9 9 0 01-18 0z" />
              </svg>
            </div>
            <div>
              <h3 class="text-lg font-bold text-gray-900">Eliminar todas las notificaciones</h3>
              <p class="text-sm text-gray-600 mt-1">Esta acción no se puede deshacer</p>
            </div>
          </div>

          <p class="text-gray-700 mb-6">
            ¿Estás seguro de que deseas eliminar todas tus notificaciones? Esta acción eliminará permanentemente
            <strong>{{ notifications.length }}</strong> notificación{{ notifications.length !== 1 ? 'es' : '' }}.
          </p>

          <div class="flex gap-3">
            <button @click="closeDeleteConfirmation"
              :disabled="deletingAllNotifications"
              class="flex-1 px-4 py-2 border border-gray-300 rounded-lg text-gray-700 font-medium hover:bg-gray-50 transition-colors disabled:opacity-50 disabled:cursor-not-allowed">
              Cancelar
            </button>
            <button @click="handleDeleteAllNotifications"
              :disabled="deletingAllNotifications"
              class="flex-1 px-4 py-2 bg-red-600 text-white rounded-lg font-medium hover:bg-red-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2">
              <svg v-if="deletingAllNotifications" class="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path class="opacity-75" fill="currentColor"
                  d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z">
                </path>
              </svg>
              <span>{{ deletingAllNotifications ? 'Eliminando...' : 'Eliminar todo' }}</span>
            </button>
          </div>
        </div>
      </div>

      <!-- MODAL DE RECORDATORIO/ALARMA -->
      <div v-if="showReminderModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
        <div class="bg-white rounded-lg shadow-lg p-8 max-w-md w-full mx-4">
          <div class="flex items-center gap-3 mb-6">
            <div class="flex-shrink-0 w-10 h-10 bg-blue-100 rounded-full flex items-center justify-center">
              <svg class="w-6 h-6 text-blue-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
            </div>
            <h3 class="text-lg font-bold text-gray-900">Programar notificación</h3>
          </div>

          <p class="text-gray-600 mb-6">
            Selecciona la fecha y hora en la que deseas que la notificación reaparezca como una alerta.
          </p>

          <div class="mb-6">
            <label class="block text-sm font-medium text-gray-700 mb-2">Fecha y hora</label>
            <input v-model="reminderDateTime" type="datetime-local" 
              class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent">
          </div>

          <div v-if="errorMessage" class="mb-4 p-3 bg-red-50 border border-red-200 rounded-lg">
            <p class="text-sm text-red-600">{{ errorMessage }}</p>
          </div>

          <div class="flex gap-3">
            <button @click="closeReminderModal"
              :disabled="settingReminder"
              class="flex-1 px-4 py-2 border border-gray-300 rounded-lg text-gray-700 font-medium hover:bg-gray-50 transition-colors disabled:opacity-50 disabled:cursor-not-allowed">
              Cancelar
            </button>
            <button @click="handleSetReminder"
              :disabled="settingReminder"
              class="flex-1 px-4 py-2 bg-blue-600 text-white rounded-lg font-medium hover:bg-blue-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2">
              <svg v-if="settingReminder" class="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path class="opacity-75" fill="currentColor"
                  d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z">
                </path>
              </svg>
              <span>{{ settingReminder ? 'Guardando...' : 'Guardar recordatorio' }}</span>
            </button>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>
