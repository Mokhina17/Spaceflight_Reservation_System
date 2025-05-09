package rest_client;

import com.google.gson.JsonObject;
import kong.unirest.*;
import model.User;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import utils.StringNames;
import javafx.application.Platform;

import java.util.Base64;
import java.util.List;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
public class RestClientTest {

    // ------------- DO NOT CHANGE THE FOLLOWING PART OF THE CODE -------------
    private static final RestClient client = RestClient.getRestClient(true);
    @Mock
    JsonNode value;
    @Mock
    private GetRequest getRequest;
    @Mock
    private HttpRequestWithBody requestWithBody;
    @Mock
    private HttpResponse<JsonNode> httpResponse;
    private static MockedStatic<Unirest> mockedUnirest;

    @BeforeAll
    public static void initMock() {
        mockedUnirest = mockStatic(Unirest.class);
    }

    @AfterAll
    public static void deregisterMock() { mockedUnirest.close();}
    // ---------------------------------- END ----------------------------------

    /**
     * As you know from the lecture, unit testing aims at only testing one component
     * at a time. The methods of the RestClient class are highly dependent on the
     * implementation of the RestServer. This means, if we are testing one method of
     * RestClient, we are actually also testing the correct behavior of the
     * RestServer. In order to only test for the correct functionality of a
     * RestClient method, we can simulate the RestServer, so that we know what it
     * will always return as a response.
     * <p>
     * This simulation can be achieved by the usage of so-called mock objects. A
     * mock object simulates a specific method that is used in the method which we
     * want to test. The behavior of this mock object is set before it is actually
     * used, and therefore we can always be sure what the result of the execution of
     * this simulated method will be. This is just a very brief explanation of the
     * concept of mock objects. For further information you can refer for example to
     * https://www.vogella.com/tutorials/Mockito/article.html
     * <p>
     * In our case this means, we are creating a mock object, to simulate the
     * response of the server for a specific endpoint. For example if we want to
     * test the method restClient.requestCinemas() we only need to simulate the
     * endpoint /cinemas of the RestServer because this is the only endpoint that is
     * used (this is performed by using the when(...).thenReturn(...) structure as
     * shown in all test cases below).
     * <p>
     * After creating the mock object, we can then perform the actual test by
     * calling the method restClient.requestCinemas() and asserting afterwards that
     * the result returned by the mock object is equal to the result we expect.
     * <p>
     * In principle the structure of all test cases is the same for every REST
     * client method:
     * <p>
     * 1. Setup mock object using when and thenReturn. <br>
     * 2. Make actual request to REST client. <br>
     * 3. Assert that returned result is equal to expected result.
     * <p>
     * Because of this, comments that describe which steps are performed, will only
     * be provided within the first method.
     */

    @Test
    public void testCaseTemplateWithGet() {
        // testGetUserInfoByMailStatus401 in CinemaCase
        String email = "noah.const@web.de";
        client.setUser(new User(email, "9ikwelf%"));
        String auth = "Basic " + Base64.getEncoder().encodeToString("noah.const@web.de:9ikwelf%".getBytes());

        when(Unirest.get("/customers")).thenReturn(getRequest);
        when(getRequest.queryString(StringNames.email, email)).thenReturn(getRequest);
        when(getRequest.header(StringNames.authorization, auth)).thenReturn(getRequest);
        when(getRequest.asJson()).thenReturn(httpResponse);
        when(httpResponse.getStatus()).thenReturn(401);

        assertNull(client.getUserInfoByMail(email));
    }

    @Test
    public void testCaseTemplateWithPost() {
        /*
         * If you need to use another REST method than GET you need to use another
         * procedure to create the mock object. This is shown in the following.
         */
        // testCreateNewUserStatus201 in CinemaCase
        String firstname = "Max";
        String lastname = "Mustermann";
        String email = "max.mt@web.de";
        String password = "dieSonne";

        when(Unirest.post("/customer/create")).thenReturn(requestWithBody);
        when(requestWithBody.queryString(StringNames.firstname, firstname)).thenReturn(requestWithBody);
        when(requestWithBody.queryString(StringNames.lastname, lastname)).thenReturn(requestWithBody);
        when(requestWithBody.queryString(StringNames.email, email)).thenReturn(requestWithBody);
        when(requestWithBody.queryString(StringNames.password, password)).thenReturn(requestWithBody);
        when(requestWithBody.asJson()).thenReturn(httpResponse);
        when(httpResponse.getStatus()).thenReturn(201);

        assertTrue(client.createNewUser(firstname, lastname, email, password));
    }

    /**
     * Tests the retrieval of available dates from the server.
     * Verifies that the date information is correctly parsed and returned.
     */
    // DATE REQUESTS
    @Test
    public void testRequestDates() {
        when(Unirest.get("/dates")).thenReturn(getRequest);
        when(getRequest.asJson()).thenReturn(httpResponse);
        when(httpResponse.getBody()).thenReturn(value);
        when(value.toString()).thenReturn("[{\"id\": 1, \"date\": \"March 22, 2025\"}]");

        List<JsonObject> result = client.requestDates();

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).get("id").getAsInt());
        assertEquals("March 22, 2025", result.get(0).get("date").getAsString());
    }

    // SPACE COMPANY REQUESTS
    /**
     * Tests the retrieval of space company information from the server.
     * Verifies that the company details including location and name are correctly returned.
     */
    @Test
    public void testRequestSpaceCompanies() {
        when(Unirest.get("/companies")).thenReturn(getRequest);
        when(getRequest.asJson()).thenReturn(httpResponse);
        when(httpResponse.getStatus()).thenReturn(200);
        when(httpResponse.getBody()).thenReturn(value);
        when(value.toString())
                .thenReturn("[{\"location\":\"Spaceport America, New Mexico\",\"name\":\"Virgin Galactic\"}]");

        List<JsonObject> result = client.requestSpaceCompanies();
        assertEquals(1, result.size());
        assertEquals("Spaceport America, New Mexico", result.get(0).get("location").getAsString());
        assertEquals("Virgin Galactic", result.get(0).get("name").getAsString());
    }

    /**
     * Tests successful retrieval of detailed company information with status 200.
     * Verifies that company details are correctly parsed from the server response.
     */
    @Test
    public void testRequestCompanyInformationStatus200() {
        int companyId = 1;
        when(Unirest.get("/companies")).thenReturn(getRequest);
        when(getRequest.queryString(StringNames.companyId, companyId)).thenReturn(getRequest);
        when(getRequest.asJson()).thenReturn(httpResponse);
        when(httpResponse.getBody()).thenReturn(value);
        when(value.toString())
                .thenReturn("[{\"location\":\"Spaceport America, New Mexico\",\"name\":\"Virgin Galactic\"}]");
        when(httpResponse.getStatus()).thenReturn(200);

        JsonObject result = client.requestCompanyInformation(companyId);
        assertEquals("Spaceport America, New Mexico", result.get("location").getAsString());
        assertEquals("Virgin Galactic", result.get("name").getAsString());
    }

    /**
     * Tests handling of company information request with error status 400.
     * Verifies that null is returned when the server responds with an error.
     */
    @Test
    public void testRequestCompanyInformationStatus400() {
        int companyId = 1;
        when(Unirest.get("/companies")).thenReturn(getRequest);
        when(getRequest.queryString(StringNames.companyId, companyId)).thenReturn(getRequest);
        when(getRequest.asJson()).thenReturn(httpResponse);
        when(httpResponse.getStatus()).thenReturn(400);

        assertNull(client.requestCompanyInformation(companyId));
    }

    // FLIGHT REQUESTS
    /**
     * Tests successful retrieval of flights for a specific company with status 200.
     * Verifies that flight details including name, duration, and schedule information are correctly parsed.
     */
    @Test
    public void testRequestFlightsOfCompanyStatus200() {
        int companyId = 1;
        when(Unirest.get("/flights")).thenReturn(getRequest);
        when(getRequest.queryString(StringNames.companyId, companyId)).thenReturn(getRequest);
        when(getRequest.asJson()).thenReturn(httpResponse);
        when(httpResponse.getBody()).thenReturn(value);
        when(value.toString()).thenReturn(
                "[{\"name\":\"Europe Journey\",\"flight_duration\":60,\"view_type\":\"Europe\",\"flightScheduleId\":1,\"flightId\":1}]");
        when(httpResponse.getStatus()).thenReturn(200);

        List<JsonObject> result = client.requestFlightsOfCompany(companyId);
        assertEquals(1, result.size());
        assertEquals("Europe Journey", result.get(0).get("name").getAsString());
        assertEquals(60, result.get(0).get("flight_duration").getAsInt());
        assertEquals("Europe", result.get(0).get("view_type").getAsString());
        assertEquals(1, result.get(0).get("flightScheduleId").getAsInt());
        assertEquals(1, result.get(0).get("flightId").getAsInt());
    }

    /**
     * Tests handling of company flights request with error status 400.
     * Verifies that null is returned when the server responds with an error.
     */
    @Test
    public void testRequestFlightsOfCompanyStatus400() {
        int companyId = 1;
        when(Unirest.get("/flights")).thenReturn(getRequest);
        when(getRequest.queryString(StringNames.companyId, companyId)).thenReturn(getRequest);
        when(getRequest.asJson()).thenReturn(httpResponse);
        when(httpResponse.getStatus()).thenReturn(400);

        assertNull(client.requestFlightsOfCompany(companyId));
    }

    /**
     * Tests successful retrieval of detailed flight information for a company with status 200.
     * Verifies that flight details including date, name, duration, and launch time are correctly parsed.
     */
    @Test
    public void testRequestFlightInformationOfCompanyStatus200() {
        int companyId = 1;
        int flightId = 2;
        when(Unirest.get("/flights")).thenReturn(getRequest);
        when(getRequest.queryString(StringNames.companyId, companyId)).thenReturn(getRequest);
        when(getRequest.queryString(StringNames.flightId, flightId)).thenReturn(getRequest);
        when(getRequest.asJson()).thenReturn(httpResponse);
        when(httpResponse.getBody()).thenReturn(value);
        when(value.toString()).thenReturn("[{\"date\":\"Apr. 23, 2025\",\"name\":\"Europe Journey\",\"flight_duration\":60,"
                + "\"view_type\":\"Europe\",\"launch_time\":\"13:30:00\",\"flightDateId\":14}]");
        when(httpResponse.getStatus()).thenReturn(200);

        List<JsonObject> result = client.requestFlightInformationOfCompany(companyId, flightId);
        assertEquals(1, result.size());
        assertEquals("Apr. 23, 2025", result.get(0).get("date").getAsString());
        assertEquals("Europe Journey", result.get(0).get("name").getAsString());
        assertEquals(60, result.get(0).get("flight_duration").getAsInt());
        assertEquals("Europe", result.get(0).get("view_type").getAsString());
        assertEquals("13:30:00", result.get(0).get("launch_time").getAsString());
        assertEquals(14, result.get(0).get("flightDateId").getAsInt());
    }

    /**
     * Tests handling of flight information request with error status 400.
     * Verifies that null is returned when the server responds with an error.
     */
    @Test
    public void testRequestFlightInformationOfCompanyStatus400() {
        int companyId = 1;
        int flightId = 2;
        when(Unirest.get("/flights")).thenReturn(getRequest);
        when(getRequest.queryString(StringNames.companyId, companyId)).thenReturn(getRequest);
        when(getRequest.queryString(StringNames.flightId, flightId)).thenReturn(getRequest);
        when(getRequest.asJson()).thenReturn(httpResponse);
        when(httpResponse.getStatus()).thenReturn(400);

        assertNull(client.requestFlightInformationOfCompany(companyId, flightId));
    }

    // CLIENT REQUESTS
    /**
     * Tests successful retrieval of user information by email with status 200.
     * Verifies that user details including name, password, and ID are correctly parsed.
     */
    @Test
    public void testGetUserInfoByMailStatus200() {
        String email = "noah.const@web.de";
        client.setUser(new User(email, "9ikwelf%"));
        String auth = "Basic " + Base64.getEncoder().encodeToString("noah.const@web.de:9ikwelf%".getBytes());

        when(Unirest.get("/customers")).thenReturn(getRequest);
        when(getRequest.queryString(StringNames.email, email)).thenReturn(getRequest);
        when(getRequest.header(StringNames.authorization, auth)).thenReturn(getRequest);
        when(getRequest.asJson()).thenReturn(httpResponse);
        when(httpResponse.getBody()).thenReturn(value);
        when(value.toString())
                .thenReturn("[{\"firstName\":\"Noah\",\"lastName\":\"Constantino\",\"password\":\"9ikwelf%\","
                        + "\"id\":12, \"email\":\"noah.const@web.de\"}]");
        when(httpResponse.getStatus()).thenReturn(200);

        JsonObject result = client.getUserInfoByMail(email);
        assertEquals("Noah", result.get("firstName").getAsString());
        assertEquals("Constantino", result.get("lastName").getAsString());
        assertEquals("9ikwelf%", result.get("password").getAsString());
        assertEquals(12, result.get("id").getAsInt());
        assertEquals("noah.const@web.de", result.get("email").getAsString());
    }

    /**
     * Tests handling of unauthorized user information request with status 401.
     * Verifies that null is returned when authentication fails.
     */
    @Test
    public void testGetUserInfoByMailStatus401() {
        String email = "noah.const@web.de";
        client.setUser(new User(email, "9ikwelf%"));
        String auth = "Basic " + Base64.getEncoder().encodeToString("noah.const@web.de:9ikwelf%".getBytes());

        when(Unirest.get("/customers")).thenReturn(getRequest);
        when(getRequest.queryString(StringNames.email, email)).thenReturn(getRequest);
        when(getRequest.header(StringNames.authorization, auth)).thenReturn(getRequest);
        when(getRequest.asJson()).thenReturn(httpResponse);
        when(httpResponse.getStatus()).thenReturn(401);

        assertNull(client.getUserInfoByMail(email));
    }

    // SCHEDULE REQUESTS
    /**
     * Tests successful retrieval of flight date information with status 200.
     * Verifies that flight schedule details including seats and IDs are correctly parsed.
     */
    @Test
    public void testGetFlightDateInfoStatus200() {
        when(Unirest.get("/schedules")).thenReturn(getRequest);
        when(getRequest.queryString(StringNames.flightDateId, 1)).thenReturn(getRequest);
        when(getRequest.asJson()).thenReturn(httpResponse);
        when(httpResponse.getBody()).thenReturn(value);
        when(value.toString()).thenReturn("[{\"dateId\":1,\"available_seats\":50,\"flightScheduleId\":1,\"id\":1}]");
        when(httpResponse.getStatus()).thenReturn(200);

        JsonObject result = client.getFlightDateInfo(1);
        assertEquals(1, result.get("dateId").getAsInt());
        assertEquals(50, result.get("available_seats").getAsInt());
        assertEquals(1, result.get("flightScheduleId").getAsInt());
        assertEquals(1, result.get("id").getAsInt());
    }

    /**
     * Tests handling of flight date information request with error status 400.
     * Verifies that null is returned when the server responds with an error.
     */
    @Test
    public void testGetFlightDateInfoStatus400() {
        when(Unirest.get("/schedules")).thenReturn(getRequest);
        when(getRequest.queryString(StringNames.flightDateId, 1)).thenReturn(getRequest);
        when(getRequest.asJson()).thenReturn(httpResponse);
        when(httpResponse.getStatus()).thenReturn(400);

        assertNull(client.getFlightDateInfo(1));
    }

    /**
     * Tests successful retrieval of schedule information with status 200.
     * Verifies that schedule details including date, company, and flight information are correctly parsed.
     */
    @Test
    public void testGetScheduleInfoStatus200() {
        // Mock the GET request to the "/schedules" endpoint
        when(Unirest.get("/schedules")).thenReturn(getRequest);
        
        // Add query parameters to the GET request: flightDateId and info flag
        when(getRequest.queryString(StringNames.flightDateId, 1)).thenReturn(getRequest);
        when(getRequest.queryString(StringNames.info, true)).thenReturn(getRequest);
        
        // Mock the JSON response from the server
        when(getRequest.asJson()).thenReturn(httpResponse);
        when(httpResponse.getBody()).thenReturn(value);
        when(value.toString()).thenReturn(
                "[{\"date\":\"March 22, 2025\",\"companyId\":1,\"companyName\":\"Virgin Galactic\",\"launch_time\":\"13:30:00\","
                        + "\"flightId\":1,\"flightName\":\"Europe Journey\",\"flightDateId\":1}]");
        
        // Mock the HTTP status code to be 200 (OK)
        when(httpResponse.getStatus()).thenReturn(200);

        // Call the client method to get schedule information and verify the returned data
        JsonObject result = client.getScheduleInfo(1);
        assertEquals("March 22, 2025", result.get("date").getAsString());
        assertEquals(1, result.get("companyId").getAsInt());
        assertEquals("Virgin Galactic", result.get("companyName").getAsString());
        assertEquals("13:30:00", result.get("launch_time").getAsString());
        assertEquals(1, result.get("flightId").getAsInt());
        assertEquals("Europe Journey", result.get("flightName").getAsString());
        assertEquals(1, result.get("flightDateId").getAsInt());
    }

    /**
     * Tests handling of schedule information request with error status 400.
     * Verifies that null is returned when the server responds with an error.
     */
    @Test
    public void testGetScheduleInfoStatus400() {
        when(Unirest.get("/schedules")).thenReturn(getRequest);
        when(getRequest.queryString(StringNames.flightDateId, 1)).thenReturn(getRequest);
        when(getRequest.queryString(StringNames.info, true)).thenReturn(getRequest);
        when(getRequest.asJson()).thenReturn(httpResponse);
        when(httpResponse.getStatus()).thenReturn(400);

        assertNull(client.getScheduleInfo(1));
    }

    // RESERVATION REQUESTS
    /**
     * Tests successful creation of a new reservation with status 201.
     * Verifies that the reservation is created with the correct customer and flight information.
     *
     * Sprint 3: the base testing case
     * Sprint 4: modify createNewReservation() with third parameter "seatNumbers"
     * and increment of user tokens
     */
    @Test
    public void testCreateNewReservationStatus201() {
        try (MockedStatic<Platform> platformMock = mockStatic(Platform.class)) {
            client.setUser(new User("noah.const@web.de", "9ikwelf%"));
            client.getUser().setId(1);
            String auth = "Basic " + Base64.getEncoder().encodeToString("noah.const@web.de:9ikwelf%".getBytes());

            // Mock Platform.runLater
            platformMock.when(() -> Platform.runLater(any(Runnable.class)))
                       .thenAnswer(inv -> {
                           ((Runnable) inv.getArgument(0)).run();
                           return null;
                       });

            // Mock create request
            when(Unirest.post("/reservation/create")).thenReturn(requestWithBody);
            when(requestWithBody.queryString(StringNames.customerId, 1)).thenReturn(requestWithBody);
            when(requestWithBody.queryString(StringNames.flightDateId, 2)).thenReturn(requestWithBody);
            when(requestWithBody.queryString(StringNames.reservedSeats, 3)).thenReturn(requestWithBody);
            when(requestWithBody.queryString(StringNames.seatNumbers, "1,2,3")).thenReturn(requestWithBody);
            when(requestWithBody.header(StringNames.authorization, auth)).thenReturn(requestWithBody);
            when(requestWithBody.asJson()).thenReturn(httpResponse);
            when(httpResponse.getStatus()).thenReturn(201);

            List<Integer> seatNumbers = Arrays.asList(1, 2, 3);
            assertTrue(client.createNewReservation(2, 3, seatNumbers));
        }
    }

    /**
     * Tests handling of reservation creation with error status 400.
     * Verifies that false is returned when the server responds with an error.
     *
     * Sprint 3: the base testing case
     * Sprint 4: modify with added "seatNumbers" in the reservation info
     * and increment of user tokens
     */
    @Test
    public void testCreateNewReservationStatus400() {
        client.setUser(new User("noah.const@web.de", "9ikwelf%"));
        client.getUser().setId(1);
        String auth = "Basic " + Base64.getEncoder().encodeToString("noah.const@web.de:9ikwelf%".getBytes());

        when(Unirest.post("/reservation/create")).thenReturn(requestWithBody);
        when(requestWithBody.queryString(StringNames.customerId, 1)).thenReturn(requestWithBody);
        when(requestWithBody.queryString(StringNames.flightDateId, 2)).thenReturn(requestWithBody);
        when(requestWithBody.queryString(StringNames.reservedSeats, 3)).thenReturn(requestWithBody);
        when(requestWithBody.queryString(StringNames.seatNumbers, "1,2,3")).thenReturn(requestWithBody);  // Add seat numbers mock
        when(requestWithBody.header(StringNames.authorization, auth)).thenReturn(requestWithBody);
        when(requestWithBody.asJson()).thenReturn(httpResponse);
        when(httpResponse.getStatus()).thenReturn(400);

        List<Integer> seatNumbers = Arrays.asList(1, 2, 3);
        assertFalse(client.createNewReservation(2, 3, seatNumbers));
    }

    /**
     * Tests successful retrieval of client reservations with status 200.
     * Verifies that reservation details are correctly parsed from the server response.
     *
     * Sprint 3: the base testing case
     * Sprint 4: modify with added "seatNumbers" in the reservation info
     */
    @Test
    public void testGetReservationsOfClientStatus200() {
        client.setUser(new User("noah.const@web.de", "9ikwelf%"));
        client.getUser().setId(1);
        String auth = "Basic " + Base64.getEncoder().encodeToString("noah.const@web.de:9ikwelf%".getBytes());

        when(Unirest.get("/reservations")).thenReturn(getRequest);
        when(getRequest.queryString(StringNames.customerId, 1)).thenReturn(getRequest);
        when(getRequest.header(StringNames.authorization, auth)).thenReturn(getRequest);
        when(getRequest.asJson()).thenReturn(httpResponse);
        when(httpResponse.getBody()).thenReturn(value);
        when(value.toString()).thenReturn("[{\"reserved_seats\":3,\"customerId\":12,\"id\":32,\"flightDateId\":11}]");
        when(httpResponse.getStatus()).thenReturn(200);

        List<JsonObject> result = client.getReservationsOfClient();
        assertEquals(1, result.size());
        assertEquals(3, result.get(0).get("reserved_seats").getAsInt());
        assertEquals(12, result.get(0).get("customerId").getAsInt());
        assertEquals(32, result.get(0).get("id").getAsInt());
        assertEquals(11, result.get(0).get("flightDateId").getAsInt());
    }

    /**
     * Tests handling of client reservations request with error status 400.
     * Verifies that null is returned when the server responds with an error.
     */
    @Test
    public void testGetReservationsOfClientStatus400() {
        client.setUser(new User("noah.const@web.de", "9ikwelf%"));
        client.getUser().setId(1);
        String auth = "Basic " + Base64.getEncoder().encodeToString("noah.const@web.de:9ikwelf%".getBytes());

        when(Unirest.get("/reservations")).thenReturn(getRequest);
        when(getRequest.queryString(StringNames.customerId, 1)).thenReturn(getRequest);
        when(getRequest.header(StringNames.authorization, auth)).thenReturn(getRequest);
        when(getRequest.asJson()).thenReturn(httpResponse);
        when(httpResponse.getStatus()).thenReturn(400);

        assertNull(client.getReservationsOfClient());
    }

    /**
     * Tests successful deletion of a reservation with status 201.
     * Verifies that the reservation is properly deleted and returns true.
     *
     * Sprint 3: the base testing case
     * Sprint 4: modify with added "seatNumbers" in the reservation info
     */
    @Test
    public void testDeleteReservationStatus201() {
        try (MockedStatic<Platform> platformMock = mockStatic(Platform.class)) {
            client.setUser(new User("noah.const@web.de", "9ikwelf%"));
            client.getUser().setId(1);
            String auth = "Basic " + Base64.getEncoder().encodeToString("noah.const@web.de:9ikwelf%".getBytes());

            // Mock Platform.runLater
            platformMock.when(() -> Platform.runLater(any(Runnable.class)))
                       .thenAnswer(inv -> {
                           ((Runnable) inv.getArgument(0)).run();
                           return null;
                       });

            // Mock delete request
            HttpResponse<JsonNode> deleteResponse = mock(HttpResponse.class);
            JsonNode deleteValue = mock(JsonNode.class);
            lenient().when(Unirest.delete("/reservation/delete")).thenReturn(requestWithBody);
            lenient().when(requestWithBody.queryString(StringNames.reservationId, 2)).thenReturn(requestWithBody);
            lenient().when(requestWithBody.header(StringNames.authorization, auth)).thenReturn(requestWithBody);
            lenient().when(requestWithBody.asJson()).thenReturn(deleteResponse);
            lenient().when(deleteResponse.getBody()).thenReturn(deleteValue);
            lenient().when(deleteValue.toString()).thenReturn("[{\"message\":\"Successfully deleted.\"}]");
            lenient().when(deleteResponse.getStatus()).thenReturn(201);

            // Mock token update request
            GetRequest tokenRequest = mock(GetRequest.class);
            HttpResponse<JsonNode> tokenResponse = mock(HttpResponse.class);
            JsonNode tokenValue = mock(JsonNode.class);
            lenient().when(Unirest.get("/customer/getTokens")).thenReturn(tokenRequest);
            lenient().when(tokenRequest.queryString(eq(StringNames.customerId), anyInt())).thenReturn(tokenRequest);
            lenient().when(tokenRequest.header(eq(StringNames.authorization), eq(auth))).thenReturn(tokenRequest);
            lenient().when(tokenRequest.asJson()).thenReturn(tokenResponse);
            lenient().when(tokenResponse.getBody()).thenReturn(tokenValue);
            lenient().when(tokenValue.toString()).thenReturn("[{\"tokens\":10}]");
            lenient().when(tokenResponse.getStatus()).thenReturn(200);

            assertTrue(client.deleteReservation(2));
        }
    }

    /**
     * Tests handling of reservation deletion with error status 400.
     * Verifies that false is returned when the server responds with an error.
     */
    @Test
    public void testDeleteReservationStatus400() {
        client.setUser(new User("noah.const@web.de", "9ikwelf%"));
        String auth = "Basic " + Base64.getEncoder().encodeToString("noah.const@web.de:9ikwelf%".getBytes());

        when(Unirest.delete("/reservation/delete")).thenReturn(requestWithBody);
        when(requestWithBody.queryString(StringNames.reservationId, 2)).thenReturn(requestWithBody);
        when(requestWithBody.header(StringNames.authorization, auth)).thenReturn(requestWithBody);
        when(requestWithBody.asJson()).thenReturn(httpResponse);
        when(httpResponse.getStatus()).thenReturn(400);

        assertFalse(client.deleteReservation(2));
    }

    /**
     * Tests handling of reservation modification with error status 400.
     * Verifies that false is returned when the server responds with an error.
     */
    @Test
    public void testModifyReservationStatus400() {
        client.setUser(new User("noah.const@web.de", "9ikwelf%"));
        String auth = "Basic " + Base64.getEncoder().encodeToString("noah.const@web.de:9ikwelf%".getBytes());

        when(Unirest.put("/reservation/modify")).thenReturn(requestWithBody);
        when(requestWithBody.queryString(StringNames.reservationId, 2)).thenReturn(requestWithBody);
        when(requestWithBody.queryString(StringNames.reservedSeats, 4)).thenReturn(requestWithBody);
        when(requestWithBody.queryString(StringNames.seatNumbers, "1,2,3,4")).thenReturn(requestWithBody);
        when(requestWithBody.header(StringNames.authorization, auth)).thenReturn(requestWithBody);
        when(requestWithBody.asJson()).thenReturn(httpResponse);
        when(httpResponse.getBody()).thenReturn(value);
        when(value.toString()).thenReturn("[{\"message\":\"Error message\"}]");
        when(httpResponse.getStatus()).thenReturn(400);

        List<Integer> seatNumbers = Arrays.asList(1, 2, 3, 4);
        assertFalse(client.modifyReservation(2, 4, seatNumbers));
    }

    /**
     * Tests successful redemption of tokens with status 200.
     * Verifies that tokens are properly redeemed and returns true.
     */
    @Test
    public void testRedeemTokensStatus200() {
        client.setUser(new User("test@email.com", "password"));
        client.getUser().setId(1);
        String auth = "Basic " + Base64.getEncoder().encodeToString("test@email.com:password".getBytes());

        when(Unirest.put("/tokens/redeem")).thenReturn(requestWithBody);
        when(requestWithBody.queryString(StringNames.customerId, 1)).thenReturn(requestWithBody);
        when(requestWithBody.queryString(StringNames.tokens, 5)).thenReturn(requestWithBody);
        when(requestWithBody.header(StringNames.authorization, auth)).thenReturn(requestWithBody);
        when(requestWithBody.asJson()).thenReturn(httpResponse);
        when(httpResponse.getStatus()).thenReturn(200);
        assertTrue(client.redeemTokens(5));
    }

    /**
     * Tests handling of token redemption with error status 400.
     * Verifies that false is returned when the server responds with an error.
     */
    @Test
    public void testRedeemTokensStatus400() {
        client.setUser(new User("test@email.com", "password"));
        client.getUser().setId(1);
        String auth = "Basic " + Base64.getEncoder().encodeToString("test@email.com:password".getBytes());

        when(Unirest.put("/tokens/redeem")).thenReturn(requestWithBody);
        when(requestWithBody.queryString(StringNames.customerId, 1)).thenReturn(requestWithBody);
        when(requestWithBody.queryString(StringNames.tokens, 5)).thenReturn(requestWithBody);
        when(requestWithBody.header(StringNames.authorization, auth)).thenReturn(requestWithBody);
        when(requestWithBody.asJson()).thenReturn(httpResponse);
        when(httpResponse.getStatus()).thenReturn(400);

        assertFalse(client.redeemTokens(5));
    }

    /**
     * Tests successful retrieval of user tokens with status 200.
     * Verifies that token count is correctly updated and returned.
     */
    @Test
    public void testGetTokensStatus200() {
        try (MockedStatic<Platform> platformMock = mockStatic(Platform.class)) {
            client.setUser(new User("test@email.com", "password"));
            client.getUser().setId(1);
            String auth = "Basic " + Base64.getEncoder().encodeToString("test@email.com:password".getBytes());

            // Mock Platform.runLater
            platformMock.when(() -> Platform.runLater(any(Runnable.class)))
                       .thenAnswer(inv -> {
                           ((Runnable) inv.getArgument(0)).run();
                           return null;
                       });

            // Mock GET tokens request
            when(Unirest.get("/customer/getTokens")).thenReturn(getRequest);
            when(getRequest.queryString(StringNames.customerId, 1)).thenReturn(getRequest);
            when(getRequest.header(StringNames.authorization, auth)).thenReturn(getRequest);
            when(getRequest.asJson()).thenReturn(httpResponse);
            when(httpResponse.getBody()).thenReturn(value);
            when(value.toString()).thenReturn("[{\"tokens\":10}]");
            when(httpResponse.getStatus()).thenReturn(200);

            // Mock PUT redeem tokens request
            when(Unirest.put("/tokens/redeem")).thenReturn(requestWithBody);
            when(requestWithBody.queryString(StringNames.customerId, 1)).thenReturn(requestWithBody);
            when(requestWithBody.queryString(StringNames.tokens, 5)).thenReturn(requestWithBody);
            when(requestWithBody.header(StringNames.authorization, auth)).thenReturn(requestWithBody);
            when(requestWithBody.asJson()).thenReturn(httpResponse);
            when(httpResponse.getStatus()).thenReturn(200);

            // Directly set tokens to simulate Platform.runLater
            client.getUser().setTokens(10);
            assertTrue(client.redeemTokens(5));
            assertEquals(10, client.getUser().getTokens());
        }
    }

    /**
     * Tests successful token redemption with token update.
     * Verifies that tokens are properly redeemed and user token count is updated.
     */
    @Test
    public void testRedeemTokensSuccessfulUpdate() {
        try (MockedStatic<Platform> platformMock = mockStatic(Platform.class)) {
            client.setUser(new User("test@email.com", "password"));
            client.getUser().setId(1);
            String auth = "Basic " + Base64.getEncoder().encodeToString("test@email.com:password".getBytes());

            // Mock Platform.runLater
            platformMock.when(() -> Platform.runLater(any(Runnable.class)))
                       .thenAnswer(inv -> {
                           ((Runnable) inv.getArgument(0)).run();
                           return null;
                       });

            // Mock both requests
            when(Unirest.put("/tokens/redeem")).thenReturn(requestWithBody);
            when(requestWithBody.queryString(StringNames.customerId, 1)).thenReturn(requestWithBody);
            when(requestWithBody.queryString(StringNames.tokens, 5)).thenReturn(requestWithBody);
            when(requestWithBody.header(StringNames.authorization, auth)).thenReturn(requestWithBody);
            when(requestWithBody.asJson()).thenReturn(httpResponse);
            when(httpResponse.getStatus()).thenReturn(200);

            when(Unirest.get("/customer/getTokens")).thenReturn(getRequest);
            when(getRequest.queryString(eq(StringNames.customerId), eq(1))).thenReturn(getRequest);
            when(getRequest.header(eq(StringNames.authorization), eq(auth))).thenReturn(getRequest);
            when(getRequest.asJson()).thenReturn(httpResponse);
            when(httpResponse.getBody()).thenReturn(value);
            when(value.toString()).thenReturn("[{\"tokens\":15}]");

            assertTrue(client.redeemTokens(5));
            assertEquals(15, client.getUser().getTokens());
        }
    }

    // FLIGHT DATES PER DATE REQUESTS
    /**
     * Tests successful retrieval of flight dates for a specific date with status 200.
     * Verifies that flight schedule details are correctly parsed from the server response.
     */
    @Test
    public void testGetFlightDatesPerDateStatus200() {
        java.sql.Date testDate = java.sql.Date.valueOf("2025-03-22");

        // Mock dates-with-flights request
        when(Unirest.get("/dates-with-flights")).thenReturn(getRequest);
        when(getRequest.asJson()).thenReturn(httpResponse);
        when(httpResponse.getBody()).thenReturn(value);
        when(value.toString()).thenReturn("[{\"dateId\":1,\"date\":\"2025-03-22\"}]");
        when(httpResponse.getStatus()).thenReturn(200);

        // Mock schedules-by-date request
        GetRequest secondGetRequest = mock(GetRequest.class);
        HttpResponse<JsonNode> secondResponse = mock(HttpResponse.class);
        JsonNode secondValue = mock(JsonNode.class);

        when(Unirest.get("/schedules/by-date")).thenReturn(secondGetRequest);
        when(secondGetRequest.queryString(StringNames.dateId, "1")).thenReturn(secondGetRequest);
        when(secondGetRequest.asJson()).thenReturn(secondResponse);
        when(secondResponse.getBody()).thenReturn(secondValue);
        when(secondValue.toString()).thenReturn("[{\"flightName\":\"Europe Journey\"," +
                "\"flight_duration\":60,\"view_type\":\"Europe\"," +
                "\"companyName\":\"Virgin Galactic\",\"launch_time\":\"13:30:00\"," +
                "\"available_seats\":50,\"flightDateId\":1}]");
        when(secondResponse.getStatus()).thenReturn(200);

        List<JsonObject> result = client.getFlightDatesPerDate(testDate);

        assertEquals(1, result.size());
        assertEquals("Europe Journey", result.get(0).get("flightName").getAsString());
        assertEquals(60, result.get(0).get("flight_duration").getAsInt());
        assertEquals("Europe", result.get(0).get("view_type").getAsString());
        assertEquals("Virgin Galactic", result.get(0).get("companyName").getAsString());
        assertEquals("13:30:00", result.get(0).get("launch_time").getAsString());
        assertEquals(50, result.get(0).get("available_seats").getAsInt());
        assertEquals(1, result.get(0).get("flightDateId").getAsInt());
    }

    /**
     * Tests handling of flight dates request when the first request fails.
     * Verifies that an empty list is returned when the dates-with-flights request fails.
     */
    @Test
    public void testGetFlightDatesPerDateFirstRequestFails() {
        java.sql.Date testDate = java.sql.Date.valueOf("2025-03-22");

        // Mock dates-with-flights request failure
        when(Unirest.get("/dates-with-flights")).thenReturn(getRequest);
        when(getRequest.asJson()).thenReturn(httpResponse);
        when(httpResponse.getStatus()).thenReturn(400);

        List<JsonObject> result = client.getFlightDatesPerDate(testDate);
        assertTrue(result.isEmpty());
    }

    /**
     * Tests handling of flight dates request when the second request fails.
     * Verifies that an empty list is returned when the schedules-by-date request fails.
     */
    @Test
    public void testGetFlightDatesPerDateSecondRequestFails() {
        java.sql.Date testDate = java.sql.Date.valueOf("2025-03-22");

        // Mock successful dates-with-flights request
        when(Unirest.get("/dates-with-flights")).thenReturn(getRequest);
        when(getRequest.asJson()).thenReturn(httpResponse);
        when(httpResponse.getBody()).thenReturn(value);
        when(value.toString()).thenReturn("[{\"dateId\":1,\"date\":\"2025-03-22\"}]");
        when(httpResponse.getStatus()).thenReturn(200);

        // Mock failed schedules-by-date request
        GetRequest secondGetRequest = mock(GetRequest.class);
        HttpResponse<JsonNode> secondResponse = mock(HttpResponse.class);

        when(Unirest.get("/schedules/by-date")).thenReturn(secondGetRequest);
        when(secondGetRequest.queryString(StringNames.dateId, "1")).thenReturn(secondGetRequest);
        when(secondGetRequest.asJson()).thenReturn(secondResponse);
        when(secondResponse.getStatus()).thenReturn(400);

        List<JsonObject> result = client.getFlightDatesPerDate(testDate);
        assertTrue(result.isEmpty());
    }

    /**
     * Tests handling of flight dates request when no matching date is found.
     * Verifies that an empty list is returned when the requested date doesn't match any available dates.
     */
    @Test
    public void testGetFlightDatesPerDateNoMatchingDate() {
        java.sql.Date testDate = java.sql.Date.valueOf("2025-03-22");

        // Mock dates-with-flights request with non-matching date
        when(Unirest.get("/dates-with-flights")).thenReturn(getRequest);
        when(getRequest.asJson()).thenReturn(httpResponse);
        when(httpResponse.getBody()).thenReturn(value);
        when(value.toString()).thenReturn("[{\"dateId\":1,\"date\":\"2025-04-17\"}]");
        when(httpResponse.getStatus()).thenReturn(200);

        List<JsonObject> result = client.getFlightDatesPerDate(testDate);
        assertTrue(result.isEmpty());
    }

}