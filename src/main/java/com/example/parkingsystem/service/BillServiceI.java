package com.example.parkingsystem.service;

import com.example.parkingsystem.model.Bill;

import java.util.List;

public interface BillServiceI {
    List<Bill> getBillsByUserId(int userId);
    void saveBill(Bill bill);
}
