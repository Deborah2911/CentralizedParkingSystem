package com.example.parkingsystem.repository;

import com.example.parkingsystem.model.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface BillRepoI extends JpaRepository<Bill, Integer> {
    List<Bill> findByUserId(Integer userId);
}
