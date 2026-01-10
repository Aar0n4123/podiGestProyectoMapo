<script lang="ts">
import { defineComponent } from 'vue'
import SideBar from '../components/SideBar.vue'
import { obtenerCitas, cancelarCitaPaciente, type Cita } from '../services/appointmentService'

export default defineComponent({
  name: 'CancelarCitas',
  components: {
    SideBar
  },
  data() {
    return {
      usuarioAutenticado: false,
      isCollapsed: false,
      citasDisponibles: [] as Cita[],
      cargando: false,
      usuarioCorreo: '',
      usuarioNombre: '',
      errorMessage: '',
      successMessage: '',
      citaEnProceso: '',
      mostrarConfirmacion: false,
      citaAConfirmar: null as Cita | null
    }
  },
  async mounted() {
    this.verificarSesion()
    if (this.usuarioAutenticado) {
      await this.cargarCitas()
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
    async cargarCitas() {
      this.cargando = true
      this.errorMessage = ''
      try {
        console.log('Cargando citas para:', this.usuarioCorreo)
        const todasLasCitas = await obtenerCitas()
        console.log('Total de citas obtenidas:', todasLasCitas.length)

        const citasFiltradas = todasLasCitas.filter(cita => {
          const esDelUsuario = cita.pacienteCorreo === this.usuarioCorreo
          const noEsCancelada = cita.estado !== 'cancelada'
          console.log(
            'Verificando cita:',
            cita.id,
            '| pacienteCorreo:',
            cita.pacienteCorreo,
            '| usuarioCorreo:',
            this.usuarioCorreo,
            '| estado:',
            cita.estado,
            '| válida:',
            esDelUsuario && noEsCancelada
          )
          return esDelUsuario && noEsCancelada
        })

        console.log('Citas disponibles para cancelar:', citasFiltradas.length)

        this.citasDisponibles = citasFiltradas.sort((a, b) => {
          const fechaA = new Date(a.fecha)
          const fechaB = new Date(b.fecha)
          return fechaB.getTime() - fechaA.getTime()
        })
      } catch (error) {
        console.error('Error al cargar citas:', error)
        this.errorMessage = 'Error al cargar las citas disponibles para cancelar'
      } finally {
        this.cargando = false
      }
    },
    abrirConfirmacion(cita: Cita) {
      this.citaAConfirmar = cita
      this.mostrarConfirmacion = true
    },
    cerrarConfirmacion() {
      this.mostrarConfirmacion = false
      this.citaAConfirmar = null
    },
    async confirmarCancelacion() {
      if (!this.citaAConfirmar) return

      this.citaEnProceso = this.citaAConfirmar.id
      this.errorMessage = ''
      this.successMessage = ''

      try {
        const resultado = await cancelarCitaPaciente(this.citaAConfirmar.id)

        if (resultado.success) {
          this.successMessage = 'Cita cancelada exitosamente'

          this.citasDisponibles = this.citasDisponibles.filter(
            cita => cita.id !== this.citaAConfirmar!.id
          )

          setTimeout(() => {
            this.successMessage = ''
          }, 5000)
        } else {
          this.errorMessage = resultado.message
        }
      } catch (error) {
        console.error('Error al cancelar cita:', error)
        this.errorMessage = 'Error inesperado al cancelar la cita'
      } finally {
        this.citaEnProceso = ''
        this.cerrarConfirmacion()
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
        <p class="mb-3">Debes iniciar sesión para cancelar tus citas.</p>
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
            <h1 class="text-4xl font-bold text-blue-500 mb-2">Cancelar Citas</h1>
            <p class="text-gray-600">Cancela tus citas agendadas fácilmente</p>
          </div>

          <div v-if="errorMessage" class="bg-red-100 text-red-700 border border-red-300 p-4 rounded mb-5">
            {{ errorMessage }}
          </div>

          <div v-if="successMessage" class="bg-green-100 text-green-700 border border-green-300 p-4 rounded mb-5">
            {{ successMessage }}
          </div>

          <div v-if="cargando" class="flex flex-col items-center justify-center p-10">
            <div class="w-16 h-16 border-4 border-blue-300 border-t-blue-600 rounded-full animate-spin"></div>
            <span class="mt-4 text-gray-500 text-lg">Cargando tus citas...</span>
          </div>

          <div v-else-if="citasDisponibles.length === 0" class="text-center p-8">
            <div class="text-6xl mb-4">✅</div>
            <h3 class="text-2xl font-semibold text-gray-800 mb-2">Sin Citas para Cancelar</h3>
            <p class="text-gray-600 mb-6">No tienes citas activas para cancelar.</p>
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
                Citas disponibles para cancelar: <span class="text-blue-500">{{ citasDisponibles.length }}</span>
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
                v-for="cita in citasDisponibles"
                :key="cita.id"
                class="bg-gradient-to-r from-blue-50 to-indigo-50 border-2 border-blue-200 rounded-lg p-6 hover:shadow-lg transition"
              >
                <div class="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
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
                    <span class="text-sm text-gray-600 font-semibold">Especialidad</span>
                    <span class="text-lg text-gray-800 font-bold">{{ cita.especialidadBuscada }}</span>
                  </div>
                </div>

                <div v-if="cita.razonConsulta" class="mb-4 pt-4 border-t border-blue-200">
                  <span class="text-sm text-gray-600 font-semibold">Razón de la Consulta</span>
                  <p class="text-gray-700 mt-1">{{ cita.razonConsulta }}</p>
                </div>

                <div class="flex gap-3 pt-4 border-t border-blue-200">
                  <button
                    @click="abrirConfirmacion(cita)"
                    :disabled="citaEnProceso === cita.id"
                    class="flex-1 bg-red-500 hover:bg-red-600 text-white font-semibold py-2 px-4 rounded transition disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    {{ citaEnProceso === cita.id ? 'Cancelando...' : 'Cancelar Cita' }}
                  </button>
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

    <div v-if="mostrarConfirmacion" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
      <div class="bg-white rounded-lg shadow-xl max-w-md mx-4 p-6">
        <h3 class="text-2xl font-bold text-gray-800 mb-4">Confirmar Cancelación</h3>

        <div v-if="citaAConfirmar" class="bg-gray-100 rounded-lg p-4 mb-6">
          <div class="grid gap-2 text-sm">
            <div>
              <span class="font-semibold text-gray-700">Especialista:</span>
              <span class="text-gray-600 ml-2">{{ citaAConfirmar.especialista }}</span>
            </div>
            <div>
              <span class="font-semibold text-gray-700">Fecha:</span>
              <span class="text-gray-600 ml-2">{{ formatearFecha(citaAConfirmar.fecha) }}</span>
            </div>
            <div>
              <span class="font-semibold text-gray-700">Hora:</span>
              <span class="text-gray-600 ml-2">{{ citaAConfirmar.hora }}</span>
            </div>
          </div>
        </div>

        <p class="text-gray-600 mb-6">¿Está seguro de que desea cancelar esta cita? Esta acción no se puede deshacer.</p>

        <div class="flex gap-3">
          <button
            @click="cerrarConfirmacion"
            class="flex-1 bg-gray-400 hover:bg-gray-500 text-white font-semibold py-2 px-4 rounded transition"
          >
            Cancelar
          </button>
          <button
            @click="confirmarCancelacion"
            class="flex-1 bg-red-500 hover:bg-red-600 text-white font-semibold py-2 px-4 rounded transition"
          >
            Confirmar Cancelación
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
