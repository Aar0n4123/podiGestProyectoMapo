// src/router/index.js

import { createRouter, createWebHistory } from 'vue-router'
// Importa tus componentes/vistas desde la carpeta 'views'
import Login from '../views/Login.vue'
import Registro from '../views/Registro.vue'
import MainPage from '../views/MainPage.vue'
import Perfil from '../views/Perfil.vue'
import Notifications from '../views/Notifications.vue'
import Information from '../views/Information.vue'
import Configuration from '../views/Configuration.vue'
import AgendarCita from '../views/AgendarCita.vue'
import MisCitas from '../views/MisCitas.vue'
import ConsultarCitas from '../views/ConsultarCitas.vue'
import ConsultarCitaEsp from '../views/ConsultarCitaEsp.vue'
import ModificarCitaEsp from '../views/ModificarCitaEsp.vue'

// Define las URLS y el componente asociado
const routes = [
    {
        path: '/',
        redirect: '/login' // 1. Ruta por defecto: redirige la raíz a /login
    },
    {
        path: '/login', // URL: /login
        name: 'Login',
        component: Login
    },
    {
        path: '/registro', // URL: /registro
        name: 'Registro',
        component: Registro
    },
    {
        path: '/mainpage',
        name: 'Main',
        component: MainPage
    },
    {
        path: '/profile',
        name: 'profile',
        component: Perfil
    },
    {
        path: '/notifications',
        name: 'notifications',
        component: Notifications
    },
    {
        path: '/information',
        name: 'information',
        component: Information
    },
    {
        path: '/configuration',
        name: 'configuration',
        component: Configuration
    },
    {
        path: '/agendar-cita',
        name: 'agendar-cita',
        component: AgendarCita
    },
    {
        path: '/mis-citas',
        name: 'mis-citas',
        component: MisCitas
    },
    {
        path: '/consultar-citas',
        name: 'consultar-citas',
        component: ConsultarCitas
    },
    {
        path: '/consultar-citas-esp',
        name: 'consultar-citas-esp',
        component: ConsultarCitaEsp
    },
    {
        path: '/modificar-citas-esp',
        name: 'modificar-citas-esp',
        component: ModificarCitaEsp
    },
    
]

// Crea la instancia del router
const router = createRouter({
    history: createWebHistory(), // Utiliza rutas limpias
    routes
})

export default router