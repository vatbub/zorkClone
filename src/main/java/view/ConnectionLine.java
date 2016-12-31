package view;

import javafx.scene.shape.Line;
import model.WalkDirection;

import java.util.Map;

/**
 * A line that connects two rooms
 */
public class ConnectionLine extends Line {
    private RoomRectangle startRoom;
    private RoomRectangle endRoom;
    private InvalidationRunnable invalidationRunnable;

    public ConnectionLine() {
        this(null, null);
    }

    public ConnectionLine(RoomRectangle startRoom, RoomRectangle endRoom) {
        setStartRoom(startRoom);
        setEndRoom(endRoom);
        updateLocation();
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
     * Updates the location of this line
     */
    public void updateLocation() {
        if (getStartRoom() != null && getEndRoom() != null) {
            if (!getStartRoom().getRoom().isDirectlyConnectedTo(getEndRoom().getRoom())) {
                // rooms not connected, detach this line
                // TODO dispose object
                if (invalidationRunnable != null) {
                    invalidationRunnable.run(this);
                }
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

    public interface InvalidationRunnable{
        void run(ConnectionLine lineToDispose);
    }
}
