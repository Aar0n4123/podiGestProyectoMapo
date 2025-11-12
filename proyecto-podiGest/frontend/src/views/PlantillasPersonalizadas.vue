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
        <h1 class="text-4xl font-bold text-gray-800 mb-6">plantillas personalizadas</h1>
        <div class="bg-white rounded-lg shadow-lg overflow-hidden mb-6">
          <img src="/images/plantilla.jpg" alt="plantillas personalizadas" class="w-full h-64 object-cover" />
        </div>
        <div class="bg-white rounded-lg shadow-lg p-6">
          <p class="text-gray-700 leading-relaxed">
            Son órtesis plantares diseñadas a medida tras un estudio biomecánico. Ayudan a corregir la pisada, distribuir mejor las cargas, aliviar dolores articulares y mejorar el rendimiento físico. Se adaptan a las necesidades específicas de cada paciente, ya sea por deporte, trabajo o patologías crónicas.
          </p>
        </div>
      </div>
    </main>
  </div>
</template>