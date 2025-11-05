package service;

import model.ParkingLot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repository.ParkingRepoI;

import java.util.List;

@Service
public class ParkingServiceImpl implements  ParkingServiceI {

    @Autowired
    private ParkingRepoI parkingRepo;

    @Override
    public List<ParkingLot> getAllLots() {
        return parkingRepo.findAll();
    }
}
