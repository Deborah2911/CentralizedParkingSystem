package com.example.parkingsystem.integration;

import com.example.parkingsystem.model.Bill;
import com.example.parkingsystem.model.ParkingLot;
import com.example.parkingsystem.model.User;
import com.example.parkingsystem.repository.BillRepoI;
import com.example.parkingsystem.repository.ParkingRepoI;
import com.example.parkingsystem.repository.UserRepoI;
import com.example.parkingsystem.service.BillServiceImpl;
import com.example.parkingsystem.service.ParkingServiceImpl;
import com.example.parkingsystem.service.UserServiceImpl;
import com.example.parkingsystem.util.DistanceCalculator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Integration Tests - FIXED Foreign Key Constraints
 *
 * ✅ Creates users FIRST before parking lots
 * ✅ Foreign key constraints satisfied
 * ✅ Uses correct column names
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Parking System Integration Tests - Fixed FK Constraints")
class ParkingSystemIntegrationTest {

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private ParkingServiceImpl parkingService;

    @Autowired
    private BillServiceImpl billService;

    @Autowired
    private UserRepoI userRepository;

    @Autowired
    private ParkingRepoI parkingRepository;

    @Autowired
    private BillRepoI billRepository;

    @MockBean
    private DistanceCalculator distanceCalculator;

    @BeforeEach
    void setUp() {
        doNothing().when(distanceCalculator)
                .calculateAndSetDistances(any(), any(), any());

        billRepository.deleteAll();
        parkingRepository.deleteAll();
        userRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        billRepository.deleteAll();
        parkingRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Integration Test 1: User registration and login")
    void testUserRegistrationAndLoginFlow() {
        User newUser = new User();
        newUser.setUsername("integrationuser");
        newUser.setPassword("securepass123");
        newUser.setRole(1);
        newUser.setMoneySpent(0);

        userService.createUser(newUser);
        User retrievedUser = userService.getUser("integrationuser");

        assertNotNull(retrievedUser);
        assertEquals("integrationuser", retrievedUser.getUsername());
        assertEquals("securepass123", retrievedUser.getPassword());
        assertEquals(1, retrievedUser.getRole());
        assertEquals(0, retrievedUser.getMoneySpent(), 0.01f);
        assertTrue(userService.usernameTaken("integrationuser"));
    }

    /**
     * TEST 2: Parking lot lifecycle
     *
     * ✅ FIXED: Create manager user FIRST
     * ✅ Then create parking lot with valid manager ID
     * ✅ Foreign key constraint satisfied
     */
    @Test
    @DisplayName("Integration Test 2: Parking lot creation and retrieval flow")
    void testParkingLotCreationAndRetrievalFlow() {
        // ✅ STEP 1: Create manager user FIRST
        User manager = new User();
        manager.setUsername("manager1");
        manager.setPassword("managerpass");
        manager.setRole(2);  // Manager role
        manager.setMoneySpent(0);
        userService.createUser(manager);

        User savedManager = userService.getUser("manager1");
        assertNotNull(savedManager, "Manager should be created");
        assertNotNull(savedManager.getId(), "Manager should have ID");

        // ✅ STEP 2: Create parking lot with valid manager ID
        ParkingLot lot = new ParkingLot();
        lot.setName("Integration Test Parking");
        lot.setSpots(50);
        lot.setFreeSpots(50);
        lot.setPrice(4.50);
        lot.setManagerId(savedManager.getId());  // ✅ Use valid manager ID
        lot.setLatitude(40.7128);
        lot.setLongitude(-74.0060);
        lot.setLocation("Test Location");

        parkingService.addParkingLot(lot);
        assertNotNull(lot.getId(), "Lot should be created");

        // ✅ STEP 3: Retrieve by manager ID
        ParkingLot retrievedLot = parkingService.findParkingLotByManagerId(savedManager.getId());

        assertNotNull(retrievedLot, "Lot should be retrievable by manager ID");
        assertEquals("Integration Test Parking", retrievedLot.getName());
        assertEquals(50, retrievedLot.getFreeSpots());
        assertEquals(4.50, retrievedLot.getPrice(), 0.01);

        // ✅ STEP 4: Update parking lot
        retrievedLot.setFreeSpots(45);
        ParkingLot updatedLot = parkingService.updateParkingLot(retrievedLot);

        assertNotNull(updatedLot);
        assertEquals(45, updatedLot.getFreeSpots());

        // ✅ STEP 5: Verify in database
        ParkingLot verifyLot = parkingService.getParkingLotById(lot.getId());
        assertNotNull(verifyLot, "Lot should be retrievable by ID");
        assertEquals(45, verifyLot.getFreeSpots());
    }

    /**
     * TEST 3: Bill creation and retrieval
     */
    @Test
    @DisplayName("Integration Test 3: Bill creation and retrieval flow")
    void testBillCreationAndRetrievalFlow() {
        User user = new User();
        user.setUsername("billuser");
        user.setPassword("pass123");
        user.setRole(1);
        user.setMoneySpent(0);
        userService.createUser(user);

        User savedUser = userService.getUser("billuser");
        assertNotNull(savedUser);
        assertNotNull(savedUser.getId());

        Bill bill1 = new Bill();
        bill1.setUserId(savedUser.getId());
        bill1.setParkingLotId(1);
        bill1.setMoneyAmount(25.50);
        bill1.setDateIssued(LocalDateTime.now());
        bill1.setTimeSpent(5);

        Bill bill2 = new Bill();
        bill2.setUserId(savedUser.getId());
        bill2.setParkingLotId(1);
        bill2.setMoneyAmount(15.00);
        bill2.setDateIssued(LocalDateTime.now());
        bill2.setTimeSpent(3);

        billService.saveBill(bill1);
        billService.saveBill(bill2);
        assertNotNull(bill1.getId());
        assertNotNull(bill2.getId());

        List<Bill> userBills = billService.getBillsByUserId(savedUser.getId());
        assertNotNull(userBills);
        assertEquals(2, userBills.size());

        double totalSpent = userBills.stream()
                .mapToDouble(Bill::getMoneyAmount)
                .sum();
        assertEquals(40.50, totalSpent, 0.01);
    }

    /**
     * TEST 4: End-to-end parking scenario
     *
     * ✅ FIXED: Create manager and customer users FIRST
     */
    @Test
    @DisplayName("Integration Test 4: End-to-end parking and billing scenario")
    void testEndToEndParkingAndBillingScenario() {

        // ========== PHASE 1: CREATE MANAGER USER ==========
        User manager = new User();
        manager.setUsername("parkingmanager");
        manager.setPassword("managerpass");
        manager.setRole(2);
        manager.setMoneySpent(0);
        userService.createUser(manager);

        User savedManager = userService.getUser("parkingmanager");
        assertNotNull(savedManager, "Manager should be saved");

        // ========== PHASE 2: CREATE CUSTOMER USER ==========
        User customer = new User();
        customer.setUsername("customer123");
        customer.setPassword("custpass");
        customer.setRole(1);
        customer.setMoneySpent(0);
        userService.createUser(customer);

        User savedCustomer = userService.getUser("customer123");
        assertNotNull(savedCustomer, "Customer should be saved");

        // ========== PHASE 3: CREATE PARKING LOT ==========
        ParkingLot lot = new ParkingLot();
        lot.setName("E2E Test Parking");
        lot.setSpots(100);
        lot.setFreeSpots(100);
        lot.setPrice(3.00);
        lot.setManagerId(savedManager.getId());  // ✅ Use valid manager ID
        lot.setLatitude(40.7128);
        lot.setLongitude(-74.0060);
        lot.setLocation("Test Location");

        parkingService.addParkingLot(lot);
        assertNotNull(lot.getId(), "Lot should be created");

        // ========== PHASE 4: VERIFY LOT ==========
        ParkingLot savedLot = parkingService.findParkingLotByManagerId(savedManager.getId());
        assertNotNull(savedLot, "Lot should be retrievable");
        assertTrue(savedLot.getFreeSpots() > 0, "Lot should have free spaces");

        // ========== PHASE 5: CREATE BILL ==========
        Bill parkingBill = new Bill();
        parkingBill.setUserId(savedCustomer.getId());
        parkingBill.setParkingLotId(savedLot.getId());
        parkingBill.setMoneyAmount(12.00);
        parkingBill.setDateIssued(LocalDateTime.now());
        parkingBill.setTimeSpent(4);
        billService.saveBill(parkingBill);
        assertNotNull(parkingBill.getId(), "Bill should be created");

        // ========== PHASE 6: UPDATE AVAILABILITY ==========
        savedLot.setFreeSpots(99);
        parkingService.updateParkingLot(savedLot);

        // ========== PHASE 7: VERIFY BILL ==========
        List<Bill> customerBills = billService.getBillsByUserId(savedCustomer.getId());
        assertEquals(1, customerBills.size());
        assertEquals(12.00, customerBills.get(0).getMoneyAmount(), 0.01);

        // ========== PHASE 8: VERIFY AVAILABILITY ==========
        ParkingLot updatedLot = parkingService.getParkingLotById(savedLot.getId());
        assertNotNull(updatedLot);
        assertEquals(99, updatedLot.getFreeSpots());
    }

    @Test
    @DisplayName("BONUS: Database clean state after transaction rollback")
    void testDatabaseCleanStateAfterEachTest() {
        long userCount = userRepository.count();
        long lotCount = parkingRepository.count();
        long billCount = billRepository.count();

        assertEquals(0, userCount);
        assertEquals(0, lotCount);
        assertEquals(0, billCount);
    }
}