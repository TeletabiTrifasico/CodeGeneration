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
     * Clear exchange rate cache
     */
    clearCache(): void {
        this.exchangeRatesCache.clear();
    }
}

// Export singleton instance
export const currencyService = new CurrencyService();