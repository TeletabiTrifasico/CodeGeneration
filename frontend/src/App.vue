<script setup lang="ts">
import { onMounted } from 'vue';
import { useRouter } from 'vue-router';
import NavBar from './components/NavBar.vue'; // Make sure this path is correct
import { useAuthStore } from './stores/auth.store';

const router = useRouter();
const authStore = useAuthStore();

onMounted(() => {
  // Check if token is valid on application start
  if (authStore.isLoggedIn) {
    authStore.validateToken()
        .catch(() => {
          // Token validation failed, will redirect in the validateToken method
          console.log('Token validation failed on app start');
        });
  }
});
</script>

<template>
  <div class="app">
    <NavBar />
    <main class="main-content">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>
    <footer class="app-footer">
      <div class="footer-content">
        <div class="footer-brand">
          <img src="./assets/bank logo.png" alt="Bank Logo" class="footer-logo">
          <p class="footer-copyright">&copy; {{ new Date().getFullYear() }} Bank Name. All rights reserved.</p>
        </div>
        <div class="footer-links">
          <a href="#" class="footer-link">Privacy Policy</a>
          <a href="#" class="footer-link">Terms of Service</a>
          <a href="#" class="footer-link">Contact Us</a>
        </div>
      </div>
    </footer>
  </div>
</template>

<style>
/* Global styles */
*, *::before, *::after {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

:root {
  --primary-color: #4CAF50;
  --primary-dark: #388E3C;
  --primary-light: #A5D6A7;
  --accent-color: #FFC107;
  --error-color: #F44336;
  --success-color: #4CAF50;
  --info-color: #2196F3;
  --warning-color: #FF9800;
  --text-primary: #333333;
  --text-secondary: #757575;
  --bg-color: #f8f9fa;
  --card-bg: #ffffff;
  --border-color: #e0e0e0;
  --shadow-sm: 0 2px 10px rgba(0, 0, 0, 0.05);
  --shadow-md: 0 5px 20px rgba(0, 0, 0, 0.08);
  --shadow-lg: 0 8px 30px rgba(0, 0, 0, 0.12);
  --transition: all 0.3s ease;
  --radius-sm: 4px;
  --radius-md: 8px;
  --radius-lg: 12px;
}

html, body {
  height: 100%;
  width: 100%;
  max-width: 100vw;
  overflow-x: hidden;
}

body {
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  background-color: var(--bg-color);
  color: var(--text-primary);
  line-height: 1.6;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

.app {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.main-content {
  flex: 1;
  width: 100%;
}

/* Animations */
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from, .fade-leave-to {
  opacity: 0;
}

/* Footer styles */
.app-footer {
  background-color: #333;
  color: white;
  padding: 2rem 0;
  margin-top: 3rem;
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 2rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 1rem;
}

.footer-brand {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.footer-logo {
  height: 40px;
  margin-bottom: 0.8rem;
  filter: brightness(0) invert(1); /*to make the logo white*/
}

.footer-links {
  display: flex;
  gap: 1.5rem;
}

.footer-link {
  color: #ddd;
  text-decoration: none;
  transition: color 0.2s ease;
}

.footer-link:hover {
  color: white;
  text-decoration: underline;
}

/* Responsive footer */
@media (max-width: 768px) {
  .footer-content {
    flex-direction: column;
    text-align: center;
    gap: 1.5rem;
  }

  .footer-brand {
    align-items: center;
  }

  .footer-links {
    width: 100%;
    justify-content: center;
    flex-wrap: wrap;
  }
}
</style>