package com.example.parkingsystem.service;

import com.example.parkingsystem.model.ParkingLot;

import java.util.List;

public interface ParkingServiceI {

    List<ParkingLot> getAllLots();
    ParkingLot findParkingLotByManagerId(Integer id);
    void deleteById(Integer id);
    void addParkingLot(ParkingLot parkingLot);
    void updateParkingLot(ParkingLot parkingLot);
    List<ParkingLot> getSortedLots(String sortBy, Double userLat, Double userLon);
}
