<script setup lang="ts">
import { onMounted, ref } from 'vue'
import SideBar from '../components/SideBar.vue'
import {
  fetchNotificationById,
  fetchNotifications,
  type NotificationSummary
} from '../services/notificationsService'

const isCollapsed = ref(false)
const notifications = ref<NotificationSummary[]>([])
const loading = ref(true)
const errorMessage = ref('')
const selectedNotification = ref<NotificationSummary | null>(null)
const loadingDetails = ref(false)
const showDetailView = ref(false)

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

onMounted(() => {
  loadNotifications()
})
</script>

<template>
  <div class="flex">
    <SideBar :is-collapsed="isCollapsed" @toggle="toggleSidebar" />
    <main :class="[
      'transition-all duration-300 p-6 overflow-auto bg-amber-150 min-h-screen w-full',
      isCollapsed ? 'ml-20' : 'ml-64'
    ]">
      <!-- VISTA DE LISTA DE NOTIFICACIONES -->
      <section v-if="!showDetailView" class="max-w-6xl mx-auto space-y-6">
        <header class="bg-white shadow-sm rounded-lg p-6 border border-amber-200">
          <h1 class="text-3xl font-bold text-amber-700 mb-2">Notificaciones</h1>
          <p class="text-gray-600">
            Consulta los recordatorios y avisos importantes enviados por la institución.
          </p>
        </header>

        <article class="bg-white rounded-lg shadow border border-amber-200">
          <div class="flex items-center justify-between px-6 py-4 border-b border-amber-100">
            <h2 class="text-xl font-semibold text-amber-700">Bandeja de entrada</h2>
            <button
              class="text-sm text-amber-600 hover:text-amber-800 font-medium transition"
              @click="loadNotifications"
            >
              🔄 Recargar
            </button>
          </div>

          <div v-if="loading" class="p-6 text-gray-500 text-center">
            <div class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-amber-600"></div>
            <p class="mt-2">Cargando notificaciones...</p>
          </div>

          <div v-else-if="!notifications.length" class="p-8 text-center text-gray-500">
            <svg class="mx-auto h-12 w-12 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4" />
            </svg>
            <p class="mt-2">No hay notificaciones disponibles.</p>
          </div>

          <ul v-else class="divide-y divide-amber-100">
            <li
              v-for="notification in notifications"
              :key="notification.id"
              class="px-6 py-4 cursor-pointer hover:bg-amber-50 transition-colors duration-200"
              @click="openNotification(notification.id)"
            >
              <div class="flex items-start justify-between">
                <div class="flex-1">
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
                  </div>
                  <h3 class="text-lg font-semibold text-gray-800 mb-1">
                    {{ notification.asunto }}
                  </h3>
                  <p class="text-sm text-gray-600">
                    <span class="font-medium">Remitente:</span> {{ notification.remitente }}
                  </p>
                </div>
                <div class="ml-4">
                  <svg
                    class="w-6 h-6 text-amber-600"
                    fill="none"
                    viewBox="0 0 24 24"
                    stroke="currentColor"
                  >
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
          <button
            @click="backToList"
            class="inline-flex items-center gap-2 px-4 py-2 text-amber-700 hover:text-amber-900 hover:bg-amber-50 rounded-lg transition-colors duration-200 font-medium"
          >
            <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
            </svg>
            Volver a notificaciones
          </button>
        </div>

        <!-- Contenido de la notificación -->
        <div v-if="loadingDetails" class="bg-white rounded-lg shadow border border-amber-200 p-12 text-center">
          <div class="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-amber-600"></div>
          <p class="mt-4 text-gray-600">Cargando notificación...</p>
        </div>

        <article v-else-if="selectedNotification" class="bg-white rounded-lg shadow border border-amber-200 overflow-hidden">
          <!-- Encabezado de la notificación -->
          <header class="bg-gradient-to-r from-amber-50 to-amber-100 px-8 py-6 border-b border-amber-200">
            <div class="flex items-start gap-4">
              <div class="flex-shrink-0 w-12 h-12 bg-amber-600 rounded-full flex items-center justify-center">
                <svg class="w-6 h-6 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
                </svg>
              </div>
              <div class="flex-1">
                <h1 class="text-2xl font-bold text-gray-900 mb-2">
                  {{ selectedNotification.asunto }}
                </h1>
                <div class="flex flex-wrap gap-4 text-sm text-gray-600">
                  <div class="flex items-center gap-1">
                    <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                    </svg>
                    <span><strong>De:</strong> {{ selectedNotification.remitente }}</span>
                  </div>
                  <div class="flex items-center gap-1">
                    <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
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
            <button
              @click="backToList"
              class="px-6 py-2 bg-amber-600 hover:bg-amber-700 text-white font-medium rounded-lg transition-colors duration-200"
            >
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