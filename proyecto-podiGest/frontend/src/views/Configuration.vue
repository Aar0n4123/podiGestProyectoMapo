<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import SideBar from '../components/SideBar.vue'

const router = useRouter()

const isCollapsed = ref(false)
const activeTab = ref('servicios')

const toggleSidebar = () => {
  isCollapsed.value = !isCollapsed.value
}

const services = [
  { name: 'Podologia General', image: '/images/podologia_General.jpg', path: '/configuration/podologia-general' },
  { name: 'Podologia pediatrica', image: '/images/podologia-pediatrica.jpg', path: '/configuration/podologia-pediatrica' },
  { name: 'Quiropodia', image: '/images/quiropodia.jpg', path: '/configuration/quiropodia' },
  { name: 'Plantillas personalizadas', image: '/images/plantilla.jpg', path: '/configuration/plantillas-personalizadas' },
  { name: 'Evaluacion integral del pie', image: '/images/Evaluacion_Integral.jpg', path: '/configuration/evaluacion-integral' },
  { name: 'Podologia Deportiva', image: '/images/podologia-deportiva.jpg', path: '/configuration/podologia-deportiva' }
]
</script>

<template>
  <div class="flex">
    <SideBar :is-collapsed="isCollapsed" @toggle="toggleSidebar" />
    <main :class="[
      'transition-all duration-300 p-6 overflow-auto bg-blue-150 min-h-screen',
      isCollapsed ? 'ml-20' : 'ml-64'
    ]">
      <!-- Sub-navbar -->
      <nav class="flex bg-gray-800 text-white rounded-lg mb-6 overflow-hidden">
        <button
          @click="activeTab = 'servicios'"
          :class="[
            'px-6 py-3 transition-colors duration-200 flex-1',
            activeTab === 'servicios' ? 'bg-blue-500 text-black' : 'hover:bg-blue-500 hover:text-black'
          ]"
        >
          Servicios
        </button>
        <button
          @click="activeTab = 'especialistas'"
          :class="[
            'px-6 py-3 transition-colors duration-200 flex-1',
            activeTab === 'especialistas' ? 'bg-blue-500 text-black' : 'hover:bg-blue-500 hover:text-black'
          ]"
        >
          Especialistas
        </button>
      </nav>

      <!-- Content based on active tab -->
      <div v-if="activeTab === 'servicios'">
        <h2 class="text-3xl font-bold text-gray-800 mb-6">Nuestros Servicios</h2>
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          <div
            v-for="service in services"
            :key="service.name"
            class="bg-white rounded-lg shadow-lg overflow-hidden hover:shadow-xl transition-shadow duration-300 cursor-pointer"
            @click="router.push(service.path)"
          >
            <img :src="service.image" :alt="service.name" class="w-full h-48 object-cover" />
            <div class="p-4">
              <h3 class="text-xl font-semibold text-gray-800">{{ service.name }}</h3>
            </div>
          </div>
        </div>
      </div>

      <div v-else-if="activeTab === 'especialistas'">
        <h2 class="text-3xl font-bold text-gray-800 mb-6">Nuestros Especialistas</h2>
        <p class="text-gray-600">Información de especialistas próximamente.</p>
      </div>
    </main>
  </div>
</template>