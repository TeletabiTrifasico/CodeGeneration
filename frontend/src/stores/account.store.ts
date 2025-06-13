import { defineStore } from 'pinia';
import { Account, User } from '@/models';
import { useAuthStore } from './auth.store';
import { apiClient, API_ENDPOINTS, getAuthHeader } from '@/services/api.config';
import { currencyService } from '@/services/CurrencyService.ts';

interface AccountState {
    accounts: Account[];
    currentAccount: Account | null;
    loading: boolean;
    error: string | null;
    totalBalanceEur: number;
    isLoadingBalance: boolean;
}

export const useAccountStore = defineStore('account', {
    state: (): AccountState => ({
        accounts: [],
        currentAccount: null,
        loading: false,
        error: null,
        totalBalanceEur: 0,
        isLoadingBalance: false
    }),

    getters: {
        allAccounts(): Account[] {
            return this.accounts;
        },

        currentCurrency(): string {
            return this.currentAccount ? this.currentAccount.currency : 'EUR';
        },

        accountBalance(): number {
            if (this.currentAccount) {
                // Return balance for selected account in its own currency
                return this.currentAccount.balance;
            } else {
                // Return total balance in EUR
                return this.totalBalanceEur;
            }
        },

        accountsBycurrency(): Record<string, Account[]> {
            return this.accounts.reduce((acc, account) => {
                if (!acc[account.currency]) {
                    acc[account.currency] = [];
                }
                acc[account.currency].push(account);
                return acc;
            }, {} as Record<string, Account[]>);
        },

        isLoading(): boolean {
            return this.loading;
        },

        isLoadingTotalBalance(): boolean {
            return this.isLoadingBalance;
        }
    },

    actions: {
        async fetchAllAccounts() {
            const authStore = useAuthStore();

            if (!authStore.isLoggedIn) {
                this.error = 'Not authenticated';
                return [];
            }

            try {
                this.loading = true;
                this.error = null;

                const response = await apiClient.get(
                    API_ENDPOINTS.account.getAll,
                    { headers: getAuthHeader() }
                );

                this.accounts = response.data.accounts || [];

                // Calculate total balance in EUR after fetching accounts
                await this.calculateTotalBalanceInEur();

                return this.accounts;
            } catch (error: any) {
                console.error('Error fetching accounts:', error);
                this.error = error.message || 'Failed to load accounts';
                throw error;
            } finally {
                this.loading = false;
            }
        },

        async calculateTotalBalanceInEur() {
            if (this.accounts.length === 0) {
                this.totalBalanceEur = 0;
                return;
            }

            try {
                this.isLoadingBalance = true;

                // Convert all account balances to EUR
                const totalBalance = await currencyService.getTotalBalanceInEur(
                    this.accounts.map(account => ({
                        balance: account.balance,
                        currency: account.currency
                    }))
                );

                this.totalBalanceEur = totalBalance;
            } catch (error) {
                console.error('Error calculating total balance in EUR:', error);

                // Fallback to simple addition without conversion if API fails
                this.totalBalanceEur = this.accounts.reduce((total, account) => {
                    // Use fallback rates
                    const fallbackRates: Record<string, number> = {
                        'EUR': 1.0,
                        'USD': 0.93,
                        'GBP': 1.16,
                        'CHF': 1.05,
                        'PLN': 0.24
                    };

                    const rate = fallbackRates[account.currency] || 1.0;
                    return total + (account.balance * rate);
                }, 0);
            } finally {
                this.isLoadingBalance = false;
            }
        },

        setSelectedAccount(account: Account | null) {
            this.currentAccount = account;
        },

        async convertToEur(amount: number, fromCurrency: string): Promise<number> {
            try {
                return await currencyService.convertToEur(amount, fromCurrency);
            } catch (error) {
                console.error('Error converting to EUR:', error);

                // Fallback rates
                const fallbackRates: Record<string, number> = {
                    'EUR': 1.0,
                    'USD': 0.93,
                    'GBP': 1.16,
                    'CHF': 1.05,
                    'PLN': 0.24
                };

                const rate = fallbackRates[fromCurrency] || 1.0;
                return amount * rate;
            }
        },

        async refreshTotalBalance() {
            await this.calculateTotalBalanceInEur();
        },

        clearCache() {
            currencyService.clearCache();
        },
        async editLimits(values: {}) {
            console.log(values);
            const authStore = useAuthStore();

            if (!authStore.isLoggedIn) {
                this.error = 'Not authenticated';
                return [];
            }

            try {
                this.loading = true;
                this.error = null;

                const response = await apiClient.put(
                    API_ENDPOINTS.account.limits, values, 
                    { headers: getAuthHeader() }
                );
                console.log(response.data);
            }
            catch (error: any) {
                console.error('Error fetching accounts:', error);
                this.error = error.message || 'Failed to load accounts';
                throw error;
            } finally {
                this.loading = false;
            }
        },

        async createAccount(accountData: {
            accountName: string;
            accountType: string;
            currency: string;
            userId?: number;
        }) {
            const authStore = useAuthStore();

            if (!authStore.isLoggedIn) {
                throw new Error('Not authenticated');
            }

            try {
                this.loading = true;
                this.error = null;

                const response = await apiClient.post(
                    API_ENDPOINTS.account.create,
                    accountData,
                    { headers: getAuthHeader() }
                );

                const newAccount = response.data;

                return newAccount;
            } catch (error: any) {
                console.error('Error creating account:', error);
                this.error = error.response?.data?.message || 'Failed to create account';
                throw error;
            } finally {
                this.loading = false;
            }
        },

        async deleteAccount(accountNumber: string) {
            const authStore = useAuthStore();

            if (!authStore.isLoggedIn) {
                throw new Error('Not authenticated');
            }

            try {
                this.loading = true;
                this.error = null;

                const response = await apiClient.put(
                `${API_ENDPOINTS.account.disable}/${accountNumber}`,
                { headers: getAuthHeader() }
                );

                return response.data;
            } catch (error: any) {
                console.error('Error deleting account:', error);
                this.error = error.response?.data?.message || error.message || 'Failed to delete account';
                throw this.error;
            } finally {
                this.loading = false;
            }
        }
    }
});