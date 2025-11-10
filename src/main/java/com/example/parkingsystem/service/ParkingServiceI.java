package com.example.parkingsystem.service;

import com.example.parkingsystem.model.ParkingLot;

import java.util.List;

public interface ParkingServiceI {

    List<ParkingLot> getAllLots();
    ParkingLot findParkingLotByManagerId(Integer id);
    void deleteById(Integer id);
    void addParkingLot(ParkingLot parkingLot);
    void updateParkingLot(ParkingLot parkingLot);
//    List<ParkingLot> getByName(String name);
//    List<ParkingLot> getByPrice(double price);
}
