package rest_server;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONObject;
import model.DatabaseConnector;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import utils.StringNames;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RestServerTest {

    // ------------- DO NOT CHANGE THE FOLLOWING PART OF THE CODE -------------
    private static RestServer restServer;
    private static final int restServerTestPort = 4569;

    @BeforeAll
    public static void setUpConnectionAndVariables() {
        Unirest.config().defaultBaseUrl("http://localhost:" + restServerTestPort);

        DatabaseConnector mockDbConnector = mock(DatabaseConnector.class);
        restServer = new RestServer(mockDbConnector, new DataValidation(mockDbConnector), restServerTestPort);
    }

    @AfterAll
    public static void tearDown() {
        restServer.stopServer();
    }

    /**
     * Creates a new mock object for the class
     * <code>{@link DatabaseConnector}</code> and defines which output should be
     * returned for the given parameters when executing a SELECT query on the
     * database.
     *
     * @param projection    the array with column names; cannot be
     *                      <code>null</code>; can contain only "*" for selecting
     *                      all columns
     * @param tables        the array with database table names; cannot be
     *                      <code>null</code>
     * @param tableAlias    the array with table name aliases; can be
     *                      <code>null</code> to omit aliases
     * @param selection     the array with conditions; can be <code>null</code> to
     *                      omit conditions
     * @param selectionArgs the array with the corresponding values for the
     *                      selection; can be <code>null</code> if no parameter
     *                      values are needed
     * @param addListItem   <code>true</code> if an item should be added to the list
     *                      which is returned by the SELECT query;
     *                      <code>false</code> otherwise
     * @param key           the string defining the key of the map which is added to
     *                      the output list
     * @param value         the corresponding value to the key
     * @return the create mock object for the class
     * <code>{@link DatabaseConnector}</code>
     */
    public DatabaseConnector createAndAssignMockObjectSelectQuery(String[] projection, String[] tables,
                                                                  String[] tableAlias, String selection, String[] selectionArgs, boolean addListItem, String key,
                                                                  Object value) {
        // define mock database connection object
        DatabaseConnector mockDbConn = mock(DatabaseConnector.class);

        // define mock list to return
        List<Map<String, Object>> outList = new ArrayList<>();
        if (addListItem) {
            Map<String, Object> map = new HashMap<>();
            map.put(key, value);
            outList.add(map);
        }

        // specify when-then-return structure
        when(mockDbConn.executeSelectQuery(projection, tables, tableAlias, selection, selectionArgs))
                .thenReturn(outList);

        // assign mock database connection to server
        restServer.setDbConnectorAndDataValidator(mockDbConn);

        return mockDbConn;
    }

    // Sprint 4: New method of select query to fetch new Reservation data model
    /**
     * Creates a mock DatabaseConnector object and configures it to return specified output for a SELECT query.
     *
     * @param projection    array of column names; cannot be <code>null</code>; can use "*" to select all columns
     * @param tables       array of database table names; cannot be <code>null</code>
     * @param tableAlias   array of table aliases; can be <code>null</code> to omit aliases
     * @param selection    conditions string; can be <code>null</code> to omit conditions
     * @param selectionArgs values for selection parameters; can be <code>null</code> if no parameters needed
     * @param addListItem  <code>true</code> to add an item to query results, <code>false</code> otherwise
     * @param fieldMap     map of field names to values to add to query results
     * @return the configured mock DatabaseConnector object
     */
    public DatabaseConnector createAndAssignMockObjectSelectQuery(
            String[] projection, String[] tables, String[] tableAlias,
            String selection, String[] selectionArgs, boolean addListItem,
            Map<String, Object> fieldMap
    ) {
        DatabaseConnector mockDbConn = mock(DatabaseConnector.class);
        List<Map<String, Object>> outList = new ArrayList<>();
        if (addListItem) {
            outList.add(fieldMap);
        }
        when(mockDbConn.executeSelectQuery(projection, tables, tableAlias, selection, selectionArgs))
                .thenReturn(outList);
        restServer.setDbConnectorAndDataValidator(mockDbConn);
        return mockDbConn;
    }

    /**
     * Adds a SELECT query to the given mock object of
     * <code>{@link DatabaseConnector}</code>. The output that is returned by
     * executing the SELECT query with the given parameters is defined as a list
     * that contains maps with the given keys and values (similar to
     * <code>{@link #createAndAssignMockObjectSelectQuery(String[], String[], String[], String, String[], boolean, String, Object)}</code>).
     *
     * @param mockDbConn    the mock object to which a SELECT query is added
     * @param projection    the array with column names; cannot be
     *                      <code>null</code>; can contain only "*" for selecting
     *                      all columns
     * @param tables        the array with database table names; cannot be
     *                      <code>null</code>
     * @param tableAlias    the array with table name aliases; can be
     *                      <code>null</code> to omit aliases
     * @param selection     the array with conditions; can be <code>null</code> to
     *                      omit conditions
     * @param selectionArgs the array with the corresponding values for the
     *                      selection; can be <code>null</code> if no parameter
     *                      values are needed
     * @param keys          the list defining the keys of the maps which are added
     *                      to the output list
     * @param values        the corresponding values to each of the keys
     */
    public void addMockSelectQuery(DatabaseConnector mockDbConn, String[] projection, String[] tables,
                                   String[] tableAlias, String selection, String[] selectionArgs, String[] keys, Object[] values) {
        // define mock list to return
        List<Map<String, Object>> outList = new ArrayList<>();

        if (keys != null && values != null) {
            Map<String, Object> map = new HashMap<>();
            for (int i = 0; i < keys.length; i++) {
                map.put(keys[i], values[i]);
            }
            outList.add(map);
        }

        // specify when-then-return structure
        when(mockDbConn.executeSelectQuery(projection, tables, tableAlias, selection, selectionArgs))
                .thenReturn(outList);
    }

    /**
     * Creates a new mock object for the class
     * <code>{@link DatabaseConnector}</code> and defines which output should be
     * returned for the given parameters when executing an INSERT query on the
     * database.
     *
     * @param table       the name of the table; cannot be <code>null</code>
     * @param columns     the array containing the columns to insert values; cannot
     *                    be <code>null</code>
     * @param values      the array with the values to insert; cannot be
     *                    <code>null</code>; must have the same length as columns
     * @param returnValue <code>true</code> if the insertion should be mocked as
     *                    successful; <code>false</code> otherwise
     * @return the create mock object for the class
     * <code>{@link DatabaseConnector}</code>
     */
    public DatabaseConnector createAndAssignMockObjectInsertQuery(String table, String[] columns, String[] values,
                                                                  boolean returnValue) {
        // define mock database connection object
        DatabaseConnector mockDbConn = mock(DatabaseConnector.class);

        // specify when-then-return structure
        when(mockDbConn.executeInsertQuery(table, columns, values)).thenReturn(returnValue);

        // assign mock database connection to server
        restServer.setDbConnectorAndDataValidator(mockDbConn);

        return mockDbConn;
    }

    /**
     * Adds an UPDATE query to the given mock object of
     * <code>{@link DatabaseConnector}</code>. Defines the output that is returned
     * by executing the UPDATE query with the given parameters as the given
     * returnValue.
     *
     * @param mockDbConn       the mock object to which an UPDATE query is added
     * @param table            the name of the table; cannot be <code>null</code>
     * @param modification     the array with columns that are changed; cannot be
     *                         <code>null</code>;
     * @param modificationArgs the array with the corresponding values for the
     *                         modification; can be <code>null</code> if no
     *                         modification parameter are needed
     * @param selection        the array with conditions; can be <code>null</code>
     *                         to omit conditions
     * @param selectionArgs    the array with the corresponding values for the
     *                         selection; can be <code>null</code> if no parameter
     *                         values are needed
     * @param returnValue      <code>true</code> if the update should be mocked as
     *                         successful; <code>false</code> otherwise
     */
    public void addMockUpdateQuery(DatabaseConnector mockDbConn, String table, String[] modification,
                                   String[] modificationArgs, String selection, String[] selectionArgs, boolean returnValue) {
        // specify when-then-return structure
        when(mockDbConn.executeUpdateQuery(table, modification, modificationArgs, selection, selectionArgs))
                .thenReturn(returnValue);
    }

    /**
     * Adds a DELETE query to the given mock object of
     * <code>{@link DatabaseConnector}</code>. Defines the output that is returned
     * by executing the DELETE query with the given parameters as the given
     * returnValue.
     *
     * @param mockDbConn    the mock object to which a DELETE query is added
     * @param table         the name of the table to delete from; cannot be
     *                      <code>null</code>
     * @param selection     the array with conditions; can be <code>null</code> to
     *                      delete all records in table
     * @param selectionArgs the array with the corresponding values for the
     *                      selection; can be <code>null</code> if no parameter
     *                      values are needed
     * @param returnValue   <code>true</code> if the deletion should be mocked as
     *                      successful; <code>false</code> otherwise
     */
    public void addMockDeleteQuery(DatabaseConnector mockDbConn, String table, String selection, String[] selectionArgs,
                                   boolean returnValue) {
        // specify when-then-return structure
        when(mockDbConn.executeDeleteQuery(table, selection, selectionArgs)).thenReturn(returnValue);
    }

    private void addMockInsertQuery(DatabaseConnector mockDbConn, String table, String[] columns, String[][] values, boolean returnValue) {
        for (String[] value : values) {
            when(mockDbConn.executeInsertQuery(
                    eq(table),
                    eq(columns),
                    eq(value)
            )).thenReturn(returnValue);
        }
    }

    // ---------------------------------- END ----------------------------------

    /**
     * The implemented endpoints of the class RestServer which should be tested in
     * this class are all highly dependent on the class DatabaseConnector. Therefore,
     * it is again necessary to use mock objects in order to be sure to only test
     * the functionality of the RestServer. This is the same approach as explained
     * in the class RestClientTest however in this test class the creation of the
     * mock objects is slightly different.
     * <p>
     * For example if we want to test the endpoint /cinemas (without query
     * parameters) we only need to simulate one SELECT query on the database which
     * is done by using the method createAndAssignMockObjectSelectQuery. Here, we
     * need to make sure that the correct parameters for the SELECT query, that will
     * be later used in the RestServer, are passed to the method.
     * <p>
     * Afterwards, we can normally proceed by making the actual request to the
     * RestServer endpoint and asserting that the response contains the correct
     * data.
     * <p>
     * Again, in principle the structure of all test cases is the same for every
     * endpoint of the REST server:
     * <p>
     * 1. Setup mock object using predefined methods (possibly calling more than one
     * if multiple queries on the database are made).
     * 2. Make actual request to REST server.
     * 3. Assert that returned response has the correct status and the data is equal
     * to expected result.
     * <p>
     * Because of this, comments that describe which steps are performed, will only
     * be provided within the first method.
     */

// ------------------------------------------------------------------------------------------------------------------------
// DATE REQUESTS

    /**
     * Tests retrieval of all dates from the database.
     * Verifies successful response with status 200 and correct data returned.
     */
    // no query parameters
    @Test
    public void testGetAllDates() {
        // create mock object for DatabaseConnector
        createAndAssignMockObjectSelectQuery(new String[]{"*"}, new String[]{DatabaseConnector.DATES}, null, null,
                null, true, "testSelect", "selectItem");

        // make request to REST client
        HttpResponse<JsonNode> response = Unirest.get("/dates").asJson();

        // assert the response has expected status and data
        assertEquals(200, response.getStatus());
        assertEquals("selectItem", response.getBody().getArray().getJSONObject(0).getString("testSelect"));
    }

    /**
     * Tests retrieval of a specific date by ID.
     * Verifies successful response with status 200 and correct date data.
     */
    // with query parameter dateId
    @Test
    public void testGetDatesWithCorrectId() {
        createAndAssignMockObjectSelectQuery(new String[]{"date"}, new String[]{DatabaseConnector.DATES}, null,
                "id = ?", new String[]{"1"}, true, "testSelect", "selectItem");

        HttpResponse<JsonNode> response = Unirest.get("/dates").queryString("dateId", "1").asJson();
        assertEquals(200, response.getStatus());
        assertEquals("selectItem", response.getBody().getArray().getJSONObject(0).getString("testSelect"));
    }

    /**
     * Tests error handling when requesting a non-existent date ID.
     * Verifies 404 status and appropriate error message.
     */
    @Test
    public void testGetDatesWithInvalidId() {
        createAndAssignMockObjectSelectQuery(new String[]{"date"}, new String[]{DatabaseConnector.DATES}, null,
                "id = ?", new String[]{"1"}, true, "testSelect", "selectItem");

        HttpResponse<JsonNode> response = Unirest.get("/dates").queryString("dateId", "10").asJson();
        assertEquals(404, response.getStatus());
        assertEquals("Date with id 10 not found.", response.getBody().getArray().getString(0));
    }

    @Test
    public void testGetDatesWithIdWrongDatatype() {
        HttpResponse<JsonNode> response = Unirest.get("/dates").queryString("dateId", "abcd").asJson();
        assertEquals(400, response.getStatus());
        assertEquals("Id must be an integer and greater than 0.", response.getBody().getArray().getString(0));
    }

// ------------------------------------------------------------------------------------------------------------------------
// SPACE COMPANY REQUESTS

    /**
     * Tests retrieval of all space companies from the database.
     * Verifies successful response with status 200 and correct company data.
     */
    // no query parameters
    @Test
    public void testGetAllSpaceCompanies() {
        createAndAssignMockObjectSelectQuery(new String[]{"*"}, new String[]{DatabaseConnector.COMPANIES}, null,
                null, null, true, "testSelect", "selectItem");
        HttpResponse<JsonNode> response = Unirest.get("/companies").asJson();
        assertEquals(200, response.getStatus());
        assertEquals("selectItem", response.getBody().getArray().getJSONObject(0).getString("testSelect"));
    }

    // with query parameter companyId
    @Test
    public void testGetSpaceCompaniesWithCorrectId() {
        createAndAssignMockObjectSelectQuery(new String[]{"*"}, new String[]{DatabaseConnector.COMPANIES}, null,
                "id = ?", new String[]{"1"}, true, "testSelect", "selectItem");
        HttpResponse<JsonNode> response = Unirest.get("/companies").queryString("companyId", "1").asJson();
        assertEquals(200, response.getStatus());
        assertEquals("selectItem", response.getBody().getArray().getJSONObject(0).getString("testSelect"));
    }

    @Test
    public void testGetSpaceCompaniesWithInvalidId() {
        createAndAssignMockObjectSelectQuery(new String[]{"*"}, new String[]{DatabaseConnector.COMPANIES}, null,
                "id = ?", new String[]{"1"}, true, "testSelect", "selectItem");
        HttpResponse<JsonNode> response = Unirest.get("/companies").queryString(StringNames.companyId, "10").asJson();
        assertEquals(404, response.getStatus());
        assertEquals("Company with id 10 not found.", response.getBody().getArray().getString(0));
    }

    @Test
    public void testGetSpaceCompaniesWithIdWrongDatatype() {
        HttpResponse<JsonNode> response = Unirest.get("/companies").queryString(StringNames.companyId, "abcd").asJson();
        assertEquals(400, response.getStatus());
        assertEquals("Id must be an integer and greater than 0.", response.getBody().getArray().getString(0));
    }

// ------------------------------------------------------------------------------------------------------------------------
// SPACE FLIGHT REQUESTS

    // no query parameters
    @Test
    public void testGetAllSpaceFlights() {
        createAndAssignMockObjectSelectQuery(new String[]{"*"}, new String[]{DatabaseConnector.FLIGHTS}, null,
                null, null, true, "testSelect", "selectItem");
        HttpResponse<JsonNode> response = Unirest.get("/flights").asJson();
        assertEquals(200, response.getStatus());
        assertEquals("selectItem", response.getBody().getArray().getJSONObject(0).getString("testSelect"));
    }

    /**
     * Tests retrieval of scheduled flights for a specific company.
     * Verifies successful response with status 200 and correct flight data.
     */
    // with query parameter companyId
    @Test
    public void testGetAllSpaceFlightsOfOneCompanyWithCorrectId() {
        createAndAssignMockObjectSelectQuery(
                new String[]{"fs.id as flightScheduleId", "f.id as flightId", "f.name", "f.flight_duration", "f.view_type"},
                new String[]{DatabaseConnector.FLIGHT_SCHEDULES, DatabaseConnector.FLIGHTS,
                        DatabaseConnector.FLIGHT_DATES, DatabaseConnector.DATES},
                new String[]{"fs", "f", "fd", "d"},
                "fs.companyId = ? and fs.flightId = f.id and fs.id = fd.flightScheduleId and fd.dateId = d.id and fd.available_seats > 0 and (( d.date = CURDATE() and fs.launch_time > CURTIME() ) OR d.date > CURDATE())",
                new String[]{"1"}, true, "testSelect", "selectItem");
        HttpResponse<JsonNode> response = Unirest.get("/flights").queryString(StringNames.companyId, "1").asJson();
        assertEquals(200, response.getStatus());
        assertEquals("selectItem", response.getBody().getArray().getJSONObject(0).getString("testSelect"));
    }

    @Test
    public void testGetAllSpaceFlightsOfOneCompanyWithInvalidId() {
        createAndAssignMockObjectSelectQuery(
                new String[]{"fs.id as flightScheduleId", "f.id as flightId", "f.name", "f.flight_duration", "f.view_type"},
                new String[]{DatabaseConnector.FLIGHT_SCHEDULES, DatabaseConnector.FLIGHTS,
                        DatabaseConnector.FLIGHT_DATES, DatabaseConnector.DATES},
                new String[]{"fs", "f", "fd", "d"},
                "fs.companyId = ? and fs.flightId = f.id and fs.id = fd.flightScheduleId and fd.dateId = d.id and fd.available_seats > 0 and (( d.date = CURDATE() and fs.launch_time > CURTIME() ) OR d.date > CURDATE())",
                new String[]{"1"}, true, "testSelect", "selectItem");
        HttpResponse<JsonNode> response = Unirest.get("/flights").queryString(StringNames.companyId, "10").asJson();
        assertEquals(404, response.getStatus());
        assertEquals("Given id(s) not found or no entries with this id(s).",
                response.getBody().getArray().getString(0));
    }

    @Test
    public void testGetAllSpaceFlightsOfOneCompanyWithIdWrongDatatype() {
        HttpResponse<JsonNode> response = Unirest.get("/flights").queryString(StringNames.companyId, "abcd").asJson();
        assertEquals(400, response.getStatus());
        assertEquals("Id(s) must be integer and greater than 0.", response.getBody().getArray().getString(0));
    }

    // with query parameter flightId
    @Test
    public void testGetAllCompaniesOfOneFlightWithCorrectId() {
        createAndAssignMockObjectSelectQuery(new String[]{"c.*"},
                new String[]{DatabaseConnector.FLIGHT_SCHEDULES, DatabaseConnector.COMPANIES},
                new String[]{"fs", "c"}, "fs.flightId = ? AND fs.companyId = c.id", new String[]{"1"}, true,
                "testSelect", "selectItem");
        HttpResponse<JsonNode> response = Unirest.get("/flights").queryString(StringNames.flightId, "1").asJson();
        assertEquals(200, response.getStatus());
        assertEquals("selectItem", response.getBody().getArray().getJSONObject(0).getString("testSelect"));
    }

    // with query parameters companyId and flightId
    @Test
    public void testGetInfoOfOneFlightInOneCompanyWithCorrectIds() {
        createAndAssignMockObjectSelectQuery(
                new String[]{"fd.id as flightDateId", "fs.launch_time", "d.date", "f.name", "f.flight_duration", "f.view_type"},
                new String[]{DatabaseConnector.FLIGHT_SCHEDULES, DatabaseConnector.FLIGHT_DATES,
                        DatabaseConnector.DATES, DatabaseConnector.FLIGHTS},
                new String[]{"fs", "fd", "d", "f"},
                "f.id = ? and f.id = fs.flightId and fs.companyId = ? and fs.id = fd.flightScheduleId and fd.dateId = d.id and fd.available_seats > 0 and "
                        + "(( d.date = CURDATE() and fs.launch_time > CURTIME()) OR d.date > CURDATE())",
                new String[]{"1", "2"}, true, "testSelect", "selectItem");
        HttpResponse<JsonNode> response = Unirest.get("/flights").queryString(StringNames.flightId, "1")
                .queryString(StringNames.companyId, "2").asJson();
        assertEquals(200, response.getStatus());
        assertEquals("selectItem", response.getBody().getArray().getJSONObject(0).getString("testSelect"));
    }

// ------------------------------------------------------------------------------------------------------------------------
// FLIGHT SCHEDULES REQUESTS

    /**
     * Tests retrieval of all flight schedules from the database.
     * Verifies successful response with status 200 and correct schedule data.
     */
    // no query parameters
    @Test
    public void testGetAllFlightSchedules() {
        createAndAssignMockObjectSelectQuery(new String[]{"*"}, new String[]{DatabaseConnector.FLIGHT_SCHEDULES},
                null, null, null, true, "testSelect", "selectItem");
        HttpResponse<JsonNode> response = Unirest.get("/schedules").asJson();
        assertEquals(200, response.getStatus());
        assertEquals("selectItem", response.getBody().getArray().getJSONObject(0).getString("testSelect"));
    }

    // with query parameter flightDateId
    @Test
    public void testGetOneScheduleWithCorrectId() {
        createAndAssignMockObjectSelectQuery(new String[]{"*"}, new String[]{DatabaseConnector.FLIGHT_DATES},
                null, "id = ?", new String[]{"1"}, true, "testSelect", "selectItem");
        HttpResponse<JsonNode> response = Unirest.get("/schedules").queryString(StringNames.flightDateId, "1").asJson();
        assertEquals(200, response.getStatus());
        assertEquals("selectItem", response.getBody().getArray().getJSONObject(0).getString("testSelect"));
    }

    @Test
    public void testGetOneScheduleWithInvalidId() {
        createAndAssignMockObjectSelectQuery(new String[]{"*"}, new String[]{DatabaseConnector.FLIGHT_DATES},
                null, "id = ?", new String[]{"1"}, true, "testSelect", "selectItem");
        HttpResponse<JsonNode> response = Unirest.get("/schedules").queryString(StringNames.flightDateId, "10").asJson();
        assertEquals(404, response.getStatus());
        assertEquals("Schedule with id 10 not found or no entries with this id.",
                response.getBody().getArray().getString(0));
    }

    @Test
    public void testGetOneScheduleWithIdWrongDatatype() {
        HttpResponse<JsonNode> response = Unirest.get("/schedules").queryString(StringNames.flightDateId, "abcd").asJson();
        assertEquals(400, response.getStatus());
        assertEquals("Id must be an integer and greater than 0.", response.getBody().getArray().getString(0));
    }

    // with query parameters flightDateId and info
    @Test
    public void testGetOneScheduleAndInfoWithCorrectId() {
        createAndAssignMockObjectSelectQuery(
                new String[]{"c.name as companyName", "f.name as flightName", "fs.launch_time", "d.date",
                        "fd.id as flightDateId", "f.id as flightId", "c.id as companyId"},
                new String[]{DatabaseConnector.FLIGHT_SCHEDULES, DatabaseConnector.FLIGHT_DATES,
                        DatabaseConnector.COMPANIES, DatabaseConnector.FLIGHTS, DatabaseConnector.DATES},
                new String[]{"fs", "fd", "c", "f", "d"},
                "fd.id = ? and fd.flightScheduleId = fs.id and fd.dateId = d.id and c.id = fs.companyId and f.id = fs.flightId",
                new String[]{"1"}, true, "testSelect", "selectItem");
        HttpResponse<JsonNode> response = Unirest.get("/schedules").queryString(StringNames.flightDateId, "1")
                .queryString(StringNames.info, "true").asJson();
        assertEquals(200, response.getStatus());
        assertEquals("selectItem", response.getBody().getArray().getJSONObject(0).getString("testSelect"));
    }

// ------------------------------------------------------------------------------------------------------------------------
// CUSTOMER REQUESTS

    /**
     * Tests user authentication and information retrieval.
     * Verifies successful response with status 200 when valid credentials are provided.
     */
    // with query parameter email
    @Test
    public void testGetUserInformationWithValidAuthorizationAndCredentials() {
        DatabaseConnector mockDbConn = createAndAssignMockObjectSelectQuery(new String[]{"*"},
                new String[]{DatabaseConnector.CUSTOMERS}, null, "email = ?", new String[]{"email@test.de"}, true,
                "id", 1);

        addMockSelectQuery(mockDbConn, new String[]{"*"}, new String[]{DatabaseConnector.CUSTOMERS}, null,
                "id = ? and email = ? and password = ?", new String[]{"1", "email@test.de", "testPassword"},
                new String[]{"key"}, new Object[]{"value"});

        String authorization = "Basic " + Base64.getEncoder().encodeToString("email@test.de:testPassword".getBytes());
        HttpResponse<JsonNode> response = Unirest.get("/customers").queryString(StringNames.email, "email@test.de")
                .header(StringNames.authorization, authorization).asJson();

        assertEquals(200, response.getStatus());
        assertEquals(1, response.getBody().getArray().getJSONObject(0).getInt("id"));
    }

// ------------------------------------------------------------------------------------------------------------------------
// RESERVATION REQUESTS

    /**
     * Tests successful reservation retrieval with valid authentication.
     * Verifies response status 200 and correct reservation data.
     */
    @Test
    public void testGetReservationsWithValidAuthorizationAndParameter() {
        DatabaseConnector mockDbConn = createAndAssignMockObjectSelectQuery(new String[]{"*"},
                new String[]{DatabaseConnector.RESERVATIONS}, null, "customerId = ?", new String[]{"1"}, true,
                "testSelect", "selectItem");

        addMockSelectQuery(mockDbConn, new String[]{"*"}, new String[]{DatabaseConnector.CUSTOMERS}, null,
                "id = ? and email = ? and password = ?", new String[]{"1", "email@test.de", "testPassword"},
                new String[]{"key"}, new Object[]{"value"});

        String authorization = "Basic " + Base64.getEncoder().encodeToString("email@test.de:testPassword".getBytes());
        HttpResponse<JsonNode> response = Unirest.get("/reservations").queryString(StringNames.customerId, "1")
                .header(StringNames.authorization, authorization).asJson();

        assertEquals(200, response.getStatus());
        assertEquals("selectItem", response.getBody().getArray().getJSONObject(0).getString("testSelect"));
    }

    // CREATE RESERVATION

    // Sprint 3: the base testing case
    // Sprint 4: modify with added "seatNumbers" in the reservation info
    /**
     * Tests successful creation of a new reservation with valid authentication.
     * Verifies seat allocation, token updates, and reservation confirmation.
     * Validates handling of seat numbers and updates to available seats.
     */
    @Test
    public void testCreateReservationsWithValidAuthorizationAndParameters() {
        DatabaseConnector mockDbConn = createAndAssignMockObjectInsertQuery(
                DatabaseConnector.RESERVATIONS,
                new String[]{"customerId", "flightDateId", "reserved_seats"},
                new String[]{"1", "2", "9"},
                true
        );

        // Mock flight date validation
        addMockSelectQuery(mockDbConn,
                new String[]{"*"},
                new String[]{DatabaseConnector.FLIGHT_DATES},
                null,
                "id = ?",
                new String[]{"2"},
                new String[]{"id"},
                new Object[]{2}
        );

        // Mock seats availability check
        addMockSelectQuery(mockDbConn,
                new String[]{"*"},
                new String[]{DatabaseConnector.FLIGHT_DATES},
                null,
                "id = ?",
                new String[]{"2"},
                new String[]{"available_seats"},
                new Object[]{100}
        );

        // Mock authorization check
        addMockSelectQuery(mockDbConn,
                new String[]{"*"},
                new String[]{DatabaseConnector.CUSTOMERS},
                null,
                "id = ? and email = ? and password = ?",
                new String[]{"1", "email@test.de", "testPassword"},
                new String[]{"key"},
                new Object[]{"value"}
        );

        // Mock getting newly created reservation ID
        addMockSelectQuery(mockDbConn,
                new String[]{"id"},
                new String[]{DatabaseConnector.RESERVATIONS},
                null,
                "customerId = ? AND flightDateId = ? ORDER BY id DESC LIMIT 1",
                new String[]{"1", "2"},
                new String[]{"id"},
                new Object[]{1}
        );

        // Mock seats update
        addMockUpdateQuery(mockDbConn,
                DatabaseConnector.FLIGHT_DATES,
                new String[]{"available_seats = available_seats - ?"},
                new String[]{"9"},
                "id = ?",
                new String[]{"2"},
                true
        );

        // Mock tokens update
        addMockUpdateQuery(mockDbConn,
                DatabaseConnector.CUSTOMERS,
                new String[]{"tokens = tokens + ?"},
                new String[]{"9"},
                "id = ?",
                new String[]{"1"},
                true
        );

        // Mock seat numbers insertion
        addMockInsertQuery(mockDbConn,
                DatabaseConnector.SEAT_NUMBERS,
                new String[]{"reservationId", "seat_number"},
                new String[][]{
                        {"1", "1"},
                        {"1", "2"},
                        {"1", "3"},
                        {"1", "4"},
                        {"1", "5"},
                        {"1", "6"},
                        {"1", "7"},
                        {"1", "8"},
                        {"1", "9"}
                },
                true
        );

        String authorization = "Basic " + Base64.getEncoder().encodeToString("email@test.de:testPassword".getBytes());
        HttpResponse<JsonNode> response = Unirest.post("/reservation/create")
                .queryString(StringNames.customerId, "1")
                .queryString(StringNames.flightDateId, "2")
                .queryString(StringNames.reservedSeats, "9")
                .queryString(StringNames.seatNumbers, "1,2,3,4,5,6,7,8,9")
                .header(StringNames.authorization, authorization)
                .asJson();

        assertEquals(201, response.getStatus());
        assertEquals(
                "Reservation created",
                response.getBody().getObject().getString("message")  
        );
    }

    // MODIFY RESERVATION

    // Sprint 3: the base testing case
    // Sprint 4: modify with added "seatNumbers" in the reservation info
    /**
     * Tests successful modification of an existing reservation.
     * Verifies seat number updates, available seat counts, and authorization.
     */
    @Test
    public void testModifyReservationsWithValidAuthorizationAndParameters() {
        DatabaseConnector mockDbConn = createAndAssignMockObjectSelectQuery(
                new String[]{"*"},
                new String[]{DatabaseConnector.CUSTOMERS},
                null,
                "id = ? and email = ? and password = ?",
                new String[]{"2", "email@test.de", "testPassword"},
                true,
                "key",
                "value"
        );

        // Mock existing reservation query
        addMockSelectQuery(mockDbConn,
                new String[]{"*"},
                new String[]{DatabaseConnector.RESERVATIONS},
                null,
                "id = ?",
                new String[]{"1"},
                new String[]{"customerId", "reserved_seats", "flightDateId"},
                new Object[]{2, 5, 3}
        );

        // Mock seat numbers query
        addMockSelectQuery(mockDbConn,
                new String[]{"seat_number"},
                new String[]{DatabaseConnector.SEAT_NUMBERS},
                null,
                "reservationId = ?",
                new String[]{"1"},
                new String[]{"seat_number"},
                new Object[]{1, 2, 3, 4, 5}
        );

        // Mock available seats query
        addMockSelectQuery(mockDbConn,
                new String[]{"*"},
                new String[]{DatabaseConnector.FLIGHT_DATES},
                null,
                "id = ?",
                new String[]{"3"},
                new String[]{"available_seats"},
                new Object[]{100}
        );

        // Mock update queries
        addMockUpdateQuery(mockDbConn,
                DatabaseConnector.RESERVATIONS,
                new String[]{"reserved_seats = ?"},
                new String[]{"8"},
                "id = ?",
                new String[]{"1"},
                true
        );

        addMockUpdateQuery(mockDbConn,
                DatabaseConnector.FLIGHT_DATES,
                new String[]{"available_seats = available_seats + ?"},
                new String[]{"-3"},
                "id = ?",
                new String[]{"3"},
                true
        );

        // Mock seat numbers deletion and insertion
        addMockDeleteQuery(mockDbConn,
                DatabaseConnector.SEAT_NUMBERS,
                "reservationId = ?",
                new String[]{"1"},
                true
        );

        // Mock seat numbers deletion
        addMockDeleteQuery(mockDbConn,
                DatabaseConnector.SEAT_NUMBERS,
                "reservationId = ?",
                new String[]{"1"},
                true
        );
        
        addMockInsertQuery(mockDbConn,
                DatabaseConnector.SEAT_NUMBERS,
                new String[]{"reservationId", "seat_number"},
                new String[][]{
                        {"1", "1"},
                        {"1", "2"},
                        {"1", "3"},
                        {"1", "4"},
                        {"1", "5"},
                        {"1", "6"},
                        {"1", "7"},
                        {"1", "8"}
                },
                true
        );

        String authorization = "Basic " + Base64.getEncoder().encodeToString("email@test.de:testPassword".getBytes());
        HttpResponse<JsonNode> response = Unirest.put("/reservation/modify")
                .queryString(StringNames.reservationId, "1")
                .queryString(StringNames.reservedSeats, "8")
                .queryString(StringNames.seatNumbers, "1,2,3,4,5,6,7,8")
                .header(StringNames.authorization, authorization)
                .asJson();

        assertEquals(200, response.getStatus());
        assertEquals(
                "Reservation updated",
                response.getBody().getObject().getString("message")  
        );
    }

    // DELETE RESERVATION

    /**
     * Tests reservation deletion attempt without query parameters.
     * Verifies 400 status and appropriate error message.
     */
    // no query parameters (invalid)
    @Test
    public void testDeleteReservationsWithNoQueryParameters() {
        HttpResponse<JsonNode> response = Unirest.delete("/reservation/delete").asJson();
        assertEquals(400, response.getStatus());
        
        assertEquals(
                "Valid reservation ID required",
                response.getBody().getObject().getString("error")  
        );
    }

    /**
     * Tests successful reservation deletion with valid authorization.
     * Verifies proper updates to seat availability and reservation removal.
     */
    // Sprint 4: Using new overloaded method of select query to fetch new Reservation data model
    // with all query parameters
    @Test
    public void testDeleteReservationsWithValidAuthorizationAndParameters() {
        Map<String, Object> reservationFields = new HashMap<>();
        reservationFields.put("customerId", 2);
        reservationFields.put("reserved_seats", 5);
        reservationFields.put("flightDateId", 3);

        // Mock the initial reservation query
        DatabaseConnector mockDbConn = createAndAssignMockObjectSelectQuery(
                new String[]{"*"},                          // projection
                new String[]{DatabaseConnector.RESERVATIONS}, // tables
                null,                                       // tableAlias
                "id = ?",                                   // selection
                new String[]{"1"},                          // selectionArgs
                true,                                       // addListItem
                reservationFields                           // fieldMap
        );

        // Mock authorization check
        addMockSelectQuery(
            mockDbConn,
            new String[]{"*"},
            new String[]{DatabaseConnector.CUSTOMERS},
            null,
            "id = ? and email = ? and password = ?",
            new String[]{"2", "email@test.de", "testPassword"},
            new String[]{"id", "email"},
            new Object[]{2, "email@test.de"}
        );

        // Mock reservation details query
        addMockSelectQuery(
            mockDbConn,
            new String[]{"reserved_seats", "flightDateId"},
            new String[]{DatabaseConnector.RESERVATIONS},
            null,
            "id = ?",
            new String[]{"1"},
            new String[]{"reserved_seats", "flightDateId"},
            new Object[]{5, 3}
        );

        // Mock delete query
        addMockDeleteQuery(
            mockDbConn,
            DatabaseConnector.RESERVATIONS,
            "id = ?",
            new String[]{"1"},
            true
        );

        // Mock update available seats
        addMockUpdateQuery(
            mockDbConn,
            DatabaseConnector.FLIGHT_DATES,
            new String[]{"available_seats = available_seats + ?"},
            new String[]{"5"},
            "id = ?",
            new String[]{"3"},
            true
        );

        String authorization = "Basic " + Base64.getEncoder().encodeToString("email@test.de:testPassword".getBytes());
        HttpResponse<JsonNode> response = Unirest.delete("/reservation/delete")
                .queryString(StringNames.reservationId, "1")
                .header(StringNames.authorization, authorization)
                .asJson();

        assertEquals(201, response.getStatus());
        assertEquals(
                "Reservation deleted",
                response.getBody().getObject().getString("message")  
        );
    }

    @Test
    public void testDeleteReservationsWithReservationIdWrongDatatype() {
        String authorization = "Basic " + Base64.getEncoder().encodeToString("email@test.de:testPassword".getBytes());
        HttpResponse<JsonNode> response = Unirest.delete("/reservation/delete").queryString(StringNames.reservationId, "abcd")
                .header(StringNames.authorization, authorization).asJson();

        assertEquals(400, response.getStatus());
        
        assertEquals(
                "Valid reservation ID required",
                response.getBody().getObject().getString("error")  
        );
    }

    @Test
    public void testDeleteReservationsWithoutAuthorization() {
        DatabaseConnector mockDbConn = createAndAssignMockObjectSelectQuery(new String[]{"*"},
                new String[]{DatabaseConnector.RESERVATIONS}, null, "id = ?", new String[]{"1"}, true,
                "customerId", "2");
        // for checking authorization
        addMockSelectQuery(mockDbConn, new String[]{"*"}, new String[]{DatabaseConnector.CUSTOMERS}, null,
                "id = ? and email = ? and password = ?", new String[]{"2", "email@test.de", "testPassword"},
                new String[]{"key"}, new Object[]{"value"});

        HttpResponse<JsonNode> response = Unirest.delete("/reservation/delete").queryString(StringNames.reservationId, "1")
                .asJson();

        assertEquals(401, response.getStatus());
        
        assertEquals(
                "User is not authorized to perform this action.",
                response.getBody().getObject().getString("error")  
        );
    }

    @Test
    public void testDeleteReservationsWithInvalidAuthorization() {
        DatabaseConnector mockDbConn = createAndAssignMockObjectSelectQuery(new String[]{"*"},
                new String[]{DatabaseConnector.RESERVATIONS}, null, "id = ?", new String[]{"1"}, true,
                "customerId", "2");
        // for checking authorization
        addMockSelectQuery(mockDbConn, new String[]{"*"}, new String[]{DatabaseConnector.CUSTOMERS}, null,
                "id = ? and email = ? and password = ?", new String[]{"2", "email@test.de", "testPassword"},
                new String[]{"key"}, new Object[]{"value"});

        String authorization = "Basic " + Base64.getEncoder().encodeToString("email@test.de:wrongPassword".getBytes());
        HttpResponse<JsonNode> response = Unirest.delete("/reservation/delete")
                .queryString(StringNames.reservationId, "1")
                .header(StringNames.authorization, authorization)
                .asJson();

        assertEquals(401, response.getStatus());
        assertEquals(
                "User is not authorized to perform this action.",
                response.getBody().getObject().getString("error")  
        );
    }

    @Test
    public void testDeleteReservationsWithInvalidReservationId() {
        createAndAssignMockObjectSelectQuery(new String[]{"customerId"},
                new String[]{DatabaseConnector.RESERVATIONS}, null, "id = ?", new String[]{"5"}, true,
                "customerId", "2");

        String authorization = "Basic " + Base64.getEncoder().encodeToString("email@test.de:testPassword".getBytes());
        HttpResponse<JsonNode> response = Unirest.delete("/reservation/delete").queryString(StringNames.reservationId, "1")
                .header(StringNames.authorization, authorization).asJson();

        assertEquals(404, response.getStatus());
        
        assertEquals(
                "Reservation not found",
                response.getBody().getObject().getString("error")  
        );
    }

    // Sprint 3: the base testing case
    // Sprint 4: modify with added "seatNumbers" in the reservation info
    @Test
    public void testRedeemTokensWithValidAuthorization() {
        // For checking authorization and current tokens
        DatabaseConnector mockDbConn = createAndAssignMockObjectSelectQuery(
            new String[]{"*"},
            new String[]{DatabaseConnector.CUSTOMERS},
            null,
            "id = ? and email = ? and password = ?",
            new String[]{"1", "test@email.com", "password"},
            true,
            "tokens",
            10
        );

        // Mock the update query for tokens
        addMockUpdateQuery(
            mockDbConn,
            DatabaseConnector.CUSTOMERS,
            new String[]{"tokens = tokens - ?"},
            new String[]{"5"},
            "id = ?",
            new String[]{"1"},
            true
        );

        // Mock the select query to verify tokens after update
        addMockSelectQuery(
            mockDbConn,
            new String[]{"tokens"},
            new String[]{DatabaseConnector.CUSTOMERS},
            null,
            "id = ?",
            new String[]{"1"},
            new String[]{"tokens"},
            new Object[]{5}  // Updated token value after redemption
        );

        String authorization = "Basic " + Base64.getEncoder().encodeToString("test@email.com:password".getBytes());
        HttpResponse<JsonNode> response = Unirest.put("/tokens/redeem")
            .queryString(StringNames.customerId, "1")
            .queryString(StringNames.tokens, "5")
            .header(StringNames.authorization, authorization)
            .asJson();

        assertEquals(200, response.getStatus());
        assertEquals("Tokens redeemed successfully", response.getBody().getArray().getString(0));
    }

    @Test
    public void testRedeemTokensWithInsufficientTokens() {
        DatabaseConnector mockDbConn = createAndAssignMockObjectSelectQuery(
            new String[]{"*"},
            new String[]{DatabaseConnector.CUSTOMERS},
            null,
            "id = ? and email = ? and password = ?",
            new String[]{"1", "test@email.com", "password"},
            true,
            "tokens",
            3  // User only has 3 tokens
        );

        // Mock token check query
        addMockSelectQuery(
            mockDbConn,
            new String[]{"tokens"},
            new String[]{DatabaseConnector.CUSTOMERS},
            null,
            "id = ?",
            new String[]{"1"},
            new String[]{"tokens"},
            new Object[]{3}  // Confirm user has insufficient tokens
        );

        String authorization = "Basic " + Base64.getEncoder().encodeToString("test@email.com:password".getBytes());
        HttpResponse<JsonNode> response = Unirest.put("/tokens/redeem")
            .queryString(StringNames.customerId, "1")
            .queryString(StringNames.tokens, "5")  // Trying to redeem 5 tokens
            .header(StringNames.authorization, authorization)
            .asJson();

        assertEquals(400, response.getStatus());
        assertEquals("Not enough tokens available", response.getBody().getArray().getString(0));
    }

    // SCHEDULE BY DATE REQUESTS
    // Requirement 1
    // Sprint 4: Using new overloaded method of select query to fetch new Reservation data model
    @Test
    public void testGetSchedulesByDateWithValidDateId() {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("flightName", "Test Flight");
        resultMap.put("flight_duration", 120);
        resultMap.put("view_type", "Europe");
        resultMap.put("companyName","Test Company");
        resultMap.put("launch_time","14:00");
        resultMap.put("available_seats",1);
        resultMap.put("flightDateId",1);
        resultMap.put("companyId",1);
        resultMap.put("flightId",1);
        createAndAssignMockObjectSelectQuery(
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
                new String[]{"1"},
                true,
                resultMap
        );

        HttpResponse<JsonNode> response = Unirest.get("/schedules/by-date")
                .queryString("dateId", "1")
                .asJson();

        assertEquals(200, response.getStatus());
        JSONObject result = response.getBody().getArray().getJSONObject(0);
        assertEquals("Test Flight", result.getString("flightName"));
        assertEquals("Test Company", result.getString("companyName"));
    }

    // Requirement 1
    /**
     * Tests schedule retrieval attempt without providing date ID.
     * Verifies 400 status and appropriate error message.
     */
    @Test
    public void testGetSchedulesByDateWithoutDateId() {
        HttpResponse<JsonNode> response = Unirest.get("/schedules/by-date").asJson();

        assertEquals(400, response.getStatus());
        assertEquals("Date ID is required", response.getBody().getArray().getString(0));
    }

    // Requirement 1
    @Test
    public void testGetSchedulesByDateWithInvalidDateId() {
        HttpResponse<JsonNode> response = Unirest.get("/schedules/by-date")
                .queryString("dateId", "-1")
                .asJson();

        assertEquals(400, response.getStatus());
        assertEquals("Invalid date ID format. Must be a positive integer",
                response.getBody().getArray().getString(0));
    }

    // Requirement 1
    @Test
    public void testGetSchedulesByDateWithNoFlights() {
        createAndAssignMockObjectSelectQuery(
                new String[]{
                        "f.name as flightName",
                        "f.flight_duration",
                        "f.view_type",
                        "c.name as companyName",
                        "fs.launch_time",
                        "fd.available_seats",
                        "fd.id as flightDateId"
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
                new String[]{"1"},
                false,
                null,
                null
        );

        HttpResponse<JsonNode> response = Unirest.get("/schedules/by-date")
                .queryString("dateId", "1")
                .asJson();

        assertEquals(404, response.getStatus());
        assertEquals("No available flights found for the specified date ID",
                response.getBody().getArray().getString(0));
    }

    // Requirement 1
    @Test
    public void testGetSchedulesByDateWithMalformedDateId() {
        HttpResponse<JsonNode> response = Unirest.get("/schedules/by-date")
                .queryString("dateId", "abc123")
                .asJson();

        assertEquals(400, response.getStatus());
        assertEquals("Invalid date ID format. Must be a positive integer",
                response.getBody().getArray().getString(0));
    }

    // Requirement 1
    @Test
    public void testGetSchedulesByDateWithZeroDateId() {
        HttpResponse<JsonNode> response = Unirest.get("/schedules/by-date")
                .queryString("dateId", "0")
                .asJson();

        assertEquals(400, response.getStatus());
        assertEquals("Invalid date ID format. Must be a positive integer",
                response.getBody().getArray().getString(0));
    }

    // --------------------------------
    // Requirement 1
    // DATES WITH FLIGHTS REQUESTS
    /**
     * Tests successful retrieval of dates with available flights.
     * Verifies response includes correct date IDs and flight information.
     */
    @Test
    public void testGetDatesWithFlightsSuccess() {
        createAndAssignMockObjectSelectQuery(
                new String[]{"DISTINCT d.id as dateId, d.date"},
                new String[]{
                        DatabaseConnector.DATES,
                        DatabaseConnector.FLIGHT_DATES,
                        DatabaseConnector.FLIGHT_SCHEDULES
                },
                new String[]{"d", "fd", "fs"},
                "fd.dateId = d.id AND fd.flightScheduleId = fs.id " +
                        "AND fd.available_seats > 0 " +
                        "AND (d.date > CURDATE() OR (d.date = CURDATE() AND fs.launch_time > CURTIME()))",
                null,
                true,
                "dateId",
                1
        );

        HttpResponse<JsonNode> response = Unirest.get("/dates-with-flights").asJson();

        assertEquals(200, response.getStatus());
        assertEquals(1, response.getBody().getArray().getJSONObject(0).getInt("dateId"));
    }

    // Requirement 1
    /**
     * Tests retrieval attempt when no flights are available.
     * Verifies 404 status and appropriate error message.
     */
    @Test
    public void testGetDatesWithFlightsNoResults() {
        createAndAssignMockObjectSelectQuery(
                new String[]{"DISTINCT d.id as dateId, d.date"},
                new String[]{
                        DatabaseConnector.DATES,
                        DatabaseConnector.FLIGHT_DATES,
                        DatabaseConnector.FLIGHT_SCHEDULES
                },
                new String[]{"d", "fd", "fs"},
                "fd.dateId = d.id AND fd.flightScheduleId = fs.id " +
                        "AND fd.available_seats > 0 " +
                        "AND (d.date > CURDATE() OR (d.date = CURDATE() AND fs.launch_time > CURTIME()))",
                null,
                false,
                null,
                null
        );

        HttpResponse<JsonNode> response = Unirest.get("/dates-with-flights").asJson();

        assertEquals(404, response.getStatus());
        assertEquals("No dates with available flights found",
                response.getBody().getArray().getString(0));
    }

}
