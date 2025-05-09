package rest_server;

import com.google.gson.Gson;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.json.JsonMapper;
import model.DatabaseConnector;
import org.jetbrains.annotations.NotNull;
import utils.StringNames;
import utils.Utils;

import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;


public class RestServer {

    // reach server under: http://localhost:4568/ (simply type it in your web browser)

    private static DataValidation dataVal;
    private final Javalin javalinApp;
    private DatabaseConnector dbConnector;


    public RestServer(DatabaseConnector dbConnector, DataValidation dataValidation) {
        this(dbConnector, dataValidation, 4568);
    }

    //Initializes the server and sets up the required components.
    public RestServer(DatabaseConnector dbConnector, DataValidation dataValidation, int port) {
        this.dbConnector = dbConnector;
        dataVal = dataValidation;

        Gson gson = new Gson();
        JsonMapper gsonMapper = new JsonMapper() {
            @Override
            public @NotNull String toJsonString(@NotNull Object obj, @NotNull Type type) {
                return gson.toJson(obj, type);
            }

            @Override
            public <T> @NotNull T fromJsonString(@NotNull String json, @NotNull Type targetType) {
                return gson.fromJson(json, targetType);
            }
        };
        this.javalinApp = Javalin.create(config -> config.jsonMapper(gsonMapper)).start(port);
        defineRoutes();
    }

    //Starts the server with the database and validation.
    public static void main(String[] args) {
        DatabaseConnector dbConnector = new DatabaseConnector("reservation_system");
        new RestServer(dbConnector, new DataValidation(dbConnector));

    }

    public void setDbConnectorAndDataValidator(DatabaseConnector dbConnector) {
        this.dbConnector = dbConnector;
        dataVal = new DataValidation(dbConnector);
    }

    public void stopServer() {
        javalinApp.stop();
    }

    //Defines the API endpoints the server supports.
    public void defineRoutes() {
        javalinApp.get("test", context -> context.result("Test successfull, server is reachable!"));

        // DATE REQUESTS
        javalinApp.get("/dates", context -> {
            String dateId = context.queryParam(StringNames.dateId);

            if (dateId == null) {
                context.json(dbConnector.executeSelectQuery(
                        new String[]{"*"},
                        new String[]{DatabaseConnector.DATES},
                        null, null, null));
            } else {
                if (!dataVal.isValidId(dateId)) {
                    context.status(400);
                    context.json(new String[]{"Id must be an integer and greater than 0."});
                    return;
                }

                List<Map<String, Object>> queryResult = dbConnector.executeSelectQuery(
                        new String[]{"date"},
                        new String[]{DatabaseConnector.DATES},
                        null, "id = ?",
                        new String[]{dateId});

                if (queryResult.isEmpty()) {
                    context.status(404);
                    context.json(new String[]{"Date with id " + dateId + " not found."});
                    return;
                }
                context.json(queryResult);
            }
        });

        // SPACE COMPANIES REQUESTS
        javalinApp.get("/companies", context -> {
            String companyId = context.queryParam(StringNames.companyId);

            if (companyId == null) {
                List<Map<String, Object>> res = dbConnector.executeSelectQuery(
                        new String[]{"*"},
                        new String[]{DatabaseConnector.COMPANIES},
                        null, null, null);
                context.json(res);
            } else {
                if (!dataVal.isValidId(companyId)) {
                    context.status(400);
                    context.json(new String[]{"Id must be an integer and greater than 0."});
                    return;
                }

                List<Map<String, Object>> queryResult = dbConnector.executeSelectQuery(
                        new String[]{"*"},
                        new String[]{DatabaseConnector.COMPANIES},
                        null, "id = ?",
                        new String[]{companyId});

                if (queryResult.isEmpty()) {
                    context.status(404);
                    context.json(new String[]{"Company with id " + companyId + " not found."});
                    return;
                }
                context.json(queryResult);
            }
        });

        // SPACE FLIGHTS REQUESTS
        javalinApp.get("/flights", context -> {
            String companyId = context.queryParam(StringNames.companyId);
            String flightId = context.queryParam(StringNames.flightId);
            List<Map<String, Object>> queryResult;

            if ((companyId != null && !dataVal.isValidId(companyId))
                    || (flightId != null && !dataVal.isValidId(flightId))) {
                context.status(400);
                context.json(new String[]{"Id(s) must be integer and greater than 0."});
                return;
            }

            if (companyId == null && flightId == null) {
                queryResult = dbConnector.executeSelectQuery(
                        new String[]{"*"},
                        new String[]{DatabaseConnector.FLIGHTS},
                        null, null, null);
            } else if (companyId != null && flightId == null) {
                queryResult = dbConnector.executeSelectQuery(
                        new String[]{"fs.id as flightScheduleId", "f.id as flightId",
                                "f.name", "f.flight_duration", "f.view_type"},
                        new String[]{DatabaseConnector.FLIGHT_SCHEDULES,
                                DatabaseConnector.FLIGHTS,
                                DatabaseConnector.FLIGHT_DATES,
                                DatabaseConnector.DATES},
                        new String[]{"fs", "f", "fd", "d"},
                        "fs.companyId = ? and fs.flightId = f.id and fs.id = fd.flightScheduleId " +
                                "and fd.dateId = d.id and fd.available_seats > 0 and " +
                                "(( d.date = CURDATE() and fs.launch_time > CURTIME() ) OR d.date > CURDATE())",
                        new String[]{companyId});
            } else if (companyId == null && flightId != null) {
                queryResult = dbConnector.executeSelectQuery(
                        new String[]{"c.*"},
                        new String[]{DatabaseConnector.FLIGHT_SCHEDULES,
                                DatabaseConnector.COMPANIES},
                        new String[]{"fs", "c"},
                        "fs.flightId = ? AND fs.companyId = c.id",
                        new String[]{flightId});
            } else {
                queryResult = dbConnector.executeSelectQuery(
                        new String[]{"fd.id as flightDateId", "fs.launch_time",
                                "d.date", "f.name", "f.flight_duration", "f.view_type"},
                        new String[]{DatabaseConnector.FLIGHT_SCHEDULES,
                                DatabaseConnector.FLIGHT_DATES,
                                DatabaseConnector.DATES,
                                DatabaseConnector.FLIGHTS},
                        new String[]{"fs", "fd", "d", "f"},
                        "f.id = ? and f.id = fs.flightId and fs.companyId = ? " +
                                "and fs.id = fd.flightScheduleId and fd.dateId = d.id " +
                                "and fd.available_seats > 0 and " +
                                "(( d.date = CURDATE() and fs.launch_time > CURTIME()) OR d.date > CURDATE())",
                        new String[]{flightId, companyId});
            }

            if (queryResult.isEmpty()) {
                context.status(404);
                context.json(new String[]{"Given id(s) not found or no entries with this id(s)."});
                return;
            }
            context.json(queryResult);
        });

        // FLIGHT SCHEDULES REQUESTS
        javalinApp.get("/schedules", context -> {
            String flightScheduleId = context.queryParam(StringNames.flightDateId);
            String info = context.queryParam(StringNames.info);
            List<Map<String, Object>> queryResult;

            if (flightScheduleId != null && !dataVal.isValidId(flightScheduleId)) {
                context.status(400);
                context.json(new String[]{"Id must be an integer and greater than 0."});
                return;
            }

            if (flightScheduleId == null && info == null) {
                queryResult = dbConnector.executeSelectQuery(
                        new String[]{"*"},
                        new String[]{DatabaseConnector.FLIGHT_SCHEDULES},
                        null, null, null);
            } else if (flightScheduleId != null && info == null) {
                queryResult = dbConnector.executeSelectQuery(
                        new String[]{"*"},
                        new String[]{DatabaseConnector.FLIGHT_DATES},
                        null, "id = ?",
                        new String[]{flightScheduleId});
            } else if (flightScheduleId == null && info != null) {
                context.status(400);
                context.json(new String[]{"Schedule id required when info is given."});
                return;
            } else {
                queryResult = dbConnector.executeSelectQuery(
                        new String[]{"c.name as companyName", "f.name as flightName",
                                "fs.launch_time", "d.date",
                                "fd.id as flightDateId", "f.id as flightId",
                                "c.id as companyId"},
                        new String[]{DatabaseConnector.FLIGHT_SCHEDULES,
                                DatabaseConnector.FLIGHT_DATES,
                                DatabaseConnector.COMPANIES,
                                DatabaseConnector.FLIGHTS,
                                DatabaseConnector.DATES},
                        new String[]{"fs", "fd", "c", "f", "d"},
                        "fd.id = ? and fd.flightScheduleId = fs.id and fd.dateId = d.id " +
                                "and c.id = fs.companyId and f.id = fs.flightId",
                        new String[]{flightScheduleId});
            }

            if (queryResult.isEmpty()) {
                context.status(404);
                context.json(new String[]{"Schedule with id " + flightScheduleId +
                        " not found or no entries with this id."});
                return;
            }
            context.json(queryResult);
        });

        // RESERVATIONS REQUESTS
        javalinApp.get("/reservations", context -> {
            String customerId = context.queryParam(StringNames.customerId);
            String authString = context.header(StringNames.authorization);

            if (customerId != null) {
                if (!dataVal.isValidId(customerId)) {
                    context.status(400);
                    context.json(new String[]{"Id must be an integer and greater than 0."});
                    return;
                }

                if (authString == null || !dataVal.isUserAuthorized(authString, customerId)) {
                    context.status(401);
                    context.json(new String[]{"User is not authorized to view this information."});
                    return;
                }

                List<Map<String, Object>> queryResult = dbConnector.executeSelectQuery(
                        new String[]{"*"},
                        new String[]{DatabaseConnector.RESERVATIONS},
                        null, "customerId = ?",
                        new String[]{customerId});

                if (queryResult.isEmpty()) {
                    context.status(404);
                    context.json(new String[]{"Customer with id " + customerId +
                            " not found or has no reservations."});
                    return;
                }
                context.json(queryResult);
            } else {
                context.status(400);
                context.json(new String[]{"For retrieving all reservations of a customer, " +
                        "a customer id must be given."});
            }
        });

        javalinApp.get("/seatNumbers", ctx -> {
            String flightDateId = ctx.queryParam(StringNames.flightDateId);
            String reservationId = ctx.queryParam(StringNames.reservationId);

            // Error if both parameters are provided
            if (flightDateId != null && reservationId != null) {
                ctx.status(400).json(Map.of("error", "Only one of flightDateId or reservationId should be provided."));
                return;
            }

            List<Integer> seatNumbers = new ArrayList<>();

            try {
                if (flightDateId != null) {
                    // Validate flightDateId
                    if (!dataVal.isValidId(flightDateId)) {
                        ctx.status(400).json(Map.of("error", "Invalid flightDateId."));
                        return;
                    }

                    // Get all seat numbers for this flight date (JOIN reservations + seat_numbers)
                    List<Map<String, Object>> seats = dbConnector.executeSelectQuery(
                            new String[]{"sn.seat_number"},
                            new String[]{"seat_numbers sn JOIN reservations r ON sn.reservationId = r.id"},
                            null,
                            "r.flightDateId = ?",
                            new String[]{flightDateId}
                    );

                    seats.forEach(seat -> seatNumbers.add((Integer) seat.get("seat_number")));

                } else if (reservationId != null) {
                    // Validate reservationId
                    if (!dataVal.isValidId(reservationId)) {
                        ctx.status(400).json(Map.of("error", "Invalid reservationId."));
                        return;
                    }

                    // Directly get seats from seat_numbers table
                    List<Map<String, Object>> seats = dbConnector.executeSelectQuery(
                            new String[]{"seat_number"},
                            new String[]{"seat_numbers"},
                            null,
                            "reservationId = ?",
                            new String[]{reservationId}
                    );

                    seats.forEach(seat -> seatNumbers.add((Integer) seat.get("seat_number")));

                } else {
                    ctx.status(400).json(Map.of("error", "Either datePlaytimeId or reservationId must be provided."));
                    return;
                }

                ctx.json(seatNumbers);

            } catch (Exception e) {
                ctx.status(500).json(Map.of("error", "Internal server error: " + e.getMessage()));
            }
        });

        // CREATE RESERVATION
        javalinApp.post("/reservation/create", ctx -> {
            String customerId = ctx.queryParam(StringNames.customerId);
            String flightDateId = ctx.queryParam(StringNames.flightDateId);
            String seats = ctx.queryParam(StringNames.reservedSeats);
            String seatNumbers = ctx.queryParam(StringNames.seatNumbers);
            String authString = ctx.header(StringNames.authorization);

            // Validate required parameters
            if (customerId == null || flightDateId == null || seats == null || seatNumbers == null) {
                ctx.status(400).json(Map.of("error", "All parameters are required"));
                return;
            }

            // Validate IDs
            if (!dataVal.isValidId(customerId) || !dataVal.isValidId(flightDateId) || !dataVal.isValidId(seats)) {
                ctx.status(401).json(Map.of("error", "Invalid ID format"));
                return;
            }

            // Validate authorization
            if (!dataVal.isUserAuthorized(authString, customerId)) {
                ctx.status(402).json(Map.of("error", "Unauthorized"));
                return;
            }

            // Create reservation
            if (!dbConnector.executeInsertQuery(
                    DatabaseConnector.RESERVATIONS,
                    new String[]{"customerId", "flightDateId", "reserved_seats"},
                    new String[]{customerId, flightDateId, seats}
            )) {
                ctx.status(500).json(Map.of("error", "Reservation creation failed"));
                return;
            }

            // Add tokens based on number of seats
            dbConnector.executeUpdateQuery(
                    DatabaseConnector.CUSTOMERS,
                    new String[]{"tokens = tokens + ?"},
                    new String[]{seats},
                    "id = ?",
                    new String[]{customerId}
            );

            // Get new reservation ID
            List<Map<String, Object>> newRes = dbConnector.executeSelectQuery(
                    new String[]{"id"},
                    new String[]{DatabaseConnector.RESERVATIONS},
                    null,
                    "customerId = ? AND flightDateId = ? ORDER BY id DESC LIMIT 1",
                    new String[]{customerId, flightDateId}
            );

            if (newRes.isEmpty()) {
                ctx.status(500).json(Map.of("error", "Failed to retrieve new reservation"));
                return;
            }
            int reservationId = (int) newRes.get(0).get("id");

            // Insert seat numbers
            Arrays.stream(seatNumbers.split(","))
                    .forEach(seat -> dbConnector.executeInsertQuery(
                            DatabaseConnector.SEAT_NUMBERS,
                            new String[]{"reservationId", "seat_number"},
                            new String[]{String.valueOf(reservationId), seat.trim()}
                    ));

            // Update available seats
            dbConnector.executeUpdateQuery(
                    DatabaseConnector.FLIGHT_DATES,
                    new String[]{"available_seats = available_seats - ?"},
                    new String[]{seats},
                    "id = ?",
                    new String[]{flightDateId}
            );

            ctx.status(201).json(Map.of("message", "Reservation created", "reservationId", reservationId));
        });

        // DELETE RESERVATION
        javalinApp.delete("/reservation/delete", ctx -> {
            String reservationId = ctx.queryParam(StringNames.reservationId);
            String authString = ctx.header(StringNames.authorization);

            if (reservationId == null || !dataVal.isValidId(reservationId)) {
                ctx.status(400).json(Map.of("error", "Valid reservation ID required"));
                return;
            }

            // Get reservation details
            List<Map<String, Object>> reservation = dbConnector.executeSelectQuery(
                    new String[]{"*"},
                    new String[]{DatabaseConnector.RESERVATIONS},
                    null,
                    "id = ?",
                    new String[]{reservationId}
            );

            if (reservation.isEmpty()) {
                ctx.status(404).json(Map.of("error", "Reservation not found"));
                return;
            }

            // Validate authorization
            String customerId = String.valueOf(reservation.get(0).get(StringNames.customerId));
            if (!dataVal.isUserAuthorized(authString, customerId)) {
                ctx.status(401).json(Collections.singletonMap(
                        "error",
                        "User is not authorized to perform this action."
                ));
                return;
            }


            // Delete seat numbers
            dbConnector.executeDeleteQuery(
                    DatabaseConnector.SEAT_NUMBERS,
                    "reservationId = ?",
                    new String[]{reservationId}
            );

            // Delete reservation
            dbConnector.executeDeleteQuery(
                    DatabaseConnector.RESERVATIONS,
                    "id = ?",
                    new String[]{reservationId}
            );

            // Restore seats
            if(reservation.get(0).get(StringNames.reservedSeats) == null) {
                ctx.status(405).json(Map.of("error", "No reserved seats are found"));
                return;
            }

            String seats = String.valueOf(reservation.get(0).get(StringNames.reservedSeats));
            String flightDateId = String.valueOf(reservation.get(0).get(StringNames.flightDateId));
            dbConnector.executeUpdateQuery(
                    DatabaseConnector.FLIGHT_DATES,
                    new String[]{"available_seats = available_seats + ?"},
                    new String[]{seats},
                    "id = ?",
                    new String[]{flightDateId}
            );

            ctx.status(201).json(Map.of("message", "Reservation deleted"));
        });

        // MODIFY RESERVATION
        javalinApp.put("/reservation/modify", ctx -> {
            String reservationId = ctx.queryParam(StringNames.reservationId);
            String newSeats = ctx.queryParam(StringNames.reservedSeats);
            String newSeatNumbers = ctx.queryParam(StringNames.seatNumbers);
            String authString = ctx.header(StringNames.authorization);

            // Validate inputs
            if (reservationId == null || newSeats == null || newSeatNumbers == null) {
                ctx.status(400).json(Map.of("error", "All parameters required"));
                return;
            }

            // Get existing reservation
            List<Map<String, Object>> reservation = dbConnector.executeSelectQuery(
                    new String[]{"*"},
                    new String[]{DatabaseConnector.RESERVATIONS},
                    null,
                    "id = ?",
                    new String[]{reservationId}
            );

            if (reservation.isEmpty()) {
                ctx.status(404).json(Map.of("error", "Reservation not found"));
                return;
            }

            // Validate authorization
            String customerId = String.valueOf(reservation.get(0).get(StringNames.customerId));
            if (!dataVal.isUserAuthorized(authString, customerId)) {
                ctx.status(401).json(Map.of("error", "Unauthorized"));
                return;
            }

            // Get old seat numbers
            List<Map<String, Object>> oldSeats = dbConnector.executeSelectQuery(
                    new String[]{"seat_number"},
                    new String[]{DatabaseConnector.SEAT_NUMBERS},
                    null,
                    "reservationId = ?",
                    new String[]{reservationId}
            );

            Set<String> oldSeatSet = oldSeats.stream()
                    .map(seat -> String.valueOf(seat.get(StringNames.seatNumbers)))
                    .collect(Collectors.toSet());

            Set<String> newSeatSet = Arrays.stream(newSeatNumbers.split(","))
                    .map(String::trim)
                    .collect(Collectors.toSet());

            // Check for no changes
            if (oldSeatSet.equals(newSeatSet)) {
                ctx.status(400).json(Map.of("error", "No changes detected"));
                return;
            }

            // Update seat numbers
            dbConnector.executeDeleteQuery(
                    DatabaseConnector.SEAT_NUMBERS,
                    "reservationId = ?",
                    new String[]{reservationId}
            );

            newSeatSet.forEach(seat -> dbConnector.executeInsertQuery(
                    DatabaseConnector.SEAT_NUMBERS,
                    new String[]{"reservationId", "seat_number"},
                    new String[]{reservationId, seat}
            ));

            // Update reservation
            dbConnector.executeUpdateQuery(
                    DatabaseConnector.RESERVATIONS,
                    new String[]{"reserved_seats = ?"},
                    new String[]{newSeats},
                    "id = ?",
                    new String[]{reservationId}
            );

            ctx.status(200).json(Map.of("message", "Reservation updated"));
        });

        // Get schedules by date
        javalinApp.get("/schedules/by-date", context -> {
            String dateId = context.queryParam(StringNames.dateId);

            if (dateId == null) {
                context.status(400);
                context.json(new String[]{"Date ID is required"});
                return;
            }

            // Validate date ID format
            if (!dataVal.isValidId(dateId)) {
                context.status(400);
                context.json(new String[]{"Invalid date ID format. Must be a positive integer"});
                return;
            }

            List<Map<String, Object>> queryResult = dbConnector.executeSelectQuery(
                    new String[]{
                            "f.name as flightName",
                            "f.flight_duration",
                            "f.view_type",
                            "c.name as companyName",
                            "fs.launch_time",
                            "fd.available_seats",
                            "fd.id as flightDateId",
                            "c.id as companyId",
                            "f.id as flightId"
                    },
                    new String[]{
                            DatabaseConnector.FLIGHTS,
                            DatabaseConnector.COMPANIES,
                            DatabaseConnector.FLIGHT_SCHEDULES,
                            DatabaseConnector.FLIGHT_DATES,
                            DatabaseConnector.DATES
                    },
                    new String[]{"f", "c", "fs", "fd", "d"},
                    "d.id = ? AND fd.dateId = d.id AND fd.flightScheduleId = fs.id " +
                            "AND fs.flightId = f.id AND fs.companyId = c.id " +
                            "AND fd.available_seats > 0 " +
                            "AND (d.date > CURDATE() OR (d.date = CURDATE() AND fs.launch_time > CURTIME()))",
                    new String[]{dateId}
            );

            if (queryResult.isEmpty()) {
                context.status(404);
                context.json(new String[]{"No available flights found for the specified date ID"});
                return;
            }

            context.json(queryResult);
        });

        // Get all dates that have flights
        javalinApp.get("/dates-with-flights", context -> {
            List<Map<String, Object>> queryResult = dbConnector.executeSelectQuery(
                    new String[]{"DISTINCT d.id as dateId, d.date"},  // Add return dateId
                    new String[]{
                            DatabaseConnector.DATES,
                            DatabaseConnector.FLIGHT_DATES,
                            DatabaseConnector.FLIGHT_SCHEDULES
                    },
                    new String[]{"d", "fd", "fs"},
                    "fd.dateId = d.id AND fd.flightScheduleId = fs.id " +
                            "AND fd.available_seats > 0 " +
                            "AND (d.date > CURDATE() OR (d.date = CURDATE() AND fs.launch_time > CURTIME()))",
                    null
            );

            if (queryResult.isEmpty()) {
                context.status(404);
                context.json(new String[]{"No dates with available flights found"});
                return;
            }

            context.json(queryResult);
        });

        /*
         * --------------------------------------------------------------------
         * -------------- DO NOT CHANGE THE FOLLOWING ENDPOINTS! --------------
         * --------------------------------------------------------------------
         */

        javalinApp.get("/customers", context -> {
            String email = context.queryParam(StringNames.email);
            String password = context.queryParam(StringNames.password);
            String authString = context.header(StringNames.authorization);
            List<Map<String, Object>> queryResult;

            //data validation
            if (email != null && !Utils.isValidEmailAddress(email)) {
                context.status(400);
                context.json(new String[]{"Email address format not correct."});
                return;
            }
            if (password != null && !Utils.isValidPassword(password)) {
                context.status(400);
                context.json(new String[]{"Password format not correct."});
                return;
            }

            if (email == null && password == null) {
                context.status(400);
                context.json(new String[]{"At least e-mail is required as parameter."});
                return;
            } else if (email != null && password == null) { //gets customer information by email
                queryResult = dbConnector.executeSelectQuery(new String[]{"*"},
                        new String[]{DatabaseConnector.CUSTOMERS}, null, "email = ?", new String[]{email});
                if (queryResult.isEmpty()) { // email not found
                    context.status(404);
                    context.json(new String[]{"E-mail not found."});
                    return;
                } else {
                    //checks if user is authorized
                    String customerId = String.valueOf(queryResult.get(0).get("id"));
                    if (authString == null || !dataVal.isUserAuthorized(authString, customerId)) {
                        context.status(401);
                        context.json(new String[]{"User is not authorized to perform this action."});
                        return;
                    }
                }
            } else if (email == null && password != null) {
                context.status(400);
                context.json(new String[]{"E-mail required when password is given."});
                return;
            } else { //checks client credentials
                queryResult = dbConnector.executeSelectQuery(new String[]{"*"},
                        new String[]{DatabaseConnector.CUSTOMERS}, null, "email = ? and password = ?",
                        new String[]{email, password});
            }
            //checks for empty result set
            if (queryResult.isEmpty()) {
                context.status(404);
                context.json(new String[]{"E-mail not found or no valid credentials given."});
                return;
            }
            context.json(queryResult);
        });

        //Adds a new customer to the database.
        javalinApp.post("/customer/create", context -> {
            String firstname = context.queryParam(StringNames.firstname);
            String lastname = context.queryParam(StringNames.lastname);
            String email = context.queryParam(StringNames.email);
            String password = context.queryParam(StringNames.password);


            if (firstname != null && lastname != null && email != null && password != null) {
                //data validation
                if (!Utils.isAlpha(firstname) || !Utils.isAlpha(lastname)) {
                    context.status(400);
                    context.json(new String[]{"Firstname and lastname can only contain letters."});
                    return;
                }
                if (!Utils.isValidEmailAddress(email)) {
                    context.status(400);
                    context.json(new String[]{"Email address format not correct."});
                    return;
                }
                if (!Utils.isValidPassword(password)) {
                    context.status(400);
                    context.json(new String[]{"Password format not correct."});
                    return;
                }

                if (!dbConnector.executeInsertQuery(DatabaseConnector.CUSTOMERS,
                        new String[]{StringNames.firstname, StringNames.lastname, StringNames.email,
                                StringNames.password},
                        new String[]{firstname, lastname, email, password})) {
                    context.status(400);
                    context.json(new String[]{"E-mail address does already exist."});
                    return;
                }



                context.status(201);
                context.json(new String[]{"Customer created successfully!"});
            } else {
                context.status(400);
                context.json(new String[]{"For creating a customer, firstname, lastname, " +
                        "e-mail and password is required."});
            }
        });

        // Add token redemption endpoint
        javalinApp.put("/tokens/redeem", context -> {
            String authString = context.header(StringNames.authorization);
            String customerId = context.queryParam(StringNames.customerId);
            String tokensToRedeem = context.queryParam(StringNames.tokens);

            try {
                if (customerId == null || tokensToRedeem == null) {
                    context.status(400);
                    context.json(new String[]{"Customer ID and tokens amount required"});
                    return;
                }

                if (!dataVal.isUserAuthorized(authString, customerId)) {
                    context.status(401);
                    context.json(new String[]{"User not authorized"});
                    return;
                }

                int requestedTokens = Integer.parseInt(tokensToRedeem);
                if (!Utils.isValidTokenAmount(requestedTokens)) {
                    context.status(400);
                    context.json(new String[]{"Invalid token amount"});
                    return;
                }

                try {
                    List<Map<String, Object>> result = dbConnector.executeSelectQuery(
                            new String[]{"tokens"},
                            new String[]{DatabaseConnector.CUSTOMERS},
                            null,
                            "id = ?",
                            new String[]{customerId}
                    );

                    int currentTokens = (int) result.get(0).get(StringNames.tokens);
                    if (currentTokens < requestedTokens) {
                        context.status(400);
                        context.json(new String[]{"Not enough tokens available"});
                        return;
                    }

                    dbConnector.executeUpdateQuery(
                            DatabaseConnector.CUSTOMERS,
                            new String[]{"tokens = tokens - ?"},
                            new String[]{String.valueOf(requestedTokens)},
                            "id = ?",
                            new String[]{customerId}
                    );

                    context.status(200);
                    context.json(new String[]{"Tokens redeemed successfully"});
                } catch (Exception e) {
                    throw e;
                }
            } catch (Exception e) {
                context.status(500);
                context.json(new String[]{"Internal server error"});
            }
        });

        // Add get tokens endpoint
        javalinApp.get("/customer/getTokens", context -> {
            String customerId = context.queryParam(StringNames.customerId);
            String authString = context.header(StringNames.authorization);

            if (!dataVal.isUserAuthorized(authString, customerId)) {
                context.status(401);
                context.json(new String[]{"User not authorized"});
                return;
            }

            List<Map<String, Object>> result = dbConnector.executeSelectQuery(
                new String[]{"tokens"},
                new String[]{DatabaseConnector.CUSTOMERS},
                null,
                "id = ?",
                new String[]{customerId}
            );

            context.json(result);
        });
    }

}
