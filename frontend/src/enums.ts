
enum UserRole {
    ROLE_CLIENT = "Client",
    ROLE_EMPLOYEE = "Employee",
}

enum Currency {
    EUR,
    USD,
    GBP,
    CHF,
    PLN
}

enum TransactionStatus {
    PENDING,
    COMPLETED,
    FAILED,
    CANCELLED
}

enum TransactionType {
    TRANSFER,
    DEPOSIT,
    WITHDRAWAL,
    PAYMENT
}