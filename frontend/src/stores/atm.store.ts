import { defineStore } from 'pinia';
import { useAuthStore } from './auth.store';
import { apiClient, API_ENDPOINTS, getAuthHeader } from '@/services/api.config';

interface AtmState {
    isProcessing: boolean;
    error: string | null;
    lastTransaction: {
        transactionReference: string | null;
        amount: number | null;
        success: boolean;
        updatedBalance: number | null;
    };
}

/**
 * Store for ATM operations (deposit and withdrawal)
 */
export const useAtmStore = defineStore('atm', {
    state: (): AtmState => ({
        isProcessing: false,
        error: null,
        lastTransaction: {
            transactionReference: null,
            amount: null,
            success: false,
            updatedBalance: null
        }
    }),

    actions: {
        /**
         * Clear any error messages
         */
        clearError() {
            this.error = null;
        },

        /**
         * Reset the transaction state
         */
        resetTransaction() {
            this.lastTransaction = {
                transactionReference: null,
                amount: null,
                success: false,
                updatedBalance: null
            };
        },

        /**
         * Deposit money into an account
         * 
         * @param accountNumber Account number to deposit into
         * @param amount Amount to deposit
         * @param description Optional transaction description
         * @returns Boolean indicating success
         */
        async deposit(accountNumber: string, amount: number, description?: string): Promise<boolean> {
            const authStore = useAuthStore();

            if (!authStore.isLoggedIn) {
                this.error = 'Not authenticated';
                return false;
            }

            try {
                this.isProcessing = true;
                this.error = null;

                const response = await apiClient.post(
                    API_ENDPOINTS.atm.deposit,
                    {
                        accountNumber,
                        amount,
                        description: description || 'ATM Deposit'
                    },
                    { headers: getAuthHeader() }
                );

                // Update last transaction info
                this.lastTransaction = {
                    transactionReference: response.data.transactionReference,
                    amount,
                    success: response.data.success,
                    updatedBalance: response.data.updatedBalance
                };

                return response.data.success;
            } catch (error: any) {
                console.error('Error processing deposit:', error);
                
                if (error.response?.data?.errorMessage) {
                    this.error = error.response.data.errorMessage;
                } else if (error.response?.status === 401) {
                    this.error = 'Authentication failed. Please log in again.';
                    authStore.logout();
                } else {
                    this.error = error.message || 'Failed to process deposit';
                }
                
                return false;
            } finally {
                this.isProcessing = false;
            }
        },

        /**
         * Withdraw money from an account
         * 
         * @param accountNumber Account number to withdraw from
         * @param amount Amount to withdraw
         * @param description Optional transaction description
         * @returns Boolean indicating success
         */
        async withdraw(accountNumber: string, amount: number, description?: string): Promise<boolean> {
            const authStore = useAuthStore();

            if (!authStore.isLoggedIn) {
                this.error = 'Not authenticated';
                return false;
            }

            try {
                this.isProcessing = true;
                this.error = null;

                const response = await apiClient.post(
                    API_ENDPOINTS.atm.withdraw,
                    {
                        accountNumber,
                        amount,
                        description: description || 'ATM Withdrawal'
                    },
                    { headers: getAuthHeader() }
                );

                // Update last transaction info
                this.lastTransaction = {
                    transactionReference: response.data.transactionReference,
                    amount,
                    success: response.data.success,
                    updatedBalance: response.data.updatedBalance
                };

                return response.data.success;
            } catch (error: any) {
                console.error('Error processing withdrawal:', error);
                
                if (error.response?.data?.errorMessage) {
                    this.error = error.response.data.errorMessage;
                } else if (error.response?.status === 401) {
                    this.error = 'Authentication failed. Please log in again.';
                    authStore.logout();
                } else {
                    this.error = error.message || 'Failed to process withdrawal';
                }
                
                return false;
            } finally {
                this.isProcessing = false;
            }
        }
    }
});