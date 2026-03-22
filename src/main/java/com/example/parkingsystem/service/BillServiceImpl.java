package com.example.parkingsystem.service;

import com.example.parkingsystem.model.Bill;
import com.example.parkingsystem.repository.BillRepoI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
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
}
