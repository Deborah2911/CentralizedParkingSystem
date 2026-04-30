package com.example.parkingsystem.service;

import com.example.parkingsystem.model.ParkingLot;

import java.util.List;

public interface ParkingServiceI {

    ParkingLot findParkingLotByManagerId(Integer id);
    void addParkingLot(ParkingLot parkingLot);
    ParkingLot updateParkingLot(ParkingLot parkingLot);    List<ParkingLot> getSortedLots(String sortBy, Double userLat, Double userLon);
    ParkingLot getParkingLotById(Integer id);
}
