<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue';
import { useAuthStore } from '@/stores/auth.store';
import { useAccountStore } from '@/stores/account.store';
import { Account } from '@/models';
import { apiClient, API_ENDPOINTS, getAuthHeader } from '@/services/api.config';

const props = defineProps<{
  show: boolean;
  selectedAccount?: Account | null;
}>();

const emit = defineEmits(['close', 'transfer-complete']);

// Stores
const authStore = useAuthStore();
const accountStore = useAccountStore();

// Form state
const fromAccount = ref<Account | null>(null);
const toAccount = ref<string>('');
const amount = ref<number | null>(null);
const description = ref<string>('');
const isProcessing = ref(false);
const error = ref<string | null>(null);
const success = ref<string | null>(null);

// User search
const searchTerm = ref<string>('');
const searchResults = ref<any[]>([]);
const isSearching = ref(false);
const searchError = ref<string | null>(null);

// Validation
const amountError = ref<string | null>(null);
const toAccountError = ref<string | null>(null);

// Computed values
const availableFromAccounts = computed(() => accountStore.allAccounts);
const isFormValid = computed(() => {
  return !!fromAccount.value &&
      !!toAccount.value &&
      !!amount.value &&
      amount.value > 0 &&
      !amountError.value &&
      !toAccountError.value;
});

const insufficientFunds = computed(() => {
  if (!fromAccount.value || !amount.value) return false;
  return amount.value > fromAccount.value.balance;
});

const formattedCurrency = computed(() => {
  if (!fromAccount.value) return '';
  return fromAccount.value.currency;
});

// Format currency
const formatCurrency = (amount: number, currency: string): string => {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: currency,
  }).format(amount);
};

// Initialize form when modal opens
watch(() => props.show, (newVal) => {
  if (newVal) {
    resetForm();
    // Set default from account if provided
    if (props.selectedAccount) {
      fromAccount.value = props.selectedAccount;
    } else if (availableFromAccounts.value.length > 0) {
      fromAccount.value = availableFromAccounts.value[0];
    }
  }
});

// Reset form
const resetForm = () => {
  fromAccount.value = null;
  toAccount.value = '';
  amount.value = null;
  description.value = '';
  error.value = null;
  success.value = null;
  searchTerm.value = '';
  searchResults.value = [];
  amountError.value = null;
  toAccountError.value = null;
};

// Close modal
const closeModal = () => {
  resetForm();
  emit('close');
};

// Validate amount against limits
const validateAmount = () => {
  amountError.value = null;

  if (!amount.value || !fromAccount.value) return;

  if (amount.value <= 0) {
    amountError.value = 'Amount must be greater than zero';
    return;
  }

  if (amount.value > fromAccount.value.balance) {
    amountError.value = 'Insufficient funds';
    return;
  }

  if (amount.value > fromAccount.value.singleTransferLimit) {
    amountError.value = `Exceeds single transfer limit of ${formatCurrency(fromAccount.value.singleTransferLimit, fromAccount.value.currency)}`;
    return;
  }

  const newTransferTotal = fromAccount.value.transferUsedToday + amount.value;
  if (newTransferTotal > fromAccount.value.dailyTransferLimit) {
    amountError.value = `Exceeds daily transfer limit of ${formatCurrency(fromAccount.value.dailyTransferLimit, fromAccount.value.currency)}`;
    return;
  }
};

// Watch for amount changes to validate
watch(amount, validateAmount);
watch(fromAccount, validateAmount);

// Search for accounts by username
const searchAccounts = async () => {
  if (!searchTerm.value || searchTerm.value.length < 2) {
    searchResults.value = [];
    return;
  }

  try {
    isSearching.value = true;
    searchError.value = null;

    const response = await apiClient.get(
        `${API_ENDPOINTS.account.search}?term=${encodeURIComponent(searchTerm.value)}`,
        { headers: getAuthHeader() }
    );

    searchResults.value = response.data.accounts || [];
  } catch (err: any) {
    console.error('Error searching accounts:', err);
    searchError.value = err.message || 'Failed to search accounts';
    searchResults.value = [];
  } finally {
    isSearching.value = false;
  }
};

// Validate account number
const validateToAccount = async () => {
  toAccountError.value = null;

  if (!toAccount.value) return;

  // Check if it's the same as source account
  if (fromAccount.value && toAccount.value === fromAccount.value.accountNumber) {
    toAccountError.value = 'Cannot transfer to the same account';
    return;
  }

  // Check if account exists (this could be optional)
  try {
    const response = await apiClient.get(
        `${API_ENDPOINTS.account.details}/${toAccount.value}`,
        { headers: getAuthHeader() }
    );

    // Account exists but is your own account (not an error, just info)
    if (availableFromAccounts.value.some(acc => acc.accountNumber === toAccount.value)) {
      toAccountError.value = 'This is your own account';
    }
  } catch (err: any) {
    if (err.response && err.response.status === 404) {
      // Don't set error here because the account might belong to another user
      // which is valid but the API can't directly access it
    }
  }
};

// Watch for account number changes to validate
watch(toAccount, validateToAccount);

// Handle account selection
const selectAccount = (account: any) => {
  toAccount.value = account.accountNumber;
  searchResults.value = [];
  searchTerm.value = `${account.accountName} (${account.accountNumber})`;
};

// Submit transfer
const submitTransfer = async () => {
  if (!isFormValid.value) return;

  try {
    isProcessing.value = true;
    error.value = null;

    const transferData = {
      fromAccount: fromAccount.value?.accountNumber,
      toAccount: toAccount.value,
      amount: amount.value,
      description: description.value || 'Transfer'
    };

    const response = await apiClient.post(
        API_ENDPOINTS.transaction.transfer,
        transferData,
        { headers: getAuthHeader() }
    );

    success.value = 'Transfer completed successfully!';

    // Refresh accounts after successful transfer
    await accountStore.fetchAllAccounts();

    // Reset form after short delay
    setTimeout(() => {
      resetForm();
      emit('transfer-complete', response.data);
      emit('close');
    }, 2000);

  } catch (err: any) {
    console.error('Error processing transfer:', err);
    if (err.response?.status === 422) {
      error.value = 'Transfer failed: Insufficient funds or limit exceeded';
    } else {
      error.value = err.response?.data?.message || err.message || 'Failed to process transfer';
    }
  } finally {
    isProcessing.value = false;
  }
};

// Debounce search
let debounceTimeout: NodeJS.Timeout | null = null;
const debouncedSearch = () => {
  if (debounceTimeout) clearTimeout(debounceTimeout);
  debounceTimeout = setTimeout(() => {
    searchAccounts();
  }, 300);
};

// Watch for search term changes
watch(searchTerm, () => {
  debouncedSearch();
});

// Fetch accounts when component mounts
onMounted(async () => {
  if (accountStore.allAccounts.length === 0) {
    await accountStore.fetchAllAccounts();
  }
});
</script>

<template>
  <div v-if="show" class="modal-overlay" @click="closeModal">
    <div class="modal-container" @click.stop>
      <div class="modal-header">
        <h2>Transfer Money</h2>
        <button class="close-button" @click="closeModal">&times;</button>
      </div>

      <div class="modal-body">
        <div v-if="error" class="error-message">
          {{ error }}
        </div>

        <div v-if="success" class="success-message">
          <div class="success-icon">✓</div>
          <div>{{ success }}</div>
        </div>

        <form @submit.prevent="submitTransfer" v-if="!success">
          <!-- From Account -->
          <div class="form-group">
            <label for="fromAccount">From Account</label>
            <select
                id="fromAccount"
                v-model="fromAccount"
                required
                :disabled="isProcessing"
                class="form-control"
            >
              <option v-for="account in availableFromAccounts"
                      :key="account.id"
                      :value="account">
                {{ account.accountName }} ({{ account.accountNumber }}) -
                {{ formatCurrency(account.balance, account.currency) }}
              </option>
            </select>
            <div class="form-info" v-if="fromAccount">
              Transfer limit: {{ formatCurrency(fromAccount.singleTransferLimit, fromAccount.currency) }} per transaction
            </div>
          </div>

          <!-- To Account (Search) -->
          <div class="form-group">
            <label for="toAccountSearch">To Account (Search by username)</label>
            <div class="search-container">
              <input
                  id="toAccountSearch"
                  v-model="searchTerm"
                  placeholder="Search by username"
                  :disabled="isProcessing"
                  autocomplete="off"
                  class="form-control"
              />

              <div v-if="isSearching" class="search-spinner"></div>

              <!-- Search Results -->
              <div v-if="searchResults.length > 0" class="search-results">
                <div
                    v-for="account in searchResults"
                    :key="account.id"
                    class="search-result-item"
                    @click="selectAccount(account)"
                >
                  <span class="account-name">{{ account.accountName }}</span>
                  <span class="account-number">{{ account.accountNumber }}</span>
                </div>
              </div>

              <div v-if="searchError" class="search-error">
                {{ searchError }}
              </div>
            </div>
          </div>

          <!-- To Account (Manual Input) -->
          <div class="form-group">
            <label for="toAccount">To Account Number</label>
            <input
                id="toAccount"
                v-model="toAccount"
                placeholder="Enter account number"
                required
                :disabled="isProcessing"
                class="form-control"
            />
            <div v-if="toAccountError" class="field-info">
              {{ toAccountError }}
            </div>
          </div>

          <!-- Amount -->
          <div class="form-group">
            <label for="amount">Amount ({{ formattedCurrency }})</label>
            <input
                id="amount"
                type="number"
                v-model="amount"
                min="0.01"
                step="0.01"
                required
                :disabled="isProcessing"
                class="form-control"
            />
            <div v-if="amountError" class="field-error">
              {{ amountError }}
            </div>
            <div v-else-if="fromAccount" class="form-info">
              Available: {{ formatCurrency(fromAccount.balance, fromAccount.currency) }}
            </div>
          </div>

          <!-- Description -->
          <div class="form-group">
            <label for="description">Description (Optional)</label>
            <input
                id="description"
                v-model="description"
                placeholder="Add a note for this transfer"
                :disabled="isProcessing"
                class="form-control"
            />
          </div>

          <!-- Submit Button -->
          <div class="form-actions">
            <button
                type="button"
                class="button secondary"
                @click="closeModal"
                :disabled="isProcessing"
            >
              Cancel
            </button>
            <button
                type="submit"
                class="button primary"
                :disabled="!isFormValid || isProcessing"
            >
              <span v-if="isProcessing" class="spinner"></span>
              <span v-else>Transfer</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  animation: fadeIn 0.3s ease;
}

.modal-container {
  width: 90%;
  max-width: 500px;
  background-color: white;
  border-radius: 12px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  animation: slideIn 0.3s ease;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 25px;
  border-bottom: 1px solid #eee;
  background-color: #f9f9f9;
}

.modal-header h2 {
  margin: 0;
  color: #333;
  font-size: 1.5rem;
}

.close-button {
  background: none;
  border: none;
  font-size: 1.5rem;
  color: #666;
  cursor: pointer;
}

.close-button:hover {
  color: #333;
}

.modal-body {
  padding: 25px;
}

.form-group {
  margin-bottom: 20px;
}

label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #555;
}

.form-control {
  width: 100%;
  padding: 12px 15px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 1rem;
  transition: border-color 0.3s;
}

.form-control:focus {
  border-color: #4CAF50;
  outline: none;
}

.form-control:disabled {
  background-color: #f5f5f5;
  cursor: not-allowed;
}

.search-container {
  position: relative;
}

.search-results {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: white;
  border: 1px solid #ddd;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  max-height: 200px;
  overflow-y: auto;
  z-index: 10;
}

.search-result-item {
  padding: 12px 15px;
  cursor: pointer;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-result-item:not(:last-child) {
  border-bottom: 1px solid #eee;
}

.search-result-item:hover {
  background-color: #f5f5f5;
}

.account-name {
  font-weight: 500;
}

.account-number {
  color: #666;
  font-size: 0.9rem;
}

.search-spinner {
  position: absolute;
  right: 15px;
  top: 50%;
  transform: translateY(-50%);
  width: 16px;
  height: 16px;
  border: 2px solid rgba(0, 0, 0, 0.1);
  border-radius: 50%;
  border-top-color: #4CAF50;
  animation: spin 1s linear infinite;
}

.search-error {
  color: #d32f2f;
  font-size: 0.9rem;
  margin-top: 8px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 15px;
  margin-top: 30px;
}

.button {
  padding: 12px 20px;
  border-radius: 8px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 100px;
}

.button.primary {
  background-color: #4CAF50;
  color: white;
}

.button.primary:hover:not(:disabled) {
  background-color: #43a047;
  transform: translateY(-2px);
  box-shadow: 0 3px 8px rgba(0, 0, 0, 0.1);
}

.button.secondary {
  background-color: #f5f5f5;
  color: #333;
}

.button.secondary:hover:not(:disabled) {
  background-color: #e8e8e8;
}

.button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.spinner {
  display: inline-block;
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  border-top-color: white;
  animation: spin 1s linear infinite;
}

.error-message {
  color: #d32f2f;
  background-color: #ffebee;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 20px;
  font-size: 0.95rem;
}

.field-error {
  color: #d32f2f;
  font-size: 0.85rem;
  margin-top: 5px;
}

.field-info {
  color: #0277bd;
  font-size: 0.85rem;
  margin-top: 5px;
}

.form-info {
  color: #757575;
  font-size: 0.85rem;
  margin-top: 5px;
}

.success-message {
  color: #2e7d32;
  background-color: #e8f5e9;
  padding: 25px 15px;
  border-radius: 8px;
  margin-bottom: 20px;
  text-align: center;
  font-weight: 500;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 15px;
}

.success-icon {
  width: 50px;
  height: 50px;
  background-color: #4CAF50;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30px;
}

@keyframes spin {
  from { transform: translateY(-50%) rotate(0deg); }
  to { transform: translateY(-50%) rotate(360deg); }
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes slideIn {
  from { transform: translateY(-50px); opacity: 0; }
  to { transform: translateY(0); opacity: 1; }
}

@media (max-width: 576px) {
  .modal-container {
    width: 95%;
    max-height: 90vh;
    overflow-y: auto;
  }

  .modal-header {
    padding: 15px 20px;
  }

  .modal-body {
    padding: 20px;
  }

  .form-actions {
    flex-direction: column;
  }

  .button {
    width: 100%;
  }
}
</style>