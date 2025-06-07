<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth.store';

// Form state
const username = ref('');
const password = ref('');
const isLoading = ref(false);
const error = ref('');

// Get router for navigation
const router = useRouter();
const authStore = useAuthStore();

// Handle login submission
const handleLogin = async () => {
  // Reset error state
  error.value = '';

  // Validate form
  if (!username.value || !password.value) {
    error.value = 'Please enter both username and password';
    return;
  }

  try {
    isLoading.value = true;

    // Call login action from the auth store
    await authStore.login(username.value, password.value);

    // Redirect to dashboard on success
    router.push('/dashboard');
    } catch (err: any) {
    console.error('Login error:', err);

    // Set error message based on response
    if (err.response && err.response.status === 401) {
      // Check if we have a specific error message from the backend
      const errorMessage = err.response?.data?.message || err.response?.data?.error;
      if (errorMessage && errorMessage.toLowerCase().includes('disabled')) {
        error.value = errorMessage;
      } else {
        error.value = 'Invalid username or password';
      }
    } else {
      error.value = err.message || 'An error occurred during login';
    }
  } finally {
    isLoading.value = false;
  }
};
</script>

<template>
  <div class="login-container">
    <div class="login-card">
      <h1>Banking App Login</h1>

      <div v-if="error" class="error-message">
        {{ error }}
      </div>

      <form @submit.prevent="handleLogin" class="login-form">
        <div class="form-group">
          <label for="username">Username</label>
          <input
              type="text"
              id="username"
              v-model="username"
              placeholder="Enter your username"
              :disabled="isLoading"
              autocomplete="username"
          />
        </div>

        <div class="form-group">
          <label for="password">Password</label>
          <input
              type="password"
              id="password"
              v-model="password"
              placeholder="Enter your password"
              :disabled="isLoading"
              autocomplete="current-password"
          />
        </div>

        <button
            type="submit"
            class="login-button"
            :disabled="isLoading"
        >
          <span v-if="isLoading" class="spinner"></span>
          <span v-else>Login</span>
        </button>
      </form>

      <div class="login-footer">
        <p>Test credentials:</p>
        <p>Username: user1 | Password: user123</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background-color: #f5f5f5;
  padding: 20px;
}

.login-card {
  width: 100%;
  max-width: 450px;
  background-color: white;
  border-radius: 12px;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
  padding: 40px;
}

h1 {
  text-align: center;
  color: #333;
  margin-bottom: 30px;
  font-size: 1.8rem;
}

.error-message {
  background-color: #ffebee;
  color: #d32f2f;
  padding: 12px;
  border-radius: 6px;
  margin-bottom: 20px;
  font-size: 0.9rem;
  text-align: center;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

label {
  font-size: 0.9rem;
  color: #555;
  font-weight: 500;
}

input {
  padding: 12px 16px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 1rem;
  transition: border-color 0.2s ease;
}

input:focus {
  border-color: #4CAF50;
  outline: none;
}

input:disabled {
  background-color: #f9f9f9;
  cursor: not-allowed;
}

.login-button {
  margin-top: 10px;
  padding: 14px;
  background-color: #4CAF50;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  justify-content: center;
  align-items: center;
}

.login-button:hover:not(:disabled) {
  background-color: #43a047;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.login-button:active:not(:disabled) {
  transform: translateY(0);
}

.login-button:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.spinner {
  display: inline-block;
  width: 20px;
  height: 20px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  border-top-color: white;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.login-footer {
  margin-top: 40px;
  text-align: center;
  color: #777;
  font-size: 0.85rem;
}

.login-footer p {
  margin: 5px 0;
}

@media (max-width: 600px) {
  .login-card {
    padding: 30px 20px;
  }

  h1 {
    font-size: 1.6rem;
  }
}
</style>