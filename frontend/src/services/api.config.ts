/**
 * Centralized API configuration to be used across the application
 * This ensures consistent API usage in all Pinia stores
 */

import axios from 'axios';
import { useAuthStore } from '@/stores/auth.store';

// Base API configuration
export const API_BASE_URL = '/api';

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

// Transaction filter interface
export interface TransactionFilters {
    startDate?: string;
    endDate?: string;
    amountLessThan?: number;
    amountGreaterThan?: number;
    amountEqualTo?: number;
    iban?: string;
    transactionType?: string;
    transactionStatus?: string;
    description?: string;
}

// Currency exchange interfaces
export interface CurrencyExchange {
    fromCurrency: string;
    toCurrency: string;
    rate: number;
    originalAmount: number;
    convertedAmount: number;
    rateInfo: string;
}

export interface TransferPreview {
    transaction?: any;
    currencyExchangeApplied: boolean;
    exchangeInfo?: CurrencyExchange;
    message: string;
}

// Helper function to build query parameters from filters
export const buildFilterParams = (filters: TransactionFilters): URLSearchParams => {
    const params = new URLSearchParams();

    Object.entries(filters).forEach(([key, value]) => {
        if (value !== undefined && value !== null && value !== '') {
            params.append(key, value.toString());
        }
    });

    return params;
};

// Common API endpoints
export const API_ENDPOINTS = {
    auth: {
        login: '/auth/login',
        logout: '/auth/logout',
        validate: '/auth/validate',
        register: '/auth/register'
    },    account: {
        getAll: '/account/getall',
        details: (accountNumber: string) => `/account/details/${accountNumber}`,
        search: '/account/search',
        getIBANByUsername: '/account/getIBANByUsername',
        limits: '/account/limits',
        create: '/account/create'
    },
    transaction: {
        getAll: '/transaction/getall',
        filter: '/transaction/filter',
        byAccount: (accountNumber: string) => `/transaction/byaccount/${accountNumber}`,
        byAccountFilter: (accountNumber: string) => `/transaction/byaccount/${accountNumber}/filter`,
        transfer: '/transaction/transfer',
        transferPreview: '/transaction/transfer/preview'
    },
    currency: {
        exchangeRate: '/currency/exchange-rate',
        convert: '/currency/convert'
    },
    user: {
        byPage: (pageNumber: number, limit: number) => `/users?page=${pageNumber}&limit=${limit}`,
        byId:(id: number) => `/users/${id}`,
        disabled: (pageNumber: number, limit: number) => `/users/disabled?page=${pageNumber}&limit=${limit}`,
        enable: (userId: number) => `/users/${userId}/enable`,
    },
    
    // ATM endpoints
    atm: {
        deposit: '/atm/deposit', // POST 
        withdraw: '/atm/withdraw' // POST
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