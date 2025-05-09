package gui.controller.information;

import com.google.gson.JsonObject;
import gui.controller.Controller;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import utils.Utils;

import java.util.*;

public class InformationPanelController extends Controller {

    // ------------------ class attributes ------------------
    // variables for storing current selected company and flight ids
    private int companyId;
    private int flightId;

    // list of all flight IDs associated with the company
    private final List<Integer> flightIds = new ArrayList<>();
    // list of lists containing date and time IDs for each flight
    private List<List<String>> flightDateIds = new ArrayList<>();

    // tracks the last clicked flight in the list
    private int lastClickedFlightIndex = -1;
    // store last clicked date and launch time index (corresponds to column and row index)
    private int lastClickedDateIndex = 0;
    private int lastClickedLaunchTimeIndex = 0;

    // store all ListView components for flight launch times
    private List<ListView<String>> launchTimeListViews = new ArrayList<>();
    // ------------------------------------------------------

    // ------------------ FXML components ------------------
    /**
     * The following attributes all describe features of the selected item (company).
     * The attribute and fx:id of the corresponding component in the InformationPanel.fxml file are renamed.
     */
    @FXML
    private Label companyNameLabel;
    @FXML
    private Label locationLabel;
    @FXML
    private Label descriptionLabel;
    @FXML
    private ListView<String> flightListView;
    /**
     * The following attributes all describe features of the selected subitem (flight)
     * The attribute and fx:id of the corresponding component in the InformationPanel.fxml file are renamed.
     */
    @FXML
    private VBox flightInformationPanel;
    @FXML
    private Label flightNameLabel; // displays the name of the selected flight
    @FXML
    private Label viewTypeLabel; // displays the view type for the selected flight
    @FXML
    private Label flightDurationLabel;

    // ------------- DO NOT CHANGE THE FOLLOWING PART OF THE CODE -------------
    @FXML
    private GridPane flightScheduleGridPane;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private ScrollPane scheduleWrapScrollPane;

    @FXML
    private Button reserveButton;
    @FXML
    private Label messageLabel;
    // ------------------------------------------------------

    /**
     * Hides the <code>InformationPanel</code> because at the beginning no
     * flight is selected. Is shown later, when user selects a flight.<br>
     * <br>
     * Retrieves delivered data from previous scene and sets the text of all labels.<br>
     * If currently the space company has no flight to launch, create a list with one dummy
     * JSON object containing a message which states that no flights are available.<br>
     * At the end initializes the list view in which all flights are displayed.
     */
    @Override
    public <T> void initializeSceneData(List<T> data) {
        // ------------- DO NOT CHANGE THE FOLLOWING PART OF THE CODE -------------
        // at first hide space flight information panel
        flightInformationPanel.setVisible(false);
        flightInformationPanel.setMinHeight(0);
        flightInformationPanel.setPrefHeight(0);
        // disable reservation until a launch time is selected
        setReservable(false);
        // get the selected company ID from data list
        try {
            if (data.get(0) instanceof Integer) {
                companyId = (Integer) data.get(0);
            } else if (data.get(0) instanceof String) {
                companyId = Integer.parseInt((String) data.get(0));
            } else {
                throw new IllegalArgumentException("Unexpected data type for company ID");
            }
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        }
        // ---------------------------------- END ----------------------------------

        // retrieve company and flight information via REST client
        JsonObject companyJson = restClient.requestCompanyInformation(companyId);
        List<JsonObject> flightJson = restClient.requestFlightsOfCompany(companyId);

       // set the text for company labels based on retrieved data
        companyNameLabel.setText(companyJson.get("name").getAsString());
        locationLabel.setText(companyJson.get("location").getAsString());
        descriptionLabel.setText(companyJson.get("description").getAsString());

        // if currently no flights are available, show a message instead of flight list
        if (flightJson == null) {
            ObservableList<String> obsList = createNoItemsAvailableList(flightListView,
                    "Currently no flights available.");
            flightListView.setItems(obsList);
        } else {
            // populate the flight list view with flight names
            initializeFlightListView(flightJson);
        }
    }

    /**
     * Extracts all unique flights names from the list of space flights. Determines the
     * corresponding ids for the flights and sets the items of
     * <code>flightListView</code> to the unique flights.
     *
     * @param flightJson the list containing all flights as <code>JsonObject</code>
     */
    private void initializeFlightListView(List<JsonObject> flightJson) {
        // initializes flight list by extracting unique flight names
        List<String> uniqueFlights = Utils.getUniqueItems(flightJson, "name");

        // map flight names to their corresponding IDs
        for (String flight : uniqueFlights) {
            // iterate over every entry in flight json
            for (JsonObject json : flightJson) {
                if (json.get("name").getAsString().equals(flight)) {
                    flightIds.add(json.get("flightId").getAsInt());
                    break;
                }
            }
        }
        // set the flight names in the flight list view
        flightListView.setItems(FXCollections.observableArrayList(uniqueFlights));
    }

    /**
     * Is called when the user clicks on an item in the
     * <code>flightListView</code>.<br>
     * <br>
     * Only if the user clicked not on an empty list item and the selected item is
     * different to the previously selected item, retrieves flight information and
     * initializes the <code>InformationPanel</code>, including the view for showing the flight schedules.
     */
    public void onSubitemListItemClicked() {
        // get the index and name of the selected flight
        int clickedIndex = flightListView.getSelectionModel().getSelectedIndex();
        String subitem = flightListView.getSelectionModel().getSelectedItem();

        // only proceed if a valid item is selected and it's different from the last selection
        if (clickedIndex != -1 && clickedIndex != lastClickedFlightIndex) {
            lastClickedFlightIndex = clickedIndex;
            flightId = flightIds.get(clickedIndex);
            // disable reserve button and show message
            setReservable(false);

            // --------------- ONLY MAKE CHANGES TO THE CODE IN THIS AREA ---------------
            // retrieve detailed flight information via REST client
            List<JsonObject> flightJson = restClient.requestFlightInformationOfCompany(companyId, flightId);

            System.out.println("Flight data received: " + flightJson);
            if (flightJson != null) {
                for (JsonObject json : flightJson) {
                    System.out.println("launch_time: " + json.get("launch_time") +  ", flightDateId: " +
                            json.get("flightDateId"));
                }
            }

            // update UI components with the retrieved flight details
            flightNameLabel.setText(subitem);
            viewTypeLabel.setText(flightJson.get(0).get("view_type").getAsString());
            flightDurationLabel.setText(flightJson.get(0).get("flight_duration").getAsString() + " min");
            // ---------------------------------- END ----------------------------------

            // ------------- DO NOT CHANGE THE FOLLOWING PART OF THE CODE -------------
            // initialize the schedule view for the selected flight
            initializeViewForSubsubitems(flightJson);

            // make the flight information panel visible
            flightInformationPanel.setMinHeight(Control.USE_COMPUTED_SIZE);
            flightInformationPanel.setPrefHeight(Control.USE_COMPUTED_SIZE);
            flightInformationPanel.setVisible(true);

            // automatically scroll to bottom of page
            Animation animation = new Timeline(
                    new KeyFrame(Duration.seconds(0.5), new KeyValue(scrollPane.vvalueProperty(), 1)));
            animation.play();
            // ---------------------------------- END ----------------------------------
        }
    }

    /**
     * Initializes the GridPane which contains the flight schedule information. Retrieves
     * unique dates and sorts them. Adds a column for each date to the GridPane and
     * a ListView containing the times when the space flight is shown for this date.
     *
     * @param flightJson the list of <code>JsonObject</code>, each containing information of one flight
     */
    public void initializeViewForSubsubitems(List<JsonObject> flightJson) {
        // ------------- DO NOT CHANGE THE FOLLOWING PART OF THE CODE -------------
        // reset variables so that previous information will not be regarded
        flightScheduleGridPane.getChildren().clear();
        flightScheduleGridPane.getColumnConstraints().clear();
        launchTimeListViews = new ArrayList<>();
        flightDateIds = new ArrayList<>();
        // ---------------------------------- END ----------------------------------

        // get unique dates, reformat them and remove dates before today's date
        List<Date> dates = Utils.parseListOfStringsToDates(Utils.getUniqueItems(flightJson, "date"),
                Utils.monthDayYearDateFormat);
        Collections.sort(dates);

        // iterate over each date
        for (int dateIndex = 0; dateIndex < dates.size(); dateIndex++) { // rename i to dateIndex for clarity
            Date currDate = dates.get(dateIndex);
            initializeTableColumnsAndConstraints(Utils.dayMonthDateFormat.format(currDate), dateIndex, 80);

            // populate the column with flight times
            List<JsonObject> columnData = Utils.getItemsWithValue(flightJson, "date",
                    Utils.monthDayYearDateFormat.format(currDate));
            initializeDetailListViewAsColumn(columnData, dateIndex, "launch_time",
                    "flightDateId", true);
        }
    }

    /**
     * Is called when the reserve button is clicked.<br>
     * <br>
     * Retrieves all necessary information for the reservation scene: space company, flights,
     * date, launch time and company id, flight id, flight schedule id. Gathers information in
     * list and shows reservation scene with this information.
     */
    @FXML
    public void onReserveButtonClicked() {
        sceneNavigator.setPreviousScene(sceneNavigator.INFO);
        // retrieve information from GUI
        String flight = flightListView.getSelectionModel().getSelectedItem();
        // * 2 because GridPane also contains the list view for launch time
        String date = ((Label) flightScheduleGridPane.getChildren().get(lastClickedDateIndex * 2)).getText();
        String launchTime = launchTimeListViews.get(lastClickedDateIndex).getSelectionModel().getSelectedItem();
        String scheduleId = flightDateIds.get(lastClickedDateIndex).get(lastClickedLaunchTimeIndex);

        // load reservation view and initialize with current data
        String[] data = {companyNameLabel.getText(), flight, launchTime, date, companyId + "",
                flightId + "", scheduleId + ""};
        sceneNavigator.loadSceneToMainWindow(sceneNavigator.RESERVATION, Arrays.asList(data));
    }

    /*
     * --------------------------------------------------------------------
     * --------------- DO NOT CHANGE THE FOLLOWING METHODS! ---------------
     * --------------------------------------------------------------------
     */

    /**
     * Is called if there are no items to be displayed in the given ListView. Sets a
     * cell factory to the ListView to change it appearance and creates a
     * <code>ObservableList</code> containing the message to display instead of
     * elements.
     *
     * @param <T>      the type of items in the ListView
     * @param listView the ListView on which to set properties
     * @param text     the text to show in the ListView
     * @return the observable list
     */
    private <T> ObservableList<String> createNoItemsAvailableList(ListView<T> listView, String text) {
        // disable that items are clickable
        listView.setMouseTransparent(true);
        listView.setFocusTraversable(false);

        // change appearance of cell: text is centered and height of item is equal to height of list view
        listView.setCellFactory(lst -> new ListCell<>() {
			@Override
			protected void updateItem(T item, boolean empty) {
				super.updateItem(item, empty);
				// Create the HBox
				HBox hBox = new HBox();
				hBox.setAlignment(Pos.CENTER);
				hBox.setPrefHeight(listView.getPrefHeight() - 10);

				// Create centered Label
				Label label = new Label((String) item);
				label.setStyle("-fx-font-style: italic;");
				label.setAlignment(Pos.CENTER);

				hBox.getChildren().add(label);
				setGraphic(hBox);
			}
		});
        ObservableList<String> message = FXCollections.observableArrayList();
        message.add(text);
        return message;
    }
    private void initializeTableColumnsAndConstraints(String colName, int i, int width) {
        // add column name label
        Label label = new Label(colName);
        flightScheduleGridPane.add(label, i, 0);
        label.setPadding(new Insets(5, 0, 5, 5));
        label.getStyleClass().add("boldLabel");

        // set column constraints: every column should take equal width
        ColumnConstraints colConstraints = new ColumnConstraints();
        colConstraints.setPrefWidth(width);
        flightScheduleGridPane.getColumnConstraints().add(colConstraints);
    }

    /**
     * Creates a list view for all flight schedules of one date and adds it to the
     * GridPane. Adds all launch times as items to the list view and sets the
     * corresponding flight schedule ids. Adds a click listener for the list view.
     *
     * @param flightSchedulesOfDate the list of all flight schedules of one date
     * @param colIndex        the column index for the GridPane
     * @param itemName        the name of the key containing the detail information
     *                        to show in the table
     * @param idName          the name of the key containing the id that identifies
     *                        itemName
     * @param itemIsTime      <code>true</code> if itemName contains time values (in
     *                        12 hour format); <code>false</code> otherwise
     */
    private void initializeDetailListViewAsColumn(List<JsonObject> flightSchedulesOfDate, int colIndex,
                                                  String itemName, String idName, boolean itemIsTime) {
        if (flightSchedulesOfDate == null || flightSchedulesOfDate.isEmpty()) {
            ListView<String> emptyListView = new ListView<>();
            flightScheduleGridPane.add(emptyListView, colIndex, 1);
            launchTimeListViews.add(emptyListView);
            flightDateIds.add(new ArrayList<>());
            addListViewClickListener(emptyListView, colIndex);
            return;
        }

        Map<String, String> columnToId = new HashMap<>();

        // Handle time values
        if (itemIsTime) {
            for (JsonObject json : flightSchedulesOfDate) {
                try {
                    String timeValue = json.get(itemName).getAsString();
                    String idValue = json.get(idName).getAsString();

                    // Use the original time format directly without conversion
                    columnToId.put(timeValue, idValue);
                } catch (Exception e) {
                    System.err.println("Error processing time entry: " + e.getMessage());
                    continue;
                }
            }

            // Sort times if needed
            Map<String, String> sortedMap = new TreeMap<>(columnToId);
            columnToId = sortedMap;
        } else {
            // Handle non-time values using existing utility method
            columnToId = Utils.mapKeyToAnotherKeyFromJsonList(flightSchedulesOfDate, itemName, idName);
        }

        // create list view and add items
        ListView<String> columnListView = new ListView<>();
        ObservableList<String> obsList = FXCollections.observableArrayList(new ArrayList<>(columnToId.keySet()));
        columnListView.setItems(obsList);
        flightScheduleGridPane.add(columnListView, colIndex, 1);

        // Add to tracking lists
        launchTimeListViews.add(columnListView);
        flightDateIds.add(new ArrayList<>(columnToId.values()));

        // Add click listener
        addListViewClickListener(columnListView, colIndex);
    }

    /**
     * Adds a click listener to the given list view. The <code>handle</code> method
     * gets called when the user clicks on an item in the list view, this means
     * clicks on a specific launch time.<br>
     * Sets different indices and unselects all selections from other list views
     * showing flight schedules information. Checks if user selected a not empty list item
     * and either enables or disables the reserve button.
     *
     * @param timesListView the list view to add the listener
     * @param dateIndex     the index of the column from the GridPane
     */
    private void addListViewClickListener(ListView<String> timesListView, int dateIndex) {
        timesListView.setOnMouseClicked(new EventHandler<Event>() {
            @Override
            public void handle(Event arg0) {
                // update global variables saving which date and time was selected
                lastClickedDateIndex = dateIndex;
                lastClickedLaunchTimeIndex = timesListView.getSelectionModel().getSelectedIndex();
                // unselect items in all other listview except for the current one
                for (int k = 0; k < launchTimeListViews.size(); k++) {
                    if (k != lastClickedDateIndex) {
                        launchTimeListViews.get(k).getSelectionModel().clearSelection();
                    }
                }
                // only if clicked list item is not empty, enable reserve button and hide
                // message label
                setReservable(lastClickedLaunchTimeIndex != -1);
            }
        });
    }

    /**
     * If <code>ifReservable</code> is <code>true</code>, enables the reserve button
     * and hides the message. Otherwise, disables the reserve button and shows the
     * message. If the user is currently logged in, the user can make a reservation,
     * this means the method should be called with <code>isReservable</code> equals
     * to <code>true</code>.
     *
     * @param isReservable the flag specifying if user can reserve
     */
    private void setReservable(boolean isReservable) {
        reserveButton.setDisable(!isReservable);
        messageLabel.setVisible(!isReservable);
    }

    /*
     * --------------------------------------------------------
     * -------------------------- END -------------------------
     * --------------------------------------------------------
     */

}
