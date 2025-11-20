package com.example.parkingsystem.controller;


import com.example.parkingsystem.model.ParkingLot;
import com.example.parkingsystem.model.User;
import com.example.parkingsystem.service.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import com.example.parkingsystem.service.ParkingServiceImpl;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class ParkingSystemController{

    @Autowired
    private ParkingServiceImpl parkingService;

    @Autowired
    private UserServiceImpl userService;

    @GetMapping("/")
    public String home() { return "redirect:/login"; }

    @GetMapping("/parking-lots")
    public String showParkingLots(Model model) {
        List<ParkingLot> parkingLots = parkingService.getAllLots();
        model.addAttribute("parkingLots", parkingLots);
        return "all_parking_lots";
    }

    @GetMapping("/login")
    public String showLoginForm(Model model) {
        model.addAttribute("user", new User());
        return "login";
    }

    @PostMapping("/login")
    public String processLogin(@ModelAttribute("user") User user, Model model) {
        if ("admin".equals(user.getUsername()) && "password".equals(user.getPassword())) {
            return "redirect:/parking-lots";
        }else if ("manager1".equals(user.getUsername()) && "password1".equals(user.getPassword())) {
            user = userService.getUser(user.getUsername());
            return "redirect:/parkinglot?managerId=" + user.getId();
        }
        else {
            model.addAttribute("loginError", "Invalid username or password");
            return "login";
        }
    }

    @GetMapping("/signup")
    public String showSignupForm(Model model) {
        model.addAttribute("user", new User());
        return "signup";
    }

    @PostMapping("/signup")
    public String processSignup(@ModelAttribute("user") User user, Model model) {
        // userService.save(user);

        return "redirect:/login";
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


    @PostMapping("/update")
    public String updateParkingLot(@ModelAttribute("parkingLot") ParkingLot parkingLot) {

        parkingService.updateParkingLot(parkingLot);
        return "redirect:/manager/parkinglot?managerId=" + parkingLot.getManagerId();
    }
}
