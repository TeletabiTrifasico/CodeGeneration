import requestService from './RequestService';
import { Router } from 'vue-router';
import { User } from '@/models';

export interface LoginResponse {
    token: string;
    refreshToken: string;
    user: User;
    expiresIn: number;
}

class AuthService {
    private tokenKey: string = 'token';
    private userKey: string = 'user';
    private refreshTokenKey: string = 'refresh_token';
    private expiresAtKey: string = 'token_expires_at';
    private router: Router | null = null;

    // Set the router for navigation
    setRouter(router: Router): void {
        this.router = router;
    }

    // Login method
    async login(username: string, password: string): Promise<LoginResponse> {
        try {
            // Send login request
            const response = await requestService.post('/auth/login', { username, password });

            // Store authentication data
            this.setSession(response);

            return response;
        } catch (error: any) {
            console.error('Login failed:', error);
            throw error;
        }
    }

    // Set the session data
    private setSession(authResult: LoginResponse): void {
        const expiresAt = new Date().getTime() + authResult.expiresIn * 1000;

        localStorage.setItem(this.tokenKey, authResult.token);
        localStorage.setItem(this.refreshTokenKey, authResult.refreshToken);
        localStorage.setItem(this.expiresAtKey, expiresAt.toString());
        localStorage.setItem(this.userKey, JSON.stringify(authResult.user));
    }

    // Logout method
    logout(): void {
        // Try to call logout API
        const token = this.getToken();
        if (token) {
            try {
                requestService.post('/auth/logout', { token }).catch(() => {
                    // Ignore errors during logout
                });
            } catch (error) {
                // Ignore errors
            }
        }

        // Clear token data
        localStorage.removeItem(this.tokenKey);
        localStorage.removeItem(this.refreshTokenKey);
        localStorage.removeItem(this.expiresAtKey);
        localStorage.removeItem(this.userKey);

        // Redirect to login page
        if (this.router) {
            this.router.push('/login');
        } else {
            window.location.href = '/login';
        }
    }

    // Get the current user
    getCurrentUser(): User | null {
        try {
            const userStr = localStorage.getItem(this.userKey);
            return userStr ? JSON.parse(userStr) : null;
        } catch (error) {
            return null;
        }
    }

    // Check if user is logged in
    isLoggedIn(): boolean {
        const token = localStorage.getItem(this.tokenKey);
        const expiresAt = Number(localStorage.getItem(this.expiresAtKey) || '0');
        const now = new Date().getTime();

        return !!token && expiresAt > now;
    }

    // Get the auth token
    getToken(): string | null {
        return localStorage.getItem(this.tokenKey);
    }

    // Validate the token with the server
    async validateToken(): Promise<User | null> {
        try {
            if (!this.isLoggedIn()) {
                return null;
            }

            const token = this.getToken();
            if (!token) {
                return null;
            }

            const response = await requestService.post('/auth/validate', { token });
            return response;
        } catch (error: any) {
            console.error('Token validation failed:', error);

            if (!error.response || error.response.status !== 401) {
                this.logout();
            }

            return null;
        }
    }
}

export default new AuthService();