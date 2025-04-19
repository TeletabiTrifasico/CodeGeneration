<script lang="ts">
import { defineComponent, ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import AuthService from '@/services/AuthService';

export default defineComponent({
  name: 'App',
  setup() {
    const router = useRouter();
    const currentYear = new Date().getFullYear();
    const isLoggedIn = ref(false);

    const checkLoginStatus = () => {
      isLoggedIn.value = AuthService.isLoggedIn();
    };

    // Check login status when component mounts
    onMounted(() => {
      checkLoginStatus();
    });

    // Logout function
    const logout = () => {
      AuthService.logout();
      checkLoginStatus();
      router.push('/');
    };

    return {
      currentYear,
      isLoggedIn,
      logout
    };
  }
});
</script>

<template>
  <div class="app">
    <header>
      <nav class="navbar">
        <div class="logo">
          <router-link to="/">Banking App</router-link>
        </div>
        <div class="nav-links">
          <router-link to="/" class="nav-link">Home</router-link>
          <template v-if="!isLoggedIn">
            <router-link to="/login" class="nav-link">Login</router-link>
          </template>
          <template v-else>
            <router-link to="/dashboard" class="nav-link">Dashboard</router-link>
            <a href="#" @click.prevent="logout" class="nav-link">Logout</a>
          </template>
        </div>
      </nav>
    </header>

    <main>
      <router-view />
    </main>

    <footer>
      <div class="footer-content">
        <p>&copy; {{ currentYear }} Banking Application. All rights reserved.</p>
      </div>
    </footer>
  </div>
</template>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  line-height: 1.6;
  color: #333;
  background-color: #f9f9f9;
  width: 100%;
  max-width: 100vw;
  overflow-x: hidden;
}

.app {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  width: 100%;
}

header {
  background-color: #ffffff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  width: 100%;
}

.navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 2rem;
  max-width: 1400px;
  margin: 0 auto;
  width: 100%;
}

.logo a {
  font-size: 1.5rem;
  font-weight: bold;
  color: #4CAF50;
  text-decoration: none;
}

.nav-links {
  display: flex;
  gap: 2rem;
}

.nav-link {
  color: #555;
  text-decoration: none;
  font-weight: 500;
  transition: color 0.3s;
  font-size: 1.05rem;
}

.nav-link:hover {
  color: #4CAF50;
}

main {
  flex: 1;
  width: 100%;
  max-width: 100%;
  padding: 0;
  margin: 0;
}

footer {
  background-color: #f5f5f5;
  padding: 1.5rem 0;
  margin-top: auto;
  width: 100%;
}

.footer-content {
  max-width: 1400px;
  margin: 0 auto;
  text-align: center;
  color: #666;
  padding: 0 20px;
}

/* Large desktop */
@media (min-width: 1200px) {
  .navbar {
    padding: 1rem 3rem;
  }
}

/* Medium desktop */
@media (min-width: 992px) and (max-width: 1199px) {
  .navbar {
    padding: 1rem 2rem;
  }
}

/* Small desktop and tablets */
@media (min-width: 768px) and (max-width: 991px) {
  .navbar {
    padding: 1rem 1.5rem;
  }
}

/* Mobile */
@media (max-width: 767px) {
  .navbar {
    flex-direction: column;
    padding: 1rem;
    gap: 1rem;
  }

  .nav-links {
    width: 100%;
    justify-content: center;
    gap: 1.5rem;
  }
}

/* Small mobile */
@media (max-width: 480px) {
  .nav-links {
    flex-direction: column;
    align-items: center;
    gap: 0.8rem;
  }

  .nav-link {
    padding: 0.5rem 0;
  }
}
</style>