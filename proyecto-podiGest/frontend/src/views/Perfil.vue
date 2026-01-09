<script setup lang="ts">
import { ref, onMounted } from 'vue';
import SideBar from '../components/SideBar.vue';

interface Usuario {
  cedula: string;
  nombre: string;
  apellido: string;
  fechaNacimiento: string | null | undefined;
  correoElectronico: string;
  contrasenia: string;
  rol: string;
}

const isCollapsed = ref(false);
const toggleSidebar = () => {
  isCollapsed.value = !isCollapsed.value;
};

const usuario = ref<Usuario | null>(null);
const errorCarga = ref<string>('');
const cargando = ref<boolean>(true);

// --- LÓGICA DE FECHAS ---
const formatFecha = (fechaString: string | null | undefined): string => {
  if (!fechaString) return 'Fecha no disponible';
  // Si viene como lista [1990, 1, 1], el JSON.stringify lo convierte a string o hay que manejarlo.
  // Pero asumiendo que tu backend lo manda como string "YYYY-MM-DD" o similar:
  const parts = fechaString.split('-');
  if (parts.length !== 3) return fechaString;
  const monthNames = [
    "enero", "febrero", "marzo", "abril", "mayo", "junio",
    "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"
  ];
  const monthIndex = parseInt(parts[1], 10) - 1;
  const day = parts[2];
  const year = parts[0];
  const monthName = monthNames[monthIndex];
  if (!monthName) return 'Mes inválido';
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
    usuario.value = null;
  } finally {
    cargando.value = false;
  }
};

onMounted(() => {
  cargarPerfil();
});

// --- LÓGICA PARA MODIFICAR PERFIL ---

const editando = ref(false);
const mostrarConfirmacion = ref(false);
const usuarioEditado = ref<Usuario>({
  cedula: '',
  nombre: '',
  apellido: '',
  fechaNacimiento: '',
  correoElectronico: '',
  contrasenia: '',
  rol: ''
});

const activarEdicion = () => {
  if (usuario.value) {
    usuarioEditado.value = { ...usuario.value };
    editando.value = true;
  }
};

const cancelarEdicion = () => {
  editando.value = false;
};

const confirmarGuardado = () => {
  mostrarConfirmacion.value = true;
};

const cancelarConfirmacion = () => {
  mostrarConfirmacion.value = false;
};

const guardarCambios = async () => {
  if (!usuarioEditado.value) return;

  try {
    const response = await fetch("http://localhost:8080/api/usuarios", {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(usuarioEditado.value)
    });

    if (response.ok) {
      const dataActualizada = await response.json();
      usuario.value = dataActualizada;
      editando.value = false;
      mostrarConfirmacion.value = false;
      alert("¡Perfil actualizado con éxito!");
    } else {
      alert("Error al actualizar el perfil.");
    }
  } catch (error) {
    console.error(error);
    alert("No se pudo conectar con el servidor.");
  }
};

// --- LÓGICA PARA ELIMINAR PERFIL (NUEVO) ---
const eliminarPerfil = async () => {
  // 1. Preguntar confirmación
  if(!confirm("¿Estás SEGURO de que deseas eliminar tu cuenta? \n\nEsta acción borrará tus datos permanentemente y no se puede deshacer.")) {
    return;
  }

  try {
    // 2. Hacer la petición DELETE al backend
    const response = await fetch("http://localhost:8080/api/usuarios", {
      method: 'DELETE'
    });

    // 3. Verificar respuesta
    if (response.ok) {
      alert("Tu cuenta ha sido eliminada correctamente.");
      // 4. Redirigir al Login
      window.location.href = "/";
    } else {
      const textoError = await response.text();
      alert("Error del servidor: " + textoError);
    }
  } catch (error) {
    console.error(error);
    alert("Error de conexión. No se pudo contactar con el servidor.");
  }
};
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
            <h3 class="mt-4 text-2xl font-semibold text-gray-800">Error al Cargar</h3>
            <p class="mt-2 text-gray-600">{{ errorCarga }}</p>
          </div>

          <div v-else-if="usuario" class="text-center">
            <h1 class="text-4xl font-bold text-blue-500">Tu Perfil</h1>
            <p class="mt-2 text-lg text-gray-600">Actualiza tus datos o contraseña.</p>

            <div class="mt-8 text-left space-y-5">

              <div class="flex flex-col md:flex-row p-4 bg-gray-50 rounded-lg border border-gray-200 items-center">
                <span class="font-semibold text-blue-500 w-full md:w-1/3">Cédula:</span>
                <div class="w-full md:w-2/3">
                  <span class="text-gray-800 font-bold opacity-70 cursor-not-allowed" title="La cédula no se puede modificar">
                    {{ usuario.cedula }}
                  </span>
                  <span v-if="editando" class="text-xs text-red-400 ml-2">(No editable)</span>
                </div>
              </div>

              <div class="flex flex-col md:flex-row p-4 bg-gray-50 rounded-lg border border-gray-200 items-center">
                <span class="font-semibold text-blue-500 w-full md:w-1/3">Nombre:</span>
                <div class="w-full md:w-2/3">
                  <span v-if="!editando" class="text-gray-800">{{ usuario.nombre }}</span>
                  <input v-else v-model="usuarioEditado.nombre" type="text" class="w-full p-2 border border-blue-300 rounded focus:outline-none focus:ring-2 focus:ring-blue-500" />
                </div>
              </div>

              <div class="flex flex-col md:flex-row p-4 bg-gray-50 rounded-lg border border-gray-200 items-center">
                <span class="font-semibold text-blue-500 w-full md:w-1/3">Apellido:</span>
                <div class="w-full md:w-2/3">
                  <span v-if="!editando" class="text-gray-800">{{ usuario.apellido }}</span>
                  <input v-else v-model="usuarioEditado.apellido" type="text" class="w-full p-2 border border-blue-300 rounded focus:outline-none focus:ring-2 focus:ring-blue-500" />
                </div>
              </div>

              <div class="flex flex-col md:flex-row p-4 bg-gray-50 rounded-lg border border-gray-200 items-center">
                <span class="font-semibold text-blue-500 w-full md:w-1/3">Correo:</span>
                <div class="w-full md:w-2/3">
                  <span v-if="!editando" class="text-gray-800">{{ usuario.correoElectronico }}</span>
                  <input v-else v-model="usuarioEditado.correoElectronico" type="email" class="w-full p-2 border border-blue-300 rounded focus:outline-none focus:ring-2 focus:ring-blue-500" />
                </div>
              </div>

              <div class="flex flex-col md:flex-row p-4 bg-gray-50 rounded-lg border border-gray-200 items-center">
                <span class="font-semibold text-blue-500 w-full md:w-1/3">Contraseña:</span>
                <div class="w-full md:w-2/3">
                  <span v-if="!editando" class="text-gray-800 tracking-widest">••••••••</span>
                  <input v-else v-model="usuarioEditado.contrasenia" type="text" placeholder="Nueva contraseña" class="w-full p-2 border border-blue-300 rounded focus:outline-none focus:ring-2 focus:ring-blue-500" />
                </div>
              </div>

              <div class="flex flex-col md:flex-row p-4 bg-gray-50 rounded-lg border border-gray-200 items-center">
                <span class="font-semibold text-blue-500 w-full md:w-1/3">Fecha Nacimiento:</span>
                <div class="w-full md:w-2/3">
                  <span v-if="!editando" class="text-gray-800">{{ formatFecha(usuario.fechaNacimiento) }}</span>
                  <input v-else v-model="usuarioEditado.fechaNacimiento" type="date" class="w-full p-2 border border-blue-300 rounded focus:outline-none focus:ring-2 focus:ring-blue-500" />
                </div>
              </div>

              <div class="flex justify-end gap-4 mt-6 pt-4 border-t border-gray-200">
                <template v-if="!editando">
                  <button @click="eliminarPerfil" class="px-6 py-2 bg-red-100 text-red-600 font-semibold rounded-lg hover:bg-red-200 transition-colors">
                    Eliminar Perfil
                  </button>
                  <button @click="activarEdicion" class="px-6 py-2 bg-blue-600 text-white font-semibold rounded-lg hover:bg-blue-700 transition-colors shadow-md">
                    Modificar Perfil
                  </button>
                </template>

                <template v-else>
                  <button @click="cancelarEdicion" class="px-6 py-2 bg-gray-300 text-gray-700 font-semibold rounded-lg hover:bg-gray-400 transition-colors">
                    Cancelar
                  </button>
                  <button @click="confirmarGuardado" class="px-6 py-2 bg-green-500 text-white font-semibold rounded-lg hover:bg-green-600 transition-colors shadow-md">
                    Guardar Cambios
                  </button>
                </template>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>

    <div v-if="mostrarConfirmacion" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
      <div class="bg-white rounded-2xl shadow-2xl p-8 max-w-sm mx-4">
        <div class="text-center">
          <div class="mx-auto flex items-center justify-center h-12 w-12 rounded-full bg-yellow-100 mb-4">
            <svg class="h-6 w-6 text-yellow-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4v2m0 0a9 9 0 11-9-9m9 9a9 9 0 119-9"></path>
            </svg>
          </div>
          <h3 class="text-lg font-semibold text-gray-900 mt-4">Confirmar cambios</h3>
          <p class="mt-2 text-sm text-gray-600">
            ¿Estás seguro de que deseas guardar los cambios en tu perfil? Esta acción actualizará todos los datos.
          </p>
        </div>
        <div class="flex gap-4 mt-8">
          <button 
            @click="cancelarConfirmacion"
            class="flex-1 px-4 py-2 bg-gray-200 text-gray-800 font-semibold rounded-lg hover:bg-gray-300 transition-colors"
          >
            Cancelar
          </button>
          <button 
            @click="guardarCambios"
            class="flex-1 px-4 py-2 bg-green-500 text-white font-semibold rounded-lg hover:bg-green-600 transition-colors shadow-md"
          >
            Confirmar y Guardar
          </button>
        </div>
      </div>
    </div>
  </div>
</template>