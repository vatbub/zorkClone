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
import com.github.vatbub.common.view.core.CustomGroup;
import com.github.vatbub.common.view.core.ExceptionAlert;
import com.github.vatbub.common.view.reporting.ReportingDialog;
import common.AppConfig;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ZoomEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Line;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.Game;
import model.Room;
import model.WalkDirection;
import model.WalkDirectionUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

public class EditorView extends Application {

    public static EditorView currentEditorInstance;
    public static ResourceBundle bundle;
    private static Stage stage;
    public final ConnectionLineList lineList = new ConnectionLineList();
    private final ObjectProperty<Game> currentGame = new SimpleObjectProperty<>();
    private boolean unselectingDisabled;
    // Unconnected Rooms will not be saved but need to be hold in the RAM while editing
    private RoomRectangleList unconnectedRooms = new RoomRectangleList();
    private RoomRectangleList allRoomsAsList;
    private RoomRectangleList allRoomsAsListCopy;
    private EditMode currentEditMode = EditMode.MOVE;
    private EditMode previousEditMode;
    private boolean isMouseOverDrawing = false;
    private boolean compassIconFaded = false;
    private boolean insertRoomDragDetected;
    private ExecutorService renderThreadPool = Executors.newFixedThreadPool(1);

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
    private final ConnectionLine.InvalidationRunnable lineInvalidationRunnable = (lineToDispose) -> {
        FOKLogger.info(EditorView.class.getName(), "Invalidated line that connected " + lineToDispose.getStartRoom().getRoom().getName() + " and " + lineToDispose.getEndRoom().getRoom().getName());
        lineList.remove(lineToDispose);
        if (lineToDispose.getStartRoom().getRoom().isDirectlyConnectedTo(lineToDispose.getEndRoom().getRoom())) {
            // Connection between rooms must be deleted
            lineToDispose.getStartRoom().getRoom().getAdjacentRooms().remove(WalkDirectionUtils.getFromLine(lineToDispose));
            lineToDispose.getEndRoom().getRoom().getAdjacentRooms().remove(WalkDirectionUtils.invert(WalkDirectionUtils.getFromLine(lineToDispose)));
        }
        Platform.runLater(() -> drawing.getChildren().remove(lineToDispose));
    };
    @FXML // fx:id="insertPath"
    private ToggleButton insertPath; // Value injected by FXMLLoader
    @FXML
    private ToggleButton moveButton;
    @FXML
    private Button autoLayoutButton;
    @FXML
    private Button refreshViewButton;
    @SuppressWarnings("unused")
    @FXML
    private MenuItem newMenuItem;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private ImageView compassImage;
    @SuppressWarnings("unused")
    @FXML
    private MenuItem menuItemClose;
    @SuppressWarnings("unused")
    @FXML
    private MenuItem menuItemOpen;
    @FXML
    private MenuItem menuItemSave;
    @SuppressWarnings("unused")
    @FXML
    private MenuItem menuItemSaveAs;
    @FXML
    private AnchorPane scrollPaneContainer;
    @SuppressWarnings("unused")
    private EventHandler<MouseEvent> forwardEventsToSelectableNodesHandler = (event -> {
        if (getCurrentEditMode() == EditMode.MOVE) {
            for (Node child : new ArrayList<>(drawing.getChildren())) {
                if (child instanceof Selectable) {
                    if (((Selectable) child).isSelected() && event.getTarget() != child) {
                        FOKLogger.fine(EditorView.class.getName(), "Child is:  " + child.toString() + "\ntarget is: " + event.getTarget().toString());
                        child.fireEvent(event);
                        event.consume();
                    }
                }
            }
        }
    });

    public static void main(String[] args) {
        Common.getInstance().setAppName("zorkGameEditor");
        Common.getInstance().setAwsAccessKey(AppConfig.awsLogAccessKeyID);
        Common.getInstance().setAwsSecretAccessKey(AppConfig.awsLogSecretAccessKeyID);
        FOKLogger.enableLoggingOfUncaughtExceptions();
        for (String arg : args) {
            if (arg.toLowerCase().matches("mockappversion=.*")) {
                // Set the mock version
                String version = arg.substring(arg.toLowerCase().indexOf('=') + 1);
                Common.getInstance().setMockAppVersion(version);
            } else if (arg.toLowerCase().matches("mockbuildnumber=.*")) {
                // Set the mock build number
                String buildnumber = arg.substring(arg.toLowerCase().indexOf('=') + 1);
                Common.getInstance().setMockBuildNumber(buildnumber);
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
    void fileBugMenuItemOnAction(@SuppressWarnings("unused") ActionEvent event) {
        FOKLogger.info(EditorView.class.getName(), "Manual call of the ReportingDialog");
        new ReportingDialog(stage.getScene()).show(AppConfig.gitHubUserName, AppConfig.gitHubRepoName);
    }

    @FXML
    void menuItemSaveOnAction(ActionEvent event) {
        if (getCurrentGame().getFileSource() == null) {
            // show the save as dialog
            menuItemSaveAsOnAction(event);
        } else {
            // simply save
            try {
                getCurrentGame().save(getCurrentGame().getFileSource());
            } catch (IOException e) {
                FOKLogger.log(MainWindow.class.getName(), Level.SEVERE, "Could not save the game from the \"Save\" menu", e);
                new Alert(Alert.AlertType.ERROR, "Could not save the game file: \n\n" + ExceptionUtils.getRootCauseMessage(e)).show();
            }
        }
    }

    @FXML
    void menuItemSaveAsOnAction(@SuppressWarnings("unused") ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save game file");
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            // if file == null the action was aborted
            try {
                getCurrentGame().save(file);
            } catch (IOException e) {
                FOKLogger.log(MainWindow.class.getName(), Level.SEVERE, "Could not save the game from the \"Save As\" menu", e);
                new Alert(Alert.AlertType.ERROR, "Could not save the game file: \n\n" + ExceptionUtils.getRootCauseMessage(e)).show();
            }
        }
    }

    @FXML
    void menuItemOpenOnAction(@SuppressWarnings("unused") ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open a game file");
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            // if file == null the action was aborted
            try {
                loadGame(file);
            } catch (IOException | ClassNotFoundException e) {
                FOKLogger.log(MainWindow.class.getName(), Level.SEVERE, "Failed to open game " + file.toString(), e);
                new Alert(Alert.AlertType.ERROR, "Could not open the game file: \n\n" + ExceptionUtils.getRootCauseMessage(e)).show();
            }
        }
    }

    @FXML
    void menuItemCloseOnAction(@SuppressWarnings("unused") ActionEvent event) {
        Platform.exit();
    }

    @FXML
    void insertRoomOnAction(@SuppressWarnings("unused") ActionEvent event) {
        this.setCurrentEditMode(EditMode.INSERT_ROOM);
    }

    @FXML
    void insertRoomOnDragDetected(@SuppressWarnings("unused") MouseEvent event) {
        insertRoomDragDetected = true;
        setCurrentEditMode(EditMode.INSERT_ROOM);
    }

    @FXML
    void insertRoomOnMouseDragged(MouseEvent event) {
        Bounds scrollPaneBounds = scrollPaneContainer.localToScene(scrollPane.getBoundsInLocal());
        if (event.getSceneX() >= scrollPaneBounds.getMinX() && event.getSceneX() <= scrollPaneBounds.getMaxX() && event.getSceneY() >= scrollPaneBounds.getMinY() && event.getSceneY() <= scrollPaneBounds.getMaxY()) {
            // we're in the scroll pane
            if (!isMouseOverDrawing) {
                scrollPaneOnMouseEntered(event);
            }
        } else {
            if (isMouseOverDrawing) {
                scrollPaneOnMouseExited(event);
            }
        }
        scrollPaneOnMouseMoved(event);
    }

    @FXML
    void insertRoomOnMousePressed(@SuppressWarnings("unused") MouseEvent event) {
        insertRoom.setCursor(Cursor.CLOSED_HAND);
    }

    @FXML
    void insertRoomOnMouseReleased(MouseEvent event) {
        insertRoom.setCursor(Cursor.OPEN_HAND);
        if (insertRoomDragDetected) {
            insertRoomDragDetected = false;
            scrollPaneOnMouseClicked(event);
        }
    }

    @FXML
    void insertPathOnAction(@SuppressWarnings("unused") ActionEvent event) {
        this.setCurrentEditMode(EditMode.INSERT_PATH);
    }

    @FXML
    void newMenuItemOnAction(@SuppressWarnings("unused") ActionEvent event) {
        initGame();
    }

    @FXML
    void moveButtonOnAction(@SuppressWarnings("unused") ActionEvent event) {
        this.setCurrentEditMode(EditMode.MOVE);
    }

    @FXML
    void autoLayoutButtonOnAction(@SuppressWarnings("unused") ActionEvent event) {
        renderView();
    }

    @FXML
    void refreshViewButtonOnAction(@SuppressWarnings("unused") ActionEvent event) {
        renderView(false);
    }

    @FXML
    void scrollPaneOnZoom(ZoomEvent event) {
        FOKLogger.fine(MainWindow.class.getName(), "Zooming in view, new Zoom level: " + event.getZoomFactor());
        drawing.setScaleX(drawing.getScaleX() * event.getZoomFactor());
        drawing.setScaleY(drawing.getScaleY() * event.getZoomFactor());
        // TODO: Update the actual size in the scrollpane (so that scrollbars appear when zooming in
        // TODO: Add Keyboard and touchpad zoom
        // TODO: do the zoom with the right zoom center
    }

    @FXML
    void scrollPaneOnMouseReleased(MouseEvent event) {
        if (!event.isControlDown() & !unselectingDisabled) {
            FOKLogger.finest(MainWindow.class.getName(), "Unselected all rooms through clicking the scroll pane");
            unselectEverything();
        }

        unselectingDisabled = false;
    }

    @FXML
    void scrollPaneOnMouseClicked(MouseEvent event) {
        /*
        event.getTarget() instanceof RoomRectangle is necessary because the event is required twice:
            - once with event.getTarget() instanceof RoomRectangle and
            - once with event.getTarget() instanceof Label (the name label of the room)
        ... and we need to suppress one of the two because we don't want to insert two rooms
         */
        /*
        When the user uses a touch screen and adds a new room by clicking the insertRoom-button and then clicking the
        scrollPane, the event target is an instance of ScrollPaneSkin$4 which is an anonymous inner class in ScrollPane.
        Since we cannot directly check the class type using instanceof against that inner class, we need to use
        event.getTarget().getClass().getName() and do a String comparison for this particular use case.
        Keep in mind that this is an internal api of java and the class name of that class might change at ANY TIME so
        things might break just by upgrading the java version! (But we have no other choice unfortunately :( )
        See https://github.com/vatbub/zorkClone/issues/7 and http://stackoverflow.com/questions/41454202/javafx-instanceof-scrollpaneskin-fails
        for more info
         */
        FOKLogger.finest(EditorView.class.getName(), "scrollPaneOnMouseClicked occurred. event target class is " + event.getTarget().getClass().getName());
        if (currentEditMode == EditMode.INSERT_ROOM && (event.getTarget() instanceof RoomRectangle || event.getTarget() instanceof ToggleButton || event.getTarget().getClass().getName().equals("com.sun.javafx.scene.control.skin.ScrollPaneSkin$4")) && event.getClickCount() == 1 && tempRoomForRoomInsertion != null) {
            // add tempRoomForRoomInsertion to the game
            FOKLogger.fine(MainWindow.class.getName(), "Added room to game: " + tempRoomForRoomInsertion.getRoom().getName());
            tempRoomForRoomInsertion.setTemporary(false);
            tempRoomForRoomInsertion.setSelected(false);
            allRoomsAsList.add(tempRoomForRoomInsertion);
            // this.renderView(false, false, true);
            this.renderView(false, false);

            this.setCurrentEditMode(this.getPreviousEditMode());
        }
    }

    @FXML
    void scrollPaneOnMouseEntered(MouseEvent event) {
        isMouseOverDrawing = true;
        currentMouseX = event.getScreenX();
        currentMouseY = event.getScreenY();
        if (this.getCurrentEditMode() == EditMode.INSERT_ROOM) {
            initInsertRoomEditMode();
        }
    }

    @FXML
    void scrollPaneOnMouseExited(@SuppressWarnings("unused") MouseEvent event) {
        isMouseOverDrawing = false;
        if (this.getCurrentEditMode() == EditMode.INSERT_ROOM) {
            terminateInsertRoomEditMode();
        }
    }

    @FXML
    void scrollPaneOnMouseMoved(MouseEvent event) {
        currentMouseX = event.getScreenX();
        currentMouseY = event.getScreenY();
        if (currentEditMode == EditMode.INSERT_ROOM) {
            insertRoomUpdateTempRoomPosition();
        }

        // Fade compassIcon
        if (event.getX() >= compassImage.getLayoutX() && event.getX() <= (compassImage.getLayoutX() + compassImage.getFitWidth()) && event.getY() >= compassImage.getLayoutY() && event.getY() <= (compassImage.getLayoutY() + compassImage.getFitHeight()) && !compassIconFaded) {
            // fade out
            compassIconFaded = true;
            compassImageFadeOut();
        }
        if ((event.getX() < compassImage.getLayoutX() || event.getX() > (compassImage.getLayoutX() + compassImage.getFitWidth()) || event.getY() < compassImage.getLayoutY() || event.getY() > (compassImage.getLayoutY() + compassImage.getFitHeight())) && compassIconFaded) {
            // fade in
            compassIconFaded = false;
            compassImageFadeIn();
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
        tempRoomForRoomInsertion.setTemporary(true);
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
            // convert coordinates
            Point2D point = drawing.screenToLocal(currentMouseX - tempRoomForRoomInsertion.getWidth() / 2.0, currentMouseY - tempRoomForRoomInsertion.getHeight() / 2.0);
            tempRoomForRoomInsertion.setX(point.getX());
            tempRoomForRoomInsertion.setY(point.getY());
        }
    }

    private void compassImageFadeOut() {
        FadeTransition ft = new FadeTransition(Duration.millis(250), compassImage);
        ft.setFromValue(compassImage.getOpacity());
        ft.setToValue(0.2);
        ft.setAutoReverse(false);
        ft.play();
    }

    private void compassImageFadeIn() {
        FadeTransition ft = new FadeTransition(Duration.millis(250), compassImage);
        ft.setFromValue(compassImage.getOpacity());
        ft.setAutoReverse(false);
        ft.setToValue(0.5);
        ft.play();
    }

    public void unselectEverything() {
        for (Node child : drawing.getChildren()) {
            if (child instanceof RoomRectangle) {
                ((RoomRectangle) child).setSelected(false);
            } else if (child instanceof ConnectionLine) {
                ((ConnectionLine) child).setSelected(false);
            }
        }
    }

    /**
     * Updates the internal connection status of a room (if it is connected to the current room of the game)
     *
     * @param room The room to update
     */
    private void updateConnectionStatusOfRoom(RoomRectangle room) {
        boolean isConnected = getCurrentGame().getCurrentRoom().isConnectedTo(room.getRoom());
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
     * @param onlyUpdateLines If {@code true}, only connecting lines between the rooms are rendered, rooms are left as they are. Useful if the user is currently moving the room around with the mouse.
     */
    public void renderView(boolean autoLayout, boolean onlyUpdateLines) {
        int indexCorrection = 0;
        while (drawing.getChildren().size() > indexCorrection) {
            if (!onlyUpdateLines && !(drawing.getChildren().get(indexCorrection) instanceof ConnectionLine)) {
                drawing.getChildren().remove(indexCorrection);
            } else if (drawing.getChildren().get(indexCorrection) instanceof ConnectionLine) {
                // Check if line is still valid
                ((ConnectionLine) drawing.getChildren().get(indexCorrection)).updateLocation();
                indexCorrection++;
            } else {
                indexCorrection++;
            }
        }

        renderThreadPool.submit(() -> {
            // update the connection status of all rooms
            if (allRoomsAsList != null) {
                for (RoomRectangle room : allRoomsAsList) {
                    updateConnectionStatusOfRoom(room);
                }
            }

            LinkedList<RoomRectangle> renderQueue = new LinkedList<>();

            // The distance between connected rooms
            double roomDistance = 50;

            RoomRectangle startRoom;
            if (allRoomsAsList == null) {
                // First time to render
                startRoom = new RoomRectangle(drawing, this.getCurrentGame().getCurrentRoom());
                allRoomsAsListCopy = new RoomRectangleList();
                allRoomsAsList = new RoomRectangleList();
                allRoomsAsList.add(startRoom);
            } else {
                startRoom = allRoomsAsList.findByRoom(this.getCurrentGame().getCurrentRoom());
                allRoomsAsListCopy = allRoomsAsList;
                if (!onlyUpdateLines) {
                    allRoomsAsList = new RoomRectangleList();
                }
            }

            renderQueue.add(startRoom);

            // render unconnected rooms
            renderQueue.addAll(unconnectedRooms);

            while (!renderQueue.isEmpty()) {
                RoomRectangle currentRoom = renderQueue.remove();
                if (currentRoom == null) {
                    FOKLogger.severe(EditorView.class.getName(), "currentRoom == null means that the room was never added to allRoomsAsList and that means that we ran into a bug, so report it :(");
                    Platform.runLater(() -> new ReportingDialog(stage.getScene()).show(AppConfig.gitHubUserName, AppConfig.gitHubRepoName, new IllegalStateException("A room of the game was never added to allRoomsAsList. This is an internal bug and needs to be reported to the dev team. Please tell us at https://github.com/vatbub/zorkClone/issues what you did when this exception occurred.")));
                }

                //noinspection ConstantConditions
                if (!currentRoom.isRendered()) {
                    if (!allRoomsAsList.contains(currentRoom)) {
                        allRoomsAsList.add(currentRoom);
                    }
                    currentRoom.setCustomParent(drawing);
                    currentRoom.updateNameLabelPosition();
                }
                for (Map.Entry<WalkDirection, Room> entry : currentRoom.getRoom().getAdjacentRooms().entrySet()) {
                    RoomRectangle newRoom;
                    newRoom = allRoomsAsListCopy.findByRoom(entry.getValue());

                    if (newRoom == null) {
                        // not rendered yet
                        newRoom = new RoomRectangle(drawing, entry.getValue());
                        allRoomsAsList.add(newRoom);
                    }

                    // Set room position
                    if (autoLayout && !newRoom.isRendered()) {
                        switch (entry.getKey()) {
                            case NORTH:
                                newRoom.setY(currentRoom.getY() - newRoom.getHeight() - roomDistance);
                                newRoom.setX(currentRoom.getX() + currentRoom.getWidth() / 2 - newRoom.getWidth() / 2);
                                break;
                            case WEST:
                                newRoom.setY(currentRoom.getY());
                                newRoom.setX(currentRoom.getX() - newRoom.getWidth() - roomDistance);
                                break;
                            case EAST:
                                newRoom.setY(currentRoom.getY());
                                newRoom.setX(currentRoom.getX() + currentRoom.getWidth() + roomDistance);
                                break;
                            case SOUTH:
                                newRoom.setY(currentRoom.getY() + currentRoom.getHeight() + roomDistance);
                                newRoom.setX(currentRoom.getX() + currentRoom.getWidth() / 2 - newRoom.getWidth() / 2);
                                break;
                            case NORTH_WEST:
                                newRoom.setY(currentRoom.getY() - newRoom.getHeight() - roomDistance);
                                newRoom.setX(currentRoom.getX() - newRoom.getWidth() - roomDistance);
                                break;
                            case NORTH_EAST:
                                newRoom.setY(currentRoom.getY() - newRoom.getHeight() - roomDistance);
                                newRoom.setX(currentRoom.getX() + currentRoom.getWidth() + roomDistance);
                                break;
                            case SOUTH_WEST:
                                newRoom.setY(currentRoom.getY() + currentRoom.getHeight() + roomDistance);
                                newRoom.setX(currentRoom.getX() - newRoom.getWidth() - roomDistance);
                                break;
                            case SOUTH_EAST:
                                newRoom.setY(currentRoom.getY() + currentRoom.getHeight() + roomDistance);
                                newRoom.setX(currentRoom.getX() + currentRoom.getWidth() + roomDistance);
                                break;
                        }
                    }


                    ConnectionLine connectionLine = lineList.findByStartAndEndRoomIgnoreLineDirection(currentRoom, newRoom);
                    if (connectionLine == null) {
                        // create a new line
                        connectionLine = new ConnectionLine(currentRoom, newRoom);
                        connectionLine.setInvalidationRunnable(lineInvalidationRunnable);
                        lineList.add(connectionLine);

                        final Line connectionLineCopy = connectionLine;
                        Platform.runLater(() -> drawing.getChildren().add(connectionLineCopy));
                    }

                    ConnectionLine finalConnectionLine = connectionLine;
                    Platform.runLater(finalConnectionLine::updateLocation);

                    if (!newRoom.isRendered()) {
                        // render the child
                        renderQueue.add(newRoom);
                    }
                }
            }

            // set the room count
            currentRoomCount = allRoomsAsList.size();
            allRoomsAsListCopy = null;
        });
    }


    @FXML
        // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        assert insertRoom != null : "fx:id=\"insertRoom\" was not injected: check your FXML file 'EditorMain.fxml'.";
        assert drawing != null : "fx:id=\"drawing\" was not injected: check your FXML file 'EditorMain.fxml'.";
        assert insertPath != null : "fx:id=\"insertPath\" was not injected: check your FXML file 'EditorMain.fxml'.";

        currentEditorInstance = this;

        // modify the default exception handler to show the ReportingDialog on every uncaught exception
        final Thread.UncaughtExceptionHandler currentUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((thread, exception) -> {
            if (currentUncaughtExceptionHandler != null) {
                // execute current handler as we only want to append it
                currentUncaughtExceptionHandler.uncaughtException(thread, exception);
            }
            Platform.runLater(() -> {
                new ExceptionAlert(exception).showAndWait();
                new ReportingDialog(stage.getScene()).show(AppConfig.gitHubUserName, AppConfig.gitHubRepoName, exception);
            });
        });

        currentGame.addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                newValue.modifiedProperty().addListener((observable1, oldValue1, newValue1) -> {
                    this.menuItemSave.setDisable(!newValue1);
                    setWindowTitle(newValue);
                });
            }
            setWindowTitle(newValue);
        });

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
        refreshViewButton.setTooltip(new Tooltip("Refresh the current view"));

        // forward events to all selected items
        // drawing.setOnMouseClicked(forwardEventsToSelectableNodesHandler);
        // drawing.setOnMousePressed(forwardEventsToSelectableNodesHandler);
        // drawing.setOnMouseReleased(forwardEventsToSelectableNodesHandler);
        // drawing.setOnDragDetected(forwardEventsToSelectableNodesHandler);
        // drawing.setOnMouseDragged(forwardEventsToSelectableNodesHandler);
        scrollPane.setOnKeyPressed(event -> {
            if (event.getCode().equals(KeyCode.DELETE)) {
                for (Node child : new ArrayList<>(drawing.getChildren())) {
                    if (child instanceof Disposable) {
                        if (((Disposable) child).isSelected() && event.getTarget() != child) {
                            FOKLogger.fine(EditorView.class.getName(), "Sending disposal command to child, Child is:  " + child.toString() + "\ntarget is: " + event.getTarget().toString());
                            try {
                                ((Disposable) child).dispose();
                            } catch (IllegalStateException e) {
                                FOKLogger.log(EditorView.class.getName(), Level.INFO, "User tried to remove the current room (not allowed)", e);
                                new Alert(Alert.AlertType.ERROR, "Could not perform delete operation: \n\n" + ExceptionUtils.getRootCauseMessage(e)).show();
                            }
                        }
                    }
                }
            } else if (event.getCode().equals(KeyCode.A) && event.isControlDown()) {
                // select everything
                for (Node child : new ArrayList<>(drawing.getChildren())) {
                    if (child instanceof Selectable) {
                        ((Selectable) child).setSelected(true);
                    }
                }
            }
        });
    }

    /**
     * Initializes a new game
     */
    public void initGame() {
        Game game = new Game();
        game.getCurrentRoom().setName("startRoom");
        loadGame(game);
    }

    /**
     * Loads the specified game file to this gui
     *
     * @param file The file to load
     */
    public void loadGame(File file) throws IOException, ClassNotFoundException {
        Game game = Game.load(file);
        loadGame(game);
    }

    /**
     * Loads the specified game to this gui
     *
     * @param game The game to load
     */
    public void loadGame(Game game) {
        currentGame.setValue(game);
        unconnectedRooms = new RoomRectangleList();
        allRoomsAsList = null;
        for (ConnectionLine line : new ConnectionLineList(lineList)) {
            line.invalidate();
        }
        renderView();
    }

    @SuppressWarnings("unused")
    public void setWindowTitle() {
        setWindowTitle(currentGame.getValue());
    }

    public void setWindowTitle(Game game) {
        String title = bundle.getString("windowTitle");

        if (game != null) {
            title = title + " - ";

            if (game.getFileSource() == null) {
                title = title + "unsaved game";
            } else {
                title = title + game.getFileSource().getName();
            }

            if (game.isModified()) {
                title = title + "*";
            }
        }

        stage.setTitle(title);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Platform.setImplicitExit(true);
        bundle = ResourceBundle.getBundle("view.strings");
        stage = primaryStage;

        try {
            Parent root = FXMLLoader.load(getClass().getResource("EditorMain.fxml"), bundle);

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("EditorMain.css").toExternalForm());

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

    @Override
    public void stop() {
        renderThreadPool.shutdownNow();

        // We need to call that explicitly because the ExecutorService makes the default exit bug around
        System.exit(0);
    }

    public EditMode getCurrentEditMode() {
        return currentEditMode;
    }

    public void setCurrentEditMode(EditMode currentEditMode) {
        FOKLogger.finer(MainWindow.class.getName(), "Setting currentEditMode to " + currentEditMode.toString());
        previousEditMode = this.currentEditMode;

        // Initialize or terminate the insert room mode
        if (isMouseOverDrawing) {
            if (currentEditMode == EditMode.INSERT_ROOM && previousEditMode != EditMode.INSERT_ROOM) {
                initInsertRoomEditMode();
            } else if (currentEditMode != EditMode.INSERT_ROOM && previousEditMode == EditMode.INSERT_ROOM) {
                terminateInsertRoomEditMode();
            }
        }

        this.currentEditMode = currentEditMode;
        this.insertPath.setSelected(currentEditMode == EditMode.INSERT_PATH);
        this.moveButton.setSelected(currentEditMode == EditMode.MOVE);
        this.insertRoom.setSelected(currentEditMode == EditMode.INSERT_ROOM);

    }

    public EditMode getPreviousEditMode() {
        return previousEditMode;
    }

    public RoomRectangleList getAllRoomsAsList() {
        if (allRoomsAsListCopy != null) {
            return allRoomsAsListCopy;
        } else {
            return allRoomsAsList;
        }
    }

    public RoomRectangleList getUnconnectedRooms() {
        return unconnectedRooms;
    }

    public Game getCurrentGame() {
        return currentGame.get();
    }

    @SuppressWarnings("unused")
    public ObjectProperty<Game> currentGameProperty() {
        return currentGame;
    }
}