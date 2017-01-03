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


import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import logging.FOKLogger;
import model.WalkDirection;
import model.WalkDirectionUtils;

import java.util.Map;

/**
 * A line that connects two rooms
 */
public class ConnectionLine extends Line implements Selectable, Disposable {
    private RoomRectangle startRoom;
    private RoomRectangle endRoom;
    private InvalidationRunnable invalidationRunnable;
    private BooleanProperty selected = new SimpleBooleanProperty();
    private ConnectionLine thisRef = this;

    public ConnectionLine() {
        this(null, null);
    }

    public ConnectionLine(RoomRectangle startRoom, RoomRectangle endRoom) {
        setStartRoom(startRoom);
        setEndRoom(endRoom);
        updateLocation();

        this.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1) {
                this.setSelected(true);
            }
        });

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

    public RoomRectangle getEndRoom() {
        return endRoom;
    }

    public RoomRectangle getStartRoom() {
        return startRoom;
    }

    public void setEndRoom(RoomRectangle endRoom) {
        this.endRoom = endRoom;
        updateLocation();
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
        }
    }

    public InvalidationRunnable getInvalidationRunnable() {
        return invalidationRunnable;
    }

    public void setInvalidationRunnable(InvalidationRunnable invalidationRunnable) {
        this.invalidationRunnable = invalidationRunnable;
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

    public interface InvalidationRunnable {
        void run(ConnectionLine lineToDispose);
    }

    @Override
    public String toString() {
        return super.toString() + ", connecting " + getStartRoom().getRoom().getName() + " and " + getEndRoom().getRoom().getName();
    }
}
