<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue';
import { useAccountStore } from '@/stores/account.store';
import { useAtmStore } from '@/stores/atm.store';
import { useAuthStore } from '@/stores/auth.store';
import { useTransactionStore } from '@/stores/transaction.store';
import { currencyService } from '@/services/CurrencyService';
import { Account } from '@/models';

// Get stores
const accountStore = useAccountStore();
const atmStore = useAtmStore();
const authStore = useAuthStore();
const transactionStore = useTransactionStore();

// State management
const selectedAccount = ref<Account | null>(null);
const transactionType = ref<'deposit' | 'withdraw'>('deposit');
const amount = ref<number>(0);
const description = ref<string>('');
const isSubmitting = ref<boolean>(false);
const isSuccess = ref<boolean>(false);
const successMessage = ref<string>('');
const accountLimits = ref<any>(null);
const isLoadingLimits = ref<boolean>(false);
const accounts = computed(() => accountStore.allAccounts);
const loading = computed(() => accountStore.isLoading || atmStore.isProcessing);
const error = computed(() => atmStore.error);

// Calculate minimum and maximum values for amount using account's native currency limits
const minAmount = computed(() => 1);
const maxAmount = computed(() => {
  if (!selectedAccount.value || !accountLimits.value) return 0;
  
  if (transactionType.value === 'deposit') {
    // For deposits, use transfer limits from converted account limits
    const remainingDaily = accountLimits.value.dailyTransferLimit - selectedAccount.value.transferUsedToday;
    return Math.min(accountLimits.value.singleTransferLimit, Math.max(0, remainingDaily));
  } else {
    // For withdrawals, consider balance and withdrawal limits from converted account limits
    const remainingDaily = accountLimits.value.dailyWithdrawalLimit - selectedAccount.value.withdrawalUsedToday;
    return Math.min(
      selectedAccount.value.balance,
      accountLimits.value.singleWithdrawalLimit,
      Math.max(0, remainingDaily)
    );
  }
});

// Load account limits in account currency (from EUR defaults)
const loadAccountLimits = async () => {
  if (!selectedAccount.value) {
    accountLimits.value = null;
    return;
  }

  try {
    isLoadingLimits.value = true;
    accountLimits.value = await currencyService.convertLimitsToAccountCurrency(selectedAccount.value.currency);
  } catch (error) {
    console.error('Error loading account limits:', error);
    // Fallback to default EUR limits if conversion fails
    accountLimits.value = {
      singleTransferLimit: 3000,
      dailyTransferLimit: 5000,
      singleWithdrawalLimit: 500,
      dailyWithdrawalLimit: 5000
    };
  } finally {
    isLoadingLimits.value = false;
  }
};

// Format currency
const formatCurrency = (amount: number, currency = 'EUR'): string => {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: currency,
  }).format(amount);
};

// Check if form is valid
const isFormValid = computed(() => {
  if (!selectedAccount.value || !accountLimits.value || amount.value <= 0) return false;
  
  if (transactionType.value === 'deposit') {
    // Check deposit limits (using converted transfer limits)
    const remainingDaily = accountLimits.value.dailyTransferLimit - selectedAccount.value.transferUsedToday;
    return amount.value <= accountLimits.value.singleTransferLimit && 
           amount.value <= remainingDaily &&
           amount.value <= maxAmount.value;
  } else {
    // Check withdrawal limits and balance (using converted withdrawal limits)
    const remainingDaily = accountLimits.value.dailyWithdrawalLimit - selectedAccount.value.withdrawalUsedToday;
    return amount.value <= selectedAccount.value.balance &&
           amount.value <= accountLimits.value.singleWithdrawalLimit &&
           amount.value <= remainingDaily &&
           amount.value <= maxAmount.value;
  }
});

// Handle account selection
const handleAccountSelect = (account: Account) => {
  selectedAccount.value = account;
};

// Watch for account changes and reset success/error states
watch(selectedAccount, async () => {
  isSuccess.value = false;
  successMessage.value = '';
  atmStore.clearError();
  amount.value = 0;
  description.value = '';
  await loadAccountLimits(); // Load limits for the new account
});

// Handle transaction type change
const handleTypeChange = (type: 'deposit' | 'withdraw') => {
  transactionType.value = type;
  
  // Reset amount if it exceeds the max for the new transaction type
  if (selectedAccount.value && amount.value > maxAmount.value) {
    amount.value = Math.min(amount.value, maxAmount.value);
  }
};

// Get limit information for display
const getLimitInfo = computed(() => {
  if (!selectedAccount.value || !accountLimits.value) return null;
  
  if (transactionType.value === 'deposit') {
    const remainingDaily = accountLimits.value.dailyTransferLimit - selectedAccount.value.transferUsedToday;
    return {
      singleLimit: accountLimits.value.singleTransferLimit,
      dailyLimit: accountLimits.value.dailyTransferLimit,
      dailyUsed: selectedAccount.value.transferUsedToday,
      dailyRemaining: Math.max(0, remainingDaily),
      limitType: 'transfer'
    };
  } else {
    const remainingDaily = accountLimits.value.dailyWithdrawalLimit - selectedAccount.value.withdrawalUsedToday;
    return {
      singleLimit: accountLimits.value.singleWithdrawalLimit,
      dailyLimit: accountLimits.value.dailyWithdrawalLimit,
      dailyUsed: selectedAccount.value.withdrawalUsedToday,
      dailyRemaining: Math.max(0, remainingDaily),
      limitType: 'withdrawal'
    };
  }
});

// Handle transaction submission
const handleSubmit = async () => {
  if (!isFormValid.value || !selectedAccount.value) return;
  
  isSubmitting.value = true;
  isSuccess.value = false;
  successMessage.value = '';
  atmStore.clearError();
  
  try {
    let success = false;
    
    if (transactionType.value === 'deposit') {
      success = await atmStore.deposit(
        selectedAccount.value.accountNumber,
        amount.value,
        description.value
      );
    } else {
      success = await atmStore.withdraw(
        selectedAccount.value.accountNumber,
        amount.value,
        description.value
      );
    }      if (success) {
      // Update account and transaction data
      await accountStore.fetchAllAccounts();
      
      // Update the selected account reference with fresh data to reflect limit changes
      if (selectedAccount.value) {
        const updatedAccount = accountStore.allAccounts.find(
          acc => acc.accountNumber === selectedAccount.value!.accountNumber
        );
        if (updatedAccount) {
          selectedAccount.value = updatedAccount;
        }
        await transactionStore.fetchTransactionsByAccount(selectedAccount.value.accountNumber);
      }
      
      // Show success message
      isSuccess.value = true;
      successMessage.value = `${transactionType.value === 'deposit' ? 'Deposit' : 'Withdrawal'} of ${formatCurrency(amount.value)} processed successfully!`;
      
      // Reset form
      amount.value = 0;
      description.value = '';
    }
  } catch (err) {
    console.error('ATM transaction error:', err);
  } finally {
    isSubmitting.value = false;
  }
};

// Initialize data on component mount
onMounted(async () => {
  try {
    // Validate token and fetch accounts
    await authStore.validateToken();
    await accountStore.fetchAllAccounts();
      // Pre-select first account if any are available
    if (accounts.value.length > 0) {
      selectedAccount.value = accounts.value[0];
      await loadAccountLimits(); // Load account limits for the selected account
    }
  } catch (err) {
    console.error('Error loading ATM data:', err);
  }
});
</script>

<template>
  <div class="atm-container">
    <header class="atm-header">
      <h1>ATM</h1>
      <p class="subtitle">Deposit or withdraw money from your accounts</p>
    </header>

    <div v-if="loading" class="loading">
      <div class="spinner"></div>
      <p>Processing your request...</p>
    </div>

    <div v-else-if="accounts.length === 0" class="no-accounts">
      <h2>No accounts available</h2>
      <p>You don't have any accounts set up yet. Please contact a bank representative.</p>
    </div>

    <div v-else class="atm-content">
      <!-- Account selection -->
      <div class="account-selection">
        <h2>Select Account</h2>
        
        <div class="accounts-list">
          <div
            v-for="account in accounts"
            :key="account.id"
            class="account-item"
            :class="{ 'selected': selectedAccount?.id === account.id }"
            @click="handleAccountSelect(account)"
          >
            <div class="account-info">
              <span class="account-name">{{ account.accountName }}</span>
              <span class="account-number">{{ account.accountNumber }}</span>
              <span class="account-type">{{ account.accountType }}</span>
            </div>
            <span class="account-balance">{{ formatCurrency(account.balance, account.currency) }}</span>
          </div>
        </div>
      </div>

      <!-- Transaction form -->
      <div class="transaction-form">
        <h2>ATM Transaction</h2>
        
        <!-- Transaction type -->
        <div class="form-group">
          <label>Transaction Type</label>
          <div class="transaction-type-buttons">
            <button 
              class="type-button"
              :class="{ 'active': transactionType === 'deposit' }"
              @click="handleTypeChange('deposit')"
            >
              Deposit
            </button>
            <button 
              class="type-button"
              :class="{ 'active': transactionType === 'withdraw' }"
              @click="handleTypeChange('withdraw')"
            >
              Withdraw
            </button>
          </div>
        </div>
        
        <!-- Amount -->
        <div class="form-group">
          <label for="amount">Amount ({{ selectedAccount?.currency || 'EUR' }})</label>
          <input 
            type="number" 
            id="amount" 
            v-model="amount" 
            :min="minAmount" 
            :max="maxAmount" 
            step="0.01"
            required
          />
        </div>        <!-- Limit Information -->
        <div v-if="selectedAccount && getLimitInfo" class="limit-info">
          <h4>{{ transactionType === 'deposit' ? 'Transfer' : 'Withdrawal' }} Limits</h4>
          <div class="limit-details">
            <div class="limit-item">
              <span class="limit-label">Single limit:</span>
              <span class="limit-value">{{ formatCurrency(getLimitInfo.singleLimit, selectedAccount.currency) }}</span>
            </div>
            <div class="limit-item">
              <span class="limit-label">Daily remaining:</span>
              <span class="limit-value" :class="{ 'limit-warning': getLimitInfo.dailyRemaining < 100 }">
                {{ formatCurrency(getLimitInfo.dailyRemaining, selectedAccount.currency) }}
              </span>
            </div>
            <div v-if="transactionType === 'withdraw'" class="limit-item">
              <span class="limit-label">Available balance:</span>
              <span class="limit-value">{{ formatCurrency(selectedAccount.balance, selectedAccount.currency) }}</span>
            </div>
          </div>
          <div v-if="maxAmount <= 0" class="limit-warning-message">
            <p>⚠️ {{ transactionType === 'deposit' ? 'Daily transfer limit reached' : 'Daily withdrawal limit reached or insufficient balance' }}</p>
          </div>
        </div>

        <!-- Description -->
        <div class="form-group">
          <label for="description">Description (Optional)</label>
          <input 
            type="text" 
            id="description" 
            v-model="description" 
            placeholder="Enter transaction description"
          />
        </div>
        
        <!-- Submit button -->
        <button 
          class="submit-button" 
          :disabled="!isFormValid || isSubmitting" 
          @click="handleSubmit"
        >
          <span v-if="isSubmitting" class="spinner small"></span>
          <span v-else>Process {{ transactionType === 'deposit' ? 'Deposit' : 'Withdrawal' }}</span>
        </button>
        
        <!-- Error message -->
        <div v-if="error" class="error-message">
          {{ error }}
        </div>
        
        <!-- Success message -->
        <div v-if="isSuccess" class="success-message">
          {{ successMessage }}
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.atm-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 2rem;
}

.atm-header {
  text-align: center;
  margin-bottom: 2rem;
}

h1 {
  color: #2c3e50;
  font-size: 2.5rem;
  margin-bottom: 0.5rem;
}

.subtitle {
  color: #7f8c8d;
  font-size: 1.2rem;
}

.loading, .no-accounts {
  text-align: center;
  padding: 3rem;
  background-color: #f9f9f9;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.spinner {
  display: inline-block;
  width: 40px;
  height: 40px;
  border: 4px solid rgba(0, 0, 0, 0.1);
  border-radius: 50%;
  border-top-color: #4CAF50;
  animation: spin 1s linear infinite;
  margin-bottom: 1rem;
}

.spinner.small {
  width: 20px;
  height: 20px;
  border-width: 2px;
  margin-right: 8px;
  margin-bottom: 0;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.atm-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 2rem;
}

.account-selection, .transaction-form {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 15px rgba(0, 0, 0, 0.1);
  padding: 1.5rem;
}

h2 {
  color: #2c3e50;
  font-size: 1.5rem;
  margin-bottom: 1.5rem;
  border-bottom: 1px solid #eee;
  padding-bottom: 0.75rem;
}

.accounts-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  max-height: 400px;
  overflow-y: auto;
}

.account-item {
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  padding: 1rem;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.account-item:hover {
  border-color: #4CAF50;
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(76, 175, 80, 0.1);
}

.account-item.selected {
  border-color: #4CAF50;
  background-color: rgba(76, 175, 80, 0.05);
}

.account-info {
  display: flex;
  flex-direction: column;
}

.account-name {
  font-weight: 600;
  font-size: 1.1rem;
  color: #2c3e50;
}

.account-number {
  font-size: 0.9rem;
  color: #7f8c8d;
  margin-top: 4px;
}

.account-type {
  font-size: 0.8rem;
  color: #95a5a6;
  margin-top: 2px;
}

.account-balance {
  font-weight: 700;
  font-size: 1.2rem;
  color: #4CAF50;
}

.form-group {
  margin-bottom: 1.5rem;
}

label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
  color: #2c3e50;
}

input[type="number"],
input[type="text"] {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
  transition: border-color 0.2s;
}

input:focus {
  border-color: #4CAF50;
  outline: none;
}

.transaction-type-buttons {
  display: flex;
  gap: 1rem;
}

.type-button {
  flex: 1;
  padding: 0.75rem;
  border: 2px solid #ddd;
  border-radius: 4px;
  background-color: white;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.type-button:hover {
  border-color: #4CAF50;
}

.type-button.active {
  border-color: #4CAF50;
  background-color: #4CAF50;
  color: white;
}

.amount-slider {
  margin-top: 1rem;
}

input[type="range"] {
  width: 100%;
  margin-bottom: 0.5rem;
}

.slider-labels {
  display: flex;
  justify-content: space-between;
  font-size: 0.8rem;
  color: #7f8c8d;
}

.submit-button {
  width: 100%;
  padding: 1rem;
  border: none;
  border-radius: 4px;
  background-color: #4CAF50;
  color: white;
  font-size: 1.1rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 1rem;
}

.submit-button:hover:not(:disabled) {
  background-color: #3d8b40;
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(76, 175, 80, 0.2);
}

.submit-button:disabled {
  background-color: #a5d6a7;
  cursor: not-allowed;
}

.limit-info {
  margin-bottom: 1.5rem;
  padding: 0.75rem;
  background-color: #fafbfc;
  border-radius: 4px;
  border-left: 3px solid #e9ecef;
  border: 1px solid #e9ecef;
}

.limit-info h4 {
  margin: 0 0 0.75rem 0;
  font-size: 0.9rem;
  color: #6c757d;
  font-weight: 500;
  border-bottom: none;
  padding-bottom: 0;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.limit-details {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}

.limit-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.15rem 0;
}

.limit-label {
  font-size: 0.85rem;
  color: #6c757d;
  font-weight: 400;
}

.limit-value {
  font-weight: 500;
  color: #495057;
  font-size: 0.85rem;
}

.limit-value.limit-warning {
  color: #fd7e14;
  font-weight: 600;
}

.limit-warning-message {
  margin-top: 0.75rem;
  padding: 0.5rem;
  background-color: #fff8e1;
  border: 1px solid #ffe0b2;
  border-radius: 3px;
  color: #e65100;
}

.limit-warning-message p {
  margin: 0;
  font-size: 0.8rem;
  font-weight: 500;
}

.error-message {
  margin-top: 1rem;
  padding: 0.75rem;
  background-color: #ffebee;
  color: #d32f2f;
  border-radius: 4px;
  font-size: 0.9rem;
}

.success-message {
  margin-top: 1rem;
  padding: 0.75rem;
  background-color: #e8f5e9;
  color: #2e7d32;
  border-radius: 4px;
  font-size: 0.9rem;
}

@media (max-width: 768px) {
  .atm-content {
    grid-template-columns: 1fr;
  }
  
  .account-selection {
    margin-bottom: 2rem;
  }
}
</style>