package com.example.parkingsystem.service;

import java.time.LocalDate;
import java.util.Map;

public interface StatisticsServiceI {

    /**
     * Get monthly parking statistics for a parking lot in a given year
     * @param parkingLotId The parking lot ID
     * @param year The year to filter by
     * @return Map with month (1-12) as key and count of parking sessions as value
     */
    Map<Integer, Integer> getMonthlyStats(Integer parkingLotId, Integer year);

    /**
     * Get hourly parking statistics for a parking lot on a specific date
     * @param parkingLotId The parking lot ID
     * @param date The date to filter by
     * @return Map with hour (0-23) as key and count of parking sessions as value
     */
    Map<Integer, Integer> getHourlyStats(Integer parkingLotId, LocalDate date);
}