<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import AuthService from '@/services/AuthService';
import requestService from '@/services/RequestService';

interface Transaction {
  id: number;
  date: string;
  description: string;
  amount: number;
}

// User reactive state
const user = ref(AuthService.getCurrentUser());
const accountBalance = ref(0);
const transactions = ref<Transaction[]>([]);
const isLoading = ref(true);
const error = ref('');

// Computed properties
const sortedTransactions = computed(() => {
  return [...transactions.value].sort((a, b) =>
      new Date(b.date).getTime() - new Date(a.date).getTime()
  );
});

const formatCurrency = (amount: number): string => {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD'
  }).format(amount);
};

const formatDate = (dateString: string): string => {
  const date = new Date(dateString);
  return new Intl.DateTimeFormat('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  }).format(date);
};

const fetchDashboardData = async () => {
  try {
    isLoading.value = true;
    error.value = '';

    // Simulate API call - in real app, you'd use:
    // const balanceData = await requestService.get('/account/balance');
    // const transactionsData = await requestService.get('/account/transactions');

    // For demonstration, we'll use setTimeout
    setTimeout(() => {
      accountBalance.value = 2547.63;
      transactions.value = [
        { id: 1, date: '2025-04-18', description: 'Grocery Store', amount: -86.42 },
        { id: 2, date: '2025-04-17', description: 'Salary Deposit', amount: 1500.00 },
        { id: 3, date: '2025-04-15', description: 'Restaurant', amount: -42.75 },
        { id: 4, date: '2025-04-12', description: 'Online Purchase', amount: -129.99 },
        { id: 5, date: '2025-04-10', description: 'Utility Bill', amount: -85.50 },
        { id: 6, date: '2025-04-05', description: 'Transfer from Savings', amount: 200.00 }
      ];
      isLoading.value = false;
    }, 1000);
  } catch (err: any) {
    console.error('Error fetching dashboard data:', err);
    isLoading.value = false;
    error.value = err.message || 'Failed to load dashboard data';
  }
};

const refreshData = () => {
  fetchDashboardData();
};

const handleLogout = () => {
  AuthService.logout();
};

onMounted(() => {
  // Validate authentication token
  AuthService.validateToken()
      .then(userData => {
        if (userData) {
          user.value = userData;
        }
        fetchDashboardData();
      })
      .catch(err => {
        console.error('Token validation error:', err);
        // AuthService.logout() will be called in the validateToken method if it fails
      });
});
</script>

<template>
  <div class="dashboard-container">
    <!-- Header with user info and actions -->
    <header class="dashboard-header">
      <div class="user-welcome">
        <h1>Welcome, {{ user ? user.name : 'User' }}!</h1>
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
      <div class="dashboard-summary">
        <!-- Account balance card -->
        <div class="summary-card balance-card">
          <h2>Account Balance</h2>
          <div v-if="isLoading" class="card-loading">
            <div class="skeleton-loader balance-skeleton"></div>
          </div>
          <p v-else class="balance">{{ formatCurrency(accountBalance) }}</p>
        </div>

        <!-- Recent transactions card -->
        <div class="summary-card transactions-card">
          <h2>Recent Transactions</h2>
          <div v-if="isLoading" class="card-loading">
            <div v-for="i in 4" :key="i" class="skeleton-loader transaction-skeleton">
              <div class="skeleton-line"></div>
              <div class="skeleton-line short"></div>
            </div>
          </div>
          <div v-else-if="transactions.length === 0" class="no-data">
            No recent transactions found.
          </div>
          <ul v-else class="transaction-list">
            <li v-for="transaction in sortedTransactions" :key="transaction.id" class="transaction-item">
              <div class="transaction-info">
                <span class="transaction-date">{{ formatDate(transaction.date) }}</span>
                <span class="transaction-description">{{ transaction.description }}</span>
              </div>
              <span class="transaction-amount" :class="transaction.amount < 0 ? 'negative' : 'positive'">
                {{ formatCurrency(transaction.amount) }}
              </span>
            </li>
          </ul>
        </div>
      </div>

      <!-- Quick actions panel -->
      <div class="dashboard-actions">
        <button class="action-button">
          <span class="action-icon">↗</span>
          Transfer Money
        </button>
        <button class="action-button">
          <span class="action-icon">📄</span>
          Pay Bills
        </button>
        <button class="action-button">
          <span class="action-icon">📊</span>
          View Statements
        </button>
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

.refresh-button, .logout-button {
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

.dashboard-summary {
  display: grid;
  grid-template-columns: 1fr 2fr;
  gap: 30px;
  margin-bottom: 40px;
}

.summary-card {
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

.balance {
  font-size: 2.5rem;
  font-weight: bold;
  color: #4CAF50;
  margin: 0;
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
  font-size: 0.9rem;
  color: #888;
}

.transaction-description {
  margin-top: 6px;
  font-size: 1.05rem;
  font-weight: 500;
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
}
</style>