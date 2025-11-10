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
      'transition-all duration-300 p-6 overflow-auto bg-amber-150 min-h-screen',
      isCollapsed ? 'ml-20' : 'ml-64'
    ]">
      <div class="max-w-4xl mx-auto">
        <button @click="router.push('/configuration')" class="bg-amber-400 text-gray-800 px-4 py-2 rounded mb-4">Volver</button>
        <h1 class="text-4xl font-bold text-gray-800 mb-6">Podologia Deportiva</h1>
        <div class="bg-white rounded-lg shadow-lg overflow-hidden mb-6">
          <img src="/images/podologia-deportiva.jpg" alt="Podologia Deportiva" class="w-full h-64 object-cover" />
        </div>
        <div class="bg-white rounded-lg shadow-lg p-6">
          <p class="text-gray-700 leading-relaxed">
            Esta especialidad se centra en el análisis biomecánico del pie y la marcha en atletas, para prevenir lesiones, mejorar el rendimiento y tratar dolencias como fascitis plantar, tendinitis o sobrecargas musculares. Incluye estudios de pisada, recomendaciones de calzado y diseño de plantillas deportivas.
          </p>
        </div>
      </div>
    </main>
  </div>
</template>