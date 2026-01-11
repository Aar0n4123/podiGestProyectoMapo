<script lang="ts">
import { defineComponent } from 'vue'
import SideBar from '../components/SideBar.vue'
import { obtenerCitas, type Cita } from '../services/appointmentService'

export default defineComponent({
  name: 'HistorialCitasEspecialista',
  components: {
    SideBar
  },
  data() {
    return {
      usuarioAutenticado: false,
      isCollapsed: false,
      citasHistorial: [] as Cita[],
      cargando: false,
      usuarioNombre: '',
      usuarioApellido: '',
      usuarioRol: '',
      errorMessage: '',
      filtroEstado: 'todas' as string
    }
  },
  computed: {
    citasFiltradas(): Cita[] {
      if (this.filtroEstado === 'todas') {
        return this.citasHistorial
      }
      return this.citasHistorial.filter(cita => cita.estado === this.filtroEstado)
    },
    estadisticas() {
      return {
        total: this.citasHistorial.length,
        completadas: this.citasHistorial.filter(c => c.estado === 'completada').length,
        pendientes: this.citasHistorial.filter(c => c.estado === 'pendiente').length,
        canceladas: this.citasHistorial.filter(c => c.estado === 'cancelada').length
      }
    }
  },
  async mounted() {
    this.verificarSesion()
    if (this.usuarioAutenticado) {
      await this.cargarHistorial()
    }
  },
  methods: {
    verificarSesion() {
      try {
        const usuarioJSON = localStorage.getItem('currentUser')
        if (usuarioJSON) {
          const usuario = JSON.parse(usuarioJSON)
          this.usuarioAutenticado = usuario.rol && usuario.rol.toLowerCase() === 'especialista'
          this.usuarioNombre = usuario.nombre || ''
          this.usuarioApellido = usuario.apellido || ''
          this.usuarioRol = usuario.rol || ''
          console.log('Especialista autenticado:', this.usuarioNombre, this.usuarioApellido)
        } else {
          this.usuarioAutenticado = false
          console.log('No hay usuario en localStorage')
        }
      } catch (error) {
        console.error('Error al verificar sesión:', error)
        this.usuarioAutenticado = false
      }
    },
    async cargarHistorial() {
      this.cargando = true
      this.errorMessage = ''
      try {
        console.log('Cargando historial de citas para especialista:', this.usuarioNombre)
        
        const response = await fetch('http://localhost:8080/api/citas/propias', {
          method: 'GET',
          headers: {
            'Content-Type': 'application/json'
          },
          cache: 'no-store'
        })

        if (!response.ok) {
          if (response.status === 401) {
            this.errorMessage = 'Sesión expirada. Por favor, inicia sesión nuevamente.'
          } else if (response.status === 403) {
            this.errorMessage = 'No tienes permiso para ver el historial de citas.'
          } else {
            this.errorMessage = `Error ${response.status}: No se pudieron cargar las citas`
          }
          console.error('Error en respuesta:', response.status)
          return
        }

        const citas = await response.json()
        console.log('Citas cargadas:', citas.length)

        this.citasHistorial = citas.sort((a, b) => {
          const fechaA = new Date(a.fecha)
          const fechaB = new Date(b.fecha)
          return fechaB.getTime() - fechaA.getTime()
        })
      } catch (error) {
        console.error('Error al cargar historial:', error)
        this.errorMessage = 'Error al cargar el historial de citas'
      } finally {
        this.cargando = false
      }
    },
    formatearFecha(fecha: string): string {
      try {
        const date = new Date(fecha + 'T00:00:00')
        return date.toLocaleDateString('es-VE', {
          year: 'numeric',
          month: 'long',
          day: 'numeric'
        })
      } catch {
        return fecha
      }
    },
    toggleSidebar() {
      this.isCollapsed = !this.isCollapsed
    },
    obtenerColorEstado(estado: string): string {
      switch (estado) {
        case 'completada':
          return 'bg-green-100 text-green-700'
        case 'cancelada':
          return 'bg-red-100 text-red-700'
        case 'pendiente':
          return 'bg-yellow-100 text-yellow-700'
        default:
          return 'bg-gray-100 text-gray-700'
      }
    }
  }
})
</script>

<template>
  <div class="flex">
    <SideBar :is-collapsed="isCollapsed" @toggle="toggleSidebar" />

    <main
      :class="[
        'transition-all duration-300 p-6 overflow-auto min-h-screen',
        'bg-blue-200/50 rounded-lg shadow-lg mx-auto border border-blue-300',
        isCollapsed ? 'ml-20' : 'ml-64'
      ]"
    >
      <div v-if="!usuarioAutenticado" class="bg-red-100 text-red-700 border border-red-300 p-5 rounded-lg mb-5">
        <h3 class="text-xl font-bold mb-2">Acceso Denegado</h3>
        <p class="mb-3">Solo los especialistas pueden acceder a esta sección.</p>
        <router-link
          to="/login"
          class="inline-block mt-3 px-4 py-2 rounded bg-blue-600 text-white font-semibold text-sm hover:bg-blue-700 transition"
        >
          Ir al Login
        </router-link>
      </div>

      <div v-else class="w-full max-w-6xl mx-auto">
        <div class="bg-white w-full p-8 rounded-2xl shadow-xl">
          <!-- Encabezado -->
          <div class="text-center mb-8">
            <h1 class="text-4xl font-bold text-blue-500 mb-2">Historial de Citas</h1>
            <p class="text-gray-600">Visualiza todas tus citas agendadas como especialista</p>
          </div>

          <!-- Mensaje de error -->
          <div v-if="errorMessage" class="bg-red-100 text-red-700 border border-red-300 p-4 rounded mb-5">
            {{ errorMessage }}
          </div>

          <!-- Cargando -->
          <div v-if="cargando" class="flex flex-col items-center justify-center p-10">
            <div class="w-16 h-16 border-4 border-blue-300 border-t-blue-600 rounded-full animate-spin"></div>
            <span class="mt-4 text-gray-500 text-lg">Cargando historial...</span>
          </div>

          <!-- Sin citas -->
          <div v-else-if="citasHistorial.length === 0" class="text-center p-8">
            <div class="text-6xl mb-4">📋</div>
            <h3 class="text-2xl font-semibold text-gray-800 mb-2">Sin Citas Agendadas</h3>
            <p class="text-gray-600">Aún no tienes citas agendadas contigo.</p>
          </div>

          <!-- Citas disponibles -->
          <div v-else>
            <!-- Estadísticas -->
            <div class="grid grid-cols-1 md:grid-cols-4 gap-4 mb-8">
              <div class="bg-blue-50 border border-blue-200 rounded-lg p-4">
                <div class="text-2xl font-bold text-blue-600">{{ estadisticas.total }}</div>
                <div class="text-sm text-gray-600 mt-1">Total de Citas</div>
              </div>
              <div class="bg-yellow-50 border border-yellow-200 rounded-lg p-4">
                <div class="text-2xl font-bold text-yellow-600">{{ estadisticas.pendientes }}</div>
                <div class="text-sm text-gray-600 mt-1">Pendientes</div>
              </div>
              <div class="bg-green-50 border border-green-200 rounded-lg p-4">
                <div class="text-2xl font-bold text-green-600">{{ estadisticas.completadas }}</div>
                <div class="text-sm text-gray-600 mt-1">Completadas</div>
              </div>
              <div class="bg-red-50 border border-red-200 rounded-lg p-4">
                <div class="text-2xl font-bold text-red-600">{{ estadisticas.canceladas }}</div>
                <div class="text-sm text-gray-600 mt-1">Canceladas</div>
              </div>
            </div>

            <!-- Filtro de estado -->
            <div class="mb-6 flex items-center gap-4">
              <label class="text-sm font-semibold text-gray-700">Filtrar por estado:</label>
              <select
                v-model="filtroEstado"
                class="px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:border-blue-500"
              >
                <option value="todas">Todas las citas</option>
                <option value="pendiente">Pendientes</option>
                <option value="completada">Completadas</option>
                <option value="cancelada">Canceladas</option>
              </select>
            </div>

            <!-- Lista de citas -->
            <div class="space-y-4">
              <div v-if="citasFiltradas.length === 0" class="text-center p-8 text-gray-500">
                No hay citas con el estado seleccionado
              </div>
              <div
                v-for="cita in citasFiltradas"
                :key="cita.id"
                class="bg-gradient-to-r from-blue-50 to-indigo-50 border-2 border-blue-200 rounded-lg p-6 hover:shadow-lg transition"
              >
                <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
                  <!-- Información del Paciente -->
                  <div>
                    <div class="flex flex-col">
                      <span class="text-xs text-gray-600 font-semibold uppercase tracking-wide">Paciente</span>
                      <span class="text-lg text-gray-800 font-bold mt-1">{{ cita.pacienteNombre }}</span>
                      <span class="text-sm text-gray-500 mt-1">{{ cita.pacienteCorreo }}</span>
                      <span class="text-sm text-gray-500">{{ cita.pacienteTelefono }}</span>
                    </div>
                  </div>

                  <!-- Información de la Cita -->
                  <div>
                    <div class="flex flex-col space-y-3">
                      <div>
                        <span class="text-xs text-gray-600 font-semibold uppercase tracking-wide">Fecha</span>
                        <span class="text-lg text-gray-800 font-bold">{{ formatearFecha(cita.fecha) }}</span>
                      </div>
                      <div>
                        <span class="text-xs text-gray-600 font-semibold uppercase tracking-wide">Hora</span>
                        <span class="text-lg text-gray-800 font-bold">{{ cita.hora }}</span>
                      </div>
                    </div>
                  </div>

                  <!-- Estado y Especialidad -->
                  <div>
                    <div class="flex flex-col space-y-3">
                      <div>
                        <span class="text-xs text-gray-600 font-semibold uppercase tracking-wide">Estado</span>
                        <span
                          :class="[
                            'text-lg font-bold px-3 py-1 rounded-full w-fit mt-1 block',
                            obtenerColorEstado(cita.estado)
                          ]"
                        >
                          {{ cita.estado.charAt(0).toUpperCase() + cita.estado.slice(1) }}
                        </span>
                      </div>
                      <div v-if="cita.especialidadBuscada">
                        <span class="text-xs text-gray-600 font-semibold uppercase tracking-wide">Especialidad</span>
                        <span class="text-sm text-gray-800">{{ cita.especialidadBuscada }}</span>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- Razón de la Consulta -->
                <div v-if="cita.razonConsulta" class="mt-4 pt-4 border-t border-blue-200">
                  <span class="text-xs text-gray-600 font-semibold uppercase tracking-wide">Razón de la Consulta</span>
                  <p class="text-gray-700 mt-2">{{ cita.razonConsulta }}</p>
                </div>

                <!-- ID de la Cita -->
                <div class="mt-3 text-xs text-gray-500">
                  ID: {{ cita.id }}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>
