package com.example.parkingsystem.repository;

import com.example.parkingsystem.model.Bill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BillRepoI extends JpaRepository<Bill, Integer> {
    List<Bill> findByUserId(Integer userId);
}
