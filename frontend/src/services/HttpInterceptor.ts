import axios, { AxiosError, AxiosRequestConfig, AxiosResponse } from 'axios';
import AuthService from './AuthService';

// Flag to prevent multiple simultaneous token refresh requests
let isRefreshing = false;
// Queue of requests that were sent during token refresh
let refreshSubscribers: Array<(token: string) => void> = [];

// Function to add request to queue
const subscribeTokenRefresh = (callback: (token: string) => void) => {
    refreshSubscribers.push(callback);
};

// Function to process queue when token is refreshed
const onTokenRefreshed = (newToken: string) => {
    refreshSubscribers.forEach(callback => callback(newToken));
    refreshSubscribers = [];
};

// Function to reject all queued requests
const onRefreshError = (error: any) => {
    refreshSubscribers.forEach(callback => callback(''));
    refreshSubscribers = [];
    return Promise.reject(error);
};

const apiClient = axios.create({
    baseURL: 'http://localhost:8080/api',
    headers: {
        'Content-Type': 'application/json'
    },
    timeout: 15000 // 15 seconds timeout
});

// Add request interceptor
apiClient.interceptors.request.use(
    (config) => {
        const token = AuthService.getToken();
        if (token && config.headers) {
            config.headers['Authorization'] = `Bearer ${token}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

// Add response interceptor
apiClient.interceptors.response.use(
    (response) => {
        return response;
    },
    async (error: AxiosError) => {
        const originalRequest = error.config as AxiosRequestConfig & { _retry?: boolean };

        // If error is 401 Unauthorized and not already retrying
        if (error.response?.status === 401 && !originalRequest._retry) {
            if (isRefreshing) {
                // If already refreshing, add to queue
                return new Promise<string>(resolve => {
                    subscribeTokenRefresh((token: string) => {
                        if (token && originalRequest.headers) {
                            originalRequest.headers['Authorization'] = `Bearer ${token}`;
                        }
                        resolve(axios(originalRequest));
                    });
                });
            }

            // Start refreshing token
            originalRequest._retry = true;
            isRefreshing = true;

            try {
                // Try to refresh token
                const refreshed = await AuthService.refreshToken();

                if (refreshed) {
                    const newToken = AuthService.getToken() || '';

                    // Process queue
                    onTokenRefreshed(newToken);

                    // Update authorization header
                    if (originalRequest.headers) {
                        originalRequest.headers['Authorization'] = `Bearer ${newToken}`;
                    }

                    isRefreshing = false;
                    return axios(originalRequest);
                } else {
                    // If refresh failed
                    isRefreshing = false;
                    onRefreshError(error);
                    AuthService.logout();
                    return Promise.reject(error);
                }
            } catch (refreshError) {
                // If refresh throws an error
                isRefreshing = false;
                onRefreshError(refreshError);
                AuthService.logout();
                return Promise.reject(refreshError);
            }
        }

        // For connection issues, provide better error handling
        if (!error.response) {
            return Promise.reject({
                status: 0,
                message: 'Server connection failed. Please check your internet connection.'
            });
        }

        return Promise.reject(error);
    }
);

export default apiClient;