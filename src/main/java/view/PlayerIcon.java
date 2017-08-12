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


import com.github.vatbub.common.core.logging.FOKLogger;
import javafx.scene.Cursor;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * A image view that has events attached specifically required in the GameEditor
 */
public class PlayerIcon extends ImageView {
    private boolean dragStarted;
    private double moveStartLocalX;
    private double moveStartLocalY;
    private RoomRectangle previousTarget;
    private RoomRectangle parent;
    private PlayerIcon iconCopy;

    @SuppressWarnings("unused")
    public PlayerIcon() {
        this(null);
    }

    public PlayerIcon(@SuppressWarnings("SameParameterValue") RoomRectangle parent) {
        this((Image) null, parent);
    }

    @SuppressWarnings("unused")
    public PlayerIcon(String url, RoomRectangle parent) {
        this(new Image(url), parent);
    }

    public PlayerIcon(Image image, RoomRectangle parent) {
        super(image);
        setCustomParent(parent);
        this.setCursor(Cursor.MOVE);

        // initialize the events
        this.setOnDragDetected(event -> {
            dragStarted = true;

            // ensure the icon is drawn on top
            this.toFront();
        });

        this.setOnMousePressed(event -> {
            this.moveStartLocalX = event.getX() - this.getX();
            this.moveStartLocalY = event.getY() - this.getY();

            // draw copy of icon on its original spot
            iconCopy = new PlayerIcon(this.getImage(), this.getCustomParent());
            this.getCustomParent().getCustomParent().getChildren().add(iconCopy);
            iconCopy.setX(this.getX());
            iconCopy.setY(this.getY());
            iconCopy.setVisible(true);
            iconCopy.setOpacity(0.3);

            event.consume();
        });

        this.setOnMouseDragged(event -> {
            // move the image
            FOKLogger.fine(PlayerIcon.class.getName(), "Moving the player icon...");
            this.setX(event.getX() - this.moveStartLocalX);
            this.setY(event.getY() - this.moveStartLocalY);

            RoomRectangle newTarget = (RoomRectangle) this.getCustomParent().getCustomParent().getRectangleByCoordinatesPreferFront(event.getX(), event.getY());
            if (newTarget != previousTarget && previousTarget != null) {
                previousTarget.setSelected(false);
            }
            if (newTarget != null && newTarget != this.getCustomParent()) {
                newTarget.setSelected(true);
            }

            previousTarget = newTarget;

            // draw the iconCopy in the new target

            if (newTarget == null) {
                // just to draw the iconCopy
                newTarget = this.getCustomParent();
            }

            // Get the center of the parent rectangle
            double centerX = newTarget.getX() + newTarget.getWidth() / 2.0;
            double centerY = newTarget.getY() + newTarget.getHeight() / 2.0;

            // calculate the upper left corner of the player icon
            iconCopy.setX(centerX - iconCopy.getImage().getWidth() / 2.0);
            iconCopy.setY(centerY + (newTarget.getNameLabel().getHeight() / 2.0) + 15 - iconCopy.getImage().getHeight() / 2.0);
        });
        this.setOnMouseReleased(event -> {
            if (dragStarted) {
                dragStarted = false;

                FOKLogger.fine(PlayerIcon.class.getName(), "Drag done");
                RoomRectangle target = (RoomRectangle) this.getCustomParent().getCustomParent().getRectangleByCoordinatesPreferFront(event.getX(), event.getY());
                if (target != null && target != this.getCustomParent()) {
                    target.setSelected(false);
                    EditorView.currentEditorInstance.getCurrentGame().setCurrentRoom(target.getRoom());
                }

                EditorView.currentEditorInstance.renderView(false);
            }
        });
    }

    public RoomRectangle getCustomParent() {
        return parent;
    }

    public void setCustomParent(RoomRectangle parent) {
        this.parent = parent;
    }
}
