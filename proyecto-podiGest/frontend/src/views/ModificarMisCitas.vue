<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import SideBar from '../components/SideBar.vue'

const router = useRouter();
const BASE_URL = 'http://localhost:8080/api';

const citas = ref<any[]>([]);
const isLoading = ref(true);
const hoyFecha = ref('');
const isCollapsed = ref(false)

const toggleSidebar = () => {
  isCollapsed.value = !isCollapsed.value
}

const citaSeleccionada = ref<any | null>(null);
const nuevaFecha = ref('');
const nuevaHora = ref('');

const estadoModal = ref<string | null>(null);
const mensajeModal = ref('');
const isExito = ref(false);

function goToMainPage() {
  router.push('/mis-citas')
}

onMounted(() => {
  establecerFechaMinima();
  cargarCitasPaciente();
});

function establecerFechaMinima() {
  const hoy = new Date();
  const año = hoy.getFullYear();
  const mes = String(hoy.getMonth() + 1).padStart(2, '0');
  const día = String(hoy.getDate()).padStart(2, '0');
  hoyFecha.value = `${año}-${mes}-${día}`;
}

async function cargarCitasPaciente() {
  isLoading.value = true;
  try {
    const response = await fetch(`${BASE_URL}/citas/paciente/propias`, {
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
    if (errorMessage.includes("sesión")) {
      estadoModal.value = 'formulario';
    }
  } finally {
    isLoading.value = false;
  }
}

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
    const response = await fetch(`${BASE_URL}/citas/${citaSeleccionada.value.id}/paciente`, {
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
        mensajeModal.value = "ERROR: Fecha y hora no disponible. Por favor, elija otra.";
      } else if (response.status === 403) {
        mensajeModal.value = `ERROR: ${errorBody || 'No tienes permiso para modificar esta cita.'}`;
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
  cargarCitasPaciente();
}
</script>

<template>
  <div class="flex">
    <SideBar :is-collapsed="isCollapsed" @toggle="toggleSidebar" />

    <main :class="[
      'transition-all duration-300 p-6 overflow-auto bg-blue-150 min-h-screen',
      isCollapsed ? 'ml-20' : 'ml-64'
    ]">
      <div
        class="flex min-h-screen bg-linear-to-br border border-blue-300 bg-blue-200/50 rounded-2xl p-10 transition-all duration-300">
        <div class="bg-white rounded-xl shadow-2xl overflow-hidden w-full max-w-4xl mx-auto mt-10">
          <header class="p-8 text-center text-white bg-linear-to-r bg-blue-500/90">
            <h2 class="text-5xl font-bold">Modificar Mis Citas</h2>
          </header>

          <section v-if="isLoading" class="p-6 text-center">
            <p class="text-lg text-gray-600">Cargando tus citas...</p>
          </section>

          <section v-else-if="citas.length === 0" class="p-6 text-center">
            <p class="text-lg text-gray-500">No tienes citas agendadas para modificar.</p>
            <button @click="goToMainPage"
                class="mt-6 inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white hover:-translate-y-0.5 bg-blue-500 hover:bg-blue-600 transition-colors">
                Volver al Menu Principal
              </button>
          </section>

          <section v-else class="p-8">
            <p class="mb-6 font-semibold text-gray-700 text-lg">Selecciona la cita que deseas modificar:</p>
            <div class="space-y-4">
              <article v-for="cita in citas" :key="cita.id"
                class="p-5 bg-white border border-gray-200 rounded-lg shadow-md flex justify-between items-center hover:shadow-lg">
                <div>
                  <p class="font-bold text-xl text-blue-700">{{ cita.especialista }}</p>
                  <p class="text-gray-600 mt-1">Fecha: {{ cita.fecha }} | Hora: {{ cita.hora }}</p>
                  <p class="text-sm text-gray-500 mt-1">Estado: {{ cita.estado }}</p>
                </div>
                <button @click="abrirConfirmacion(cita)"
                  :disabled="cita.estado === 'cancelada'"
                  class="ml-6 px-6 py-2 text-white font-semibold rounded-md shadow-lg bg-linear-to-r bg-blue-500 hover:bg-blue-600 hover:-translate-y-0.5 disabled:opacity-50 disabled:cursor-not-allowed">
                  Modificar
                </button>
              </article>
            </div>
          </section>
        </div>

        <div v-if="estadoModal === 'confirmacion'"
          class="fixed inset-0 bg-blue-100/50 backdrop-blur-sm flex items-center justify-center z-50">
          <div class="bg-white rounded-3xl shadow-2xl w-full max-w-md">
            <header class="p-5 text-center text-white bg-linear-to-r from-blue-500 to-indigo-600 rounded-t-3xl">
              <h3 class="text-2xl font-semibold">Modificar Cita</h3>
            </header>
            <div class="p-6">
              <p class="mb-6 text-gray-700 text-center">
                ¿Deseas modificar tu cita con
                <span class="font-bold text-indigo-600">{{ citaSeleccionada.especialista }}</span>,
                actualmente agendada para el {{ citaSeleccionada.fecha }} a las {{ citaSeleccionada.hora }}?
              </p>
              <div class="flex justify-end space-x-3 mt-4">
                <button @click="estadoModal = null; citaSeleccionada = null"
                  class="px-5 py-2 text-gray-700 bg-gray-200 rounded-lg font-semibold hover:bg-gray-300 hover:-translate-y-0.5">
                  Cancelar
                </button>
                <button @click="estadoModal = 'formulario'"
                  class="px-5 py-2 text-white font-semibold rounded-lg shadow-md bg-linear-to-r from-blue-500 to-indigo-600 hover:from-blue-600 hover:-translate-y-0.5">
                  OK (Modificar)
                </button>
              </div>
            </div>
          </div>
        </div>

        <div v-if="estadoModal === 'formulario'"
          class="fixed inset-0 bg-blue-100/50 backdrop-blur-sm flex items-center justify-center z-50">
          <div class="bg-white rounded-3xl shadow-2xl w-full max-w-md">
            <header class="p-5 text-center text-white bg-linear-to-r from-blue-500 to-indigo-600 rounded-t-3xl">
              <h3 class="text-2xl font-semibold">Reprogramar Cita</h3>
            </header>

            <form @submit.prevent="verificarDisponibilidad" class="p-6">
              <div v-if="mensajeModal" :class="[
                'p-4 mb-4 rounded-lg font-medium',
                isExito ? 'bg-green-100 text-green-800 border border-green-300' : 'bg-red-100 text-red-800 border border-red-300'
              ]">
                {{ mensajeModal }}
              </div>

              <p v-if="!isExito && mensajeModal" class="mb-4 text-center">
                <button @click="resetFormularioErrores" type="button"
                  class="mt-2 px-4 py-2 text-white bg-gray-500 rounded-lg font-semibold hover:bg-gray-600 hover:-translate-y-0.5">
                  Presionar OK para continuar
                </button>
              </p>

              <div v-if="!mensajeModal || !isExito">
                <div class="mb-4">
                  <label for="nuevaFecha" class="block text-sm font-medium text-gray-700 mb-2">Nueva Fecha</label>
                  <input type="date" id="nuevaFecha" v-model="nuevaFecha" :min="hoyFecha"
                    class="w-full p-3 border border-gray-300 rounded-lg shadow-sm focus:border-blue-500 focus:ring-1 focus:ring-blue-500"
                    required />
                </div>

                <div class="mb-6">
                  <label for="nuevaHora" class="block text-sm font-medium text-gray-700 mb-2">Nueva Hora</label>
                  <input type="time" id="nuevaHora" v-model="nuevaHora"
                    class="w-full p-3 border border-gray-300 rounded-lg shadow-sm focus:border-blue-500 focus:ring-1 focus:ring-blue-500"
                    required />
                </div>

                <div class="flex justify-between space-x-3">
                  <button type="button" @click="estadoModal = null; citaSeleccionada = null"
                    class="px-5 py-2 text-gray-700 bg-red-100 rounded-lg font-semibold hover:bg-red-200 hover:-translate-y-0.5">
                    Cerrar
                  </button>

                  <div class="flex space-x-3">
                    <button type="button" @click="estadoModal = 'confirmacion'"
                      class="px-5 py-2 text-gray-700 bg-gray-200 rounded-lg font-semibold hover:bg-gray-300 hover:-translate-y-0.5">
                      Regresar
                    </button>

                    <button type="submit"
                      class="px-5 py-2 text-white font-semibold rounded-lg shadow-md bg-linear-to-r from-blue-500 to-indigo-600 hover:from-blue-600 hover:-translate-y-0.5">
                      Verificar y Confirmar
                    </button>
                  </div>
                </div>
              </div>
            </form>
          </div>
        </div>

        <div v-if="estadoModal === 'exito'"
          class="fixed inset-0 bg-blue-100/50 backdrop-blur-sm flex items-center justify-center z-50">
          <div class="bg-white rounded-3xl shadow-2xl w-full max-w-md">
            <header class="p-5 text-center text-white bg-linear-to-r from-green-500 to-emerald-600 rounded-t-3xl">
              <h3 class="text-2xl font-semibold">¡Éxito!</h3>
            </header>

            <div class="p-6 text-center">
              <p class="mb-6 font-semibold text-green-700">
                Tu cita ha sido modificada con éxito para el {{ nuevaFecha }} a las {{ nuevaHora }}.
              </p>
              <p class="mb-6 text-gray-700">¿Quieres modificar otra cita?</p>

              <div class="flex justify-center space-x-4 mt-4">
                <button type="button" @click="volverALista"
                  class="px-5 py-2 text-white font-semibold rounded-lg shadow-md bg-linear-to-r from-blue-500 to-indigo-600 hover:from-blue-600 hover:-translate-y-0.5">
                  Sí (Modificar Otra)
                </button>

                <button type="button" @click="$router.push('/mis-citas')"
                  class="px-5 py-2 text-gray-700 bg-gray-200 rounded-lg font-semibold hover:bg-gray-300 hover:-translate-y-0.5">
                  No (Ir a Menú)
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>

</template>
