package gui.controller.authentication;

import com.google.gson.JsonObject;
import gui.controller.Controller;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.User;
import utils.Utils;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * DO NOT CHANGE THE CONTENT OF THIS CLASS, UNLESS YOU WANT TO IMPLEMENT
 * ADVANCED OPERATIONS! <br>
 * <br>
 * Handles the process of signing up.
 */
public class SignUpDialogController extends Controller implements Initializable {

    // FXML components
    @FXML
    private TextField firstnameTextField;
    @FXML
    private TextField lastnameTextField;
    @FXML
    private TextField emailTextField;
    @FXML
    private TextField passwordTextField;
    @FXML
    private TextField passwordRepeatTextField;
    @FXML
    private Label errorMessageLabel;

    /**
     * Is called when the user clicks the sign up button. <br>
     * <br>
     * Retrieves information from all textfields and checks if all requirements for
     * the input are met. This also includes the attempt to create the user, as it
     * might be possible that the given email is already taken. If the new user
     * could be created, the scene is change to the MainWindow and a message is
     * displayed that the sign up was successful.
     */
    @FXML
    public void onSignupButtonClicked() {
        errorMessageLabel.setText("");
        // extract entered information
        String firstname = firstnameTextField.getText();
        String lastname = lastnameTextField.getText();
        String email = emailTextField.getText();
        String password = passwordTextField.getText();
        String passwordRepeat = passwordRepeatTextField.getText();

        // Error handling
        if (firstname.isEmpty() || lastname.isEmpty() || email.isEmpty() || password.isEmpty()
                || passwordRepeat.isEmpty()) {
            errorMessageLabel.setText("All text fields must be filled out!");
        } else if (!Utils.isAlpha(firstname) || !Utils.isAlpha(lastname)) {
            errorMessageLabel.setText("First and last name are not allowed to contain numbers.");
        } else if (!Utils.isValidEmailAddress(email)) {
            errorMessageLabel.setText("No valid e-mail address.");
        } else if (!Utils.isValidPassword(password)) {
            errorMessageLabel.setText("The password must contain at least 8 characters.");
        } else if (!password.equals(passwordRepeat)) {
            errorMessageLabel.setText("The passwords do not correspond to each other.");
        } else if (!restClient.createNewUser(firstname, lastname, email, password)) {
            errorMessageLabel.setText("E-mail address is already taken.");
        } else { // customer could be created successfully
            // create user and pass to RestClient (split up necessary because of
            // authorization)
            User user = new User(firstname, lastname, email, password);
            restClient.setUser(user);
            JsonObject clientInfo = restClient.getUserInfoByMail(email);
            user.setId(clientInfo.get("id").getAsInt());

            // load main window and set panel to successful sign up panel
            sceneNavigator.loadCompleteWindow(sceneNavigator.MAIN_WINDOW,
                    (Stage) emailTextField.getScene().getWindow());

            List<String> controllerData = new ArrayList<>();
            controllerData.add("Welcome!");
            controllerData.add("You are now successfully registered.");
            if (sceneNavigator.getPreviousScene() != null) {
                controllerData.add("To reservation");
            }
            sceneNavigator.loadSceneToMainWindow(sceneNavigator.SUCCESS_PANEL, controllerData);
            sceneNavigator.loadToolBar(sceneNavigator.LOG_OUT_BAR);
        }
    }

    /**
     * Is called when the user clicks the cancel button. <br>
     * <br>
     * Loads the MainWindow to the complete stage and the log in bar to the
     * MainWindow. If the previous scene is not <code>null</code>, this means the
     * user came from the reservation scene, the previous scene is loaded to the
     * MainWindow and afterwards reset to <code>null</code>. Otherwise, the previous
     * scene is <code>null</code> meaning the user came from the log in or sign up
     * scene.
     */
    @FXML
    public void onCancelButtonClicked() {
        sceneNavigator.loadCompleteWindow(sceneNavigator.MAIN_WINDOW, (Stage) emailTextField.getScene().getWindow());
        sceneNavigator.loadToolBar(sceneNavigator.LOG_IN_BAR);

        if (sceneNavigator.getPreviousScene() != null) {
            sceneNavigator.loadSceneToMainWindow(sceneNavigator.getPreviousScene(),
                    sceneNavigator.getDataPreviousScene());

            sceneNavigator.setPreviousScene(null);
            sceneNavigator.setDataPreviousScene(null);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
}
