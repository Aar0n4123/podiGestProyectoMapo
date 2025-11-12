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
        <h1 class="text-4xl font-bold text-gray-800 mb-6">Evaluacion integral del pie</h1>
        <div class="bg-white rounded-lg shadow-lg overflow-hidden mb-6">
          <img src="/images/Evaluacion_Integral.jpg" alt="Evaluacion integral del pie" class="w-full h-64 object-cover" />
        </div>
        <div class="bg-white rounded-lg shadow-lg p-6">
          <p class="text-gray-700 leading-relaxed">
            La evaluación integral del pie es un estudio completo que permite analizar la estructura, función y biomecánica del pie y su relación con el resto del cuerpo. Este proceso es clave para detectar alteraciones que pueden causar dolor, afectar la marcha o predisponer a lesiones.
          </p>
        </div>
      </div>
    </main>
  </div>
</template>