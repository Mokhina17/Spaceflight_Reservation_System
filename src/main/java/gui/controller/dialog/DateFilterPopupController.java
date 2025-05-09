package gui.controller.dialog;

import com.google.gson.JsonObject;
import gui.controller.Controller;
import javafx.fxml.FXML;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

public class DateFilterPopupController extends Controller {

    @FXML
    private DatePicker datePicker;

    /**
     * Initializes scene data with the given data list.
     * This method is used to initialize the scene with specific data, such as setting the date picker value and highlighting available dates.
     * 
     * @param data A list of data of any type, used for scene initialization.
     */
    @Override
    public <T> void initializeSceneData(List<T> data) {
        System.out.println("Initializing scene data...");
        
        // Initialize with current date
        datePicker.setValue(LocalDate.now());
    
        // Get available dates from server and highlight them
        List<JsonObject> datesWithFlights = restClient.requestUniqueDates();
        if (datesWithFlights == null || datesWithFlights.isEmpty()) {
            System.out.println("Warning: No dates returned from server");
            return;
        }
        addDatePickerHighlighting(datePicker, datesWithFlights);
    }

    @FXML
    private void onCancelButtonClicked() {
        Stage popupStage = sceneNavigator.getPopupStage();
        if (popupStage != null) {
            popupStage.close();
        }
    }

    @FXML
    private void onConfirmButtonClicked() {
        LocalDate selectedDate = datePicker.getValue();
        if (selectedDate != null) {
            Stage popupStage = sceneNavigator.getPopupStage();
            if (popupStage != null) {
                popupStage.close();
            }

            // Convert LocalDate to java.sql.Date for database compatibility
            java.sql.Date sqlDate = java.sql.Date.valueOf(selectedDate);
            sceneNavigator.loadSceneToMainWindow(
                    sceneNavigator.FLIGHT_DATE_PANEL,
                    Collections.singletonList(sqlDate)
            );
        }
    }

    /**
     * Adds highlighting effects to specific dates in a DatePicker.
     * This method customizes the DatePicker by highlighting dates that need special attention, based on a list of date information provided.
     * 
     * @param datePicker The DatePicker component to be customized.
     * @param highlightDates A list containing information about the dates to be highlighted, each date corresponding to a JsonObject.
     */
    private void addDatePickerHighlighting(DatePicker datePicker, List<JsonObject> highlightDates) {
        System.out.println("Setting up date picker highlighting...");
        System.out.println("Number of dates to highlight: " + highlightDates.size());

        // Set the date cell factory of the date selector
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);

                if (date != null && !empty) {
                    // Convert the current cell date to a string for comparison
                    String cellDate = date.toString();

                    setTextFill(javafx.scene.paint.Color.BLACK);
                    // Check if it is a date that needs to be highlighted
                    boolean hasFlights = highlightDates.stream()
                            .map(obj -> obj.get("date").getAsString())
                            .anyMatch(serverDate -> {
                                boolean matches = utils.Utils.datesMatch(serverDate, cellDate);
                                if (matches) {
                                    System.out.println("Highlighting date: " + cellDate);
                                }
                                return matches;
                            });

                    if (hasFlights) {
                        // Highlight the cell by setting a custom style
                        setStyle("-fx-background-color: lightblue;");
                        setTextFill(javafx.scene.paint.Color.WHITE);
                    }
                }
            }
        });

        System.out.println("Date picker highlighting setup completed");
    }
}