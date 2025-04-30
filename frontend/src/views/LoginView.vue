<script lang="ts">
import { defineComponent, ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import AuthService from '@/services/AuthService';
import { ErrorResponse } from '@/services/RequestService';

export default defineComponent({
  name: 'LoginView',

  setup() {
    const router = useRouter();
    const username = ref('');
    const password = ref('');
    const error = ref('');
    const isLoading = ref(false);

    onMounted(() => {
      if (AuthService.isLoggedIn()) {
        router.push('/dashboard');
      }
    });

    const handleLogin = async () => {
      try {
        error.value = '';
        isLoading.value = true;

        await AuthService.login(username.value, password.value);

        await router.push('/dashboard');
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

    return {
      username,
      password,
      error,
      isLoading,
      handleLogin,
      showForgotPassword
    };
  }
});
</script>

<template>
  <div class="login-container">
    <div class="login-form">
      <h1>Login to Your Account</h1>
      <div v-if="error" class="error-message">{{ error }}</div>
      <form @submit.prevent="handleLogin">
        <div class="form-group">
          <label for="username">Username</label>
          <input
              type="text"
              id="username"
              v-model="username"
              required
              placeholder="Enter your username"
              :disabled="isLoading"
          />
        </div>
        <div class="form-group">
          <label for="password">Password</label>
          <input
              type="password"
              id="password"
              v-model="password"
              required
              placeholder="Enter your password"
              :disabled="isLoading"
          />
        </div>
        <button type="submit" :disabled="isLoading">
          <span v-if="isLoading" class="spinner"></span>
          {{ isLoading ? 'Logging in...' : 'Login' }}
        </button>
      </form>
      <div class="links">
        <router-link to="/">Back to Home</router-link>
        <router-link to="/register">Don't have an account? Register</router-link>
        <a href="#" @click.prevent="showForgotPassword">Forgot Password?</a>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 85vh;
  padding: 30px 20px;
  background-color: #f5f5f5;
  width: 100%;
}

.login-form {
  width: 100%;
  max-width: 450px;
  padding: 40px;
  background-color: white;
  border-radius: 10px;
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease;
}

.login-form:hover {
  transform: translateY(-5px);
}

h1 {
  text-align: center;
  color: #333;
  margin-bottom: 30px;
  font-size: 2rem;
}

.form-group {
  margin-bottom: 25px;
}

label {
  display: block;
  margin-bottom: 10px;
  font-weight: 500;
  color: #555;
  font-size: 1.05rem;
}

input {
  width: 100%;
  padding: 15px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 1.05rem;
  transition: border-color 0.3s, box-shadow 0.3s;
}

input:focus {
  outline: none;
  border-color: #4CAF50;
  box-shadow: 0 0 0 3px rgba(76, 175, 80, 0.2);
}

input:disabled {
  background-color: #f9f9f9;
  cursor: not-allowed;
}

button {
  width: 100%;
  padding: 15px;
  background-color: #4CAF50;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 1.1rem;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.3s, transform 0.2s;
  position: relative;
}

button:hover:not(:disabled) {
  background-color: #45a049;
  transform: translateY(-2px);
}

button:disabled {
  background-color: #9e9e9e;
  cursor: not-allowed;
}

.spinner {
  display: inline-block;
  width: 1rem;
  height: 1rem;
  margin-right: 0.5rem;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  border-top-color: #fff;
  animation: spin 1s linear infinite;
  vertical-align: middle;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.error-message {
  background-color: #ffebee;
  color: #d32f2f;
  padding: 15px;
  border-radius: 6px;
  margin-bottom: 25px;
  font-size: 0.95rem;
}

.links {
  display: flex;
  justify-content: space-between;
  margin-top: 25px;
  font-size: 0.95rem;
}

.links a {
  color: #4CAF50;
  text-decoration: none;
  transition: color 0.2s;
}

.links a:hover {
  text-decoration: underline;
  color: #388E3C;
}

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
    padding: 20px;
    min-height: 80vh;
  }

  .login-form {
    padding: 30px 25px;
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

  input {
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
    flex-direction: column;
    align-items: center;
    gap: 15px;
  }
}
</style>