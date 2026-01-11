// src/main.js

import { createApp } from 'vue'
import './style.css' // O el nombre de tu archivo CSS global
import App from './App.vue'
import router from './router' // <-- 1. Importa la configuración

const app = createApp(App)

app.use(router) // <-- 2. Usa el router en la aplicación

app.mount('#app')