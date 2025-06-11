<template>
  <div v-if="isOpen" class="modal-overlay" @click="handleBackdropClick">
    <div class="modal-content" @click.stop>
      <div class="modal-header">
        <h2>Create New Account</h2>
        <button class="close-button" @click="closeModal" aria-label="Close modal">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="18" y1="6" x2="6" y2="18"></line>
            <line x1="6" y1="6" x2="18" y2="18"></line>
          </svg>
        </button>
      </div>

      <form @submit.prevent="handleSubmit" class="account-form">
        <div class="form-group">
          <label for="accountName" class="form-label">Account Name</label>
          <input
            id="accountName"
            v-model="form.accountName"
            type="text"
            class="form-input"
            :class="{ 'error': errors.accountName }"
            placeholder="e.g., My Savings Account"
            maxlength="50"
            required
          />
          <span v-if="errors.accountName" class="error-message">{{ errors.accountName }}</span>
        </div>

        <div class="form-group">
          <label for="accountType" class="form-label">Account Type</label>
          <select
            id="accountType"
            v-model="form.accountType"
            class="form-select"
            :class="{ 'error': errors.accountType }"
            required
          >
            <option value="">Select account type</option>
            <option value="SAVINGS">Savings Account</option>
            <option value="CHECKING">Checking Account</option>
            <option value="BUSINESS">Business Account</option>
            <option value="INVESTMENT">Investment Account</option>
          </select>
          <span v-if="errors.accountType" class="error-message">{{ errors.accountType }}</span>
        </div>

        <div class="form-group">
          <label for="currency" class="form-label">Currency</label>
          <select
            id="currency"
            v-model="form.currency"
            class="form-select"
            :class="{ 'error': errors.currency }"
            required
          >
            <option value="">Select currency</option>
            <option value="EUR">EUR - Euro</option>
            <option value="USD">USD - US Dollar</option>
            <option value="GBP">GBP - British Pound</option>
            <option value="CHF">CHF - Swiss Franc</option>
            <option value="PLN">PLN - Polish Zloty</option>
          </select>
          <span v-if="errors.currency" class="error-message">{{ errors.currency }}</span>
        </div>

        <div class="form-actions">
          <button
            type="button"
            @click="closeModal"
            class="cancel-button"
            :disabled="isSubmitting"
          >
            Cancel
          </button>
          <button
            type="submit"
            class="create-button"
            :disabled="isSubmitting || !isFormValid"
          >
            <span v-if="isSubmitting" class="spinner small"></span>
            {{ isSubmitting ? 'Creating...' : 'Create Account' }}
          </button>
        </div>
      </form>

      <div v-if="submitError" class="error-panel">
        <p>{{ submitError }}</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, reactive, watch } from 'vue';
import { useAccountStore } from '@/stores/account.store';
import { User } from '@/models';

// Props
interface Props {
  isOpen: boolean;
  user?: User
}

const props = defineProps<Props>();

// Emits
const emit = defineEmits<{
  close: [];
  accountCreated: [account: any];
}>();

// Stores
const accountStore = useAccountStore();

// Form state
const form = reactive({
  accountName: '',
  accountType: '',
  currency: '',
  initialBalance: null as number | null,
  dailyTransferLimit: null as number | null
});

// Error handling
const errors = reactive({
  accountName: '',
  accountType: '',
  currency: '',
  initialBalance: '',
  dailyTransferLimit: ''
});

const isSubmitting = ref(false);
const submitError = ref('');

// Computed
const isFormValid = computed(() => {
  return form.accountName.trim() && 
         form.accountType &&
         form.currency &&
         !Object.values(errors).some(error => error);
});

// Validation
const validateForm = () => {
  // Reset errors
  Object.keys(errors).forEach(key => {
    errors[key as keyof typeof errors] = '';
  });

  let isValid = true;

  // Account name validation
  if (!form.accountName.trim()) {
    errors.accountName = 'Account name is required';
    isValid = false;
  } else if (form.accountName.length < 2) {
    errors.accountName = 'Account name must be at least 2 characters';
    isValid = false;
  } else if (form.accountName.length > 50) {
    errors.accountName = 'Account name must be less than 50 characters';
    isValid = false;
  }

  // Account type validation
  if (!form.accountType) {
    errors.accountType = 'Account type is required';
    isValid = false;
  }

  // Currency validation
  if (!form.currency) {
    errors.currency = 'Currency is required';
    isValid = false;
  }

  // Initial balance validation
  if (form.initialBalance !== null && form.initialBalance < 0) {
    errors.initialBalance = 'Initial balance cannot be negative';
    isValid = false;
  }

  // Daily transfer limit validation
  if (form.dailyTransferLimit !== null && form.dailyTransferLimit < 1) {
    errors.dailyTransferLimit = 'Daily transfer limit must be at least 1.00';
    isValid = false;
  }

  return isValid;
};

// Watch for real-time validation
watch(form, () => {
  if (submitError.value) {
    submitError.value = '';
  }
  validateForm();
}, { deep: true });

// Methods
const resetForm = () => {
  form.accountName = '';
  form.accountType = '';
  form.currency = '';
  form.initialBalance = null;
  form.dailyTransferLimit = null;
  
  Object.keys(errors).forEach(key => {
    errors[key as keyof typeof errors] = '';
  });
  
  submitError.value = '';
};

const closeModal = () => {
  resetForm();
  emit('close');
};

const handleBackdropClick = (event: MouseEvent) => {
  if (event.target === event.currentTarget) {
    closeModal();
  }
};

const handleSubmit = async () => {
  if (!validateForm()) {
    return;
  }

  try {
    isSubmitting.value = true;
    submitError.value = '';

    const accountData = {
      accountName: form.accountName.trim(),
      accountType: form.accountType,
      currency: form.currency,
      initialBalance: form.initialBalance || 0,
      dailyTransferLimit: form.dailyTransferLimit || 5000,
      ...(props.user ? { userId: props.user.id } : {})
    };

    const newAccount = await accountStore.createAccount(accountData);
    
    // Emit success
    emit('accountCreated', newAccount);
    
    // Reset form
    resetForm();
  } catch (error: any) {
    console.error('Error creating account:', error);
    submitError.value = error.message || 'Failed to create account. Please try again.';
  } finally {
    isSubmitting.value = false;
  }
};

// Watch for modal open/close
watch(() => props.isOpen, (isOpen) => {
  if (isOpen) {
    // Reset form when modal opens
    resetForm();
  }
});
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
  backdrop-filter: blur(2px);
}

.modal-content {
  background: white;
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  width: 100%;
  max-width: 500px;
  max-height: 90vh;
  overflow-y: auto;
  position: relative;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 24px 0;
  margin-bottom: 20px;
}

.modal-header h2 {
  margin: 0;
  color: #1a365d;
  font-size: 1.5rem;
  font-weight: 600;
}

.close-button {
  background: none;
  border: none;
  cursor: pointer;
  padding: 8px;
  border-radius: 8px;
  color: #64748b;
  transition: all 0.2s ease;
}

.close-button:hover {
  background: #f1f5f9;
  color: #475569;
}

.account-form {
  padding: 0 24px 24px;
}

.form-group {
  margin-bottom: 20px;
}

.form-label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #374151;
  font-size: 0.9rem;
}

.form-input,
.form-select {
  width: 100%;
  padding: 12px 16px;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  font-size: 1rem;
  transition: all 0.2s ease;
  background: white;
  box-sizing: border-box;
}

.form-input:focus,
.form-select:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.form-input.error,
.form-select.error {
  border-color: #ef4444;
}

.amount-input-container {
  position: relative;
  display: flex;
  align-items: center;
}

.amount-input {
  padding-right: 60px;
}

.currency-label {
  position: absolute;
  right: 16px;
  color: #64748b;
  font-weight: 500;
  pointer-events: none;
}

.error-message {
  display: block;
  color: #ef4444;
  font-size: 0.875rem;
  margin-top: 4px;
}

.input-hint {
  display: block;
  color: #64748b;
  font-size: 0.8rem;
  margin-top: 4px;
}

.form-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  margin-top: 32px;
  padding-top: 20px;
  border-top: 1px solid #e2e8f0;
}

.cancel-button,
.create-button {
  padding: 12px 24px;
  border-radius: 8px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  border: none;
  font-size: 0.95rem;
  min-width: 100px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.cancel-button {
  background: #f8fafc;
  color: #475569;
  border: 2px solid #e2e8f0;
}

.cancel-button:hover:not(:disabled) {
  background: #f1f5f9;
  border-color: #cbd5e1;
}

.create-button {
  background: #4CAF50;
  color: white;
}

.create-button:hover:not(:disabled) {
  background: #4CAF50;
  transform: translateY(-1px);
}

.create-button:disabled,
.cancel-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.error-panel {
  margin: 0 24px 24px;
  padding: 12px 16px;
  background: #fef2f2;
  border: 1px solid #fecaca;
  border-radius: 8px;
  color: #dc2626;
}

.error-panel p {
  margin: 0;
  font-size: 0.9rem;
}

.spinner {
  width: 16px;
  height: 16px;
  border: 2px solid transparent;
  border-top: 2px solid currentColor;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.spinner.small {
  width: 14px;
  height: 14px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* Responsive */
@media (max-width: 768px) {
  .modal-overlay {
    padding: 10px;
  }
  
  .modal-content {
    max-height: 95vh;
  }
  
  .modal-header {
    padding: 20px 20px 0;
  }
  
  .account-form {
    padding: 0 20px 20px;
  }
  
  .form-actions {
    flex-direction: column;
  }
  
  .cancel-button,
  .create-button {
    width: 100%;
  }
}
</style>
