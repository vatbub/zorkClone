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


import model.Room;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A List of {@link RoomRectangle}s that can find a {@link RoomRectangle} by its {@link Room}
 */
public class RoomRectangleList extends CopyOnWriteArrayList<RoomRectangle> {
    public RoomRectangleList() {
        super();
    }

    @SuppressWarnings({"unused"})
    public RoomRectangleList(Collection<? extends RoomRectangle> c) {
        super(c);
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

    /**
     * Finds the {@link RoomRectangle} from {@code this} list that has the least pythagorean distance to the specified source
     *
     * @param source The {@link RoomRectangle} to measure the distance from
     * @return The {@link RoomRectangle} from {@code this} list that has the least pythagorean distance to the specified source or {@code null} if this list does not contain any room
     */
    @Nullable
    public RoomRectangle findRoomWithMinimumDistanceTo(RoomRectangle source) {
        if (this.isEmpty()) {
            return null;
        } else {
            RoomRectangle res = this.get(0);

            for (RoomRectangle room : this) {
                if (source.distanceTo(room) < source.distanceTo(res)) {
                    // found a room with a smaller distance
                    res = room;
                }
            }

            return res;
        }
    }

    @Override
    public boolean add(RoomRectangle roomRectangle){
        if (this.contains(roomRectangle)){
            throw new IllegalArgumentException("Duplicate child: " + roomRectangle.toString());
        }
        return super.add(roomRectangle);
    }
}
