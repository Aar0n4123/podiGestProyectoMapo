<template>
  <div class="register-container flex items-center justify-center min-h-screen w-250 bg-blue-100/50 rounded-lg">

    <div class="register-box bg-white p-8 rounded-xl shadow-2xl shadow-blue-300 w-full max-w-xl">
      <h2 class="text-2xl font-semibold text-center text-gray-800 border-b-2 border-blue-500 pb-2 mb-6">
         Crear Cuenta
      </h2>

      <form @submit.prevent="handleRegister" class="space-y-5 text-gray-900">

        <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
          <div class="input-group">
            <label for="nombre" class="block mb-1 font-medium text-gray-700">👤 Nombre</label>
            <input
                type="text"
                id="nombre"
                v-model="registration.nombre"
                required
                placeholder="Ingrese su Nombre"
                class="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>

          <div class="input-group">
            <label for="apellido" class="block mb-1 font-medium text-gray-700">👥 Apellido</label>
            <input
                type="text"
                id="apellido"
                v-model="registration.apellido"
                required
                placeholder="Ingrese su Apellido"
                class="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>
        </div>
        <div class="input-group">
          <label for="cedula" class="block mb-1 font-medium text-gray-700">💳 Cédula/ID</label>
          <input
              type="text"
              id="cedula"
              v-model="registration.cedula"
              required
              placeholder="Ingrese su Cedula Ej:(V-12345678)"
              class="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>

        <div class="input-group">
          <label for="email" class="block mb-1 font-medium text-gray-700">📧 Correo Electrónico</label>
          <input
              type="email"
              id="email"
              v-model="registration.correoElectronico"
              required
              placeholder="Ingrese su Correo Electronico Ej:(ejemplo@gmail.com)"
              class="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>

        <div class="input-group">
          <label for="password" class="block mb-1 font-medium text-gray-700">🔒 Contraseña</label>
          <input
              type="password"
              id="password"
              v-model="registration.contrasenia"
              required
              placeholder="••••••••"
              class="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>

        <div class="input-group">
          <label for="fechaNacimiento" class="block mb-1 font-medium text-gray-700">🎂 Fecha de Nacimiento</label>
          <input
              type="date"
              id="fechaNacimiento"
              v-model="registration.fechaNacimiento"
              required
              class="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>

        <div v-if="errorMessage" class="message error-message text-red-700 bg-red-100 border border-red-500 rounded-md p-3 text-center">
          {{ errorMessage }}
        </div>
        <div v-if="successMessage" class="message success-message text-green-700 bg-green-100 border border-green-500 rounded-md p-3 text-center">
          {{ successMessage }}
        </div>

        <button type="submit" class="register-button-submit w-full py-2 px-4 bg-blue-500 text-white rounded-md hover:bg-blue-600 transition-colors text-lg font-bold">
          Registrarse
        </button>
      </form>

      <div class="separator my-6 border-t border-gray-200"></div>

      <p class="login-text text-center text-sm text-gray-600">
        ¿Ya tienes cuenta?
        <router-link to="/login" class="login-link text-blue-600 underline hover:text-blue-700 font-medium">
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
        correoElectronico: '',
        contrasenia: '',
        fechaNacimiento: '',
      },
      errorMessage: null,
      successMessage: null
    };
  },
  methods: {
    checkAge(dateOfBirth) {
      if (!dateOfBirth) return false;
      const today = new Date();
      const birthDate = new Date(dateOfBirth);
      let age = today.getFullYear() - birthDate.getFullYear();
      const monthDifference = today.getMonth() - birthDate.getMonth();

      if (monthDifference < 0 || (monthDifference === 0 && today.getDate() < birthDate.getDate())) {
        age--;
      }
      return age >= 18;
    },

    async handleRegister() {
      this.errorMessage = null;
      this.successMessage = null;

      if (!this.checkAge(this.registration.fechaNacimiento)) {
        this.errorMessage = "Debes ser mayor de 18 años para registrarte.";
        return;
      }

      try {
        const response = await fetch('http://localhost:8080/api/usuarios', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(this.registration)
        });

        if (response.ok) {
          this.successMessage = "¡Registro exitoso! Redireccionando al login...";
          setTimeout(() => {
            this.$router.push('/login');
          }, 1500);

        } else {
          const errorText = await response.text();
          if (response.status === 409) {
            this.errorMessage = errorText || "Error: Ya existe un usuario con esa cédula o correo electrónico.";
          } else if (response.status === 400 && errorText.includes("mayor de edad")) {
            this.errorMessage = "El backend ha rechazado el registro: Debes ser mayor de 18 años.";
          } else {
            this.errorMessage = errorText || "Error en el servidor al intentar registrar. Intente más tarde.";
          }
        }

      } catch (error) {
        console.error("Error de conexión:", error);
        this.errorMessage = "No se pudo conectar con el backend. Asegúrate de que Spring Boot esté funcionando.";
      }
    }
  }
};
</script>