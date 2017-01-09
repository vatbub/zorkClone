package view;

/*-
 * #%L
 * Zork Clone
 * %%
 * Copyright (C) 2016 - 2017 Frederik Kammel
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
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import logging.FOKLogger;
import model.WalkDirection;
import model.WalkDirectionUtils;

import java.io.Serializable;
import java.util.Map;
import java.util.logging.Level;

/**
 * A line that connects two rooms
 */
public class ConnectionLine extends Line implements Selectable, Disposable {
    private RoomRectangle startRoom;
    private RoomRectangle endRoom;
    private InvalidationRunnable invalidationRunnable;
    private BooleanProperty selected = new SimpleBooleanProperty();
    private ConnectionLine thisRef = this;
    private CustomGroup parent;
    private Line hitboxLine = new Line();

    public ConnectionLine() {
        this(null, null);
    }

    public ConnectionLine(RoomRectangle startRoom, RoomRectangle endRoom) {
        setStartRoom(startRoom);
        setEndRoom(endRoom);
        updateLocation();

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

        // bind the hitbox line to the location of this line
        hitboxLine.startXProperty().bind(this.startXProperty());
        hitboxLine.startYProperty().bind(this.startYProperty());
        hitboxLine.endXProperty().bind(this.endXProperty());
        hitboxLine.endYProperty().bind(this.endYProperty());

        // set the style of the hitbox line
        hitboxLine.setStrokeWidth(10);
        hitboxLine.setStroke(Color.RED);
        hitboxLine.setOpacity(0);

        hitboxLine.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1) {
                this.setSelected(true);
            }
        });

        // track changes of the parent node
        this.parentProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue instanceof CustomGroup || newValue == null) {
                thisRef.setCustomParent((CustomGroup) newValue, false);
            } else {
                throw new IllegalStateException("The parent of a RoomRectangle must be an instance of view.CustomGroup");
            }
        });
    }

    public RoomRectangle getEndRoom() {
        return endRoom;
    }

    public void setEndRoom(RoomRectangle endRoom) {
        this.endRoom = endRoom;
        updateLocation();
    }

    public RoomRectangle getStartRoom() {
        return startRoom;
    }

    public void setStartRoom(RoomRectangle startRoom) {
        this.startRoom = startRoom;
        updateLocation();
    }

    /**
     * Invalidates this line
     */
    public void invalidate() {
        if (invalidationRunnable != null) {
            invalidationRunnable.run(this);
        }
    }

    /**
     * Updates the location of this line
     */
    public void updateLocation() {
        if (getStartRoom() != null && getEndRoom() != null) {
            if (!getStartRoom().getRoom().isDirectlyConnectedTo(getEndRoom().getRoom())) {
                // rooms not connected, detach this line
                invalidate();
                return;
            }

            // get the connection direction
            WalkDirection dir = null;
            // dir will never be null as start and end room are proven to be direct neighbours
            for (Map.Entry entry : getStartRoom().getRoom().getAdjacentRooms().entrySet()) {
                if (entry.getValue() == getEndRoom().getRoom()) {
                    dir = (WalkDirection) entry.getKey();
                }
            }

            switch (dir) {
                case NORTH:
                    this.setStartX(getStartRoom().getX() + getStartRoom().getWidth() / 2.0);
                    this.setStartY(getStartRoom().getY());
                    this.setEndX(getEndRoom().getX() + getEndRoom().getWidth() / 2.0);
                    this.setEndY(getEndRoom().getY() + getEndRoom().getHeight());
                    break;
                case WEST:
                    this.setStartX(getStartRoom().getX());
                    this.setStartY(getStartRoom().getY() + getStartRoom().getHeight() / 2);
                    this.setEndX(getEndRoom().getX() + getEndRoom().getWidth());
                    this.setEndY(getEndRoom().getY() + getEndRoom().getHeight() / 2);
                    break;
                case EAST:
                    this.setStartX(getStartRoom().getX() + getStartRoom().getWidth());
                    this.setStartY(getStartRoom().getY() + getStartRoom().getHeight() / 2);
                    this.setEndX(getEndRoom().getX());
                    this.setEndY(getEndRoom().getY() + getEndRoom().getHeight() / 2);
                    break;
                case SOUTH:
                    this.setStartX(getStartRoom().getX() + getStartRoom().getWidth() / 2.0);
                    this.setStartY(getStartRoom().getY() + getStartRoom().getHeight());
                    this.setEndX(getEndRoom().getX() + getEndRoom().getWidth() / 2.0);
                    this.setEndY(getEndRoom().getY());
                    break;
                case NORTH_WEST:
                    this.setStartX(getStartRoom().getX());
                    this.setStartY(getStartRoom().getY());
                    this.setEndX(getEndRoom().getX() + getEndRoom().getWidth());
                    this.setEndY(getEndRoom().getY() + getEndRoom().getHeight());
                    break;
                case NORTH_EAST:
                    this.setStartX(getStartRoom().getX() + getStartRoom().getWidth());
                    this.setStartY(getStartRoom().getY());
                    this.setEndX(getEndRoom().getX());
                    this.setEndY(getEndRoom().getY() + getEndRoom().getHeight());
                    break;
                case SOUTH_WEST:
                    this.setStartX(getStartRoom().getX());
                    this.setStartY(getStartRoom().getY() + getStartRoom().getHeight());
                    this.setEndX(getEndRoom().getX() + getEndRoom().getWidth());
                    this.setEndY(getEndRoom().getY());
                    break;
                case SOUTH_EAST:
                    this.setStartX(getStartRoom().getX() + getStartRoom().getWidth());
                    this.setStartY(getStartRoom().getY() + getStartRoom().getHeight());
                    this.setEndX(getEndRoom().getX());
                    this.setEndY(getEndRoom().getY());
                    break;
            }

            // add the hitbox line if not added already
            if (getCustomParent() != null) {
                if (!getCustomParent().getChildren().contains(hitboxLine)) {
                    getCustomParent().getChildren().add(hitboxLine);
                }
            }
        }
    }

    public InvalidationRunnable getInvalidationRunnable() {
        return invalidationRunnable;
    }

    public void setInvalidationRunnable(InvalidationRunnable invalidationRunnable) {
        this.invalidationRunnable = invalidationRunnable;
    }

    /**
     * Calculates the angle that this line should be rendered with when using autoLayout.
     *
     * @return The angle that this line should be rendered with when using autoLayout in rad.
     */
    public double getPreferredAngle() {
        double res = 0;
        switch (this.getStartRoom().getRoom().getAdjacentRooms().getKeyForObject(this.getEndRoom().getRoom())) {
            case NORTH:
                res = 0;
                break;
            case WEST:
                res = -0.5;
                break;
            case EAST:
                res = 0.5;
                break;
            case SOUTH:
                res = 1;
                break;
            case NORTH_WEST:
                res = -0.25;
                break;
            case NORTH_EAST:
                res = 0.25;
                break;
            case SOUTH_WEST:
                res = -0.75;
                break;
            case SOUTH_EAST:
                res = 0.75;
                break;
            case NONE:
                // Will not occur
                break;
        }

        return res * Math.PI;
    }

    /**
     * Calculates the angle that the line is pointing in
     *
     * @return The angle that the line is pointing in
     */
    public double getAngle() {
        return Math.atan2(this.getEndX() - this.getStartX(), this.getStartY() - this.getEndY());
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

    @Override
    public void dispose() {
        WalkDirection dir = WalkDirectionUtils.getFromLine(this);
        this.getStartRoom().getRoom().getAdjacentRooms().remove(dir);
        this.getEndRoom().getRoom().getAdjacentRooms().remove(WalkDirectionUtils.invert(dir));
        this.invalidate();
    }

    @Override
    public String toString() {
        return super.toString() + ", connecting " + getStartRoom().getRoom().getName() + " and " + getEndRoom().getRoom().getName();
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
            this.getCustomParent().getChildren().remove(hitboxLine);
            this.getCustomParent().getChildren().remove(this);
        }

        this.parent = parent;

        Thread t = new Thread((Runnable & Serializable) () -> {
            try {
                Thread.sleep(12);
            } catch (InterruptedException e) {
                FOKLogger.log(ConnectionLine.class.getName(), Level.SEVERE, "An error occurred", e);
            }

            Platform.runLater(this::updateLocation);
        });

        t.start();

        Platform.runLater((Runnable & Serializable) () -> {
            // add to new parent
            if (registerAsChild & parent != null) {
                parent.getChildren().add(thisRef);
                parent.getChildren().add(hitboxLine);
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

    public interface InvalidationRunnable {
        void run(ConnectionLine lineToDispose);
    }
}
