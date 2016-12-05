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


import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import logging.FOKLogger;
import model.Room;

/**
 * The graphical representation of a {@link model.Room} in the {@link EditorView}
 */
public class RoomRectangle extends Rectangle {
    private Room room;
    private static FOKLogger log = new FOKLogger(RoomRectangle.class.getName());
    private BooleanProperty selected = new SimpleBooleanProperty();
    private RoomRectangle thisRef = this;

    public RoomRectangle() {
        this(new Room());
    }

    public RoomRectangle(Room room) {
        super();
        this.setRoom(room);

        this.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1) {
                this.setSelected(true);
            } else if (event.getClickCount() == 2) {
                // launch editor
                log.getLogger().info("RoomEditor launched");
            }
        });

        // Style
        this.setWidth(100);
        this.setHeight(100);
        this.setFill(Color.WHITE);
        this.setStroke(Color.BLACK);

        selected.addListener((observable, oldValue, newValue) -> {
            System.out.println("Selected = " + newValue);
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
}
