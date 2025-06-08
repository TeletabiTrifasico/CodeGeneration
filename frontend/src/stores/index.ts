// Barrel file to export all stores
import { createPinia } from 'pinia';
import { useAuthStore } from './auth.store';
import { useAccountStore } from './account.store';
import { useTransactionStore } from './transaction.store';
import { useAtmStore } from './atm.store';

export const pinia = createPinia();

export {
    useAuthStore,
    useAccountStore,
    useTransactionStore,
    useAtmStore

};