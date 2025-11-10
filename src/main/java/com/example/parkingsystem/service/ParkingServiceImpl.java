package com.example.parkingsystem.service;

import com.example.parkingsystem.model.ParkingLot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.parkingsystem.repository.ParkingRepoI;

import java.util.List;

@Service
public class ParkingServiceImpl implements  ParkingServiceI {

    @Autowired
    private ParkingRepoI parkingRepo;

    @Override
    public List<ParkingLot> getAllLots() {
        return parkingRepo.findAll();
    }

    @Override
    public void deleteById(Integer id){
        parkingRepo.deleteById(id);
    }

    @Override
    public ParkingLot findParkingLotByManagerId(Integer id){
        return parkingRepo.findByManagerId(id);
    }

    @Override
    public void addParkingLot(ParkingLot parkingLot){
        parkingRepo.save(parkingLot);
    }

    @Override
    public void updateParkingLot(ParkingLot parkingLot){
        parkingRepo.save(parkingLot);
    }

}
