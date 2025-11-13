<script setup lang="ts">
import { onMounted, ref } from 'vue'
import SideBar from '../components/SideBar.vue'
import { ArrowPathIcon } from '@heroicons/vue/24/solid'
import {
  fetchNotificationById,
  fetchNotifications,
  muteNotification,
  unmuteNotification,
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
  // Actualizar el contador en el sidebar
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
  event.stopPropagation() // Evitar que se abra la notificación al hacer clic en silenciar

  mutingNotificationId.value = id
  errorMessage.value = ''

  try {
    const success = await muteNotification(id)

    if (success) {
      // Actualizar la notificación en la lista local
      const notification = notifications.value.find(n => n.id === id)
      if (notification) {
        notification.silenciada = true
      }

      // Decrementar el contador de notificaciones
      decrementCount()

      // Mostrar mensaje de éxito temporal
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
  event.stopPropagation() // Evitar que se abra la notificación al hacer clic en dessilenciar

  unmutingNotificationId.value = id
  errorMessage.value = ''

  try {
    const success = await unmuteNotification(id)

    if (success) {
      // Actualizar la notificación en la lista local
      const notification = notifications.value.find(n => n.id === id)
      if (notification) {
        notification.silenciada = false
      }

      // Incrementar el contador de notificaciones
      incrementCount()

      // Mostrar mensaje de éxito temporal
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

        <!-- Mensaje informativo cuando las alertas están silenciadas -->
        <div v-if="isMuted" class="bg-gray-100 border border-gray-300 rounded-lg p-4 flex items-center gap-3">
          <svg class="w-6 h-6 text-gray-600 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          <div class="flex-1">
            <p class="text-gray-800 font-medium">Las alertas de notificaciones están silenciadas</p>
            <p class="text-gray-600 text-sm">No verás el badge rojo en el icono de notificaciones hasta que las actives
              nuevamente.</p>
          </div>
        </div>

        <article class="bg-white rounded-lg shadow border border-blue-200">
          <h2 class="text-2xl font-bold text-blue-500 mt-4">Bandeja de Entrada</h2>
          <div class="flex items-center justify-end px-6 py-4 border-b border-blue-100">

            <div class="flex items-center gap-3">
              <!-- Botón de silenciar/activar alertas -->
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
                    d="M5.586 15H4a1 1 0 01-.707-1.707l1.586-1.586a1 1 0 01.707-.293h3.172a1 1 0 00.707-.293l2.414-2.414A1 1 0 0113.586 8H15m5.586 7H22a1 1 0 00.707-1.707l-1.586-1.586a1 1 0 00-.707-.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 0010.414 8H9M3 3l18 18" />
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
                          d="M5.586 15H4a1 1 0 01-.707-1.707l1.586-1.586a1 1 0 01.707-.293h3.172a1 1 0 00.707-.293l2.414-2.414A1 1 0 0113.586 8H15m5.586 7H22a1 1 0 00.707-1.707l-1.586-1.586a1 1 0 00-.707-.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 0010.414 8H9M3 3l18 18" />
                      </svg>
                      Silenciada
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
                  <!-- Botón de silenciar (solo si NO está silenciada) -->
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
                        d="M5.586 15H4a1 1 0 01-.707-1.707l1.586-1.586a1 1 0 01.707-.293h3.172a1 1 0 00.707-.293l2.414-2.414A1 1 0 0113.586 8H15m5.586 7H22a1 1 0 00.707-1.707l-1.586-1.586a1 1 0 00-.707-.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 0010.414 8H9M3 3l18 18" />
                    </svg>
                  </button>

                  <!-- Botón de dessilenciar (solo si ESTÁ silenciada) -->
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

                  <!-- Flecha para abrir (solo si no está silenciada) -->
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
        <!-- Botón de retroceso -->
        <div>
          <button @click="backToList"
            class="inline-flex items-center gap-2 px-4 py-2 text-blue-700 hover:text-blue-900 hover:bg-blue-50 rounded-lg transition-colors duration-200 font-medium">
            <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
            </svg>
            Volver a notificaciones
          </button>
        </div>

        <!-- Contenido de la notificación -->
        <div v-if="loadingDetails" class="bg-white rounded-lg shadow border border-blue-200 p-12 text-center">
          <div class="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
          <p class="mt-4 text-gray-600">Cargando notificación...</p>
        </div>

        <article v-else-if="selectedNotification"
          class="bg-white rounded-lg shadow border border-blue-200 overflow-hidden">
          <!-- Encabezado de la notificación -->
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

          <!-- Cuerpo del mensaje -->
          <div class="px-8 py-8">
            <div class="prose max-w-none">
              <div class="bg-gray-50 rounded-lg p-6 border border-gray-200">
                <p class="text-gray-800 whitespace-pre-line leading-relaxed text-base">
                  {{ selectedNotification.mensaje }}
                </p>
              </div>
            </div>
          </div>

          <!-- Footer con acciones -->
          <footer class="bg-gray-50 px-8 py-4 border-t border-gray-200 flex justify-between items-center">
            <p class="text-sm text-gray-500">
              ID de notificación: {{ selectedNotification.id }}
            </p>
            <button @click="backToList"
              class="px-6 py-2 bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-lg transition-colors duration-200">
              Cerrar
            </button>
          </footer>
        </article>

        <p v-if="errorMessage" class="p-6 text-sm text-red-600 bg-red-50 rounded-lg border border-red-200">
          ⚠️ {{ errorMessage }}
        </p>
      </section>
    </main>
  </div>
</template>