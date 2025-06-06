<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { useRoute } from 'vue-router';
import { useAuthStore } from '@/stores/auth.store';

const authStore = useAuthStore();

const isMobileMenuOpen = ref(false);
const route = useRoute();

const isLoggedIn = computed(() => authStore.isLoggedIn);
const user = computed(() => authStore.currentUser);
const currentRoute = computed(() => route.path);

let authCheckInterval: number | null = null;

const toggleMobileMenu = () => {
  isMobileMenuOpen.value = !isMobileMenuOpen.value;
};


const closeMobileMenu = () => {
  isMobileMenuOpen.value = false;
};


const handleLogout = () => {
  authStore.logout();
  closeMobileMenu();
};

// Check authentication status periodically
onMounted(() => {
  // Set up interval to periodically check token validity
  authCheckInterval = window.setInterval(() => {

    // We don't need to update local refs anymore, as they're computed from the store
    // Just check if token needs refreshing
    if (isLoggedIn.value) {
      authStore.validateToken().catch(() => {
        // Token validation errors are handled in the store
        console.log('Token validation check failed');
      });
    }
  }, 30000);

  // Close mobile menu when clicking outside
  document.addEventListener('click', (event) => {
    const target = event.target as HTMLElement;
    const navbar = document.querySelector('.navbar');
    if (navbar && !navbar.contains(target) && isMobileMenuOpen.value) {
      closeMobileMenu();
    }
  });
});

onUnmounted(() => {
  if (authCheckInterval !== null) {
    clearInterval(authCheckInterval);
  }
});
</script>

<template>
  <nav class="navbar">
    <div class="navbar-container">
      <router-link to="/" class="navbar-brand">
        <img src="@/assets/bank logo.png" alt="Bank Logo" class="brand-logo">
        <!-- <span class="brand-name">Bank Name</span> -->
      </router-link>

      <!-- Mobile menu toggle button -->
      <button class="mobile-menu-toggle" @click.stop="toggleMobileMenu" aria-label="Toggle menu">
        <span class="toggle-icon">☰</span>
      </button>

      <!-- Navigation links -->
      <div class="navbar-menu" :class="{ 'is-active': isMobileMenuOpen }">        <!-- Public links (only for guests) -->
        <div class="navbar-links">
          <router-link v-if="!isLoggedIn" to="/" class="nav-link" :class="{ 'active': currentRoute === '/' }" @click="closeMobileMenu">
            Home
          </router-link>

          <!-- Authenticated links -->
          <template v-if="isLoggedIn">
            <router-link to="/dashboard" class="nav-link" :class="{ 'active': currentRoute === '/dashboard' }" @click="closeMobileMenu">
              Dashboard
            </router-link>
            <router-link to="/atm" class="nav-link" :class="{ 'active': currentRoute === '/atm' }" @click="closeMobileMenu">
              ATM
            </router-link>            <router-link v-if="user && user.role === 'EMPLOYEE'" to="/employeePanel" class="nav-link" :class="{ 'active': currentRoute === '/employeePanel' }" @click="closeMobileMenu">
              Employee Panel
            </router-link>
            <a href="#" class="nav-link logout-link" @click.prevent="handleLogout">
              Logout
            </a>
            <div class="user-info">
              <span class="user-name">{{ user ? user.name : '' }}</span>
            </div>
          </template>

          <!-- Guest links -->
          <template v-else>
            <router-link to="/login" class="nav-link" :class="{ 'active': currentRoute === '/login' }" @click="closeMobileMenu">
              Login
            </router-link>
            <router-link to="/register" class="nav-link register-link" :class="{ 'active': currentRoute === '/register' }" @click="closeMobileMenu">
              Register
            </router-link>
          </template>
        </div>
      </div>
    </div>
  </nav>
</template>

<style scoped>
.navbar {
  background-color: white;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.08);
  position: sticky;
  top: 0;
  z-index: 1000;
  width: 100%;
}

.navbar-container {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 2rem;
  position: relative;
}

.navbar-brand {
  display: flex;
  align-items: center;
  text-decoration: none;
  color: #333;
  font-weight: 600;
  font-size: 1.3rem;
  transition: color 0.2s ease;
}

.navbar-brand:hover {
  color: #4CAF50;
}

.brand-icon {
  margin-right: 8px;
  font-size: 1.5rem;
}

.brand-logo {
  height: 32px;
  margin-right: 10px;
}

.mobile-menu-toggle {
  display: none;
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: #333;
  padding: 0.5rem;
  transition: color 0.2s ease;
}

.mobile-menu-toggle:hover {
  color: #4CAF50;
}

.navbar-menu {
  display: flex;
  align-items: center;
}

.navbar-links {
  display: flex;
  align-items: center;
  gap: 1.5rem;
}

.nav-link {
  color: #555;
  text-decoration: none;
  padding: 0.5rem;
  font-size: 1rem;
  font-weight: 500;
  transition: all 0.2s ease;
  border-radius: 4px;
}

.nav-link:hover {
  color: #4CAF50;
  background-color: rgba(76, 175, 80, 0.1);
}

.nav-link.active {
  color: #4CAF50;
  font-weight: 600;
}

.logout-link {
  color: #f44336;
}

.logout-link:hover {
  color: #d32f2f;
  background-color: rgba(244, 67, 54, 0.1);
}

.register-link {
  background-color: #4CAF50;
  color: white;
  padding: 0.5rem 1rem;
  border-radius: 4px;
}

.register-link:hover {
  background-color: #43a047;
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(76, 175, 80, 0.2);
}

.register-link.active {
  background-color: #388e3c;
  color: white;
}

.user-info {
  margin-left: 1rem;
  padding: 0.5rem 0.8rem;
  background-color: #f5f5f5;
  border-radius: 4px;
  font-size: 0.9rem;
  color: #666;
}

.user-name {
  font-weight: 500;
}

/* Responsive styles */
@media (max-width: 768px) {
  .mobile-menu-toggle {
    display: block;
  }

  .navbar-menu {
    position: absolute;
    top: 100%;
    left: 0;
    width: 100%;
    background-color: white;
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
    padding: 1rem;
    display: none;
    flex-direction: column;
    align-items: flex-start;
    z-index: 1000;
    overflow: hidden;
    max-height: 0;
    transition: max-height 0.3s ease, padding 0.3s ease;
  }

  .navbar-menu.is-active {
    display: flex;
    max-height: 300px;
    padding: 1rem;
  }

  .navbar-links {
    flex-direction: column;
    align-items: flex-start;
    width: 100%;
  }

  .nav-link {
    width: 100%;
    padding: 0.8rem;
    border-bottom: 1px solid #f0f0f0;
  }

  .nav-link:last-child {
    border-bottom: none;
  }

  .user-info {
    margin: 1rem 0 0 0;
    width: 100%;
    text-align: center;
  }
}
</style>