package com.codegeneration.banking.api.repository;

import com.codegeneration.banking.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by username
     *
     * @param username The username to search for
     * @return Optional containing the user if found
     */
    Optional<User> findByUsername(String username);


    /**
     * Find a user by email
     *
     * @param email The email to search for
     * @return Optional containing the user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if a username already exists
     *
     * @param username The username to check
     * @return True if exists, false otherwise
     */
    boolean existsByUsername(String username);

    /**
     * Check if an email already exists
     *
     * @param email The email to check
     * @return True if exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Find users by username (partial match, case insensitive)
     *
     * @param username The username to search for
     * @return List of users matching the search criteria
     */
    List<User> findByUsernameContainingIgnoreCase(String username);

    /**
     * Find users by name (partial match, case insensitive)
     *
     * @param name The name to search for
     * @return List of users matching the search criteria
     */
    List<User> findByNameContainingIgnoreCase(String name);

    /**
     * Find users by name (partial match, case insensitive)
     *
     * @param id The id to search for
     * @return User matching specified id
     */
    List<User> findById(Number id);
}