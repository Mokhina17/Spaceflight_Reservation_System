package gui.controller.information;

import com.google.gson.JsonObject;
import gui.controller.Controller;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class FlightDatePanelController  extends Controller {

    @FXML
    private Label dateLabel;
    @FXML
    private ListView<String> flightListView;
    @FXML
    private Label errorLabel;
    @FXML
    private Button reserveButton;

    private List<JsonObject> flightData;
    private int selectedIndex = -1;

    /**
     * Initializes scene data with the given list of data.
     * This method is used to update the UI components based on the data passed.
     * For example, it sets the flight date label, hides the reserve button initially,
     * clears error messages, and loads flight information for the selected date.
     * 
     * @param data A list containing date information, the first element is expected to be of type Date.
     */
    @Override
    public <T> void initializeSceneData(List<T> data) {
        // Extract the selected date from the passed data list
        Date selectedDate = (Date) data.get(0);
        // Set the flight date label text
        dateLabel.setText("Flights on " + selectedDate.toString());
    
        // Hide reserve button initially
        reserveButton.setVisible(false);
    
        // Clear any previous error messages
        errorLabel.setText("");
    
        try {
            // Get flights for selected date from server
            flightData = restClient.getFlightDatesPerDate(selectedDate);
    
            // Check if there are any flights available on the selected date
            if (flightData.isEmpty()) {
                // Display no flight information error message
                errorLabel.setText("No flights available on selected date.");
                return;
            }
    
            // Populate ListView with flight information
            initializeFlightView();
    
        } catch (Exception e) {
            // Handle exceptions when loading flight data, display error message
            errorLabel.setText("Error loading flight data: " + e.getMessage());
        }
    }

    /**
     * Initializes the flight display view.
     * This method clears the existing items in the flight list view, iterates through the flight data,
     * formats the flight information as a string, and adds it to the list view.
     * At the same time, it adds a selection listener to update the index of the selected item and control the visibility of the reserve button.
     */
    private void initializeFlightView() {
        // Clear existing items in the flight list view
        flightListView.getItems().clear();
    
        // Iterate through the flight data, format and add flight information to the list view
        for (JsonObject flight : flightData) {
            // Format flight information as string
            String flightInfo = String.format("%s - %s (%s) - %s",
                    flight.get("companyName").getAsString(),
                    flight.get("flightName").getAsString(),
                    flight.get("view_type").getAsString(),
                    flight.get("launch_time").getAsString()
            );
            // Add formatted flight information to the list view
            flightListView.getItems().add(flightInfo);
        }
    
        // Add selection listener
        flightListView.getSelectionModel().selectedIndexProperty().addListener(
                (observable, oldValue, newValue) -> {
                    // Update the index of the selected item
                    selectedIndex = newValue.intValue();
                    // Control the visibility of the reserve button based on whether an item is selected
                    reserveButton.setVisible(selectedIndex >= 0);
                }
        );
    }

    @FXML
    /**
     * Handles the action when the reserve button is clicked.
     * This method is triggered when the user clicks the reserve button. 
     * It processes the selected flight information and navigates to the reservation scene with the necessary data.
     */
    private void onReserveButtonClicked() {
        // Checks if a valid flight index is selected
        if (selectedIndex >= 0) {
            // Retrieves the selected flight's JSON object from the flight data list
            JsonObject selectedFlight = flightData.get(selectedIndex);

            // Initializes a list to store reservation details
            List<String> reservationData = new ArrayList<>();
            
            // Populates the reservation data list with relevant flight information
            reservationData.add(selectedFlight.get("companyName").getAsString());
            reservationData.add(selectedFlight.get("flightName").getAsString());
            reservationData.add(selectedFlight.get("launch_time").getAsString());
            reservationData.add(dateLabel.getText().replace("Flights on ", ""));
            reservationData.add(selectedFlight.get("companyId").getAsString());
            reservationData.add(selectedFlight.get("flightId").getAsString());
            reservationData.add(selectedFlight.get("flightDateId").getAsString());

            // Loads the reservation scene with the collected reservation data
            sceneNavigator.loadSceneToMainWindow(sceneNavigator.RESERVATION, reservationData);
        }
    }

    @FXML
    private void onBackButtonClicked() {
        sceneNavigator.loadSceneToMainWindow(sceneNavigator.INFO, null);
    }
}
