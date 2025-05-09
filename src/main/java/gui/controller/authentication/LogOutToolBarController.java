package gui.controller.authentication;

import gui.controller.Controller;
import javafx.fxml.FXML;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * DO NOT CHANGE THE CONTENT OF THIS CLASS, UNLESS YOU WANT TO IMPLEMENT
 * ADVANCED OPERATIONS! <br>
 * <br>
 * Controls the tool bar when a user is logged in.
 */
public class LogOutToolBarController extends Controller {

    /**
     * Is called when the user clicks the profile button. <br>
     * <br>
     * Loads the profile scene to the GUI.
     */
    @FXML
    public void onProfileButtonClicked() {
        sceneNavigator.loadSceneToMainWindow(sceneNavigator.PROFILE, null);
    }

    /**
     * Is called when the user clicks the log out button. <br>
     * <br>
     * Resets the logged in user to <code>null</code>, loads the other toolbar and a
     * scene showing a message for the successful log out.
     */
    @FXML
    public void onLogOutButtonClicked() {
        restClient.setUser(null);
        sceneNavigator.loadToolBar(sceneNavigator.LOG_IN_BAR);

        List<String> data = new ArrayList<>();
        data.add("Goodbye!");
        data.add("You are now successfully logged out.");
        sceneNavigator.loadSceneToMainWindow(sceneNavigator.SUCCESS_PANEL, data);
    }

    // Add filter button click handler
    // This method will be called when the filter button is clicked
    @FXML
    public void onFilterButtonClicked() {
        sceneNavigator.openPopupWindow(sceneNavigator.DATE_FILTER_POPUP, "Select Date", Collections.emptyList());
    }

}
