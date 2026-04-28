package com.example.parkingsystem.controller;


import com.example.parkingsystem.model.Bill;
import com.example.parkingsystem.model.ParkingLot;
import com.example.parkingsystem.model.User;
import com.example.parkingsystem.service.BillServiceImpl;
import com.example.parkingsystem.service.UserServiceImpl;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import com.example.parkingsystem.service.ParkingServiceImpl;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.example.parkingsystem.service.StatisticsServiceImpl;
import java.time.LocalDate;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ParkingSystemController{

    @Autowired
    private ParkingServiceImpl parkingService;

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private BillServiceImpl billService;

    @Autowired
    private StatisticsServiceImpl statisticsService;

    @GetMapping("/")
    public String home() { return "redirect:/login"; }

    @GetMapping("/parking-lots")
    public String showParkingLots(
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Double userLat,
            @RequestParam(required = false) Double userLon,
            Model model) {

        List<ParkingLot> parkingLots;

        String sortBy = (sort != null && !sort.isEmpty()) ? sort : "name";

        parkingLots = parkingService.getSortedLots(sortBy, userLat, userLon);

        model.addAttribute("parkingLots", parkingLots);
        model.addAttribute("currentSort", sortBy);
        return "all_parking_lots";
    }

    @GetMapping("/login")
    public String showLoginForm(Model model) {
        model.addAttribute("user", new User());
        return "login";
    }

    @PostMapping("/login")
    public String processLogin(@ModelAttribute("user") User user, HttpSession session, Model model) {
        User dbUser = userService.getUser(user.getUsername());
        if (dbUser != null && dbUser.getPassword().equals(user.getPassword())) {

            session.setAttribute("loggedInUser", dbUser);

            if (dbUser.getRole() == 1) {
                return "redirect:/parking-lots";
            } else if (dbUser.getRole() == 2) {
                return "redirect:/parkinglot?managerId=" + dbUser.getId();
            } else if (dbUser.getRole() == 3) {
                return "redirect:/add";
            }
        }
        model.addAttribute("loginError", "Invalid username or password");
        return "login";
    }

    @GetMapping("/signup")
    public String showSignupForm(Model model) {
        model.addAttribute("user", new User());
        return "signup";
    }

    @PostMapping("/signup")
    public String processSignup(@ModelAttribute("user") User user, Model model) {
        if(userService.usernameTaken(user.getUsername())) {
            userService.createUser(user);
            return "redirect:/login";
        }
        else{
            model.addAttribute("signupError", "Username is already taken. Please choose another.");
        }
        return "signup";
    }

    @GetMapping("/parkinglot")
    public String loadParkingLot(Model model, @RequestParam("managerId") int managerId) {

        ParkingLot parkingLot = parkingService.findParkingLotByManagerId(managerId);

        if (parkingLot == null) {
            model.addAttribute("error", "No parking lot found for this manager.");
            return "login";
        }

        int currentYear = LocalDate.now().getYear();
        LocalDate today = LocalDate.now();

        Map<Integer, Integer> monthlyStats = statisticsService.getMonthlyStats(parkingLot.getId(), currentYear);
        Map<Integer, Integer> hourlyStats = statisticsService.getHourlyStats(parkingLot.getId(), today);

        model.addAttribute("parkingLot", parkingLot);
        model.addAttribute("monthlyStats", monthlyStats);
        model.addAttribute("hourlyStats", hourlyStats);
        return "manageParkingLot";
    }

    @PostMapping("/parkinglot")
    public String updateParkingLot(@ModelAttribute("parkingLot") ParkingLot parkingLot, RedirectAttributes redirectAttributes) {
        try {
            parkingService.updateParkingLot(parkingLot);
            redirectAttributes. addFlashAttribute("successMessage", "Parking lot updated successfully!");
        } catch (Exception e) {
            redirectAttributes. addFlashAttribute("errorMessage", "Failed to update parking lot: " + e.getMessage());
        }
        return "redirect:/parkinglot?managerId=" + parkingLot.getManagerId();
    }

    @GetMapping("/add")
    public String showAddParkingLotForm(Model model) {
        model.addAttribute("parkingLot", new ParkingLot());
        return "addParkingLot";
    }

    @PostMapping("/add")
    public String addParkingLot(@ModelAttribute("parkingLot") ParkingLot parkingLot, RedirectAttributes redirectAttributes) {
        try {
            parkingService.addParkingLot(parkingLot);
            redirectAttributes.addFlashAttribute("successMessage", "Parking lot added successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to add parking lot:  " + e.getMessage());
        }
        return "redirect:/add";
    }

    @PostMapping("/save-bill")
    @ResponseBody
    public ResponseEntity<?> saveBill(@RequestBody Bill bill, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return ResponseEntity.status(401).body("Unauthorized: Please log in first.");
        }

        // Validate dateIssued is provided
        if (bill.getDateIssued() == null) {
            return ResponseEntity.badRequest().body("Date and time are required.");
        }

        // Validate date is not in the future
        if (bill.getDateIssued().isAfter(java.time.LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("Cannot create a bill with a future date. Please select a date and time in the past.");
        }

        bill.setUserId(loggedInUser.getId());
        billService.saveBill(bill);

        return ResponseEntity.ok("Bill saved successfully");
    }

    @GetMapping("/my-bills")
    public String showMyBills(
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String month,
            @RequestParam(required = false) String day,
            HttpSession session,
            Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

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

        // Add aggregation data for dropdowns
        Map<String, Double> monthlyTotals = billService.getMonthlyTotals(loggedInUser.getId());
        Map<String, Double> dailyTotals = billService.getDailyTotals(loggedInUser.getId());

        model.addAttribute("monthlyTotals", monthlyTotals);
        model.addAttribute("dailyTotals", dailyTotals);

        Double selectedTotal = 0.0;
        String selectedLabel = "";

        // If month is selected, filter bills and show total
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
            selectedLabel = month;
            model.addAttribute("selectedMonth", month);
        }
        // If day is selected, filter bills and show total
        else if (day != null && !day.isEmpty()) {
            bills = bills.stream()
                    .filter(b -> b.getDateIssued() != null &&
                            b.getDateIssued().toLocalDate().toString().equals(day))
                    .collect(Collectors.toList());

            selectedTotal = dailyTotals.getOrDefault(day, 0.0);
            selectedLabel = day;
            model.addAttribute("selectedDay", day);
        }

        model.addAttribute("bills", bills);
        model.addAttribute("selectedTotal", selectedTotal);
        model.addAttribute("selectedLabel", selectedLabel);
        model.addAttribute("currentSort", sort != null ? sort : "date");
        return "my_bills";
    }
}
