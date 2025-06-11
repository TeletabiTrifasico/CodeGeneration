import { apiClient, API_ENDPOINTS, getAuthHeader } from '@/services/api.config';

interface ExchangeRateResponse {
    fromCurrency: string;
    toCurrency: string;
    rate: number;
    originalAmount: number;
    convertedAmount: number;
    rateInfo: string;
}

class CurrencyService {
    private exchangeRatesCache: Map<string, { rate: number; timestamp: number }> = new Map();
    private readonly CACHE_DURATION = 5 * 60 * 1000; // 5 minutes cache

    /**
     * Get exchange rate from source currency to EUR
     */
    async getExchangeRateToEur(fromCurrency: string): Promise<number> {
        if (fromCurrency === 'EUR') {
            return 1.0;
        }

        const cacheKey = `${fromCurrency}_EUR`;
        const cached = this.exchangeRatesCache.get(cacheKey);

        // Check if we have a valid cached rate
        if (cached && (Date.now() - cached.timestamp) < this.CACHE_DURATION) {
            return cached.rate;
        }

        try {
            const response = await apiClient.get(API_ENDPOINTS.currency.exchangeRate, {
                params: {
                    fromCurrency: fromCurrency,
                    toCurrency: 'EUR',
                    amount: 1
                },
                headers: getAuthHeader()
            });

            const data: ExchangeRateResponse = response.data;
            const rate = data.rate;

            // Cache the rate
            this.exchangeRatesCache.set(cacheKey, {
                rate: rate,
                timestamp: Date.now()
            });

            return rate;
        } catch (error) {
            console.error(`Error fetching exchange rate for ${fromCurrency} to EUR:`, error);

            // Return fallback rates if API fails
            const fallbackRates: Record<string, number> = {
                'USD': 0.93,
                'GBP': 1.16,
                'CHF': 1.05,
                'PLN': 0.24
            };

            return fallbackRates[fromCurrency] || 1.0;
        }
    }

    /**
     * Convert amount to EUR using current exchange rates
     */
    async convertToEur(amount: number, fromCurrency: string): Promise<number> {
        if (fromCurrency === 'EUR') {
            return amount;
        }

        const rate = await this.getExchangeRateToEur(fromCurrency);
        return amount * rate;
    }

    /**
     * Convert multiple amounts to EUR in batch
     */
    async convertMultipleToEur(amounts: Array<{ amount: number; currency: string }>): Promise<number[]> {
        const conversions = await Promise.all(
            amounts.map(async ({ amount, currency }) => {
                return await this.convertToEur(amount, currency);
            })
        );

        return conversions;
    }

    /**
     * Get total balance in EUR for multiple accounts
     */
    async getTotalBalanceInEur(accounts: Array<{ balance: number; currency: string }>): Promise<number> {
        const conversions = await this.convertMultipleToEur(
            accounts.map(acc => ({ amount: acc.balance, currency: acc.currency }))
        );

        return conversions.reduce((total, amount) => total + amount, 0);
    }

    /**
     * Get exchange rate from EUR to target currency
     */
    async getExchangeRateFromEur(toCurrency: string): Promise<number> {
        if (toCurrency === 'EUR') {
            return 1.0;
        }

        const cacheKey = `EUR_${toCurrency}`;
        const cached = this.exchangeRatesCache.get(cacheKey);

        // Check if we have a valid cached rate
        if (cached && (Date.now() - cached.timestamp) < this.CACHE_DURATION) {
            return cached.rate;
        }

        try {
            const response = await apiClient.get(API_ENDPOINTS.currency.exchangeRate, {
                params: {
                    fromCurrency: 'EUR',
                    toCurrency: toCurrency,
                    amount: 1
                },
                headers: getAuthHeader()
            });

            const data: ExchangeRateResponse = response.data;
            const rate = data.rate;

            // Cache the rate
            this.exchangeRatesCache.set(cacheKey, {
                rate: rate,
                timestamp: Date.now()
            });

            return rate;
        } catch (error) {
            console.error(`Error fetching exchange rate from EUR to ${toCurrency}:`, error);

            // Return fallback rates if API fails (inverse of EUR rates)
            const fallbackRates: Record<string, number> = {
                'USD': 1.08,  // 1/0.93
                'GBP': 0.86,  // 1/1.16
                'CHF': 0.95,  // 1/1.05
                'PLN': 4.17   // 1/0.24
            };

            return fallbackRates[toCurrency] || 1.0;
        }
    }

    /**
     * Convert amount from EUR to target currency
     */
    async convertFromEur(amount: number, toCurrency: string): Promise<number> {
        if (toCurrency === 'EUR') {
            return amount;
        }

        const rate = await this.getExchangeRateFromEur(toCurrency);
        return amount * rate;
    }

    /**
     * Convert account limits from EUR defaults to account currency
     * EUR defaults: singleTransferLimit=3000, dailyTransferLimit=5000, singleWithdrawalLimit=500, dailyWithdrawalLimit=5000
     */
    async convertLimitsToAccountCurrency(accountCurrency: string): Promise<{
        singleTransferLimit: number;
        dailyTransferLimit: number;
        singleWithdrawalLimit: number;
        dailyWithdrawalLimit: number;
    }> {
        // Default EUR limits
        const eurLimits = {
            singleTransferLimit: 3000,
            dailyTransferLimit: 5000,
            singleWithdrawalLimit: 500,
            dailyWithdrawalLimit: 5000
        };

        if (accountCurrency === 'EUR') {
            return eurLimits;
        }

        // Convert all limits to account currency
        const [singleTransferLimit, dailyTransferLimit, singleWithdrawalLimit, dailyWithdrawalLimit] = await Promise.all([
            this.convertFromEur(eurLimits.singleTransferLimit, accountCurrency),
            this.convertFromEur(eurLimits.dailyTransferLimit, accountCurrency),
            this.convertFromEur(eurLimits.singleWithdrawalLimit, accountCurrency),
            this.convertFromEur(eurLimits.dailyWithdrawalLimit, accountCurrency)
        ]);

        return {
            singleTransferLimit: Math.round(singleTransferLimit * 100) / 100, // Round to 2 decimal places
            dailyTransferLimit: Math.round(dailyTransferLimit * 100) / 100,
            singleWithdrawalLimit: Math.round(singleWithdrawalLimit * 100) / 100,
            dailyWithdrawalLimit: Math.round(dailyWithdrawalLimit * 100) / 100
        };
    }

    /**
     * Clear exchange rate cache
     */
    clearCache(): void {
        this.exchangeRatesCache.clear();
    }
}

// Export singleton instance
export const currencyService = new CurrencyService();