<script setup lang="ts">
import { ref, watch } from 'vue';
import { useAccountStore } from '@/stores/account.store';
import { Account } from '@/models';

const props = defineProps<{
  show: boolean;
  selectedAccount?: Account;
}>();

const emit = defineEmits(['close', 'delete-complete']);
const isProcessing = ref(false);
const errorMessage = ref('');

const closeModal = () => {
  if (!isProcessing.value) {
    errorMessage.value = '';
    emit('close');
  }
};

const deleteAccount = async () => {
  if (!props.selectedAccount) return;

  isProcessing.value = true;
  errorMessage.value = '';

  try {
    const accountStore = useAccountStore();
    await accountStore.deleteAccount(props.selectedAccount.accountNumber);
    emit('delete-complete');
    closeModal();
  } catch (err: any) {
    console.error('Error deleting account:', err);

  // If it's a full Axios error
  if (err?.response?.data?.message) {
    errorMessage.value = err.response.data.message;
  }
  // If it's a plain error object or string
  else if (typeof err === 'string') {
    errorMessage.value = err;
  }
  else if (err instanceof Error && err.message) {
    errorMessage.value = err.message;
  }
  else {
    errorMessage.value = 'Something went wrong while deleting the account.';
  }
  } finally {
    isProcessing.value = false;
  }
};


watch(() => props.show, (newVal) => {
  if (newVal) {
    errorMessage.value = '';
  }
});
</script>

<template>
  <div v-if="show" class="modal-overlay" @click="closeModal">
    <div class="modal-container" @click.stop>
      <div class="modal-header">
        <h2>Close Account</h2>
        <button class="close-button" @click="closeModal">&times;</button>
      </div>

      <div class="modal-body">
        <p>
          Are you sure you want to close this account?
          <strong>This action is permanent</strong> and cannot be undone.
        </p>
        <p>Account Number: <strong>{{ selectedAccount?.accountNumber }}</strong></p>

        <div v-if="errorMessage" class="error-message">{{ errorMessage }}</div>

        <div class="form-actions">
          <button class="button primary" @click="deleteAccount" :disabled="isProcessing">
            Close Account
          </button>
          <button class="button secondary" @click="closeModal" :disabled="isProcessing">
            Cancel
          </button>
        </div>
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

.modal-body {
  padding: 25px;
}

.error-message {
  color: #d32f2f;
  font-weight: bold;
  margin-top: 15px;
}

.form-actions {
  display: flex;
  justify-content: flex-start;
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
  min-width: 140px;
}
.button.primary {
  background-color: #d32f2f;
  color: white;
}
.button.primary:hover:not(:disabled) {
  background-color: #b71c1c;
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
</style>
