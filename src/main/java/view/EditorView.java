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
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
    private RoomRectangleList unconnectedRooms = new RoomRectangleList();
    private RoomRectangleList allRoomsAsList;
    private EditMode currentEditMode;
    private boolean isMouseOverDrawing = false;
    /**
     * Used to display a temporary room when in EditMode.INSERT_ROOM
     */
    private RoomRectangle tempRoomForRoomInsertion;

    /**
     * A thread safe room counter
     */
    private int currentRoomCount = 0;

    private double currentMouseX = 0;
    private double currentMouseY = 0;

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

    @SuppressWarnings("unused")
    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @SuppressWarnings("unused")
    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML // fx:id="insertRoom"
    private ToggleButton insertRoom; // Value injected by FXMLLoader

    @FXML // fx:id="drawing"
    private CustomGroup drawing; // Value injected by FXMLLoader

    @FXML // fx:id="insertPath"
    private ToggleButton insertPath; // Value injected by FXMLLoader

    @FXML
    private ToggleButton moveButton;

    @FXML
    private Button autoLayoutButton;

    @FXML
    private Button refreshViewButton;

    @FXML
    private MenuItem newMenuItem;

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
        this.setCurrentEditMode(EditMode.INSERT_ROOM);
    }

    @FXML
    void insertPathOnAction(ActionEvent event) {
        this.setCurrentEditMode(EditMode.INSERT_PATH);
    }

    @FXML
    void newMenuItemOnAction(ActionEvent event) {
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
    void refreshViewButtonOnAction(ActionEvent event) {
        renderView(false);
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
    void scrollPaneOnMouseReleased(MouseEvent event) {
        if (!event.isControlDown() & !unselectingDisabled) {
            log.getLogger().finest("Unselected all rooms through clicking the scroll pane");
            unselectEverything();
        }

        unselectingDisabled = false;
    }

    @FXML
    void scrollPaneOnMouseClicked(MouseEvent event){
        /*
        event.getTarget() instanceof RoomRectangle is necessary because the event is required twice:
            - once with event.getTarget() instanceof RoomRectangle and
            - once with event.getTarget() instanceof Label (the name label of the room)
        ... and we need to suppress one of the two because we don't want to insert two rooms
         */
        if (currentEditMode == EditMode.INSERT_ROOM && event.getTarget() instanceof RoomRectangle) {
            // add tempRoomForRoomInsertion to the game
            log.getLogger().fine("Added room to game: " + tempRoomForRoomInsertion.getRoom().getName());
            allRoomsAsList.add(tempRoomForRoomInsertion);
            this.renderView(false, false, true);
            initInsertRoomEditMode();
        }
    }

    @FXML
    void scrollPaneOnMouseEntered(MouseEvent event) {
        isMouseOverDrawing = true;
        if (this.getCurrentEditMode() == EditMode.INSERT_ROOM) {
            initInsertRoomEditMode();
        }
    }

    @FXML
    void scrollPaneOnMouseExited(MouseEvent event) {
        isMouseOverDrawing = false;
        if (this.getCurrentEditMode() == EditMode.INSERT_ROOM) {
            terminateInsertRoomEditMode();
        }
    }

    @FXML
    void scrollPaneOnMouseMoved(MouseEvent event) {
        currentMouseX = event.getX();
        currentMouseY = event.getY();
        if (currentEditMode == EditMode.INSERT_ROOM) {
            insertRoomUpdateTempRoomPosition();
        }
    }

    /**
     * Performs initializing actions for the EditMode.INSERT_ROOM
     */
    private void initInsertRoomEditMode() {
        tempRoomForRoomInsertion = new RoomRectangle(null);

        int roomIndex;

        if (allRoomsAsList == null) {
            roomIndex = 0;
        } else {
            roomIndex = currentRoomCount;
        }

        tempRoomForRoomInsertion.getRoom().setName("Room " + roomIndex);
        tempRoomForRoomInsertion.setCustomParent(drawing);
        insertRoomUpdateTempRoomPosition();
    }

    /**
     * Performs terminating actions for the EditMode.INSERT_ROOM
     */
    private void terminateInsertRoomEditMode() {
        if (tempRoomForRoomInsertion != null) {
            tempRoomForRoomInsertion.setCustomParent(null);
            tempRoomForRoomInsertion = null;
        }
    }

    /**
     * Updates the position of tempRoomForRoomInsertion when the current edit mode is EditMode.INSERT_ROOM
     */
    private void insertRoomUpdateTempRoomPosition() {
        if (tempRoomForRoomInsertion != null) {
            tempRoomForRoomInsertion.setX(currentMouseX - tempRoomForRoomInsertion.getWidth() / 2.0);
            tempRoomForRoomInsertion.setY(currentMouseY - tempRoomForRoomInsertion.getHeight() / 2.0);
        }
    }

    public void unselectEverything() {
        for (Node child : drawing.getChildren()) {
            if (child instanceof RoomRectangle) {
                ((RoomRectangle) child).setSelected(false);
            }
        }
    }

    /**
     * Updates the internal connection status of a room (if it is connected to the current room of the game)
     *
     * @param room The room to update
     */
    private void updateConnectionStatusOfRoom(RoomRectangle room) {
        boolean isConnected = currentGame.getCurrentRoom().isConnectedTo(room.getRoom());
        if (isConnected && unconnectedRooms.contains(room)) {
            // room was marked as unconnected and now is connected
            unconnectedRooms.remove(room);
        } else if (!isConnected && !unconnectedRooms.contains(room)) {
            // room was marked as connected and now is unconnected
            unconnectedRooms.add(room);
        }

        // in any other case, the status did not change and we don't need to do anything
    }

    /**
     * Renders the current game and unconnected rooms in the view.
     */
    public void renderView() {
        this.renderView(true);
    }

    /**
     * Renders the current game and unconnected rooms in the view.
     *
     * @param autoLayout If {@code true}, the rooms will be automatically laid out according to their topology.
     */
    public void renderView(boolean autoLayout) {
        this.renderView(autoLayout, false);
    }

    /**
     * Renders the current game and unconnected rooms in the view.
     *
     * @param autoLayout      If {@code true}, the rooms will be automatically laid out according to their topology.
     * @param onlyUpdateLines If {@code true}, onyl connecting lines between the rooms are rendered, rooms are left as they are. Useful if the user is currently moving the room around with the mouse.
     */
    public void renderView(boolean autoLayout, boolean onlyUpdateLines) {
        renderView(autoLayout, onlyUpdateLines, false);
    }

    /**
     * Renders the current game and unconnected rooms in the view.
     *
     * @param autoLayout           If {@code true}, the rooms will be automatically laid out according to their topology.
     * @param onlyUpdateLines      If {@code true}, onyl connecting lines between the rooms are rendered, rooms are left as they are. Useful if the user is currently moving the room around with the mouse.
     * @param synchronousRendering If {@code true}, the rendering will happen synchronously, otherwise asynchronously.
     */
    public void renderView(boolean autoLayout, boolean onlyUpdateLines, @SuppressWarnings("SameParameterValue") boolean synchronousRendering) {
        // Only render if it's not already rendering
        int indexCorrection = 0;
        while (drawing.getChildren().size() > indexCorrection) {
            if (!onlyUpdateLines || drawing.getChildren().get(indexCorrection) instanceof Line) {
                drawing.getChildren().remove(indexCorrection);
            } else {
                indexCorrection++;
            }
        }

        Thread renderThread = new Thread(() -> {
            // update the connection status of all rooms
            if (allRoomsAsList != null) {
                for (RoomRectangle room : allRoomsAsList) {
                    updateConnectionStatusOfRoom(room);
                }
            }

            LinkedList<RoomRectangle> renderQueue = new LinkedList<>();
            RoomRectangleList allRoomsAsListCopy;

            // The distance between connected rooms
            double roomDistance = 150;

            RoomRectangle startRoom;
            if (allRoomsAsList == null) {
                // First time to render
                startRoom = new RoomRectangle(drawing, this.currentGame.getCurrentRoom());
                allRoomsAsListCopy = new RoomRectangleList();
                allRoomsAsList = new RoomRectangleList();
                allRoomsAsList.add(startRoom);
            } else {
                startRoom = allRoomsAsList.findByRoom(this.currentGame.getCurrentRoom());
                allRoomsAsListCopy = allRoomsAsList;
                if (!onlyUpdateLines) {
                    allRoomsAsList = new RoomRectangleList();
                }
            }

            renderQueue.add(startRoom);
                /*
                assert startRoom != null;
                startRoom.updateNameLabelPosition();*/

            // render unconnected rooms
            for (RoomRectangle room : unconnectedRooms) {
                renderQueue.add(room);
            }

            while (!renderQueue.isEmpty()) {
                RoomRectangle currentRoom = renderQueue.remove();

                if (!currentRoom.isRendered()) {
                    allRoomsAsList.add(currentRoom);
                    currentRoom.updateNameLabelPosition();
                    currentRoom.setCustomParent(drawing);
                }
                for (Map.Entry<WalkDirection, Room> entry : currentRoom.getRoom().getAdjacentRooms().entrySet()) {
                    RoomRectangle newRoom;
                    newRoom = allRoomsAsListCopy.findByRoom(entry.getValue());

                    if (newRoom == null) {
                        // not rendered yet
                        newRoom = new RoomRectangle(drawing, entry.getValue());
                    }

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
                        renderQueue.add(newRoom);
                    }
                }
            }

            // set the room count
            currentRoomCount = allRoomsAsList.size();
        });

        renderThread.setName("renderThread");
        renderThread.start();

        // Wait for thread to finish if specified
        if (synchronousRendering) {
            try {
                renderThread.join();
            } catch (InterruptedException e) {
                log.getLogger().log(Level.SEVERE, "An error occurred", e);
            }
        }
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

        // add button icons
        insertRoom.setGraphic(new ImageView(new Image(EditorView.class.getResourceAsStream("add-room.png"))));
        moveButton.setGraphic(new ImageView(new Image(EditorView.class.getResourceAsStream("move-arrows.png"))));
        insertPath.setGraphic(new ImageView(new Image(EditorView.class.getResourceAsStream("connecting-points.png"))));
        autoLayoutButton.setGraphic(new ImageView(new Image(EditorView.class.getResourceAsStream("autoLayout.png"))));
        refreshViewButton.setGraphic(new ImageView(new Image(EditorView.class.getResourceAsStream("refreshView.png"))));

        // add tooltips
        insertRoom.setTooltip(new Tooltip("Insert a new room"));
        moveButton.setTooltip(new Tooltip("Move rooms"));
        insertPath.setTooltip(new Tooltip("Connect rooms to create walk paths"));
        autoLayoutButton.setTooltip(new Tooltip("Automatically rearrange the rooms in the view below"));
        autoLayoutButton.setTooltip(new Tooltip("Refresh the current view"));
    }

    /**
     * Initializes a new game
     */
    public void initGame() {
        currentGame = new Game();
        currentGame.getCurrentRoom().setName("startRoom");
        unconnectedRooms = new RoomRectangleList();
        allRoomsAsList = null;
        renderView();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        bundle = ResourceBundle.getBundle("view.strings");

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
        log.getLogger().finer("Setting currentEditMode to " + currentEditMode.toString());

        // Initialize or terminate the insert room mode
        if (isMouseOverDrawing) {
            if (currentEditMode == EditMode.INSERT_ROOM && this.currentEditMode != EditMode.INSERT_ROOM) {
                initInsertRoomEditMode();
            } else if (currentEditMode != EditMode.INSERT_ROOM && this.currentEditMode == EditMode.INSERT_ROOM) {
                terminateInsertRoomEditMode();
            }
        }

        this.currentEditMode = currentEditMode;
        this.insertPath.setSelected(currentEditMode == EditMode.INSERT_PATH);
        this.moveButton.setSelected(currentEditMode == EditMode.MOVE);
        this.insertRoom.setSelected(currentEditMode == EditMode.INSERT_ROOM);

    }

    public RoomRectangleList getAllRoomsAsList() {
        return allRoomsAsList;
    }
}
