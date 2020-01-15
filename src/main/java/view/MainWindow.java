package view;

/*-
 * #%L
 * Zork Clone
 * %%
 * Copyright (C) 2016 Frederik Kammel
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import com.github.vatbub.common.core.Common;
import com.github.vatbub.common.core.logging.FOKLogger;
import com.github.vatbub.common.updater.UpdateChecker;
import com.github.vatbub.common.updater.UpdateInfo;
import com.github.vatbub.common.updater.view.UpdateAvailableDialog;
import com.github.vatbub.common.view.core.ExceptionAlert;
import com.github.vatbub.common.view.reporting.ReportingDialog;
import common.AppConfig;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import model.Game;
import parser.Parser;

import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;

public class MainWindow extends Application {

    public static ResourceBundle bundle;
    private static boolean disableUpdateChecks;
    private static Stage stage;
    @SuppressWarnings("CanBeFinal")
    Game currentGame = new Game();
    @SuppressWarnings("unused")
    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;
    @SuppressWarnings("unused")
    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;
    @FXML // fx:id="commandLine"
    private TextField commandLine; // Value injected by FXMLLoader
    @FXML // fx:id="getAvailableCommandsButton"
    private Button getAvailableCommandsButton; // Value injected by FXMLLoader
    @FXML
    private WebView messageView;

    public static void main(String[] args) {
        Common.getInstance().setAppName("zork");
        Common.getInstance().setAwsAccessKey(AppConfig.awsLogAccessKeyID);
        Common.getInstance().setAwsSecretAccessKey(AppConfig.awsLogSecretAccessKeyID);
        FOKLogger.enableLoggingOfUncaughtExceptions();
        // modify the default exception handler to show the ReportingDialog on every uncaught exception
        final Thread.UncaughtExceptionHandler currentUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((thread, exception) -> {
            if (currentUncaughtExceptionHandler != null) {
                // execute current handler as we only want to append it
                currentUncaughtExceptionHandler.uncaughtException(thread, exception);
            }
            Platform.runLater(() -> {
                new ExceptionAlert(exception).showAndWait();
                new ReportingDialog().show(AppConfig.gitHubUserName, AppConfig.gitHubRepoName, exception);
            });
        });
        for (String arg : args) {
            if (arg.toLowerCase().matches("mockappversion=.*")) {
                // Set the mock version
                String version = arg.substring(arg.toLowerCase().indexOf('=') + 1);
                Common.getInstance().setMockAppVersion(version);
            } else if (arg.toLowerCase().matches("mockbuildnumber=.*")) {
                // Set the mock build number
                String buildnumber = arg.substring(arg.toLowerCase().indexOf('=') + 1);
                Common.getInstance().setMockBuildNumber(buildnumber);
            } else if (arg.toLowerCase().matches("disableupdatechecks")) {
                FOKLogger.info(MainWindow.class.getName(), "Update checks are disabled as app was launched from launcher.");
                disableUpdateChecks = true;
            } else if (arg.toLowerCase().matches("mockpackaging=.*")) {
                // Set the mock packaging
                String packaging = arg.substring(arg.toLowerCase().indexOf('=') + 1);
                Common.getInstance().setMockPackaging(packaging);
            } else if (arg.toLowerCase().matches("locale=.*")) {
                // set the gui language
                String guiLanguageCode = arg.substring(arg.toLowerCase().indexOf('=') + 1);
                FOKLogger.info(MainWindow.class.getName(), "Setting language: " + guiLanguageCode);
                Locale.setDefault(new Locale(guiLanguageCode));
            }
        }

        launch(args);
    }

    @FXML
        // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        assert commandLine != null : "fx:id=\"commandLine\" was not injected: check your FXML file 'BasicApplication_i18n.fxml'.";
        assert getAvailableCommandsButton != null : "fx:id=\"getAvailableCommandsButton\" was not injected: check your FXML file 'BasicApplication_i18n.fxml'.";
        currentGame.getMessages().add(new GameMessage("ZORK I: The Great Underground Empire\nCopyright (c) 1981, 1982, 1983 Infocom, Inc. All rights reserved.\nZORK is a registered trademark of Infocom, Inc.\n Revision " + Common.getInstance().getAppVersion() + "-" + Common.getInstance().getBuildNumber() + "\n\nThis game is not yet functional. Give the team some time and come back in some time. See ya :)", true));
        updateCommandView();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        bundle = ResourceBundle.getBundle("view.strings");
        stage = primaryStage;

        try {
            Thread updateThread = new Thread(() -> {
                UpdateInfo update = UpdateChecker.isUpdateAvailable(AppConfig.getUpdateRepoBaseURL(),
                        AppConfig.groupID, AppConfig.artifactID, AppConfig.updateFileClassifier,
                        Common.getInstance().getPackaging());
                if (update.showAlert) {
                    Platform.runLater(() -> new UpdateAvailableDialog(update));
                }
            });
            updateThread.setName("updateThread");

            if (!disableUpdateChecks) {
                updateThread.start();
            }

            Parent root = FXMLLoader.load(getClass().getResource("MainWindow.fxml"), bundle);

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("MainWindow.css").toExternalForm());

            primaryStage.setTitle(bundle.getString("windowTitle"));

            primaryStage.setMinWidth(scene.getRoot().minWidth(0) + 70);
            primaryStage.setMinHeight(scene.getRoot().minHeight(0) + 70);

            primaryStage.setScene(scene);

            // Set Icon
            primaryStage.getIcons().add(new Image(MainWindow.class.getResourceAsStream("icon.png")));

            primaryStage.show();
        } catch (Exception e) {
            FOKLogger.log(MainWindow.class.getName(), Level.SEVERE, "An error occurred", e);
        }
    }

    @FXML
    void commandLineOnKeyPressed(KeyEvent event) {
        if (event.getCode().equals(KeyCode.ENTER)) {
            String playerMessage = this.commandLine.getText();
            currentGame.getMessages().add(new GameMessage(playerMessage, false));
            currentGame.getMessages().add(new GameMessage(Parser.parse(playerMessage), true));
            this.commandLine.setText("");
            updateCommandView();
        }
    }

    @FXML
    void fileBugMenuItemOnAction(@SuppressWarnings("unused") ActionEvent event) {
        new ReportingDialog(stage.getScene()).show(AppConfig.gitHubUserName, AppConfig.gitHubRepoName);
    }

    public void updateCommandView() {
        String html = HTMLGenerator.generate(currentGame.getMessages());
        this.messageView.getEngine().loadContent(html);
    }
}
