<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useAuthStore } from '@/stores/auth.store';
import { useUserStore } from '@/stores/user.store';
import { useTransactionStore } from '@/stores/transaction.store';
import UserItem from '../components/EmployeeUserItem.vue';
import { Account, Transaction, User } from '@/models';
import { useRoute } from 'vue-router';
import LimitModal from '../components/modals/LimitModal.vue'
import { useAccountStore } from '@/stores';



// User reactive state
const authStore = useAuthStore();
const userStore = useUserStore();
const accountStore = useAccountStore();
const transactionStore = useTransactionStore();

// Check if filters are active
const hasActiveFilters = computed(() => transactionStore.hasActiveFilters);
const currentFilters = computed(() => transactionStore.currentFilters);

// Get sorted transactions from store
const sortedTransactions = computed(() => transactionStore.sortedTransactions);

const formatDate = (date: Date | string): string => {
  const dateObj = date instanceof Date ? date : new Date(date);
  return new Intl.DateTimeFormat('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: 'numeric',
    minute: '2-digit'
  }).format(dateObj);
};

const isLoading = ref(true);
const error = ref('');
const route = useRoute();
const userId = route.params.id;
const showLimitModal = ref(false);
let selectedAccount: Account;
let user: User | null = null;

const openLimitModal = () => {
  showLimitModal.value = true;
  console.log("opening");
  console.log(showLimitModal);
}
const closeLimitModal = () => {
  showLimitModal.value = false;
  console.log("closing");
}
const editLimits = async (values: {}) => {
  accountStore.editLimits(values);
}

const handleLogout = () => {
  authStore.logout();
};
const selectAccount = async (accountId: Number) => {
  selectedAccount = user.accounts.filter(acc => acc.id === accountId)[0];
  try {
    isLoading.value = true;
    error.value = '';

    // Then fetch transactions based on selected account
    if (selectedAccount.accountNumber) {
      await transactionStore.fetchTransactionsByAccount(selectedAccount.accountNumber);
    } else {
      await transactionStore.fetchAllTransactions();
    }
  } catch (err: any) {
    console.error('Error fetching dashboard data:', err);
    error.value = err.message || 'Failed to load dashboard data';
  } finally {
    isLoading.value = false;
  }
};

onMounted(async () => {
  // Validate authentication token
  try {
    await authStore.validateToken();
    user = await userStore.getUserById(Number(userId));
    console.log(user);
    //Clear transactions if user has no accounts
    transactionStore.clearTransactions();
    if (user.accounts.length > 0) {
      selectAccount(user.accounts[0].id);
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

// Transaction display helpers
const getTransactionDescription = (transaction: Transaction): string => {
  const userAccountNumbers = user.accounts.map(acc => acc.accountNumber);

  let isIncoming = userAccountNumbers.includes(transaction.destinationAccount.accountNumber) &&
      !userAccountNumbers.includes(transaction.sourceAccount.accountNumber);

  let isOutgoing = userAccountNumbers.includes(transaction.sourceAccount.accountNumber) &&
      !userAccountNumbers.includes(transaction.destinationAccount.accountNumber);

  let isInternal = userAccountNumbers.includes(transaction.sourceAccount.accountNumber) &&
      userAccountNumbers.includes(transaction.destinationAccount.accountNumber);

  if (isIncoming) {
    return `From: ${transaction.sourceAccount.accountName} (${transaction.sourceAccount.accountNumber})`;
  } else if (isOutgoing) {
    return `To: ${transaction.destinationAccount.accountName} (${transaction.destinationAccount.accountNumber})`;
  } else if (isInternal) {
    return `Transfer: ${transaction.sourceAccount.accountName} → ${transaction.destinationAccount.accountName}`;
  }

  return transaction.description || 'Transaction';
};

const isPositiveTransaction = (transaction: Transaction): boolean => {
  const userAccountNumbers = user.accounts.map(acc => acc.accountNumber);

  if (selectedAccount) {
    // If a specific account is selected
    if (transaction.destinationAccount.accountNumber === selectedAccount.accountNumber) {
      return true; // Money coming in to this account
    }
    if (transaction.sourceAccount.accountNumber === selectedAccount.accountNumber) {
      return false; // Money going out from this account
    }
  } else {
    // If money coming in from external source
    if (userAccountNumbers.includes(transaction.destinationAccount.accountNumber) &&
        !userAccountNumbers.includes(transaction.sourceAccount.accountNumber)) {
      return true;
    }
    // If money going out to external destination
    if (userAccountNumbers.includes(transaction.sourceAccount.accountNumber) &&
        !userAccountNumbers.includes(transaction.destinationAccount.accountNumber)) {
      return false;
    }
    // If internal transfer, it's neutral (but we'll show as positive)
    if (userAccountNumbers.includes(transaction.sourceAccount.accountNumber) &&
        userAccountNumbers.includes(transaction.destinationAccount.accountNumber)) {
      return true;
    }
  }

  return true;
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
      <div v-if="!isLoading" class="accounts-panel">
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
            <div><button  @click="openLimitModal"class="action-button">Edit transfer limits</button>
              <div>Daily transfer limit: {{ selectedAccount.dailyTransferLimit }}</div>
              <div>Daily withdrawal limit: {{selectedAccount.dailyWithdrawalLimit}}</div>
              <div>Absolute transfer limit: {{ selectedAccount.singleTransferLimit}} </div>
              <div>Absolute withdrawal limit: {{selectedAccount.singleWithdrawalLimit}}</div>
            </div>
          </div>
          
          
        </div>
        <div v-if="isLoading || transactionStore.isLoading" class="card-loading">
            <div v-for="i in 4" :key="i" class="skeleton-loader transaction-skeleton">
              <div class="skeleton-line"></div>
              <div class="skeleton-line short"></div>
            </div>
          </div>
          <div v-else-if="sortedTransactions.length === 0" class="no-data">
            <p v-if="hasActiveFilters">
              No transactions found matching the current filters.
            </p>
            <p v-else>
              No transactions found.
            </p>
          </div>
          <ul v-else class="transaction-list">
            <li v-for="transaction in sortedTransactions" :key="transaction.id" class="transaction-item">
              <div class="transaction-info">
                <span class="transaction-date">{{ formatDate(transaction.createAt) }}</span>
                <span class="transaction-description">{{ getTransactionDescription(transaction) }}</span>
                <span class="transaction-details">{{ transaction.description }}</span>
                <span class="transaction-status">{{ transaction.transactionStatus }}</span>
              </div>
              <span v-if="isPositiveTransaction(transaction)" class="transaction-amount positive">
                {{ formatCurrency(transaction.amount, transaction.currency) }}
              </span>
              <span v-else class="transaction-amount negative">
                {{ `-${formatCurrency(transaction.amount, transaction.currency)}` }}
              </span>
            </li>
          </ul>
      </div>
      <LimitModal :show="showLimitModal" :selectedAccount="selectedAccount" @close="closeLimitModal" @edit-complete="editLimits"/>
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
.transaction-list {
  list-style: none;
  padding: 0;
  margin: 0;
  max-height: 400px;
  overflow-y: auto;
}

.transaction-list::-webkit-scrollbar {
  width: 8px;
}

.transaction-list::-webkit-scrollbar-track {
  background: #f5f5f5;
  border-radius: 4px;
}

.transaction-list::-webkit-scrollbar-thumb {
  background: #ddd;
  border-radius: 4px;
}

.transaction-list::-webkit-scrollbar-thumb:hover {
  background: #ccc;
}

.transaction-item {
  display: flex;
  justify-content: space-between;
  padding: 16px 0;
  border-bottom: 1px solid #f0f0f0;
}

.transaction-item:last-child {
  border-bottom: none;
}

.transaction-info {
  display: flex;
  flex-direction: column;
}

.transaction-date {
  font-size: 0.85rem;
  color: #888;
}

.transaction-description {
  margin-top: 6px;
  font-size: 1.05rem;
  font-weight: 500;
}

.transaction-details {
  font-size: 0.85rem;
  color: #777;
  margin-top: 4px;
}

.transaction-status {
  font-size: 0.8rem;
  color: #555;
  margin-top: 3px;
  font-style: italic;
}

.transaction-amount {
  font-weight: 600;
  font-size: 1.1rem;
}

.positive {
  color: #4CAF50;
}

.negative {
  color: #F44336;
}
</style>
