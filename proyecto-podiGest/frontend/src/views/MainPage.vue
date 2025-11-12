<script setup lang="ts">
import { ref } from 'vue'
import Carrousel from '../components/Carrousel.vue'
import SideBar from '../components/SideBar.vue'

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
      <div class="bg-blue-200/50 rounded-lg shadow-lg p-6 max-w-3xl mx-auto mt-8 border border-blue-300">
        <h2 class="text-2xl font-bold text-blue-500 mb-4">Bienvenido a la Clínica Podológica Avanzada</h2>
        <p class="text-gray-700 leading-relaxed mb-2">
          Gabinete Podológico, tu centro de referencia para el cuidado integral de los pies en Caracas, Venezuela.
          Entendemos que la salud de tus pies es fundamental para tu calidad de vida diaria y tu bienestar general.
          Nuestra misión es proporcionarte diagnósticos precisos y tratamientos personalizados, utilizando la tecnología
          más avanzada del sector, para que puedas caminar sin dolor y con total confianza.
          En Gabinete Podológico, combinamos un enfoque profesional y humano para garantizar que cada paso que des sea
          firme y saludable.
          <strong>Agenda tu cita hoy</strong> y empieza a cuidar de tus pies con los mejores especialistas.
        </p>
      </div>
      <Carrousel class="mt-8"/>
      <div class="bg-blue-200/50 rounded-lg shadow-lg p-6 max-w-3xl mx-auto mt-8 border border-blue-300">
        <h2 class="text-2xl font-bold text-blue-500 mb-4">Sobre Nosotros</h2>
        <p class="text-gray-700 leading-relaxed mb-2">
          Contamos con un equipo de especialistas con más de 3 años de experiencia en
          el sector de la podología. Nuestra clínica ha crecido con la filosofía
          inquebrantable de tratar a cada paciente de manera holística, entendiendo y
          abordando la causa raíz del problema, no solo el síntoma superficial. Nos
          enorgullecemos de un enfoque que combina la calidez humana con la
          excelencia médica.
          Nuestro equipo está formado por podólogos altamente cualificados y
          colegiados, con especializaciones en áreas como podología deportiva y pie
          diabético. La formación continua es un pilar fundamental en nuestra clínica,
          permitiéndonos invertir constantemente en la capacitación de nuestro personal
          y en la adquisición de equipos de vanguardia.
          Esto nos permite ofrecer tratamientos de máxima eficacia, mínimamente
          invasivos y libres de dolor. Rompemos con la idea de que la podología debe
          ser un proceso doloroso; nuestro objetivo es tu bienestar total, asegurando que
          tu salud podal esté en manos expertas y cuidadosas.&quot;
        </p>
      </div>
    </main>
  </div>
</template>