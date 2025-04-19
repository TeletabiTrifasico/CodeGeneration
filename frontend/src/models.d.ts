interface Transaction {
    id: number;
    date: string;
    description: string;
    amount: number;
}

interface User {
    id: number;
    username: string;
    name: string;
    email: string;
    roles: string[];
}

interface LoginResponse {
    token: string;
    user: User;
    expiresIn: number;
}