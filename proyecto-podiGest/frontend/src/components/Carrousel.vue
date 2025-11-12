<script setup>
import { ref } from 'vue'

const images = [
  '/images/Doctores.jpg',
  '/images/Doctora.jpg',
  '/images/podologia-deportiva.jpg',
]

const currentIndex = ref(0)

const next = () => {
  currentIndex.value = (currentIndex.value + 1) % images.length
}

const prev = () => {
  currentIndex.value = (currentIndex.value - 1 + images.length) % images.length
}

const goTo = (index) => {
  currentIndex.value = index
}
</script>

<template>
  <div class="relative w-200 h-120 overflow-hidden rounded-lg shadow-lg">
    <!-- Imagen actual -->
    <img
      :src="images[currentIndex]"
      alt="Imagen del carrusel"
      class="w-full h-full object-cover transition-opacity duration-500"
    />

    <!-- Botón anterior -->
    <button
      @click="prev"
      class="absolute top-1/2 left-4 transform -translate-y-1/2 bg-white bg-opacity-70 hover:bg-opacity-100 text-gray-800 px-3 py-1 rounded-full shadow"
    >
      ‹
    </button>

    <!-- Botón siguiente -->
    <button
      @click="next"
      class="absolute top-1/2 right-4 transform -translate-y-1/2 bg-white bg-opacity-70 hover:bg-opacity-100 text-gray-800 px-3 py-1 rounded-full shadow"
    >
      ›
    </button>

    <!-- Indicadores -->
    <div class="absolute bottom-4 left-1/2 transform -translate-x-1/2 flex gap-2">
      <span
        v-for="(img, index) in images"
        :key="index"
        @click="goTo(index)"
        class="w-3 h-3 rounded-full cursor-pointer"
        :class="index === currentIndex ? 'bg-white' : 'bg-gray-400'"
      ></span>
    </div>
  </div>
</template>