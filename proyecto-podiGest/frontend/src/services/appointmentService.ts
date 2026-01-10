export interface Cita {
  id: string
  pacienteNombre: string
  pacienteCorreo: string
  pacienteTelefono: string
  especialista: string
  cedulaEspecialista: string
  especialidadBuscada: string
  fecha: string
  hora: string
  razonConsulta: string
  estado: string
  fechaCreacion: string
}

const API_URL = 'http://localhost:8080/api/citas'
const ESPECIALISTAS_URL = 'http://localhost:8080/api/usuarios/especialistas'

export const getEspecialistas = async () => {
  try {
    const response = await fetch(ESPECIALISTAS_URL)
    if (!response.ok) {
      throw new Error('Error al obtener especialistas')
    }
    return await response.json()
  } catch (error) {
    console.error('No fue posible cargar especialistas', error)
    return []
  }
}

export const crearCita = async (cita: Omit<Cita, 'id' | 'fechaCreacion'>) => {
  try {
    const citaConMetadata = {
      ...cita,
      id: 'CITA-' + Date.now() + '-' + Math.random().toString(36).substr(2, 9),
      fechaCreacion: new Date().toISOString(),
    }

    console.log('Enviando cita al backend:', citaConMetadata)

    const response = await fetch(API_URL, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(citaConMetadata),
    })

    console.log('Respuesta del servidor:', response.status, response.statusText)

    if (!response.ok) {
      const errorText = await response.text()
      console.error('Error del servidor:', errorText)
      throw new Error(`Error al crear cita: ${response.status} ${response.statusText}`)
    }

    const resultado = await response.json()
    console.log('Cita guardada exitosamente:', resultado)
    
    guardarCitaLocalmente(resultado)
    
    return resultado
  } catch (error) {
    console.error('Error al crear cita:', error)
    guardarCitaLocalmente(citaConMetadata)
    throw error
  }
}

const guardarCitaLocalmente = (cita: Cita) => {
  try {
    const citasLocales = JSON.parse(localStorage.getItem('citasLocales') || '[]') as Cita[]
    const citaExiste = citasLocales.some(c => c.id === cita.id)
    if (!citaExiste) {
      citasLocales.push(cita)
      localStorage.setItem('citasLocales', JSON.stringify(citasLocales))
      console.log('Cita guardada en localStorage')
    }
  } catch (error) {
    console.error('Error al guardar cita localmente:', error)
  }
}

export const obtenerCitas = async (): Promise<Cita[]> => {
  try {
    const response = await fetch(API_URL)
    let citasBackend: Cita[] = []
    
    if (!response.ok) {
      console.warn('No se pudo obtener citas del backend')
      citasBackend = []
    } else {
      citasBackend = await response.json()
    }
    
    const citasLocales = JSON.parse(localStorage.getItem('citasLocales') || '[]') as Cita[]
    
    const citasCombinadas = [...citasBackend]
    for (const citaLocal of citasLocales) {
      if (!citasCombinadas.some(c => c.id === citaLocal.id)) {
        citasCombinadas.push(citaLocal)
      }
    }
    
    console.log('Citas obtenidas - Backend:', citasBackend.length, '| Locales:', citasLocales.length, '| Total:', citasCombinadas.length)
    return citasCombinadas
  } catch (error) {
    console.error('Error al obtener citas:', error)
    const citasLocales = JSON.parse(localStorage.getItem('citasLocales') || '[]') as Cita[]
    console.log('Devolviendo citas del localStorage:', citasLocales.length)
    return citasLocales
  }
}

export const obtenerCitasPorPaciente = async (correoElectronico: string): Promise<Cita[]> => {
  try {
    const citas = await obtenerCitas()
    const citasDelPaciente = citas.filter(cita => cita.pacienteCorreo === correoElectronico)
    console.log(`Citas del paciente ${correoElectronico}:`, citasDelPaciente.length)
    return citasDelPaciente
  } catch (error) {
    console.error('Error al obtener citas del paciente:', error)
    const citasLocales = JSON.parse(localStorage.getItem('citasLocales') || '[]') as Cita[]
    return citasLocales.filter(cita => cita.pacienteCorreo === correoElectronico)
  }
}

export const cancelarCita = async (citaId: string): Promise<boolean> => {
  try {
    const response = await fetch(API_URL + '/' + citaId, {
      method: 'DELETE',
      headers: {
        'Content-Type': 'application/json',
      },
    })

    if (response.ok) {
      eliminarCitaLocalmente(citaId)
      return true
    }
    return false
  } catch (error) {
    console.error('Error al cancelar cita:', error)
    eliminarCitaLocalmente(citaId)
    return true
  }
}

export const cancelarCitaPaciente = async (citaId: string): Promise<{ success: boolean; message: string }> => {
  try {
    const response = await fetch(API_URL + '/' + citaId + '/paciente', {
      method: 'DELETE',
      headers: {
        'Content-Type': 'application/json',
      },
    })

    const responseText = await response.text()
    
    if (response.ok) {
      eliminarCitaLocalmente(citaId)
      return { success: true, message: 'Cita cancelada exitosamente' }
    }
    
    if (response.status === 401) {
      return { success: false, message: 'Debe iniciar sesión para cancelar citas' }
    }
    
    if (response.status === 403) {
      return { success: false, message: responseText || 'No tiene permiso para cancelar esta cita' }
    }
    
    if (response.status === 404) {
      return { success: false, message: 'Cita no encontrada' }
    }
    
    return { success: false, message: responseText || 'Error al cancelar la cita' }
  } catch (error) {
    console.error('Error al cancelar cita:', error)
    return { success: false, message: 'Error de conexión al cancelar la cita' }
  }
}

const eliminarCitaLocalmente = (citaId: string) => {
  try {
    const citasLocales = JSON.parse(localStorage.getItem('citasLocales') || '[]') as Cita[]
    const citasFiltradas = citasLocales.filter(c => c.id !== citaId)
    localStorage.setItem('citasLocales', JSON.stringify(citasFiltradas))
    console.log('Cita eliminada del localStorage')
  } catch (error) {
    console.error('Error al eliminar cita localmente:', error)
  }
}

