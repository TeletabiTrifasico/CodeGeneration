<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';


// User reactive state
const user = ref(AuthService.getCurrentUser());
const isLoading = ref(true);
const error = ref('');
</script>

<template>
  <div>default tab</div>
</template>
