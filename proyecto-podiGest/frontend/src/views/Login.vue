<template>
<div class="login-container">
<div class="login-box">
  <h2>Iniciar Sesión</h2>

  <form @submit.prevent="handleLogin">

    <div class="input-group">
      <label for="email">📧 Correo Electrónico</label>
      <input
          type="email"
          id="email"
          v-model="credentials.email"
          required
          placeholder="ejemplo@gmail.com"
      />
    </div>

    <div class="input-group">
      <label for="password">🔒 Contraseña</label>
      <input
          type="password"
          id="password"
          v-model="credentials.password"
          required
          placeholder="••••••••"
      />
    </div>

    <!-- Mensaje de error (opcional pero muy útil) -->
    <div v-if="errorMessage" class="error-message">{{ errorMessage }}</div>
    <div v-if="successMessage" class="success-message">{{ successMessage }}</div>

    <button type="submit" class="login-button">
      Iniciar Sesión
    </button>
  </form>

  <div class="separator"></div>

  <p class="register-text">
    ¿Aún no tienes cuenta?

    <router-link to="/registro" class="register-link">
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

<style scoped>
/* Estilos generales del contenedor (sin cambios, solo añadimos el mensaje de éxito) */
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f0f2f5;
}

.login-box {
  background: white;
  padding: 40px;
  border-radius: 12px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 400px;
}

h2 {
  text-align: center;
  color: #333;
  margin-bottom: 30px;
  border-bottom: 2px solid #AA6C39;
  padding-bottom: 10px;
}

.input-group {
  margin-bottom: 20px;
}

label {
  display: block;
  margin-bottom: 8px;
  font-weight: bold;
  color: #555;
}

input {
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

.login-button {
  width: 100%;
  padding: 12px;
  background-color: #AA6C39;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 18px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.login-button:hover {
  background-color: #8C562A;
}

.separator {
  margin: 25px 0;
  border-bottom: 1px solid #eee;
}

.register-text {
  text-align: center;
  color: #666;
  font-size: 14px;
}

.register-link {
  background: none;
  border: none;
  color: #AA6C39;
  text-decoration: underline;
  cursor: pointer;
  padding: 0;
  display: inline;
  font-size: 14px;
}

.register-link:hover {
  color: #8C562A;
}

.error-message {
  color: #ff4444;
  text-align: center;
  margin-bottom: 15px;
  padding: 10px;
  border: 1px solid #ff4444;
  background-color: #ffeeee;
  border-radius: 6px;
}

.success-message {
  color: #28a745;
  text-align: center;
  margin-bottom: 15px;
  padding: 10px;
  border: 1px solid #28a745;
  background-color: #e6ffed;
  border-radius: 6px;
}
</style>
