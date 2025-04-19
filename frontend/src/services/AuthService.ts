import requestService, { ErrorResponse } from './RequestService';

class AuthService {
    private AUTH_API_PATH = '/auth';

    async login(username: string, password: string): Promise<LoginResponse> {
        try {
            const response = await requestService.post<LoginResponse>(
                `${this.AUTH_API_PATH}/login`,
                { username, password }
            );

            // Store token and user data
            requestService.setAuthToken(response.token);

            if (response.user) {
                localStorage.setItem('user', JSON.stringify(response.user));
            }

            return response;
        } catch (error) {
            throw error as ErrorResponse;
        }
    }

    /**
     * Logout method
     */
    logout(): void {
        // Clear token via RequestService
        requestService.clearAuthToken();
        localStorage.removeItem('user');

        // Add logout api call!
    }

    getCurrentUser(): User | null {
        const userStr = localStorage.getItem('user');
        if (userStr) {
            return JSON.parse(userStr);
        }
        return null;
    }

    isLoggedIn(): boolean {
        return requestService.isAuthenticated();
    }

    getToken(): string | null {
        return localStorage.getItem('token');
    }

    async validateToken(): Promise<User | null> {
        try {
            if (!this.isLoggedIn()) {
                return null;
            }

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