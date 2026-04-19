package com.example.parkingsystem.repository;

import com.example.parkingsystem.model.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.time.LocalDate;

@Repository
public interface BillRepoI extends JpaRepository<Bill, Integer> {
    List<Bill> findByUserId(Integer userId);

    List<Bill> findByParkingLotId(Integer parkingLotId);


    @Query("SELECT b FROM Bill b WHERE b.parkingLotId = ?1 AND YEAR(b.dateIssued) = ?2")
    List<Bill> findByParkingLotIdAndYear(Integer parkingLotId, Integer year);

    @Query("SELECT b FROM Bill b WHERE b.parkingLotId = ?1 AND CAST(b.dateIssued AS DATE) = ?2")
    List<Bill> findByParkingLotIdAndDate(Integer parkingLotId, LocalDate date);
}
