<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import SideBar from '../components/SideBar.vue'

const router = useRouter()
const isCollapsed = ref(false)

const toggleSidebar = () => {
  isCollapsed.value = !isCollapsed.value
}
</script>

<template>
  <div class="flex">
    <SideBar :is-collapsed="isCollapsed" @toggle="toggleSidebar" />
    <main :class="[
      'transition-all duration-300 p-6 overflow-auto bg-blue-150 min-h-screen',
      isCollapsed ? 'ml-20' : 'ml-64'
    ]">
      <div class="max-w-4xl mx-auto">
        <button @click="router.push('/configuration')" class="bg-blue-400 text-gray-800 px-4 py-2 rounded mb-4">Volver</button>
        <h1 class="text-4xl font-bold text-gray-800 mb-6">Podologia General</h1>
        <div class="bg-white rounded-lg shadow-lg overflow-hidden mb-6">
          <img src="/images/podologia_General.jpg" alt="Podologia General" class="w-full h-64 object-cover" />
        </div>
        <div class="bg-white rounded-lg shadow-lg p-6">
          <p class="text-gray-700 leading-relaxed">
            La podología general se enfoca en el diagnóstico, tratamiento y prevención de afecciones comunes del pie, como callosidades, uñas encarnadas, hongos, verrugas plantares y alteraciones en la piel. Es ideal para personas de todas las edades que buscan mantener la salud podal y prevenir complicaciones.
          </p>
        </div>
      </div>
    </main>
  </div>
</template>