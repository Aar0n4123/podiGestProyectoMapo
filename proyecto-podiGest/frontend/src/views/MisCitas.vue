<template>
  <div class="citas-container">
    <div class="sidebar-wrapper">
      <SideBar :is-collapsed="isCollapsed" @toggle="toggleSidebar" />
    </div>

    <main :class="[
      'transition-all duration-300 p-6 overflow-auto bg-amber-150 min-h-screen',
      isCollapsed ? 'ml-20' : 'ml-64'
    ]">
      <div v-if="!usuarioAutenticado" class="alert alert-danger">
        <h3>Acceso Denegado</h3>
        <p>Debes iniciar sesión para acceder a las citas.</p>
        <router-link to="/login" class="btn btn-primary mt-3">
          Ir al Login
        </router-link>
      </div>

      <div v-else>
        <div class="header-section">
          <h2>Gestión de Citas</h2>
        </div>

        <div class="menu-grid">
          <router-link to="/agendar-cita" class="menu-card">
            <div class="menu-icon">📅</div>
            <h3>Agendar Cita</h3>
            <p>Agenda una nueva cita con un especialista</p>
          </router-link>

          <router-link to="/consultar-citas" class="menu-card">
            <div class="menu-icon">🔍</div>
            <h3>Consultar Citas Disponibles</h3>
            <p>Consulta las citas disponibles</p>
          </router-link>

          <div class="menu-card disabled">
            <div class="menu-icon">✏️</div>
            <h3>Modificar Citas</h3>
            <p>Modifica tus citas agendadas</p>
            <span class="badge-soon">Próximamente</span>
          </div>

          <div class="menu-card disabled">
            <div class="menu-icon">❌</div>
            <h3>Cancelar Citas</h3>
            <p>Cancela tus citas agendadas</p>
            <span class="badge-soon">Próximamente</span>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import SideBar from '../components/SideBar.vue'

export default defineComponent({
  name: 'MisCitas',
  components: {
    SideBar
  },
  data() {
    return {
      usuarioAutenticado: false,
      isCollapsed: false
    }
  },
  mounted() {
    this.verificarSesion()
  },
  methods: {
    verificarSesion() {
      const usuarioJSON = localStorage.getItem('currentUser')
      if (usuarioJSON) {
        try {
          JSON.parse(usuarioJSON)
          this.usuarioAutenticado = true
        } catch (error) {
          this.usuarioAutenticado = false
        }
      } else {
        this.usuarioAutenticado = false
      }
    },
    toggleSidebar() {
      this.isCollapsed = !this.isCollapsed
    }
  }
})
</script>

<style scoped>
.citas-container {
  display: flex;
}

main {
  flex: 1;
}

.header-section {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 40px;
}

.header-section h2 {
  font-size: 32px;
  color: #333;
  margin: 0;
  font-weight: 700;
}

.alert {
  background-color: #f8d7da;
  color: #721c24;
  border: 1px solid #f5c6cb;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.alert h3 {
  margin-top: 0;
  margin-bottom: 10px;
}

.alert p {
  margin-bottom: 15px;
}

.btn {
  padding: 10px 16px;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  text-decoration: none;
  display: inline-block;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.menu-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 30px;
  max-width: 1200px;
  margin: 0 auto;
}

.menu-card {
  background: white;
  border-radius: 12px;
  padding: 40px 30px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  text-decoration: none;
  color: inherit;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  position: relative;
  cursor: pointer;
}

.menu-card:not(.disabled):hover {
  transform: translateY(-8px);
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.3);
}

.menu-card.disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.menu-icon {
  font-size: 64px;
  margin-bottom: 20px;
}

.menu-card h3 {
  font-size: 22px;
  color: #333;
  margin: 0 0 12px 0;
  font-weight: 600;
}

.menu-card p {
  font-size: 14px;
  color: #666;
  margin: 0;
  line-height: 1.5;
}

.badge-soon {
  position: absolute;
  top: 15px;
  right: 15px;
  background: linear-gradient(135deg, #ffc107 0%, #ff9800 100%);
  color: white;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

@media (max-width: 768px) {
  .menu-grid {
    grid-template-columns: 1fr;
    gap: 20px;
  }

  .header-section h2 {
    font-size: 24px;
  }

  .menu-card {
    padding: 30px 20px;
  }

  .menu-icon {
    font-size: 48px;
  }

  .menu-card h3 {
    font-size: 18px;
  }
}
</style>
