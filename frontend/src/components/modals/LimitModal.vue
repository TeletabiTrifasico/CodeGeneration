<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue';
import { useAuthStore } from '@/stores/auth.store';
import { useAccountStore } from '@/stores/account.store';
import { Account } from '@/models';
import { apiClient, API_ENDPOINTS, getAuthHeader, TransferPreview, CurrencyExchange } from '@/services/api.config';

const props = defineProps<{
  show: boolean;
  selectedAccount?: Account;
}>();
let formValues = {
    dailyTransferLimit: 0,
    singleTransferLimit: 0,
    dailyWithdrawalLimit: 0,
    singleWithdrawalLimit: 0
}
const emit = defineEmits(['close', 'edit-complete']);
const closeModal = () => {
    resetForm();
    emit('close', formValues);
};
const resetForm  = () => {
    console.log("resetting");
};
onMounted( () => {
    console.log("test");
});
const submitForm = () => {
    console.log(formValues);
};
watch(() => props.show, (newVal, oldVal) => {
  console.log('show changed from', oldVal, 'to', newVal)
  if (newVal = true) {
    formValues.dailyTransferLimit = props.selectedAccount.dailyTransferLimit;
    formValues.dailyWithdrawalLimit = props.selectedAccount.dailyWithdrawalLimit;
    formValues.singleTransferLimit = props.selectedAccount.singleTransferLimit;
    formValues.singleWithdrawalLimit = props.selectedAccount.singleWithdrawalLimit;
  }
});

</script>
<template>
    <div v-if="show" class="modal-overlay" @click="closeModal">
        <div class="modal-container" @click.stop>
            <div class="modal-header">
                <h2>Edit account limits</h2> <!-- Only the text is specific -->
                <button class="close-button" @click="closeModal">&times;</button>
            </div>

            <div class="modal-body">
                <!-- Modal content goes here -->
                <form class="form" @submit.prevent="submitForm">
                    <label :for="'dailyTransfer'">Daily transfer limit</label>
                    <input
                        :id="'dailyTransfer'"
                        v-model="formValues.dailyTransferLimit"
                        type="number"
                        min="0"
                        :step="1"
                        class="form-control"
                        required
                    />
                    <label :for="'dailyWithdrawal'">Daily withdrawal limit</label>
                    <input
                        :id="'dailyWithdrawal'"
                        v-model="formValues.dailyWithdrawalLimit"
                        type="number"
                        min="0"
                        :step="1"
                        class="form-control"
                        required
                    />
                    <label :for="'singleTransfer'">Single transfer limit</label>
                    <input
                        :id="'singleTransfer'"
                        v-model="formValues.singleTransferLimit"
                        type="number"
                        min="0"
                        :step="1"
                        class="form-control"
                        required
                    />
                    <label :for="'singleWithdrawal'">Single withdrawal limit</label>
                    <input
                        :id="'singleWithdrawal'"
                        v-model="formValues.singleWithdrawalLimit"
                        type="number"
                        min="0"
                        :step="1"
                        class="form-control"
                        required
                    />

                    <div class="form-actions">
                    <button type="submit" class="button primary">Save</button>
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
.form {
    display: flex;
    flex-direction: column;
    align-items: baseline;
}
</style>