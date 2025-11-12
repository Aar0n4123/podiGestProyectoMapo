<template>
  <div class="flex">
    <div :class="[
      'min-h-screen flex justify-center items-start p-10 flex-1 transition-all duration-300',
      'bg-gradient-to-br from-indigo-500 to-purple-600',
      // Ajusta 'ml-0' si SideBar está en uso (ej: isCollapsed ? 'ml-20' : 'ml-64')
      'ml-0'
    ]">
      <div class="bg-white rounded-xl shadow-2xl overflow-hidden w-full max-w-4xl mt-10">

        <div class="p-8 text-center text-white bg-gradient-to-r from-indigo-500 to-purple-700">
          <h2 class="text-3xl font-semibold">Mis Citas Agendadas</h2>
        </div>

        <div v-if="isLoading" class="p-6 text-center">
          <p class="text-lg text-gray-600">Cargando citas...</p>
        </div>

        <div v-else-if="citas.length === 0" class="p-6 text-center">
          <p class="text-lg text-gray-500">No tienes citas agendadas.</p>
          <router-link to="/" class="mt-4 inline-flex items-center justify-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-gray-500 hover:bg-gray-600 transition-colors">
            Volver al Menú
          </router-link>
        </div>

        <div v-else class="p-8">
          <p class="mb-6 font-semibold text-gray-700 text-lg">Selecciona la cita que deseas modificar:</p>

          <div class="space-y-4">
            <div
                v-for="cita in citas"
                :key="cita.id"
                class="p-5 bg-white border border-gray-200 rounded-lg shadow-md flex justify-between items-center transition-shadow hover:shadow-lg"
            >
              <div>
                <p class="font-bold text-xl text-indigo-700">Paciente: {{ cita.pacienteNombre }}</p>
                <p class="text-gray-600 mt-1">Fecha Actual: {{ cita.fecha }} | Hora Actual: {{ cita.hora }}</p>
              </div>
              <button
                  @click="abrirConfirmacion(cita)"
                  class="px-6 py-2 border border-transparent text-white font-semibold rounded-md shadow-lg bg-gradient-to-r from-indigo-500 to-purple-600 hover:from-indigo-600 hover:to-purple-700 transition-all transform hover:scale-105"
              >
                Seleccionar
              </button>
            </div>
          </div>
        </div>

      </div>
    </div>

    <!-- MODAL DE CONFIRMACIÓN -->
    <div
        v-if="estadoModal === 'confirmacion'"
        class="fixed inset-0 bg-gray-900 bg-opacity-50 flex items-center justify-center z-50"
    >
      <div class="bg-white rounded-xl shadow-2xl overflow-hidden w-full max-w-md">
        <div class="p-5 text-center text-white bg-gradient-to-r from-indigo-500 to-purple-700">
          <h3 class="text-2xl font-semibold">Modificar Cita</h3>
        </div>
        <div class="p-6">
          <p class="mb-6 text-gray-700 text-center">
            ¿Deseas modificar la cita con el paciente
            <span class="font-bold text-indigo-600">{{ citaSeleccionada.pacienteNombre }}</span>,
            actualmente agendada para el {{ citaSeleccionada.fecha }} a las {{ citaSeleccionada.hora }}?
          </p>

          <div class="flex justify-end space-x-3 mt-4">
            <!-- Botón para CANCELAR y volver a la lista -->
            <button
                type="button"
                @click="estadoModal = null; citaSeleccionada = null;"
                class="px-5 py-2 text-gray-700 bg-gray-200 rounded-lg font-semibold hover:bg-gray-300 transition-colors"
            >
              Cancelar
            </button>
            <!-- Botón para CONTINUAR al formulario -->
            <button
                type="button"
                @click="estadoModal = 'formulario'"
                class="px-5 py-2 text-white font-semibold rounded-lg shadow-md bg-gradient-to-r from-indigo-500 to-purple-600 hover:from-indigo-600 hover:to-purple-700 transition-colors"
            >
              Presion OK (Modificar)
            </button>
          </div>
        </div>
      </div>
    </div>


    <!-- MODAL DE FORMULARIO -->
    <div
        v-if="estadoModal === 'formulario'"
        class="fixed inset-0 bg-gray-900 bg-opacity-50 flex items-center justify-center z-50"
    >
      <div class="bg-white rounded-xl shadow-2xl overflow-hidden w-full max-w-md">
        <div class="p-5 text-center text-white bg-gradient-to-r from-indigo-500 to-purple-700">
          <h3 class="text-2xl font-semibold">Reprogramar Cita</h3>
        </div>

        <form @submit.prevent="verificarDisponibilidad" class="p-6">

          <div v-if="mensajeModal" :class="[
            'p-4 mb-4 rounded-lg font-medium',
            isExito ? 'bg-green-100 text-green-800 border border-green-300' : 'bg-red-100 text-red-800 border border-red-300'
          ]">
            {{ mensajeModal }}
          </div>

          <!-- Si hay un error, mostramos el botón de OK para limpiar el modal -->
          <p v-if="!isExito && mensajeModal" class="mb-4 text-center">
            <button @click="resetFormularioErrores" type="button" class="mt-2 px-4 py-2 text-white bg-gray-500 rounded-lg font-semibold hover:bg-gray-600 transition-colors">
              Presionar OK para continuar
            </button>
          </p>

          <div v-if="!mensajeModal || !isExito">
            <div class="mb-4">
              <label for="nuevaFecha" class="block text-sm font-medium text-gray-700 mb-2">Nueva Fecha</label>
              <input
                  type="date"
                  id="nuevaFecha"
                  v-model="nuevaFecha"
                  :min="hoyFecha"
                  class="w-full p-3 border border-gray-300 rounded-lg shadow-sm focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500 transition-shadow"
                  required
              />
            </div>
            <div class="mb-6">
              <label for="nuevaHora" class="block text-sm font-medium text-gray-700 mb-2">Nueva Hora</label>
              <input
                  type="time"
                  id="nuevaHora"
                  v-model="nuevaHora"
                  class="w-full p-3 border border-gray-300 rounded-lg shadow-sm focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500 transition-shadow"
                  required
              />
            </div>

            <div class="flex justify-between space-x-3">
              <!-- Botón agregado para SALIR del flujo de modificación y volver a la lista -->
              <button
                  type="button"
                  @click="estadoModal = null; citaSeleccionada = null;"
                  class="px-5 py-2 text-gray-700 bg-red-100 rounded-lg font-semibold hover:bg-red-200 transition-colors"
              >
                Cerrar
              </button>

              <div class="flex space-x-3">
                <!-- Botón para volver a la CONFIRMACIÓN (si es necesario) -->
                <button
                    type="button"
                    @click="estadoModal = 'confirmacion'"
                    class="px-5 py-2 text-gray-700 bg-gray-200 rounded-lg font-semibold hover:bg-gray-300 transition-colors"
                >
                  Regresar
                </button>
                <!-- Botón de SUBMIT -->
                <button
                    type="submit"
                    class="px-5 py-2 text-white font-semibold rounded-lg shadow-md bg-gradient-to-r from-indigo-500 to-purple-600 hover:from-indigo-600 hover:to-purple-700 transition-colors"
                >
                  Verificar y Confirmar
                </button>
              </div>

            </div>
          </div>
        </form>
      </div>
    </div>

    <!-- MODAL DE ÉXITO -->
    <div
        v-if="estadoModal === 'exito'"
        class="fixed inset-0 bg-gray-900 bg-opacity-50 flex items-center justify-center z-50"
    >
      <div class="bg-white rounded-xl shadow-2xl overflow-hidden w-full max-w-md">
        <div class="p-5 text-center text-white bg-green-500">
          <h3 class="text-2xl font-semibold">¡Éxito!</h3>
        </div>
        <div class="p-6 text-center">
          <p class="mb-6 font-semibold text-green-700">
            La cita ha sido modificada con éxito para el {{ nuevaFecha }} a las {{ nuevaHora }}.
          </p>
          <p class="mb-6 text-gray-700">¿Quieres modificar otra cita?</p>

          <div class="flex justify-center space-x-4 mt-4">
            <!-- Botón para volver a la lista de citas (recarga la lista) -->
            <button
                type="button"
                @click="volverALista"
                class="px-5 py-2 text-white font-semibold rounded-lg shadow-md bg-gradient-to-r from-indigo-500 to-purple-600 hover:from-indigo-600 hover:to-purple-700 transition-colors"
            >
              Sí (Modificar Otra)
            </button>
            <!-- Botón para ir al menú principal -->
            <button
                type="button"
                @click="$router.push('/')"
                class="px-5 py-2 text-gray-700 bg-gray-200 rounded-lg font-semibold hover:bg-gray-300 transition-colors"
            >
              No (Ir a Menú)
            </button>
          </div>
        </div>
      </div>
    </div>


  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();
const BASE_URL = 'http://localhost:8080/api';

// --- ESTADOS DE LA PÁGINA ---
const citas = ref<any[]>([]);
const isLoading = ref(true);
const hoyFecha = ref('');

// --- ESTADOS DE LA MODIFICACIÓN ---
const citaSeleccionada = ref<any | null>(null);
const nuevaFecha = ref('');
const nuevaHora = ref('');

// --- ESTADOS DEL MODAL Y FLUJO ---
// null | 'confirmacion' | 'formulario' | 'exito'
const estadoModal = ref<string | null>(null);
const mensajeModal = ref('');
const isExito = ref(false);


// --- HOOKS Y MÉTODOS INICIALES ---

onMounted(() => {
  establecerFechaMinima();
  cargarCitasEspecialista();
});

function establecerFechaMinima() {
  const hoy = new Date();
  const año = hoy.getFullYear();
  const mes = String(hoy.getMonth() + 1).padStart(2, '0');
  const día = String(hoy.getDate()).padStart(2, '0');
  hoyFecha.value = `${año}-${mes}-${día}`;
}

async function cargarCitasEspecialista() {
  isLoading.value = true;
  try {
    // Llamando a /citas/propias para obtener solo las citas del especialista logueado
    const response = await fetch(`${BASE_URL}/citas/propias`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include'
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(response.status === 401 ? "Tu sesión ha expirado o no tienes permisos." : errorText || "Error al cargar citas.");
    }

    citas.value = await response.json();

  } catch (error: any) {
    console.error("Error al cargar citas:", error);
    const errorMessage = error.message.includes("expirado") || error.message.includes("permisos")
        ? error.message
        : "No se pudieron cargar tus citas.";

    mensajeModal.value = errorMessage;
    isExito.value = false;
    // Si hay un error de autenticación, lo mostramos en el modal de formulario
    if (errorMessage.includes("sesión")) {
      estadoModal.value = 'formulario';
    }
  } finally {
    isLoading.value = false;
  }
}

// --- LÓGICA DE FLUJO ---

function abrirConfirmacion(cita: any) {
  citaSeleccionada.value = { ...cita };
  nuevaFecha.value = cita.fecha;
  nuevaHora.value = cita.hora;

  estadoModal.value = 'confirmacion';
  mensajeModal.value = '';
  isExito.value = false;
}

function resetFormularioErrores() {
  if (!isExito.value) {
    mensajeModal.value = '';
  }
}

async function verificarDisponibilidad() {
  if (!citaSeleccionada.value) return;

  mensajeModal.value = '';
  isExito.value = false;

  const payload = {
    fecha: nuevaFecha.value,
    hora: nuevaHora.value
  };

  try {
    const response = await fetch(`${BASE_URL}/citas/${citaSeleccionada.value.id}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(payload),
      credentials: 'include'
    });

    if (response.ok) {
      estadoModal.value = 'exito';
    } else {
      let errorBody = await response.text();

      if (response.status === 404) {
        mensajeModal.value = "ERROR: La cita no fue encontrada en el sistema.";
      } else if (response.status === 409) {
        // Asumiendo que el backend devuelve 409 para conflicto de horario
        mensajeModal.value = "ERROR: Fecha y hora no disponible. Por favor, elija otra.";
      } else {
        mensajeModal.value = `ERROR ${response.status}: ${errorBody || 'Ocurrió un error inesperado.'}`;
      }
      isExito.value = false;
    }

  } catch (error: any) {
    console.error("Error al modificar la cita:", error);
    mensajeModal.value = "Error de red. No se pudo conectar con el servidor.";
    isExito.value = false;
  }
}

function volverALista() {
  estadoModal.value = null;
  citaSeleccionada.value = null;
  nuevaFecha.value = '';
  nuevaHora.value = '';
  mensajeModal.value = '';
  isExito.value = false;
  cargarCitasEspecialista();
}
</script>