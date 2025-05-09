package gui.controller.authentication;

import gui.controller.Controller;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.scene.control.Alert;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.net.URL;
import java.util.Base64;
import java.util.Collections;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.JsonObject;

/**
 * DO NOT CHANGE THE CONTENT OF THIS CLASS, UNLESS YOU WANT TO IMPLEMENT
 * ADVANCED OPERATIONS! <br>
 * <br>
 * Controls the tool bar when no user is logged in.
 */
public class LogInToolBarController extends Controller implements Initializable {

    // FXML components
    @FXML
    private Button logInButton;



    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {

    }
    /**
     * Is called when the user clicks the log in button. <br>
     * <br>
     * Loads the log in scene to the GUI.
     */
    @FXML
    public void onLogInButtonClicked() {
        sceneNavigator.loadCompleteWindow(sceneNavigator.LOG_IN_DIALOG, (Stage) logInButton.getScene().getWindow());
    }

    /**
     * Is called when the user clicks the sign up button. <br>
     * <br>
     * Loads the sign up scene to the GUI.
     */
    @FXML
    public void onSignUpButtonClicked() {
        sceneNavigator.loadCompleteWindow(sceneNavigator.SIGN_UP_DIALOG, (Stage) logInButton.getScene().getWindow());
    }

    // Add new filter button click handler
    // This method will be called when the filter button is clicked
    @FXML
    public void onFilterButtonClicked() {
        sceneNavigator.openPopupWindow(sceneNavigator.DATE_FILTER_POPUP, "Select Date", Collections.emptyList());
    }
}
