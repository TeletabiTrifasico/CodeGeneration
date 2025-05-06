import axios from 'axios';

class RequestService {
    // Use a simple base URL without hostname
    // The Vite proxy will handle the routing
    private readonly baseURL: string = '/api';

    async get(endpoint: string, params = {}) {
        try {
            // Get token from localStorage
            const token = localStorage.getItem('token');

            // Set headers with Authorization if token exists
            const headers: Record<string, string> = {
                'Content-Type': 'application/json'
            };

            if (token) {
                headers['Authorization'] = `Bearer ${token}`;
            }

            const response = await axios.get(`${this.baseURL}${endpoint}`, {
                params,
                headers
            });
            return response.data;
        } catch (error: any) {
            this.handleError(error);
            throw error;
        }
    }

    async post(endpoint: string, data = {}) {
        try {
            const token = localStorage.getItem('token');

            const headers: Record<string, string> = {
                'Content-Type': 'application/json'
            };

            if (token && !endpoint.includes('/auth/login') && !endpoint.includes('/auth/register')) {
                headers['Authorization'] = `Bearer ${token}`;
            }

            const response = await axios.post(`${this.baseURL}${endpoint}`, data, {
                headers
            });
            return response.data;
        } catch (error: any) {
            this.handleError(error);
            throw error;
        }
    }

    async put(endpoint: string, data = {}) {
        try {
            const token = localStorage.getItem('token');

            const headers: Record<string, string> = {
                'Content-Type': 'application/json'
            };

            if (token) {
                headers['Authorization'] = `Bearer ${token}`;
            }

            const response = await axios.put(`${this.baseURL}${endpoint}`, data, {
                headers
            });
            return response.data;
        } catch (error: any) {
            this.handleError(error);
            throw error;
        }
    }

    async delete(endpoint: string) {
        try {
            const token = localStorage.getItem('token');

            const headers: Record<string, string> = {
                'Content-Type': 'application/json'
            };

            if (token) {
                headers['Authorization'] = `Bearer ${token}`;
            }

            const response = await axios.delete(`${this.baseURL}${endpoint}`, {
                headers
            });
            return response.data;
        } catch (error: any) {
            this.handleError(error);
            throw error;
        }
    }

    private handleError(error: any) {
        console.error('API Error:', error);

        if (error.response) {
            console.error('Response data:', error.response.data);
            console.error('Response status:', error.response.status);

            // If token is invalid, log out the user
            if (error.response.status === 401) {
                console.warn('Authentication error detected');

                if (!window.location.pathname.includes('/login')) {
                    localStorage.removeItem('token');
                    localStorage.removeItem('user');
                    localStorage.removeItem('refresh_token');
                    localStorage.removeItem('token_expires_at');
                    window.location.href = '/login';
                }
            }
        } else if (error.request) {
            console.error('Request made but no response received:', error.request);
        } else {
            console.error('Error setting up request:', error.message);
        }
    }
}

export default new RequestService();