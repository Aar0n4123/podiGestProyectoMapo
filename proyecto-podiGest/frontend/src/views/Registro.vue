<template>
  <div class="register-container">
    <div class="register-box">
      <h2>Crear Cuenta</h2>

      <!-- Muestra mensajes de éxito o error -->
      <div v-if="errorMessage" class="message error-message">{{ errorMessage }}</div>
      <div v-if="successMessage" class="message success-message">{{ successMessage }}</div>

      <form @submit.prevent="handleRegister">

        <!-- Campo: Nombre -->
        <div class="input-group">
          <label for="nombre">👤 Nombre</label>
          <input
              type="text"
              id="nombre"
              v-model="registration.nombre"
              required
              placeholder="Tu nombre"
          />
        </div>

        <!-- Campo: Apellido -->
        <div class="input-group">
          <label for="apellido">👥 Apellido</label>
          <input
              type="text"
              id="apellido"
              v-model="registration.apellido"
              required
              placeholder="Tu apellido"
          />
        </div>

        <!-- Campo: Cédula -->
        <div class="input-group">
          <label for="cedula">💳 Cédula/ID</label>
          <input
              type="text"
              id="cedula"
              v-model="registration.cedula"
              required
              placeholder="Ej: V-12345678"
          />
        </div>

        <!-- Campo: Correo Electrónico -->
        <div class="input-group">
          <label for="email">📧 Correo Electrónico</label>
          <input
              type="email"
              id="email"
              v-model="registration.correoElectronico"
              required
              placeholder="ejemplo@gmail.com"
          />
        </div>

        <!-- Campo: Contraseña (Nota: Mapea a 'contrasenia' en el backend) -->
        <div class="input-group">
          <label for="password">🔒 Contraseña</label>
          <input
              type="password"
              id="password"
              v-model="registration.contrasenia"
              required
              placeholder="••••••••"
          />
        </div>

        <!-- Campo: Fecha de Nacimiento -->
        <div class="input-group">
          <label for="fechaNacimiento">🎂 Fecha de Nacimiento</label>
          <input
              type="date"
              id="fechaNacimiento"
              v-model="registration.fechaNacimiento"
              required
          />
        </div>

        <!-- Campo: Selección de Rol -->
        <div class="input-group role-group">
          <label>Tipo de Usuario</label>
          <div class="role-options">
            <input type="radio" id="paciente" value="paciente" v-model="registration.rol" required>
            <label for="paciente">Paciente</label>

            <input type="radio" id="especialista" value="especialista" v-model="registration.rol" required>
            <label for="especialista">Especialista</label>
          </div>
        </div>

        <button type="submit" class="register-button-submit">
          Registrarse
        </button>
      </form>

      <div class="separator"></div>

      <p class="login-text">
        ¿Ya tienes cuenta?
        <!-- Enlace para volver al Login -->
        <router-link to="/login" class="login-link">
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

<style scoped>
/* Estilos generales del contenedor (similares a Login.vue) */
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f0f2f5;
}

.register-box {
  background: white;
  padding: 40px;
  border-radius: 12px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 450px; /* Un poco más ancho para tantos campos */
}

h2 {
  text-align: center;
  color: #333;
  margin-bottom: 30px;
  border-bottom: 2px solid #AA6C39;
  padding-bottom: 10px;
}

/* Estilos de inputs y labels */
.input-group {
  margin-bottom: 18px;
}

label {
  display: block;
  margin-bottom: 6px;
  font-weight: bold;
  color: #555;
}

input[type="text"],
input[type="email"],
input[type="password"],
input[type="date"] {
  width: 95%;
  padding: 12px 10px;
  border: 1px solid #ddd;
  border-radius: 6px;
  box-sizing: border-box;
  font-size: 16px;
  transition: border-color 0.3s;
}

input:focus {
  border-color: #AA6C39;
  outline: none;
}

/* Estilos de mensajes */
.message {
  padding: 10px;
  border-radius: 6px;
  margin-bottom: 15px;
  text-align: center;
  font-weight: bold;
}

.error-message {
  background-color: #fdd;
  color: #d32f2f;
  border: 1px solid #d32f2f;
}

.success-message {
  background-color: #dfd;
  color: #388e3c;
  border: 1px solid #388e3c;
}

/* Estilos específicos para la selección de rol */
.role-group {
  border: 1px solid #ddd;
  padding: 15px;
  border-radius: 6px;
}

.role-options {
  display: flex;
  gap: 20px;
  margin-top: 10px;
}

.role-options label {
  font-weight: normal;
  color: #333;
  display: flex;
  align-items: center;
  cursor: pointer;
}

.role-options input[type="radio"] {
  width: auto;
  margin-right: 5px;
}


/* Estilos del botón de Registro */
.register-button-submit {
  width: 100%;
  padding: 12px;
  background-color: #AA6C39;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 18px;
  cursor: pointer;
  transition: background-color 0.3s;
  margin-top: 10px;
}

.register-button-submit:hover {
  background-color: #8C592C;
}

/* Estilos de la sección de Login */
.separator {
  margin: 25px 0;
  border-bottom: 1px solid #eee;
}

.login-text {
  text-align: center;
  color: #666;
  font-size: 14px;
}

.login-link {
  background: none;
  border: none;
  color: #F0E68C; /* Color dorado claro (Goldenrod) */
  text-decoration: underline;
  cursor: pointer;
  padding: 0;
  display: inline;
  font-size: 14px;
}

.login-link:hover {
  color: #AA6C39;
}
</style>