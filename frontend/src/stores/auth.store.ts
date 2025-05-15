import { defineStore } from 'pinia';
import { useRouter } from 'vue-router';
import { User } from '@/models';
import { apiClient, API_ENDPOINTS } from '@/services/api.config';

interface AuthState {
    token: string | null;
    refreshToken: string | null;
    user: User | null;
    expiresAt: number | null;
    isAuthenticated: boolean;
}

export const useAuthStore = defineStore('auth', {
    state: (): AuthState => ({
        token: localStorage.getItem('token') || null,
        refreshToken: localStorage.getItem('refresh_token') || null,
        user: JSON.parse(localStorage.getItem('user') || 'null'),
        expiresAt: Number(localStorage.getItem('token_expires_at') || '0'),
        isAuthenticated: false,
    }),

    getters: {
        isLoggedIn(): boolean {
            if (!this.token || !this.expiresAt) return false;
            return this.expiresAt > new Date().getTime();
        },

        currentUser(): User | null {
            return this.user;
        },

        authToken(): string | null {
            return this.token;
        }
    },

    actions: {
        setSession(authResult: { token: string; refreshToken: string; user: User; expiresIn: number }) {
            const expiresAt = new Date().getTime() + authResult.expiresIn * 1000;

            this.token = authResult.token;
            this.refreshToken = authResult.refreshToken;
            this.user = authResult.user;
            this.expiresAt = expiresAt;
            this.isAuthenticated = true;

            // Save to localStorage
            localStorage.setItem('token', authResult.token);
            localStorage.setItem('refresh_token', authResult.refreshToken);
            localStorage.setItem('user', JSON.stringify(authResult.user));
            localStorage.setItem('token_expires_at', expiresAt.toString());
        },

        async login(username: string, password: string) {
            try {
                const response = await apiClient.post(API_ENDPOINTS.auth.login, { username, password });
                this.setSession(response.data);
                return response.data;
            } catch (error: any) {
                console.error('Login failed:', error);
                throw error;
            }
        },

        logout() {
            // Try to call logout API if token exists
            if (this.token) {
                try {
                    apiClient.post(API_ENDPOINTS.auth.logout, { token: this.token }).catch(() => {
                        // Ignore errors during logout
                    });
                } catch (error) {
                    // Ignore errors
                }
            }

            // Clear state
            this.token = null;
            this.refreshToken = null;
            this.user = null;
            this.expiresAt = null;
            this.isAuthenticated = false;

            // Clear localStorage
            localStorage.removeItem('token');
            localStorage.removeItem('refresh_token');
            localStorage.removeItem('user');
            localStorage.removeItem('token_expires_at');

            window.location.href="/login";
        },

        async validateToken() {
            try {
                if (!this.isLoggedIn) {
                    return null;
                }

                if (!this.token) {
                    return null;
                }

                const response = await apiClient.get(API_ENDPOINTS.auth.validate, {
                    headers: {
                        'Authorization': `Bearer ${this.token}`
                    }
                });

                this.user = response.data;
                return response.data;
            } catch (error: any) {
                console.error('Token validation failed:', error);

                if (!error.response || error.response.status === 401) {
                    this.logout();
                }

                return null;
            }
        },

        async refreshToken() {
            try {
                if (!this.refreshToken) {
                    return false;
                }

                const user = await this.validateToken();
                return !!user;
            } catch (error) {
                console.error('Failed to refresh token:', error);
                return false;
            }
        }
    }
});