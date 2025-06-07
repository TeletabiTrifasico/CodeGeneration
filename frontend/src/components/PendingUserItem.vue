<template>
  <div class="user-card">
    <div class="user-content">
      <div class="name">{{ user.name || 'Unknown User' }}</div>
      <div class="meta">
        <span>{{ user.email || 'No email' }}</span>
        <span class="role">{{ user.role || 'CLIENT' }}</span>
        <span class="status pending">Pending Approval</span>
      </div>
    </div>
    
    <div class="user-actions">
      <button 
        @click="handleApproveUser" 
        :disabled="isApproving" 
        class="approve-button"
      >
        <span v-if="isApproving" class="spinner-small"></span>
        <span v-else>✓</span>
        {{ isApproving ? 'Approving...' : 'Approve' }}
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useUserStore } from '@/stores/user.store';
import type { User } from '@/models';

interface Props {
  user: User;
}

const props = defineProps<Props>();
const emit = defineEmits<{
  userApproved: [userId: number];
}>();

const userStore = useUserStore();
const isApproving = ref(false);

const handleApproveUser = async () => {
  if (isApproving.value) return;
  
  try {
    isApproving.value = true;
    await userStore.enableUser(props.user.id);
    emit('userApproved', props.user.id);
  } catch (error) {
    console.error('Error approving user:', error);
    // You might want to show a toast or error message here
  } finally {
    isApproving.value = false;
  }
};
</script>

<style scoped>
.user-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #ffffff;
  border: 1px solid #ddd;
  border-radius: 10px;
  padding: 16px;
  color: #333;
  text-decoration: none;
  transition: box-shadow 0.2s ease;
  margin-bottom: 8px;
}

.user-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.user-content {
  flex: 1;
}

.name {
  font-weight: 600;
  color: #4CAF50;
  margin-bottom: 8px;
  font-size: 1.1rem;
}

.meta {
  display: flex;
  justify-content: space-between;
  font-size: 0.95rem;
  color: #666;
  align-items: center;
}

.role {
  text-transform: uppercase;
  font-size: 0.8rem;
  font-weight: 500;
}

.status {
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 500;
  text-transform: uppercase;
}

.status.pending {
  background-color: #fff3cd;
  color: #856404;
  border: 1px solid #ffeaa7;
}

.user-actions {
  margin-left: 16px;
}

.approve-button {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 8px 16px;
  background-color: #4CAF50;
  color: white;
  border: none;
  border-radius: 6px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 0.9rem;
}

.approve-button:hover:not(:disabled) {
  background-color: #45a049;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(76, 175, 80, 0.3);
}

.approve-button:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
  transform: none;
}

.spinner-small {
  width: 14px;
  height: 14px;
  border: 2px solid #ffffff;
  border-top: 2px solid transparent;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

@media (max-width: 640px) {
  .user-card {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }
  
  .meta {
    flex-direction: column;
    gap: 4px;
    align-items: flex-start;
  }
  
  .user-actions {
    margin-left: 0;
    display: flex;
    justify-content: center;
  }
  
  .approve-button {
    width: 100%;
    justify-content: center;
  }
}
</style>
