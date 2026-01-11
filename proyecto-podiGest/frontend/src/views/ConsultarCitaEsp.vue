<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import SideBar from '../components/SideBar.vue'

const router = useRouter();



const BASE_URL = 'http://localhost:8080/api';

// --- ESTADOS REACTIVOS ---
const citas = ref<any[]>([]);
const isLoading = ref(true);
const expandedCitaId = ref<number | null>(null);
const isCollapsed = ref(false)

const toggleSidebar = () => {
  isCollapsed.value = !isCollapsed.value
}

function goToMainPage() {
  router.push('/mainpage')
}
// --- HOOKS Y MÉTODOS DE CARGA ---

onMounted(() => {
  cargarCitasEspecialista();
});

// Función para obtener las citas del especialista autenticado
async function cargarCitasEspecialista() {
  isLoading.value = true;
  try {
    // Endpoint utilizado: /citas/propias (citas del usuario logueado)
    const response = await fetch(`${BASE_URL}/citas/propias`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include'
    });

    if (!response.ok) {
      // Si la sesión expira o no hay permisos, capturamos el error
      const errorText = await response.text();
      throw new Error(response.status === 401 ? "Tu sesión ha expirado o no tienes permisos." : errorText || "Error al cargar citas.");
    }

    const allCitas = await response.json();
    citas.value = allCitas.filter((cita: any) => cita.estado && cita.estado.toLowerCase() === 'pendiente');

  } catch (error: any) {
    console.error("Error al cargar citas:", error.message);

    // Aquí podrías redirigir o mostrar un mensaje de error global
    if (error.message.includes("sesión")) {
      alert("Error de sesión. Serás redirigido."); // Usar un modal en una app real
      router.push('/login');
    }
  } finally {
    isLoading.value = false;
  }
}

// --- LÓGICA DE LA VISTA (Acordeón y Filtro) ---

// Función para alternar el detalle de la cita
function toggleDetails(citaId: number) {
  expandedCitaId.value = expandedCitaId.value === citaId ? null : citaId;
}

// Función que filtra y formatea los detalles de la cita para la vista expandida
function getFilteredDetails(cita: any): [string, any][] {
  const forbiddenKeys = [
    'id',
    'pacienteId', // Asumimos que también hay un ID de paciente
    'pacienteNombre',
    'fecha',
    'hora',
    'especialidadBuscada', // Excluir por instrucción
    'especialista' // Excluir por instrucción
  ];

  const details: Record<string, any> = {};

  for (const key in cita) {
    if (Object.prototype.hasOwnProperty.call(cita, key) && !forbiddenKeys.includes(key)) {

      let displayKey = key;

      // Traducciones a un español amigable para el usuario
      switch (key) {
        case 'motivo':
          displayKey = 'Motivo de la Cita';
          break;
        case 'duracion':
          displayKey = 'Duración Estimada (min)';
          break;
        case 'emailPaciente':
          displayKey = 'Email del Paciente';
          break;
        case 'telefonoPaciente':
          displayKey = 'Teléfono del Paciente';
          break;
        case 'estadoCita':
          displayKey = 'Estado Actual';
          break;
        default:
          // Si hay otras claves, las mostramos capitalizando (ej: "notasInternas" -> "Notas internas")
          displayKey = key.replace(/([A-Z])/g, ' $1').replace(/^./, str => str.toUpperCase());
          break;
      }

      details[displayKey] = cita[key];
    }
  }

  // Convertir el objeto a un array de tuplas [key, value] para v-for
  return Object.entries(details);
}
</script>


<template>
  <div class="flex">
    <SideBar :is-collapsed="isCollapsed" @toggle="toggleSidebar" />

    <main :class="[
      'transition-all duration-300 p-6 overflow-auto bg-blue-150 min-h-screen',
      isCollapsed ? 'ml-20' : 'ml-64'
    ]">
      <div class="min-h-screen border-2 rounded-2xl border-blue-300 bg-blue-200/50 p-8 ">
        <div class="max-w-4xl mx-auto bg-white shadow-2xl rounded-xl overflow-hidden">

          <!-- Encabezado -->
          <header class="p-8 text-white text-center bg-linear-to-r bg-blue-500/90">
            <h1 class="text-3xl font-bold tracking-tight">Citas Agendadas</h1>
            <p class="mt-1 text-blue-100">Consulta y gestiona tus citas programadas.</p>
          </header>

          <div class="p-6">
            <!-- Estado de Carga -->
            <div v-if="isLoading" class="text-center p-12">
              <svg class="animate-spin h-8 w-8 text-blue-500 mx-auto" xmlns="http://www.w3.org/2000/svg" fill="none"
                viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path class="opacity-75" fill="currentColor"
                  d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z">
                </path>
              </svg>
              <p class="mt-3 text-lg text-gray-600">Cargando citas...</p>
            </div>

            <!-- Estado Vacío -->
            <div v-else-if="citas.length === 0"
              class="text-center p-12 border border-dashed border-gray-300 rounded-lg bg-gray-50">
              <svg class="w-12 h-12 text-gray-400 mx-auto" fill="none" stroke="currentColor" viewBox="0 0 24 24"
                xmlns="http://www.w3.org/2000/svg">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-3 7h3m-3 4h3m-6-4h.01M9 16h.01">
                </path>
              </svg>
              <p class="mt-4 text-xl font-semibold text-gray-700">¡Genial! No tienes citas pendientes.</p>
              <p class="mt-2 text-gray-500">Todo tu calendario está libre.</p>
              <button @click="goToMainPage"
                class="mt-6 inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white hover:-translate-y-0.5 bg-blue-500 hover:bg-blue-600 transition-colors">
                Volver al Menu Principal
              </button>

            </div>

            <!-- Lista de Citas (Acordeón) -->
            <div v-else class="space-y-4">
              <p class="text-sm text-gray-500 mb-6">Haz clic en una cita para ver los detalles completos.</p>

              <div v-for="cita in citas" :key="cita.id"
                class="border border-gray-200 rounded-lg shadow-md transition-all duration-300 overflow-hidden"
                :class="{ 'ring-2 ring-cyan-500 shadow-xl': expandedCitaId === cita.id }">
                <!-- Cabecera de la Cita -->
                <button @click="toggleDetails(cita.id)"
                  class="w-full flex justify-between items-center p-5 text-left bg-white hover:bg-gray-50 transition-colors focus:outline-none">
                  <div>
                    <p class="text-lg font-semibold text-blue-700">
                      Cita con: {{ cita.pacienteNombre }}
                    </p>
                    <p class="text-sm text-gray-500 mt-1">
                      {{ cita.fecha }} a las <span class="font-medium text-gray-700">{{ cita.hora }}</span>
                    </p>
                  </div>

                  <!-- Icono de Acordeón -->
                  <svg
                    :class="['w-5 h-5 text-gray-600 transition-transform duration-300', { 'rotate-180': expandedCitaId === cita.id }]"
                    fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"></path>
                  </svg>
                </button>

                <!-- Contenido Detallado -->
                <transition name="accordion">
                  <div v-if="expandedCitaId === cita.id" class="p-5 border-t border-gray-200 bg-gray-50">
                    <dl class="space-y-2 text-sm">

                      <div v-for="[key, value] in getFilteredDetails(cita)" :key="key"
                        class="flex justify-between border-b border-gray-200 pb-1">
                        <dt class="font-medium text-gray-600">{{ key }}:</dt>
                        <dd class="text-gray-800 font-semibold text-right">{{ value }}</dd>
                      </div>

                      <div v-if="Object.keys(getFilteredDetails(cita)).length === 0"
                        class="text-center py-4 text-gray-500">
                        No hay detalles adicionales que mostrar.
                      </div>
                    </dl>
                  </div>
                </transition>
              </div>
            </div>

          </div>
        </div>
      </div>
    </main>
  </div>

</template>

<style scoped>
/* Transición simple para el acordeón */
.accordion-enter-active,
.accordion-leave-active {
  transition: all 0.3s ease-in-out;
  max-height: 200px;
  /* Altura máxima razonable para el efecto */
}

.accordion-enter-from,
.accordion-leave-to {
  max-height: 0;
  opacity: 0;
  padding-top: 0;
  padding-bottom: 0;
}
</style>