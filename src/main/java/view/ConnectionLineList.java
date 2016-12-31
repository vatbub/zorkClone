package view;

import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A list of {@link ConnectionLine}s that can find a line by its start and and room
 */
public class ConnectionLineList extends CopyOnWriteArrayList<ConnectionLine> {
    public ConnectionLineList() {
        super();
    }

    @SuppressWarnings({"unused"})
    public ConnectionLineList(Collection<? extends ConnectionLine> c) {
        super(c);
    }

    /**
     * Searches for the {@link ConnectionLine} that connects the specified rooms.
     *
     * @param startRoom The start room of the line to look for
     * @param endRoom   The end room of the line to look for
     * @return The the {@link ConnectionLine} that connects the specified rooms or {@code null} if no such line was not found.
     */
    @Nullable
    public ConnectionLine findByStartAndEndRoom(RoomRectangle startRoom, RoomRectangle endRoom) {
        for (ConnectionLine line : this) {
            if (line.getStartRoom() == startRoom && line.getEndRoom() == endRoom) {
                return line;
            }
        }

        // nothing found
        return null;
    }
}
