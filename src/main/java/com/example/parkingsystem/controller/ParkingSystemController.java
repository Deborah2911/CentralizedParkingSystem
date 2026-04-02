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

import java.util.List;

@Controller
public class ParkingSystemController{

    @Autowired
    private ParkingServiceImpl parkingService;

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private BillServiceImpl billService;

    @GetMapping("/")
    public String home() { return "redirect:/login"; }

    @GetMapping("/parking-lots")
    public String showParkingLots(
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Double userLat,
            @RequestParam(required = false) Double userLon,
            Model model) {

        List<ParkingLot> parkingLots;

        if (sort != null) {
            parkingLots = parkingService. getSortedLots(sort, userLat, userLon);
        } else {
            parkingLots = parkingService. getAllLots();
        }

        model.addAttribute("parkingLots", parkingLots);
        model.addAttribute("currentSort", sort != null ? sort :  "name");
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

        model.addAttribute("parkingLot", parkingLot);
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

        bill.setUserId(loggedInUser.getId());
        bill.setDateIssued(java.time.LocalDateTime.now());
        billService.saveBill(bill);

        return ResponseEntity.ok("Bill saved successfully");
    }

    @GetMapping("/my-bills")
    public String showMyBills(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        List<Bill> bills = billService.getBillsByUserId(loggedInUser.getId());

        for (Bill bill : bills) {
            ParkingLot lot = parkingService.getParkingLotById(bill.getParkingLotId());
            if (lot != null) {
                bill.setParkingLotName(lot.getName());
            } else {
                bill.setParkingLotName("Unknown Location"); // Fallback if lot was deleted
            }
        }

        model.addAttribute("bills", bills);
        return "my_bills";
    }
}
