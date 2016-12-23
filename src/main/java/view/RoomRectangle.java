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
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import logging.FOKLogger;
import model.Room;
import model.WalkDirection;
import model.WalkDirectionUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;

/**
 * The graphical representation of a {@link model.Room} in the {@link EditorView}
 */
public class RoomRectangle extends Rectangle {
    private Room room;
    private static FOKLogger log = new FOKLogger(RoomRectangle.class.getName());
    private BooleanProperty selected = new SimpleBooleanProperty();
    private BooleanProperty isTemporary = new SimpleBooleanProperty();
    private RoomRectangle thisRef = this;
    private boolean dragStarted;
    private Line line;
    private RoomRectangle previousTarget;
    private double moveStartLocalX = -1;
    private double moveStartLocalY = -1;
    private Label nameLabel = new Label();
    /**
     * Label to show the user that this is the current room
     */
    private ImageView currentPlayerIcon = new ImageView(new Image(this.getClass().getResourceAsStream("playerIcon.png")));
    private CustomGroup parent;
    private WalkDirection reevaluatedDirection;

    public RoomRectangle(CustomGroup parent) {
        this(parent, new Room());
    }

    public RoomRectangle(CustomGroup parent, Room room) {
        super();
        this.setRoom(room);
        this.setCustomParent(parent);

        this.nameLabel.textProperty().bind(this.getRoom().nameProperty());

        this.nameLabel.setTextFill(Color.BLACK);

        isTemporary.addListener((observable, oldValue, newValue) -> {
            Paint color;

            if (newValue) {
                // is temporary
                color = Color.GRAY;
            } else {
                // not temporary
                color = Color.BLACK;
            }

            nameLabel.setTextFill(color);
            this.setStroke(color);
        });

        // forward events from nameLabel and currentPlayerIcon to this rectangle
        nameLabel.setOnMousePressed(event -> thisRef.fireEvent(event));
        nameLabel.setOnMouseClicked(event -> thisRef.fireEvent(event));
        nameLabel.setOnMouseReleased(event -> thisRef.fireEvent(event));
        nameLabel.setOnDragDetected(event -> thisRef.fireEvent(event));
        nameLabel.setOnMouseDragged(event -> thisRef.fireEvent(event));

        currentPlayerIcon.setOnMousePressed(event -> thisRef.fireEvent(event));
        currentPlayerIcon.setOnMouseClicked(event -> thisRef.fireEvent(event));
        currentPlayerIcon.setOnMouseReleased(event -> thisRef.fireEvent(event));
        currentPlayerIcon.setOnDragDetected(event -> thisRef.fireEvent(event));
        currentPlayerIcon.setOnMouseDragged(event -> thisRef.fireEvent(event));

        // track changes of the parent node
        this.parentProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue instanceof CustomGroup || newValue == null) {
                thisRef.setCustomParent((CustomGroup) newValue, false);
            } else {
                throw new IllegalStateException("The parent of a RoomRectangle must be an instance of view.CustomGroup");
            }
        });

        this.currentPlayerIcon.setVisible(this.getRoom().isCurrentRoom());
        this.getRoom().isCurrentRoomProperty().addListener((observable, oldValue, newValue) -> currentPlayerIcon.setVisible(newValue));


        this.heightProperty().addListener((observable, oldValue, newValue) -> updateNameLabelPosition());
        this.widthProperty().addListener((observable, oldValue, newValue) -> updateNameLabelPosition());
        this.xProperty().addListener((observable, oldValue, newValue) -> updateNameLabelPosition());
        this.yProperty().addListener((observable, oldValue, newValue) -> updateNameLabelPosition());
        this.getRoom().nameProperty().addListener((observable, oldValue, newValue) -> {
            Thread t = new Thread(() -> {
                try {
                    Thread.sleep(12);
                } catch (InterruptedException e) {
                    log.getLogger().log(Level.SEVERE, "An error occurred", e);
                }

                Platform.runLater(this::updateNameLabelPosition);
            });

            t.start();
        });

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
                    line = new Line(this.getCenterX(), this.getCenterY(), event.getX(), event.getY());
                    this.getCustomParent().getChildren().add(line);
                } else {
                    line.setEndX(event.getX());
                    line.setEndY(event.getY());
                }

                RoomRectangle newTarget = (RoomRectangle) this.getCustomParent().getRectangleByCoordinatesPreferFront(event.getX(), event.getY());
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

                RoomRectangleList reevaluatedAdjacentRooms = new RoomRectangleList();

                for (Map.Entry<WalkDirection, Room> entry : this.getRoom().getAdjacentRooms().entrySet()) {
                    // reevaluate connection
                    RoomRectangle targetRoomRectangle = EditorView.currentEditorInstance.getAllRoomsAsList().findByRoom(entry.getValue());
                    assert targetRoomRectangle != null;
                    targetRoomRectangle.reevaluatedDirection = WalkDirectionUtils.getFromLine(this.getLineToRectangle(targetRoomRectangle));
                    reevaluatedAdjacentRooms.add(targetRoomRectangle);
                }

                // apply the reevaluated directions
                for (WalkDirection dir : WalkDirection.values()) {
                    RoomRectangleList subListWithCurrentDirection = new RoomRectangleList();
                    for (RoomRectangle r : reevaluatedAdjacentRooms) {
                        if (r.reevaluatedDirection == dir) {
                            subListWithCurrentDirection.add(r);
                        }
                    }

                    // only use the one with the smallest distance to this room
                    RoomRectangle finalRoom = subListWithCurrentDirection.findRoomWithMinimumDistanceTo(this);
                    if (finalRoom != null) {
                        if (this.getRoom().getAdjacentRooms().containsKey(dir)) {
                            // this has got a connection to another room in that direction that we need to delete
                            this.getRoom().getAdjacentRooms().get(dir).getAdjacentRooms().remove(WalkDirectionUtils.invert(dir));
                            this.getRoom().getAdjacentRooms().remove(dir);
                        }

                        if (finalRoom.getRoom().getAdjacentRooms().containsKey(WalkDirectionUtils.invert(dir))) {
                            // finalRoom has got a connection to another room in our direction that we need to delete
                            finalRoom.getRoom().getAdjacentRooms().get(WalkDirectionUtils.invert(dir)).getAdjacentRooms().remove(dir);
                            finalRoom.getRoom().getAdjacentRooms().remove(WalkDirectionUtils.invert(dir));
                        }

                        // delete the old connection between this and finalRoom
                        WalkDirection oldDirThisToFinalRoom = null;
                        for (Map.Entry<WalkDirection, Room> entry : this.getRoom().getAdjacentRooms().entrySet()) {
                            if (entry.getValue() == finalRoom.getRoom()) {
                                oldDirThisToFinalRoom = entry.getKey();
                                break;
                            }
                        }

                        if (oldDirThisToFinalRoom != null) {
                            this.getRoom().getAdjacentRooms().remove(oldDirThisToFinalRoom);
                            finalRoom.getRoom().getAdjacentRooms().remove(WalkDirectionUtils.invert(oldDirThisToFinalRoom));
                        }

                        for (Map.Entry<WalkDirection, Room> entry : finalRoom.getRoom().getAdjacentRooms().entrySet()) {
                            if (entry.getValue() == this.getRoom()) {
                                finalRoom.getRoom().getAdjacentRooms().remove(entry.getKey());
                            }
                        }

                        this.getRoom().getAdjacentRooms().put(dir, finalRoom.getRoom());
                        finalRoom.getRoom().getAdjacentRooms().put(WalkDirectionUtils.invert(dir), this.getRoom());
                    }
                }

                EditorView.currentEditorInstance.renderView(false, true);
            }
        });

        this.setOnMouseReleased(event -> {
            if (dragStarted) {
                dragStarted = false;
                log.getLogger().fine("Drag done");
                RoomRectangle target = (RoomRectangle) this.getCustomParent().getRectangleByCoordinatesPreferFront(event.getX(), event.getY());

                if (target != null && target != thisRef && EditorView.currentEditorInstance.getCurrentEditMode() == EditMode.INSERT_PATH) {
                    WalkDirection fromThisToTarget = WalkDirectionUtils.getFromLine(line);
                    WalkDirection fromTargetToThis = WalkDirectionUtils.invert(fromThisToTarget);

                    // Delete old references
                    if (this.getRoom().getAdjacentRooms().get(fromThisToTarget) != null) {
                        this.getRoom().getAdjacentRooms().get(fromThisToTarget).getAdjacentRooms().remove(fromTargetToThis);
                        this.getRoom().getAdjacentRooms().remove(fromThisToTarget);
                    }
                    if (target.getRoom().getAdjacentRooms().get(fromTargetToThis) != null) {
                        target.getRoom().getAdjacentRooms().get(fromTargetToThis).getAdjacentRooms().remove(fromThisToTarget);
                        target.getRoom().getAdjacentRooms().remove(fromTargetToThis);
                    }

                    log.getLogger().fine("Room is " + fromThisToTarget.toString());
                    this.getRoom().getAdjacentRooms().put(fromThisToTarget, target.getRoom());
                    target.getRoom().getAdjacentRooms().put(fromTargetToThis, this.getRoom());
                }

                // reset the dragging line if one was drawn
                if (line != null) {
                    line = null;
                }

                if (EditorView.currentEditorInstance.getCurrentEditMode() != EditMode.INSERT_ROOM) {
                    EditorView.currentEditorInstance.renderView(false);
                }
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

    @SuppressWarnings({"unused"})
    public boolean isSelected() {
        return selected.get();
    }

    public void setSelected(boolean selected) {
        this.selected.set(selected);
    }

    @SuppressWarnings({"unused"})
    public BooleanProperty isSelectedProperty() {
        return selected;
    }

    public void updateNameLabelPosition() {
        // Get the center of this rectangle
        double centerX = this.getX() + this.getWidth() / 2.0;
        double centerY = this.getY() + this.getHeight() / 2.0;

        // calculate the upper left corner of the label
        this.nameLabel.setLayoutX(centerX - nameLabel.getWidth() / 2.0);
        this.nameLabel.setLayoutY(centerY - nameLabel.getHeight() / 2.0);

        // calculate the upper left corner of the player icon
        this.currentPlayerIcon.setLayoutX(centerX - currentPlayerIcon.getImage().getWidth() / 2.0);
        this.currentPlayerIcon.setLayoutY(centerY + (nameLabel.getHeight() / 2.0) + 15 - currentPlayerIcon.getImage().getHeight() / 2.0);
    }

    /**
     * Sets the parent of this node like {@code Node.getChildren.add(this)}. The custom implementation was required to enforce that this node always has a parent.
     *
     * @param parent The parent to set
     */
    public void setCustomParent(CustomGroup parent) {
        this.setCustomParent(parent, true);
    }

    /**
     * Sets the current parent of the node. The custom implementation was required to enforce that this node always has a parent.
     *
     * @param parent          The new parent to set
     * @param registerAsChild If {@code true}, {@code Node.getParent()} will be updated too.
     */
    private void setCustomParent(CustomGroup parent, boolean registerAsChild) {
        // remove from previous parent
        if (registerAsChild && this.getCustomParent() != null) {
            this.getCustomParent().getChildren().remove(this.nameLabel);
            this.getCustomParent().getChildren().remove(this.currentPlayerIcon);
            this.getCustomParent().getChildren().remove(this);
        }

        this.parent = parent;

        Thread t = new Thread(() -> {
            try {
                Thread.sleep(12);
            } catch (InterruptedException e) {
                log.getLogger().log(Level.SEVERE, "An error occurred", e);
            }

            Platform.runLater(this::updateNameLabelPosition);
        });

        t.start();

        Platform.runLater(() -> {
            // add to new parent
            if (registerAsChild & parent != null) {
                parent.getChildren().add(thisRef);
                parent.getChildren().add(this.nameLabel);
                parent.getChildren().add(this.currentPlayerIcon);
            }
        });

    }

    /**
     * Gets the current parent of the node. The custom implementation was required to enforce that this node always has a parent.
     *
     * @return The current parent of the node.
     */
    public CustomGroup getCustomParent() {
        return parent;
    }

    public boolean isRendered() {
        return this.getCustomParent() != null;
    }

    public void setTemporary(boolean isTemporary) {
        this.isTemporary.set(isTemporary);
    }

    public boolean isTemporary() {
        return isTemporary.get();
    }

    public BooleanProperty isTemporaryProperty() {
        return isTemporary;
    }

    @Override
    public String toString() {
        return this.getRoom().getName();
    }

    /**
     * Returns the x coordinate of the center of this rectangle.
     *
     * @return The x coordinate of the center of this rectangle.
     */
    public double getCenterX() {
        return this.getX() + (this.getWidth() / 2);
    }

    /**
     * Returns the y coordinate of the center of this rectangle.
     *
     * @return The y coordinate of the center of this rectangle.
     */
    public double getCenterY() {
        return this.getY() + (this.getHeight() / 2);
    }

    /**
     * Calculates the pythagorean distance between the center coordinates of {@code this} and the {@code target}
     *
     * @param target The rectangle to calculate the distance to.
     * @return the pythagorean distance between the center coordinates of {@code this} and the {@code target}
     */
    public double distanceTo(@NotNull RoomRectangle target) {
        Objects.requireNonNull(target);

        return Math.sqrt(Math.pow(target.getCenterX() - this.getCenterX(), 2) + Math.pow(target.getCenterY() - this.getCenterY(), 2));
    }

    /**
     * Creates a Line between the centers of {@code this} and the target
     *
     * @param target The room rectangle whose center coordinates will serve as the end of the line
     * @return A {@code Line} that starts at the center of {@code this} and ends at the center of {@code target}
     */
    public Line getLineToRectangle(RoomRectangle target) {
        return new Line(this.getCenterX(), this.getCenterY(), target.getCenterX(), target.getCenterY());
    }
}
