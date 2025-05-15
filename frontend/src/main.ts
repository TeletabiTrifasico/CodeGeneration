import { createApp } from 'vue';
import App from './App.vue';
import router from './router';
import { pinia } from './stores';
import './assets/main.css';
import 'bootstrap'

// Create and mount app with pinia and router
createApp(App)
    .use(pinia)
    .use(router)
    .mount('#app');