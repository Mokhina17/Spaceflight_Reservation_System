package gui.controller.information;

import com.google.gson.JsonObject;
import gui.controller.Controller;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import model.Reservation;
import utils.Utils;

import java.net.URL;
import java.util.*;

public class ProfilePanelController extends Controller implements Initializable {

    private final int tableRowHeight = 30;
    private TableColumn<Reservation, Date> dateColumn;
    private TableColumn<Reservation, String> timeColumn;

    @FXML
    private Label firstnameLabel;
    @FXML
    private Label lastnameLabel;
    @FXML
    private Label emailLabel;

    @FXML
    private TableView<Reservation> reservationsTableView;
    @FXML
    private GridPane buttonGridPane;
    @FXML
    private ProgressBar tokensProgressBar;
    @FXML
    private ComboBox<Integer> redeemTokensChoiceBox;
    @FXML
    private Button redeemTokensButton;

    public void initializeReservationTableView() {
        initializeTableColumns();
        List<JsonObject> reservations = restClient.getReservationsOfClient();

        if (reservations == null) {
            Label label = new Label("No reservations available until now.");
            label.setStyle("-fx-font-style: italic;");
            reservationsTableView.setPlaceholder(label);
            reservationsTableView.setPrefHeight(100);
        } else {
            for (JsonObject reservationJson : reservations) {
                addReservationToTable(reservationJson);
            }
            if (dateColumn != null) {
                reservationsTableView.getSortOrder().add(dateColumn);
            }
            if (timeColumn != null) {
                reservationsTableView.getSortOrder().add(timeColumn);
            }
            reservationsTableView.sort();
            setTableRowAndHeaderHeight();
            styleRows();
        }
    }

    /**
     * Iterates over every table column, which were previously already created in
     * the fxml layout. For every column
     * {@link javafx.scene.control.TableColumn#setCellValueFactory(javafx.util.Callback)}
     * is called which connects the table column with an attribute from the
     * <code>Reservation</code> class.<br>
     * Adds further properties for specific columns:
     * <ul>
     * <li><b>date:</b> register column for default sorting; add
     * {@link javafx.scene.control.TableColumn#setCellFactory(javafx.util.Callback)}
     * to show date in correct format</li>
     * <li><b>time:</b> register column for default sorting</li>
     * <li><b>flight/company:</b> add
     * {@link javafx.scene.control.TableColumn#setCellFactory(javafx.util.Callback)}
     * to add tooltip showing the complete flight/company name</li>
     * </ul>
     */
    public void initializeTableColumns() {
        ObservableList<TableColumn<Reservation, ?>> columns = reservationsTableView.getColumns();
        String[] variableNames = Reservation.getVariableNames();

        // iterate over each column and set factory to variable in POJO
        for (int i = 0; i < columns.size(); i++) {
            columns.get(i).setCellValueFactory(new PropertyValueFactory<>(variableNames[i]));
            TableColumn<Reservation, String> col = (TableColumn<Reservation, String>) columns.get(i);

            switch (variableNames[i]) {
                case "date":
                    dateColumn = (TableColumn<Reservation, Date>) columns.get(i);
                    dateColumn.setSortType(TableColumn.SortType.ASCENDING);
                    dateColumn.setCellFactory(column -> {
                        return new TableCell<>() {
                            @Override
                            protected void updateItem(Date item, boolean empty) {
                                super.updateItem(item, empty);
                                if (empty) {
                                    setText(null);
                                } else {
                                    setText(Utils.dayMonthYearDateFormat.format(item));
                                }
                            }
                        };
                    });
                    break;
                case "launch_time":
                    timeColumn = col;
                    timeColumn.setSortType(TableColumn.SortType.ASCENDING);
                    break;
                case "flight":
                case "company":
                    col.setCellFactory(column -> {
                        return new TableCell<>() {
                            @Override
                            protected void updateItem(String item, boolean empty) {
                                super.updateItem(item, empty);
                                setText(item);
                                setTooltip(new Tooltip(item));
                            }
                        };
                    });
                    break;
                case "seatNumbers":
                    col.setCellFactory(column -> {
                        return new TableCell<>() {
                            @Override
                            protected void updateItem(String item, boolean empty) {
                                super.updateItem(item, empty);
                                setText(item);
                            }
                        };
                    });
                    break;
            }
        }
    }

    /**
     * Makes request to REST client to extract the flight information of the
     * flight_dates id contained in the reservation. This way the ids of the company and
     * flight, time and date of the reservation are extracted. Creates new
     * <code>Reservation</code> object which is add to the table view. The mapping
     * of the attributes of <code>Reservation</code> to the columns was previously
     * defined.
     *
     * @param json the reservation in JSON format
     */
    private void addReservationToTable(JsonObject json) {
        JsonObject flightInfo = restClient.getScheduleInfo(json.get("flightDateId").getAsInt());

        int companyId = flightInfo.get("companyId").getAsInt();
        int flightId = flightInfo.get("flightId").getAsInt();
        int flightDateId = flightInfo.get("flightDateId").getAsInt();

        String time = flightInfo.get("launch_time").getAsString();
        Date date = Utils.parseStringToDate(flightInfo.get("date").getAsString() + " " + time,
                Utils.monthDayYearDateTimeFormat);

        // Fetch seat numbers as integers
        List<Integer> seatNumbers = restClient.getSeatNumbersForReservation(json.get("id").getAsInt());

        Reservation res = new Reservation(
                json.get("id").getAsInt(),
                companyId,
                flightId,
                flightDateId,
                flightInfo.get("companyName").getAsString(),
                flightInfo.get("flightName").getAsString(),
                time,
                json.get("reserved_seats").getAsInt(),
                date,
                seatNumbers
        );

        reservationsTableView.getItems().add(res);
    }

    /**
     * Defines two <code>PseudoClass</code> objects that define the appearance of
     * the tables rows which contain a reservation from the past. The style is
     * defined in <code>application.css</code> with the tag
     * <code>.table-view .table-row-cell:pastReservation/pastReservationSelected .text</code>.<br>
     * Adds a
     * {@link javafx.scene.control.TableView#setRowFactory(javafx.util.Callback)} to
     * the table view to handle the event of changing the row color of past
     * reservations. In the <code>updateItem</code> method it checks if the
     * reservation is <code>null</code> in which case the table view is only
     * refreshed. If the reservation is from the past, changes the pseudo class to
     * one of the previously defined pseudo classes, depending on whether the
     * reservation is selected or not (if the reservation is in the past and
     * selected, the row gets a lighter grey color).
     */
    private void styleRows() {
        final PseudoClass pastReservationClass = PseudoClass.getPseudoClass("pastReservation");
        final PseudoClass pastReservationSelectedClass = PseudoClass.getPseudoClass("pastReservationSelected");
        reservationsTableView.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Reservation item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    // refresh view so that after resorting the rows, the coloring is still correct
                    reservationsTableView.refresh();
                } else if (Utils.isPastDate(item.getDate())) {
                    Reservation selectedReservation = reservationsTableView.getSelectionModel().getSelectedItem();
                    if (item.equals(selectedReservation)) {
                        pseudoClassStateChanged(pastReservationSelectedClass, true);
                    } else {
                        pseudoClassStateChanged(pastReservationClass, true);
                    }
                    reservationsTableView.refresh();
                }
            }
        });
    }

    public void addDeleteButtonListener(Button button, Reservation reservation) {
        button.setOnAction(new EventHandler<>() {
            @Override
            public void handle(ActionEvent arg0) {
                restClient.deleteReservation(reservation.getReservationId());
                sceneNavigator.loadSceneToMainWindow(sceneNavigator.PROFILE, null);
            }
        });
    }

    /**
     * Adds a click listener to the edit button. This <code>handle</code> method is
     * called when the user clicks on the edit button. Inside the method the
     * necessary information from the <code>Reservation</code> object is passed to a
     * data list. Next, loads the reservation scene is loaded in which the user can
     * then edit the selected reservation.
     *
     * @param button      the edit button
     * @param reservation the <code>Reservation</code> object on which was clicked
     */
    public void addEditButtonListener(Button button, Reservation reservation) {
        button.setOnAction(new EventHandler<>() {
            @Override
            public void handle(ActionEvent arg0) {
                sceneNavigator.setPreviousScene(sceneNavigator.PROFILE);

                String[] data = {
                        reservation.getCompany(),
                        reservation.getFlight(),
                        reservation.getLaunchTime(),
                        Utils.dayMonthYearDateFormat.format(reservation.getDate()),
                        reservation.getCompanyId() + "",
                        reservation.getFlightId() + "",
                        reservation.getFlightDateId() + "",
                        reservation.getReservationId() + "",
                        reservation.getReservedSeats() + "",
                        reservation.getSeatNumbers()
                };
                // Load reservation scene with data
                sceneNavigator.loadSceneToMainWindow(sceneNavigator.RESERVATION, Arrays.asList(data));
            }
        });
    }

    /**
     * Is called when the user clicks on a table row. <br>
     * <br>
     * Determines which row was clicked (index and item). Computes the position
     * where the delete and modify button should be added for the selected
     * reservation and adds both buttons. If the selected reservation is in the
     * past, both buttons get disabled.
     */
    @FXML
    public void onReservationItemClicked() {
        // clear all elements in button grid pane
        buttonGridPane.getChildren().clear();

        // get index and content of clicked row
        int index = reservationsTableView.getSelectionModel().getSelectedIndex();
        Reservation res = reservationsTableView.getSelectionModel().getSelectedItem();

        // only if not clicked on table header
        if (res != null) {
            // add empty pane in button grid pane so that buttons will be added right
            // underneath the pane
            Pane pane = new Pane();
            int height = (index + 1) * tableRowHeight - 3;
            pane.setMinHeight(height);
            pane.setMaxHeight(height);
            buttonGridPane.add(pane, 0, 0);

            // add buttons for edit and delete with picture and respective click listener
            Button editButton = addButtonWithIcon("/images/edit_icon.png", 0, 1, false, res);
            Button deleteButton = addButtonWithIcon("/images/delete_icon.png", 1, 1, true, res);

            // if reservation is in the past, disable both buttons
            if (Utils.isPastDate(res.getDate())) {
                editButton.setDisable(true);
                deleteButton.setDisable(true);
            }
        }
    }


    /**
     * Overrides the method from the <code>Initializable</code> interface which is
     * called when the fxml layout is loaded.<br>
     * Initializes the text of all labels and the table view showing the
     * reservations.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("ProfilePanel - Initialize called");
        System.out.println("ProfilePanel - User state: " + (restClient.getUser() != null));

        firstnameLabel.setText(restClient.getUser().getFirstName());
        lastnameLabel.setText(restClient.getUser().getLastName());
        emailLabel.setText(restClient.getUser().getEmail());

        initializeReservationTableView();

        // Initialize progress bar
        tokensProgressBar.setProgress(0.0);
        tokensProgressBar.setStyle("-fx-accent: #dc3545;"); // Start with red

        // Initialize the tokens display
        initializeTokensComponents();


        // Add click handler for redeem button
        redeemTokensButton.setOnAction(event -> onRedeemActionClicked());
    }

    /**
     * Sets the height of all table rows and of the header to the same size. Binds
     * the height of the complete table view to the overall height of all rows plus
     * header height. This way no scrollbar is added to the table view when to many
     * rows are added. The scrollbar is however contained in the PROFILE layout so
     * that nevertheless all table entries are shown.
     */
    private void setTableRowAndHeaderHeight() {
        // set item count (+1 for table header) and set row height
        int tableItemCount = Bindings.size(reservationsTableView.getItems()).get() + 1;
        reservationsTableView.setFixedCellSize(tableRowHeight);

        // set total height of table view so that no empty rows are shown/ no scroll bar
        // is inserted
        reservationsTableView.prefHeightProperty()
                .bind(reservationsTableView.fixedCellSizeProperty().multiply(tableItemCount));
        reservationsTableView.minHeightProperty().bind(reservationsTableView.prefHeightProperty());
        reservationsTableView.maxHeightProperty().bind(reservationsTableView.prefHeightProperty());

        // set height of table header to same size as table rows
        reservationsTableView.skinProperty().addListener((obs, ol, ne) -> {
            Pane header = (Pane) reservationsTableView.lookup("TableHeaderRow");
            header.prefHeightProperty().bind(reservationsTableView.prefHeightProperty().divide(tableItemCount));
            header.minHeightProperty().bind(header.prefHeightProperty());
            header.maxHeightProperty().bind(header.prefHeightProperty());
        });
    }

    /**
     * Creates a new button with the specified image on it. Adds a listener to the
     * button which is dependent on the type of button that is created (either
     * delete or edit button). Then adds the button to the button grid (right next
     * to the table view and right beneath the previously added empty pane in
     * {@link #onReservationItemClicked()})
     *
     * @param path           the path to the image
     * @param col            the number for the column
     * @param row            the number for the row
     * @param isDeleteButton the flag specifying if a delete button is added
     * @param reservation    the <code>Reservation</code> object on which was
     *                       clicked
     */
    public Button addButtonWithIcon(String path, int col, int row, boolean isDeleteButton, Reservation reservation) {
        ImageView imageview = new ImageView(new Image(path));
        imageview.setFitWidth(15);
        imageview.setPreserveRatio(true);
        Button button = new Button();
        button.setGraphic(imageview);
        button.setPadding(new Insets(5, 5, 5, 5));

        if (isDeleteButton) {
            addDeleteButtonListener(button, reservation);
        } else {
            addEditButtonListener(button, reservation);
        }
        buttonGridPane.add(button, col, row);
        return button;
    }

    /**
     * If the user selects a table view row with key arrows up or down, the same
     * functionality is performed as when the user clicked. This means
     * {@link #onReservationItemClicked()} is called.
     *
     * @param event the action which caused the method call
     */
    @FXML
    public void onKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.UP || event.getCode() == KeyCode.DOWN) {
            onReservationItemClicked();
        }
    }

    /**
     * Display of user tokens.
     * This method updates the progress bar and choice box based on the user's current token count.
     * It runs on the JavaFX Application Thread to ensure UI updates are thread-safe.
     */
    private void initializeTokensComponents() {
        // Get user's current token balance
        int userTokens = restClient.getUser().getTokens();

        // Set up progress bar (assuming max tokens is 100 for simplicity)
        final int MAX_TOKENS = 100;
        double progress = (double) userTokens / MAX_TOKENS;
        tokensProgressBar.setProgress(progress);

        // Update progress bar color based on tokens
        if (userTokens >= 70) {
            tokensProgressBar.setStyle("-fx-accent: #28a745;"); // Green
        } else if (userTokens >= 30) {
            tokensProgressBar.setStyle("-fx-accent: #ffc107;"); // Yellow
        } else {
            tokensProgressBar.setStyle("-fx-accent: #dc3545;"); // Red
        }

        // Set up choice box with available token amounts
        redeemTokensChoiceBox.getItems().clear();
        redeemTokensChoiceBox.setPromptText("Select");

        if (userTokens == 0) {
            redeemTokensChoiceBox.setDisable(true);
        } else {
            redeemTokensChoiceBox.setDisable(false);
            for (int i = 1; i <= userTokens; i++) {
                redeemTokensChoiceBox.getItems().add(i);
            }
        }
    }


    @FXML
    public void onRedeemActionClicked() {
        Integer tokensToRedeem = redeemTokensChoiceBox.getValue();
        if (tokensToRedeem == null) {
            // Show error message to user
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Invalid Selection");
            alert.setContentText("Please select the number of tokens to redeem.");
            alert.showAndWait();
            return;
        }

        if (restClient.redeemTokens(tokensToRedeem)) {
            initializeTokensComponents();

            // Show success message
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText("Tokens Redeemed");
            alert.setContentText("Successfully redeemed " + tokensToRedeem + " tokens.");
            alert.showAndWait();
        } else {
            // Show error message
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Redemption Failed");
            alert.setContentText("Failed to redeem tokens. Please try again.");
            alert.showAndWait();
        }
    }
}