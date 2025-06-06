<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue';
import { useAuthStore } from '@/stores/auth.store';
import { useAccountStore } from '@/stores/account.store';
import { useTransactionStore } from '@/stores/transaction.store';
import { Account, Transaction } from '@/models';
import { TransactionFilters } from '@/services/api.config';
import TransferModal from "@/components/modals/TransferModal.vue";
import TransactionFilter from "@/components/TransactionFilter.vue";

// Get the stores
const authStore = useAuthStore();
const accountStore = useAccountStore();
const transactionStore = useTransactionStore();

const isLoading = ref(true);
const error = ref('');
const showTransferModal = ref(false);

// Get sorted transactions from store
const sortedTransactions = computed(() => transactionStore.sortedTransactions);

// Get account data from stores
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

    if (selectedAccount.value) {
      await transactionStore.fetchFilteredTransactionsByAccount(
          selectedAccount.value.accountNumber,
          filters
      );
    } else {
      await transactionStore.fetchFilteredTransactions(filters);
    }
  } catch (err: any) {
    console.error('Error fetching filtered data:', err);
    error.value = err.message || 'Failed to load filtered data';
  } finally {
    isLoading.value = false;
  }
};

const selectAccount = (account: Account) => {
  accountStore.setSelectedAccount(account);
  // Clear filters when switching accounts
  transactionStore.clearFilters();
};

const viewAllAccounts = () => {
  accountStore.setSelectedAccount(null);
  // Clear filters when switching to all accounts view
  transactionStore.clearFilters();
};

const refreshData = async () => {
  if (hasActiveFilters.value) {
    await fetchFilteredData(currentFilters.value);
  } else {
    await fetchDashboardData();
  }

  // Also refresh the total balance calculation
  if (!selectedAccount.value) {
    await accountStore.refreshTotalBalance();
  }
};

const handleLogout = () => {
  authStore.logout();
};

const handleFiltersChanged = (filters: TransactionFilters) => {
  const hasFilters = Object.values(filters).some(value =>
      value !== undefined && value !== null && value !== ''
  );

  if (hasFilters) {
    fetchFilteredData(filters);
  } else {
    // No filters, fetch all data
    fetchDashboardData();
  }
};

// Transaction display helpers
const getTransactionDescription = (transaction: Transaction): string => {
  const userAccountNumbers = accounts.value.map(acc => acc.accountNumber);

  // Check if it's an ATM deposit or withdrawal first
  if (transaction.transactionType === 'ATM_DEPOSIT') {
    return `ATM Deposit`;
  } else if (transaction.transactionType === 'ATM_WITHDRAWAL') {
    return `ATM Withdrawal`;
  }

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
  const userAccountNumbers = accounts.value.map(acc => acc.accountNumber);

  if (selectedAccount.value) {
    // If a specific account is selected
    if (transaction.destinationAccount.accountNumber === selectedAccount.value.accountNumber) {
      return true; // Money coming in to this account
    }
    if (transaction.sourceAccount.accountNumber === selectedAccount.value.accountNumber) {
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
});
</script>

<template>
  <div class="dashboard-container">
    <!-- Header with user info and actions -->
    <header class="dashboard-header">
      <div class="user-welcome">
        <h1>Welcome, {{ authStore.currentUser ? authStore.currentUser.name : 'User' }}!</h1>
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
          <button
              v-if="selectedAccount"
              @click="viewAllAccounts"
              class="view-all-button"
          >
            View All
          </button>
        </div>

        <div v-if="isLoading || accountStore.isLoading" class="card-loading">
          <div v-for="i in 3" :key="i" class="skeleton-loader account-skeleton"></div>
        </div>
        <div v-else-if="accounts.length === 0" class="no-data">
          No accounts found.
        </div>
        <ul v-else class="accounts-list">
          <li
              v-for="account in accounts"
              :key="account.id"
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
          </div>          <div class="transfer w-100">
            <button class="action-button" @click="openTransferModal">
              <span class="action-icon">↗</span>
              Transfer Money
            </button>
            <router-link to="/atm" class="action-button">
              <span class="action-icon">🏧</span>
              ATM Access
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
      </div>
    </div>
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

.refresh-button, .logout-button, .view-all-button {
  padding: 10px 15px;
  border-radius: 8px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 8px;
}

.refresh-button, .view-all-button {
  background-color: #f5f5f5;
  color: #333;
  border: 1px solid #ddd;
}

.refresh-button:hover:not(:disabled), .view-all-button:hover {
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

.transfer {
  margin-top: 100%;
  display: flex;
  gap: 10px;
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
  width: 100%;
  flex: 1;
  text-decoration: none;
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
}

@media (min-width: 576px) and (max-width: 767px) {
  .dashboard-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }

  h1 {
    font-size: 1.8rem;
  }

  .dashboard-summary {
    grid-template-columns: 1fr;
    gap: 20px;
  }

  .dashboard-actions {
    grid-template-columns: repeat(3, 1fr);
  }

  .summary-card {
    padding: 25px 20px;
  }

  .currency-breakdown {
    flex-direction: column;
    gap: 8px;
  }
}

@media (max-width: 575px) {
  .dashboard-container {
    padding: 20px 15px;
  }

  .dashboard-header {
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

  .refresh-button, .logout-button {
    flex: 1;
    justify-content: center;
  }

  .dashboard-summary {
    grid-template-columns: 1fr;
    gap: 20px;
  }

  .dashboard-actions {
    grid-template-columns: 1fr;
    gap: 15px;
  }

  .summary-card {
    padding: 20px;
  }

  .balance {
    font-size: 2rem;
  }

  .action-button {
    padding: 15px;
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
}
</style>