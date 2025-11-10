<script setup lang="ts">
import { useRoute } from 'vue-router'
import { defineProps, defineEmits } from 'vue'
import {
  HomeIcon,
  UserIcon,
  CogIcon,
  Bars3Icon,
  BellIcon,
  SquaresPlusIcon,
  DocumentIcon,
  XMarkIcon,
  HeartIcon
} from '@heroicons/vue/24/outline'

const props = defineProps<{ isCollapsed: boolean }>()
const emit = defineEmits(['toggle'])

const route = useRoute()

// Rol actual del usuario (esto vendría de tu login/backend)
const userRole = 'cliente' // especialista o cliente

// Items con restricción de roles
const navItems = [
  { name: 'Inicio', to: '/mainpage', icon: HomeIcon, roles: ['especialista','cliente'] },
  { name: 'Perfil', to: '/profile', icon: UserIcon, roles: ['especialista','cliente'] },
  { name: 'Notificaciones', to: '/notifications', icon: BellIcon, roles: ['especialista','cliente'] },
  { name: 'Citas', to: '/appointments', icon: SquaresPlusIcon, roles: ['especialista','cliente'] },
  { name: 'Atención al Cliente', to: '/configuration', icon: CogIcon, roles: ['especialista','cliente'] },
  { name: 'Información', to: '/information', icon: DocumentIcon, roles: ['especialista','cliente'] },
  { name: 'Gestión de Especialista', to: '/especialist', icon: HeartIcon, roles: ['especialista'] },
  { name: 'Salir', to: '/', icon: XMarkIcon, roles: ['especialista','cliente'] },
]

const isActive = (path: string) => route.path === path
</script>

<template>
  <aside
    :class="[
      'fixed top-0 left-0 h-screen bg-gray-900 text-white flex flex-col z-50 transition-all duration-300',
      props.isCollapsed ? 'w-20' : 'w-64'
    ]"
  >
    <!-- Header -->
    <div class="flex items-center justify-between p-4 border-b border-amber-700">
      <span v-if="!props.isCollapsed" class="text-xl font-bold">PodiGest</span>
      <button @click="emit('toggle')" class="text-white hover:text-amber-500 flex">
        <Bars3Icon class="w-6 h-6" />
      </button>
    </div>

    <!-- Navigation -->
    <nav class="flex-1 overflow-y-auto">
      <ul>
        <li v-for="item in navItems.filter(i => i.roles.includes(userRole))" :key="item.name">
          <RouterLink
            :to="item.to"
            class="group flex items-center gap-3 px-4 py-3 hover:bg-amber-500 transition-colors duration-200"
            :class="{ 'bg-gray-800': isActive(item.to) }"
          >
            <component :is="item.icon" class="text-white group-hover:text-black w-5 h-5" />
            <span
              v-if="!props.isCollapsed"
              class="text-white group-hover:text-black transition-all duration-200"
            >
              {{ item.name }}
            </span>
          </RouterLink>
        </li>
      </ul>
    </nav>
  </aside>
</template>