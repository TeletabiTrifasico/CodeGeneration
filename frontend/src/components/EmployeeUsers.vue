<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import UserItem from './EmployeeUserItem.vue';
import { useAuthStore } from '@/stores/auth.store';
import { useUserStore } from '@/stores/user.store';

const authStore = useAuthStore();
const userStore = useUserStore();
const isLoading = ref(true);
const error = ref('');
let pageUsers = {};

onMounted(async () => {
  // Validate authentication token
  try {
    await authStore.validateToken();
    pageUsers = await userStore.getFirstPage();
    console.log(pageUsers);
  } catch (err) {
    console.error('Token validation error:', err);
    // authStore.logout() will be called in validateToken if it fails
  }
  isLoading.value = false;
});
</script>

<template>
  <span v-if="isLoading" class="spinner small"></span>
  <div v-else>
    <div v-for="item in pageUsers">
      <UserItem :user="item"/>
    </div>
  </div>
  
</template>

<style scoped>
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
</style>
