<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useAuthStore } from '@/stores/auth.store';
import { useUserStore } from '@/stores/user.store';
import UserItem from '../components/EmployeeUserItem.vue';
import PendingUserItem from '../components/PendingUserItem.vue';



// User reactive state
const authStore = useAuthStore();
const userStore = useUserStore();
const user = ref(authStore.currentUser);
const isLoading = ref(true);
const error = ref('');
const showPending = ref(false);
let currentPage = 1;
let pageUsers = {};

const handleLogout = () => {
  authStore.logout();
};

const toggleView = async () => {
  showPending.value = !showPending.value;
  currentPage = 1;
  await loadData();
};

const loadData = async () => {
  isLoading.value = true;
  if (showPending.value) {
    pageUsers = await userStore.getDisabledUsersByPage(currentPage);
  } else {
    pageUsers = await userStore.getUsersByPage(currentPage);
  }
  isLoading.value = false;
};

const refreshData = () => {
  loadData();
};
async function changePage(changeAmount: number) {
  isLoading.value = true;
  currentPage += changeAmount;
  
  if (showPending.value) {
    pageUsers = await userStore.getDisabledUsersByPage(currentPage);
  } else {
    pageUsers = await userStore.getUsersByPage(currentPage);
  }
  
  if (Object.keys(pageUsers).length === 0) {
    currentPage--;
    // Try again with the previous page
    if (showPending.value) {
      pageUsers = await userStore.getDisabledUsersByPage(currentPage);
    } else {
      pageUsers = await userStore.getUsersByPage(currentPage);
    }
  }
  isLoading.value = false;
}

onMounted(async () => {
  // Validate authentication token
  try {
    await authStore.validateToken();
    await loadData();
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
      </div>      <div class="header-actions">
        <button @click="toggleView" class="toggle-button">
          {{ showPending ? 'Show All Users' : 'Show Pending Users' }}
        </button>
        <button @click="refreshData" class="refresh-button" :disabled="isLoading">
          <span v-if="isLoading" class="spinner small"></span>
          <span v-else>↻</span>
        </button>
      </div>
    </header>

    <!-- Main panel content -->
    <div v-if="error" class="error-panel">
      <p>{{ error }}</p>
      <button @click="refreshData" class="action-button">Try Again</button>
    </div>    <div v-else class="panel-container">
      <div v-if="isLoading" class="loading-container">
        <span class="spinner"></span>
        <p>Loading users...</p>
      </div>
      <div v-else class="users-container">
        <div class="users-list">
          <div v-for="item in pageUsers" :key="item.id" class="user-item">
            <PendingUserItem v-if="showPending" :user="item" @user-approved="loadData"/>
            <UserItem v-else :user="item"/>
          </div>
          <div v-if="Object.keys(pageUsers).length === 0" class="no-users">
            <p>{{ showPending ? 'No pending users found' : 'No users found' }}</p>
          </div>
        </div>
        
        <div class="pagination">
          <button 
            @click="changePage(-1)"
            :disabled="currentPage === 1"
            class="pagination-button"
          >
            ← Previous
          </button>
          <span class="page-info">Page {{ currentPage }}</span>
          <button 
            @click="changePage(1)"
            class="pagination-button"
          >
            Next →
          </button>
        </div>
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
.toggle-button {
  padding: 10px 15px;
  border-radius: 8px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 8px;
}

.toggle-button {
  background-color: #2196F3;
  color: white;
  border: none;
}

.toggle-button:hover {
  background-color: #1976D2;
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
  .logout-button,
  .toggle-button {
    flex: 1;
    justify-content: center;
  }

  .action-button {
    padding: 15px;
  }
}

.panel-container {
  border: 2px solid #ddd;
  padding: 20px;
  border-radius: 0 0 10px 10px;
  margin-top: -3px;
  min-height: 400px;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  gap: 20px;
}

.loading-container p {
  color: #666;
  font-size: 1.1rem;
  margin: 0;
}

.users-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.users-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: 300px;
}

.user-item {
  animation: fadeInUp 0.3s ease;
}

.no-users {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 200px;
  color: #888;
  font-size: 1.1rem;
}

.no-users p {
  margin: 0;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  padding: 20px 0;
  border-top: 1px solid #eee;
  margin-top: 20px;
}

.pagination-button {
  padding: 10px 20px;
  border: 1px solid #ddd;
  border-radius: 6px;
  background-color: #fff;
  color: #333;
  cursor: pointer;
  transition: all 0.2s ease;
  font-weight: 500;
}

.pagination-button:hover:not(:disabled) {
  background-color: #f5f5f5;
  border-color: #ccc;
  transform: translateY(-1px);
}

.pagination-button:disabled {
  background-color: #f9f9f9;
  color: #ccc;
  cursor: not-allowed;
  transform: none;
}

.page-info {
  padding: 10px 15px;
  background-color: #4CAF50;
  color: white;
  border-radius: 6px;
  font-weight: 500;
  min-width: 80px;
  text-align: center;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.centered {
  display: flex;
  flex-direction: row;
  justify-content: center;
}
</style>