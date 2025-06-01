<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useAuthStore } from '@/stores/auth.store';
import { useUserStore } from '@/stores/user.store';
import UserItem from '../components/EmployeeUserItem.vue';
import { useRoute } from 'vue-router';



// User reactive state
const authStore = useAuthStore();
const userStore = useUserStore();
const isLoading = ref(true);
const error = ref('');
const route = useRoute();
const userId = route.params.id;
let selectedAccount = null;

let user = {
  id: Number,
  username: String,
  name: String,
  email: String,
  enabled: true,
  accounts: [] as Account[]
};

interface Account {
  id: Number,
  accountName: String,
  accountNumber: String,
  accountType: String,
  balance: Number,
  currency: String,
  dailyTransferLimit: Number,
  dailyWithdrawalLimit: Number,
  lastLimitResetDate: String,
  singleTransferLimit: Number,
  singleWithdrawalLimit: Number,
}


const handleLogout = () => {
  authStore.logout();
};
const selectAccount = (accountId: Number) => {
  selectedAccount = user.accounts.filter(acc => acc.id === accountId)
};

onMounted(async () => {
  // Validate authentication token
  try {
    await authStore.validateToken();
    user = await userStore.getUserById(Number(userId));
    if (user.accounts.length > 0) {
      selectedAccount = user.accounts[0];
    }
  } catch (err) {
    console.error('Token validation error:', err);
    // authStore.logout() will be called in validateToken if it fails
  }
  isLoading.value = false;
});

const formatCurrency = (amount: number, currency: string): string => {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: currency,
  }).format(amount);
};
</script>

<template>
    <div class="view-container">
      <!-- Header with user info and actions -->
      <header class="panel-header">
        <div class="user-welcome">
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
      </div>
      <div class="accounts-panel">
        <div class="accounts-header">
          <h2>{{ user.name }}'s Accounts</h2>
        </div>

        <div v-if="isLoading" class="card-loading">
          <div v-for="i in 3" :key="i" class="skeleton-loader account-skeleton"></div>
        </div>
        <!-- Currently causes issues because user.accounts might not be given a value yet-->
        <div v-else-if="user.accounts.length < 1" class="no-data">
          No accounts found.
        </div>
        <div v-if="!isLoading && user.accounts.length" class="accounts-container">
          <ul v-if="!isLoading && user.accounts.length > 0" class="accounts-list">
          <li
              v-for="account in user.accounts"
              :key="account.id"
              class="account-item"
              @click="selectAccount(account.id)"
          >
            <div class="account-info">
              <span class="account-name">{{ account.accountName }}</span>
              <span class="account-number">{{ account.accountNumber }}</span>
              <span class="account-type">{{ account.accountType }}</span>
            </div>
            <span class="account-balance">
              {{ formatCurrency(account.balance, account.currency) }}
            </span>
          </li>
          </ul>
          <div class="editButtons">
            {{ selectedAccount}}
            <div><button class="action-button">Edit daily transfer limit</button><div>Daily transfer limit: {{ selectedAccount.dailyTransferLimit }}</div></div>
            <div><button class="action-button">Edit single transfer limit</button><div>Single transfer limit: {{ selectedAccount.singleTransferLimit }}</div></div>
          </div>
        </div>
      </div>
    </div>
</template>


<style scoped>
/* Accounts Panel */
.accounts-panel {
  background-color: white;
  border-radius: 12px;
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.05);
  padding: 25px;
  margin-bottom: 30px;
  transition: all 0.3s ease;
}

.accounts-panel:hover {
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}
.accounts-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.accounts-header h2 {
  font-size: 1.4rem;
  color: #555;
  margin: 0;
}
.accounts-container {
  display:flex;

}
.editButtons {
  margin:10px;
}
.accounts-list {
  flex-direction: column;
}

.account-item {
  flex: 1;
  min-width: 250px;
  width: 50%;
  background-color: #f9f9f9;
  border-radius: 8px;
  padding: 15px;
  border: 2px solid transparent;
  cursor: pointer;
  transition: all 0.2s ease;
}

.account-item:hover {
  background-color: #f0f0f0;
  transform: translateY(-3px);
}

.account-item.selected {
  border-color: #4CAF50;
  background-color: #f0f8f0;
}
.account-info {
  display: flex;
  flex-direction: column;
  margin-bottom: 10px;
}
.account-name {
  font-weight: 600;
  font-size: 1.1rem;
  color: #333;
}
.account-number {
  font-size: 0.9rem;
  color: #777;
  margin-top: 4px;
}
.account-type {
  font-size: 0.8rem;
  color: #999;
  margin-top: 2px;
  font-style: italic;
}
.account-balance {
  display: block;
  font-weight: 600;
  font-size: 1.2rem;
  color: #4CAF50;
  margin-top: 5px;
}
.skeleton-loader {
  background: linear-gradient(90deg, #f0f0f0 25%, #e8e8e8 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: loading 1.5s infinite;
  border-radius: 4px;
}
.account-skeleton {
  height: 80px;
  margin-bottom: 15px;
}
.no-data {
  color: #888;
  padding: 30px 0;
  text-align: center;
  font-size: 1.1rem;
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
  width: 50%;
}

.action-button:hover {
  background-color: #43a047;
  transform: translateY(-3px);
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.1);
}

.action-button:active {
  transform: translateY(-1px);
}
</style>
