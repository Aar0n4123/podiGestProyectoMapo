<template>
  <div class="login-container flex items-center justify-center min-h-screen w-150 bg-blue-100/50 rounded-lg">

    <div class="login-box bg-white p-8 rounded-xl shadow-2xl shadow-blue-300 w-full max-w-md">
      <h2 class="text-2xl font-semibold text-center text-gray-800 border-b-2 border-blue-500 pb-2 mb-6">
          Iniciar Sesión
      </h2>

      <form @submit.prevent="handleLogin" class="space-y-5 text-gray-900">

        <div class="input-group">
          <label for="email" class="block mb-1 font-medium text-gray-700">📧 Correo Electrónico</label>
          <input
              type="email"
              id="email"
              v-model="credentials.email"
              required
              placeholder="Ingrese Correo Ej:(ejemplo@gmail.com)"
              class="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
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
              class="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>

        <div v-if="errorMessage" class="error-message text-red-700 bg-red-100 border border-red-500 rounded-md p-3 text-center">
          {{ errorMessage }}
        </div>

        <div v-if="successMessage" class="success-message text-green-700 bg-green-100 border border-green-500 rounded-md p-3 text-center">
          {{ successMessage }}
        </div>
        
        <button router-link to="/mainpage" type="submit" class="login-button w-full py-2 px-4 bg-blue-500 text-white rounded-md hover:bg-blue-700 transition-colors text-lg">
          Iniciar Sesión
        </button>
      </form>

      <div class="separator my-6 border-t border-gray-200"></div>

      <p class="register-text text-center text-sm text-gray-600">
        ¿Aún no tienes cuenta?
        <router-link to="/registro" class="register-link text-blue-600 underline hover:text-blue-700 font-medium">
          Regístrate aquí
        </router-link>
      </p>
    </div>
  </div>
</template>


<script>
const API_URL = 'http://localhost:8080/api/usuarios/login';


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
      this.errorMessage = '';
      this.successMessage = '';

      const { email, password } = this.credentials;

      if (!email || !password) {
        this.errorMessage = "Por favor, introduce tu correo y contraseña.";
        return;
      }


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


          session.set('currentUser', userData);


          this.$router.push('/mainpage');


        } else if (response.status === 401) {
          const errorMsg = await response.text();
          this.errorMessage = errorMsg;

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