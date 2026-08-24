package com.docusync.repository;

import com.docusync.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * User Repository
 * 
 * Data access layer for User entity with optimized queries
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    
    /**
     * Find user by email (case-insensitive)
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.email) = LOWER(:email)")
    Optional<User> findByEmailIgnoreCase(@Param("email") String email);
    
    /**
     * Find user by username (case-insensitive)
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.username) = LOWER(:username)")
    Optional<User> findByUsernameIgnoreCase(@Param("username") String username);
    
    /**
     * Check if email exists
     */
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE LOWER(u.email) = LOWER(:email)")
    boolean existsByEmailIgnoreCase(@Param("email") String email);
    
    /**
     * Check if username exists
     */
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE LOWER(u.username) = LOWER(:username)")
    boolean existsByUsernameIgnoreCase(@Param("username") String username);
    
    /**
     * Find active users with pagination support
     */
    @Query("SELECT u FROM User u WHERE u.isActive = true ORDER BY u.createdAt DESC")
    List<User> findActiveUsers();
    
    /**
     * Update last login timestamp
     */
    @Modifying
    @Query("UPDATE User u SET u.lastLoginAt = :lastLoginAt WHERE u.id = :userId")
    void updateLastLoginAt(
            @Param("userId") UUID userId, 
            @Param("lastLoginAt") LocalDateTime lastLoginAt);
    
    /**
     * Search users by name or email (for collaboration invites)
     */
    @Query("""
           SELECT u FROM User u 
           WHERE u.isActive = true 
           AND (
               LOWER(u.fullName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) 
               OR LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
               OR LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
           )
           ORDER BY u.fullName
           """)
    List<User> searchUsers(@Param("searchTerm") String searchTerm);
}