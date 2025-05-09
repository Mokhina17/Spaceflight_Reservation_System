package rest_server;

import model.DatabaseConnector;

import java.util.Base64;
import java.util.List;
import java.util.Map;

public class DataValidation {

    private final DatabaseConnector dbConnector;

    public DataValidation(DatabaseConnector dbConnector) {
        this.dbConnector = dbConnector;
    }

    //Verifies if a given string represents a valid positive integer.
    public boolean isValidId(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        try {
            return Integer.parseInt(input) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    //Checks if a specific id exists in the given database table.
    public boolean isValidId(int id, String table) {
        List<Map<String, Object>> result = dbConnector.executeSelectQuery(new String[]{"*"}, new String[]{table},
                null, "id = ?", new String[]{String.valueOf(id)});

        return !result.isEmpty();
    }

    //Checks if a requested number of seats can be reserved for a specific date and playtime.
    public boolean isValidAmountOfSeats(int flightDateId, int seats) {
        if (seats <= 0) {
            return false;
        }

        List<Map<String, Object>> result = dbConnector.executeSelectQuery(new String[]{"*"},
                new String[]{DatabaseConnector.FLIGHT_DATES}, null, "id = ?",
                new String[]{String.valueOf(flightDateId)});

        //checks if more or equal seats are available.
        return !result.isEmpty() && seats <= (int) result.get(0).get("available_seats");
    }

    //Authenticates a user based on a provided authorization string and userId.
    public boolean isUserAuthorized(String authString, String userId) {
        if (authString == null || authString.isEmpty()) {
            return false;
        }

        //extracts data before decoding it back to original string.
        String[] authParts = authString.split("\\s+");
        String authInfo = authParts[1];

        //decodes the data back to original string.
        byte[] decodedBytes = Base64.getDecoder().decode(authInfo);
        String decodedAuth = new String(decodedBytes);

        //extracts username (email) and password
        String[] credentials = decodedAuth.split(":");
        String email = credentials[0];
        String password = credentials[1];

        //checks if entry in table exists with given email, password and id.
        List<Map<String, Object>> result = dbConnector.executeSelectQuery(new String[]{"*"},
                new String[]{DatabaseConnector.CUSTOMERS}, null, "id = ? and email = ? and password = ?",
                new String[]{userId, email, password});

        //checks if user exists with this credentials and id.
        return result.size() == 1;
    }

}
