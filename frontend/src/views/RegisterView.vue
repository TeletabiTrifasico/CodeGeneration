<script setup lang="ts">
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import apiClient from '@/services/http.interceptor';

const router = useRouter();

const form = reactive({
  username: '',
  name: '',
  email: '',
  password: '',
  confirmPassword: ''
});

const successMessage = ref('');
const errorMessage = ref('');
const loading = ref(false);
const passwordVisible = ref(false);
const confirmPasswordVisible = ref(false);

const validateForm = () => {
  // Reset error messages
  errorMessage.value = '';

  // Validate username
  if (!form.username.trim()) {
    errorMessage.value = 'Username is required';
    return false;
  }

  // Validate name
  if (!form.name.trim()) {
    errorMessage.value = 'Full name is required';
    return false;
  }

  // Validate email
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!form.email.trim() || !emailRegex.test(form.email)) {
    errorMessage.value = 'Please enter a valid email address';
    return false;
  }

  // Validate password
  if (!form.password) {
    errorMessage.value = 'Password is required';
    return false;
  }

  if (form.password.length < 6) {
    errorMessage.value = 'Password must be at least 6 characters long';
    return false;
  }

  // Validate password confirmation
  if (form.password !== form.confirmPassword) {
    errorMessage.value = 'Passwords do not match';
    return false;
  }

  return true;
};

const submitForm = async () => {
  // Validate form
  if (!validateForm()) {
    return;
  }

  successMessage.value = '';
  errorMessage.value = '';
  loading.value = true;

  try {
    // Create payload (without confirmPassword)
    const payload = {
      username: form.username,
      name: form.name,
      email: form.email,
      password: form.password
    };

    // Send POST request to the register API endpoint with form data
    await apiClient.post('/auth/register', payload);

    // If successful, set a success message
    successMessage.value = 'Registration successful! You can now log in.';

    // Clear password fields
    form.password = '';
    form.confirmPassword = '';

    // Redirect to login after 2 seconds
    setTimeout(() => {
      router.push('/login');
    }, 2000);
  } catch (error: any) {
    // If an error occurred, extract error message
    if (error && error.status === 409) {
      errorMessage.value = error.message || 'Username or email already exists.';
    } else if (error && error.message) {
      errorMessage.value = error.message;
    } else {
      errorMessage.value = 'Registration failed. Please check your input and try again.';
    }
    console.error('Registration error:', error);
  } finally {
    loading.value = false;
  }
};

const togglePasswordVisibility = (field: 'password' | 'confirmPassword') => {
  if (field === 'password') {
    passwordVisible.value = !passwordVisible.value;
  } else {
    confirmPasswordVisible.value = !confirmPasswordVisible.value;
  }
};
</script>

<template>
  <div class="register-container">
    <div class="register-form">
      <h1>Create Your Account</h1>

      <!-- Success and Error Messages -->
      <transition name="fade">
        <div v-if="successMessage" class="success-message">
          <span class="success-icon">✓</span>
          {{ successMessage }}
        </div>
      </transition>

      <transition name="fade">
        <div v-if="errorMessage" class="error-message">
          <span class="error-icon">⚠️</span>
          {{ errorMessage }}
        </div>
      </transition>

      <form @submit.prevent="submitForm" novalidate>
        <!-- Username Field -->
        <div class="form-group">
          <label for="username">Username</label>
          <div class="input-wrapper">
            <input
                id="username"
                v-model="form.username"
                type="text"
                required
                placeholder="Choose a username"
                :disabled="loading"
                autocomplete="username"
            />
          </div>
        </div>

        <!-- Name Field -->
        <div class="form-group">
          <label for="name">Full Name</label>
          <div class="input-wrapper">
            <input
                id="name"
                v-model="form.name"
                type="text"
                required
                placeholder="Enter your full name"
                :disabled="loading"
                autocomplete="name"
            />
          </div>
        </div>

        <!-- Email Field -->
        <div class="form-group">
          <label for="email">Email</label>
          <div class="input-wrapper">
            <input
                id="email"
                v-model="form.email"
                type="email"
                required
                placeholder="Enter your email address"
                :disabled="loading"
                autocomplete="email"
            />
          </div>
        </div>

        <!-- Password Field -->
        <div class="form-group">
          <label for="password">Password</label>
          <div class="input-wrapper password-wrapper">
            <input
                id="password"
                v-model="form.password"
                :type="passwordVisible ? 'text' : 'password'"
                required
                placeholder="Create a password (min 6 chars)"
                :disabled="loading"
                autocomplete="new-password"
            />
            <button
                type="button"
                class="toggle-password"
                @click="togglePasswordVisibility('password')"
                :disabled="loading"
            >
              {{ passwordVisible ? '👁️' : '👁️‍🗨️' }}
            </button>
          </div>
        </div>

        <!-- Confirm Password Field -->
        <div class="form-group">
          <label for="confirmPassword">Confirm Password</label>
          <div class="input-wrapper password-wrapper">
            <input
                id="confirmPassword"
                v-model="form.confirmPassword"
                :type="confirmPasswordVisible ? 'text' : 'password'"
                required
                placeholder="Confirm your password"
                :disabled="loading"
                autocomplete="new-password"
            />
            <button
                type="button"
                class="toggle-password"
                @click="togglePasswordVisibility('confirmPassword')"
                :disabled="loading"
            >
              {{ confirmPasswordVisible ? '👁️' : '👁️‍🗨️' }}
            </button>
          </div>
        </div>

        <!-- Submit Button -->
        <button
            type="submit"
            :disabled="loading"
            :class="{ 'button-loading': loading }"
        >
          <span v-if="loading" class="spinner"></span>
          {{ loading ? "Registering..." : "Register" }}
        </button>
      </form>

      <div class="links">
        <router-link to="/login" class="link">Already have an account? Login</router-link>
        <router-link to="/" class="link">Back to Home</router-link>
      </div>
    </div>
  </div>
</template>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  padding: 20px;
  background-color: #f7f9fc;
  width: 100%;
}

.register-form {
  width: 100%;
  max-width: 480px;
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
  margin-bottom: 20px;
  position: relative;
}

label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #555;
  font-size: 1.05rem;
}

.input-wrapper {
  position: relative;
}

input[type="text"],
input[type="email"],
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
input[type="email"]:focus,
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

.password-wrapper {
  display: flex;
  align-items: center;
}

.password-wrapper input {
  flex: 1;
  border-top-right-radius: 0;
  border-bottom-right-radius: 0;
}

.toggle-password {
  background-color: #f9f9f9;
  border: 1px solid #ddd;
  border-left: none;
  padding: 0 15px;
  height: 52px;
  border-top-right-radius: 8px;
  border-bottom-right-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.toggle-password:hover:not(:disabled) {
  background-color: #f0f0f0;
}

.toggle-password:disabled {
  cursor: not-allowed;
  opacity: 0.7;
}

button[type="submit"] {
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
  margin-top: 10px;
}

button[type="submit"]:hover:not(:disabled) {
  background-color: #45a049;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(76, 175, 80, 0.2);
}

button[type="submit"]:active:not(:disabled) {
  transform: translateY(0);
}

button[type="submit"]:disabled {
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

.success-message {
  background-color: #f1f8e9;
  color: #43a047;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 25px;
  font-size: 0.95rem;
  border-left: 4px solid #43a047;
  display: flex;
  align-items: center;
}

.success-icon {
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

/* Fade transition for messages */
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s ease;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
}

/* Responsive Styles */
@media (min-width: 1200px) {
  .register-form {
    padding: 50px;
    max-width: 550px;
  }
}

@media (min-width: 768px) and (max-width: 1199px) {
  .register-form {
    padding: 35px;
  }
}

@media (max-width: 767px) {
  .register-container {
    padding: 15px;
    align-items: flex-start;
    padding-top: 50px;
  }

  .register-form {
    padding: 30px 25px;
    border-radius: 10px;
  }

  h1 {
    font-size: 1.8rem;
    margin-bottom: 25px;
  }

  .form-group {
    margin-bottom: 18px;
  }

  label {
    margin-bottom: 6px;
  }

  input[type="text"],
  input[type="email"],
  input[type="password"] {
    padding: 12px;
  }

  .toggle-password {
    height: 46px;
  }

  button[type="submit"] {
    padding: 12px;
  }
}

/* Small mobile */
@media (max-width: 480px) {
  .register-form {
    padding: 25px 20px;
  }

  .links {
    gap: 12px;
  }
}
</style>