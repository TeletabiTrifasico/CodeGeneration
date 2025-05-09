/**
 * Centralized API configuration to be used across the application
 * This ensures consistent API usage in all Pinia stores
 */

import axios from 'axios';
import { useAuthStore } from '@/stores/auth.store';

// Base API configuration
export const API_BASE_URL = '/api'; // Will be proxied by Vite

// Create a reusable axios instance with common configuration
export const apiClient = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json'
    },
    timeout: 15000 // 15 seconds timeout
});

// Helper function to get authorization header
export const getAuthHeader = () => {
    const authStore = useAuthStore();
    return authStore.authToken ? { 'Authorization': `Bearer ${authStore.authToken}` } : {};
};

// Common API endpoints
export const API_ENDPOINTS = {
    // Auth endpoints
    auth: {
        login: '/auth/login', // POST
        logout: '/auth/logout', // POST
        register: '/auth/register', // POST
        validate: '/auth/validate' // GET
    },

    // Account endpoints
    account: {
        getAll: '/account/getall', // GET with Authorization header
        details: (accountNumber: string) => `/account/details/${accountNumber}` // GET with Authorization header
    },

    // Transaction endpoints
    transaction: {
        getAll: '/transaction/getall', // GET with Authorization header
        byAccount: (accountNumber: string) => `/transaction/byaccount/${accountNumber}` // GET with Authorization header
    },
    
    // ATM endpoints
    atm: {
        deposit: '/atm/deposit', // POST 
        withdraw: '/atm/withdraw' // POST
    }
};

// Add request interceptor to automatically add Authorization header to all requests
apiClient.interceptors.request.use(
    (config) => {
        const authStore = useAuthStore();

        if (authStore.authToken) {
            config.headers['Authorization'] = `Bearer ${authStore.authToken}`;
        }

        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);