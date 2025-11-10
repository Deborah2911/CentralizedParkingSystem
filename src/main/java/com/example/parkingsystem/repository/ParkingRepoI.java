package com.example.parkingsystem.repository;

import com.example.parkingsystem.model.ParkingLot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParkingRepoI extends JpaRepository<ParkingLot, Integer> {
    ParkingLot findByManagerId(Integer id);
}
