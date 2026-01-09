<script lang="ts">
import { defineComponent } from 'vue'
import SideBar from '../components/SideBar.vue'
import { obtenerCitas, type Cita } from '../services/appointmentService'

export default defineComponent({
  name: 'HistorialCitas',
  components: {
    SideBar
  },
  data() {
    return {
      usuarioAutenticado: false,
      isCollapsed: false,
      citasHistorial: [] as Cita[],
      cargando: false,
      usuarioCorreo: '',
      usuarioNombre: '',
      errorMessage: ''
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
          this.usuarioAutenticado = true
          this.usuarioCorreo = usuario.correoElectronico || usuario.pacienteCorreo || ''
          this.usuarioNombre = usuario.nombre || ''
          console.log('Usuario autenticado:', this.usuarioCorreo)
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
        console.log('Cargando historial para:', this.usuarioCorreo)
        const todasLasCitas = await obtenerCitas()
        console.log('Total de citas obtenidas:', todasLasCitas.length)

        const citasFiltradas = todasLasCitas.filter(cita => {
          const esDelUsuario = cita.pacienteCorreo === this.usuarioCorreo
          console.log('Verificando cita:', cita.id, '| pacienteCorreo:', cita.pacienteCorreo, '| usuarioCorreo:', this.usuarioCorreo, '| coincide:', esDelUsuario)
          return esDelUsuario
        })

        console.log('Citas del usuario encontradas:', citasFiltradas.length)
        
        if (citasFiltradas.length > 0) {
          console.log('Primera cita:', citasFiltradas[0])
        }

        this.citasHistorial = citasFiltradas.sort((a, b) => {
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
        <p class="mb-3">Debes iniciar sesión para ver tu historial de citas.</p>
        <router-link
          to="/login"
          class="inline-block mt-3 px-4 py-2 rounded bg-blue-600 text-white font-semibold text-sm hover:bg-blue-700 transition"
        >
          Ir al Login
        </router-link>
      </div>

      <div v-else class="w-full max-w-4xl mx-auto">
        <div class="bg-white w-full p-8 rounded-2xl shadow-xl">
          <div class="text-center mb-8">
            <h1 class="text-4xl font-bold text-blue-500 mb-2">Mis Citas</h1>
            <p class="text-gray-600">Visualiza todas tus citas agendadas</p>
          </div>

          <div v-if="errorMessage" class="bg-red-100 text-red-700 border border-red-300 p-4 rounded mb-5">
            {{ errorMessage }}
          </div>

          <div v-if="cargando" class="flex flex-col items-center justify-center p-10">
            <div class="w-16 h-16 border-4 border-blue-300 border-t-blue-600 rounded-full animate-spin"></div>
            <span class="mt-4 text-gray-500 text-lg">Cargando historial...</span>
          </div>

          <div v-else-if="citasHistorial.length === 0" class="text-center p-8">
            <div class="text-6xl mb-4">📋</div>
            <h3 class="text-2xl font-semibold text-gray-800 mb-2">Sin Citas Agendadas</h3>
            <p class="text-gray-600 mb-6">Aún no tienes citas agendadas. ¡Agenda una ahora!</p>
            <router-link
              to="/mis-citas"
              class="inline-block px-6 py-2 bg-blue-600 text-white font-semibold rounded-lg hover:bg-blue-700 transition"
            >
              Volver al Menú
            </router-link>
          </div>

          <div v-else class="space-y-4">
            <div class="flex items-center justify-between mb-6">
              <h2 class="text-xl font-semibold text-gray-800">
                Total de citas agendadas: <span class="text-blue-500">{{ citasHistorial.length }}</span>
              </h2>
              <router-link
                to="/mis-citas"
                class="px-4 py-2 bg-gray-600 text-white font-semibold rounded-lg hover:bg-gray-700 transition"
              >
                ← Volver
              </router-link>
            </div>

            <div class="grid gap-4">
              <div
                v-for="cita in citasHistorial"
                :key="cita.id"
                class="bg-gradient-to-r from-blue-50 to-indigo-50 border-2 border-blue-200 rounded-lg p-6 hover:shadow-lg transition"
              >
                <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div class="flex flex-col">
                    <span class="text-sm text-gray-600 font-semibold">Especialista</span>
                    <span class="text-lg text-gray-800 font-bold">{{ cita.especialista }}</span>
                  </div>

                  <div class="flex flex-col">
                    <span class="text-sm text-gray-600 font-semibold">Fecha</span>
                    <span class="text-lg text-gray-800 font-bold">{{ formatearFecha(cita.fecha) }}</span>
                  </div>

                  <div class="flex flex-col">
                    <span class="text-sm text-gray-600 font-semibold">Hora</span>
                    <span class="text-lg text-gray-800 font-bold">{{ cita.hora }}</span>
                  </div>

                  <div class="flex flex-col">
                    <span class="text-sm text-gray-600 font-semibold">Estado</span>
                    <span 
                      :class="[
                        'text-lg font-bold px-3 py-1 rounded-full w-fit',
                        cita.estado === 'completada' 
                          ? 'bg-green-100 text-green-700'
                          : cita.estado === 'cancelada'
                          ? 'bg-red-100 text-red-700'
                          : 'bg-gray-100 text-gray-700'
                      ]"
                    >
                      {{ cita.estado.charAt(0).toUpperCase() + cita.estado.slice(1) }}
                    </span>
                  </div>
                </div>

                <div v-if="cita.razonConsulta" class="mt-4 pt-4 border-t border-blue-200">
                  <span class="text-sm text-gray-600 font-semibold">Razón de la Consulta</span>
                  <p class="text-gray-700 mt-1">{{ cita.razonConsulta }}</p>
                </div>

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
