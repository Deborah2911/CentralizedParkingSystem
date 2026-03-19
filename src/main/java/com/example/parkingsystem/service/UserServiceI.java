package com.example.parkingsystem.service;

import com.example.parkingsystem.model.User;

public interface UserServiceI {
    boolean usernameTaken(String username);
    void createUser(User user);
    float getMoneySpent(String username);
    User getUser(String username);
}
