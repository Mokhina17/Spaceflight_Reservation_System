package rest_server;

import model.DatabaseConnector;

import java.util.*;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.eq;

public class DataValidationTest {

    // ------------- DO NOT CHANGE THE FOLLOWING PART OF THE CODE -------------
    private static DataValidation dataVal;

    //Creates a mock DatabaseConnector and simulates database behavior for specific test cases.
    public void createAndAssignMockObjectSelectQuery(String[] projection, String[] tables, String[] tableAlias,
                                                     String selection, String[] selectionArgs, boolean addListItem,
                                                     String key, Object value) {
        //defines mock database connection object
        DatabaseConnector mockDbConn = mock(DatabaseConnector.class);

        //defines mock list to return
        List<Map<String, Object>> outList = new ArrayList<>();
        if (addListItem) {
            Map<String, Object> map = new HashMap<>();
            map.put(key, value);
            outList.add(map);
        }

        //specifies when-then-return structure
        when(mockDbConn.executeSelectQuery(projection, tables, tableAlias, selection, selectionArgs))
                .thenReturn(outList);

        //assigns mock database connection
        dataVal = new DataValidation(mockDbConn);

    }
    // ---------------------------------- END ----------------------------------

    //Test Cases
    @Test
    public void testIsValidId() {
        assertFalse(dataVal.isValidId(null));
        assertFalse(dataVal.isValidId(""));
        assertFalse(dataVal.isValidId("0"));
        assertFalse(dataVal.isValidId("-5"));

        assertTrue(dataVal.isValidId("1"));
        assertTrue(dataVal.isValidId("10"));
        assertTrue(dataVal.isValidId("1000"));
    }

    @Test
    public void testIsValidIdFromDatabase() {
        createAndAssignMockObjectSelectQuery(new String[]{"*"}, new String[]{DatabaseConnector.CUSTOMERS}, null,
                "id = ?", new String[]{"1"}, true, "key", "value");

        assertTrue(dataVal.isValidId(1, DatabaseConnector.CUSTOMERS));
        assertFalse(dataVal.isValidId(10, DatabaseConnector.CUSTOMERS));

        createAndAssignMockObjectSelectQuery(new String[]{"*"}, new String[]{DatabaseConnector.CUSTOMERS}, null,
                "id = ?", new String[]{"1"}, false, null, null);

        assertFalse(dataVal.isValidId(1, DatabaseConnector.CUSTOMERS));
        assertFalse(dataVal.isValidId(10, DatabaseConnector.CUSTOMERS));

    }

    @Test
    public void testIsUserAuthorized() {
        createAndAssignMockObjectSelectQuery(new String[]{"*"}, new String[]{DatabaseConnector.CUSTOMERS}, null,
                "id = ? and email = ? and password = ?", new String[]{"1", "email@test.de", "testPassword"},
                true,"key", "value");
        String authorization = "Basic " + Base64.getEncoder().encodeToString("email@test.de:testPassword".getBytes());

        //correct input
        assertTrue(dataVal.isUserAuthorized(authorization, "1"));

        //wrong id
        assertFalse(dataVal.isUserAuthorized(authorization, "2"));

        //wrong authorization
        authorization = "Basic " + Base64.getEncoder().encodeToString("email@test.de:wrongPassword".getBytes());
        assertFalse(dataVal.isUserAuthorized(authorization, "2"));
        assertFalse(dataVal.isUserAuthorized(authorization, "1"));
        assertFalse(dataVal.isUserAuthorized("", "1"));
        assertFalse(dataVal.isUserAuthorized(null, "1"));
    }

    @Test
    public void testIsValidAmountOfSeats() {
        createAndAssignMockObjectSelectQuery(
                new String[]{"*"},
                new String[]{DatabaseConnector.FLIGHT_DATES},
                null,
                "id = ?",
                new String[]{"1"},
                false,
                null,
                null
        );

        // Test case 1: Invalid number of seats (negative or zero)
        assertFalse(dataVal.isValidAmountOfSeats(1, 0));
        assertFalse(dataVal.isValidAmountOfSeats(1, -1));

        // Test case 2: Valid number of seats (available seats = 10)
        createAndAssignMockObjectSelectQuery(
                new String[]{"*"},
                new String[]{DatabaseConnector.FLIGHT_DATES},
                null,
                "id = ?",
                new String[]{"1"},
                true,
                "available_seats",
                10
        );

        // Check valid cases
        assertTrue(dataVal.isValidAmountOfSeats(1, 5));  // Request less than available
        assertTrue(dataVal.isValidAmountOfSeats(1, 10)); // Request exact amount
        assertFalse(dataVal.isValidAmountOfSeats(1, 11)); // Request more than available

        // Test case 3: Non-existent date_playtime_id
        createAndAssignMockObjectSelectQuery(
                new String[]{"*"},
                new String[]{DatabaseConnector.FLIGHT_DATES},
                null,
                "id = ?",
                new String[]{"999"},
                false,
                null,
                null
        );
        assertFalse(dataVal.isValidAmountOfSeats(999, 5));
    }
}
