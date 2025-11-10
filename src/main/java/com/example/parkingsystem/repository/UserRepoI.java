package com.example.parkingsystem.repository;

import com.example.parkingsystem.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepoI extends JpaRepository<User, Integer> {
    User findByUsername(String username);
}
