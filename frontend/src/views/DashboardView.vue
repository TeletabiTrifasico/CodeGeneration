<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue';
import { useAuthStore } from '@/stores/auth.store';
import { useAccountStore } from '@/stores/account.store';
import { useTransactionStore } from '@/stores/transaction.store';
import { Account, Transaction } from '@/models';
import AccountDetailsModal from '../components/modals/AccountDetailsModal.vue';
import LimitModal from '../components/modals/LimitModal.vue';
import CreateAccountModal from '../components/modals/CreateAccountModal.vue';
import DeleteAccountModal from '../components/modals/DeleteAccountModal.vue';
import TransferModal from '../components/modals/TransferModal.vue';
import TransactionFilter from '../components/TransactionFilter.vue';

// Get the stores
const authStore = useAuthStore();
const accountStore = useAccountStore();
const transactionStore = useTransactionStore();

const isLoading = ref(true);
const error = ref('');
const showTransferModal = ref(false);
const showCreateAccountModal = ref(false);

// Get sorted transactions from store
const sortedTransactions = computed(() => transactionStore.sortedTransactions);

// Get account data from stores
const user = computed(() => authStore.currentUser);
const isUserEnabled = computed(() => user.value?.enabled ?? false);
const accounts = computed(() => accountStore.allAccounts);
const selectedAccount = computed(() => accountStore.currentAccount);
const accountBalance = computed(() => accountStore.accountBalance);
const currentCurrency = computed(() => {
  // For total balance, always show EUR. For individual account, show account currency
  return selectedAccount.value ? selectedAccount.value.currency : 'EUR';
});

// Check if balance is loading
const isBalanceLoading = computed(() => accountStore.isLoadingTotalBalance);

// Check if filters are active
const hasActiveFilters = computed(() => transactionStore.hasActiveFilters);
const currentFilters = computed(() => transactionStore.currentFilters);

// Format helpers
const formatCurrency = (amount: number, currency: string): string => {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: currency,
  }).format(amount);
};

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

// Dashboard actions
const fetchDashboardData = async () => {
  try {
    isLoading.value = true;
    error.value = '';

    // Fetch accounts first (this will also calculate total balance in EUR)
    await accountStore.fetchAllAccounts();

    // Then fetch transactions based on selected account
    if (selectedAccount.value) {
      await transactionStore.fetchTransactionsByAccount(selectedAccount.value.accountNumber);
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

const fetchFilteredData = async (filters: TransactionFilters) => {
  try {
    isLoading.value = true;
    error.value = '';

    // Fetch filtered transactions for that account
    if (selectedAccount.value) {
      await transactionStore.fetchFilteredTransactionsByAccount(
          selectedAccount.value.accountNumber,
          filters
      );
    } else { //Filter all 
      await transactionStore.fetchFilteredTransactions(filters);
    }
  } catch (err: any) {
    console.error('Error fetching filtered data:', err);
    error.value = err.message || 'Failed to load filtered data';
  } finally {
    isLoading.value = false;
  }
};

// Add pagination state
const transactionsPerPage = 5;
const currentTransactionPage = ref(1);
const totalTransactionPages = ref(1);

// Get all transactions but display paginated results
const paginatedTransactions = computed(() => {
  const startIndex = (currentTransactionPage.value - 1) * transactionsPerPage;
  const endIndex = startIndex + transactionsPerPage;
  return sortedTransactions.value.slice(startIndex, endIndex);
});

// Calculate total pages based on transaction count
const calculateTotalPages = () => {
  if (sortedTransactions.value.length > 0) {
    totalTransactionPages.value = Math.ceil(sortedTransactions.value.length / transactionsPerPage);
  } else {
    totalTransactionPages.value = 1;
  }
};

const changeTransactionPage = (changeAmount: number) => {
  const newPage = currentTransactionPage.value + changeAmount;
  
  // Bounds
  if (newPage >= 1 && newPage <= totalTransactionPages.value) {
    currentTransactionPage.value = newPage;
  }
};

const selectAccount = async (account: Account) => {
  accountStore.setSelectedAccount(account);
  
  try {
    isLoading.value = true;
    error.value = '';

    // Then fetch transactions based on selected account
    if (account.accountNumber) {
      await transactionStore.fetchTransactionsByAccount(account.accountNumber);
    } else {
      await transactionStore.fetchAllTransactions();
    }
    
    // Reset to first page when changing accounts
    currentTransactionPage.value = 1;
    calculateTotalPages();
  } catch (err: any) {
    console.error('Error fetching dashboard data:', err);
    error.value = err.message || 'Failed to load dashboard data';
  } finally {
    isLoading.value = false;
  }
};

// Recalculate pages when transactions change
watch(
  () => sortedTransactions.value.length,
  () => {
    calculateTotalPages();
    if (currentTransactionPage.value > totalTransactionPages.value) {
      currentTransactionPage.value = Math.max(1, totalTransactionPages.value);
    }
  }
);

// Transfer modal methods
const openTransferModal = () => {
  showTransferModal.value = true;
};

const closeTransferModal = () => {
  showTransferModal.value = false;
};

const handleTransferComplete = async () => {
  await refreshData();
};

// Create account modal methods
const openCreateAccountModal = () => {
  showCreateAccountModal.value = true;
};

const closeCreateAccountModal = () => {
  showCreateAccountModal.value = false;
};

const handleAccountCreated = async (newAccount: Account) => {
  // Close modal
  showCreateAccountModal.value = false;
  
  // Refresh account data
  await accountStore.fetchAllAccounts();
  
  // Select the new account
  accountStore.setSelectedAccount(newAccount);
};

// Watch for changes to selectedAccount to refresh transactions
watch(() => accountStore.currentAccount, async () => {
  if (hasActiveFilters.value) {
    // Re-apply current filters for the new account selection
    await fetchFilteredData(currentFilters.value);
  } else {
    if (selectedAccount.value) {
      await transactionStore.fetchTransactionsByAccount(selectedAccount.value.accountNumber);
    } else {
      await transactionStore.fetchAllTransactions();
    }
  }
});

onMounted(async () => {
  try {
    await authStore.validateToken();
    await fetchDashboardData();
  } catch (err) {
    console.error('Token validation error:', err);
    authStore.logout();
  }

  calculateTotalPages();
});

const getTransactionDescription = (transaction: Transaction): string => {
  // Handle ATM transactions first
  if (transaction.transactionType === 'ATM_DEPOSIT') {
    return 'ATM Deposit';
  } else if (transaction.transactionType === 'ATM_WITHDRAWAL') {
    return 'ATM Withdrawal';
  }

  const userAccountNumbers = accounts.value.map(acc => acc.accountNumber);

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
  // Handle ATM transactions first based on transaction type
  if (transaction.transactionType === 'ATM_DEPOSIT') {
    return true;  // Deposits are always positive (money coming in)
  } else if (transaction.transactionType === 'ATM_WITHDRAWAL') {
    return false; // Withdrawals are always negative (money going out)
  }

  // If a specific account is selected, use that to determine direction
  if (selectedAccount.value) {
    if (transaction.destinationAccount.accountNumber === selectedAccount.value.accountNumber) {
      return true; // Money coming in (positive)
    }
    if (transaction.sourceAccount.accountNumber === selectedAccount.value.accountNumber) {
      return false; // Money going out (negative)
    }
  } else {
    // No specific account selected, show all user transactions
    
    // Money coming in from outside (positive)
    if (userAccountNumbers.includes(transaction.destinationAccount.accountNumber) &&
        !userAccountNumbers.includes(transaction.sourceAccount.accountNumber)) {
      return true;
    }
    
    // Money going out (negative)
    if (userAccountNumbers.includes(transaction.sourceAccount.accountNumber) &&
        !userAccountNumbers.includes(transaction.destinationAccount.accountNumber)) {
      return false;
    }
    
    // Internal transfer between accounts - show destination as positive
    if (userAccountNumbers.includes(transaction.sourceAccount.accountNumber) &&
        userAccountNumbers.includes(transaction.destinationAccount.accountNumber)) {
      return true;
    }
  }
  
  // Default fallback - if we can't determine, show as positive
  return true;
};

// Add the handleFiltersChanged method
const handleFiltersChanged = async (filters) => {
  await fetchFilteredData(filters);
  // Reset to first page when filters change
  currentTransactionPage.value = 1;
  calculateTotalPages();
};

// Add the refreshData method if missing
const refreshData = async () => {
  await fetchDashboardData();
  // Reset to first page
  currentTransactionPage.value = 1;
  calculateTotalPages();
};
</script>

<template>
  <div class="dashboard-container">
    <!-- Header with user info -->    
     <header class="dashboard-header">
      <div class="user-welcome">
        <h1>Welcome, {{ authStore.currentUser ? authStore.currentUser.name : 'User' }}!</h1>
      </div>
    </header>

    <!-- Main dashboard content -->
    <div v-if="error" class="error-panel">
      <p>{{ error }}</p>
      <button @click="refreshData" class="action-button">Try Again</button>
    </div>

    <div v-else class="dashboard-content">
      <!-- Account Selection Panel -->
      <div class="accounts-panel">
        <div class="accounts-header">
          <h2>Your Accounts</h2>
          <div class="header-actions">

            <button
                @click="openCreateAccountModal"
                class="create-account-btn primary">
              <span class="action-icon">+</span>
              Create Account
            </button>
          </div>
        </div>
        
        <!-- Account skeleton loaders -->
        <div v-if="isLoading || accountStore.isLoading" class="card-loading">
          <div v-for="i in 3" :key="`acc-skeleton-${i}`" class="skeleton-loader account-skeleton"></div>
        </div>
        
        <div v-else-if="accounts.length === 0" class="no-accounts-state">
          <div class="empty-state-icon">🏦</div>
          <h3>No accounts yet</h3>
          <p class="get-started-message">
            Create your first account to start banking with us.
          </p>

        </div>
        <ul v-else class="accounts-list">
          <li
              v-for="(account, index) in accounts"
              :key="`acc-${account.id}-${index}`"
              class="account-item"
              :class="{ 'selected': selectedAccount && selectedAccount.id === account.id }"
              @click="selectAccount(account)"
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
      </div>

      <div class="dashboard-summary">
        <!-- Account balance card -->
        <div class="summary-card balance-card">
          <h2>
            {{ selectedAccount ? selectedAccount.accountName : 'Total Balance' }}
            <span v-if="!selectedAccount" class="base-currency-note">(EUR)</span>
          </h2>
          <div v-if="isLoading || accountStore.isLoading || isBalanceLoading" class="card-loading">
            <div class="skeleton-loader balance-skeleton"></div>
          </div>
          <div v-else class="balance-display">
            <p class="balance">{{ formatCurrency(accountBalance, currentCurrency) }}</p>
          </div>          <div class="transfer w-100 dashboard-actions">
            <button 
              class="action-button" 
              @click="openTransferModal">
              <span class="action-icon">↗</span>
              Transfer
            </button>
            <router-link 
              to="/atm" 
              class="action-button">
              <span class="action-icon">🏧</span>
              ATM 
            </router-link>
          </div>
          
        </div>

        <!-- Recent transactions card -->
        <div class="summary-card transactions-card">
            <div class="transactions-header">
            <h2>
              {{ selectedAccount ? 'Account Transactions' : 'Recent Transactions' }}
              <span v-if="hasActiveFilters" class="filter-indicator">
              (Filtered)
              </span>
            </h2>
            </div>

          <!-- Transaction Filter Component -->
          <TransactionFilter
              :initial-filters="currentFilters"
              @filters-changed="handleFiltersChanged"
          />

          <!-- Transaction skeleton loaders -->
          <div v-if="isLoading || transactionStore.isLoading" class="card-loading">
            <div v-for="i in 4" :key="`tx-skeleton-${i}-${currentTransactionPage}`" class="skeleton-loader transaction-skeleton">
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
            <li v-for="(transaction, index) in paginatedTransactions" :key="`tx-${transaction.id}-${index}`" class="transaction-item">
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
          <!-- Pagination Controls -->
          <div class="pagination">
  <button 
    @click="changeTransactionPage(-1)"
    :disabled="currentTransactionPage === 1"
    class="pagination-button"
  >
    ← Previous
  </button>
  <span class="page-info">
    Page {{ currentTransactionPage }} of {{ totalTransactionPages }}
  </span>
  <button 
    @click="changeTransactionPage(1)"
    :disabled="currentTransactionPage === totalTransactionPages"
    class="pagination-button"
  >
    Next →
  </button>
</div>        </div>
      </div>
    </div>    <!-- Modals -->
    <TransferModal
        :show="showTransferModal"
        :selected-account="selectedAccount"
        @close="closeTransferModal"
        @transfer-complete="handleTransferComplete"
    />
    
    <CreateAccountModal
        :is-open="showCreateAccountModal"
        @close="closeCreateAccountModal"
        @account-created="handleAccountCreated"
    />
  </div>
</template>

<style scoped>
.dashboard-container {
  max-width: 1200px;
  width: 100%;
  margin: 0 auto;
  padding: 30px 20px;
}

.dashboard-header {
  margin-bottom: 40px;
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

.refresh-button, .view-all-button {
  padding: 8px 12px;
  border-radius: 6px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.9rem;
  background-color: #f8f9fa;
  color: #495057;
  border: 1px solid #dee2e6;
  text-decoration: none;
}

.refresh-button:hover:not(:disabled), 
.view-all-button:hover {
  background-color: #e9ecef;
  border-color: #adb5bd;
}

.refresh-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.transfer {
  margin-top: auto;
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
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
  to { transform: rotate(360deg); }
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

.dashboard-content {
  animation: fadeIn 0.5s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

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

.header-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

/* Create Account Button Styles */
.create-account-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  border: none;
  border-radius: 8px;
  font-weight: 500;
  font-size: 0.95rem;
  cursor: pointer;
  transition: all 0.3s ease;
  text-decoration: none;
}

.create-account-btn.primary {
  background: linear-gradient(135deg, #4CAF50, #45a049);
  color: white;
  box-shadow: 0 2px 8px rgba(76, 175, 80, 0.3);
}

.create-account-btn.primary:hover:not(:disabled) {
  background: linear-gradient(135deg, #45a049, #3e8e41);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(76, 175, 80, 0.4);
}

.create-account-btn.large {
  padding: 14px 24px;
  font-size: 1.1rem;
  border-radius: 10px;
}

.create-account-btn:disabled {
  background: #e0e0e0;
  color: #999;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.create-account-btn .action-icon {
  font-size: 1.2em;
  font-weight: bold;
}

/* No Accounts State */
.no-accounts-state {
  text-align: center;
  padding: 60px 40px;
  background: linear-gradient(135deg, #f8f9fa, #e9ecef);
  border-radius: 16px;
  border: 2px dashed #dee2e6;
  margin: 20px 0;
}

.empty-state-icon {
  font-size: 4rem;
  margin-bottom: 20px;
  opacity: 0.7;
}

.no-accounts-state h3 {
  font-size: 1.5rem;
  color: #333;
  margin: 0 0 12px 0;
  font-weight: 600;
}

.no-accounts-state p {
  color: #666;
  margin: 0 0 24px 0;
  font-size: 1rem;
  line-height: 1.5;
}

.get-started-message {
  color: #555 !important;
  font-weight: 500;
}

.approval-message {
  color: #856404 !important;
  background: #fff3cd;
  padding: 12px 16px;
  border-radius: 8px;
  border: 1px solid #ffeaa7;
  display: inline-block;
  margin: 16px 0 24px 0 !important;
}

.approval-status {
  margin-top: 20px;
}

.status-badge {
  display: inline-block;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 0.9rem;
  font-weight: 500;
}

.status-badge.pending {
  background: #fff3cd;
  color: #856404;
  border: 1px solid #ffeaa7;
}

.accounts-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
}

.account-item {
  flex: 1;
  min-width: 250px;
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

.account-skeleton {
  height: 80px;
  margin-bottom: 15px;
}

.dashboard-summary {
  display: grid;
  grid-template-columns: 1fr 2fr;
  gap: 30px;
  margin-bottom: 40px;
}

.summary-card {
  display: flex;
  flex-direction: column;
  background-color: white;
  border-radius: 12px;
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.05);
  padding: 30px;
  transition: all 0.3s ease;
  height: 100%;
}

.summary-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}

.summary-card h2 {
  font-size: 1.4rem;
  color: #555;
  margin-top: 0;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #f0f0f0;
}

.base-currency-note {
  font-size: 0.9rem;
  color: #666;
  font-weight: normal;
}

.transactions-header {
  position: relative;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.filter-indicator {
  font-size: 0.8rem;
  color: #4CAF50;
  font-weight: 500;
  background-color: #e8f5e8;
  padding: 2px 8px;
  border-radius: 12px;
  margin-left: 8px;
}

.balance-display {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.balance {
  font-size: 2.5rem;
  font-weight: bold;
  color: #4CAF50;
  margin: 0;
}

.balance-breakdown {
  background-color: #f8f9fa;
  padding: 15px;
  border-radius: 8px;
  border-left: 4px solid #4CAF50;
}

.breakdown-note {
  font-size: 0.9rem;
  color: #666;
  margin: 0 0 10px 0;
  font-weight: 500;
}

.currency-breakdown {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.breakdown-item {
  background-color: white;
  padding: 6px 12px;
  border-radius: 6px;
  font-size: 0.85rem;
  color: #555;
  border: 1px solid #e0e0e0;
  font-weight: 500;
}

.account-number-display {
  font-size: 1rem;
  color: #777;
  margin-top: 10px;
}

.exchange-rate-note {
  font-size: 0.8rem;
  color: #888;
  margin-top: 10px;
  font-style: italic;
}

.skeleton-loader {
  background: linear-gradient(90deg, #f0f0f0 25%, #e8e8e8 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: loading 1.5s infinite;
  border-radius: 4px;
}

.balance-skeleton {
  height: 50px;
  width: 70%;
}

.transaction-skeleton {
  margin-bottom: 20px;
}

.skeleton-line {
  height: 18px;
  width: 100%;
  margin-bottom: 8px;
}

.skeleton-line.short {
  width: 60%;
}

@keyframes loading {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
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

.dashboard-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  width: 100%;
}

.action-button {
  padding: 15px 20px;
  background-color: #4CAF50;
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  flex: 1;
  min-width: 140px;
  text-decoration: none;
  box-shadow: 0 2px 8px rgba(76, 175, 80, 0.2);
}

.action-button:hover {
  background-color: #43a047;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(76, 175, 80, 0.3);
}

.action-button:active {
  transform: translateY(0);
  box-shadow: 0 2px 6px rgba(76, 175, 80, 0.2);
}

.action-icon {
  font-size: 1.2rem;
}

/* Pagination Styles */
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
  min-width: 120px;
  text-align: center;
}

/* Responsive Styles */
@media (min-width: 1400px) {
  .dashboard-container {
    padding: 50px 20px;
  }

  h1 {
    font-size: 2.4rem;
  }
}

@media (min-width: 992px) and (max-width: 1399px) {
  .dashboard-container {
    padding: 40px 20px;
  }
}

@media (min-width: 768px) and (max-width: 991px) {
  .dashboard-summary {
    grid-template-columns: 1fr 1.5fr;
  }

  .summary-card {
    padding: 25px;
  }

  .balance {
    font-size: 2.2rem;
  }

  .currency-breakdown {
    flex-direction: column;
    gap: 8px;
  }

  .action-button {
    padding: 12px 16px;
    font-size: 0.95rem;
    min-width: 120px;
  }
}

@media (min-width: 576px) and (max-width: 767px) {
  h1 {
    font-size: 1.8rem;
  }

  .dashboard-summary {
    grid-template-columns: 1fr;
    gap: 20px;
  }

  .summary-card {
    padding: 25px 20px;
  }

  .currency-breakdown {
    flex-direction: column;
    gap: 8px;
  }

  .action-button {
    padding: 12px 15px;
    font-size: 0.9rem;
    min-width: 110px;
  }

  .dashboard-actions {
    gap: 10px;
  }
}

@media (max-width: 575px) {
  .dashboard-container {
    padding: 20px 15px;
  }

  .dashboard-header {
    margin-bottom: 30px;
  }

  h1 {
    font-size: 1.7rem;
  }

  .dashboard-summary {
    grid-template-columns: 1fr;
    gap: 20px;
  }

  .summary-card {
    padding: 20px;
  }

  .balance {
    font-size: 2rem;
  }

  .accounts-list {
    flex-direction: column;
  }

  .account-item {
    min-width: auto;
  }

  .currency-breakdown {
    flex-direction: column;
    gap: 6px;
  }

  .breakdown-item {
    text-align: center;
  }

  .dashboard-actions {
    flex-direction: column;
    gap: 10px;
  }

  .action-button {
    padding: 14px 15px;
    font-size: 0.9rem;
    min-width: auto;
    width: 100%;
  }

  .transactions-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
  .refresh-button {
    padding: 8px 12px;
    font-size: 0.85rem;
  }

  .header-actions {
    flex-direction: column;
    gap: 8px;
    width: 100%;
  }

  .create-account-btn {
    width: 100%;
    justify-content: center;
  }

  .no-accounts-state {
    padding: 40px 20px;
  }

  .no-accounts-state h3 {
    font-size: 1.3rem;
  }

  .empty-state-icon {
    font-size: 3rem;
  }
}
</style>