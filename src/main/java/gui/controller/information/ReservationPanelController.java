package gui.controller.information;

import gui.controller.Controller;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.Alert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ReservationPanelController extends Controller {

    // ------------------ class attributes ------------------
    private int reservationId;
    private int oldReservedSeatAmount;
    private List<Integer> oldReservedSeatNumbers;
    private int companyId;
    private int flightId;
    private int flightDateId;
    private final int maxNumberOfSeats = 100; // Maximum number of available seats

    // ------------------ FXML components ------------------
    @FXML
    private Label flightReservationLabel;
    @FXML
    private Label companyLabel;
    @FXML
    private Label flightLabel;
    @FXML
    private Label dateLabel;
    @FXML
    private Label launchTimeLabel;
    @FXML
    private ChoiceBox<Integer> seatAmountChoiceBox;
    @FXML
    private VBox seatSelectionVBox;

    @FXML
    private Button reserveButton;
    @FXML
    private Label noModificationLabel;
    @FXML
    private HBox noAccountBox;
    @FXML
    private Button createAccountButton;
    @FXML
    private Button logInButton;

    @Override
    public <T> void initializeSceneData(List<T> data) {
        if (data == null || data.isEmpty()) {
            System.out.println("No data provided to initialize scene");
            return;
        }

        // if current session is null, ask user to create new account, otherwise
        // reservations is made in logged in account directly
        if (restClient.getUser() == null) {
            noAccountBox.setVisible(true);
            reserveButton.setDisable(true);
        } else {
            noAccountBox.setVisible(false);
        }

        companyLabel.setText((String) data.get(0));
        flightLabel.setText((String) data.get(1));
        launchTimeLabel.setText((String) data.get(2));
        dateLabel.setText((String) data.get(3));

        companyId = Integer.parseInt((String) data.get(4));
        flightId = Integer.parseInt((String) data.get(5));
        flightDateId = Integer.parseInt((String) data.get(6));
        // initialize ids
        try {
            companyId = Integer.parseInt((String) data.get(4));
            flightId = Integer.parseInt((String) data.get(5));
            flightDateId = Integer.parseInt((String) data.get(6));
            System.out.println("Initialized with flightDateId: " + flightDateId);
        } catch (NumberFormatException e) {
            System.out.println("Error parsing IDs: " + e.getMessage());
        }

        int selectedSeats = 1;
        if (sceneNavigator.getPreviousScene() != null
                && sceneNavigator.getPreviousScene().equals(sceneNavigator.PROFILE)) {
            reservationId = Integer.parseInt(((String) data.get(7)).trim());
            oldReservedSeatAmount = Integer.parseInt(((String) data.get(8)).trim());
            String seatNumbersStr = (String) data.get(9);
            if (seatNumbersStr != null) {
                oldReservedSeatNumbers = Arrays.stream(seatNumbersStr.split(","))
                        .map(String::trim)
                        .map(Integer::parseInt)
                        .collect(Collectors.toList());
            }
            flightReservationLabel.setText("Change flight reservation");
            reserveButton.setText("Change reservation");
            selectedSeats = oldReservedSeatAmount;
        }

        setUpSeatSelection(selectedSeats);
        // Add debug logging
        System.out.println("ChoiceBox items: " + seatAmountChoiceBox.getItems());
        System.out.println("Selected value: " + seatAmountChoiceBox.getValue());
        updateSeatSelectionComboBoxes(selectedSeats, oldReservedSeatNumbers);

        seatAmountChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            updateSeatSelectionComboBoxes(newVal, oldReservedSeatNumbers);
        });
    }

    private void setUpSeatSelection(int selectedSeats) {
        seatAmountChoiceBox.getItems().clear();
        int availableSeats = restClient.getFlightDateInfo(flightDateId).get("available_seats").getAsInt();

        // check if user is modifying a reservation (oldReservedSeatAmount != 0) and
        // only less than 10 seats are available
        int seatMax = 10;
        if (oldReservedSeatAmount != 0 && availableSeats < 10) {
            if (oldReservedSeatAmount + availableSeats < 10) {
                seatMax = oldReservedSeatAmount + availableSeats;
            }
        } else if (availableSeats < 10) {
            seatMax = availableSeats;
        }

        int[] seatOptions = IntStream.rangeClosed(1, seatMax).toArray();
        for (int i : seatOptions) {
            seatAmountChoiceBox.getItems().add(i);
        }
        seatAmountChoiceBox.setValue(selectedSeats);
    }

    private void updateSeatSelectionComboBoxes(int numberOfSeats, List<Integer> seatNumbers) {
        seatSelectionVBox.getChildren().clear();

        // Fetch reserved seats as integers
        List<Integer> reservedSeats = restClient.fetchAvailableSeats(flightDateId);

        // Create available seats list
        List<Integer> availableSeats = IntStream.rangeClosed(1, maxNumberOfSeats)
                .boxed()
                .filter(seat -> !reservedSeats.contains(seat))
                .collect(Collectors.toList());

        // Convert to ObservableList for sorting
        ObservableList<Integer> observableSeats = FXCollections.observableArrayList(availableSeats);
        FXCollections.sort(observableSeats); // Fix 1: Correct sorting

        for (int i = 0; i < numberOfSeats; i++) {
            ComboBox<Integer> seatComboBox = new ComboBox<>();
            seatComboBox.setPrefWidth(150);
            seatComboBox.setMaxWidth(150);
            seatComboBox.setPromptText("Select seat " + (i + 1));

            // Create a copy of available seats for this combo box
            List<Integer> seatsForBox = new ArrayList<>(observableSeats);

            if (seatNumbers != null && i < seatNumbers.size()) {
                seatsForBox.add(seatNumbers.get(i));
                FXCollections.sort(FXCollections.observableArrayList(seatsForBox));
            }

            seatComboBox.setItems(FXCollections.observableArrayList(seatsForBox));

            if (seatNumbers != null && i < seatNumbers.size()) {
                seatComboBox.getSelectionModel().select(seatNumbers.get(i));
            }

            seatComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (oldVal != null) addSeatToOtherComboBoxes(oldVal);
                if (newVal != null) removeSelectedSeatFromOtherComboBoxes(newVal, seatComboBox);
            });

            seatSelectionVBox.getChildren().add(seatComboBox);
        }
    }

    private void removeSelectedSeatFromOtherComboBoxes(int selectedSeat, ComboBox<Integer> sourceComboBox) {
        for (Node node : seatSelectionVBox.getChildren()) {
            if (node instanceof ComboBox && node != sourceComboBox) {
                ComboBox<Integer> comboBox = (ComboBox<Integer>) node;
                comboBox.getItems().remove(Integer.valueOf(selectedSeat));
            }
        }
    }

    private void addSeatToOtherComboBoxes(int deselectedSeat) {
        for (Node node : seatSelectionVBox.getChildren()) {
            if (node instanceof ComboBox) {
                ComboBox<Integer> comboBox = (ComboBox<Integer>) node;
                if (!comboBox.getItems().contains(deselectedSeat)) {
                    comboBox.getItems().add(deselectedSeat);
                    FXCollections.sort(comboBox.getItems());
                }
            }
        }
    }

    private List<Integer> getSeatNumbersFromSelectionBox() {
        List<Integer> seatNumbers = new ArrayList<>();
        for (Node node : seatSelectionVBox.getChildren()) {
            if (node instanceof ComboBox) {
                ComboBox<Integer> comboBox = (ComboBox<Integer>) node;
                if (comboBox.getValue() == null) {
                    noModificationLabel.setText("Please select a seat for all tickets.");
                    return null;
                }
                seatNumbers.add(comboBox.getValue());
            }
        }
        return seatNumbers;
    }

    @FXML
    public void onReserveButtonClicked() {
        int seatAmount = this.seatAmountChoiceBox.getValue();
        List<Integer> seatNumbers = getSeatNumbersFromSelectionBox();

        if (seatNumbers == null) {
            return;
        }

        List<String> controllerData = new ArrayList<>();
        if (sceneNavigator.getPreviousScene() != null
                && sceneNavigator.getPreviousScene().equals(sceneNavigator.PROFILE)) {
            // Handle modification of existing reservation
            if (seatAmount == oldReservedSeatAmount && seatNumbers.equals(oldReservedSeatNumbers)) {
                noModificationLabel.setText("No changes were performed.");
            } else {
                restClient.modifyReservation(reservationId, seatAmount, seatNumbers);
                controllerData.add("Change of reservation successful");
                controllerData.add("The amount of reserved tickets was successfully changed.");
                controllerData.add("Continue");
                sceneNavigator.loadSceneToMainWindow(sceneNavigator.SUCCESS_PANEL, controllerData);
            }
        } else {
            // Handle new reservation
            restClient.createNewReservation(flightDateId, seatAmount, seatNumbers);
            controllerData.add("Reservation successful");
            controllerData.add("Tickets were successfully reserved for the chosen space flight.");
            controllerData.add("Continue");
            // Store information for return to company view
            List<Integer> companyData = Collections.singletonList(companyId);
            sceneNavigator.setPreviousScene(sceneNavigator.INFO);
            sceneNavigator.setDataPreviousScene((List)companyData);  // 使用强制类型转换

            sceneNavigator.loadSceneToMainWindow(sceneNavigator.SUCCESS_PANEL, controllerData);
        }
    }

    public void setPreviousSceneInformation() {
        sceneNavigator.setPreviousScene(sceneNavigator.RESERVATION);
        String[] data = {companyLabel.getText(), flightLabel.getText(), launchTimeLabel.getText(), dateLabel.getText(),
                companyId + "", flightId + "", flightDateId + ""};
        sceneNavigator.setDataPreviousScene(Arrays.asList(data));
    }

    @FXML
    public void onCancelButtonClicked() {
        // if previous scene was profile scene, return to it
        if (sceneNavigator.getPreviousScene() != null
                && sceneNavigator.getPreviousScene().equals(sceneNavigator.PROFILE)) {
            sceneNavigator.setPreviousScene(null);
            sceneNavigator.setDataPreviousScene(null);
            sceneNavigator.loadSceneToMainWindow(sceneNavigator.PROFILE, null);
        } else { // otherwise return to cinema information scene
            sceneNavigator.loadSceneToMainWindow(sceneNavigator.INFO, Collections.singletonList(companyId));
        }
    }

    @FXML
    public void onCreateAccountButtonClicked() {
        // set up information about previous scene and call sign up scene
        setPreviousSceneInformation();
        sceneNavigator.loadCompleteWindow(sceneNavigator.SIGN_UP_DIALOG,
                (Stage) createAccountButton.getScene().getWindow());
    }

    @FXML
    public void onLogInButtonClicked() {
        // set up information about previous scene and call log in scene
        setPreviousSceneInformation();
        sceneNavigator.loadCompleteWindow(sceneNavigator.LOG_IN_DIALOG, (Stage) logInButton.getScene().getWindow());
    }
}
