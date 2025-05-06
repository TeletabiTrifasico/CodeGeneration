<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import AuthService from '@/services/AuthService';
import { ErrorResponse } from '@/services/RequestService';

const router = useRouter();
const route = useRoute();
const username = ref('');
const password = ref('');
const error = ref('');
const isLoading = ref(false);
const rememberMe = ref(false);

onMounted(() => {
  // Check if redirected from protected route
  const redirectPath = route.query.redirect as string;

  // Check if already logged in
  if (AuthService.isLoggedIn()) {
    router.push(redirectPath || '/dashboard');
  }

  // Restore remembered username if available
  const savedUsername = localStorage.getItem('remembered_username');
  if (savedUsername) {
    username.value = savedUsername;
    rememberMe.value = true;
  }
});

const handleLogin = async () => {
  try {
    error.value = '';
    isLoading.value = true;

    // Validate form
    if (!username.value.trim()) {
      error.value = 'Username is required';
      isLoading.value = false;
      return;
    }

    if (!password.value) {
      error.value = 'Password is required';
      isLoading.value = false;
      return;
    }

    // Handle remember me
    if (rememberMe.value) {
      localStorage.setItem('remembered_username', username.value);
    } else {
      localStorage.removeItem('remembered_username');
    }

    await AuthService.login(username.value, password.value);

    // Get redirect path if exists
    const redirectPath = route.query.redirect as string;
    await router.push(redirectPath || '/dashboard');
  } catch (err) {
    const errorResponse = err as ErrorResponse;

    if (errorResponse.status === 401) {
      error.value = 'Invalid username or password. Please try again.';
    } else if (errorResponse.status === 0) {
      error.value = 'Unable to connect to server. Please check your internet connection.';
    } else {
      error.value = errorResponse.message || 'An unexpected error occurred. Please try again.';
    }

    console.error('Login error:', errorResponse);
  } finally {
    isLoading.value = false;
  }
};

const showForgotPassword = () => {
  alert('Forgot password functionality will be implemented soon.');
};
</script>

<template>
  <div class="login-container">
    <div class="login-form">
      <h1>Login to Your Account</h1>

      <transition name="fade">
        <div v-if="error" class="error-message">
          <span class="error-icon">⚠️</span>
          {{ error }}
        </div>
      </transition>

      <form @submit.prevent="handleLogin" novalidate>
        <div class="form-group">
          <label for="username">Username</label>
          <div class="input-wrapper">
            <input
                type="text"
                id="username"
                v-model="username"
                required
                placeholder="Enter your username"
                :disabled="isLoading"
                autocomplete="username"
            />
          </div>
        </div>

        <div class="form-group">
          <label for="password">Password</label>
          <div class="input-wrapper">
            <input
                type="password"
                id="password"
                v-model="password"
                required
                placeholder="Enter your password"
                :disabled="isLoading"
                autocomplete="current-password"
            />
          </div>
        </div>

        <div class="form-group remember-me">
          <label class="checkbox-container">
            <input
                type="checkbox"
                v-model="rememberMe"
                :disabled="isLoading"
            />
            <span class="checkmark"></span>
            Remember me
          </label>
        </div>

        <button
            type="submit"
            :disabled="isLoading"
            :class="{ 'button-loading': isLoading }"
        >
          <span v-if="isLoading" class="spinner"></span>
          {{ isLoading ? 'Logging in...' : 'Login' }}
        </button>
      </form>

      <div class="links">
        <router-link to="/" class="link">Back to Home</router-link>
        <router-link to="/register" class="link">Don't have an account? Register</router-link>
        <a href="#" @click.prevent="showForgotPassword" class="link">Forgot Password?</a>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  padding: 20px;
  background-color: #f7f9fc;
  width: 100%;
}

.login-form {
  width: 100%;
  max-width: 450px;
  padding: 40px;
  background-color: white;
  border-radius: 12px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

h1 {
  text-align: center;
  color: #333;
  margin-bottom: 30px;
  font-size: 2rem;
  font-weight: 600;
}

.form-group {
  margin-bottom: 25px;
  position: relative;
}

label {
  display: block;
  margin-bottom: 10px;
  font-weight: 500;
  color: #555;
  font-size: 1.05rem;
}

.input-wrapper {
  position: relative;
}

input[type="text"],
input[type="password"] {
  width: 100%;
  padding: 15px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 1.05rem;
  transition: all 0.3s ease;
  background-color: #f9f9f9;
}

input[type="text"]:focus,
input[type="password"]:focus {
  outline: none;
  border-color: #4CAF50;
  box-shadow: 0 0 0 3px rgba(76, 175, 80, 0.2);
  background-color: white;
}

input:disabled {
  background-color: #f0f0f0;
  cursor: not-allowed;
  opacity: 0.7;
}

/* Remember me checkbox styling */
.remember-me {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.checkbox-container {
  display: flex;
  align-items: center;
  position: relative;
  padding-left: 35px;
  cursor: pointer;
  user-select: none;
}

.checkbox-container input {
  position: absolute;
  opacity: 0;
  cursor: pointer;
  height: 0;
  width: 0;
}

.checkmark {
  position: absolute;
  top: 0;
  left: 0;
  height: 20px;
  width: 20px;
  background-color: #f9f9f9;
  border: 1px solid #ddd;
  border-radius: 4px;
  transition: all 0.2s ease;
}

.checkbox-container:hover input ~ .checkmark {
  background-color: #f0f0f0;
}

.checkbox-container input:checked ~ .checkmark {
  background-color: #4CAF50;
  border-color: #4CAF50;
}

.checkmark:after {
  content: "";
  position: absolute;
  display: none;
}

.checkbox-container input:checked ~ .checkmark:after {
  display: block;
}

.checkbox-container .checkmark:after {
  left: 7px;
  top: 3px;
  width: 5px;
  height: 10px;
  border: solid white;
  border-width: 0 2px 2px 0;
  transform: rotate(45deg);
}

button {
  width: 100%;
  padding: 15px;
  background-color: #4CAF50;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 1.1rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  display: flex;
  justify-content: center;
  align-items: center;
}

button:hover:not(:disabled) {
  background-color: #45a049;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(76, 175, 80, 0.2);
}

button:active:not(:disabled) {
  transform: translateY(0);
}

button:disabled {
  background-color: #9e9e9e;
  cursor: not-allowed;
  opacity: 0.7;
}

.button-loading {
  pointer-events: none;
}

.spinner {
  display: inline-block;
  width: 20px;
  height: 20px;
  margin-right: 10px;
  border: 3px solid rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  border-top-color: #fff;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.error-message {
  background-color: #fff8f8;
  color: #e53935;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 25px;
  font-size: 0.95rem;
  border-left: 4px solid #e53935;
  display: flex;
  align-items: center;
}

.error-icon {
  margin-right: 10px;
  font-size: 1.1rem;
}

.links {
  margin-top: 30px;
  display: flex;
  flex-direction: column;
  gap: 15px;
  text-align: center;
}

.link {
  color: #4CAF50;
  text-decoration: none;
  transition: color 0.2s;
  font-size: 0.95rem;
  padding: 5px;
  border-radius: 4px;
}

.link:hover {
  color: #388E3C;
  background-color: rgba(76, 175, 80, 0.1);
}

/* Fade transition for error messages */
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s ease;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
}

/* Responsive Styles */
@media (min-width: 1200px) {
  .login-form {
    padding: 50px;
    max-width: 500px;
  }
}

@media (min-width: 768px) and (max-width: 1199px) {
  .login-form {
    padding: 35px;
  }
}

@media (max-width: 767px) {
  .login-container {
    padding: 15px;
    align-items: flex-start;
    padding-top: 50px;
  }

  .login-form {
    padding: 30px 25px;
    border-radius: 10px;
  }

  h1 {
    font-size: 1.8rem;
    margin-bottom: 25px;
  }

  .form-group {
    margin-bottom: 20px;
  }

  label {
    margin-bottom: 8px;
  }

  input[type="text"],
  input[type="password"] {
    padding: 12px;
  }

  button {
    padding: 12px;
  }
}

/* Small mobile */
@media (max-width: 480px) {
  .login-form {
    padding: 25px 20px;
  }

  .links {
    gap: 12px;
  }
}
</style>