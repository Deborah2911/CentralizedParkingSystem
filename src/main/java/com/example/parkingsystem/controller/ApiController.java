package com.example.parkingsystem.controller;

import com.example.parkingsystem.model.Bill;
import com.example.parkingsystem.model.ParkingLot;
import com.example.parkingsystem.model.User;
import com.example.parkingsystem.service.BillServiceImpl;
import com.example.parkingsystem.service.ParkingServiceImpl;
import com.example.parkingsystem.service.StatisticsServiceImpl;
import com.example.parkingsystem.service.UserServiceImpl;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class ApiController {

    @Autowired
    private ParkingServiceImpl parkingService;

    @Autowired
    private BillServiceImpl billService;

    @Autowired
    private StatisticsServiceImpl statisticsService;

    /**
     * API Endpoint for Bills Page - Returns bills data as JSON
     */
    @GetMapping("/bills")
    public ResponseEntity<Map<String, Object>> getBillsData(
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String month,
            @RequestParam(required = false) String day,
            HttpSession session) {

        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return ResponseEntity.status(401).body(Collections.singletonMap("error", "Unauthorized"));
        }

        // Get bills sorted
        List<Bill> bills = billService.getSortedBills(loggedInUser.getId(), sort != null ? sort : "date");

        // Set parking lot names
        for (Bill bill : bills) {
            ParkingLot lot = parkingService.getParkingLotById(bill.getParkingLotId());
            if (lot != null) {
                bill.setParkingLotName(lot.getName());
            } else {
                bill.setParkingLotName("Unknown Location");
            }
        }

        // Get aggregation data for dropdowns
        Map<String, Double> monthlyTotals = billService.getMonthlyTotals(loggedInUser.getId());
        Map<String, Double> dailyTotals = billService.getDailyTotals(loggedInUser.getId());

        Double selectedTotal = 0.0;

        // Apply month filter if provided
        if (month != null && !month.isEmpty()) {
            String[] monthParts = month.split("-");
            int year = Integer.parseInt(monthParts[0]);
            int monthNum = Integer.parseInt(monthParts[1]);

            bills = bills.stream()
                    .filter(b -> b.getDateIssued() != null &&
                            b.getDateIssued().getYear() == year &&
                            b.getDateIssued().getMonthValue() == monthNum)
                    .collect(Collectors.toList());

            selectedTotal = monthlyTotals.getOrDefault(month, 0.0);
        }
        // Apply day filter if provided
        else if (day != null && !day.isEmpty()) {
            bills = bills.stream()
                    .filter(b -> b.getDateIssued() != null &&
                            b.getDateIssued().toLocalDate().toString().equals(day))
                    .collect(Collectors.toList());

            selectedTotal = dailyTotals.getOrDefault(day, 0.0);
        }

        // Build response
        Map<String, Object> response = new HashMap<>();
        response.put("bills", bills);
        response.put("monthlyTotals", monthlyTotals);
        response.put("dailyTotals", dailyTotals);
        response.put("selectedTotal", selectedTotal);

        return ResponseEntity.ok(response);
    }

    /**
     * API Endpoint for Parking Lots Page - Returns parking lots data as JSON
     */
    @GetMapping("/parking-lots")
    public ResponseEntity<Map<String, Object>> getParkingLotsData(
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Double userLat,
            @RequestParam(required = false) Double userLon) {

        String sortBy = (sort != null && !sort.isEmpty()) ? sort : "name";

        List<ParkingLot> parkingLots = parkingService.getSortedLots(sortBy, userLat, userLon);

        Map<String, Object> response = new HashMap<>();
        response.put("parkingLots", parkingLots);
        response.put("currentSort", sortBy);

        return ResponseEntity.ok(response);
    }

    /**
     * API Endpoint for Manager Dashboard - Returns statistics as JSON
     */
    @GetMapping("/parkinglot-stats")
    public ResponseEntity<Map<String, Object>> getParkingLotStats(
            @RequestParam int managerId,
            HttpSession session) {

        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return ResponseEntity.status(401).body(Collections.singletonMap("error", "Unauthorized"));
        }

        ParkingLot parkingLot = parkingService.findParkingLotByManagerId(managerId);

        if (parkingLot == null) {
            return ResponseEntity.notFound().build();
        }

        int currentYear = LocalDate.now().getYear();
        LocalDate today = LocalDate.now();

        Map<Integer, Integer> monthlyStats = statisticsService.getMonthlyStats(parkingLot.getId(), currentYear);
        Map<Integer, Integer> hourlyStats = statisticsService.getHourlyStats(parkingLot.getId(), today);

        Map<String, Object> response = new HashMap<>();
        response.put("monthlyStats", monthlyStats);
        response.put("hourlyStats", hourlyStats);
        response.put("parkingLot", parkingLot);

        return ResponseEntity.ok(response);
    }

    /**
     * API Endpoint for updating parking lot - Returns success/error as JSON
     */
    @PostMapping("/parkinglot")
    @Transactional
    public ResponseEntity<Map<String, Object>> updateParkingLotApi(
            @RequestBody ParkingLot parkingLot,
            HttpSession session) {

        System.out.println("\n=== UPDATE PARKING LOT API CALLED ===");
        System.out.println("Received data: ID=" + parkingLot.getId() + ", Name=" + parkingLot.getName() +
                ", Spots: " + parkingLot.getSpots() + ", Free: " + parkingLot.getFreeSpots() +
                ", Price: " + parkingLot.getPrice());

        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            System.out.println("ERROR: User not logged in");
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Unauthorized: Please log in first.");
            return ResponseEntity.status(401).body(response);
        }

        System.out.println("Logged in user: " + loggedInUser.getId());

        try {
            // Validate the parking lot exists
            ParkingLot existingLot = parkingService.getParkingLotById(parkingLot.getId());
            System.out.println("Existing lot found: " + (existingLot != null));

            if (existingLot == null) {
                System.out.println("ERROR: Parking lot not found with ID: " + parkingLot.getId());
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Parking lot not found.");
                return ResponseEntity.notFound().build();
            }

            System.out.println("Existing lot manager ID: " + existingLot.getManagerId() +
                    ", Current user ID: " + loggedInUser.getId());

            // Validate manager ownership
            if (existingLot.getManagerId() != loggedInUser.getId()) {
                System.out.println("ERROR: User does not own this parking lot");
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Unauthorized: You can only update your own parking lot.");
                return ResponseEntity.status(403).body(response);
            }

            // Preserve fields that should not be updated
            parkingLot.setManagerId(existingLot.getManagerId());
            parkingLot.setLatitude(existingLot.getLatitude());
            parkingLot.setLongitude(existingLot.getLongitude());
            parkingLot.setLocation(existingLot.getLocation());

            // Update the parking lot
            ParkingLot updatedLot = parkingService.updateParkingLot(parkingLot);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Parking lot updated successfully!");
            response.put("parkingLot", updatedLot);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("ERROR updating parking lot: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to update parking lot: " + e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }
}