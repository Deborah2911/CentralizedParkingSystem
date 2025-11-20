package com.example.parkingsystem.service;

import com.example.parkingsystem.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.parkingsystem.repository.UserRepoI;

@Service
public class UserServiceImpl implements UserServiceI {

    @Autowired
    private UserRepoI userRepo;

    @Override
    public boolean usernameTaken(String username) {
        return userRepo.findByUsername(username) == null;
    }

    @Override
    public void createUser(User user) {
        userRepo.save(user);
    }

    public User getUser(String username) {
        return userRepo.findByUsername(username);
    }
}
