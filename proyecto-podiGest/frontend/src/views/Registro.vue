<template>
  <div class="register-container flex items-center justify-center min-h-screen w-250 bg-blue-300/50 rounded-lg">
    <div class="register-box bg-white p-8 rounded-xl shadow-md w-full max-w-xl">
      <h2 class="text-2xl font-semibold text-center text-gray-800 border-b border-blue-600 pb-2 mb-6">
        Crear Cuenta
      </h2>

      <!-- Muestra mensajes de éxito o error -->
      <div v-if="errorMessage" class="message error-message text-red-600 bg-red-100 border border-red-400 rounded-md p-3 text-center mb-4">
        {{ errorMessage }}
      </div>
      <div v-if="successMessage" class="message success-message text-green-700 bg-green-100 border border-green-400 rounded-md p-3 text-center mb-4">
        {{ successMessage }}
      </div>

      <form @submit.prevent="handleRegister" class="space-y-5 text-black">
        <!-- Campo: Nombre -->
        <div class="input-group">
          <label for="nombre" class="block mb-1 font-medium text-gray-700">👤 Nombre</label>
          <input
            type="text"
            id="nombre"
            v-model="registration.nombre"
            required
            placeholder="Ingrese su Nombre"
            class="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-600"
          />
        </div>

        <!-- Campo: Apellido -->
        <div class="input-group">
          <label for="apellido" class="block mb-1 font-medium text-gray-700">👥 Apellido</label>
          <input
            type="text"
            id="apellido"
            v-model="registration.apellido"
            required
            placeholder="Ingrese su Apellido"
            class="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-600"
          />
        </div>

        <!-- Campo: Cédula -->
        <div class="input-group">
          <label for="cedula" class="block mb-1 font-medium text-gray-700">💳 Cédula/ID</label>
          <input
            type="text"
            id="cedula"
            v-model="registration.cedula"
            required
            placeholder="Ingrese su Cedula Ej:(V-12345678)"
            class="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-600"
          />
        </div>

        <!-- Campo: Correo Electrónico -->
        <div class="input-group">
          <label for="email" class="block mb-1 font-medium text-gray-700">📧 Correo Electrónico</label>
          <input
            type="email"
            id="email"
            v-model="registration.correoElectronico"
            required
            placeholder="Ingrese su Correo Electronico Ej:(ejemplo@gmail.com)"
            class="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-600"
          />
        </div>

        <!-- Campo: Contraseña -->
        <div class="input-group">
          <label for="password" class="block mb-1 font-medium text-gray-700">🔒 Contraseña</label>
          <input
            type="password"
            id="password"
            v-model="registration.contrasenia"
            required
            placeholder="••••••••"
            class="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-600"
          />
        </div>

        <!-- Campo: Fecha de Nacimiento -->
        <div class="input-group">
          <label for="fechaNacimiento" class="block mb-1 font-medium text-gray-700">🎂 Fecha de Nacimiento</label>
          <input
            type="date"
            id="fechaNacimiento"
            v-model="registration.fechaNacimiento"
            required
            class="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-600"
          />
        </div>

        <!-- Campo: Selección de Rol -->
        <div class="input-group role-group">
          <label class="block mb-1 font-medium text-gray-700">Tipo de Usuario</label>
          <div class="role-options flex gap-6">
            <div class="flex items-center gap-2">
              <input type="radio" id="paciente" value="paciente" v-model="registration.rol" required />
              <label for="paciente" class="text-gray-700">Paciente</label>
            </div>
            <div class="flex items-center gap-2">
              <input type="radio" id="especialista" value="especialista" v-model="registration.rol" required />
              <label for="especialista" class="text-gray-700">Especialista</label>
            </div>
          </div>
        </div>

        <button type="submit" class="register-button-submit w-full py-2 px-4 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors text-lg">
          Registrarse
        </button>
      </form>

      <div class="separator my-6 border-t border-gray-200"></div>

      <p class="login-text text-center text-sm text-gray-600">
        ¿Ya tienes cuenta?
        <router-link to="/login" class="login-link text-blue-600 underline hover:text-blue-700">
          Inicia Sesión aquí
        </router-link>
      </p>
    </div>
  </div>
</template>

<script>
export default {
  name: 'RegistroScreen',
  data() {
    return {
      registration: {
        nombre: '',
        apellido: '',
        cedula: '',
        // IMPORTANTE: Cambiamos 'email' a 'correoElectronico' para coincidir con el modelo de Java
        correoElectronico: '',
        // IMPORTANTE: Cambiamos 'password' a 'contrasenia' para coincidir con el modelo de Java
        contrasenia: '',
        fechaNacimiento: '',
        rol: '' // 'paciente' o 'especialista'
      },
      errorMessage: null,
      successMessage: null
    };
  },
  methods: {
    async handleRegister() {
      this.errorMessage = null;
      this.successMessage = null;

      try {
        // Asegúrate de que Spring Boot esté activo en el puerto 8080
        const response = await fetch('http://localhost:8080/api/usuarios', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          // Enviamos el objeto de registro directamente
          body: JSON.stringify(this.registration)
        });

        if (response.ok) {
          // Registro exitoso (código 201 Created)
          this.successMessage = "¡Registro exitoso! Redireccionando al login...";

          // Redirección después de un breve retraso
          setTimeout(() => {
            this.$router.push('/login');
          }, 1500);

        } else if (response.status === 409) {
          // Conflicto (usuario ya existe)
          const errorBody = await response.text();
          this.errorMessage = errorBody || "Error: Ya existe un usuario con esa cédula o correo electrónico.";

        } else {
          // Otros errores del servidor
          this.errorMessage = "Error en el servidor al intentar registrar. Intente más tarde.";
        }

      } catch (error) {
        // Error de red o conexión
        console.error("Error de conexión:", error);
        this.errorMessage = "No se pudo conectar con el backend. Asegúrate de que Spring Boot esté funcionando en el puerto 8080 y la configuración CORS sea correcta.";
      }
    }
  }
};
</script>

