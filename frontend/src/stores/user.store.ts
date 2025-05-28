import { defineStore } from 'pinia';
import { useAuthStore } from './auth.store';
import { User } from '@/models';
import { apiClient, API_ENDPOINTS, getAuthHeader } from '@/services/api.config';

interface UserState {
    pagedUsers: User[];
    loading: boolean;
    error: string | null;
}
const USERS_PER_PAGE = 10; //Should be a constant value

export const useUserStore = defineStore('use', {
    state: (): UserState => ({
        pagedUsers: [],
        loading: false,
        error: null
    }),

    getters: {


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
                this.pagedUsers = response.data.users;
                return this.pagedUsers;
            } catch (error: any) {
                console.error('Error fetching users:', error);
                this.error = error.message || 'Failed to load users';
                throw error;
            } finally {
                this.loading = false;
            }
        },
        async getFirstPage() {
            return await this.getUsersByPage(1);
        },
        async getUserById(id: number) {
            const authStore = useAuthStore();
            if (!authStore.isLoggedIn) {
                this.error = 'Not authenticated';
                return [];
            }
            try {
                this.loading = true;
                this.error = null;
                const response = await apiClient.get(
                    API_ENDPOINTS.user.byId(id),
                    { headers: getAuthHeader() }
                );
                
                return response.data.users[0];
            } catch (error: any) {
                console.error('Error fetching user:', error);
                this.error = error.message || 'Failed to load user';
                throw error;
            } finally {
                this.loading = false;
            }
        }
    }
});