// src/router/index.js

import { createRouter, createWebHistory } from 'vue-router'
// Importa tus componentes/vistas desde la carpeta 'views'
import Login from '../views/Login.vue'
import Registro from '../views/Registro.vue'
import MainPage from '../views/MainPage.vue'
import Perfil from '../views/Perfil.vue'
import Notifications from '../views/Notifications.vue'
import Information from '../views/Information.vue'
import Appointments from '../views/Appointments.vue'
import Configuration from '../views/Configuration.vue'
import PodologiaGeneral from '../views/PodologiaGeneral.vue'
import PodologiaPediatrica from '../views/PodologiaPediatrica.vue'
import Quiropodia from '../views/Quiropodia.vue'
import PlantillasPersonalizadas from '../views/PlantillasPersonalizadas.vue'
import EvaluacionIntegral from '../views/EvaluacionIntegral.vue'
import PodologiaDeportiva from '../views/PodologiaDeportiva.vue'

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
        path: '/appointments',
        name: 'appointments',
        component: Appointments
    },
    {
        path: '/configuration',
        name: 'configuration',
        component: Configuration
    },
    {
        path: '/configuration/podologia-general',
        name: 'podologia-general',
        component: PodologiaGeneral
    },
    {
        path: '/configuration/podologia-pediatrica',
        name: 'podologia-pediatrica',
        component: PodologiaPediatrica
    },
    {
        path: '/configuration/quiropodia',
        name: 'quiropodia',
        component: Quiropodia
    },
    {
        path: '/configuration/plantillas-personalizadas',
        name: 'plantillas-personalizadas',
        component: PlantillasPersonalizadas
    },
    {
        path: '/configuration/evaluacion-integral',
        name: 'evaluacion-integral',
        component: EvaluacionIntegral
    },
    {
        path: '/configuration/podologia-deportiva',
        name: 'podologia-deportiva',
        component: PodologiaDeportiva
    },

]

// Crea la instancia del router
const router = createRouter({
    history: createWebHistory(), // Utiliza rutas limpias
    routes
})

export default router