<template>
  <div class="register-container">
    <div class="register-form">
      <h1>Create Your Account</h1>
      <!-- Success and Error Messages -->
      <div v-if="successMessage" class="success-message">{{ successMessage }}</div>
      <div v-if="errorMessage" class="error-message">{{ errorMessage }}</div>

      <form @submit.prevent="submitForm">
        <!-- Username Field -->
        <div class="form-group">
          <label for="username">Username</label>
          <input id="username" v-model="form.username" type="text" required placeholder="Choose a username" :disabled="loading" />
        </div>
        <!-- Name Field -->
        <div class="form-group">
          <label for="name">Full Name</label>
          <input id="name" v-model="form.name" type="text" required placeholder="Enter your full name" :disabled="loading" />
        </div>
        <!-- Email Field -->
        <div class="form-group">
          <label for="email">Email</label>
          <input id="email" v-model="form.email" type="email" required placeholder="Enter your email address" :disabled="loading" />
        </div>
        <!-- Password Field -->
        <div class="form-group">
          <label for="password">Password</label>
          <input id="password" v-model="form.password" type="password" required placeholder="Create a password (min 6 chars)" :disabled="loading" />
        </div>
        <!-- Submit Button -->
        <button type="submit" :disabled="loading">
          <span v-if="loading" class="spinner"></span>
          {{ loading ? "Registering..." : "Register" }}
        </button>
      </form>

      <div class="links">
        <router-link to="/login">Already have an account? Login</router-link>
        <router-link to="/">Back to Home</router-link>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import requestService from '@/services/RequestService';  // USING THE SERVICE FOR HTTP REQUESTS

export default defineComponent({
  name: 'RegisterView',
  data() {
    return {
      form: {
        username: '',
        name: '',
        email: '',
        password: ''
      },
      successMessage: '',
      errorMessage: '',
      loading: false
    };
  },
  methods: {
    async submitForm() {
      this.successMessage = '';
      this.errorMessage = '';
      this.loading = true;
      try {
        // Send POST request to the register API endpoint with form data
        await requestService.post('/auth/register', this.form);
        // If successful, set a success message
        this.successMessage = 'Registration successful! You can now log in.';
        this.form.password = '';
      } catch (error: any) {
        // If an error occurred, extract error message (e.g., validation or duplicate error)
        // Improved error handling to display specific messages from the backend if available

        if (error && error.status === 409) { // Conflict (username/email exists)
            this.errorMessage = error.message || 'Username or email already exists.';
        } else if (error && error.message) {
            this.errorMessage = error.message;
        }
         else {
            this.errorMessage = 'Registration failed. Please check your input and try again.';
        }
        console.error('Registration error:', error);
      } finally {
        this.loading = false;
      }
    }
  }
});
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 85vh;
  padding: 30px 20px;
  background-color: #f5f5f5;
  width: 100%;
}

.register-form {
  width: 100%;
  max-width: 480px;
  padding: 40px;
  background-color: white;
  border-radius: 10px;
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease;
}

.register-form:hover {
  transform: translateY(-5px);
}

h1 {
  text-align: center;
  color: #333;
  margin-bottom: 30px;
  font-size: 2rem;
}

.form-group {
  margin-bottom: 20px;
}

label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #555;
  font-size: 1.0rem;
}

input {
  width: 100%;
  padding: 14px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 1.0rem;
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
  margin-top: 10px;
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

.success-message {
  background-color: #e8f5e9;
  color: #2e7d32;
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

/* Responsive adjustments */
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
    padding: 20px;
    min-height: 80vh;
  }

  .register-form {
    padding: 30px 25px;
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

  input {
    padding: 12px;
  }

  button {
    padding: 12px;
  }
}

/* Small mobile */
@media (max-width: 480px) {
  .register-form {
    padding: 25px 20px;
  }

  .links {
    flex-direction: column;
    align-items: center;
    gap: 15px;
  }
}
</style>
