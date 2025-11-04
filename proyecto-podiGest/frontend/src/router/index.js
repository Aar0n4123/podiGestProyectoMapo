// src/router/index.js

import { createRouter, createWebHistory } from 'vue-router'
// Importa tus componentes/vistas desde la carpeta 'views'
import Login from '../views/Login.vue'
import Registro from '../views/Registro.vue'

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
    }
]

// Crea la instancia del router
const router = createRouter({
    history: createWebHistory(), // Utiliza rutas limpias
    routes
})

export default router