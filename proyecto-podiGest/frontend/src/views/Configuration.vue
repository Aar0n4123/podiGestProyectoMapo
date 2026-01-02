<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import SideBar from '../components/SideBar.vue'

// Importamos los iconos necesarios para Especialistas
import {
  StarIcon as StarSolid,
  ChatBubbleLeftRightIcon,
  ChevronDownIcon
} from '@heroicons/vue/24/solid';
import { StarIcon as StarOutline, XMarkIcon } from '@heroicons/vue/24/outline';

const router = useRouter()

// Lógica del Sidebar y Tabs (YA EXISTÍA)
const isCollapsed = ref(false)
const activeTab = ref('servicios') // Pestaña activa por defecto

const toggleSidebar = () => {
  isCollapsed.value = !isCollapsed.value
}

// DATOS DE SERVICIOS (YA EXISTÍAN)
const services = [
  { name: 'Podologia General', image: '/images/podologia_General.jpg', path: '/configuration/podologia-general' },
  { name: 'Podologia pediatrica', image: '/images/podologia-pediatrica.jpg', path: '/configuration/podologia-pediatrica' },
  { name: 'Quiropodia', image: '/images/quiropodia.jpg', path: '/configuration/quiropodia' },
  { name: 'Plantillas personalizadas', image: '/images/plantilla.jpg', path: '/configuration/plantillas-personalizadas' },
  { name: 'Evaluacion integral del pie', image: '/images/Evaluacion_Integral.jpg', path: '/configuration/evaluacion-integral' },
  { name: 'Podologia Deportiva', image: '/images/podologia-deportiva.jpg', path: '/configuration/podologia-deportiva' }
]

// ======================================================================
//  AQUÍ COMIENZA LA LÓGICA DE ESPECIALISTAS (AGREGADO NUEVO)
// ======================================================================

// 1. DATOS DE LOS ESPECIALISTAS
const especialistas = ref([
  {
    id: 1,
    nombre: 'Dra. Marylyn Rojas',
    especialidad: 'Podología General',
    foto: '/images/doctor1.jpg',
    descripcion: 'Egresada con honores de la Universidad Central, cuenta con más de 12 años de experiencia clínica. Ha trabajado como jefa de la unidad de pie diabético en el Hospital Metropolitano y posee un Máster en Dermatología Podológica. Su enfoque empático y preventivo la convierte en la favorita de nuestros pacientes mayores y pediátricos.'
  },
  {
    id: 2,
    nombre: 'Dra. Elsee Aguilar',
    especialidad: 'Podología Deportiva',
    foto: '/images/doctor2.jpg',
    descripcion: 'Especialista en biomecánica con 8 años de trayectoria atendiendo a atletas de alto rendimiento. Realizó estudios de postgrado en Barcelona, España, y ha sido consultora para equipos de fútbol locales. Se dedica al análisis computarizado de la marcha y al diseño de plantillas inteligentes para prevenir lesiones deportivas.'
  }
]);

// 2. LÓGICA DE INTERFAZ (EXPANDIR TEXTO BIO)
const expandedDocId = ref<number | null>(null);

const toggleBio = (id: number) => {
  if (expandedDocId.value === id) {
    expandedDocId.value = null;
  } else {
    expandedDocId.value = id;
  }
};

// 3. LÓGICA DE COMENTARIOS
interface Comentario {
  especialistaId: number;
  autor: string;
  texto: string;
  estrellas: number;
  fecha: string;
}

const comentarios = ref<Comentario[]>([]);
const especialistaSeleccionado = ref<any>(null);
const mostrarModal = ref(false);
const nuevoComentario = ref({ texto: '', estrellas: 0 });
const usuarioActual = ref("Usuario Anónimo");

// Cargar datos al montar
onMounted(async () => {
  const storedComments = localStorage.getItem('podiGest_opiniones');
  if (storedComments) {
    comentarios.value = JSON.parse(storedComments);
  }

  try {
     const response = await fetch("http://localhost:8080/api/usuarios", { cache: 'no-store' });
     if(response.ok) {
        const data = await response.json();
        usuarioActual.value = data.nombre + " " + data.apellido;
     }
  } catch (e) {
    console.log("No se pudo obtener usuario, usando anónimo");
  }
});

// Promedio numérico para estrellas
const obtenerPromedioNumerico = (id: number) => {
  const opiniones = comentarios.value.filter(c => c.especialistaId === id);
  if (opiniones.length === 0) return 0;
  const suma = opiniones.reduce((acc, curr) => acc + curr.estrellas, 0);
  return suma / opiniones.length;
};

// Abrir Modal
const abrirComentarios = (doctor: any) => {
  especialistaSeleccionado.value = doctor;
  nuevoComentario.value = { texto: '', estrellas: 0 };
  mostrarModal.value = true;
};

const cerrarModal = () => {
  mostrarModal.value = false;
  especialistaSeleccionado.value = null;
};

// Publicar
const publicarComentario = () => {
  if (nuevoComentario.value.estrellas === 0) {
    alert("Por favor selecciona una calificación de estrellas.");
    return;
  }
  if (!nuevoComentario.value.texto.trim()) {
    alert("Por favor escribe un comentario.");
    return;
  }

  const nuevaOpinion: Comentario = {
    especialistaId: especialistaSeleccionado.value.id,
    autor: usuarioActual.value,
    texto: nuevoComentario.value.texto,
    estrellas: nuevoComentario.value.estrellas,
    fecha: new Date().toLocaleDateString()
  };

  comentarios.value.unshift(nuevaOpinion);
  localStorage.setItem('podiGest_opiniones', JSON.stringify(comentarios.value));
  nuevoComentario.value = { texto: '', estrellas: 0 };
  alert("¡Gracias por tu opinión!");
};

const comentariosDelDoctor = computed(() => {
  if (!especialistaSeleccionado.value) return [];
  return comentarios.value.filter(c => c.especialistaId === especialistaSeleccionado.value.id);
});
</script>

<template>
  <div class="flex">
    <SideBar :is-collapsed="isCollapsed" @toggle="toggleSidebar" />

    <main :class="[
      'transition-all duration-300 p-6 overflow-auto bg-blue-150 min-h-screen w-full',
      isCollapsed ? 'ml-20' : 'ml-64'
    ]">

      <nav class="flex bg-gray-800 text-white rounded-lg mb-6 overflow-hidden">
        <button
          @click="activeTab = 'servicios'"
          :class="[
            'px-6 py-3 transition-colors duration-200 flex-1 font-bold',
            activeTab === 'servicios' ? 'bg-blue-500 text-black' : 'hover:bg-blue-500 hover:text-black '
          ]"
        >
          Servicios
        </button>
        <button
          @click="activeTab = 'especialistas'"
          :class="[
            'px-6 py-3 transition-colors duration-200 flex-1 font-bold',
            activeTab === 'especialistas' ? 'bg-blue-500 text-black' : 'hover:bg-blue-500 hover:text-black '
          ]"
        >
          Especialistas
        </button>
      </nav>

      <div v-if="activeTab === 'servicios'">
        <h2 class="text-3xl font-bold text-gray-800 mb-6">Nuestros Servicios</h2>
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          <div
            v-for="service in services"
            :key="service.name"
            class="bg-white rounded-lg shadow-lg overflow-hidden hover:shadow-xl transition-shadow duration-300 cursor-pointer hover:-translate-y-0.5 hover:bg-blue-500/50"
            @click="router.push(service.path)"
          >
            <img :src="service.image" :alt="service.name" class="w-full h-48 object-cover" />
            <div class="p-4 ">
              <h3 class="text-xl font-bold text-gray-800 ">{{ service.name }}</h3>
            </div>
          </div>
        </div>
      </div>

      <div v-else-if="activeTab === 'especialistas'">
        <h2 class="text-3xl font-bold text-gray-800 mb-6">Nuestros Especialistas</h2>
        <p class="text-gray-600 mb-6">Conoce a nuestro equipo profesional.</p>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-8 max-w-5xl mx-auto">
          <div v-for="doctor in especialistas" :key="doctor.id"
               class="bg-white rounded-2xl shadow-lg border border-blue-100 overflow-hidden flex flex-col hover:shadow-2xl transition-shadow duration-300">

            <div class="p-6 flex flex-col items-center text-center">
                <div class="w-32 h-32 rounded-full overflow-hidden border-4 border-blue-200 mb-3 shadow-md">
                    <img :src="doctor.foto" :alt="doctor.nombre" class="w-full h-full object-cover bg-gray-200" />
                </div>

                <h2 class="text-2xl font-bold text-gray-800">{{ doctor.nombre }}</h2>
                <p class="text-blue-500 font-medium text-sm uppercase tracking-wide mb-3">{{ doctor.especialidad }}</p>

                <div class="flex items-center gap-1 mb-2 bg-blue-50 px-3 py-1 rounded-full">
                    <div class="flex text-yellow-400">
                        <template v-for="star in 5" :key="star">
                            <StarSolid v-if="star <= Math.round(obtenerPromedioNumerico(doctor.id))" class="w-5 h-5" />
                            <StarOutline v-else class="w-5 h-5 text-gray-300" />
                        </template>
                    </div>
                    <span class="text-xs font-bold text-gray-600 ml-1">
                        ({{ obtenerPromedioNumerico(doctor.id).toFixed(1) }})
                    </span>
                </div>
            </div>

            <div class="px-6 pb-2">
                <button @click="toggleBio(doctor.id)"
                        class="flex items-center justify-between w-full text-left text-sm font-semibold text-blue-600 hover:text-blue-800 transition-colors focus:outline-none group">
                    <span>Sobre el especialista</span>
                    <ChevronDownIcon
                        class="w-5 h-5 transition-transform duration-300"
                        :class="expandedDocId === doctor.id ? 'rotate-180' : ''"
                    />
                </button>
                <div class="overflow-hidden transition-all duration-500 ease-in-out"
                     :class="expandedDocId === doctor.id ? 'max-h-96 opacity-100 mt-2' : 'max-h-0 opacity-0'">
                    <p class="text-gray-600 text-sm leading-relaxed text-justify bg-gray-50 p-3 rounded-lg border border-gray-100">
                        {{ doctor.descripcion }}
                    </p>
                </div>
            </div>

            <div class="p-6 mt-auto border-t border-gray-100">
                <button @click="abrirComentarios(doctor)"
                        class="w-full flex items-center justify-center gap-2 px-6 py-3 bg-blue-600 text-white rounded-xl font-bold hover:bg-blue-700 transition-colors shadow-lg active:scale-95">
                  <ChatBubbleLeftRightIcon class="w-5 h-5" />
                  Ver Opiniones y Calificar
                </button>
            </div>
          </div>
        </div>
      </div>

      <div v-if="mostrarModal" class="fixed inset-0 z-[60] flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm">
        <div class="bg-white rounded-2xl shadow-2xl w-full max-w-2xl max-h-[85vh] overflow-hidden flex flex-col relative animate-fade-in-up">

          <div class="bg-blue-600 p-4 flex justify-between items-center text-white">
              <div class="flex items-center gap-3">
                  <div class="w-10 h-10 rounded-full overflow-hidden border-2 border-white">
                      <img :src="especialistaSeleccionado.foto" class="w-full h-full object-cover" />
                  </div>
                  <div>
                      <h3 class="font-bold text-lg leading-tight">{{ especialistaSeleccionado.nombre }}</h3>
                      <p class="text-blue-100 text-xs">Opiniones y Calificaciones</p>
                  </div>
              </div>
              <button @click="cerrarModal" class="hover:bg-blue-700 p-1 rounded-full transition-colors">
                  <XMarkIcon class="w-6 h-6" />
              </button>
          </div>

          <div class="flex flex-col h-full overflow-hidden bg-gray-50">
            <div class="flex-1 overflow-y-auto p-6 space-y-4">
              <div v-if="comentariosDelDoctor.length === 0" class="flex flex-col items-center justify-center h-full text-gray-400">
                <ChatBubbleLeftRightIcon class="w-12 h-12 mb-2 opacity-20" />
                <p class="italic">Aún no hay opiniones. ¡Sé el primero!</p>
              </div>

              <div v-for="(comment, index) in comentariosDelDoctor" :key="index" class="bg-white p-4 rounded-xl shadow-sm border border-gray-100">
                <div class="flex justify-between items-start mb-2">
                  <div class="flex items-center gap-2">
                      <div class="w-8 h-8 rounded-full bg-blue-100 flex items-center justify-center text-blue-600 font-bold text-xs">
                          {{ comment.autor.charAt(0) }}
                      </div>
                      <span class="font-bold text-gray-800 text-sm">{{ comment.autor }}</span>
                  </div>
                  <span class="text-xs text-gray-400">{{ comment.fecha }}</span>
                </div>
                <div class="flex text-yellow-400 mb-2">
                  <StarSolid v-for="n in 5" :key="n" :class="n <= comment.estrellas ? 'text-yellow-400' : 'text-gray-200'" class="w-4 h-4" />
                </div>
                <p class="text-gray-600 text-sm">{{ comment.texto }}</p>
              </div>
            </div>

            <div class="bg-white p-4 border-t border-gray-200 shadow-[0_-4px_6px_-1px_rgba(0,0,0,0.05)]">
              <p class="text-xs font-bold text-gray-500 uppercase mb-2">Tu Calificación:</p>
              <div class="flex gap-2 mb-3">
                <button v-for="star in 5" :key="star" @click="nuevoComentario.estrellas = star" type="button" class="transition-transform hover:scale-110">
                  <StarSolid v-if="star <= nuevoComentario.estrellas" class="w-8 h-8 text-yellow-400 drop-shadow-sm" />
                  <StarOutline v-else class="w-8 h-8 text-gray-300 hover:text-yellow-400" />
                </button>
              </div>
              <div class="flex gap-2">
                  <input v-model="nuevoComentario.texto"
                        type="text"
                        placeholder="Escribe tu opinión aquí..."
                        class="flex-1 p-3 rounded-lg border border-gray-300 focus:outline-none focus:ring-2 focus:ring-blue-500 text-sm"
                        @keyup.enter="publicarComentario"
                  />
                  <button @click="publicarComentario" class="px-4 py-2 bg-blue-600 text-white font-bold rounded-lg hover:bg-blue-700 transition-colors">
                    Enviar
                  </button>
              </div>
            </div>
          </div>
        </div>
      </div>

    </main>
  </div>
</template>

<style scoped>
@keyframes fade-in-up {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}
.animate-fade-in-up {
  animation: fade-in-up 0.3s ease-out;
}
</style>