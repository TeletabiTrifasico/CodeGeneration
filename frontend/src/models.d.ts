// Type definitions for the application's data models

// User model
export interface User {
    id: number;
    username: string;
    name: string;
    email: string;
    role: UserRole;
    enabled: boolean;
    accounts: Account[];
}

export interface Account {
    id: number;
    accountNumber: string;
    accountName: string;
    accountType: string;
    balance: number;
    currency: string;
    dailyTransferLimit: number;
    dailyWithdrawalLimit: number;
    singleTransferLimit: number;
    singleWithdrawalLimit: number;
    transferUsedToday: number;
    withdrawalUsedToday: number;
    lastLimitResetDate: Date;
    user: User;
    createdAt: Date;
    updatedAt: Date;
}

export interface Transaction {
    id: number;
    transactionReference: string;
    sourceAccount: Account;
    destinationAccount: Account;
    amount: number;
    currency: Currency;
    description: string;
    transactionStatus: TransactionStatus;
    transactionType: TransactionType;
    createAt: Date;
    completedAt: Date | null;
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