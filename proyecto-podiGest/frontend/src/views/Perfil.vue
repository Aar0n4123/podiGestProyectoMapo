<script setup lang="ts">
import { ref, onMounted } from 'vue';
import SideBar from '../components/SideBar.vue';

interface Usuario {
  cedula: string;
  nombre: string;
  apellido: string;
  fechaNacimiento: string | null | undefined;
  correoElectronico: string;
  rol: string;
}

const isCollapsed = ref(false);
const toggleSidebar = () => {
  isCollapsed.value = !isCollapsed.value;
};

const usuario = ref<Usuario | null>(null);
const errorCarga = ref<string>('');
const cargando = ref<boolean>(true);


const formatFecha = (fechaString: string | null | undefined): string => {
  if (!fechaString) {
    return 'Fecha no disponible';
  }


  const parts = fechaString.split('-');

  if (parts.length !== 3) {
    return 'Formato de fecha inválido';
  }


  const monthNames = [
    "enero", "febrero", "marzo", "abril", "mayo", "junio",
    "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"
  ];


  const monthIndex = parseInt(parts[1], 10) - 1;
  const day = parts[2];
  const year = parts[0];

  const monthName = monthNames[monthIndex];


  if (!monthName) {
    return 'Mes inválido';
  }


  return `${day} de ${monthName} de ${year}`;
};


const cargarPerfil = async () => {
  cargando.value = true;
  errorCarga.value = '';
  try {
    const urlBackend = "http://localhost:8080/api/usuarios";

    const response = await fetch(urlBackend, { cache: 'no-store' });

    if (response.ok) {

      const data = await response.json();

      usuario.value = data;


    } else {
      if (response.status === 401) {
        errorCarga.value = "No hay una sesión activa. Por favor, inicia sesión.";
      } else {
        errorCarga.value = `Error ${response.status}: ${response.statusText}`;
      }
      usuario.value = null;
    }
  } catch (error) {
    errorCarga.value = "Error de red. No se pudo conectar al servidor.";
    console.error("Error al cargar perfil:", error);
    usuario.value = null;
  } finally {
    cargando.value = false;
  }
};


onMounted(() => {
  cargarPerfil();
});
</script>

<template>
  <div class="flex">
    <SideBar :is-collapsed="isCollapsed" @toggle="toggleSidebar" />

    <main :class="[
      'transition-all duration-300 p-6 overflow-auto min-h-screen',
      'bg-blue-200/50 rounded-lg shadow-lg mx-auto border border-blue-300',
      isCollapsed ? 'ml-20' : 'ml-64'
    ]">

      <div class="w-full max-w-3xl mx-auto">
        <div class="bg-white w-full p-8 rounded-2xl shadow-xl">

          <div v-if="cargando" class="flex flex-col items-center justify-center p-10">
            <div class="w-16 h-16 border-4 border-blue-300 border-t-blue-600 rounded-full animate-spin"></div>
            <span class="mt-4 text-gray-500 text-lg">Cargando perfil...</span>
          </div>

          <div v-else-if="errorCarga" class="text-center p-6">
            <svg class="mx-auto h-16 w-16 text-red-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            <h3 class="mt-4 text-2xl font-semibold text-gray-800">Error al Cargar</h3>
            <p class="mt-2 text-gray-600">{{ errorCarga }}</p>
            <a href="/login" class="mt-6 inline-block bg-blue-600 text-white font-bold py-2 px-6 rounded-lg hover:bg-blue-500 transition-colors">
              Ir a Iniciar Sesión
            </a>
          </div>

          <div v-else-if="usuario" class="text-center">
            <h1 class="text-4xl font-bold text-blue-500">
              Tu Perfil
            </h1>
            <p class="mt-2 text-lg text-gray-600">Aquí están tus datos personales.</p>
            <div class="mt-8 text-left space-y-5">

              <div class="flex flex-col md:flex-row p-4 bg-gray-50 rounded-lg border border-gray-200">
                <span class="font-semibold text-blue-500 w-full md:w-1/3">Cédula:</span>
                <span class="text-gray-800 w-full md:w-2/3">{{ usuario.cedula }}</span>
              </div>

              <div class="flex flex-col md:flex-row p-4 bg-gray-50 rounded-lg border border-gray-200">
                <span class="font-semibold text-blue-500 w-full md:w-1/3">Nombre:</span>
                <span class="text-gray-800 w-full md:w-2/3">{{ usuario.nombre }}</span>
              </div>

              <div class="flex flex-col md:flex-row p-4 bg-gray-50 rounded-lg border border-gray-200">
                <span class="font-semibold text-blue-500 w-full md:w-1/3">Apellido:</span>
                <span class="text-gray-800 w-full md:w-2/3">{{ usuario.apellido }}</span>
              </div>

              <div class="flex flex-col md:flex-row p-4 bg-gray-50 rounded-lg border border-gray-200">
                <span class="font-semibold text-blue-500 w-full md:w-1/3">Correo:</span>
                <span class="text-gray-800 w-full md:w-2/3">{{ usuario.correoElectronico }}</span>
              </div>

              <div class="flex flex-col md:flex-row p-4 bg-gray-50 rounded-lg border border-gray-200">
                <span class="font-semibold text-blue-500 w-full md:w-1/3">Fecha de Nacimiento:</span>
                <span class="text-gray-800 w-full md:w-2/3 mt-2">{{ formatFecha(usuario.fechaNacimiento) }}</span>
              </div>


            </div>
          </div>

        </div>
      </div>

    </main>
  </div>
</template>