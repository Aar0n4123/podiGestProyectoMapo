<script setup lang="ts">
import { useRoute } from 'vue-router'
import { defineProps, defineEmits, onMounted, ref, computed } from 'vue'
import {
  HomeIcon,
  UserIcon,
  CogIcon,
  Bars3Icon,
  BellIcon,
  SquaresPlusIcon,
  DocumentIcon,
  XMarkIcon,
  PencilIcon,
  EyeIcon
} from '@heroicons/vue/24/outline'
import { useNotificationCount } from '../composables/useNotificationCount'

const props = defineProps<{ isCollapsed: boolean }>()
const emit = defineEmits(['toggle'])

const route = useRoute()
const { notificationCount, isMuted, loadNotificationCount } = useNotificationCount()

// Interfaz del usuario
interface Usuario {
  cedula: string
  nombre: string
  apellido: string
  fechaNacimiento: string | null | undefined
  correoElectronico: string
  rol: string
}

// Estado del usuario
const usuario = ref<Usuario | null>(null)
const cargando = ref(true)
const errorCarga = ref('')

// Función para cargar perfil desde backend
const cargarPerfil = async () => {
  cargando.value = true
  errorCarga.value = ''
  try {
    const response = await fetch("http://localhost:8080/api/usuarios", { cache: 'no-store' })
    if (response.ok) {
      usuario.value = await response.json()
    } else {
      errorCarga.value = response.status === 401
        ? "No hay una sesión activa. Por favor, inicia sesión."
        : `Error ${response.status}: ${response.statusText}`
      usuario.value = null
    }
  } catch (error) {
    errorCarga.value = "Error de red. No se pudo conectar al servidor."
    console.error("Error al cargar perfil:", error)
    usuario.value = null
  } finally {
    cargando.value = false
  }
}

// ✅ Computed para obtener el rol dinámicamente
const userRole = computed(() => usuario.value?.rol?.toLowerCase() || '')

// Items con restricción de roles
const navItems = [
  { name: 'Inicio', to: '/mainpage', icon: HomeIcon, roles: ['especialista', 'paciente'] },
  { name: 'Perfil', to: '/profile', icon: UserIcon, roles: ['especialista', 'paciente'] },
  { name: 'Notificaciones', to: '/notifications', icon: BellIcon, roles: ['especialista', 'paciente'] },
  { name: 'Citas', to: '/mis-citas', icon: SquaresPlusIcon, roles: ['paciente'] },
  { name: 'Atención al Cliente', to: '/configuration', icon: CogIcon, roles: ['especialista', 'paciente'] },
  { name: 'Información', to: '/information', icon: DocumentIcon, roles: ['especialista', 'paciente'] },
  { name: 'Consultar Citas ', to: '/consultar-citas-esp', icon: EyeIcon, roles: ['especialista'] },
  { name: 'Modificar Citas ', to: '/modificar-citas-esp', icon: PencilIcon, roles: ['especialista'] },
  { name: 'Salir', to: '/', icon: XMarkIcon, roles: ['especialista', 'paciente'] },
]

const isActive = (path: string) => route.path === path

// Al montar, cargar perfil y notificaciones
onMounted(() => {
  cargarPerfil()
  loadNotificationCount()
  setInterval(loadNotificationCount, 30000)
})
</script>


<template>
  <aside :class="[
    'fixed top-0 left-0 h-screen bg-gray-900 text-white flex flex-col z-50 transition-all duration-300',
    props.isCollapsed ? 'w-20' : 'w-64'
  ]">
    <!-- Header -->
    <div class="flex items-center justify-between p-4 border-b border-blue-700">
      <span v-if="!props.isCollapsed" class="text-xl font-bold">PodiGest</span>
      <button @click="emit('toggle')" class="text-white hover:text-blue-500 flex">
        <Bars3Icon class="w-6 h-6" />
      </button>
    </div>

    <!-- Navigation -->
    <nav class="flex-1 overflow-y-auto">
      <ul>
        <li v-for="item in navItems.filter(i => i.roles.includes(userRole))" :key="item.name">
          <RouterLink :to="item.to" class="group flex items-center gap-3 px-4 py-3 transition-colors duration-200"
            :class="[
              isActive(item.to) ? 'bg-gray-800' : '',
              item.name === 'Salir' ? 'hover:bg-red-500/90' : 'hover:bg-blue-500'
            ]">

            <div class="relative">
              <component :is="item.icon" class="text-white group-hover:text-black w-5 h-5" />
              <!-- Badge de notificaciones (solo se muestra si no está silenciado) -->
              <span v-if="item.name === 'Notificaciones' && notificationCount > 0 && !isMuted"
                class="absolute -top-1 -right-1 bg-red-600 text-white text-xs font-bold rounded-full h-4 w-4 flex items-center justify-center">
                {{ notificationCount > 9 ? '9+' : notificationCount }}
              </span>
            </div>
            <span v-if="!props.isCollapsed" class="text-white group-hover:text-black transition-all duration-200">
              {{ item.name }}
            </span>
          </RouterLink>
        </li>
      </ul>
    </nav>
  </aside>
</template>