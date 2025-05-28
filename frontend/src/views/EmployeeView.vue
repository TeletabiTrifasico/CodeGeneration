<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useAuthStore } from '@/stores/auth.store';
import { useUserStore } from '@/stores/user.store';
import UserItem from '../components/EmployeeUserItem.vue';



// User reactive state
const authStore = useAuthStore();
const userStore = useUserStore();
const user = ref(authStore.currentUser);
const isLoading = ref(true);
const error = ref('');
const currentPanel = ref('default');
let currentPage = 1;
let pageUsers = {};

const handleLogout = () => {
  authStore.logout();
};
const refreshData = () => {
  
};
async function changePage(changeAmount: number) {
  isLoading.value = true;
  currentPage += changeAmount;
  pageUsers = await userStore.getUsersByPage(currentPage);
  if (Object.keys(pageUsers).length === 0) {
    currentPage--;
    pageUsers = await userStore.getUsersByPage(currentPage);
    //Add an error message saying next page was empty
  }
  isLoading.value = false;
}

onMounted(async () => {
  // Validate authentication token
  try {
    await authStore.validateToken();
    pageUsers = await userStore.getFirstPage();
    console.log(pageUsers);
  } catch (err) {
    console.error('Token validation error:', err);
    // authStore.logout() will be called in validateToken if it fails
  }
  isLoading.value = false;
});
</script>

<template>
  <div class="view-container">
    <!-- Header with user info and actions -->
    <header class="panel-header">
      <div class="user-welcome">
        <h1>Welcome, employee {{ user ? user.name : 'User' }}!</h1>
      </div>
      <div class="header-actions">
        <button @click="refreshData" class="refresh-button" :disabled="isLoading">
          <span v-if="isLoading" class="spinner small"></span>
          <span v-else>↻</span>
          Refresh
        </button>
        <button @click="handleLogout" class="logout-button">Logout</button>
      </div>
    </header>

    <!-- Main panel content -->
    <div v-if="error" class="error-panel">
      <p>{{ error }}</p>
      <button @click="refreshData" class="action-button">Try Again</button>
    </div>

    <div v-else class="panel-container">
      <span v-if="isLoading" class="spinner small"></span>
        <div v-else>
          <div v-for="item in pageUsers">
            <UserItem :user="item"/>
          </div>
        </div>
        <div class="centered">
          <button 
            @click="changePage(-1)"
            :disabled="currentPage === 1"
            class="px-3 py-1 border rounded"
          >
            &lt;
          </button>
          <div>{{currentPage}}</div>
          <button 
            @click="changePage(1)"
            class="px-3 py-1 border rounded"
          >
            &gt;
          </button>
        </div>
        
    </div>
  </div>
</template>

<style scoped>
.view-container {
  max-width: 1200px;
  width: 100%;
  margin: 0 auto;
  padding: 30px 20px;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 40px;
  flex-wrap: wrap;
  gap: 20px;
}

h1 {
  margin: 0;
  color: #333;
  font-size: 2rem;
  font-weight: 600;
}

.header-actions {
  display: flex;
  gap: 15px;
}

.refresh-button,
.logout-button {
  padding: 10px 15px;
  border-radius: 8px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 8px;
}

.refresh-button {
  background-color: #f5f5f5;
  color: #333;
  border: 1px solid #ddd;
}

.refresh-button:hover:not(:disabled) {
  background-color: #e8e8e8;
}

.refresh-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.logout-button {
  background-color: #f44336;
  color: white;
  border: none;
}

.logout-button:hover {
  background-color: #d32f2f;
  transform: translateY(-2px);
}

.spinner {
  display: inline-block;
  width: 16px;
  height: 16px;
  border: 2px solid rgba(0, 0, 0, 0.1);
  border-radius: 50%;
  border-top-color: #333;
  animation: spin 1s linear infinite;
}

.spinner.small {
  width: 14px;
  height: 14px;
  border-width: 2px;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.error-panel {
  background-color: #ffebee;
  border-radius: 8px;
  padding: 30px;
  text-align: center;
  margin-bottom: 30px;
}

.error-panel p {
  color: #d32f2f;
  margin-bottom: 20px;
  font-size: 1.1rem;
}

.panel-content {
  animation: fadeIn 0.5s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.skeleton-loader {
  background: linear-gradient(90deg, #f0f0f0 25%, #e8e8e8 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: loading 1.5s infinite;
  border-radius: 4px;
}

@keyframes loading {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}

.card-loading {
  min-height: 100px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.no-data {
  color: #888;
  padding: 30px 0;
  text-align: center;
  font-size: 1.1rem;
}

.positive {
  color: #4CAF50;
}

.negative {
  color: #F44336;
}

.panel-actions {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.action-button {
  padding: 18px;
  background-color: #4CAF50;
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 1.1rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.action-button:hover {
  background-color: #43a047;
  transform: translateY(-3px);
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.1);
}

.action-button:active {
  transform: translateY(-1px);
}

.action-icon {
  font-size: 1.2rem;
}

/* Responsive Styles */
@media (min-width: 1400px) {
  .view-container {
    padding: 50px 20px;
  }

  h1 {
    font-size: 2.4rem;
  }
}

@media (min-width: 992px) and (max-width: 1399px) {
  .view-container {
    padding: 40px 20px;
  }
}

@media (min-width: 576px) and (max-width: 767px) {
  .panel-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }

  h1 {
    font-size: 1.8rem;
  }

  .panel-actions {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 575px) {
  .view-container {
    padding: 20px 15px;
  }

  .panel-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
    margin-bottom: 30px;
  }

  h1 {
    font-size: 1.7rem;
  }

  .header-actions {
    width: 100%;
  }

  .refresh-button,
  .logout-button {
    flex: 1;
    justify-content: center;
  }

  .panel-actions {
    grid-template-columns: 1fr;
    gap: 15px;
  }

  .action-button {
    padding: 15px;
  }
}

/* Tab styling */
.tabs-container {
  display: flex;
  justify-content: flex-start;
  gap: 20px;
}

.tab-link {
  text-decoration: none;
  color: #555;
  font-size: 1.1rem;
  font-weight: 600;
  padding: 10px 20px;
  border-radius: 8px 8px 0 0;
  transition: all 0.3s ease;
  cursor: pointer;
  position: relative;
  border: 2px solid black;
  border-bottom-color: var(--bg-color);
  border-bottom-width: 5px;
}

.tab-link:hover {
  background-color: #f0f0f0;
}

.tab-link:focus {
  outline: none;
}

.active-tab {
  border-top-color: green;
  border-top-width: 5px;
}

.tab-link:hover:not(.active-tab) {
  background-color: #f0f0f0;
}

.panel-container {
  border: 2px solid black;
  padding: 20px;
  border-radius: 0 0 10px 10px;
  margin-top: -3px;
}

@media (max-width: 767px) {
  .tabs-container {
    flex-direction: column;
    align-items: flex-start;
  }
}

.centered {
  display: flex;
  flex-direction: row;
  justify-content: center;
}
</style>