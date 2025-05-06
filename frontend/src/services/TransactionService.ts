import requestService from './RequestService';
import AuthService from './AuthService';
import { Transaction } from '@/models';

class TransactionService {
    async getAllTransactions(): Promise<Transaction[]> {
        try {
            const token = AuthService.getToken();
            if (!token) {
                throw new Error('Not authenticated');
            }

            // Change to GET request!!
            const response = await requestService.post('/transaction/getall', { token });
            return response.transactions || [];
        } catch (err: any) {
            console.error('Error fetching transactions:', err);
            throw new Error(err.message || 'Failed to load transactions');
        }
    }

    async getTransactionsByAccount(accountNumber: string): Promise<Transaction[]> {
        try {
            const token = AuthService.getToken();
            if (!token) {
                throw new Error('Not authenticated');
            }

            // Remove the '/api' prefix since it's already in the RequestService base URL
            const response = await requestService.post('/transaction/byaccount', {
                token,
                accountNumber
            });
            return response.transactions || [];
        } catch (err: any) {
            console.error('Error fetching account transactions:', err);
            throw new Error(err.message || 'Failed to load account transactions');
        }
    }
}

export default new TransactionService();