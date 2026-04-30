package com.example.parkingsystem.service;

import com.example.parkingsystem.model.ParkingLot;
import com.example.parkingsystem.repository.ParkingRepoI;
import com.example.parkingsystem.util.DistanceCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("ParkingServiceImpl Unit Tests")
class ParkingServiceImplTest {

    @Mock
    private ParkingRepoI parkingRepository;

    @Mock
    private DistanceCalculator distanceCalculator;

    @InjectMocks
    private ParkingServiceImpl parkingService;

    private ParkingLot parkingLot1;
    private ParkingLot parkingLot2;
    private ParkingLot parkingLot3;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        parkingLot1 = new ParkingLot();
        parkingLot1.setId(1);
        parkingLot1.setName("Downtown Parking");
        parkingLot1.setFreeSpots(10);
        parkingLot1.setPrice(5.00);
        parkingLot1.setManagerId(1);

        parkingLot2 = new ParkingLot();
        parkingLot2.setId(2);
        parkingLot2.setName("Airport Parking");
        parkingLot2.setFreeSpots(25);
        parkingLot2.setPrice(3.50);
        parkingLot2.setManagerId(2);

        parkingLot3 = new ParkingLot();
        parkingLot3.setId(3);
        parkingLot3.setName("Mall Parking");
        parkingLot3.setFreeSpots(0);
        parkingLot3.setPrice(2.00);
        parkingLot3.setManagerId(3);
    }

    @Test
    @DisplayName("Test 1: Should find parking lot by manager ID")
    void testFindParkingLotByManagerId() {
        when(parkingRepository.findByManagerId(1)).thenReturn(parkingLot1);

        ParkingLot result = parkingService.findParkingLotByManagerId(1);

        assertNotNull(result);
        assertEquals("Downtown Parking", result.getName());
        assertEquals(1, result.getManagerId());
        verify(parkingRepository, times(1)).findByManagerId(1);
    }

    @Test
    @DisplayName("Test 2: Should add parking lot successfully")
    void testAddParkingLot() {
        when(parkingRepository.save(any(ParkingLot.class))).thenReturn(parkingLot1);

        parkingService.addParkingLot(parkingLot1);

        verify(parkingRepository, times(1)).save(parkingLot1);
    }

    @Test
    @DisplayName("Test 3: Should update parking lot successfully")
    void testUpdateParkingLot() {
        parkingLot1.setFreeSpots(5);
        when(parkingRepository.save(parkingLot1)).thenReturn(parkingLot1);

        ParkingLot updated = parkingService.updateParkingLot(parkingLot1);

        assertNotNull(updated);
        assertEquals(5, updated.getFreeSpots());
        verify(parkingRepository, times(1)).save(parkingLot1);
    }

    @Test
    @DisplayName("Test 4: Should retrieve parking lot by ID")
    void testGetParkingLotById() {
        when(parkingRepository.findById(1)).thenReturn(Optional.of(parkingLot1));

        ParkingLot result = parkingService.getParkingLotById(1);

        assertNotNull(result);
        assertEquals("Downtown Parking", result.getName());
        verify(parkingRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("Test 5: Should sort parking lots by availability (available lots first)")
    void testGetSortedLotsByAvailability() {
        List<ParkingLot> allLots = Arrays.asList(parkingLot1, parkingLot2, parkingLot3);
        when(parkingRepository.findAll()).thenReturn(allLots);

        List<ParkingLot> sorted = parkingService.getSortedLots("availability", null, null);

        assertNotNull(sorted);
        assertTrue(sorted.get(0).getFreeSpots() > 0, "First lot should have free spots");
        assertEquals(0, sorted.get(sorted.size() - 1).getFreeSpots(), "Last lot should be full");
        verify(parkingRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Test 5b: Should sort parking lots by price")
    void testGetSortedLotsByPrice() {
        List<ParkingLot> allLots = Arrays.asList(parkingLot1, parkingLot2, parkingLot3);
        when(parkingRepository.findAll()).thenReturn(allLots);

        List<ParkingLot> sorted = parkingService.getSortedLots("pricing", null, null);

        assertNotNull(sorted);
        // Available lots should be sorted by price, then full lots appended
        assertTrue(sorted.get(0).getPrice() <= sorted.get(1).getPrice());
        verify(parkingRepository, times(1)).findAll();
    }
}