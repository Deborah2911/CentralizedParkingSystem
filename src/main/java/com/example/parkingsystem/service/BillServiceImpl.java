package com.example.parkingsystem.service;

import com.example.parkingsystem.model.Bill;
import com.example.parkingsystem.repository.BillRepoI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
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

    /**
     * Get sorted bills by the specified criteria
     * @param userId The user ID
     * @param sortBy The sorting criteria: "date", "location", "amount"
     * @return Sorted list of bills
     */
    public List<Bill> getSortedBills(int userId, String sortBy) {
        List<Bill> bills = billRepo.findByUserId(userId);

        switch (sortBy) {
            case "date":
                // Sort by date descending (newest first)
                bills.sort(Comparator.comparing(Bill::getDateIssued).reversed());
                break;
            case "location":
                // Sort by parking lot name alphabetically
                bills.sort(Comparator.comparing(bill ->
                        bill.getParkingLotName() != null ? bill.getParkingLotName() : ""));
                break;
            case "amount":
                // Sort by money amount descending (highest first)
                bills.sort(Comparator.comparing(Bill::getMoneyAmount).reversed());
                break;
            default:
                // Default: sort by date descending
                bills.sort(Comparator.comparing(Bill::getDateIssued).reversed());
                break;
        }

        return bills;
    }
}