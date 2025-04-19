import axios, { AxiosError, AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios';

/**
 * Error response structure
 */
export interface ErrorResponse {
    status: number;
    message: string;
    details?: any;
    timestamp?: string;
    path?: string;
}

/**
 * RequestService - A universal service for handling all types of API requests
 */
export class RequestService {
    private readonly axiosInstance: AxiosInstance;

    /**
     * Create a new RequestService instance
     * @param baseURL - Base URL for all requests
     * @param timeout - Request timeout in milliseconds
     * @param defaultHeaders - Default headers to apply to all requests
     */
    constructor(
        baseURL: string = 'http://localhost:8080/api',
        timeout: number = 30000,
        defaultHeaders: Record<string, string> = {}
    ) {
        this.axiosInstance = axios.create({
            baseURL,
            timeout,
            headers: {
                'Content-Type': 'application/json',
                ...defaultHeaders
            }
        });

        // Add request interceptor for authentication tokens
        this.axiosInstance.interceptors.request.use(
            (config) => {
                const token = localStorage.getItem('token');
                if (token && config.headers) {
                    config.headers['Authorization'] = `Bearer ${token}`;
                }
                return config;
            },
            (error) => Promise.reject(error)
        );

        // Add response interceptor for handling auth errors
        this.axiosInstance.interceptors.response.use(
            (response) => response,
            (error) => {
                // Handle authentication errors
                if (error.response && error.response.status === 401) {
                    // Clear token and redirect to login
                    localStorage.removeItem('token');
                    window.location.href = '/login';
                }
                return Promise.reject(error);
            }
        );
    }

    /**
     * Processes API error and returns standardized error response
     * @param error - The error from Axios
     * @returns Standardized error response
     */
    private handleError(error: AxiosError): ErrorResponse {
        if (error.response) {
            // The server responded with an error status code
            const { status, data } = error.response;

            // Handle structured error responses
            if (data && typeof data === 'object') {
                return {
                    status,
                    message: data.message || 'An error occurred',
                    details: data.details || data,
                    timestamp: data.timestamp,
                    path: data.path
                };
            }

            // Handle string error responses
            return {
                status,
                message: typeof data === 'string' ? data : 'An error occurred',
            };
        } else if (error.request) {
            // The request was made but no response was received
            return {
                status: 0,
                message: 'No response received from server. Please check your network connection.'
            };
        } else {
            // Something happened in setting up the request
            return {
                status: 0,
                message: error.message || 'An unexpected error occurred'
            };
        }
    }

    /**
     * Makes an HTTP request and handles errors
     * @param config - Axios request configuration
     * @returns Promise with the response data
     */
    private async request<T>(config: AxiosRequestConfig): Promise<T> {
        try {
            const response: AxiosResponse<T> = await this.axiosInstance(config);
            return response.data;
        } catch (error) {
            const errorResponse = this.handleError(error as AxiosError);
            throw errorResponse;
        }
    }

    /**
     * Performs a GET request
     * @param url - Endpoint URL (will be appended to base URL)
     * @param params - Query parameters
     * @param config - Additional Axios config options
     * @returns Promise with the response data
     */
    async get<T>(
        url: string,
        params?: Record<string, any>,
        config?: Omit<AxiosRequestConfig, 'url' | 'method' | 'params'>
    ): Promise<T> {
        return this.request<T>({
            url,
            method: 'GET',
            params,
            ...config
        });
    }

    /**
     * Performs a POST request
     * @param url - Endpoint URL (will be appended to base URL)
     * @param data - Request body data
     * @param config - Additional Axios config options
     * @returns Promise with the response data
     */
    async post<T>(
        url: string,
        data?: any,
        config?: Omit<AxiosRequestConfig, 'url' | 'method' | 'data'>
    ): Promise<T> {
        return this.request<T>({
            url,
            method: 'POST',
            data,
            ...config
        });
    }

    /**
     * Performs a PUT request
     * @param url - Endpoint URL (will be appended to base URL)
     * @param data - Request body data
     * @param config - Additional Axios config options
     * @returns Promise with the response data
     */
    async put<T>(
        url: string,
        data?: any,
        config?: Omit<AxiosRequestConfig, 'url' | 'method' | 'data'>
    ): Promise<T> {
        return this.request<T>({
            url,
            method: 'PUT',
            data,
            ...config
        });
    }

    /**
     * Performs a PATCH request
     * @param url - Endpoint URL (will be appended to base URL)
     * @param data - Request body data
     * @param config - Additional Axios config options
     * @returns Promise with the response data
     */
    async patch<T>(
        url: string,
        data?: any,
        config?: Omit<AxiosRequestConfig, 'url' | 'method' | 'data'>
    ): Promise<T> {
        return this.request<T>({
            url,
            method: 'PATCH',
            data,
            ...config
        });
    }

    /**
     * Performs a DELETE request
     * @param url - Endpoint URL (will be appended to base URL)
     * @param config - Additional Axios config options
     * @returns Promise with the response data
     */
    async delete<T>(
        url: string,
        config?: Omit<AxiosRequestConfig, 'url' | 'method'>
    ): Promise<T> {
        return this.request<T>({
            url,
            method: 'DELETE',
            ...config
        });
    }

    /**
     * Uploads a file or multiple files
     * @param url - Endpoint URL (will be appended to base URL)
     * @param files - File or array of files to upload
     * @param fieldName - Form field name for the file(s)
     * @param additionalData - Additional form data to include
     * @returns Promise with the response data
     */
    async uploadFiles<T>(
        url: string,
        files: File | File[],
        fieldName: string = 'files',
        additionalData?: Record<string, any>
    ): Promise<T> {
        const formData = new FormData();

        // Add file(s) to form data
        if (Array.isArray(files)) {
            files.forEach((file) => {
                formData.append(fieldName, file);
            });
        } else {
            formData.append(fieldName, files);
        }

        // Add additional data to form data
        if (additionalData) {
            Object.entries(additionalData).forEach(([key, value]) => {
                formData.append(key, typeof value === 'object' ? JSON.stringify(value) : value);
            });
        }

        return this.request<T>({
            url,
            method: 'POST',
            data: formData,
            headers: {
                'Content-Type': 'multipart/form-data'
            }
        });
    }

    /**
     * Downloads a file
     * @param url - Endpoint URL (will be appended to base URL)
     * @param params - Query parameters
     * @param filename - Name to save the file as (if not provided, will use from Content-Disposition)
     * @returns Promise that resolves when the download is complete
     */
    async downloadFile(
        url: string,
        params?: Record<string, any>,
        filename?: string
    ): Promise<void> {
        try {
            const response = await this.axiosInstance({
                url,
                method: 'GET',
                params,
                responseType: 'blob'
            });

            // Create a download link
            const blob = new Blob([response.data]);
            const downloadUrl = window.URL.createObjectURL(blob);
            const link = document.createElement('a');
            link.href = downloadUrl;

            // Determine filename
            if (!filename) {
                // Try to get filename from Content-Disposition header
                const contentDisposition = response.headers['content-disposition'];
                if (contentDisposition) {
                    const match = contentDisposition.match(/filename="(.+)"/);
                    if (match) {
                        filename = match[1];
                    }
                }
                // Fallback to a default name
                if (!filename) {
                    filename = 'download';
                }
            }

            link.download = filename;
            document.body.appendChild(link);
            link.click();

            // Clean up
            window.URL.revokeObjectURL(downloadUrl);
            document.body.removeChild(link);
        } catch (error) {
            const errorResponse = this.handleError(error as AxiosError);
            throw errorResponse;
        }
    }

    /**
     * Sets an authentication token for subsequent requests
     * @param token - JWT token
     */
    setAuthToken(token: string): void {
        localStorage.setItem('token', token);
    }

    /**
     * Clears the authentication token
     */
    clearAuthToken(): void {
        localStorage.removeItem('token');
    }

    /**
     * Checks if user is authenticated (has a token)
     * @returns True if authenticated
     */
    isAuthenticated(): boolean {
        return !!localStorage.getItem('token');
    }
}

// Create and export a default instance
const requestService = new RequestService();
export default requestService;