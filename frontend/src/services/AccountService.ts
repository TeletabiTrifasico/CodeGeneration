import requestService from './RequestService';
import AuthService from './AuthService';
import { Account } from '@/models';

class AccountService {
    async getAllAccounts(): Promise<Account[]> {
        try {
            const token = AuthService.getToken();
            if (!token) {
                throw new Error('Not authenticated');
            }

            const response = await requestService.post('/account/getall', { token });
            return response.accounts || [];
        } catch (err: any) {
            console.error('Error fetching accounts:', err);
            throw new Error(err.message || 'Failed to load accounts');
        }
    }

    async getAccountDetails(accountNumber: string): Promise<Account> {
        try {
            const token = AuthService.getToken();
            if (!token) {
                throw new Error('Not authenticated');
            }

            const response = await requestService.post('/account/details', {
                token,
                accountNumber
            });
            return response;
        } catch (err: any) {
            console.error('Error fetching account details:', err);
            throw new Error(err.message || 'Failed to load account details');
        }
    }
}

export default new AccountService();