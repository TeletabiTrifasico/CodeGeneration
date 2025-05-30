<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useAuthStore } from '@/stores/auth.store';
import { useUserStore } from '@/stores/user.store';
import UserItem from '../components/EmployeeUserItem.vue';
import { useRoute } from 'vue-router';



// User reactive state
const authStore = useAuthStore();
const userStore = useUserStore();
let user = {};
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
// You can replace these with actual implementations or router navigations
const openActivateAccounts = () => alert('Open activate accounts UI');
const openTransferLimits = () => alert('Open transfer limits UI');
const openTransferFunds = () => alert('Open transfer funds UI');
const openTransactions = () => alert('Open transactions UI');
const openCreateAccount = () => alert('Open create account UI');

onMounted(async () => {
  // Validate authentication token
  try {
    await authStore.validateToken();
    user = await userStore.getUserById(Number(userId));
    console.log(pageUsers);
  } catch (err) {
    console.error('Token validation error:', err);
    // authStore.logout() will be called in validateToken if it fails
  }
  isLoading.value = false;
});
const route = useRoute();
const userId = route.params.id;
</script>

<template>
    <div>
    <div class="view-container">
      <!-- Header with user info and actions -->
      <header class="panel-header">
        <div class="user-welcome">
          <h1>{{ userId }}</h1>
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
            {{ user }}
          </div>        
      </div>
    </div>
    <section class="employee-overview">

      <!-- Activate Customer Accounts -->
      <section class="task-section">
        <!--<h3>Activate Customer Accounts</h3>-->
        <div class="data-display">
          <p>Active accounts: 24</p>
          <p>Inactive accounts: 8</p>
        </div>
        <button @click="openActivateAccounts" class="action-btn">Manage Account Activation</button>
      </section>

      <!-- Set Transfer Limits -->
      <section class="task-section">
        <h3>Set Transfer Limits</h3>
        <div class="data-display">
          <p>Daily limit: $10,000</p>
          <p>Absolute limit: $50,000</p>
        </div>
        <button @click="openTransferLimits" class="action-btn">Edit Transfer Limits</button>
      </section>

      <!-- Transfer Funds Between Accounts -->
      <section class="task-section">
        <h3>Transfer Funds Between Accounts</h3>
        <div class="data-display">
          <p>Recent transfer: $500 from Account A to Account B</p>
        </div>
        <button @click="openTransferFunds" class="action-btn">Make a Transfer</button>
      </section>

      <!-- View All Transactions -->
      <section class="task-section">
        <h3>View All Transactions</h3>
        <div class="data-display">
          <p>Last 5 transactions:</p>
          <ul>
            <li>Transaction 1 - $200</li>
            <li>Transaction 2 - $1000</li>
            <li>Transaction 3 - $50</li>
            <li>Transaction 4 - $750</li>
            <li>Transaction 5 - $300</li>
          </ul>
        </div>
        <button @click="openTransactions" class="action-btn">View Full Transaction History</button>
      </section>

      <!-- Create Accounts for Customers -->
      <section class="task-section">
        <h3>Create Accounts for Customers</h3>
        <p>Number of new accounts created this month: 5</p>
        <button @click="openCreateAccount" class="action-btn">Create New Account</button>
      </section>

    </section>
  </div>
</template>


<style scoped>
.employee-overview {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px 30px;
  font-family: Arial, sans-serif;
  color: #333;
}

.task-section {
  background: #fff;
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 20px 25px;
  margin-bottom: 25px;
  box-shadow: 0 2px 6px rgba(0,0,0,0.05);
}

.task-section h3 {
  margin-top: 0;
  color: #4caf50;
  font-weight: 700;
  font-size: 1.4rem;
  margin-bottom: 12px;
}

.data-display {
  margin-bottom: 15px;
  font-size: 1rem;
  color: #444;
}

.data-display ul {
  padding-left: 20px;
  margin: 8px 0;
}

.action-btn {
  background-color: #4caf50;
  color: white;
  border: none;
  border-radius: 8px;
  padding: 10px 18px;
  font-size: 1rem;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.action-btn:hover {
  background-color: #43a047;
}
</style>
