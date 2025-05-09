import { defineStore } from 'pinia';
import { Transaction } from '@/models';
import { useAuthStore } from './auth.store';
import { apiClient, API_ENDPOINTS, getAuthHeader } from '@/services/api.config';

interface TransactionState {
    transactions: Transaction[];
    loading: boolean;
    error: string | null;
}

export const useTransactionStore = defineStore('transaction', {
    state: (): TransactionState => ({
        transactions: [],
        loading: false,
        error: null
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
        }
    }
});