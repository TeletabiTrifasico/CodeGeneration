import { defineStore } from 'pinia';
import { useAuthStore } from './auth.store';
import { User } from '@/models';
import { apiClient, API_ENDPOINTS, getAuthHeader } from '@/services/api.config';

interface UserState {
    pagedUsers: User[];
    disabledUsers: User[];
    loading: boolean;
    error: string | null;
}
const USERS_PER_PAGE = 10; //Users per page for pagination

export const useUserStore = defineStore('use', {
    state: (): UserState => ({
        pagedUsers: [],
        disabledUsers: [],
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
        },

        async getDisabledUsersByPage(pageNumber: number) {
            const authStore = useAuthStore();
            if (!authStore.isLoggedIn) {
                this.error = 'Not authenticated';
                return [];
            }

            try {
                this.loading = true;
                this.error = null;

                const response = await apiClient.get(
                    API_ENDPOINTS.user.disabled(pageNumber, USERS_PER_PAGE),
                    { headers: getAuthHeader() }
                );
                this.disabledUsers = response.data.users;
                return this.disabledUsers;
            } catch (error: any) {
                console.error('Error fetching disabled users:', error);
                this.error = error.message || 'Failed to load disabled users';
                throw error;
            } finally {
                this.loading = false;
            }
        },

        async enableUser(userId: number) {
            const authStore = useAuthStore();
            if (!authStore.isLoggedIn) {
                this.error = 'Not authenticated';
                throw new Error('Not authenticated');
            }

            try {
                this.loading = true;
                this.error = null;

                const response = await apiClient.put(
                    API_ENDPOINTS.user.enable(userId),
                    {},
                    { headers: getAuthHeader() }
                );
                
                // Remove the enabled user from the disabled users list
                this.disabledUsers = this.disabledUsers.filter(user => user.id !== userId);
                
                return response.data;
            } catch (error: any) {
                console.error('Error enabling user:', error);
                this.error = error.message || 'Failed to enable user';
                throw error;
            } finally {
                this.loading = false;
            }
        },

        // Add a method to get filtered users
        async getFilteredUsers(filters, page = 1, limit = USERS_PER_PAGE) {
          const authStore = useAuthStore();
          if (!authStore.isLoggedIn) {
            this.error = 'Not authenticated';
            return [];
          }
        
          try {
            this.loading = true;
            this.error = null;
            
            // Since we don't have a dedicated filter endpoint, let's get all users and filter client-side
            const response = await apiClient.get(
              API_ENDPOINTS.user.byPage(page, limit),
              { headers: getAuthHeader() }
            );
            
            // Get users from the response
            const allUsers = response.data.users || [];
            
            // Do client-side filtering
            const filteredUsers = this.filterUsers(allUsers, filters);
            
            return filteredUsers;
          } catch (error: any) {
            console.error('Error fetching filtered users:', error);
            this.error = error.message || 'Failed to load filtered users';
            return [];
          } finally {
            this.loading = false;
          }
        },

        // Client-side filtering function
        filterUsers(users, filters) {
          return users.filter(user => {
            // Name filter
            if (filters.name && (!user.name || !user.name.toLowerCase().includes(filters.name.toLowerCase()))) {
              return false;
            }
            
            // Username filter
            if (filters.username && (!user.username || !user.username.toLowerCase().includes(filters.username.toLowerCase()))) {
              return false;
            }
            
            // Email filter
            if (filters.email && (!user.email || !user.email.toLowerCase().includes(filters.email.toLowerCase()))) {
              return false;
            }
            
            // Role filter
            if (filters.role && (!user.role || user.role !== filters.role)) {
              return false;
            }
            
            // Status filter
            if (filters.status !== undefined && filters.status !== '' && user.enabled !== (filters.status === 'true')) {
              return false;
            }
            
            // Min accounts filter
            if (filters.minAccounts && (!user.accounts || user.accounts.length < filters.minAccounts)) {
              return false;
            }
            
            // Balance filters
            if ((filters.minBalance || filters.maxBalance) && user.accounts) {
              const totalBalance = user.accounts.reduce((sum, acc) => sum + (parseFloat(acc.balance) || 0), 0);
              
              if (filters.minBalance && totalBalance < filters.minBalance) {
                return false;
              }
              
              if (filters.maxBalance && totalBalance > filters.maxBalance) {
                return false;
              }
            }
            
            return true;
          });
        }
    }
});