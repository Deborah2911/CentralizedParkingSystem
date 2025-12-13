package com.example.parkingsystem.service;

import com.example.parkingsystem.model.ParkingLot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.parkingsystem.repository.ParkingRepoI;
import java.util.Comparator;

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
                lots. sort(Comparator.comparing(ParkingLot::getName));
                break;
            case "availability":
                lots.sort(Comparator.comparing(ParkingLot::getFreeSpots).reversed());
                break;
            case "pricing":
                lots. sort(Comparator.comparing(ParkingLot::getPrice));
                break;
            case "nearest":
                if (userLat != null && userLon != null) {
                    // Calculate and SET distance for each lot
                    for (ParkingLot lot : lots) {
                        if (lot.getLatitude() != null && lot.getLongitude() != null) {
                            double dist = calculateDistance(userLat, userLon,
                                    lot.getLatitude(), lot.getLongitude());
                            lot.setDistance(dist);  // THIS IS CRUCIAL!
                        }
                    }
                    lots.sort(Comparator. comparingDouble(lot ->
                            lot.getDistance() != null ? lot.getDistance() : Double.MAX_VALUE
                    ));
                } else {
                    lots.sort(Comparator.comparing(ParkingLot::getName));
                }
                break;
            default:
                lots.sort(Comparator.comparing(ParkingLot::getName));
                break;
        }

        return lots;
    }

    /**
     * Calculate distance between two points using Haversine formula
     * @return distance in kilometers
     */
    private double calculateDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
        if (lat2 == null || lon2 == null) {
            return Double.MAX_VALUE; // Put parking lots without coordinates at the end
        }

        final int EARTH_RADIUS = 6371; // Radius in kilometers

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }
}
