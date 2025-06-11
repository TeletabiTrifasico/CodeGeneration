<script setup lang="ts">
import { ref, onMounted, computed, onBeforeUnmount } from 'vue';
import { useAuthStore } from '@/stores/auth.store';
import { useUserStore } from '@/stores/user.store';
import { useTransactionStore } from '@/stores/transaction.store';
import UserItem from '../components/EmployeeUserItem.vue';
import { Account, Transaction, User } from '@/models';
import { useRoute, useRouter, onBeforeRouteLeave } from 'vue-router';
import LimitModal from '../components/modals/LimitModal.vue'
import CreateAccountModal from '../components/modals/CreateAccountModal.vue'
import DeleteAccountModal from '../components/modals/DeleteAccountModal.vue'
import TransferModal from '../components/modals/TransferModal.vue'
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
const router = useRouter();
const userId = route.params.id;
const showLimitModal = ref(false);
const showTransferModal = ref(false);
const showCreateAccountModal = ref(false);
const showDeleteAccountModal = ref(false);
let selectedAccount: Account;
let user: User | null = null;

const openLimitModal = () => {
  showLimitModal.value = true;
}
const closeLimitModal = () => {
  showLimitModal.value = false;
  refreshData();
}
const openTransferModal = () => {
  showTransferModal.value = true;
}
const closeTransferModal = () => {
  showTransferModal.value = false;
  refreshData();
}
const openCreateAccountModal = () => {
  showCreateAccountModal.value = true;
}
const closeCreateAccountModal = () => {
  showCreateAccountModal.value = false;
}

const openDeleteAccountModal = () => {
  showDeleteAccountModal.value = true;
}
const closeDeleteAccountModal = () => {
  showDeleteAccountModal.value = false;
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
const refreshData = () => {
  loadData();
}
const loadData = async () => {
  await authStore.validateToken();
  user = await userStore.getUserById(Number(userId));
  //Clear transactions in case user has no accounts
  transactionStore.clearTransactions();
  if (user.accounts.length > 0) {
    selectAccount(user.accounts[0].id);
  }
}

onMounted(async () => {
  // Validate authentication token
  try {
    await loadData();
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

const goBackToEmployeePanel = () => {
  // Force a refresh when navigating back
  router.push('/employeePanel?refresh=' + Date.now());
};

onBeforeRouteLeave((to, from, next) => {
  transactionStore.clearTransactions();
  isLoading.value = true;
  next();
});
</script>

<template>
  <div class="view-container">
    <!-- Main panel content -->
    <div v-if="error" class="error-panel">
      <p>{{ error }}</p>
      <button @click="refreshData" class="action-button">Try Again</button>
    </div>

    <div v-else>
      <!-- Show loading spinner -->
      <div v-if="isLoading" class="panel-container">
        <span class="spinner"></span>
      </div>
      
      <!-- Content when loaded -->
      <div v-else>
        <!-- Accounts panel -->
        <div class="accounts-panel">
          <div class="panel-header">
            <button @click="goBackToEmployeePanel" class="back-button">
              <span class="back-arrow">←</span>
              <span class="back-text">Back to Panel</span>
            </button>
            <div class="accounts-header">
              <h2>{{ user.name }}'s Accounts</h2>
            </div>
            <div class="action-buttons">
              <button @click="refreshData" class="refresh-button" :disabled="isLoading">
                <span v-if="isLoading" class="spinner small"></span>
                <span v-else>↻</span>
                Refresh
              </button>
            </div>
          </div>

          <div v-if="isLoading" class="card-loading">
            <div v-for="i in 3" :key="i" class="skeleton-loader account-skeleton"></div>
          </div>
          <div v-else-if="user.accounts.length < 1" class="no-data">
            No accounts found.
          </div>
          <div v-if="!isLoading && user.accounts.length" class="accounts-container">
            <ul class="accounts-list">
              <li
                v-for="account in user.accounts"
                :key="account.id"
                class="account-item"
                :class="{ 'selected': selectedAccount && selectedAccount.id === account.id }"
                @click="selectAccount(account.id)"
              >
                <div class="account-info">
                  <span class="account-name">{{ account.accountName }}</span>
                  <span class="account-number">{{ account.accountNumber }}</span>
                  <span class="account-type-badge">{{ account.accountType }}</span>
                </div>
                <span class="account-balance">
                  {{ formatCurrency(account.balance, account.currency) }}
                </span>
              </li>
            </ul>
          </div>
          <div class ="accounts-list">
            <button @click="openCreateAccountModal" class="action-button">
                <span class="action-icon">✏️</span> Create Account
              </button>
              <button @click="openDeleteAccountModal" class="action-button" style="background-color:#d32f2f;">
                <span class="action-icon">🗑️</span> Close Account
              </button>
              <button class="action-button" @click="openTransferModal"><span class="action-icon">↗</span>Transfer</button>
          </div>
          
          <div class="limits-panel">
            <h3>Account Limits</h3>
            <div v-if="selectedAccount != null" class="limits-container">
              <div class="limit-group">
                <div class="limit-item">
                  <span class="limit-label">Daily Transfer Limit</span>
                  <span class="limit-value">{{ formatCurrency(selectedAccount.dailyTransferLimit, selectedAccount.currency) }}</span>
                </div>
                <div class="limit-item">
                  <span class="limit-label">Daily Withdrawal Limit</span>
                  <span class="limit-value">{{ formatCurrency(selectedAccount.dailyWithdrawalLimit, selectedAccount.currency) }}</span>
                </div>
              </div>
              <div class="limit-group">
                <div class="limit-item">
                  <span class="limit-label">Single Transfer Limit</span>
                  <span class="limit-value">{{ formatCurrency(selectedAccount.singleTransferLimit, selectedAccount.currency) }}</span>
                </div>
                <div class="limit-item">
                  <span class="limit-label">Single Withdrawal Limit</span>
                  <span class="limit-value">{{ formatCurrency(selectedAccount.singleWithdrawalLimit, selectedAccount.currency) }}</span>
                </div>
              </div>
              <button @click="openLimitModal" class="action-button">
                <span class="action-icon">✏️</span> Edit Limits
              </button>
            </div>
          </div>
          
        </div>
        
        <!-- Transactions section -->
        <div class="transactions-panel">
          <div class="panel-header">
            <div class="transactions-header">
              <h2>Transaction History</h2>
              <span class="account-badge" v-if="selectedAccount">
                {{ selectedAccount.accountName }}
              </span>
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
              No transactions found for this account.
            </p>
          </div>
          <ul v-else class="transaction-list">
            <li v-for="transaction in sortedTransactions" :key="transaction.id" class="transaction-item">
              <div class="transaction-info">
                <span class="transaction-date">{{ formatDate(transaction.createAt) }}</span>
                <span class="transaction-description">{{ getTransactionDescription(transaction) }}</span>
                <span class="transaction-details">{{ transaction.description }}</span>
                <span class="transaction-status" :class="transaction.transactionStatus.toLowerCase()">
                  {{ transaction.transactionStatus }}
                </span>
              </div>
              <span v-if="isPositiveTransaction(transaction)" class="transaction-amount positive">
                <span class="transaction-icon">+</span>
                {{ formatCurrency(transaction.amount, transaction.currency) }}
              </span>
              <span v-else class="transaction-amount negative">
                <span class="transaction-icon">-</span>
                {{ formatCurrency(transaction.amount, transaction.currency) }}
              </span>
            </li>
          </ul>
        </div>
      </div>
    </div>
    <LimitModal v-if="showLimitModal" :selectedAccount="selectedAccount" @close="closeLimitModal" @edit-complete="refreshData"/>
    <TransferModal :show="showTransferModal" :selectedAccount="selectedAccount" :byEmployee="true" @close="closeTransferModal" @transfer-complete="refreshData"/>
    <CreateAccountModal :isOpen="showCreateAccountModal" :user="user ?? undefined" @close="closeCreateAccountModal" @account-created="() => { closeCreateAccountModal(); refreshData(); }"/>
    <DeleteAccountModal :show="showDeleteAccountModal" :selectedAccount="selectedAccount" @close="closeDeleteAccountModal" @delete-complete="() => { closeDeleteAccountModal(); refreshData(); }"/>
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
.panel-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  gap: 16px;
}

.accounts-header {
  flex: 1;
}

.accounts-header h2 {
  font-size: 1.4rem;
  color: #555;
  margin: 0;
}
.accounts-container {
  display: flex;
  width: 100%;
  overflow-x: auto;
  padding: 10px 0;
}
.editButtons {
  margin:10px;
}
.accounts-list {
  display: flex;
  flex-direction: row;
  gap: 20px;
  width: 100%;
  padding: 5px;
  list-style: none;
}

.account-item {
  flex: 0 0 300px;
  min-width: 250px;
  max-width: 350px;
  background-color: #f9f9f9;
  border-radius: 12px;
  padding: 20px;
  border: 2px solid transparent;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
}

.account-item:hover {
  background-color: #f0f0f0;
  transform: translateY(-3px);
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.08);
}

.account-item.selected {
  border-color: #4CAF50;
  background-color: #f0f8f0;
  box-shadow: 0 4px 12px rgba(76, 175, 80, 0.2);
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
.account-type-badge {
  display: inline-block;
  padding: 4px 8px;
  background-color: #e8f5e8;
  color: #4CAF50;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 500;
  margin-top: 8px;
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
  padding: 14px 20px;
  background-color: #4CAF50;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  align-self: flex-start;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.action-button:hover {
  background-color: #43a047;
  transform: translateY(-2px);
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.15);
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
  padding: 18px 16px;
  border-radius: 8px;
  transition: all 0.2s ease;
}

.transaction-item:hover {
  background-color: #f9f9f9;
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
  display: inline-block;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 0.7rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  background-color: #f0f0f0;
  color: #666;
}

.transaction-status.completed {
  background-color: #e8f5e8;
  color: #2e7d32;
}

.transaction-status.pending {
  background-color: #fff8e1;
  color: #ff8f00;
}

.transaction-status.failed {
  background-color: #ffebee;
  color: #c62828;
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
}

.logout-button:hover {
  background-color: #d32f2f;
  transform: translateY(-2px);
}

.spinner {
  display: inline-block;
  width: 48px; /* Larger spinner for better visibility */
  height: 48px;
  border: 3px solid rgba(0, 0, 0, 0.1);
  border-radius: 50%;
  border-top-color: #3f51b5; /* Match the blue color of your back button */
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.header-actions {
  display: flex;
  gap: 15px;
}
.limits-panel, .transfer-panel {
  background-color: white;
  border-radius: 12px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.05);
  padding: 25px;
  margin: 20px 0;
  transition: all 0.3s ease;
}

.limits-panel h3, .transfer-panel h3 {
  font-size: 1.2rem;
  color: #555;
  margin-top: 0;
  margin-bottom: 20px;
  border-bottom: 1px solid #eee;
  padding-bottom: 10px;
}

.limits-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.limit-group {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
}

.limit-item {
  flex: 1;
  min-width: 200px;
  background-color: #f8f8f8;
  border-radius: 8px;
  padding: 15px;
  border-left: 4px solid #4CAF50;
}

.limit-label {
  display: block;
  font-size: 0.85rem;
  color: #666;
  margin-bottom: 8px;
}

.limit-value {
  display: block;
  font-size: 1.2rem;
  font-weight: 600;
  color: #333;
}

.badge {
  display: inline-block;
  padding: 4px 8px;
  background-color: #e8f5e8;
  color: #4CAF50;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 500;
  margin-top: 8px;
}

/* Scrollbar styling for account container */
.accounts-container::-webkit-scrollbar {
  height: 8px;
}

.accounts-container::-webkit-scrollbar-track {
  background: #f5f5f5;
  border-radius: 4px;
}

.accounts-container::-webkit-scrollbar-thumb {
  background: #ddd;
  border-radius: 4px;
}

.accounts-container::-webkit-scrollbar-thumb:hover {
  background: #ccc;
}

/* Transaction Panel Styling */
.transactions-panel {
  background-color: white;
  border-radius: 12px;
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.05);
  padding: 25px;
  margin-bottom: 30px;
  transition: all 0.3s ease;
}

.transactions-panel:hover {
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}

.transactions-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.transactions-header h2 {
  font-size: 1.4rem;
  color: #555;
  margin: 0;
}

.account-badge {
  display: inline-block;
  padding: 4px 10px;
  background-color: #e8f5e8;
  color: #4CAF50;
  border-radius: 20px;
  font-size: 0.9rem;
  font-weight: 500;
}

.transaction-item {
  padding: 18px 16px;
  border-bottom: 1px solid #f0f0f0;
  border-radius: 8px;
  transition: all 0.2s ease;
}

.transaction-item:hover {
  background-color: #f9f9f9;
}

.transaction-status {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 0.7rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  background-color: #f0f0f0;
  color: #666;
}

.transaction-status.completed {
  background-color: #e8f5e8;
  color: #2e7d32;
}

.transaction-status.pending {
  background-color: #fff8e1;
  color: #ff8f00;
}

.transaction-status.failed {
  background-color: #ffebee;
  color: #c62828;
}

.transaction-icon {
  display: inline-block;
  margin-right: 4px;
  font-weight: bold;
}

.card-loading {
  padding: 20px 0;
}

.transaction-skeleton {
  height: 70px;
  margin-bottom: 12px;
  padding: 15px;
  border-radius: 8px;
}

.skeleton-line {
  height: 14px;
  margin-bottom: 10px;
  border-radius: 4px;
}

.skeleton-line.short {
  width: 60%;
}

@keyframes loading {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

/* Responsive Adjustments */
@media (max-width: 768px) {
  .panel-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
  
  .accounts-header {
    width: 100%;
  }
  
  .action-buttons {
    width: 100%;
  }
  
  .back-button, 
  .refresh-button, 
  .logout-button {
    padding: 8px 12px;
  }
  .transactions-panel {
    padding: 20px 15px;
  }
  
  .transaction-item {
    padding: 15px 10px;
  }
  
  .transaction-info {
    max-width: 70%;
  }
}

@media (max-width: 480px) {
  .accounts-panel, 
  .limits-panel {
    padding: 20px 15px;
  }
  
  .limit-item {
    padding: 12px;
  }
  
  .accounts-header h2 {
    font-size: 1.2rem;
  }
  .transactions-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .transaction-amount {
    font-size: 1rem;
  }
}
.back-button {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  background: linear-gradient(135deg, #3f51b5, #303f9f);
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 6px rgba(63, 81, 181, 0.3);
}

.back-button:hover {
  background: linear-gradient(135deg, #303f9f, #1a237e);
  transform: translateY(-2px);
  box-shadow: 0 4px 10px rgba(63, 81, 181, 0.4);
}

.back-arrow {
  font-size: 1.2rem;
}

.back-text {
  font-weight: 500;
}
.panel-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 60vh;
  width: 100%;
}
</style>
