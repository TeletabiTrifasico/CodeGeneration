<script lang="ts">
import { defineComponent, ref, onMounted } from 'vue';
import AuthService from '../services/AuthService';
import apiClient from '../services/HttpInterceptor';
import Transaction from '@/models'

export default defineComponent({
  name: 'DashboardView',
  setup() {
    const user = ref(AuthService.getCurrentUser());
    const accountBalance = ref(0);
    const transactions = ref<Transaction[]>([]);
    const isLoading = ref(true);

    onMounted(async () => {
      try {
        // Simulate loading account data from API
        isLoading.value = true;

        // Add an actual API calls to retrieve data!!!

        // Example data:
        setTimeout(() => {
          accountBalance.value = 2547.63;
          transactions.value = [
            { id: 1, date: '2025-04-18', description: 'Grocery Store', amount: -86.42 },
            { id: 2, date: '2025-04-17', description: 'Salary Deposit', amount: 1500.00 },
            { id: 3, date: '2025-04-15', description: 'Restaurant', amount: -42.75 },
            { id: 4, date: '2025-04-12', description: 'Online Purchase', amount: -129.99 }
          ];
          isLoading.value = false;
        }, 0);
      } catch (error) {
        console.error('Error fetching dashboard data:', error);
        isLoading.value = false;
      }
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

    return {
      user,
      accountBalance,
      transactions,
      isLoading,
      formatCurrency,
      formatDate
    };
  }
});
</script>

<template>
  <div class="dashboard-container">
    <h1>Welcome, {{ user ? user.name : 'User' }}!</h1>

    <div class="dashboard-summary">
      <div class="summary-card">
        <h2>Account Balance</h2>
        <p class="balance">{{ formatCurrency(accountBalance) }}</p>
      </div>

      <div class="summary-card">
        <h2>Recent Transactions</h2>
        <div v-if="isLoading" class="loading">Loading transactions...</div>
        <div v-else-if="transactions.length === 0" class="no-data">No recent transactions found.</div>
        <ul v-else class="transaction-list">
          <li v-for="(transaction, index) in transactions" :key="index" class="transaction-item">
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

    <div class="dashboard-actions">
      <button class="action-button">Transfer Money</button>
      <button class="action-button">Pay Bills</button>
      <button class="action-button">View Statements</button>
    </div>
  </div>
</template>


<style scoped>
.dashboard-container {
  max-width: 1400px;
  width: 100%;
  margin: 0 auto;
  padding: 40px 20px;
}

h1 {
  margin-bottom: 40px;
  color: #333;
  font-size: 2.2rem;
}

.dashboard-summary {
  display: grid;
  grid-template-columns: 1fr 2fr;
  gap: 30px;
  margin-bottom: 50px;
}

.summary-card {
  background-color: white;
  border-radius: 10px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
  padding: 30px;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.summary-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.12);
}

.summary-card h2 {
  font-size: 1.4rem;
  color: #555;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.balance {
  font-size: 2.5rem;
  font-weight: bold;
  color: #4CAF50;
}

.loading, .no-data {
  color: #888;
  padding: 25px 0;
  text-align: center;
  font-size: 1.1rem;
}

.transaction-list {
  list-style: none;
}

.transaction-item {
  display: flex;
  justify-content: space-between;
  padding: 16px 0;
  border-bottom: 1px solid #eee;
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
  padding: 16px;
  background-color: #4CAF50;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 1.1rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.action-button:hover {
  background-color: #45a049;
  transform: translateY(-3px);
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.1);
}

@media (min-width: 1400px) {
  .dashboard-container {
    padding: 60px 20px;
  }

  h1 {
    font-size: 2.5rem;
  }
}

@media (min-width: 992px) and (max-width: 1399px) {
  .dashboard-container {
    padding: 50px 20px;
  }
}

@media (min-width: 768px) and (max-width: 991px) {
  .dashboard-summary {
    grid-template-columns: 1fr 1.5fr;
  }

  .dashboard-actions {
    grid-template-columns: repeat(3, 1fr);
  }

  .summary-card {
    padding: 25px;
  }

  .balance {
    font-size: 2.2rem;
  }
}

@media (min-width: 576px) and (max-width: 767px) {
  h1 {
    font-size: 2rem;
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
    padding: 30px 15px;
  }

  h1 {
    font-size: 1.8rem;
    margin-bottom: 30px;
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

  .summary-card h2 {
    font-size: 1.25rem;
  }

  .action-button {
    padding: 14px;
  }
}
</style>