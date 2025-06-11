<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue';
import { useAuthStore } from '@/stores/auth.store';
import { useAccountStore } from '@/stores/account.store';
import { Account } from '@/models';
import { apiClient, API_ENDPOINTS, getAuthHeader, TransferPreview, CurrencyExchange } from '@/services/api.config';

const props = defineProps<{
  selectedAccount: Account;
}>();
let formValues = {
    accountNumber: ref(""),
    dailyTransferLimit: ref(0),
    singleTransferLimit: ref(0),
    dailyWithdrawalLimit: ref(0),
    singleWithdrawalLimit: ref(0)
}
let isProcessing = false;
const emit = defineEmits(['close', 'edit-complete']);
const closeModal = () => {
    if(!isProcessing) {
        emit('close');
    }
};
onMounted(() => {
  console.log("mounted");
  console.log(props.selectedAccount);
  formValues.accountNumber.value = props.selectedAccount.accountNumber;
  formValues.dailyTransferLimit.value = props.selectedAccount.dailyTransferLimit;
  formValues.dailyWithdrawalLimit.value = props.selectedAccount.dailyWithdrawalLimit;
  formValues.singleTransferLimit.value = props.selectedAccount.singleTransferLimit;
  formValues.singleWithdrawalLimit.value = props.selectedAccount.singleWithdrawalLimit;
  console.log(props.selectedAccount.singleTransferLimit);
  console.log(formValues.singleTransferLimit);
})
const submitForm = () => {
    isProcessing = true;
    let accountStore = useAccountStore();
    let values = {
      accountNumber: formValues.accountNumber.value,
      dailyTransferLimit: formValues.dailyTransferLimit.value,
      singleTransferLimit: formValues.singleTransferLimit.value,
      dailyWithdrawalLimit: formValues.dailyWithdrawalLimit.value,
      singleWithdrawalLimit: formValues.singleWithdrawalLimit.value
    }
    accountStore.editLimits(values);
    emit('edit-complete');
    isProcessing = false;
    closeModal();
};

// watch(() => props.selectedAccount, (newVal, oldVal) => {
//   console.log(newVal);
//   formValues.accountNumber = props.selectedAccount.accountNumber;
//   formValues.dailyTransferLimit = props.selectedAccount.dailyTransferLimit;
//   formValues.dailyWithdrawalLimit = props.selectedAccount.dailyWithdrawalLimit;
//   formValues.singleTransferLimit = props.selectedAccount.singleTransferLimit;
//   formValues.singleWithdrawalLimit = props.selectedAccount.singleWithdrawalLimit;
// });

</script>
<template>
    <div class="modal-overlay" @click="closeModal">
        <div class="modal-container" @click.stop>
            <div class="modal-header">
                <h2>Edit account limits</h2> <!-- Only the text is specific -->
                <button class="close-button" @click="closeModal">&times;</button>
            </div>

            <div class="modal-body">
                <!-- Modal content goes here -->
                <form class="form" @submit.prevent="submitForm">
                    <div class="form-group">
                    <label :for="'dailyTransfer'">Daily transfer limit</label>
                    <input
                        :id="'dailyTransfer'"
                        v-model="formValues.dailyTransferLimit.value"
                        type="number"
                        min="0"
                        :step="1"
                        class="form-control"
                        required
                    /></div>
                    <div class="form-group">
                    <label :for="'dailyWithdrawal'">Daily withdrawal limit</label>
                    <input
                        :id="'dailyWithdrawal'"
                        v-model="formValues.dailyWithdrawalLimit.value"
                        type="number"
                        min="0"
                        :step="1"
                        class="form-control"
                        required
                    /></div>
                    <div class="form-group">
                    <label :for="'singleTransfer'">Single transfer limit</label>
                    <input
                        :id="'singleTransfer'"
                        v-model="formValues.singleTransferLimit.value"
                        type="number"
                        min="0"
                        :step="1"
                        class="form-control"
                        required
                    /></div>
                    <div class="form-group">
                    <label :for="'singleWithdrawal'">Single withdrawal limit</label>
                    <input
                        :id="'singleWithdrawal'"
                        v-model="formValues.singleWithdrawalLimit.value"
                        type="number"
                        min="0"
                        :step="1"
                        class="form-control"
                        required
                    /></div>

                    <div class="form-actions">
                      
                    <button type="submit" class="button primary" :disabled="isProcessing">Save</button>
                    <button
                        type="button"
                        class="button secondary"
                        @click="closeModal"
                        :disabled="isProcessing"
                    >
                      Cancel
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
  max-width: 600px;
  max-height: 90vh;
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
  max-height: calc(90vh - 100px);
  overflow-y: auto;
}
.form-group {
    margin:20px;
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
  min-width: 120px;
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
.form-actions {
  display: flex;
  justify-content: flex-start;
  gap: 15px;
  margin-top: 30px;
}
</style>