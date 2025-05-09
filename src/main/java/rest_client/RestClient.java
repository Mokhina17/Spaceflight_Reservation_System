package rest_client;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import model.User;
import utils.StringNames;
import javafx.application.Platform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class RestClient {

    /**
     * --------------------------------------------------------------------
     * ---------------- DO NOT CHANGE THE FOLLOWING CODE! -----------------
     * --------------------------------------------------------------------
     */

    private static RestClient instance;

    private User user;

    /**
     * Private constructor, so that no objects can be created from the outside.
     */
    private RestClient() {
        // set base url
        String SERVER_URL = "http://localhost:4568";
        Unirest.config().defaultBaseUrl(SERVER_URL);
    }

    public static RestClient getRestClient() {
        return getRestClient(false);
    }

    public static RestClient getRestClient(boolean skipServerTest) {
        if (instance == null) {
            instance = new RestClient();
        }
        if (!skipServerTest){
            // Test if RestServer is reachable and show helpful error message if not
            try {
                Unirest.get("/test").asEmpty();
            } catch (UnirestException e) {
                Logger logger = Logger.getLogger("RestServer test");
                logger.warning("RestServer is unreachable - did you start it?");
                throw new RuntimeException("RestServer is unreachable - did you start it?");
            }
        }
        return instance;
    }

    // GET and SET user

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Converts the string in JSON format to a <code>JsonObject</code>.
     *
     * @param jsonString the string in JSON format
     * @return the <code>JsonObject</code>
     */
    private JsonObject mapStringToJsonObject(String jsonString) {
        return new Gson().fromJson(jsonString, JsonArray.class).get(0).getAsJsonObject();
    }

    /**
     * Converts the string representation of an array of JSON objects into a list of
     * <code>JsonObject</code>.
     *
     * @param jsonString the string in JSON format
     * @return the list of <code>JsonObject</code>
     */
    private List<JsonObject> mapStringToJsonObjectList(String jsonString) {
        JsonArray jsonArray = new Gson().fromJson(jsonString, JsonArray.class);

        List<JsonObject> jsonList = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            jsonList.add(jsonArray.get(i).getAsJsonObject());
        }
        return jsonList;
    }

    /*
     * ------------------------- CUSTOMER REQUESTS ------------------------
     */

    /**
     * Makes a REST request to the server. Retrieves the user id from the
     * corresponding database table by providing only the email address of the user
     * (is unique).
     *
     * @param email the email of the user
     * @return the result as a <code>JsonObject</code>
     */
    public JsonObject getUserInfoByMail(String email) {
        HttpResponse<JsonNode> jsonResponse = Unirest.get("/customers")
                .queryString(StringNames.email, email)
                .header(StringNames.authorization, user.getAuthorization())
                .asJson();
        if (jsonResponse.getStatus() != 200) {
            return null;
        }
        return mapStringToJsonObject(jsonResponse.getBody().toString());
    }

    /**
     * Makes a REST request to the server. Checks in the corresponding table if the
     * provided user credentials (email and password) are a valid entry, this means
     * if the password belongs to the email address.
     *
     * @param email    the email of the user
     * @param password the corresponding password of the same user
     * @return <code>true</code> if credentials are; <code>false</code> otherwise
     */
    public boolean validClientCredentials(String email, String password) {
        HttpResponse<JsonNode> jsonResponse = Unirest.get("/customers")
                .queryString(StringNames.email, email)
                .queryString(StringNames.password, password)
                .asJson();
        return jsonResponse.getStatus() == 200;
    }

    /**
     * Makes a REST request to the server. Tries to create a new user entry with the
     * provided information in the customer table.
     *
     * @param firstname the first name of the user
     * @param lastname  the last name of the user
     * @param email     the email of the user
     * @return <code>true</code> if user was created successfully;
     * <code>false</code> otherwise
     */
    public boolean createNewUser(String firstname, String lastname, String email, String password) {
        HttpResponse<JsonNode> jsonResponse = Unirest
                .post("/customer/create")
                .queryString(StringNames.firstname, firstname)
                .queryString(StringNames.lastname, lastname)
                .queryString(StringNames.email, email)
                .queryString(StringNames.password, password)
                .asJson();
        return jsonResponse.getStatus() == 201;
    }

    /*
     * --------------------------------------------------------
     * -------------------------- END -------------------------
     * --------------------------------------------------------
     */

    // DATE REQUESTS
    public List<JsonObject> requestDates() {
        String jsonString = Unirest.get("/dates").asJson().getBody().toString();
        return mapStringToJsonObjectList(jsonString);
    }

    // SPACE COMPANY REQUESTS
    public List<JsonObject> requestSpaceCompanies() {
        HttpResponse<JsonNode> jsonResponse = Unirest.get("/companies")
                .asJson();
        if (jsonResponse.getStatus() != 200) {
            return Collections.emptyList();
        }
        return mapStringToJsonObjectList(jsonResponse.getBody().toString());
    }

    public JsonObject requestCompanyInformation(int companyId) {
        HttpResponse<JsonNode> jsonResponse = Unirest.get("/companies")
                .queryString(StringNames.companyId, companyId)
                .asJson();
        if (jsonResponse.getStatus() != 200) {
            return null;
        }
        return mapStringToJsonObject(jsonResponse.getBody().toString());
    }

    // SPACE FLIGHT REQUESTS
    public List<JsonObject> requestFlightsOfCompany(int companyId) {
        HttpResponse<JsonNode> jsonResponse = Unirest.get("/flights")
                .queryString(StringNames.companyId, companyId)
                .asJson();
        if (jsonResponse.getStatus() != 200) {
            return null;
        }
        return mapStringToJsonObjectList(jsonResponse.getBody().toString());
    }

    public List<JsonObject> requestFlightInformationOfCompany(int companyId, int flightId) {
        HttpResponse<JsonNode> jsonResponse = Unirest.get("/flights")
                .queryString(StringNames.companyId, companyId)
                .queryString(StringNames.flightId, flightId)
                .asJson();
        if (jsonResponse.getStatus() != 200) {
            return null;
        }
        return mapStringToJsonObjectList(jsonResponse.getBody().toString());
    }

    // CUSTOMER REQUESTS

    public List<JsonObject> getReservationsOfClient() {
        if (user == null) return null;

        try {
            HttpResponse<JsonNode> jsonResponse = Unirest.get("/reservations")
                .queryString(StringNames.customerId, user.getId())
                .header(StringNames.authorization, user.getAuthorization())
                .asJson();

            System.out.println("Reservations response status: " + jsonResponse.getStatus());
            System.out.println("Reservations response body: " + jsonResponse.getBody());

            if (jsonResponse.getStatus() != 200) {
                return null;
            }
            return mapStringToJsonObjectList(jsonResponse.getBody().toString());
        } catch (UnirestException e) {
            System.out.println("Error getting reservations: " + e.getMessage());
            return null;
        }
    }

    // FLIGHT SCHEDULE REQUESTS
    public JsonObject getFlightDateInfo(int flightDateId) {
        HttpResponse<JsonNode> jsonResponse = Unirest.get("/schedules")
                .queryString(StringNames.flightDateId, flightDateId)
                .asJson();
        if (jsonResponse.getStatus() != 200) {
            return null;
        }
        return mapStringToJsonObject(jsonResponse.getBody().toString());
    }

    public JsonObject getScheduleInfo(int flightDateId) {
        HttpResponse<JsonNode> jsonResponse = Unirest.get("/schedules")
                .queryString(StringNames.flightDateId, flightDateId)
                .queryString(StringNames.info, true)
                .asJson();
        if (jsonResponse.getStatus() != 200) {
            return null;
        }
        return mapStringToJsonObject(jsonResponse.getBody().toString());
    }

    // RESERVATION REQUESTS
    public boolean createNewReservation(int flightDateId, int seats, List<Integer> seatNumbers) {
        if (user == null) return false;

        try {
            // Convert seat numbers to comma-separated string
            String seatNumbersStr = seatNumbers.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));

            HttpResponse<JsonNode> jsonResponse = Unirest.post("/reservation/create")
                    .queryString(StringNames.customerId, user.getId())
                    .queryString(StringNames.flightDateId, flightDateId)
                    .queryString(StringNames.reservedSeats, seats)
                    .queryString(StringNames.seatNumbers, seatNumbersStr) // Add seat numbers
                    .header(StringNames.authorization, user.getAuthorization())
                    .asJson();

            if (jsonResponse.getStatus() == 201) {
                updateTokensOfUser();
                return true;
            }
            return false;
        } catch (UnirestException e) {
            Logger.getLogger(getClass().getName()).log(Level.WARNING, "Failed to create reservation", e);
            return false;
        }
    }

    public boolean deleteReservation(int id) {
        if (user == null) return false;
        
        try {
            HttpResponse<JsonNode> jsonResponse = Unirest.delete("/reservation/delete")
                .queryString(StringNames.reservationId, id)
                .header(StringNames.authorization, user.getAuthorization())
                .asJson();
                
            if (jsonResponse.getStatus() == 201) {
                updateTokensOfUser();
                return true;
            }
            return false;
        } catch (UnirestException e) {
            Logger.getLogger(getClass().getName()).log(Level.WARNING, "Failed to delete reservation", e);
            return false;
        }
    }

    public boolean modifyReservation(int reservationId, int seatAmount, List<Integer> seatNumbers) {
        if (user == null) return false;

        try {
            // Convert seat numbers to comma-separated string
            String seatNumbersStr = seatNumbers.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));

            HttpResponse<JsonNode> response = Unirest.put("/reservation/modify")
                    .header(StringNames.authorization, user.getAuthorization())
                    .queryString(StringNames.reservationId, reservationId)
                    .queryString(StringNames.reservedSeats, seatAmount)
                    .queryString(StringNames.seatNumbers, seatNumbersStr)
                    .asJson();

            // Log status code first
            System.out.println("Modify reservation response status: " + response.getStatus());

            // Safely log response body if it exists
            if (response.getBody() != null) {
                System.out.println("Response body: " + response.getBody().toString());
            } else {
                System.out.println("Response body is null");
            }

            // Return true only for successful status code
            if (response.getStatus() == 200) {
                updateTokensOfUser();
                return true;
            }
            return false;

        } catch (UnirestException e) {
            Logger logger = Logger.getLogger(getClass().getName());
            logger.log(Level.WARNING, "Failed to modify reservation", e);
            return false;
        }
    }

    public List<Integer> fetchAvailableSeats(int flightDateId) {
        try {
            HttpResponse<JsonNode> response = Unirest.get("/seatNumbers")
                    .queryString("flightDateId", flightDateId)
                    .asJson();

            if (response.getStatus() == 200) {
                // Parse as Integer array first to avoid type ambiguity
                Integer[] seatNumbersArray = new Gson().fromJson(
                        response.getBody().toString(),
                        Integer[].class
                );
                return Arrays.asList(seatNumbersArray);
            }
            return Collections.emptyList();
        } catch (UnirestException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public List<Integer> getSeatNumbersForReservation(int reservationId) {
        try {
            HttpResponse<JsonNode> response = Unirest.get("/seatNumbers")
                    .queryString("reservationId", reservationId) // Correct parameter name
                    .asJson();

            if (response.getStatus() == 200) {
                // Parse as Integer array to match database schema
                Integer[] seatNumbersArray = new Gson().fromJson(
                        response.getBody().toString(),
                        Integer[].class
                );
                System.out.println(seatNumbersArray.length);
                return Arrays.asList(seatNumbersArray);
            }
            return Collections.emptyList();
        } catch (UnirestException e) {
            e.printStackTrace(); // Log the exception for debugging
            return Collections.emptyList();
        }
    }



    // Add these methods to the RestClient class

    /**
     * Makes a REST request to get all flight schedules for a specific date
     *
     * @param date the date to get flight schedules for
     * @return List of JsonObjects containing flight schedule information, empty list if request fails
     */
    public List<JsonObject> getFlightDatesPerDate(java.sql.Date date) {
        // Step 1: Get date ID for the given date
        HttpResponse<JsonNode> datesResponse = Unirest.get("/dates-with-flights")
                .asJson();

        // Print the status code of the first request
        System.out.println("First request status code: " + datesResponse.getStatus());


        if (datesResponse.getStatus() != 200) {
            return Collections.emptyList();
        }

        List<JsonObject> dates = mapStringToJsonObjectList(datesResponse.getBody().toString());
        String dateStr = date.toString();
        String dateId = null;

        // Find matching dateId for the given date
        for (JsonObject dateObj : dates) {
            String serverDate = dateObj.get("date").getAsString();
            if (utils.Utils.datesMatch(serverDate, dateStr)) {
                dateId = dateObj.get("dateId").getAsString();
                break;
            }
        }

        // Print the found dateId
        System.out.println("Found dateId: " + dateId);


        // If no matching date found, return empty list
        if (dateId == null) {
            return Collections.emptyList();
        }

        // Step 2: Get flight schedules using the date ID
        HttpResponse<JsonNode> schedulesResponse = Unirest.get("/schedules/by-date")
                .queryString(StringNames.dateId, dateId)
                .asJson();

        // Print the status code of the second request
        System.out.println("Second request status code: " + schedulesResponse.getStatus());

        if (schedulesResponse.getStatus() != 200) {
            return Collections.emptyList();
        }

        // Parse the flight schedule information
        List<JsonObject> schedules = mapStringToJsonObjectList(schedulesResponse.getBody().toString());

        // Print the number of schedules found
        System.out.println("Number of schedules found: " + schedules.size());

        // Return the list of schedules
        return schedules;
        // return mapStringToJsonObjectList(schedulesResponse.getBody().toString());
    }

    /**
     * Makes a REST request to get all dates that have scheduled flights
     *
     * @return List of JsonObjects containing dates with flights, empty list if request fails
     */
    public List<JsonObject> requestUniqueDates() {
        // Add log output
        System.out.println("Requesting unique dates with flights...");

        HttpResponse<JsonNode> jsonResponse = Unirest.get("/dates-with-flights")
                .asJson();

        // Record the response status.
        System.out.println("Response status: " + jsonResponse.getStatus());

        if (jsonResponse.getStatus() != 200) {
            System.out.println("Failed to get dates with flights");
            return Collections.emptyList();
        }

        List<JsonObject> dates = mapStringToJsonObjectList(jsonResponse.getBody().toString());
        // Output the number of dates found.
        System.out.println("Found " + dates.size() + " dates with flights");
        // Output specific dates for debugging
        dates.forEach(date ->
                System.out.println("Available date: " + date.get("date").getAsString())
        );

        return dates;
    }

    public void updateTokensOfUser() {
        if (user == null) return;
        
        try {
            HttpResponse<JsonNode> response = Unirest.get("/customer/getTokens")
                .queryString(StringNames.customerId, user.getId())
                .header(StringNames.authorization, user.getAuthorization())
                .asJson();
                
            if (response.getStatus() == 200 && response.getBody() != null) {
                List<JsonObject> result = mapStringToJsonObjectList(response.getBody().toString());
                if (!result.isEmpty()) {
                    int tokens = result.get(0).get("tokens").getAsInt();
                    user.setTokens(tokens);
                }
            }
        } catch (Exception e) {
            Logger logger = Logger.getLogger(getClass().getName());
            logger.log(Level.WARNING, "Failed to update user tokens", e);
        }
    }

    // Public method for testing
    public void refreshUserTokens() {
        updateTokensOfUser();
    }

    public boolean redeemTokens(int tokensToRedeem) {
        if (user == null) return false;
        
        try {
            HttpResponse<JsonNode> response = Unirest.put("/tokens/redeem")
                .queryString(StringNames.customerId, user.getId())
                .queryString(StringNames.tokens, tokensToRedeem)
                .header(StringNames.authorization, user.getAuthorization())
                .asJson();
            
            if (response.getStatus() == 200) {
                updateTokensOfUser();
                return true;
            }
            return false;
        } catch (UnirestException e) {
            return false;
        }
    }
}

