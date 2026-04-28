package com.example.parkingsystem.service;

import com.example.parkingsystem.model.Bill;
import com.example.parkingsystem.repository.BillRepoI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class BillServiceImpl implements BillServiceI {

    @Autowired
    private BillRepoI billRepo;

    @Override
    public List<Bill> getBillsByUserId(int userId) {
        return billRepo.findByUserId(userId);
    }

    @Override
    public void saveBill(Bill bill) {
        billRepo.save(bill);
    }

    public List<Bill> getSortedBills(int userId, String sortBy) {
        List<Bill> bills = billRepo.findByUserId(userId);

        switch (sortBy) {
            case "date":
                bills.sort(Comparator.comparing(Bill::getDateIssued).reversed());
                break;
            case "location":
                bills.sort(Comparator.comparing(bill ->
                        bill.getParkingLotName() != null ? bill.getParkingLotName() : ""));
                break;
            case "amount":
                bills.sort(Comparator.comparing(Bill::getMoneyAmount).reversed());
                break;
            default:
                bills.sort(Comparator.comparing(Bill::getDateIssued).reversed());
                break;
        }

        return bills;
    }

    // Get all months with spending
    public Map<String, Double> getMonthlyTotals(int userId) {
        List<Bill> bills = billRepo.findByUserId(userId);

        return bills.stream()
                .filter(b -> b.getDateIssued() != null)
                .collect(Collectors.groupingBy(
                        b -> b.getDateIssued().getYear() + "-" +
                                String.format("%02d", b.getDateIssued().getMonthValue()),
                        LinkedHashMap::new,
                        Collectors.summingDouble(Bill::getMoneyAmount)
                ));
    }

    // Get all dates with spending
    public Map<String, Double> getDailyTotals(int userId) {
        List<Bill> bills = billRepo.findByUserId(userId);

        return bills.stream()
                .filter(b -> b.getDateIssued() != null)
                .collect(Collectors.groupingBy(
                        b -> b.getDateIssued().toLocalDate().toString(),
                        LinkedHashMap::new,
                        Collectors.summingDouble(Bill::getMoneyAmount)
                ));
    }
}