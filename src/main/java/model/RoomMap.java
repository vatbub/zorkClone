package model;

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


import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A map of rooms designed to save adjacent rooms of a {@link Room}
 */
@SuppressWarnings("ALL")
public class RoomMap extends ConcurrentHashMap<WalkDirection, Room> implements Serializable {
    private transient List<ChangeListener> changeListenerList;

    @SuppressWarnings("unused")
    public RoomMap() {
        super();
    }

    @SuppressWarnings("unused")
    public RoomMap(int initialCapacity) {
        super(initialCapacity);
    }

    @SuppressWarnings("unused")
    public RoomMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    @SuppressWarnings("unused")
    public RoomMap(int initialCapacity, float loadFactor, int concurrencyLevel) {
        super(initialCapacity, loadFactor, concurrencyLevel);
    }

    @SuppressWarnings("unused")
    public RoomMap(Map<WalkDirection, Room> m) {
        super(m);
    }

    @Override
    public Room put(@NotNull WalkDirection key, @NotNull Room value) {
        for (ChangeListener changeListener : this.getChangeListenerList()) {
            changeListener.added(key, value);
        }
        if (this.contains(value)) {
            throw new IllegalArgumentException("Duplicate adjacent room: " + value.toString());
        }
        return super.put(key, value);
    }

    @Override
    public Room remove(@NotNull Object key) {
        for (ChangeListener changeListener : this.getChangeListenerList()) {
            changeListener.removed((WalkDirection) key, this.get(key));
        }
        return super.remove(key);
    }

    @Override
    public boolean remove(Object key, Object value) {
        for (ChangeListener changeListener : this.getChangeListenerList()) {
            changeListener.removed((WalkDirection) key, (Room) value);
        }
        return super.remove(key, value);
    }

    @Override
    public boolean replace(@NotNull WalkDirection key, @NotNull Room oldValue, @NotNull Room newValue) {
        for (ChangeListener changeListener : this.getChangeListenerList()) {
            changeListener.replaced(key, oldValue, newValue);
        }
        return super.replace(key, oldValue, newValue);
    }

    @Override
    public Room replace(@NotNull WalkDirection key, @NotNull Room value) {
        for (ChangeListener changeListener : this.getChangeListenerList()) {
            changeListener.replaced(key, this.get(key), value);
        }
        return super.replace(key, value);
    }

    public List<ChangeListener> getChangeListenerList() {
        if (changeListenerList == null) {
            changeListenerList = new ArrayList<>();
        }
        return changeListenerList;
    }

    /**
     * Returns the direction that the specified room is in or {@code null} if the specified room is not connected to this room
     *
     * @param room The room to get the direction to
     * @return The direction that the specified room is in or {@code null} if the specified room is not connected to this room
     */
    public WalkDirection getKeyForObject(Room room) {
        for (Entry<WalkDirection, Room> entry : this.entrySet()) {
            if (entry.getValue() == room) {
                return entry.getKey();
            }
        }

        return null;
    }

    @SuppressWarnings("unused")
    public interface ChangeListener {
        @SuppressWarnings("unused")
        void removed(@SuppressWarnings("unused") WalkDirection key, Room value);

        @SuppressWarnings("unused")
        void added(@SuppressWarnings("unused") WalkDirection key, Room value);

        @SuppressWarnings("unused")
        void replaced(@SuppressWarnings("unused") WalkDirection key, @SuppressWarnings("unused") Room oldValue, Room newValue);
    }
}
