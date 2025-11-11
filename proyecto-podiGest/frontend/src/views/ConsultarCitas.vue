<template>
  <div class="consultar-wrapper">
    <SideBar :is-collapsed="isCollapsed" @toggle="toggleSidebar" />

    <div :class="[
      'consultar-container transition-all duration-300',
      isCollapsed ? 'ml-20' : 'ml-64'
    ]">
      <div v-if="!usuarioAutenticado" class="alert alert-danger">
        <h3>Acceso Denegado</h3>
        <p>Debes iniciar sesión para consultar citas disponibles.</p>
        <router-link to="/login" class="btn btn-primary mt-3">
          Ir al Login
        </router-link>
      </div>

      <div v-else class="card">
        <div class="card-header">
          <h2>Consultar Citas Disponibles</h2>
        </div>

        <div v-if="errorMessage" class="alert alert-danger">
          {{ errorMessage }}
        </div>

        <div class="consultar-content">
          <div class="criterios-busqueda">
            <h3>Criterios de Búsqueda</h3>
            
            <div class="form-group">
              <label for="especialista">Especialista (Opcional)</label>
              <select 
                id="especialista" 
                v-model="criterios.especialista"
                class="form-control"
              >
                <option value="">Todos los especialistas</option>
                <option v-for="esp in especialistas" :key="esp.correoElectronico" :value="esp.nombre">
                  {{ esp.nombre }} {{ esp.apellido }}
                </option>
              </select>
            </div>

            <div class="form-group">
              <label for="fecha">Fecha (Opcional)</label>
              <input 
                type="date" 
                id="fecha" 
                v-model="criterios.fecha"
                :min="hoyFecha"
                class="form-control"
              />
            </div>

            <div class="form-group">
              <label for="horario">Horario (Opcional)</label>
              <select 
                id="horario" 
                v-model="criterios.horario"
                class="form-control"
              >
                <option value="">Cualquier horario</option>
                <option v-for="hora in horariosDisponibles" :key="hora" :value="hora">
                  {{ hora }}
                </option>
              </select>
            </div>

            <div class="form-actions">
              <button @click="buscarCitas" class="btn btn-primary" :disabled="buscando">
                {{ buscando ? 'Buscando...' : 'Buscar' }}
              </button>
              <button @click="limpiarCriterios" class="btn btn-secondary">
                Limpiar
              </button>
              <router-link to="/mis-citas" class="btn btn-secondary">
                Salir
              </router-link>
            </div>
          </div>

          <div v-if="busquedaRealizada" class="resultados-section">
            <h3>Resultados de la Búsqueda</h3>
            
            <div v-if="citasDisponibles.length === 0" class="alert alert-warning">
              No hay horarios disponibles con los criterios seleccionados.
            </div>

            <div v-else class="resultados-grid">
              <div v-for="(cita, index) in citasDisponibles" :key="index" class="resultado-card">
                <div class="resultado-header">
                  <h4>{{ cita.especialista }}</h4>
                </div>
                <div class="resultado-content">
                  <div class="resultado-row">
                    <span class="label">Fecha:</span>
                    <span class="value">{{ formatearFecha(cita.fecha) }}</span>
                  </div>
                  <div class="resultado-row">
                    <span class="label">Horario:</span>
                    <span class="value">{{ cita.horario }}</span>
                  </div>
                  <div class="resultado-row">
                    <span class="label">Estado:</span>
                    <span class="badge-disponible">Disponible</span>
                  </div>
                </div>
                <div class="resultado-actions">
                  <button @click="agendarCita(cita)" class="btn btn-primary btn-sm">
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

<style scoped>
.consultar-wrapper {
  display: flex;
}

.consultar-container {
  min-height: 100vh;
  padding: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  flex: 1;
}

.card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  max-width: 1200px;
  margin: 0 auto;
}

.card-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 30px;
  text-align: center;
}

.card-header h2 {
  margin: 0;
  font-size: 28px;
  font-weight: 600;
}

.consultar-content {
  padding: 30px;
}

.criterios-busqueda {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 25px;
  margin-bottom: 30px;
}

.criterios-busqueda h3 {
  margin: 0 0 20px 0;
  font-size: 20px;
  color: #333;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #333;
  font-size: 14px;
}

.form-control {
  width: 100%;
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

.form-actions {
  display: flex;
  gap: 10px;
  margin-top: 25px;
}

.btn {
  padding: 12px 20px;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  text-decoration: none;
  text-align: center;
  display: inline-flex;
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
}

.btn-sm {
  padding: 8px 16px;
  font-size: 13px;
}

.resultados-section {
  margin-top: 30px;
}

.resultados-section h3 {
  margin: 0 0 20px 0;
  font-size: 20px;
  color: #333;
}

.resultados-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.resultado-card {
  background: white;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s;
}

.resultado-card:hover {
  border-color: #667eea;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.2);
}

.resultado-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 15px;
}

.resultado-header h4 {
  margin: 0;
  font-size: 18px;
}

.resultado-content {
  padding: 15px;
}

.resultado-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}

.resultado-row:last-child {
  border-bottom: none;
}

.resultado-row .label {
  font-weight: 600;
  color: #333;
  font-size: 14px;
}

.resultado-row .value {
  color: #666;
  font-size: 14px;
}

.badge-disponible {
  background-color: #28a745;
  color: white;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}

.resultado-actions {
  padding: 15px;
  border-top: 1px solid #f0f0f0;
  text-align: center;
}

.alert {
  padding: 15px;
  border-radius: 4px;
  margin-bottom: 20px;
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

.alert h3 {
  margin-top: 0;
  margin-bottom: 10px;
}

.alert p {
  margin-bottom: 15px;
}

.mt-3 {
  margin-top: 15px;
}

@media (max-width: 768px) {
  .consultar-container {
    padding: 10px;
  }

  .consultar-content {
    padding: 15px;
  }

  .criterios-busqueda {
    padding: 15px;
  }

  .form-actions {
    flex-direction: column;
  }

  .btn {
    width: 100%;
  }

  .resultados-grid {
    grid-template-columns: 1fr;
  }
}
</style>
