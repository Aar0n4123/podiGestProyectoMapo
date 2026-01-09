<template>
  <div class="flex">
    <SideBar :is-collapsed="isCollapsed" @toggle="toggleSidebar" />

    <div :class="[
      'min-h-screen flex justify-center items-center p-5 transition-all duration-300 bg-linear-to-br rounded-2xl  bg-blue-200/50',
      isCollapsed ? 'ml-20' : 'ml-64'
    ]">
      <!-- Acceso denegado -->
      <div v-if="!usuarioAutenticado" class="bg-red-100 border border-red-400 text-red-700 rounded p-6 text-center">
        <h3 class="text-xl font-bold mb-2">Acceso Denegado</h3>
        <p>Debes iniciar sesión para agendar una cita.</p>
        <router-link to="/login"
          class="mt-3 inline-block bg-linear-to-r bg-blue-500 text-white font-semibold py-2 px-4 rounded hover:shadow-lg transition">
          Ir al Login
        </router-link>
      </div>

      <!-- Card principal -->
      <div v-else class="bg-white rounded-lg shadow-lg w-full max-w-3xl overflow-hidden">
        <!-- Header -->
        <div class="bg-linear-to-br bg-blue-600/60 text-white p-8 text-center">
          <h2 class="text-2xl font-semibold mb-5">Agendar Cita</h2>
          <div class="flex justify-center gap-5 mt-5">
            <span
              :class="['px-4 py-2 rounded-full text-sm font-medium transition', paso >= 1 ? 'bg-white text-blue-700' : 'bg-white/20 text-white']">
              1. Especialista
            </span>
            <span
              :class="['px-4 py-2 rounded-full text-sm font-medium transition', paso >= 2 ? 'bg-white text-blue-700' : 'bg-white/20 text-white']">
              2. Horario
            </span>
            <span
              :class="['px-4 py-2 rounded-full text-sm font-medium transition', paso >= 3 ? 'bg-white text-blue-700' : 'bg-white/20 text-white']">
              3. Confirmar
            </span>
          </div>
        </div>

        <!-- Mensajes -->
        <div v-if="successMessage" class="bg-green-100 border border-green-400 text-green-700 rounded p-4 m-6">
          {{ successMessage }}
        </div>
        <div v-if="errorMessage" class="bg-red-100 border border-red-400 text-red-700 rounded p-4 m-6">
          {{ errorMessage }}
        </div>

        <!-- Paso 1 -->
        <div v-if="paso === 1 && !successMessage" class="p-8">
          <h3 class="text-xl font-semibold text-gray-800 text-center mb-6">Seleccione un Especialista</h3>

          <div v-if="especialistas.length === 0"
            class="bg-yellow-100 border border-yellow-400 text-yellow-700 rounded p-4 text-center">
            No hay especialistas disponibles en este momento.
          </div>

          <div v-else class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-5 mb-8 ">
            <div v-for="esp in especialistas" :key="esp.correoElectronico" @click="seleccionarEspecialista(esp)" :class="[
              'bg-white border-2 rounded-lg p-6 text-center cursor-pointer transition hover:-translate-y-0.5',
              formulario.especialista === `${esp.nombre} ${esp.apellido}` ? 'border-blue-500 bg-blue-50' : 'border-gray-200 hover:border-blue-400 hover:shadow-lg'
            ]">
              <div class="text-5xl mb-3 ">👨‍⚕️</div>
              <h4 class="text-lg font-semibold text-gray-800">{{ esp.nombre }} {{ esp.apellido }}</h4>
              <p class="text-sm text-gray-500">Especialista</p>
            </div>
          </div>

          <div class="flex gap-3 mt-6">
            <button @click="siguientePaso"
              class="flex-1 bg-blue-400 hover:bg-blue-500 text-white font-semibold py-2 px-4 rounded hover:shadow-lg transition disabled:opacity-50 disabled:cursor-not-allowed focus:outline-none hover:-translate-y-0.5 focus:ring-0 hover:border-transparent"
              :disabled="!formulario.especialista">
              Siguiente
            </button>


            <router-link to="/mis-citas" class="flex-1 bg-gray-600 text-white font-semibold py-2 px-4 rounded-lg shadow-md 
         hover:bg-gray-700 hover:shadow-lg transition-transform transform hover:-translate-y-0.5">
              <p class="text-white">Cancelar</p>
            </router-link>
          </div>
        </div>

        <!-- Paso 2 -->
        <div v-if="paso === 2 && !successMessage" class="p-8">
          <h3 class="text-xl font-semibold text-gray-800 text-center mb-6">Seleccione Fecha y Horario</h3>

          <div class="flex flex-col mb-5">
            <label for="fecha" class="mb-2 font-medium text-gray-700">Fecha de la Cita</label>
            <input type="date" id="fecha" v-model="formulario.fecha" @change="cargarHorariosDisponibles" :min="hoyFecha"
              required class="p-3 border rounded focus:outline-none focus:ring-2 focus:ring-blue-400" />
          </div>

          <div v-if="formulario.fecha" class="mt-6">
            <h4 class="text-lg font-semibold text-gray-800 mb-3">Horarios Disponibles</h4>
            <div v-if="cargandoHorarios" class="text-center text-gray-500 py-4">Cargando horarios...</div>
            <div v-else-if="horariosDisponibles.length === 0"
              class="bg-yellow-100 border border-yellow-400 text-yellow-700 rounded p-4 text-center">
              No hay horarios disponibles para esta fecha.
            </div>
            <div v-else class="grid grid-cols-3 sm:grid-cols-4 gap-3">
              <button v-for="horario in horariosDisponibles" :key="horario" @click="seleccionarHorario(horario)" :class="[
                'p-3 border rounded text-sm font-medium transition',
                formulario.hora === horario ? 'bg-linear-to-r bg-blue-400 hover:bg-blue-500 text-white border-blue-500 hover:-translate-y-0.5' : 'bg-white border-gray-200 hover:border-blue-400 hover:bg-blue-50'
              ]" type="button">
                {{ horario }}
              </button>
            </div>
          </div>

          <div class="flex gap-3 mt-6">
            <button @click="paso = 1"
              class="flex-1 bg-gray-600 text-white font-semibold py-2 px-4 rounded hover:bg-gray-700 transition hover:-translate-y-0.5">
              Atrás
            </button>
            <button @click="siguientePaso"
              class="flex-1 bg-blue-400 hover:bg-blue-500 text-white font-semibold py-2 px-4 rounded hover:shadow-lg transition disabled:opacity-50 disabled:cursor-not-allowed focus:outline-none hover:-translate-y-0.5 focus:ring-0 hover:border-transparent"
              :disabled="!formulario.fecha || !formulario.hora">
              Siguiente
            </button>
          </div>
        </div>

        <!-- Paso 3 -->
        <div v-if="paso === 3 && !successMessage" class="p-8">
          <h3 class="text-xl font-semibold text-gray-800 text-center mb-6">Confirmar Cita</h3>

          <div class="flex flex-col mb-5">
            <label for="telefono" class="mb-2 font-medium text-gray-700">Teléfono de Contacto</label>
            <input type="tel" id="telefono" v-model="formulario.pacienteTelefono" placeholder="Ingrese su teléfono"
              required class="p-3 border rounded focus:outline-none focus:ring-2 focus:ring-blue-400" />
          </div>

          <div class="flex flex-col mb-5">
            <label for="razon" class="mb-2 font-medium text-gray-700">Razón de la Consulta</label>
            <textarea id="razon" v-model="formulario.razonConsulta" placeholder="Describa el motivo de la consulta"
              required rows="4"
              class="p-3 border rounded focus:outline-none focus:ring-2 focus:ring-blue-400"></textarea>
          </div>

          <div class="bg-gray-100 rounded p-5 mb-6">
            <h4 class="text-lg font-semibold text-gray-800 mb-3">Resumen de la Cita</h4>
            <div class="flex justify-between border-b py-2 text-sm">
              <span class="font-semibold text-gray-700">Paciente:</span>
              <span class="text-gray-600">{{ formulario.pacienteNombre }}</span>
            </div>

            <div class="flex justify-between border-b py-2 text-sm">
              <span class="font-semibold text-gray-700">Correo:</span>
              <span class="text-gray-600">{{ formulario.pacienteCorreo }}</span>
            </div>
            <div class="flex justify-between border-b py-2 text-sm">
              <span class="font-semibold text-gray-700">Teléfono:</span>
              <span class="text-gray-600">{{ formulario.pacienteTelefono || 'No especificado' }}</span>
            </div>
            <div class="flex justify-between border-b py-2 text-sm">
              <span class="font-semibold text-gray-700">Especialista:</span>
              <span class="text-gray-600">{{ formulario.especialista }}</span>
            </div>
            <div class="flex justify-between border-b py-2 text-sm">
              <span class="font-semibold text-gray-700">Fecha:</span>
              <span class="text-gray-600">{{ formatearFecha(formulario.fecha) }}</span>
            </div>
            <div class="flex justify-between border-b py-2 text-sm">
              <span class="font-semibold text-gray-700">Hora:</span>
              <span class="text-gray-600">{{ formulario.hora }}</span>
            </div>
            <div class="flex justify-between border-b py-2 text-sm">
              <span class="font-semibold text-gray-700">Razón:</span>
              <span class="text-gray-600">{{ formulario.razonConsulta || 'No especificada' }}</span>
            </div>
          </div>

          <div class="form-actions flex gap-3">
            <button @click="paso = 2"
              class="flex-1 bg-gray-600 hover:bg-gray-700 hover:-translate-y-0.5 text-white py-2 px-4 rounded transition">
              Atrás
            </button>
            <button @click="agendarCita"
              class="flex-1 bg-blue-400 hover:bg-blue-500 hover:-translate-y-0.5 text-white py-2 px-4 rounded transition disabled:opacity-50 disabled:cursor-not-allowed"
              :disabled="cargando || !formulario.pacienteTelefono || !formulario.razonConsulta">
              {{ cargando ? 'Agendando...' : 'Confirmar Cita' }}
            </button>
          </div>

        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import SideBar from '../components/SideBar.vue'
import { crearCita, getEspecialistas, obtenerCitas } from '../services/appointmentService'

interface Usuario {
  id: string
  nombre: string
  apellido: string
  correoElectronico: string
  especialidad: string
}

interface Formulario {
  pacienteNombre: string
  pacienteCorreo: string
  pacienteTelefono: string
  especialista: string
  cedulaEspecialista: string
  fecha: string
  hora: string
  razonConsulta: string
  estado: string
}

export default defineComponent({
  name: 'AgendarCita',
  components: {
    SideBar
  },
  data() {
    return {
      paso: 1,
      formulario: {
        pacienteNombre: '',
        pacienteCorreo: '',
        pacienteTelefono: '',
        especialista: '',
        cedulaEspecialista: '',
        fecha: '',
        hora: '',
        razonConsulta: '',
        estado: 'pendiente'
      } as Formulario,
      especialistas: [] as Usuario[],
      horariosDisponibles: [] as string[],
      cargando: false,
      cargandoHorarios: false,
      errorMessage: '',
      successMessage: '',
      hoyFecha: '',
      isCollapsed: false,
      usuarioAutenticado: false
    }
  },
  async mounted() {
    this.verificarSesion()
    if (this.usuarioAutenticado) {
      this.obtenerDatosUsuario()
      await this.cargarEspecialistas()
      this.establecerFechaMinima()
      this.cargarParametrosDeConsulta()
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
    obtenerDatosUsuario() {
      const usuarioJSON = localStorage.getItem('currentUser')
      if (usuarioJSON) {
        try {
          const usuario = JSON.parse(usuarioJSON)
          this.formulario.pacienteNombre = usuario.nombre + ' ' + usuario.apellido
          this.formulario.pacienteCorreo = usuario.correoElectronico || usuario.correo
        } catch (error) {
          this.errorMessage = 'Error al cargar los datos del usuario'
        }
      }
    },
    async cargarEspecialistas() {
      try {
        this.especialistas = await getEspecialistas()
        if (this.especialistas.length === 0) {
          this.errorMessage = 'No hay especialistas disponibles'
        }
      } catch (error) {
        this.errorMessage = 'Error al cargar especialistas'
        console.error(error)
      }
    },
    establecerFechaMinima() {
      const hoy = new Date()
      const año = hoy.getFullYear()
      const mes = String(hoy.getMonth() + 1).padStart(2, '0')
      const día = String(hoy.getDate()).padStart(2, '0')
      this.hoyFecha = `${año}-${mes}-${día}`
    },
    cargarParametrosDeConsulta() {
      const query = this.$route.query
      if (query.especialista) {
        this.formulario.especialista = query.especialista as string
        this.paso = 2
      }
      if (query.fecha) {
        this.formulario.fecha = query.fecha as string
      }
      if (query.hora) {
        this.formulario.hora = query.hora as string
        if (this.formulario.fecha && this.formulario.especialista) {
          this.paso = 3
        }
      }
    },
    seleccionarEspecialista(especialista: Usuario) {
      this.formulario.especialista = `${especialista.nombre} ${especialista.apellido}`
      this.formulario.cedulaEspecialista = especialista.cedula
    },
    async cargarHorariosDisponibles() {
      if (!this.formulario.fecha || !this.formulario.especialista) {
        return
      }

      const fechaSeleccionada = new Date(this.formulario.fecha + 'T00:00:00')
      const hoy = new Date()
      hoy.setHours(0, 0, 0, 0)

      if (fechaSeleccionada < hoy) {
        this.errorMessage = 'No puede agendar citas en fechas pasadas'
        this.horariosDisponibles = []
        return
      }

      this.cargandoHorarios = true
      this.horariosDisponibles = []

      try {
        const todasLasCitas = await obtenerCitas()
        const citasDelDia = todasLasCitas.filter(
          cita => cita.fecha === this.formulario.fecha &&
            cita.especialista === this.formulario.especialista &&
            cita.estado !== 'cancelada'
        )

        const horariosOcupados = citasDelDia.map(cita => cita.hora)

        const todosLosHorarios = []
        for (let hora = 8; hora <= 18; hora++) {
          const horaStr = String(hora).padStart(2, '0') + ':00'
          todosLosHorarios.push(horaStr)
        }

        this.horariosDisponibles = todosLosHorarios.filter(
          horario => !horariosOcupados.includes(horario)
        )
      } catch (error) {
        console.error('Error al cargar horarios:', error)
        this.errorMessage = 'Error al cargar horarios disponibles'
      } finally {
        this.cargandoHorarios = false
      }
    },
    seleccionarHorario(horario: string) {
      this.formulario.hora = horario
    },
    siguientePaso() {
      if (this.paso === 1 && !this.formulario.especialista) {
        this.errorMessage = 'Debe seleccionar un especialista'
        return
      }
      if (this.paso === 2) {
        if (!this.formulario.fecha || !this.formulario.hora) {
          this.errorMessage = 'Debe seleccionar fecha y horario'
          return
        }

        const fechaSeleccionada = new Date(this.formulario.fecha + 'T00:00:00')
        const hoy = new Date()
        hoy.setHours(0, 0, 0, 0)

        if (fechaSeleccionada < hoy) {
          this.errorMessage = 'No puede agendar citas en fechas pasadas'
          return
        }
      }
      this.errorMessage = ''
      this.paso++
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
    async agendarCita() {
      this.errorMessage = ''
      this.successMessage = ''

      if (!this.formulario.pacienteTelefono) {
        this.errorMessage = 'El teléfono es requerido'
        return
      }

      if (!this.formulario.razonConsulta) {
        this.errorMessage = 'La razón de la consulta es requerida'
        return
      }

      const fechaSeleccionada = new Date(this.formulario.fecha + 'T00:00:00')
      const hoy = new Date()
      hoy.setHours(0, 0, 0, 0)

      if (fechaSeleccionada < hoy) {
        this.errorMessage = 'No puede agendar citas en fechas pasadas'
        return
      }

      try {
        const todasLasCitas = await obtenerCitas()
        const citaExistente = todasLasCitas.find(
          cita => cita.fecha === this.formulario.fecha &&
            cita.hora === this.formulario.hora &&
            cita.especialista === this.formulario.especialista &&
            cita.estado !== 'cancelada'
        )

        if (citaExistente) {
          this.errorMessage = 'Este horario ya está ocupado. Por favor seleccione otro horario.'
          return
        }
      } catch (error) {
        console.error('Error al verificar disponibilidad:', error)
      }

      this.cargando = true
      try {
        const resultado = await crearCita({
          pacienteNombre: this.formulario.pacienteNombre,
          pacienteCorreo: this.formulario.pacienteCorreo,
          pacienteTelefono: this.formulario.pacienteTelefono,
          especialista: this.formulario.especialista,
          cedulaEspecialista: this.formulario.cedulaEspecialista,
          especialidadBuscada: '',
          fecha: this.formulario.fecha,
          hora: this.formulario.hora,
          razonConsulta: this.formulario.razonConsulta,
          estado: 'pendiente'
        })

        if (resultado) {
          this.successMessage = '¡Cita agendada exitosamente! Redirigiendo...'
          setTimeout(() => {
            this.$router.push('/mis-citas')
          }, 2000)
        } else {
          this.errorMessage = 'Error al agendar la cita'
        }
      } catch (error: any) {
        this.errorMessage = `Error al agendar la cita: ${error?.message || error}`
        console.error('Error completo:', error)
      } finally {
        this.cargando = false
      }
    },
    toggleSidebar() {
      this.isCollapsed = !this.isCollapsed
    }
  }
})
</script>
