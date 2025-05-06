import requestService, { ErrorResponse } from './RequestService';
import { Router } from 'vue-router';

// Define types for better type safety
export interface User {
    id: number;
    username: string;
    name: string;
    email: string;
}

export interface LoginResponse {
    token: string;
    refreshToken: string;
    user: User;
    expiresIn: number;
}

class AuthService {
    private AUTH_API_PATH = '/auth';
    private tokenExpirationTimer: number | null = null;
    private router: Router | null = null;

    // Method to initialize the router (call this in app initialization)
    setRouter(router: Router): void {
        this.router = router;
    }

    async login(username: string, password: string): Promise<LoginResponse> {
        try {
            const response = await requestService.post<LoginResponse>(
                `${this.AUTH_API_PATH}/login`,
                { username, password }
            );

            // Store token and user data
            this.setSession(response);

            return response;
        } catch (error) {
            throw error as ErrorResponse;
        }
    }

    /**
     * Set authentication session and schedule token refresh
     */
    private setSession(authResult: LoginResponse): void {
        // Calculate token expiration time
        const expiresAt = new Date().getTime() + authResult.expiresIn * 1000;

        // Store auth data
        localStorage.setItem('token', authResult.token);
        localStorage.setItem('refresh_token', authResult.refreshToken);
        localStorage.setItem('token_expires_at', expiresAt.toString());
        localStorage.setItem('user', JSON.stringify(authResult.user));

        // Schedule token refresh
        this.scheduleTokenRefresh();
    }

    /**
     * Schedule token refresh before it expires
     */
    private scheduleTokenRefresh(): void {
        // Clear any existing timer
        if (this.tokenExpirationTimer !== null) {
            window.clearTimeout(this.tokenExpirationTimer);
        }

        const expiresAt = Number(localStorage.getItem('token_expires_at') || '0');
        const now = new Date().getTime();

        if (expiresAt > now) {
            // Schedule refresh 1 minute before token expires
            const timeUntilRefresh = Math.max(0, expiresAt - now - 60000);

            this.tokenExpirationTimer = window.setTimeout(() => {
                this.refreshToken().catch(() => {
                    // If refresh fails, redirect to login
                    this.logout();
                });
            }, timeUntilRefresh);
        }
    }

    /**
     * Refresh the access token using the refresh token
     */
    async refreshToken(): Promise<boolean> {
        try {
            const refreshToken = localStorage.getItem('refresh_token');

            if (!refreshToken) {
                return false;
            }

            const response = await requestService.post<LoginResponse>(
                `${this.AUTH_API_PATH}/refresh-token`,
                { refreshToken }
            );

            this.setSession(response);
            return true;
        } catch (error) {
            console.error('Token refresh failed:', error);
            return false;
        }
    }

    /**
     * Logout method
     */
    logout(): void {
        // Clear token timer
        if (this.tokenExpirationTimer !== null) {
            window.clearTimeout(this.tokenExpirationTimer);
            this.tokenExpirationTimer = null;
        }

        // Clear auth data
        localStorage.removeItem('token');
        localStorage.removeItem('refresh_token');
        localStorage.removeItem('token_expires_at');
        localStorage.removeItem('user');

        // Clear token via RequestService
        requestService.clearAuthToken();

        // Redirect to login page
        if (this.router) {
            this.router.push('/login');
        } else {
            window.location.href = '/login';
        }

        // Add logout api call if needed
        try {
            requestService.post(`${this.AUTH_API_PATH}/logout`).catch(() => {
                // Ignore errors during logout API call
            });
        } catch (error) {
            // Ignore errors
        }
    }

    getCurrentUser(): User | null {
        const userStr = localStorage.getItem('user');
        if (userStr) {
            try {
                return JSON.parse(userStr);
            } catch (e) {
                return null;
            }
        }
        return null;
    }

    isLoggedIn(): boolean {
        const token = localStorage.getItem('token');
        const expiresAt = Number(localStorage.getItem('token_expires_at') || '0');
        const now = new Date().getTime();

        // Only consider logged in if token exists and is not expired
        return !!token && expiresAt > now;
    }

    getToken(): string | null {
        return localStorage.getItem('token');
    }

    async validateToken(): Promise<User | null> {
        try {
            if (!this.isLoggedIn()) {
                return null;
            }

            // Check token expiration first
            const expiresAt = Number(localStorage.getItem('token_expires_at') || '0');
            const now = new Date().getTime();

            // If token is about to expire, try to refresh it
            if (expiresAt - now < 60000) {
                const refreshed = await this.refreshToken();
                if (!refreshed) {
                    this.logout();
                    return null;
                }
            }

            // Validate token with server
            const userData = await requestService.get<User>(`${this.AUTH_API_PATH}/validate`);
            return userData;
        } catch (error) {
            // If token validation fails, clear token
            this.logout();
            return null;
        }
    }
}

export default new AuthService();