package com.codegeneration.banking.api.repository;

import com.codegeneration.banking.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by username
     *
     * @param username Username
     * @return Optional of User
     */
    Optional<User> findByUsername(String username);

    /**
     * Find a user by email
     *
     * @param email Email address
     * @return Optional of User
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if a username exists
     *
     * @param username Username
     * @return True if username exists, false otherwise
     */
    boolean existsByUsername(String username);

    /**
     * Check if an email exists
     *
     * @param email Email address
     * @return True if email exists, false otherwise
     */
    boolean existsByEmail(String email);
}