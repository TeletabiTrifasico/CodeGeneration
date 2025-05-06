// Type definitions for the application's data models

// User model
export interface User {
    id: number;
    username: string;
    name: string;
    email: string;
}

// Transaction model
export interface Transaction {
    id: number;
    date: string;
    description: string;
    amount: number;
    category?: string;
    reference?: string;
}

// Account model
export interface Account {
    id: number;
    accountNumber: string;
    type: string;
    balance: number;
    currency: string;
}

// Authentication response
export interface LoginResponse {
    token: string;
    refreshToken: string;
    user: User;
    expiresIn: number;
}

// Error response
export interface ApiError {
    status: number;
    message: string;
    details?: any;
}

export default Transaction;