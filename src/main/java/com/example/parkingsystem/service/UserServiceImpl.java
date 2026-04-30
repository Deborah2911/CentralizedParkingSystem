package com.example.parkingsystem.service;

import com.example.parkingsystem.model.User;
import com.example.parkingsystem.repository.UserRepoI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * UserServiceImpl handles all user-related business logic.
 *
 * IMPORTANT: This implementation uses findByUsername() from the repository.
 * Make sure UserRepoI has the findByUsername() method defined.
 */
@Service
public class UserServiceImpl implements UserServiceI {

    @Autowired
    private UserRepoI userRepository;

    /**
     * Creates a new user and persists to database.
     */
    @Override
    public void createUser(User user) {
        userRepository.save(user);
    }

    /**
     * Checks if a username is already taken (exists in database).
     *
     * ✅ CORRECT LOGIC:
     * - Returns TRUE if user EXISTS (username IS taken)
     * - Returns FALSE if user is NULL (username is available)
     */
    @Override
    public boolean usernameTaken(String username) {
        User user = userRepository.findByUsername(username);
        return user != null;  // ✅ TRUE when user exists
    }

    /**
     * Retrieves total money spent by a user.
     *
     * ✅ USES findByUsername() method (NOT getMoneySpentByUsername())
     */
    @Override
    public float getMoneySpent(String username) {
        User user = userRepository.findByUsername(username);
        if (user != null) {
            return user.getMoneySpent();
        }
        return 0.0f;
    }

    /**
     * Retrieves user by username.
     */
    @Override
    public User getUser(String username) {
        return userRepository.findByUsername(username);
    }
}