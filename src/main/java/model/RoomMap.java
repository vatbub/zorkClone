package model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A map of rooms designed to save adjacent rooms of a {@link Room}
 */
public class RoomMap extends ConcurrentHashMap<WalkDirection, Room> {
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
    public RoomMap(Map<WalkDirection, Room> m){
        super(m);
    }

    @Override
    public Room put(WalkDirection key, Room value) {
        if (this.contains(value)) {
            throw new IllegalArgumentException("Duplicate adjacent room: " + value.toString());
        }
        return super.put(key, value);
    }
}
