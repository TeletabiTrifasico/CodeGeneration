<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import AuthService from '@/services/AuthService';
import requestService from '@/services/RequestService';


// User reactive state
const user = ref(AuthService.getCurrentUser());
const isLoading = ref(true);
const error = ref('');
</script>

<template>
  <div>default tab</div>
</template>
