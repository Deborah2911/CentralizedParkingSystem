package com.example.parkingsystem.service;

import com.example.parkingsystem.model.Bill;
import com.example.parkingsystem.repository.BillRepoI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatisticsServiceImpl implements StatisticsServiceI {

    @Autowired
    private BillRepoI billRepo;

    @Override
    public Map<Integer, Integer> getMonthlyStats(Integer parkingLotId, Integer year) {
        // Get all bills for the parking lot in the given year
        List<Bill> bills = billRepo.findByParkingLotIdAndYear(parkingLotId, year);

        // Initialize map with all 12 months
        Map<Integer, Integer> monthlyStats = new LinkedHashMap<>();
        for (int month = 1; month <= 12; month++) {
            monthlyStats.put(month, 0);
        }

        // Count bills by month
        for (Bill bill : bills) {
            if (bill.getDateIssued() != null) {
                int month = bill.getDateIssued().getMonthValue();
                monthlyStats.put(month, monthlyStats.get(month) + 1);
            }
        }

        return monthlyStats;
    }

    @Override
    public Map<Integer, Integer> getHourlyStats(Integer parkingLotId, LocalDate date) {
        // Get all bills for the parking lot on the given date
        List<Bill> bills = billRepo.findByParkingLotIdAndDate(parkingLotId, date);

        // Initialize map with all 24 hours
        Map<Integer, Integer> hourlyStats = new LinkedHashMap<>();
        for (int hour = 0; hour < 24; hour++) {
            hourlyStats.put(hour, 0);
        }

        // Count bills by hour
        for (Bill bill : bills) {
            if (bill.getDateIssued() != null) {
                int hour = bill.getDateIssued().getHour();
                hourlyStats.put(hour, hourlyStats.get(hour) + 1);
            }
        }

        return hourlyStats;
    }
}