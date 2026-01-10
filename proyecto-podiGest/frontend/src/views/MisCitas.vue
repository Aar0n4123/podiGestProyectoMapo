<template>
  <div class="flex">
    <div>
      <SideBar :is-collapsed="isCollapsed" @toggle="toggleSidebar" />
    </div>

    <main
      :class="[
        'bg-blue-200/50 rounded-lg shadow-lg p-6 max-w-3xl mx-auto mt-8 border border-blue-300',
        isCollapsed ? 'ml-20' : 'ml-64'
      ]"
    >
      <!-- Acceso denegado -->
      <div
        v-if="!usuarioAutenticado"
        class="bg-red-100 text-red-700 border border-red-300 p-5 rounded-lg mb-5"
      >
        <h3 class="text-xl font-bold mb-2">Acceso Denegado</h3>
        <p class="mb-3">Debes iniciar sesión para acceder a las citas.</p>
        <router-link
          to="/login"
          class="inline-block mt-3 px-4 py-2 rounded bg-linear-to-r from-indigo-400 to-purple-600 text-white font-semibold text-sm hover:-translate-y-0.5 hover:shadow-lg transition"
        >
          Ir al Login
        </router-link>
      </div>

      <!-- Contenido principal -->
      <div v-else>
        <!-- Header -->
        <div class="flex justify-center items-center mb-10">
          <h2 class="text-5xl font-bold text-blue-500">Gestión de Citas</h2>
        </div>

        <!-- Grid de menú -->
        <div class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-8 max-w-6xl mx-auto">
          <!-- Agendar -->
          <router-link
            to="/agendar-cita"
            class="bg-white rounded-xl p-8 shadow-md transition transform text-center flex flex-col items-center relative hover:-translate-y-2 hover:shadow-xl"
          >
            <div class="text-6xl mb-5">📅</div>
            <h3 class="text-xl font-semibold text-gray-800 mb-3">Agendar Cita</h3>
            <p class="text-sm text-gray-600">Agenda una nueva cita con un especialista</p>
          </router-link>

          <!-- Consultar -->
          <router-link
            to="/consultar-citas"
            class="bg-white rounded-xl p-8 shadow-md transition transform text-center flex flex-col items-center relative hover:-translate-y-2 hover:shadow-xl"
          >
            <div class="text-6xl mb-5">🔍</div>
            <h3 class="text-xl font-semibold text-gray-800 mb-3">Consultar Citas Disponibles</h3>
            <p class="text-sm text-gray-600">Consulta las citas disponibles</p>
          </router-link>

          <!-- Mis Citas -->
          <router-link
            to="/historial-citas"
            class="bg-white rounded-xl p-8 shadow-md transition transform text-center flex flex-col items-center relative hover:-translate-y-2 hover:shadow-xl"
          >
            <div class="text-6xl mb-5">📋</div>
            <h3 class="text-xl font-semibold text-gray-800 mb-3">Mis Citas</h3>
            <p class="text-sm text-gray-600">Visualiza todas tus citas agendadas</p>
          </router-link>

          <!-- Modificar -->
          <router-link
            to="/modificar-mis-citas"
            class="bg-white rounded-xl p-8 shadow-md transition transform text-center flex flex-col items-center relative hover:-translate-y-2 hover:shadow-xl"
          >
            <div class="text-6xl mb-5">✏️</div>
            <h3 class="text-xl font-semibold text-gray-800 mb-3">Modificar Citas</h3>
            <p class="text-sm text-gray-600">Modifica tus citas agendadas</p>
          </router-link>

          <!-- Cancelar -->
          <router-link
            to="/cancelar-citas"
            class="bg-white rounded-xl p-8 shadow-md transition transform text-center flex flex-col items-center relative hover:-translate-y-2 hover:shadow-xl"
          >
            <div class="text-6xl mb-5">❌</div>
            <h3 class="text-xl font-semibold text-gray-800 mb-3">Cancelar Citas</h3>
            <p class="text-sm text-gray-600">Cancela tus citas agendadas</p>
          </router-link>
        </div>
      </div>
    </main>
  </div>
</template>


<script lang="ts">
import { defineComponent } from 'vue'
import SideBar from '../components/SideBar.vue'

export default defineComponent({
  name: 'MisCitas',
  components: {
    SideBar
  },
  data() {
    return {
      usuarioAutenticado: false,
      isCollapsed: false
    }
  },
  mounted() {
    this.verificarSesion()
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
    toggleSidebar() {
      this.isCollapsed = !this.isCollapsed
    }
  }
})
</script>

