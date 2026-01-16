package com.example.parkingsystem.service;

import com.example.parkingsystem.model.ParkingLot;
import com.example.parkingsystem.util.DistanceCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.parkingsystem.repository.ParkingRepoI;
import java.util. Comparator;

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

    @Override
    public List<ParkingLot> getSortedLots(String sortBy, Double userLat, Double userLon) {
        List<ParkingLot> lots = parkingRepo.findAll();

        switch (sortBy) {
            case "name":
                lots.sort(Comparator.comparing(ParkingLot::getName));
                break;
            case "availability":
                lots.sort(Comparator.comparing(ParkingLot::getFreeSpots).reversed());
                break;
            case "pricing":
                lots.sort(Comparator.comparing(ParkingLot::getPrice));
                break;
            case "nearest":
                if (userLat != null && userLon != null) {
                    DistanceCalculator calculator = DistanceCalculator.getInstance();

                    for (ParkingLot lot : lots) {
                        if (lot.getLatitude() != null && lot.getLongitude() != null) {
                            double dist = calculator.calculateDistance(userLat, userLon,
                                    lot.getLatitude(), lot.getLongitude());
                            lot.setDistance(dist);
                        }
                    }
                    lots.sort(Comparator.comparingDouble(lot ->
                            lot.getDistance() != null ? lot.getDistance() : Double.MAX_VALUE
                    ));
                } else {
                    lots.sort(Comparator.comparing(ParkingLot::getName));
                }
                break;
            default:
                lots. sort(Comparator.comparing(ParkingLot::getName));
                break;
        }

        return lots;
    }
}