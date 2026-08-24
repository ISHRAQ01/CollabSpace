package com.docusync.repository;

import com.docusync.config.BaseIntegrationTest;
import com.docusync.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * User Repository Integration Test
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = "/db/test-data.sql")
class UserRepositoryIntegrationTest extends BaseIntegrationTest {
    
    @Autowired
    private UserRepository userRepository;
    
    @Test
    @DisplayName("Should find user by email (case insensitive)")
    void shouldFindUserByEmailIgnoreCase() {
        // Given
        String email = "TEST1@DOCUSYNC.IO";
        
        // When
        Optional<User> user = userRepository.findByEmailIgnoreCase(email);
        
        // Then
        assertThat(user).isPresent();
        assertThat(user.get().getEmail()).isEqualTo("test1@docusync.io");
        assertThat(user.get().getUsername()).isEqualTo("testuser1");
    }
    
    @Test
    @DisplayName("Should find user by username")
    void shouldFindUserByUsername() {
        // Given
        String username = "testuser2";
        
        // When
        Optional<User> user = userRepository.findByUsernameIgnoreCase(username);
        
        // Then
        assertThat(user).isPresent();
        assertThat(user.get().getEmail()).isEqualTo("test2@docusync.io");
    }
    
    @Test
    @DisplayName("Should return empty for non-existent user")
    void shouldReturnEmptyForNonExistentUser() {
        // When
        Optional<User> user = userRepository.findByEmailIgnoreCase("nonexistent@test.com");
        
        // Then
        assertThat(user).isEmpty();
    }
    
    @Test
    @DisplayName("Should check if email exists")
    void shouldCheckEmailExists() {
        // When
        boolean exists = userRepository.existsByEmailIgnoreCase("test1@docusync.io");
        boolean notExists = userRepository.existsByEmailIgnoreCase("nonexistent@test.com");
        
        // Then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }
    
    @Test
    @DisplayName("Should find active users")
    void shouldFindActiveUsers() {
        // When
        var users = userRepository.findActiveUsers();
        
        // Then
        assertThat(users).isNotEmpty();
        assertThat(users).allMatch(User::getIsActive);
    }
    
    @Test
    @DisplayName("Should search users by name or email")
    void shouldSearchUsers() {
        // When
        var users = userRepository.searchUsers("test");
        
        // Then
        assertThat(users).isNotEmpty();
        assertThat(users).allMatch(user -> 
            user.getFullName().contains("Test") || 
            user.getEmail().contains("test") ||
            user.getUsername().contains("test")
        );
    }
}