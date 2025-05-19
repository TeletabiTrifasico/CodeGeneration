<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';

const isLoading = ref(true);
const error = ref('');

const props = defineProps<{
  user: object
}>();
</script>

<template>
  <a href="employeePanel/user"> {{ user }}</a>
</template>
