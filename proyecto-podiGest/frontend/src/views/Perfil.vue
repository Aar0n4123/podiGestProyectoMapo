<script setup lang="ts">

import { ref, onMounted } from 'vue';
import SideBar from '../components/SideBar.vue';


interface Usuario {
  cedula: string;
  nombre: string;
  apellido: string;
  fechaNacimiento: [number, number, number];
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

// Función para formatear la fecha
const formatFecha = (fechaArray: [number, number, number] | undefined): string => {
  if (!fechaArray || fechaArray.length !== 3) {
    return 'Fecha inválida';
  }
  const fecha = new Date(fechaArray[0], fechaArray[1] - 1, fechaArray[2]);
  return fecha.toLocaleDateString('es-ES', {
    day: 'numeric',
    month: 'long',
    year: 'numeric'
  });
};


const cargarPerfil = async () => {
  cargando.value = true;
  errorCarga.value = '';
  try {
    const urlBackend = "http://localhost:8080/api/perfil"; // esta es la ruta de ConsultarPerfilController este no se cambia


    const response = await fetch(urlBackend);


    if (response.ok) {

      usuario.value = await response.json(); // ¡Éxito! Guardamos el usuario

    } else {
      // 4. Manejamos errores HTTP (como 401 o 500)
      if (response.status === 401) {
        // Error 401: No hay sesión activa
        errorCarga.value = "No hay una sesión activa. Por favor, inicia sesión.";
      } else {
        // Otro error del servidor
        errorCarga.value = `Error ${response.status}: ${response.statusText}`;
      }
      usuario.value = null;
    }
  } catch (error) {
    // 5. Manejamos errores de red (ej. no se pudo conectar)
    errorCarga.value = "Error de red. No se pudo conectar al servidor.";
    console.error("Error al cargar perfil:", error);
    usuario.value = null;
  } finally {
    cargando.value = false;
  }
};

// Hook de Vue: Llama a cargarPerfil() cuando el componente se monta
onMounted(() => {
  cargarPerfil();
});

</script>

<template>
  <div class="flex">
    <SideBar :is-collapsed="isCollapsed" @toggle="toggleSidebar" />

    <main :class="[
      'transition-all duration-300 p-6 overflow-auto min-h-screen',
      'bg-amber-100',
      isCollapsed ? 'ml-20' : 'ml-64'
    ]">

      <div class="w-full max-w-3xl mx-auto">
        <div class="bg-white w-full p-8 rounded-2xl shadow-xl">

          <div v-if="cargando" class="flex flex-col items-center justify-center p-10">
            <div class="w-16 h-16 border-4 border-amber-300 border-t-amber-600 rounded-full animate-spin"></div>
            <span class="mt-4 text-gray-700 text-lg">Cargando perfil...</span>
          </div>

          <div v-else-if="errorCarga" class="text-center p-6">
            <svg class="mx-auto h-16 w-16 text-red-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            <h3 class="mt-4 text-2xl font-semibold text-gray-800">Error al Cargar</h3>
            <p class="mt-2 text-gray-600">{{ errorCarga }}</p>
            <a href="/login" class="mt-6 inline-block bg-amber-600 text-white font-bold py-2 px-6 rounded-lg hover:bg-amber-700 transition-colors">
              Ir a Iniciar Sesión
            </a>
          </div>

          <div v-else-if="usuario" class="text-center">
            <h1 class="text-4xl font-bold text-amber-600">
              Tu Perfil
            </h1>
            <p class="mt-2 text-lg text-gray-600">Aquí están tus datos personales.</p>
            <div class="mt-8 text-left space-y-5">

              <div class="flex flex-col md:flex-row p-4 bg-gray-50 rounded-lg border border-gray-200">
                <span class="font-semibold text-amber-700 w-full md:w-1/3">Cédula:</span>
                <span class="text-gray-800 w-full md:w-2/3">{{ usuario.cedula }}</span>
              </div>

              <div class="flex flex-col md:flex-row p-4 bg-gray-50 rounded-lg border border-gray-200">
                <span class="font-semibold text-amber-700 w-full md:w-1/3">Nombre:</span>
                <span class="text-gray-800 w-full md:w-2/3">{{ usuario.nombre }}</span>
              </div>

              <div class="flex flex-col md:flex-row p-4 bg-gray-50 rounded-lg border border-gray-200">
                <span class="font-semibold text-amber-700 w-full md:w-1/3">Apellido:</span>
                <span class="text-gray-800 w-full md:w-2/3">{{ usuario.apellido }}</span>
              </div>

              <div class="flex flex-col md:flex-row p-4 bg-gray-50 rounded-lg border border-gray-200">
                <span class="font-semibold text-amber-700 w-full md:w-1/3">Correo:</span>
                <span class="text-gray-800 w-full md:w-2/3">{{ usuario.correoElectronico }}</span>
              </div>

              <div class="flex flex-col md:flex-row p-4 bg-gray-50 rounded-lg border border-gray-200">
                <span class="font-semibold text-amber-700 w-full md:w-1/3">Nacimiento:</span>
                <span class="text-gray-800 w-full md:w-2/3">{{ formatFecha(usuario.fechaNacimiento) }}</span>
              </div>

              <div class="flex flex-col md:flex-row p-4 bg-gray-50 rounded-lg border border-gray-200">
                <span class="font-semibold text-amber-700 w-full md:w-1/3">Rol:</span>
                <span class="text-gray-800 w-full md:w-2/3 capitalize">{{ usuario.rol || 'No especificado' }}</span>
              </div>

            </div>
          </div>

        </div>
      </div>

    </main>
  </div>
</template>