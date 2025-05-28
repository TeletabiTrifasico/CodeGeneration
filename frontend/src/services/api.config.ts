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
    timeout: 15000
});

// Helper function to get authorization header
export const getAuthHeader = () => {
    const authStore = useAuthStore();
    return authStore.authToken ? { 'Authorization': `Bearer ${authStore.authToken}` } : {};
};

// Common API endpoints
export const API_ENDPOINTS = {
    auth: {
        login: '/auth/login',
        logout: '/auth/logout',
        validate: '/auth/validate',
        register: '/auth/register'
    },
    account: {
        getAll: '/account/getall',
        details: (accountNumber: string) => `/account/details/${accountNumber}`,
        search: '/account/search',
        getIBANByUsername: '/account/getIBANByUsername'
    },
    transaction: {
        getAll: '/transaction/getall',
        byAccount: (accountNumber: string) => `/transaction/byaccount/${accountNumber}`,
        transfer: '/transaction/transfer'
    },
    user: {
        byPage: (pageNumber: number, limit: number) => `/users?page=${pageNumber}&limit=${limit}`,
    },
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