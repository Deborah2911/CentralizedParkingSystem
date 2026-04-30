package com.example.parkingsystem.service;

import com.example.parkingsystem.model.User;
import com.example.parkingsystem.repository.UserRepoI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("UserServiceImpl Unit Tests")
class UserServiceImplTest {

    @Mock
    private UserRepoI userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");
        testUser.setPassword("password123");
        testUser.setRole(1);
        testUser.setMoneySpent(0);
    }

    @Test
    @DisplayName("Test 1: Should create new user successfully")
    void testCreateUserSuccess() {
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.createUser(testUser);

        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    @DisplayName("Test 2: Should return true when username is not taken")
    void testUsernameNotTaken() {
        when(userRepository.findByUsername("newuser")).thenReturn(null);

        boolean result = userService.usernameTaken("newuser");

        assertFalse(result);
        verify(userRepository, times(1)).findByUsername("newuser");
    }

    @Test
    @DisplayName("Test 3: Should return false when username is already taken")
    void testUsernameAlreadyTaken() {
        when(userRepository.findByUsername("testuser")).thenReturn(testUser);

        boolean result = userService.usernameTaken("testuser");

        assertTrue(result);
        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    @DisplayName("Test 4: Should retrieve user by username")
    void testGetUserByUsername() {
        when(userRepository.findByUsername("testuser")).thenReturn(testUser);

        User retrievedUser = userService.getUser("testuser");

        assertNotNull(retrievedUser);
        assertEquals("testuser", retrievedUser.getUsername());
        assertEquals("password123", retrievedUser.getPassword());
        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    @DisplayName("Test 5: Should return correct money spent by user")
    void testGetMoneySpentByUser() {
        testUser.setMoneySpent(500.50f);
        when(userRepository.findByUsername("testuser")).thenReturn(testUser);

        float moneySpent = userService.getMoneySpent("testuser");

        assertEquals(500.50f, moneySpent);
        verify(userRepository, times(1)).findByUsername("testuser");
    }
}