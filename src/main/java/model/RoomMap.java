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
