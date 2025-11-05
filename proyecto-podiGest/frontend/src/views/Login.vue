<template>
  <div class="login-container flex items-center justify-center min-h-screen w-150 bg-blue-300/50 rounded-lg">
    <div class="login-box bg-white p-8 rounded-xl shadow-md w-full max-w-md">
      <h2 class="text-2xl font-semibold text-center text-gray-800 border-b border-blue-600 pb-2 mb-6">
        Iniciar Sesión
      </h2>

      <form @submit.prevent="handleLogin" class="space-y-5 text-black">
        <div class="input-group">
          <label for="email" class="block mb-1 font-medium text-gray-700">📧 Correo Electrónico</label>
          <input
            type="email"
            id="email"
            v-model="credentials.email"
            required
            placeholder="Ingrese Correo Ej:(ejemplo@gmail.com)"
            class="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-600"
          />
        </div>

        <div class="input-group">
          <label for="password" class="block mb-1 font-medium text-gray-700">🔒 Contraseña</label>
          <input
            type="password"
            id="password"
            v-model="credentials.password"
            required
            placeholder="••••••••"
            class="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-600"
          />
        </div>

        <div v-if="errorMessage" class="error-message text-red-600 bg-red-100 border border-red-400 rounded-md p-3 text-center">
          {{ errorMessage }}
        </div>

        <div v-if="successMessage" class="success-message text-green-700 bg-green-100 border border-green-400 rounded-md p-3 text-center">
          {{ successMessage }}
        </div>

        <button type="submit" class="login-button w-full py-2 px-4 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors text-lg">
          Iniciar Sesión
        </button>
      </form>

      <div class="separator my-6 border-t border-gray-200"></div>

      <p class="register-text text-center text-sm text-gray-600">
        ¿Aún no tienes cuenta?
        <router-link to="/registro" class="register-link text-blue-600 underline hover:text-blue-700">
          Regístrate aquí
        </router-link>
      </p>
    </div>
  </div>
</template>


<script>
// La URL correcta, siguiendo el controlador de Spring Boot
const API_URL = 'http://localhost:8080/api/usuarios/login';

// Simulamos una gestión de sesión global simple para este ejercicio
const session = {
  set(key, value) {
    localStorage.setItem(key, JSON.stringify(value));
  },
  get(key) {
    const data = localStorage.getItem(key);
    return data ? JSON.parse(data) : null;
  },
  clear() {
    localStorage.clear();
  }
};

export default {
  name: 'LoginScreen',
  data() {
    return {
      credentials: {
        email: '',
        password: ''
      },
      errorMessage: '',
      successMessage: ''
    };
  },
  methods: {
    async handleLogin() {
      // Limpiar mensajes anteriores
      this.errorMessage = '';
      this.successMessage = '';

      const { email, password } = this.credentials;

      if (!email || !password) {
        this.errorMessage = "Por favor, introduce tu correo y contraseña.";
        return;
      }

      // Los nombres de campos deben coincidir con LoginRequest.java
      const requestBody = {
        correo: email,
        contrasenia: password
      };

      try {
        const response = await fetch(API_URL, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(requestBody),
        });

        if (response.ok) {
          const userData = await response.json();
          this.successMessage = `¡Bienvenido, ${userData.nombre}! Redirigiendo...`;

          // 1. Almacenar la sesión del usuario
          session.set('currentUser', userData);

          // 2. Redireccionar basado en el rol (Lógica clave)
          if (userData.rol && userData.rol.toLowerCase() === 'especialista') {
            this.$router.push('/dashboard-especialista');
          } else {
            // Asumimos que cualquier otro rol (paciente) va a una ruta diferente
            this.$router.push('/dashboard-paciente');
          }

        } else if (response.status === 401) {
          const errorMsg = await response.text();
          this.errorMessage = errorMsg; // Muestra el mensaje de "Credenciales incorrectas"

        } else {
          this.errorMessage = 'Ocurrió un error inesperado en el servidor.';
        }

      } catch (error) {
        this.errorMessage = 'No se pudo conectar con el backend. Asegúrate de que Spring Boot esté corriendo.';
        console.error('Error de conexión:', error);
      }
    },
  },
};
</script>

