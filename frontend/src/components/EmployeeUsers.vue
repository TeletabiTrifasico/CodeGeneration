<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useAuthStore } from '@/stores/auth.store';
import requestService from '@/services/RequestService';

const authStore = useAuthStore();


// User reactive state
const user = ref(authStore.currentUser);
const isLoading = ref(true);
const error = ref('');
</script>

<template>
  <div>user tab</div>
  <div>user tab</div>
  <div>user tab</div>
  <div>user tab</div>
  <div>user tab</div>
  <div>user tab</div>
  <div>user tab</div>
  <div>user tab</div>
  <div>user tab</div>
  <div>user tab</div>
  
</template>
