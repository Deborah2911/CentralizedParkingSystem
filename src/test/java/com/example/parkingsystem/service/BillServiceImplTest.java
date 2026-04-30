package com.example.parkingsystem.service;

import com.example.parkingsystem.model.Bill;
import com.example.parkingsystem.repository.BillRepoI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("BillServiceImpl Unit Tests")
class BillServiceImplTest {

    @Mock
    private BillRepoI billRepository;

    @InjectMocks
    private BillServiceImpl billService;

    private Bill bill1;
    private Bill bill2;
    private Bill bill3;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        bill1 = new Bill();
        bill1.setId(1);
        bill1.setUserId(1);
        bill1.setParkingLotId(1);
        bill1.setMoneyAmount(25.50);
        bill1.setDateIssued(LocalDateTime.of(2026, 4, 15, 10, 30));
        bill1.setTimeSpent(5);
        bill1.setParkingLotName("Downtown Parking");

        bill2 = new Bill();
        bill2.setId(2);
        bill2.setUserId(1);
        bill2.setParkingLotId(2);
        bill2.setMoneyAmount(15.00);
        bill2.setDateIssued(LocalDateTime.of(2026, 4, 20, 14, 45));
        bill2.setTimeSpent(3);
        bill2.setParkingLotName("Airport Parking");

        bill3 = new Bill();
        bill3.setId(3);
        bill3.setUserId(1);
        bill3.setParkingLotId(1);
        bill3.setMoneyAmount(35.75);
        bill3.setDateIssued(LocalDateTime.of(2026, 4, 25, 9, 15));
        bill3.setTimeSpent(7);
        bill3.setParkingLotName("Downtown Parking");
    }

    @Test
    @DisplayName("Test 1: Should retrieve all bills by user ID")
    void testGetBillsByUserId() {
        List<Bill> bills = Arrays.asList(bill1, bill2, bill3);
        when(billRepository.findByUserId(1)).thenReturn(bills);

        List<Bill> result = billService.getBillsByUserId(1);

        assertNotNull(result);
        assertEquals(3, result.size());
        verify(billRepository, times(1)).findByUserId(1);
    }

    @Test
    @DisplayName("Test 2: Should save bill successfully")
    void testSaveBillSuccess() {
        when(billRepository.save(any(Bill.class))).thenReturn(bill1);

        billService.saveBill(bill1);

        verify(billRepository, times(1)).save(bill1);
    }

    @Test
    @DisplayName("Test 3: Should sort bills by date (newest first)")
    void testGetSortedBillsByDate() {
        List<Bill> unsortedBills = Arrays.asList(bill1, bill2, bill3);
        when(billRepository.findByUserId(1)).thenReturn(unsortedBills);

        List<Bill> sorted = billService.getSortedBills(1, "date");

        assertNotNull(sorted);
        assertEquals(3, sorted.size());
        assertTrue(sorted.get(0).getDateIssued().isAfter(sorted.get(1).getDateIssued()));
        verify(billRepository, times(1)).findByUserId(1);
    }

    @Test
    @DisplayName("Test 4: Should calculate monthly totals correctly")
    void testGetMonthlyTotals() {
        List<Bill> bills = Arrays.asList(bill1, bill2, bill3);
        when(billRepository.findByUserId(1)).thenReturn(bills);

        Map<String, Double> monthlyTotals = billService.getMonthlyTotals(1);

        assertNotNull(monthlyTotals);
        assertTrue(monthlyTotals.containsKey("2026-04"));
        assertEquals(76.25, monthlyTotals.get("2026-04"), 0.01);
        verify(billRepository, times(1)).findByUserId(1);
    }

    @Test
    @DisplayName("Test 5: Should calculate daily totals correctly")
    void testGetDailyTotals() {
        List<Bill> bills = Arrays.asList(bill1, bill2, bill3);
        when(billRepository.findByUserId(1)).thenReturn(bills);

        Map<String, Double> dailyTotals = billService.getDailyTotals(1);

        assertNotNull(dailyTotals);
        assertTrue(dailyTotals.containsKey("2026-04-15"));
        assertEquals(25.50, dailyTotals.get("2026-04-15"), 0.01);
        assertEquals(15.00, dailyTotals.get("2026-04-20"), 0.01);
        verify(billRepository, times(1)).findByUserId(1);
    }
}