<script setup lang="ts">
import { ref } from 'vue'
import SideBar from '../components/SideBar.vue'

const isCollapsed = ref(false)
const activeTab = ref('acerca')

const toggleSidebar = () => {
  isCollapsed.value = !isCollapsed.value
}

const faqs = [
  {
    question: '¿Cuál es el horario de atención?',
    answer: 'Atendemos de lunes a viernes de 8:00 AM a 6:00 PM, y sábados de 9:00 AM a 2:00 PM.'
  },
  {
    question: '¿Requiero cita previa?',
    answer: 'Sí, recomendamos agendar cita previa a través de nuestra plataforma para garantizar una mejor atención.'
  },
  {
    question: '¿Qué servicios ofrecen?',
    answer: 'Ofrecemos podología general, pediátrica, deportiva, quiropodia, evaluación integral del pie y plantillas personalizadas.'
  },
  {
    question: '¿Aceptan diferentes métodos de pago?',
    answer: 'Sí, aceptamos efectivo, tarjetas de crédito/débito y transferencias bancarias.'
  },
  {
    question: '¿Cuál es el costo de las consultas?',
    answer: 'Los costos varían según el tipo de servicio. Por favor, contáctenos para más información específica.'
  }
]

const expandedFAQ = ref<number | null>(null)

const toggleFAQ = (index: number) => {
  expandedFAQ.value = expandedFAQ.value === index ? null : index
}
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
          @click="activeTab = 'acerca'"
          :class="[
            'px-6 py-3 transition-colors duration-200 flex-1',
            activeTab === 'acerca' ? 'bg-blue-500 text-black' : 'hover:bg-blue-500 hover:text-black'
          ]"
        >
          Acerca de nosotros
        </button>
        <button
          @click="activeTab = 'ubicacion'"
          :class="[
            'px-6 py-3 transition-colors duration-200 flex-1',
            activeTab === 'ubicacion' ? 'bg-blue-500 text-black' : 'hover:bg-blue-500 hover:text-black'
          ]"
        >
          Ubicación
        </button>
      </nav>

      <!-- Acerca de nosotros -->
      <div v-if="activeTab === 'acerca'">
        <!-- Nosotros -->
        <div class="mb-12">
          <h2 class="text-4xl font-bold text-gray-800 mb-4">Nosotros</h2>
          <div class="bg-white rounded-lg shadow-lg p-8">
            <p class="text-gray-700 text-lg leading-relaxed">
              Somos una institución dedicada a la excelencia en la atención podológica. Con más de 15 años de experiencia 
              en el cuidado de los pies, nos hemos consolidado como líderes en la región. Nuestro equipo está conformado 
              por profesionales altamente capacitados y comprometidos con brindar un servicio de calidad.
            </p>
            <p class="text-gray-700 text-lg leading-relaxed mt-4">
              Utilizamos tecnología de punta y las mejores prácticas en podología para asegurar que cada paciente reciba 
              el mejor tratamiento posible. Nuestro objetivo es contribuir a la salud y bienestar de nuestros pacientes 
              a través de un enfoque integral y personalizado.
            </p>
          </div>
        </div>

        <!-- Nuestra Misión -->
        <div class="mb-12">
          <h2 class="text-4xl font-bold text-gray-800 mb-4">Nuestra Misión</h2>
          <div class="bg-white rounded-lg shadow-lg p-8">
            <p class="text-gray-700 text-lg leading-relaxed">
              Nuestra misión es proporcionar servicios podológicos de excelencia, accesibles y de calidad a todos nuestros 
              pacientes. Nos comprometemos a:
            </p>
            <ul class="list-disc list-inside text-gray-700 text-lg mt-4 space-y-2">
              <li>Brindar diagnósticos precisos y tratamientos efectivos</li>
              <li>Garantizar un ambiente cálido y profesional</li>
              <li>Mantener los más altos estándares de higiene y seguridad</li>
              <li>Educar a nuestros pacientes sobre el cuidado preventivo de los pies</li>
              <li>Innovar continuamente en nuestros servicios y tecnología</li>
            </ul>
          </div>
        </div>

        <!-- Preguntas Frecuentes -->
        <div>
          <h2 class="text-4xl font-bold text-gray-800 mb-4">Preguntas Frecuentes</h2>
          <div class="space-y-4">
            <div
              v-for="(faq, index) in faqs"
              :key="index"
              class="bg-white rounded-lg shadow-lg overflow-hidden"
            >
              <button
                @click="toggleFAQ(index)"
                class="w-full px-6 py-4 text-left font-semibold text-gray-800 bg-gray-100 hover:bg-blue-200 transition-colors duration-200 flex justify-between items-center"
              >
                <span>{{ faq.question }}</span>
                <span class="text-xl">{{ expandedFAQ === index ? '−' : '+' }}</span>
              </button>
              <transition
                enter-active-class="transition-all duration-300"
                leave-active-class="transition-all duration-300"
                enter-from-class="opacity-0 max-h-0"
                enter-to-class="opacity-100 max-h-96"
                leave-from-class="opacity-100 max-h-96"
                leave-to-class="opacity-0 max-h-0"
              >
                <div v-if="expandedFAQ === index" class="px-6 py-4 bg-white border-t border-gray-200">
                  <p class="text-gray-700 text-base leading-relaxed">{{ faq.answer }}</p>
                </div>
              </transition>
            </div>
          </div>
        </div>
      </div>

      <!-- Ubicación -->
      <div v-else-if="activeTab === 'ubicacion'">
        <h2 class="text-4xl font-bold text-gray-800 mb-6">Ubicación</h2>
        
        <!-- Descripción -->
        <div class="bg-white rounded-lg shadow-lg p-8 mb-8">
          <h3 class="text-2xl font-semibold text-gray-800 mb-4">Encuentra nuestras instalaciones</h3>
          <p class="text-gray-700 text-lg leading-relaxed mb-4">
            Nos encontramos ubicados en el corazón de la ciudad, con fácil acceso al transporte público y estacionamiento. 
            Nuestra clínica cuenta con un ambiente cómodo y moderno, diseñado para ofrecerle la mejor experiencia durante 
            su visita.
          </p>
          <div class="text-gray-700 space-y-2">
            <p><strong>Dirección:</strong> Calle Principal 123, Centro Médico Profesional, Piso 5</p>
            <p><strong>Teléfono:</strong> 0424-xxxxxxx | 0414-xxxxxxx</p>
            <p><strong>Email:</strong> info@podigest.com</p>
            <p><strong>Horario:</strong> Lunes a Viernes 8:00 AM - 6:00 PM | Sábado 9:00 AM - 2:00 PM</p>
          </div>
        </div>

          <!-- Mapa -->
        <div class="bg-white rounded-lg shadow-lg overflow-hidden">
          <iframe
              src="https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d2018131.5281201438!2d-62.51022167272291!3d8.910875035804416!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x8c34f07cf89e979d%3A0x66fd85b3fe72ee00!2sDelta%20Amacuro%2C%20Venezuela!5e0!3m2!1sen!2snl!4v1762789738172!5m2!1sen!2snl"
              width="100%"
              height="500"
              style="border: 0"
            loading="lazy"
            referrerpolicy="no-referrer-when-downgrade"
          ></iframe>
        </div>
      </div>
    </main>
  </div>
</template>