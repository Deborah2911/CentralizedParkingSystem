package com.example.parkingsystem.service;

import com.example.parkingsystem.model.ParkingLot;
import com.example.parkingsystem.util.DistanceCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.parkingsystem.repository.ParkingRepoI;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util. Comparator;

import java.util.List;

@Service
public class ParkingServiceImpl implements  ParkingServiceI {

    @Autowired
    private ParkingRepoI parkingRepo;

    @Autowired
    private DistanceCalculator distanceCalculator;

    @Override
    public ParkingLot findParkingLotByManagerId(Integer id){
        return parkingRepo.findByManagerId(id);
    }

    @Override
    public void addParkingLot(ParkingLot parkingLot){
        parkingRepo.save(parkingLot);
    }

    @Override
    @Transactional
    public ParkingLot updateParkingLot(ParkingLot parkingLot){
        System.out.println("ParkingServiceImpl.updateParkingLot() called with: " + parkingLot.getName());
        ParkingLot saved = parkingRepo.save(parkingLot);
        System.out.println("ParkingServiceImpl.updateParkingLot() - saved to DB");
        return saved;
    }
    @Override
    public ParkingLot getParkingLotById(Integer id) {
        return parkingRepo.findById(id).orElse(null);
    }

    @Override
    public List<ParkingLot> getSortedLots(String sortBy, Double userLat, Double userLon) {
        List<ParkingLot> lots = parkingRepo.findAll();

        List<ParkingLot> availableLots = new ArrayList<>();      // freeSpots > 0
        List<ParkingLot> fullLots = new ArrayList<>();            // freeSpots == 0

        for (ParkingLot lot : lots) {
            if (lot.getFreeSpots() > 0) {
                availableLots.add(lot);
            } else {
                fullLots.add(lot);
            }
        }

        // Sort only the available lots based on the sort criteria
        switch (sortBy) {
            case "name":
                availableLots.sort(Comparator.comparing(ParkingLot::getName));
                break;
            case "availability":
                availableLots.sort(Comparator.comparing(ParkingLot::getFreeSpots).reversed());
                break;
            case "pricing":
                availableLots.sort(Comparator.comparing(ParkingLot::getPrice));
                break;
            case "nearest":
                if (userLat != null && userLon != null) {
                    distanceCalculator.calculateAndSetDistances(userLat, userLon, availableLots);
                    availableLots.sort(Comparator.comparingDouble(lot ->
                            lot.getDistance() != null ? lot.getDistance() : Double.MAX_VALUE
                    ));
                } else {
                    availableLots.sort(Comparator.comparing(ParkingLot::getName));
                }
                break;
            default:
                availableLots.sort(Comparator.comparing(ParkingLot::getName));
                break;
        }

        // Combine: available lots first, then full lots at the end
        availableLots.addAll(fullLots);

        return availableLots;
    }
}