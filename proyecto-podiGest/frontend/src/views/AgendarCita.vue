<template>
  <div class="agendar-wrapper">
    <SideBar :is-collapsed="isCollapsed" @toggle="toggleSidebar" />

    <div :class="[
      'agendar-container transition-all duration-300',
      isCollapsed ? 'ml-20' : 'ml-64'
    ]">
      <div v-if="!usuarioAutenticado" class="alert alert-danger">
        <h3>Acceso Denegado</h3>
        <p>Debes iniciar sesión para agendar una cita.</p>
        <router-link to="/login" class="btn btn-primary mt-3">
          Ir al Login
        </router-link>
      </div>

      <div v-else class="card">
        <div class="card-header">
          <h2>Agendar Cita</h2>
          <div class="steps">
            <span :class="['step', paso >= 1 ? 'active' : '']">1. Especialista</span>
            <span :class="['step', paso >= 2 ? 'active' : '']">2. Horario</span>
            <span :class="['step', paso >= 3 ? 'active' : '']">3. Confirmar</span>
          </div>
        </div>

        <div v-if="successMessage" class="alert alert-success">
          {{ successMessage }}
        </div>

        <div v-if="errorMessage" class="alert alert-danger">
          {{ errorMessage }}
        </div>

        <div v-if="paso === 1 && !successMessage" class="paso-container">
          <h3>Seleccione un Especialista</h3>
          
          <div v-if="especialistas.length === 0" class="alert alert-warning">
            No hay especialistas disponibles en este momento.
          </div>

          <div v-else class="especialistas-grid">
            <div 
              v-for="esp in especialistas" 
              :key="esp.correoElectronico"
              @click="seleccionarEspecialista(esp)"
              :class="['especialista-card', formulario.especialista === esp.nombre ? 'selected' : '']"
            >
              <div class="especialista-icon">👨‍⚕️</div>
              <h4>{{ esp.nombre }} {{ esp.apellido }}</h4>
              <p class="especialista-info">Especialista</p>
            </div>
          </div>

          <div class="form-actions">
            <button 
              @click="siguientePaso" 
              class="btn btn-primary" 
              :disabled="!formulario.especialista"
            >
              Siguiente
            </button>
            <router-link to="/mis-citas" class="btn btn-secondary">
              Cancelar
            </router-link>
          </div>
        </div>

        <div v-if="paso === 2 && !successMessage" class="paso-container">
          <h3>Seleccione Fecha y Horario</h3>
          
          <div class="form-group">
            <label for="fecha">Fecha de la Cita</label>
            <input 
              type="date" 
              id="fecha" 
              v-model="formulario.fecha"
              @change="cargarHorariosDisponibles"
              :min="hoyFecha"
              required
              class="form-control"
            />
          </div>

          <div v-if="formulario.fecha" class="horarios-section">
            <h4>Horarios Disponibles</h4>
            <div v-if="cargandoHorarios" class="loading-horarios">
              Cargando horarios...
            </div>
            <div v-else-if="horariosDisponibles.length === 0" class="alert alert-warning">
              No hay horarios disponibles para esta fecha.
            </div>
            <div v-else class="horarios-grid">
              <button
                v-for="horario in horariosDisponibles"
                :key="horario"
                @click="seleccionarHorario(horario)"
                :class="['horario-btn', formulario.hora === horario ? 'selected' : '']"
                type="button"
              >
                {{ horario }}
              </button>
            </div>
          </div>

          <div class="form-actions">
            <button @click="paso = 1" class="btn btn-secondary">
              Atrás
            </button>
            <button 
              @click="siguientePaso" 
              class="btn btn-primary" 
              :disabled="!formulario.fecha || !formulario.hora"
            >
              Siguiente
            </button>
          </div>
        </div>

        <div v-if="paso === 3 && !successMessage" class="paso-container">
          <h3>Confirmar Cita</h3>
          
          <div class="form-group">
            <label for="telefono">Teléfono de Contacto</label>
            <input 
              type="tel" 
              id="telefono" 
              v-model="formulario.pacienteTelefono"
              placeholder="Ingrese su teléfono"
              required
              class="form-control"
            />
          </div>

          <div class="form-group">
            <label for="razon">Razón de la Consulta</label>
            <textarea 
              id="razon" 
              v-model="formulario.razonConsulta"
              placeholder="Describa el motivo de la consulta"
              required
              class="form-control"
              rows="4"
            ></textarea>
          </div>

          <div class="resumen-cita">
            <h4>Resumen de la Cita</h4>
            <div class="resumen-row">
              <span class="label">Paciente:</span>
              <span class="value">{{ formulario.pacienteNombre }}</span>
            </div>
            <div class="resumen-row">
              <span class="label">Correo:</span>
              <span class="value">{{ formulario.pacienteCorreo }}</span>
            </div>
            <div class="resumen-row">
              <span class="label">Teléfono:</span>
              <span class="value">{{ formulario.pacienteTelefono || 'No especificado' }}</span>
            </div>
            <div class="resumen-row">
              <span class="label">Especialista:</span>
              <span class="value">{{ formulario.especialista }}</span>
            </div>
            <div class="resumen-row">
              <span class="label">Fecha:</span>
              <span class="value">{{ formatearFecha(formulario.fecha) }}</span>
            </div>
            <div class="resumen-row">
              <span class="label">Hora:</span>
              <span class="value">{{ formulario.hora }}</span>
            </div>
            <div class="resumen-row">
              <span class="label">Razón:</span>
              <span class="value">{{ formulario.razonConsulta || 'No especificada' }}</span>
            </div>
          </div>

          <div class="form-actions">
            <button @click="paso = 2" class="btn btn-secondary">
              Atrás
            </button>
            <button 
              @click="agendarCita" 
              class="btn btn-primary" 
              :disabled="cargando || !formulario.pacienteTelefono || !formulario.razonConsulta"
            >
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
      this.formulario.especialista = especialista.nombre
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

<style scoped>
.agendar-wrapper {
  display: flex;
}

.agendar-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  flex: 1;
}

.card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  width: 100%;
  max-width: 800px;
}

.card-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 30px;
  text-align: center;
}

.card-header h2 {
  margin: 0 0 20px 0;
  font-size: 28px;
  font-weight: 600;
}

.steps {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 20px;
}

.step {
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
  background-color: rgba(255, 255, 255, 0.2);
  transition: all 0.3s;
}

.step.active {
  background-color: white;
  color: #667eea;
}

.paso-container {
  padding: 30px;
}

.paso-container h3 {
  margin: 0 0 25px 0;
  font-size: 22px;
  color: #333;
  text-align: center;
}

.paso-container h4 {
  margin: 20px 0 15px 0;
  font-size: 18px;
  color: #333;
}

.especialistas-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.especialista-card {
  background: white;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  padding: 25px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
}

.especialista-card:hover {
  border-color: #667eea;
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.2);
}

.especialista-card.selected {
  border-color: #667eea;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
}

.especialista-icon {
  font-size: 48px;
  margin-bottom: 15px;
}

.especialista-card h4 {
  margin: 0 0 8px 0;
  font-size: 16px;
  color: #333;
}

.especialista-info {
  margin: 0;
  font-size: 13px;
  color: #666;
}

.horarios-section {
  margin-top: 25px;
}

.loading-horarios {
  text-align: center;
  padding: 20px;
  color: #666;
}

.horarios-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(80px, 1fr));
  gap: 10px;
  margin-top: 15px;
}

.horario-btn {
  padding: 12px;
  border: 2px solid #e0e0e0;
  border-radius: 6px;
  background: white;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
}

.horario-btn:hover {
  border-color: #667eea;
  background-color: rgba(102, 126, 234, 0.05);
}

.horario-btn.selected {
  border-color: #667eea;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.resumen-cita {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 25px;
}

.resumen-row {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid #e0e0e0;
}

.resumen-row:last-child {
  border-bottom: none;
}

.resumen-row .label {
  font-weight: 600;
  color: #333;
}

.resumen-row .value {
  color: #666;
  text-align: right;
}

form {
  padding: 30px;
}

.form-group {
  margin-bottom: 20px;
  display: flex;
  flex-direction: column;
}

.form-group label {
  margin-bottom: 8px;
  font-weight: 500;
  color: #333;
  font-size: 14px;
}

.form-control {
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  font-family: inherit;
  transition: border-color 0.3s;
}

.form-control:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.form-control:readonly {
  background-color: #f5f5f5;
  cursor: not-allowed;
}

.form-actions {
  display: flex;
  gap: 10px;
  margin-top: 30px;
}

.btn {
  flex: 1;
  padding: 12px;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  text-decoration: none;
  text-align: center;
  display: flex;
  align-items: center;
  justify-content: center;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-secondary {
  background-color: #6c757d;
  color: white;
}

.btn-secondary:hover {
  background-color: #5a6268;
  transform: translateY(-2px);
}

.alert {
  padding: 15px;
  border-radius: 4px;
  margin: 20px 30px;
  font-weight: 500;
}

.alert h3 {
  margin-top: 0;
  margin-bottom: 10px;
}

.alert p {
  margin-bottom: 15px;
}

.alert-success {
  background-color: #d4edda;
  color: #155724;
  border: 1px solid #c3e6cb;
}

.alert-danger {
  background-color: #f8d7da;
  color: #721c24;
  border: 1px solid #f5c6cb;
}

.alert-warning {
  background-color: #fff3cd;
  color: #856404;
  border: 1px solid #ffeaa7;
}

.mt-3 {
  margin-top: 15px;
}

@media (max-width: 768px) {
  .card {
    max-width: 100%;
  }

  .card-header {
    padding: 20px;
  }

  .card-header h2 {
    font-size: 24px;
  }

  .steps {
    flex-direction: column;
    gap: 10px;
  }

  .especialistas-grid {
    grid-template-columns: 1fr;
  }

  .horarios-grid {
    grid-template-columns: repeat(auto-fill, minmax(70px, 1fr));
  }

  .paso-container {
    padding: 20px;
  }

  .resumen-row {
    flex-direction: column;
    gap: 5px;
  }

  .resumen-row .value {
    text-align: left;
  }

  form {
    padding: 20px;
  }

  .form-actions {
    flex-direction: column;
  }

  .btn {
    width: 100%;
  }
}
</style>
