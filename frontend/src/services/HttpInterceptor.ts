import axios from 'axios';
import AuthService from './AuthService';

const apiClient = axios.create({
    baseURL: 'http://localhost:8080/api',
    headers: {
        'Content-Type': 'application/json'
    }
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
    async (error) => {
        // Handle 401 Unauthorized errors (token expired)
        if (error.response && error.response.status === 401) {
            AuthService.logout();
            window.location.href = '/login';
        }
        return Promise.reject(error);
    }
);

export default apiClient;