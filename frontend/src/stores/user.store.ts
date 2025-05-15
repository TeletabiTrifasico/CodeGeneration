import { defineStore } from 'pinia';
import { useAuthStore } from './auth.store';
import { User } from '@/models';
import { apiClient, API_ENDPOINTS, getAuthHeader } from '@/services/api.config';

interface UserState {
    users: User[];
    pagedUsers: User[];
    loading: boolean;
    error: string | null;
}
const USERS_PER_PAGE = 10; //Should be a constant value

export const useUserStore = defineStore('use', {
    state: (): UserState => ({
        users: [],
        pagedUsers: [],
        loading: false,
        error: null
    }),

    getters: {
        allUsers(): User[] {
            return this.users;
        },


        isLoading(): boolean {
            return this.loading;
        }
    },

    actions: {
        async getUsersByPage(pageNumber: number){
            const authStore = useAuthStore();

            if (!authStore.isLoggedIn) {
                this.error = 'Not authenticated';
                return [];
            }

            try {
                this.loading = true;
                this.error = null;

                const response = await apiClient.get(
                    API_ENDPOINTS.user.byPage(pageNumber, USERS_PER_PAGE),
                    { headers: getAuthHeader() }
                );
                console.log(response.data.users);
                this.pagedUsers = response.data.users;
                return this.pagedUsers;
            } catch (error: any) {
                console.error('Error fetching account transactions:', error);
                this.error = error.message || 'Failed to load account transactions';
                throw error;
            } finally {
                this.loading = false;
            }
        },
    }
});