package com.example.parkingsystem. util;

/**
 * Singleton class for calculating distances between geographic coordinates.
 * Uses the Haversine formula to calculate distance in kilometers.
 */
public class DistanceCalculator {

    private static DistanceCalculator instance;

    private DistanceCalculator() {
    }

    public static DistanceCalculator getInstance() {
        if (instance == null) {
            instance = new DistanceCalculator();
        }
        return instance;
    }

    /**
     * Calculate distance between two points using Haversine formula
     * @param lat1 Latitude of first point
     * @param lon1 Longitude of first point
     * @param lat2 Latitude of second point
     * @param lon2 Longitude of second point
     * @return distance in kilometers
     */
    public double calculateDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
        if (lat2 == null || lon2 == null) {
            return Double.MAX_VALUE;
        }

        final int EARTH_RADIUS = 6371;

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }
}