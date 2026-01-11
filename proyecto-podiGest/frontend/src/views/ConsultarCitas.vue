<script lang="ts">
import { defineComponent } from 'vue'
import SideBar from '../components/SideBar.vue'
import { getEspecialistas, obtenerCitas } from '../services/appointmentService'

interface Usuario {
  nombre: string
  apellido: string
  correoElectronico: string
}

interface CitaDisponible {
  especialista: string
  fecha: string
  horario: string
}

interface Criterios {
  especialista: string
  fecha: string
  horario: string
}

export default defineComponent({
  name: 'ConsultarCitas',
  components: {
    SideBar
  },
  data() {
    return {
      usuarioAutenticado: false,
      isCollapsed: false,
      especialistas: [] as Usuario[],
      horariosDisponibles: [] as string[],
      criterios: {
        especialista: '',
        fecha: '',
        horario: ''
      } as Criterios,
      citasDisponibles: [] as CitaDisponible[],
      buscando: false,
      busquedaRealizada: false,
      errorMessage: '',
      hoyFecha: ''
    }
  },
  async mounted() {
    this.verificarSesion()
    if (this.usuarioAutenticado) {
      await this.cargarEspecialistas()
      this.generarHorariosDisponibles()
      this.establecerFechaMinima()
    }
  },
  methods: {
    verificarSesion() {
      const usuarioJSON = localStorage.getItem('currentUser')
      if (usuarioJSON) {
        try {
          JSON.parse(usuarioJSON)
          this.usuarioAutenticado = true
        } catch (error) {
          this.usuarioAutenticado = false
        }
      } else {
        this.usuarioAutenticado = false
      }
    },
    async cargarEspecialistas() {
      try {
        this.especialistas = await getEspecialistas()
      } catch (error) {
        this.errorMessage = 'Error al cargar especialistas'
        console.error(error)
      }
    },
    generarHorariosDisponibles() {
      for (let hora = 8; hora <= 18; hora++) {
        const horaStr = String(hora).padStart(2, '0') + ':00'
        this.horariosDisponibles.push(horaStr)
      }
    },
    establecerFechaMinima() {
      const hoy = new Date()
      const año = hoy.getFullYear()
      const mes = String(hoy.getMonth() + 1).padStart(2, '0')
      const día = String(hoy.getDate()).padStart(2, '0')
      this.hoyFecha = `${año}-${mes}-${día}`
    },
    async buscarCitas() {
      this.errorMessage = ''
      this.buscando = true
      this.busquedaRealizada = false
      this.citasDisponibles = []

      if (this.criterios.fecha) {
        const fechaSeleccionada = new Date(this.criterios.fecha + 'T00:00:00')
        const hoy = new Date()
        hoy.setHours(0, 0, 0, 0)
        
        if (fechaSeleccionada < hoy) {
          this.errorMessage = 'Criterio de búsqueda de cita incorrecto: No puede buscar citas en fechas pasadas'
          this.buscando = false
          return
        }
      }

      try {
        const todasLasCitas = await obtenerCitas()
        
        const especialistasABuscar = this.criterios.especialista 
          ? [this.criterios.especialista]
          : this.especialistas.map(e => e.nombre)

        const fechasABuscar = this.criterios.fecha 
          ? [this.criterios.fecha]
          : this.generarProximasFechas(30)

        const horariosABuscar = this.criterios.horario
          ? [this.criterios.horario]
          : this.horariosDisponibles

        for (const especialista of especialistasABuscar) {
          for (const fecha of fechasABuscar) {
            const citasDelDia = todasLasCitas.filter(
              cita => cita.fecha === fecha && 
                      cita.especialista === especialista &&
                      cita.estado !== 'cancelada'
            )

            const horariosOcupados = citasDelDia.map(cita => cita.hora)

            for (const horario of horariosABuscar) {
              if (!horariosOcupados.includes(horario)) {
                this.citasDisponibles.push({
                  especialista,
                  fecha,
                  horario
                })
              }
            }
          }
        }

        this.busquedaRealizada = true
      } catch (error) {
        console.error('Error al buscar citas:', error)
        this.errorMessage = 'Error al buscar citas disponibles'
      } finally {
        this.buscando = false
      }
    },
    generarProximasFechas(dias: number): string[] {
      const fechas: string[] = []
      const hoy = new Date()
      
      for (let i = 0; i < dias; i++) {
        const fecha = new Date(hoy)
        fecha.setDate(hoy.getDate() + i)
        const año = fecha.getFullYear()
        const mes = String(fecha.getMonth() + 1).padStart(2, '0')
        const día = String(fecha.getDate()).padStart(2, '0')
        fechas.push(`${año}-${mes}-${día}`)
      }
      
      return fechas
    },
    limpiarCriterios() {
      this.criterios = {
        especialista: '',
        fecha: '',
        horario: ''
      }
      this.citasDisponibles = []
      this.busquedaRealizada = false
      this.errorMessage = ''
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
    agendarCita(cita: CitaDisponible) {
      this.$router.push({
        path: '/agendar-cita',
        query: {
          especialista: cita.especialista,
          fecha: cita.fecha,
          hora: cita.horario
        }
      })
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

    <div
      :class="[
        'bg-blue-200/50 rounded-lg shadow-lg p-6 max-w-3xl mx-auto mt-8 border border-blue-300',
        isCollapsed ? 'ml-20' : 'ml-64'
      ]"
    >
      <!-- Acceso denegado -->
      <div
        v-if="!usuarioAutenticado"
        class="bg-red-100 text-red-700 border border-red-300 p-5 rounded mb-5"
      >
        <h3 class="text-lg font-bold mb-2">Acceso Denegado</h3>
        <p class="mb-3">Debes iniciar sesión para consultar citas disponibles.</p>
        <router-link
          to="/login"
          class="inline-block mt-3 px-4 py-2 rounded bg-linear-to-r bg-blue-600/60 text-white font-semibold text-sm hover:-translate-y-0.5 hover:shadow-lg transition"
        >
          Ir al Login
        </router-link>
      </div>

      <!-- Card principal -->
      <div v-else class="bg-white rounded-lg shadow-md overflow-hidden max-w-6xl mx-auto">
        <!-- Header -->
        <div class="bg-linear-to-r bg-blue-600/60 text-white p-8 text-center">
          <h2 class="text-2xl font-semibold">Consultar Citas Disponibles</h2>
        </div>

        <!-- Mensaje de error -->
        <div v-if="errorMessage" class="bg-red-100 text-red-700 border border-red-300 p-4 rounded mb-5">
          {{ errorMessage }}
        </div>

        <!-- Contenido -->
        <div class="p-8">
          <!-- Criterios de búsqueda -->
          <div class="bg-gray-100 rounded-lg p-6 mb-8">
            <h3 class="text-lg font-semibold text-gray-800 mb-5">Criterios de Búsqueda</h3>

            <!-- Especialista -->
            <div class="mb-5">
              <label for="especialista" class="block mb-2 font-medium text-gray-700 text-sm">Especialista (Opcional)</label>
              <select
                id="especialista"
                v-model="criterios.especialista"
                class="w-full p-3 border rounded text-sm focus:outline-none focus:ring-2 focus:ring-indigo-400"
              >
                <option value="">Todos los especialistas</option>
                <option v-for="esp in especialistas" :key="esp.correoElectronico" :value="`${esp.nombre} ${esp.apellido}`">
                  {{ esp.nombre }} {{ esp.apellido }}
                </option>
              </select>
            </div>

            <!-- Fecha -->
            <div class="mb-5">
              <label for="fecha" class="block mb-2 font-medium text-gray-700 text-sm">Fecha (Opcional)</label>
              <input
                type="date"
                id="fecha"
                v-model="criterios.fecha"
                :min="hoyFecha"
                class="w-full p-3 border rounded text-sm focus:outline-none focus:ring-2 focus:ring-indigo-400"
              />
            </div>

            <!-- Horario -->
            <div class="mb-5">
              <label for="horario" class="block mb-2 font-medium text-gray-700 text-sm">Horario (Opcional)</label>
              <select
                id="horario"
                v-model="criterios.horario"
                class="w-full p-3 border rounded text-sm focus:outline-none focus:ring-2 focus:ring-indigo-400"
              >
                <option value="">Cualquier horario</option>
                <option v-for="hora in horariosDisponibles" :key="hora" :value="hora">
                  {{ hora }}
                </option>
              </select>
            </div>

            <!-- Botones -->
            <div class="flex gap-3 mt-6 flex-wrap">
              <button
                @click="buscarCitas"
                class="flex-1 bg-linear-to-r bg-blue-400 hover:bg-blue-500 text-white font-semibold py-2 px-4 rounded hover:-translate-y-0.5 hover:shadow-lg transition disabled:opacity-50 disabled:cursor-not-allowed"
                :disabled="buscando"
              >
                {{ buscando ? 'Buscando...' : 'Buscar' }}
              </button>
              <button
                @click="limpiarCriterios"
                class="flex-1 bg-gray-600 text-white font-semibold py-2 px-4 rounded hover:bg-gray-700 transition hover:-translate-y-0.5"
              >
                Limpiar
              </button>
              <router-link
                to="/mis-citas"
                class="flex-1 bg-gray-600 text-white font-semibold py-2 px-4 rounded hover:bg-red-700 transition text-center hover:-translate-y-0.5"
              >
              <P class="text-white">Salir</P>                
              </router-link>
            </div>
          </div>

          <!-- Resultados -->
          <div v-if="busquedaRealizada" class="mt-8">
            <h3 class="text-lg font-semibold text-gray-800 mb-5">Resultados de la Búsqueda</h3>

            <div v-if="citasDisponibles.length === 0" class="bg-yellow-100 text-yellow-800 border border-yellow-300 p-4 rounded">
              No hay horarios disponibles con los criterios seleccionados.
            </div>

            <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              <div
                v-for="(cita, index) in citasDisponibles"
                :key="index"
                class="bg-white border-2 border-gray-200 rounded-lg overflow-hidden transition hover:border-indigo-500 hover:shadow-lg"
              >
                <div class="bg-linear-to-r bg-blue-600/60 text-white p-4">
                  <h4 class="text-lg font-semibold">{{ cita.especialista }}</h4>
                </div>
                <div class="p-4">
                  <div class="flex justify-between items-center border-b py-2 text-sm">
                    <span class="font-semibold text-gray-700">Fecha:</span>
                    <span class="text-gray-600">{{ formatearFecha(cita.fecha) }}</span>
                  </div>
                  <div class="flex justify-between items-center border-b py-2 text-sm">
                    <span class="font-semibold text-gray-700">Horario:</span>
                    <span class="text-gray-600">{{ cita.horario }}</span>
                  </div>
                  <div class="flex justify-between items-center py-2 text-sm">
                    <span class="font-semibold text-gray-700">Estado:</span>
                    <span class="bg-green-500 text-white px-3 py-1 rounded-full text-xs font-bold">Disponible</span>
                  </div>
                </div>
                <div class="p-4 border-t text-center">
                  <button
                    @click="agendarCita(cita)"
                    class="bg-linear-to-r bg-blue-600/60 hover:bg-blue-500 text-white font-semibold py-2 px-4 rounded text-sm hover:-translate-y-0.5 hover:shadow-lg transition"
                  >
                    Aceptar horario
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
