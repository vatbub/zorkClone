package view;

import model.Room;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;

/**
 * A List of {@link RoomRectangle}s that can find a {@link RoomRectangle} by its {@link Room}
 */
public class RoomList extends ArrayList<RoomRectangle> {
    public RoomList() {
        super();
    }

    public RoomList(Collection<? extends RoomRectangle> c) {
        super(c);
    }

    public RoomList(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * Searches for the RoomRectangle that contains that particular room.
     *
     * @param room The room to find
     * @return The {@link RoomRectangle} that contains the specified rrom or {@code null} if the room was not found.
     */
    @Nullable
    public RoomRectangle findByRoom(Room room) {
        for (RoomRectangle entry : this) {
            if (entry.getRoom() == room) {
                return entry;
            }
        }

        return null;
    }
}
