<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import UserItem from './EmployeeUserItem.vue';

const isLoading = ref(true);
const error = ref('');

onMounted(async () => {
  // Validate authentication token
  try {
    await authStore.validateToken();
  } catch (err) {
    console.error('Token validation error:', err);
    // authStore.logout() will be called in validateToken if it fails
  }
    isLoading.value = false;
});
</script>

<template>
  <UserItem/>
  
</template>
