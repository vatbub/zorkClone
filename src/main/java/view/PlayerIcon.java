package view;

import javafx.scene.Cursor;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import logging.FOKLogger;

/**
 * A image view that has events attached specifically required in the GameEditor
 */
public class PlayerIcon extends ImageView {
    private boolean dragStarted;
    private double moveStartLocalX;
    private double moveStartLocalY;
    private RoomRectangle previousTarget;
    private RoomRectangle parent;

    public PlayerIcon() {
        this(null);
    }

    public PlayerIcon(RoomRectangle parent) {
        this((Image) null, parent);
    }

    public PlayerIcon(String url, RoomRectangle parent) {
        this(new Image(url), parent);
    }

    public PlayerIcon(Image image, RoomRectangle parent) {
        super(image);
        setCustomParent(parent);
        this.setCursor(Cursor.MOVE);

        // initialize the events
        // ensure the icon is drawn on top
        this.setOnDragDetected(event -> this.toFront());

        this.setOnMousePressed(event -> {
            this.moveStartLocalX = event.getX() - this.getX();
            this.moveStartLocalY = event.getY() - this.getY();
            event.consume();
        });

        this.setOnMouseDragged(event -> {
            dragStarted = true;

            // move the image
            this.setOpacity(0.5);
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
