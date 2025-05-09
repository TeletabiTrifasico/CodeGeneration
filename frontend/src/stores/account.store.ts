import { defineStore } from 'pinia';
import { Account } from '@/models';
import { useAuthStore } from './auth.store.ts';
import { apiClient, API_ENDPOINTS, getAuthHeader } from '@/services/api.config.ts';

interface AccountState {
    accounts: Account[];
    selectedAccount: Account | null;
    loading: boolean;
    error: string | null;
}

export const useAccountStore = defineStore('account', {
    state: (): AccountState => ({
        accounts: [],
        selectedAccount: null,
        loading: false,
        error: null
    }),

    getters: {
        allAccounts(): Account[] {
            return this.accounts;
        },

        currentAccount(): Account | null {
            return this.selectedAccount;
        },

        accountBalance(): number {
            if (this.selectedAccount) {
                return this.selectedAccount.balance;
            }

            // If no account is selected, sum up all account balances
            // TODO: Consider exchange rates for different currencies
            return this.accounts.reduce((total, account) => total + account.balance, 0);
        },

        currentCurrency(): string {
            return this.selectedAccount?.currency || 'EUR';
        },

        isLoading(): boolean {
            return this.loading;
        }
    },

    actions: {
        setSelectedAccount(account: Account | null) {
            this.selectedAccount = account;
        },

        async fetchAllAccounts() {
            const authStore = useAuthStore();

            if (!authStore.isLoggedIn) {
                this.error = 'Not authenticated';
                return [];
            }

            try {
                this.loading = true;
                this.error = null;

                const response = await apiClient.get(API_ENDPOINTS.account.getAll, {
                    headers: getAuthHeader()
                });

                this.accounts = response.data.accounts || [];
                return this.accounts;
            } catch (error: any) {
                console.error('Error fetching accounts:', error);
                this.error = error.message || 'Failed to load accounts';
                throw error;
            } finally {
                this.loading = false;
            }
        },

        async fetchAccountDetails(accountNumber: string) {
            const authStore = useAuthStore();

            if (!authStore.isLoggedIn) {
                this.error = 'Not authenticated';
                return null;
            }

            try {
                this.loading = true;
                this.error = null;

                const response = await apiClient.get(
                    API_ENDPOINTS.account.details(accountNumber),
                    { headers: getAuthHeader() }
                );

                // Update the account in the accounts array if it exists
                const accountIndex = this.accounts.findIndex(
                    acc => acc.accountNumber === accountNumber
                );

                if (accountIndex !== -1) {
                    this.accounts[accountIndex] = response.data;
                }

                return response.data;
            } catch (error: any) {
                console.error('Error fetching account details:', error);
                this.error = error.message || 'Failed to load account details';
                throw error;
            } finally {
                this.loading = false;
            }
        }
    }
});