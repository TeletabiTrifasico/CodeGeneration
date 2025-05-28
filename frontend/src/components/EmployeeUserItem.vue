<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';

const isLoading = ref(true);
const error = ref('');

const props = defineProps<{
  user: {
    id: number,
    username: string,
    name: string,
    email: string,
    role: UserRole,
    enabled: true,
    accounts: {
      balance: number;
    }[];
  }
}>();
const status = computed(() => (props.user.enabled ? 'Active' : 'Disabled'));

const totalBalance = computed(() =>
  props.user.accounts.reduce((sum, acc) => sum + acc.balance, 0)
);
</script>

<template>
  <a href="employeePanel/user" class="user-card">
    <div class="name">{{ user.name }}</div>
    <div class="meta">
      <span>{{ user.accounts.length }} accounts</span>
      <span>€{{ totalBalance.toFixed(2) }}</span>
      <span :class="['status', user.enabled ? 'active' : 'disabled']">{{ status }}</span>
    </div>
  </a>
</template>

<style scoped> 
.user-card {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  background-color: #ffffff;
  border: 1px solid #ddd;
  border-radius: 10px;
  padding: 16px;
  color: #333;
  text-decoration: none;
  transition: box-shadow 0.2s ease;
}

.user-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.name {
  font-weight: 600;
  color: #4CAF50;
  margin-bottom: 8px;
}

.meta {
  display: flex;
  justify-content: space-between;
  font-size: 0.95rem;
  color: #666;
}
</style>
