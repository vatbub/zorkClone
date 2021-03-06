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


import com.github.vatbub.common.core.logging.FOKLogger;
import com.github.vatbub.common.view.core.CustomGroup;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import model.Room;
import model.WalkDirection;
import model.WalkDirectionUtils;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;

/**
 * The graphical representation of a {@link model.Room} in the {@link EditorView}
 */
public class RoomRectangle extends Rectangle implements Serializable, Disposable, Selectable {
    private static final int minRectangleWidth = 100;
    private static List<RoomRectangle> movedRooms;
    private final BooleanProperty selected = new SimpleBooleanProperty();
    private final BooleanProperty isTemporary = new SimpleBooleanProperty();
    private final RoomRectangle thisRef = this;
    private final Label nameLabel = new Label();
    /**
     * Label to show the user that this is the current room
     */
    private final PlayerIcon currentPlayerIcon = new PlayerIcon(new Image(this.getClass().getResourceAsStream("playerIcon.png")), this);
    private Room room;
    private boolean dragStarted;
    private Line line;
    private RoomRectangle previousTarget;
    private double moveStartLocalX = -1;
    private double moveStartLocalY = -1;
    private CustomGroup parent;
    private WalkDirection reevaluatedDirection;

    public RoomRectangle(@SuppressWarnings("SameParameterValue") CustomGroup parent) {
        this(parent, new Room());
    }

    public RoomRectangle(CustomGroup parent, Room room) {
        super();
        this.setRoom(room);
        this.setCustomParent(parent);

        thisRef.nameLabel.setText(thisRef.getRoom().getName());
        this.getRoom().setNameChangeListener((Runnable & Serializable) () -> {
            thisRef.nameLabel.setText(thisRef.getRoom().getName());
            Platform.runLater(this::updateNameLabelPosition);
        });

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
        nameLabel.setOnMousePressed(thisRef::fireEvent);
        nameLabel.setOnMouseClicked(thisRef::fireEvent);
        nameLabel.setOnMouseReleased(thisRef::fireEvent);
        nameLabel.setOnDragDetected(thisRef::fireEvent);
        nameLabel.setOnMouseDragged(thisRef::fireEvent);

        currentPlayerIcon.setOnMouseClicked(thisRef::fireEvent);

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

        this.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1) {
                this.setSelected(!this.isSelected());
            } else if (event.getClickCount() == 2) {
                // launch editor
                FOKLogger.info(RoomRectangle.class.getName(), "RoomEditor launched");
                new RoomEditor(this.getRoom()).show();
            }
        });

        // Add Path using drag and drop
        this.setOnDragDetected(event -> dragStarted = true);

        this.setOnMousePressed(event -> {
            this.moveStartLocalX = event.getX() - this.getX();
            this.moveStartLocalY = event.getY() - this.getY();
            event.consume();
        });

        this.setOnMouseDragged(event -> {
            // Forward event to scroll pane to fade out the compass image if needed
            EditorView.currentEditorInstance.scrollPaneOnMouseMoved(event);
            if (EditorView.currentEditorInstance.getCurrentEditMode() == EditMode.INSERT_PATH) {
                FOKLogger.fine(RoomRectangle.class.getName(), "Inserting new path...");

                RoomRectangle newTarget = (RoomRectangle) this.getCustomParent().getRectangleByCoordinatesPreferFront(event.getX(), event.getY());
                if (newTarget != previousTarget && previousTarget != null) {
                    previousTarget.setSelected(false);
                }
                if (newTarget != null && newTarget != thisRef) {
                    newTarget.setSelected(true);
                }

                previousTarget = newTarget;

                if (line == null) {
                    line = new Line(this.moveStartLocalX + this.getX(), this.moveStartLocalY + this.getY(), event.getX(), event.getY());
                    this.getCustomParent().getChildren().add(line);
                } else {
                    double startX = 0;
                    double startY = 0;
                    double endX = event.getX();
                    double endY = event.getY();

                    if (event.getX() >= this.getX() && event.getX() <= this.getX() + this.getWidth() && event.getY() >= this.getY() && event.getY() <= this.getY() + this.getHeight()) {
                        startX = this.moveStartLocalX + this.getX();
                        startY = this.moveStartLocalY + this.getY();
                    } else {
                        WalkDirection dir = WalkDirectionUtils.getFromLine(new Line(this.getCenterX(), this.getCenterY(), endX, endY));
                        if (newTarget != null) {
                            dir = WalkDirectionUtils.getFromLine(new Line(this.getCenterX(), this.getCenterY(), newTarget.getCenterX(), newTarget.getCenterY()));
                            WalkDirection dir2 = WalkDirectionUtils.invert(dir);
                            switch (dir2) {
                                case NORTH:
                                    endX = newTarget.getCenterX();
                                    endY = newTarget.getY();
                                    break;
                                case WEST:
                                    endX = newTarget.getX();
                                    endY = newTarget.getCenterY();
                                    break;
                                case EAST:
                                    endX = newTarget.getX() + newTarget.getWidth();
                                    endY = newTarget.getCenterY();
                                    break;
                                case SOUTH:
                                    endX = newTarget.getCenterX();
                                    endY = newTarget.getY() + newTarget.getHeight();
                                    break;
                                case NORTH_WEST:
                                    endX = newTarget.getX();
                                    endY = newTarget.getY();
                                    break;
                                case NORTH_EAST:
                                    endX = newTarget.getX() + newTarget.getWidth();
                                    endY = newTarget.getY();
                                    break;
                                case SOUTH_WEST:
                                    endX = newTarget.getX();
                                    endY = newTarget.getY() + newTarget.getHeight();
                                    break;
                                case SOUTH_EAST:
                                    endX = newTarget.getX() + newTarget.getWidth();
                                    endY = newTarget.getY() + newTarget.getHeight();
                                    break;
                                case NONE:
                                    throw new IllegalStateException("WalkDirection is NONE");
                            }
                        }

                        for (Map.Entry<WalkDirection, Room> entry : this.getRoom().getAdjacentRooms().entrySet()) {
                            ConnectionLine line2 = EditorView.currentEditorInstance.lineList.findByStartAndEndRoomIgnoreLineDirection(this, EditorView.currentEditorInstance.getAllRoomsAsList().findByRoom(entry.getValue()));
                            if (line2 != null) {
                                line2.setSelected(entry.getKey() == dir && newTarget != null && newTarget.getRoom() != this.getRoom().getAdjacentRooms().get(entry.getKey()));
                            }
                        }
                        switch (dir) {
                            case NORTH:
                                startX = this.getCenterX();
                                startY = this.getY();
                                break;
                            case WEST:
                                startX = this.getX();
                                startY = this.getCenterY();
                                break;
                            case EAST:
                                startX = this.getX() + this.getWidth();
                                startY = this.getCenterY();
                                break;
                            case SOUTH:
                                startX = this.getCenterX();
                                startY = this.getY() + this.getHeight();
                                break;
                            case NORTH_WEST:
                                startX = this.getX();
                                startY = this.getY();
                                break;
                            case NORTH_EAST:
                                startX = this.getX() + this.getWidth();
                                startY = this.getY();
                                break;
                            case SOUTH_WEST:
                                startX = this.getX();
                                startY = this.getY() + this.getHeight();
                                break;
                            case SOUTH_EAST:
                                startX = this.getX() + this.getWidth();
                                startY = this.getY() + this.getHeight();
                                break;
                            case NONE:
                                throw new IllegalStateException("WalkDirection is NONE");
                        }
                    }

                    line.setStartX(startX);
                    line.setStartY(startY);
                    line.setEndX(endX);
                    line.setEndY(endY);
                }
            } else if (EditorView.currentEditorInstance.getCurrentEditMode() == EditMode.MOVE) {
                FOKLogger.fine(RoomRectangle.class.getName(), "Moving room...");
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
                            if (this.getRoom().getAdjacentRooms().get(dir) != finalRoom.getRoom()) {
                                // this has got a connection to another room in that direction that we need to delete
                                this.getRoom().getAdjacentRooms().get(dir).getAdjacentRooms().remove(WalkDirectionUtils.invert(dir));
                                this.getRoom().getAdjacentRooms().remove(dir);
                            }
                        }

                        if (finalRoom.getRoom().getAdjacentRooms().containsKey(WalkDirectionUtils.invert(dir))) {
                            if (finalRoom.getRoom().getAdjacentRooms().get(WalkDirectionUtils.invert(dir)) != this.getRoom()) {
                                // finalRoom has got a connection to another room in our direction that we need to delete
                                finalRoom.getRoom().getAdjacentRooms().get(WalkDirectionUtils.invert(dir)).getAdjacentRooms().remove(dir);
                                finalRoom.getRoom().getAdjacentRooms().remove(WalkDirectionUtils.invert(dir));
                            }
                        }

                        // delete the old connection between this and finalRoom
                        WalkDirection oldDirThisToFinalRoom = null;
                        for (Map.Entry<WalkDirection, Room> entry : this.getRoom().getAdjacentRooms().entrySet()) {
                            if (entry.getValue() == finalRoom.getRoom()) {
                                oldDirThisToFinalRoom = entry.getKey();
                                break;
                            }
                        }

                        if (oldDirThisToFinalRoom != null && oldDirThisToFinalRoom != dir) {
                            // there was an old connection, remove it
                            this.getRoom().getAdjacentRooms().remove(oldDirThisToFinalRoom);
                            finalRoom.getRoom().getAdjacentRooms().remove(WalkDirectionUtils.invert(oldDirThisToFinalRoom));
                        }

                        if (oldDirThisToFinalRoom != dir) {
                            this.getRoom().getAdjacentRooms().put(dir, finalRoom.getRoom());
                            finalRoom.getRoom().getAdjacentRooms().put(WalkDirectionUtils.invert(dir), this.getRoom());
                        }
                    }
                }

                EditorView.currentEditorInstance.renderView(false, true);
            }
        });

        this.setOnMouseReleased(event -> {
            if (dragStarted) {
                dragStarted = false;
                FOKLogger.fine(RoomRectangle.class.getName(), "Drag done");
                RoomRectangle target = (RoomRectangle) this.getCustomParent().getRectangleByCoordinatesPreferFront(event.getX(), event.getY());

                if (target != null && target != thisRef && EditorView.currentEditorInstance.getCurrentEditMode() == EditMode.INSERT_PATH) {
                    WalkDirection fromThisToTarget = WalkDirectionUtils.getFromLine(new Line(this.getCenterX(), this.getCenterY(), target.getCenterX(), target.getCenterY()));
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

                    // Delete old connection between this and target
                    WalkDirection dir = this.getRoom().getDirectionTo(target.getRoom());
                    if (dir != null) {
                        if (dir != fromThisToTarget) {
                            this.getRoom().getAdjacentRooms().remove(dir);
                            target.getRoom().getAdjacentRooms().remove(WalkDirectionUtils.invert(dir));
                        }
                    }

                    FOKLogger.fine(RoomRectangle.class.getName(), "Room is " + fromThisToTarget.toString());
                    this.getRoom().getAdjacentRooms().put(fromThisToTarget, target.getRoom());
                    target.getRoom().getAdjacentRooms().put(fromTargetToThis, this.getRoom());
                    target.setSelected(false);
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
            FOKLogger.finest(RoomRectangle.class.getName(), "Room selected = " + newValue);
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

    private static void moveRooms(RoomRectangle currentRoom, double widthDiff) {
        for (Map.Entry<WalkDirection, Room> entry : currentRoom.getRoom().getAdjacentRooms().entrySet()) {
            boolean skip = false;
            RoomRectangle adjRoomRectangle = EditorView.currentEditorInstance.getAllRoomsAsList().findByRoom(entry.getValue());
            assert adjRoomRectangle != null;

            switch (entry.getKey()) {
                case NORTH:
                case SOUTH:
                    adjRoomRectangle.setX(adjRoomRectangle.getX() + (widthDiff / 2));
                    break;
                case EAST:
                case NORTH_EAST:
                case SOUTH_EAST:
                    adjRoomRectangle.setX(adjRoomRectangle.getX() + widthDiff);
                    break;
                default:
                    skip = true;
                    break;
            }

            if (!skip && !movedRooms.contains(adjRoomRectangle)) {
                movedRooms.add(adjRoomRectangle);
                moveRooms(adjRoomRectangle, widthDiff);
            }
        }
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
        // adapt the width of the rectangle
        // min width 100 px
        double newWidth = Math.max(minRectangleWidth, this.nameLabel.getWidth() + 60);

        // move connected rooms
        double widthDiff = newWidth - this.getWidth();
        movedRooms = new ArrayList<>();
        moveRooms(this, widthDiff);
        Platform.runLater(() -> EditorView.currentEditorInstance.renderView(false, true));

        this.setWidth(newWidth);

        // Get the center of this rectangle
        double centerX = this.getX() + this.getWidth() / 2.0;
        double centerY = this.getY() + this.getHeight() / 2.0;

        // calculate the upper left corner of the label
        this.nameLabel.setLayoutX(centerX - nameLabel.getWidth() / 2.0);
        this.nameLabel.setLayoutY(centerY - nameLabel.getHeight() / 2.0);

        // calculate the upper left corner of the player icon
        this.currentPlayerIcon.setX(centerX - currentPlayerIcon.getImage().getWidth() / 2.0);
        this.currentPlayerIcon.setY(centerY + (nameLabel.getHeight() / 2.0) + 15 - currentPlayerIcon.getImage().getHeight() / 2.0);
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

        Thread t = new Thread((Runnable & Serializable) () -> {
            try {
                Thread.sleep(12);
            } catch (InterruptedException e) {
                FOKLogger.log(RoomRectangle.class.getName(), Level.SEVERE, "An error occurred", e);
            }

            Platform.runLater(this::updateNameLabelPosition);
        });

        t.start();

        Platform.runLater((Runnable & Serializable) () -> {
            // add to new parent
            if (registerAsChild & parent != null) {
                if (!parent.getChildren().contains(thisRef))
                    parent.getChildren().add(thisRef);
                if (!parent.getChildren().contains(this.nameLabel))
                    parent.getChildren().add(this.nameLabel);
                if (!parent.getChildren().contains(this.currentPlayerIcon))
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

    /**
     * Sets the parent of this node like {@code Node.getChildren.add(this)}. The custom implementation was required to enforce that this node always has a parent.
     *
     * @param parent The parent to set
     */
    public void setCustomParent(CustomGroup parent) {
        this.setCustomParent(parent, true);
    }

    public boolean isRendered() {
        return this.getCustomParent() != null;
    }

    @SuppressWarnings("unused")
    public boolean isTemporary() {
        return isTemporary.get();
    }

    public void setTemporary(boolean isTemporary) {
        this.isTemporary.set(isTemporary);
    }

    @SuppressWarnings("unused")
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

    public Label getNameLabel() {
        return nameLabel;
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

    @Override
    public void dispose() {
        if (this.getRoom().isCurrentRoom()) {
            throw new IllegalStateException("Cannot remove the room where the player is currently in: " + this.toString());
        }

        FOKLogger.fine(RoomRectangle.class.getName(), "Disposing room " + this.toString() + "...");
        for (Map.Entry<WalkDirection, Room> entry : this.getRoom().getAdjacentRooms().entrySet()) {
            entry.getValue().getAdjacentRooms().remove(WalkDirectionUtils.invert(entry.getKey()));
            this.getRoom().getAdjacentRooms().remove(entry.getKey());
        }

        EditorView.currentEditorInstance.getAllRoomsAsList().remove(this);
        EditorView.currentEditorInstance.getUnconnectedRooms().remove(this);
        this.setCustomParent(null);
        EditorView.currentEditorInstance.renderView(false);
    }
}
