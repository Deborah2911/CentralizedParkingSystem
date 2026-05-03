package com.example.parkingsystem.util;

import com.example.parkingsystem.model.ParkingLot;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * DistanceCalculator utilizes the Google Distance Matrix API to calculate
 * distances from a user's location to each parking lot.
 *
 * The calculated distances are set directly on ParkingLot objects for use
 * in sorting and filtering operations.
 */
@Component
public class DistanceCalculator {

    private static final Logger logger = LoggerFactory.getLogger(DistanceCalculator.class);
    private static final String GOOGLE_DISTANCE_MATRIX_API = "https://maps.googleapis.com/maps/api/distancematrix/json";

    @Value("${google.maps.api-key:}")
    private String googleApiKey;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Calculates distances from user's location to all parking lots using Google Distance Matrix API.
     * Sets the calculated distance on each ParkingLot object.
     *
     * @param userLatitude User's current latitude
     * @param userLongitude User's current longitude
     * @param parkingLots List of parking lots to calculate distances to
     */
    public void calculateAndSetDistances(Double userLatitude, Double userLongitude, List<ParkingLot> parkingLots) {
        if (userLatitude == null || userLongitude == null || parkingLots == null || parkingLots.isEmpty()) {
            logger.warn("Invalid input: userLatitude={}, userLongitude={}, parkingLots size={}",
                    userLatitude, userLongitude, parkingLots != null ? parkingLots.size() : 0);
            return;
        }

        if (googleApiKey == null || googleApiKey.isEmpty()) {
            logger.error("Google API Key not configured. Set google.api.key in application.properties");
            return;
        }

        try {
            String userLocation = userLatitude + "," + userLongitude;
            StringBuilder destinations = new StringBuilder();

            // Build destinations string from parking lot coordinates
            int validLotsCount = 0;
            for (ParkingLot lot : parkingLots) {
                if (lot.getLatitude() != null && lot.getLongitude() != null) {
                    if (validLotsCount > 0) {
                        destinations.append("|");
                    }
                    destinations.append(lot.getLatitude()).append(",").append(lot.getLongitude());
                    validLotsCount++;
                }
            }

            if (validLotsCount == 0) {
                logger.warn("No parking lots with valid coordinates found");
                return;
            }

            // Build API request URL with proper encoding
            String encodedOrigins = URLEncoder.encode(userLocation, StandardCharsets.UTF_8);
            String encodedDestinations = URLEncoder.encode(destinations.toString(), StandardCharsets.UTF_8);

            String apiUrl = String.format(
                    "%s?origins=%s&destinations=%s&mode=driving&key=%s",
                    GOOGLE_DISTANCE_MATRIX_API,
                    encodedOrigins,
                    encodedDestinations,
                    googleApiKey
            );

            logger.debug("Calling Google Distance Matrix API with {} destinations", validLotsCount);

            // Make API request
            JsonNode response = makeApiRequest(apiUrl);

            if (response == null) {
                logger.error("Failed to get response from Google Distance Matrix API");
                return;
            }

            // Parse response and extract distances
            String status = response.path("status").asText("");
            if (!"OK".equals(status)) {
                logger.error("Google Distance Matrix API returned status: {}", status);
                String errorMessage = response.path("error_message").asText("Unknown error");
                logger.error("Error message: {}", errorMessage);
                return;
            }

            JsonNode rows = response.path("rows");
            if (!rows.isArray() || rows.size() == 0) {
                logger.warn("No rows returned from Google Distance Matrix API");
                return;
            }

            JsonNode row = rows.get(0);
            JsonNode elements = row.path("elements");

            if (!elements.isArray() || elements.size() == 0) {
                logger.warn("No elements returned from Google Distance Matrix API");
                return;
            }

            // Assign distances to parking lots in the same order as destinations
            int elementIndex = 0;
            for (ParkingLot lot : parkingLots) {
                if (lot.getLatitude() != null && lot.getLongitude() != null) {
                    if (elementIndex < elements.size()) {
                        JsonNode element = elements.get(elementIndex);
                        String elementStatus = element.path("status").asText("");

                        if ("OK".equals(elementStatus)) {
                            JsonNode distance = element.path("distance");
                            if (distance != null && distance.has("value")) {
                                // Distance in meters from API
                                long distanceInMeters = distance.path("value").asLong(0);
                                // Convert to kilometers for display
                                double distanceInKm = distanceInMeters / 1000.0;
                                lot.setDistance(distanceInKm);
                                logger.debug("Parking lot '{}' (ID: {}) distance: {:.2f} km",
                                        lot.getName(), lot.getId(), distanceInKm);
                            }
                        } else {
                            logger.warn("Error calculating distance for lot '{}': {}", lot.getName(), elementStatus);
                            lot.setDistance(Double.MAX_VALUE); // Mark as unreachable
                        }
                        elementIndex++;
                    }
                }
            }

            logger.info("Successfully calculated distances for {} parking lots", elementIndex);

        } catch (Exception e) {
            logger.error("Error calculating distances: ", e);
        }
    }

    /**
     * Makes an HTTP request to Google Distance Matrix API and returns the JSON response.
     *
     * @param urlString API endpoint URL
     * @return JsonNode response or null if request fails
     */
    private JsonNode makeApiRequest(String urlString) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestProperty("User-Agent", "CentralizedParkingSystem/1.0");

            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                logger.error("Google API returned response code: {}", responseCode);
                return null;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            return objectMapper.readTree(response.toString());

        } catch (Exception e) {
            logger.error("Error making API request to Google Distance Matrix API: ", e);
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}