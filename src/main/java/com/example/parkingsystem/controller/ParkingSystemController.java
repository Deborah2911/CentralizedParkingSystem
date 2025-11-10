package com.example.parkingsystem.controller;


import com.example.parkingsystem.model.ParkingLot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import com.example.parkingsystem.service.ParkingServiceImpl;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class ParkingSystemController{

    @Autowired
    private ParkingServiceImpl parkingService;

    @GetMapping("/")
    public String home() { return "redirect:/parking-lots"; }

    @GetMapping("/parking-lots")
    public String showParkingLots(Model model) {
        List<ParkingLot> parkingLots = parkingService.getAllLots();
        model.addAttribute("parkingLots", parkingLots);
        return "all_parking_lots";
    }
}
