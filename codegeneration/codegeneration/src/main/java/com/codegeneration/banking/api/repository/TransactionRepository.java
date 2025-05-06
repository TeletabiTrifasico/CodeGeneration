package com.codegeneration.banking.api.repository;

import com.codegeneration.banking.api.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}
