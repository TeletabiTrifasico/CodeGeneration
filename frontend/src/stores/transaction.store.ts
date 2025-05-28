import { defineStore } from 'pinia';
import { Transaction } from '@/models';
import { useAuthStore } from './auth.store';
import { apiClient, API_ENDPOINTS, getAuthHeader, TransactionFilters, buildFilterParams } from '@/services/api.config';

interface TransactionState {
    transactions: Transaction[];
    loading: boolean;
    error: string | null;
    currentFilters: TransactionFilters;
}

export const useTransactionStore = defineStore('transaction', {
    state: (): TransactionState => ({
        transactions: [],
        loading: false,
        error: null,
        currentFilters: {}
    }),

    getters: {
        allTransactions(): Transaction[] {
            return this.transactions;
        },

        sortedTransactions(): Transaction[] {
            return [...this.transactions].sort((a, b) => {
                const dateA = a.createAt instanceof Date ? a.createAt : new Date(a.createAt);
                const dateB = b.createAt instanceof Date ? b.createAt : new Date(b.createAt);
                return dateB.getTime() - dateA.getTime();
            });
        },

        isLoading(): boolean {
            return this.loading;
        },

        hasActiveFilters(): boolean {
            return Object.keys(this.currentFilters).length > 0 &&
                Object.values(this.currentFilters).some(value => value !== undefined && value !== null && value !== '');
        }
    },

    actions: {
        async fetchAllTransactions() {
            const authStore = useAuthStore();

            if (!authStore.isLoggedIn) {
                this.error = 'Not authenticated';
                return [];
            }

            try {
                this.loading = true;
                this.error = null;
                this.currentFilters = {};

                const response = await apiClient.get(
                    API_ENDPOINTS.transaction.getAll,
                    { headers: getAuthHeader() }
                );

                this.transactions = response.data.transactions || [];
                return this.transactions;
            } catch (error: any) {
                console.error('Error fetching transactions:', error);
                this.error = error.message || 'Failed to load transactions';
                throw error;
            } finally {
                this.loading = false;
            }
        },

        async fetchTransactionsByAccount(accountNumber: string) {
            const authStore = useAuthStore();

            if (!authStore.isLoggedIn) {
                this.error = 'Not authenticated';
                return [];
            }

            try {
                this.loading = true;
                this.error = null;
                this.currentFilters = {};

                const response = await apiClient.get(
                    API_ENDPOINTS.transaction.byAccount(accountNumber),
                    { headers: getAuthHeader() }
                );

                this.transactions = response.data.transactions || [];
                return this.transactions;
            } catch (error: any) {
                console.error('Error fetching account transactions:', error);
                this.error = error.message || 'Failed to load account transactions';
                throw error;
            } finally {
                this.loading = false;
            }
        },

        async fetchFilteredTransactions(filters: TransactionFilters) {
            const authStore = useAuthStore();

            if (!authStore.isLoggedIn) {
                this.error = 'Not authenticated';
                return [];
            }

            try {
                this.loading = true;
                this.error = null;
                this.currentFilters = { ...filters };

                const params = buildFilterParams(filters);
                const url = `${API_ENDPOINTS.transaction.filter}?${params.toString()}`;

                const response = await apiClient.get(url, { headers: getAuthHeader() });

                this.transactions = response.data.transactions || [];
                return this.transactions;
            } catch (error: any) {
                console.error('Error fetching filtered transactions:', error);
                this.error = error.message || 'Failed to load filtered transactions';
                throw error;
            } finally {
                this.loading = false;
            }
        },

        async fetchFilteredTransactionsByAccount(accountNumber: string, filters: TransactionFilters) {
            const authStore = useAuthStore();

            if (!authStore.isLoggedIn) {
                this.error = 'Not authenticated';
                return [];
            }

            try {
                this.loading = true;
                this.error = null;
                this.currentFilters = { ...filters };

                const params = buildFilterParams(filters);
                const url = `${API_ENDPOINTS.transaction.byAccountFilter(accountNumber)}?${params.toString()}`;

                const response = await apiClient.get(url, { headers: getAuthHeader() });

                this.transactions = response.data.transactions || [];
                return this.transactions;
            } catch (error: any) {
                console.error('Error fetching filtered account transactions:', error);
                this.error = error.message || 'Failed to load filtered account transactions';
                throw error;
            } finally {
                this.loading = false;
            }
        },

        clearFilters() {
            this.currentFilters = {};
        },

        setFilters(filters: TransactionFilters) {
            this.currentFilters = { ...filters };
        }
    }
});