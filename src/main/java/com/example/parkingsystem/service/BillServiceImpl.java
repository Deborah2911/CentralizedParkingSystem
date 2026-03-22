package com.example.parkingsystem.service;

import com.example.parkingsystem.model.Bill;
import com.example.parkingsystem.repository.BillRepoI;

import java.util.List;

public class BillServiceImpl implements BillServiceI {

    private BillRepoI billRepo;

    @Override
    public List<Bill> getBillsByUser(int userId) {
        return billRepo.findByUserId(userId);
    }
}
