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


import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import logging.FOKLogger;
import model.Room;
import model.WalkDirection;

/**
 * The graphical representation of a {@link model.Room} in the {@link EditorView}
 */
public class RoomRectangle extends Rectangle {
    private Room room;
    private static FOKLogger log = new FOKLogger(RoomRectangle.class.getName());
    private BooleanProperty selected = new SimpleBooleanProperty();
    private RoomRectangle thisRef = this;
    private boolean dragStarted;
    private Line line;
    private RoomRectangle previousTarget;
    private double moveStartLocalX = -1;
    private double moveStartLocalY = -1;
    private Label nameLabel = new Label();

    public RoomRectangle() {
        this(new Room());
    }

    public RoomRectangle(Room room) {
        super();
        this.setRoom(room);

        this.nameLabel.textProperty().bind(this.getRoom().nameProperty());
        this.nameLabel.setTextFill(Color.BLACK);
        // this.nameLabel.setPrefHeight(10);
        // this.nameLabel.setPrefWidth(30);

        Thread addNameLabelThread = new Thread(() -> {
            updateNameLabelPosition();
            while (this.getParent() == null) {
                // wait for parent
            }
            System.out.println("Label added");
            Platform.runLater(() -> ((Group) this.getParent()).getChildren().add(this.nameLabel));
            System.out.println("Roomname: " + this.getRoom().getName());
            System.out.println("Labeltext: " + this.nameLabel.getText());
        });
        addNameLabelThread.setName("addNameLabelThread");
        addNameLabelThread.start();

        this.heightProperty().addListener((observable, oldValue, newValue) -> updateNameLabelPosition());
        this.widthProperty().addListener((observable, oldValue, newValue) -> updateNameLabelPosition());
        this.xProperty().addListener((observable, oldValue, newValue) -> updateNameLabelPosition());
        this.yProperty().addListener((observable, oldValue, newValue) -> updateNameLabelPosition());

        this.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1) {
                this.setSelected(true);
            } else if (event.getClickCount() == 2) {
                // launch editor
                log.getLogger().info("RoomEditor launched");
            }
        });

        // Add Path using drag and drop
        this.setOnDragDetected(event -> dragStarted = true);

        this.setOnMousePressed(event -> {
            this.moveStartLocalX = event.getX() - this.getX();
            this.moveStartLocalY = event.getY() - this.getY();
        });

        this.setOnMouseDragged(event -> {
            if (EditorView.currentEditorInstance.getCurrentEditMode() == EditMode.INSERT_PATH) {
                log.getLogger().fine("Inserting new path...");
                if (line == null) {
                    line = new Line(this.getX() + (this.getWidth() / 2), this.getY() + (this.getHeight() / 2), event.getX(), event.getY());
                    ((Group) this.getParent()).getChildren().add(line);
                } else {
                    line.setEndX(event.getX());
                    line.setEndY(event.getY());
                }

                RoomRectangle newTarget = (RoomRectangle) ((CustomGroup) this.getParent()).getRectangleByCoordinatesPreferFront(event.getX(), event.getY());
                if (newTarget != previousTarget && previousTarget != null) {
                    previousTarget.setSelected(false);
                }
                if (newTarget != null && newTarget != thisRef) {
                    newTarget.setSelected(true);
                }

                previousTarget = newTarget;
            } else if (EditorView.currentEditorInstance.getCurrentEditMode() == EditMode.MOVE) {
                log.getLogger().fine("Moving room...");
                this.setX(event.getX() - this.moveStartLocalX);
                this.setY(event.getY() - this.moveStartLocalY);
            }
        });

        this.setOnMouseReleased(event -> {
            if (dragStarted) {
                dragStarted = false;
                System.out.println("Drag done");
                RoomRectangle target = (RoomRectangle) ((CustomGroup) this.getParent()).getRectangleByCoordinatesPreferFront(event.getX(), event.getY());

                if (target != null && EditorView.currentEditorInstance.getCurrentEditMode() == EditMode.INSERT_PATH) {
                    double lineAngle = Math.atan2(line.getEndX() - line.getStartX(), line.getStartY() - line.getEndY());

                    WalkDirection fromThisToTarget = null;
                    WalkDirection fromTargetToThis = null;

                    if (lineAngle <= (Math.PI / 8.0) && lineAngle >= (-Math.PI / 8.0)) {
                        // north
                        fromThisToTarget = WalkDirection.NORTH;
                        fromTargetToThis = WalkDirection.SOUTH;
                    } else if (lineAngle < (Math.PI * 3.0 / 8.0) && lineAngle > (Math.PI / 8.0)) {
                        // ne
                        fromThisToTarget = WalkDirection.NORTH_EAST;
                        fromTargetToThis = WalkDirection.SOUTH_WEST;
                    } else if (lineAngle <= (Math.PI * 5.0 / 8.0) && lineAngle >= (Math.PI * 3.0 / 8.0)) {
                        // e
                        fromThisToTarget = WalkDirection.EAST;
                        fromTargetToThis = WalkDirection.WEST;
                    } else if (lineAngle < (Math.PI * 7.0 / 8.0) && lineAngle > (Math.PI * 5.0 / 8.0)) {
                        // se
                        fromThisToTarget = WalkDirection.SOUTH_EAST;
                        fromTargetToThis = WalkDirection.NORTH_WEST;
                    } else if ((lineAngle <= (-Math.PI * 7.0 / 8.0) && lineAngle >= (-Math.PI)) || (lineAngle <= (Math.PI) && lineAngle >= (Math.PI * 7.0 / 8.0))) {
                        // s
                        fromThisToTarget = WalkDirection.SOUTH;
                        fromTargetToThis = WalkDirection.NORTH;
                    } else if (lineAngle < (-Math.PI * 5.0 / 8.0) && lineAngle > (-Math.PI * 7.0 / 8.0)) {
                        // sw
                        fromThisToTarget = WalkDirection.SOUTH_WEST;
                        fromTargetToThis = WalkDirection.NORTH_EAST;
                    } else if (lineAngle <= (-Math.PI * 3.0 / 8.0) && lineAngle >= (-Math.PI * 5.0 / 8.0)) {
                        // w
                        fromThisToTarget = WalkDirection.WEST;
                        fromTargetToThis = WalkDirection.EAST;
                    } else if (lineAngle < (-Math.PI * 1.0 / 8.0) && lineAngle > (-Math.PI * 3.0 / 8.0)) {
                        // nw
                        fromThisToTarget = WalkDirection.NORTH_WEST;
                        fromTargetToThis = WalkDirection.SOUTH_EAST;
                    }

                    // Delete old references
                    if (this.getRoom().getAdjacentRooms().get(fromThisToTarget) != null) {
                        this.getRoom().getAdjacentRooms().get(fromThisToTarget).getAdjacentRooms().remove(fromTargetToThis);
                    }
                    if (target.getRoom().getAdjacentRooms().get(fromTargetToThis) != null) {
                        target.getRoom().getAdjacentRooms().get(fromTargetToThis).getAdjacentRooms().remove(fromThisToTarget);
                    }

                    System.out.println("Room is " + fromThisToTarget.toString());
                    this.getRoom().getAdjacentRooms().put(fromThisToTarget, target.getRoom());
                    target.getRoom().getAdjacentRooms().put(fromTargetToThis, this.getRoom());
                    EditorView.currentEditorInstance.setRoomAsConnected(this);
                    EditorView.currentEditorInstance.setRoomAsConnected(target);
                }
                EditorView.currentEditorInstance.renderView(false);
            }
        });

        // Style
        this.setWidth(100);
        this.setHeight(100);
        this.setFill(Color.WHITE);
        this.setStroke(Color.BLACK);

        selected.addListener((observable, oldValue, newValue) -> {
            log.getLogger().finest("Room selected = " + newValue);
            if (newValue) {
                // is selected
                thisRef.setStroke(Color.GRAY);
                thisRef.setStrokeWidth(4);
            } else {
                // not selected
                thisRef.setStroke(Color.BLACK);
                thisRef.setStrokeWidth(1);
            }
        });
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public boolean isSelected() {
        return selected.get();
    }

    public void setSelected(boolean selected) {
        this.selected.set(selected);
    }

    public BooleanProperty isSelectedProperty() {
        return selected;
    }

    public void updateNameLabelPosition() {
        // Get the center of this rectangle
        double centerX = this.getLayoutX() + this.getWidth() / 2.0;
        double centerY = this.getLayoutY() + this.getHeight() / 2.0;

        // calculate the upper left corner of the label
        this.nameLabel.setLayoutX(centerX - nameLabel.getWidth() / 2.0);
        this.nameLabel.setLayoutY(centerY - nameLabel.getHeight() / 2.0);
    }
}
