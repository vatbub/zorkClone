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


/**
 * Sample Skeleton for 'BasicApplication_i18n.fxml' Controller Class
 */

import common.Common;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import logging.FOKLogger;
import model.Game;
import model.Room;
import model.WalkDirection;

import java.net.URL;
import java.util.*;
import java.util.logging.Level;

public class EditorView extends Application {

    private static FOKLogger log;

    private Game currentGame;
    // Unconnected Rooms will not be saved but need to be hold in the RAM while editing
    private List<RoomRectangle> unconnectedRooms = new ArrayList<>();

    public static void main(String[] args) {
        common.Common.setAppName("zork");
        log = new FOKLogger(MainWindow.class.getName());
        for (String arg : args) {
            if (arg.toLowerCase().matches("mockappversion=.*")) {
                // Set the mock version
                String version = arg.substring(arg.toLowerCase().indexOf('=') + 1);
                Common.setMockAppVersion(version);
            } else if (arg.toLowerCase().matches("mockbuildnumber=.*")) {
                // Set the mock build number
                String buildnumber = arg.substring(arg.toLowerCase().indexOf('=') + 1);
                Common.setMockBuildNumber(buildnumber);
            } else if (arg.toLowerCase().matches("mockpackaging=.*")) {
                // Set the mock packaging
                String packaging = arg.substring(arg.toLowerCase().indexOf('=') + 1);
                Common.setMockPackaging(packaging);
            } else if (arg.toLowerCase().matches("locale=.*")) {
                // set the gui language
                String guiLanguageCode = arg.substring(arg.toLowerCase().indexOf('=') + 1);
                log.getLogger().info("Setting language: " + guiLanguageCode);
                Locale.setDefault(new Locale(guiLanguageCode));
            }
        }

        launch(args);
    }

    public static ResourceBundle bundle;
    private Stage stage;

    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML // fx:id="insertRoom"
    private Button insertRoom; // Value injected by FXMLLoader

    @FXML // fx:id="drawing"
    private Group drawing; // Value injected by FXMLLoader

    @FXML // fx:id="insertPath"
    private Button insertPath; // Value injected by FXMLLoader

    @FXML
    private ScrollPane scrollPane;

    @FXML
    void insertRoomOnAction(ActionEvent event) {
        RoomRectangle room = new RoomRectangle();
        room.setX(1000 * Math.abs(Math.random()));
        room.setY(1000 * Math.abs(Math.random()));
        unconnectedRooms.add(room);
        renderView(room);
    }

    @FXML
    void insertPathOnAction(ActionEvent event) {

    }

    @FXML
    void drawingOnMousePressed(MouseEvent event) {
        if (!event.isControlDown()) {
            System.out.println("unselected everything");
            unselectEverything();
        }
    }

    public void unselectEverything() {
        for (Node child : drawing.getChildren()) {
            if (child instanceof RoomRectangle) {
                ((RoomRectangle) child).setSelected(false);
            }
        }
    }

    private void renderView() {
        RoomRectangle startRoom = new RoomRectangle(this.currentGame.getCurrentRoom());
        startRoom.setX(400);
        startRoom.setY(400);
        renderView(startRoom);

        // render unconnected rooms
        for (RoomRectangle room : unconnectedRooms) {
            renderView(room);
        }
    }

    private void renderView(RoomRectangle currentRoom) {
        drawing.getChildren().add(currentRoom);
        for (Map.Entry<WalkDirection, Room> entry : currentRoom.getRoom().getAdjacentRooms().entrySet()) {
            RoomRectangle newRoom = new RoomRectangle(entry.getValue());

            switch (entry.getKey()) {
                case NORTH:
                    newRoom.setY(currentRoom.getY() - 15);
                    break;
                case WEST:
                    newRoom.setX(currentRoom.getX() - 15);
                    break;
                case EAST:
                    newRoom.setX(currentRoom.getX() + 15);
                    break;
                case SOUTH:
                    newRoom.setY(currentRoom.getY() + 15);
                    break;
                case NORTH_WEST:
                    newRoom.setY(currentRoom.getY() - 15);
                    newRoom.setX(currentRoom.getX() - 15);
                    break;
                case NORTH_EAST:
                    newRoom.setY(currentRoom.getY() - 15);
                    newRoom.setX(currentRoom.getX() + 15);
                    break;
                case SOUTH_WEST:
                    newRoom.setY(currentRoom.getY() + 15);
                    newRoom.setX(currentRoom.getX() - 15);
                    break;
                case SOUTH_EAST:
                    newRoom.setY(currentRoom.getY() + 15);
                    newRoom.setX(currentRoom.getX() + 15);
                    break;
            }

            renderView(newRoom);
        }
    }

    @FXML
        // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        assert insertRoom != null : "fx:id=\"insertRoom\" was not injected: check your FXML file 'EditorMain.fxml'.";
        assert drawing != null : "fx:id=\"drawing\" was not injected: check your FXML file 'EditorMain.fxml'.";
        assert insertPath != null : "fx:id=\"insertPath\" was not injected: check your FXML file 'EditorMain.fxml'.";

        currentGame = new Game();
        renderView();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        bundle = ResourceBundle.getBundle("view.strings");

        // appConfig = new Config();

        stage = primaryStage;
        try {
            Parent root = FXMLLoader.load(getClass().getResource("EditorMain.fxml"), bundle);

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("EditorMain.css").toExternalForm());

            primaryStage.setTitle(bundle.getString("windowTitle"));

            primaryStage.setMinWidth(scene.getRoot().minWidth(0) + 70);
            primaryStage.setMinHeight(scene.getRoot().minHeight(0) + 70);

            primaryStage.setScene(scene);

            // Set Icon
            primaryStage.getIcons().add(new Image(MainWindow.class.getResourceAsStream("icon.png")));

            primaryStage.show();
        } catch (Exception e) {
            log.getLogger().log(Level.SEVERE, "An error occurred", e);
        }
    }
}
