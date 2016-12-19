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


import common.Common;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ZoomEvent;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import logging.FOKLogger;
import model.Game;
import model.Room;
import model.WalkDirection;

import java.net.URL;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;

public class EditorView extends Application {

    private static FOKLogger log;

    public static EditorView currentEditorInstance;

    private boolean unselectingDisabled;

    private Game currentGame;
    // Unconnected Rooms will not be saved but need to be hold in the RAM while editing
    private RoomList unconnectedRooms = new RoomList();
    private RoomList allRoomsAsList;
    private EditMode currentEditMode;

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

    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML // fx:id="insertRoom"
    private Button insertRoom; // Value injected by FXMLLoader

    @FXML // fx:id="drawing"
    private CustomGroup drawing; // Value injected by FXMLLoader

    @FXML // fx:id="insertPath"
    private ToggleButton insertPath; // Value injected by FXMLLoader

    @FXML
    private ToggleButton moveButton;

    @FXML
    private Button autoLayoutButton;

    @FXML
    private Button resetButton;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private MenuItem menuItemClose;

    @FXML
    private MenuItem menuItemSave;

    @FXML
    private MenuItem menuItemSaveAs;

    @FXML
    void menuItemSaveOnAction(ActionEvent event) {

    }

    @FXML
    void menuItemSaveAsOnAction(ActionEvent event) {

    }

    @FXML
    void menuItemCloseOnAction(ActionEvent event) {
        Platform.exit();
    }

    @FXML
    void insertRoomOnAction(ActionEvent event) {
        log.getLogger().fine("Added room to game");
        RoomRectangle room = new RoomRectangle(null);

        int roomIndex;

        if (allRoomsAsList == null) {
            roomIndex = 0;
        } else {
            roomIndex = allRoomsAsList.size() + 1;
        }
        System.out.println("Setting room name: " + "Room " + roomIndex);
        room.getRoom().setName("Room " + roomIndex);
        room.setX(1000 * Math.abs(Math.random()));
        room.setY(1000 * Math.abs(Math.random()));
        unconnectedRooms.add(room);
        renderView();
    }

    @FXML
    void insertPathOnAction(ActionEvent event) {
        this.setCurrentEditMode(EditMode.INSERT_PATH);
    }

    @FXML
    void resetButtonOnAction(ActionEvent event) {
        initGame();
    }

    @FXML
    void moveButtonOnAction(ActionEvent event) {
        this.setCurrentEditMode(EditMode.MOVE);
    }

    @FXML
    void autoLayoutButtonOnAction(ActionEvent event) {
        renderView();
    }

    @FXML
    void scrollPaneOnZoom(ZoomEvent event) {
        System.out.println(event.getZoomFactor());
        drawing.setScaleX(drawing.getScaleX() * event.getZoomFactor());
        drawing.setScaleY(drawing.getScaleY() * event.getZoomFactor());
        // TODO: Update the actual size in the scrollpane (so that scrollbars appear when zooming in
        // TODO: Add Keyboard and touchpad zoom
        // TODO: do the zoom with th eright zoom center
    }

    @FXML
    void drawingOnMouseReleased(MouseEvent event) {
        if (!event.isControlDown() & !unselectingDisabled) {
            log.getLogger().finest("Unselected all rooms through clicking the scroll pane");
            unselectEverything();
        }

        unselectingDisabled = false;
    }

    public void unselectEverything() {
        for (Node child : drawing.getChildren()) {
            if (child instanceof RoomRectangle) {
                ((RoomRectangle) child).setSelected(false);
            }
        }
    }

    /**
     * Removes the specified room from the {@code unconnectedRooms}-list.
     *
     * @param room The room to remove
     * @return {@code true} if this list contained the specified element
     */
    public boolean setRoomAsConnected(RoomRectangle room) {
        return unconnectedRooms.remove(room);
    }

    public void renderView() {
        this.renderView(true);
    }

    public void renderView(boolean autoLayout) {
        while (drawing.getChildren().size() > 0) {
            drawing.getChildren().remove(0);
        }

        Thread renderThread = new Thread(() -> {
            LinkedList<RoomRectangle> renderQueue = new LinkedList<>();
            RoomList  allRoomsAsListCopy;

            // The distance between connected rooms
            double roomDistance = 150;

            RoomRectangle startRoom;
            if (allRoomsAsList==null){
                // First time to render
                startRoom = new RoomRectangle(drawing, this.currentGame.getCurrentRoom());
                allRoomsAsListCopy = new RoomList();
            }else {
                startRoom = allRoomsAsList.findByRoom(this.currentGame.getCurrentRoom());
                allRoomsAsListCopy = allRoomsAsList;
            }
            allRoomsAsList = new RoomList();
            renderQueue.add(startRoom);
            allRoomsAsList.add(startRoom);
            startRoom.updateNameLabelPosition();

            // render unconnected rooms
            for (RoomRectangle room : unconnectedRooms) {
                // room.getRoom().setRendered(false);
                renderQueue.add(room);
            }

            while (!renderQueue.isEmpty()) {
                RoomRectangle currentRoom = renderQueue.remove();

                if (!currentRoom.isRendered()) {
                    allRoomsAsList.add(currentRoom);
                    // currentRoom.getRoom().setRendered(true);
                    currentRoom.updateNameLabelPosition();
                    currentRoom.setCustomParent(drawing);
                }
                for (Map.Entry<WalkDirection, Room> entry : currentRoom.getRoom().getAdjacentRooms().entrySet()) {
                    RoomRectangle newRoom;
                    newRoom = allRoomsAsListCopy.findByRoom(entry.getValue());

                    if (newRoom == null) {
                        // not rendered yet
                        newRoom = new RoomRectangle(drawing, entry.getValue());

                        // Set room position
                        if (autoLayout) {
                            switch (entry.getKey()) {
                                case NORTH:
                                    newRoom.setY(currentRoom.getY() - roomDistance);
                                    newRoom.setX(currentRoom.getX());
                                    break;
                                case WEST:
                                    newRoom.setY(currentRoom.getY());
                                    newRoom.setX(currentRoom.getX() - roomDistance);
                                    break;
                                case EAST:
                                    newRoom.setY(currentRoom.getY());
                                    newRoom.setX(currentRoom.getX() + roomDistance);
                                    break;
                                case SOUTH:
                                    newRoom.setY(currentRoom.getY() + roomDistance);
                                    newRoom.setX(currentRoom.getX());
                                    break;
                                case NORTH_WEST:
                                    newRoom.setY(currentRoom.getY() - roomDistance);
                                    newRoom.setX(currentRoom.getX() - roomDistance);
                                    break;
                                case NORTH_EAST:
                                    newRoom.setY(currentRoom.getY() - roomDistance);
                                    newRoom.setX(currentRoom.getX() + roomDistance);
                                    break;
                                case SOUTH_WEST:
                                    newRoom.setY(currentRoom.getY() + roomDistance);
                                    newRoom.setX(currentRoom.getX() - roomDistance);
                                    break;
                                case SOUTH_EAST:
                                    newRoom.setY(currentRoom.getY() + roomDistance);
                                    newRoom.setX(currentRoom.getX() + roomDistance);
                                    break;
                            }
                        }
                    }
                    Line connectionLine = new Line(0, 0, 0, 0);

                    switch (entry.getKey()) {
                        case NORTH:
                            connectionLine = new Line(currentRoom.getX() + currentRoom.getWidth() / 2.0, currentRoom.getY(), newRoom.getX() + newRoom.getWidth() / 2.0, newRoom.getY() + newRoom.getHeight());
                            break;
                        case WEST:
                            connectionLine = new Line(currentRoom.getX(), currentRoom.getY() + currentRoom.getHeight() / 2, newRoom.getX() + newRoom.getWidth(), newRoom.getY() + newRoom.getHeight() / 2);
                            break;
                        case EAST:
                            connectionLine = new Line(currentRoom.getX() + currentRoom.getWidth(), currentRoom.getY() + currentRoom.getHeight() / 2, newRoom.getX(), newRoom.getY() + newRoom.getHeight() / 2);
                            break;
                        case SOUTH:
                            connectionLine = new Line(currentRoom.getX() + currentRoom.getWidth() / 2.0, currentRoom.getY() + currentRoom.getHeight(), newRoom.getX() + newRoom.getWidth() / 2.0, newRoom.getY());
                            break;
                        case NORTH_WEST:
                            connectionLine = new Line(currentRoom.getX(), currentRoom.getY(), newRoom.getX() + newRoom.getWidth(), newRoom.getY() + newRoom.getHeight());
                            break;
                        case NORTH_EAST:
                            connectionLine = new Line(currentRoom.getX() + currentRoom.getWidth(), currentRoom.getY(), newRoom.getX(), newRoom.getY() + newRoom.getHeight());
                            break;
                        case SOUTH_WEST:
                            connectionLine = new Line(currentRoom.getX(), currentRoom.getY() + currentRoom.getHeight(), newRoom.getX() + newRoom.getWidth(), newRoom.getY());
                            break;
                        case SOUTH_EAST:
                            connectionLine = new Line(currentRoom.getX() + currentRoom.getWidth(), currentRoom.getY() + currentRoom.getHeight(), newRoom.getX(), newRoom.getY());
                            break;
                    }

                    final Line connectionLineCopy = connectionLine;
                    Platform.runLater(() -> drawing.getChildren().add(connectionLineCopy));

                    if (!newRoom.isRendered()) {
                        // render the child
                        allRoomsAsList.add(newRoom);
                        // newRoom.getRoom().setRendered(true);
                        //Platform.runLater(() -> drawing.getChildren().add(newRoom));
                        renderQueue.add(newRoom);
                    }
                }
            }
        });
        renderThread.setName("renderThread");
        renderThread.start();
    }

    @FXML
        // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        assert insertRoom != null : "fx:id=\"insertRoom\" was not injected: check your FXML file 'EditorMain.fxml'.";
        assert drawing != null : "fx:id=\"drawing\" was not injected: check your FXML file 'EditorMain.fxml'.";
        assert insertPath != null : "fx:id=\"insertPath\" was not injected: check your FXML file 'EditorMain.fxml'.";

        currentEditorInstance = this;

        initGame();

        scrollPane.hvalueProperty().addListener((observable, oldValue, newValue) -> unselectingDisabled = true);

        scrollPane.vvalueProperty().addListener((observable, oldValue, newValue) -> unselectingDisabled = true);
    }

    /**
     * Initializes a new game
     */
    public void initGame(){
        currentGame = new Game();
        currentGame.getCurrentRoom().setName("startRoom");
        unconnectedRooms = new RoomList();
        allRoomsAsList = null;
        renderView();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        bundle = ResourceBundle.getBundle("view.strings");

        // appConfig = new Config();

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

    public EditMode getCurrentEditMode() {
        return currentEditMode;
    }

    public void setCurrentEditMode(EditMode currentEditMode) {
        this.currentEditMode = currentEditMode;
        this.insertPath.setSelected(currentEditMode == EditMode.INSERT_PATH);
        this.moveButton.setSelected(currentEditMode == EditMode.MOVE);
    }
}
