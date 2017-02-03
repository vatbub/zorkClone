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


import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents a room in the {@link Game}
 */
@SuppressWarnings("unused")
public class Room implements Serializable {
    private String description;
    private String name;
    private transient Runnable nameChangeListener;
    private boolean detailsTold;
    private ItemList itemsInRoom;
    private EntityList entitiesInRoom;
    private RoomMap adjacentRooms;
    private transient BooleanProperty isCurrentRoom;
    /**
     * {@code true} if this game was modified since the last save, {@code false} otherwise
     */
    private transient BooleanProperty modified;
    /**
     * Used for {@link #isConnectedTo(Room)}
     */
    private transient List<Room> visitedRooms;

    public Room() {
        this("");
    }

    public Room(@SuppressWarnings("SameParameterValue") String name) {
        this(name, "");
    }

    public Room(String name, @SuppressWarnings("SameParameterValue") String description) {
        this(name, description, new ItemList());
    }

    public Room(String name, String description, ItemList itemsInRoom) {
        this(name, description, itemsInRoom, new EntityList());
    }

    public Room(String name, String description, ItemList itemsInRoom, EntityList entitiesInRoom) {
        this(name, description, itemsInRoom, entitiesInRoom, new RoomMap(WalkDirection.values().length));
    }

    public Room(String name, String description, ItemList itemsInRoom, EntityList entitiesInRoom, RoomMap adjacentRooms) {
        this.setName(name);
        this.setDescription(description);
        this.setItemsInRoom(itemsInRoom);
        this.setEntitiesInRoom(entitiesInRoom);
        this.setAdjacentRooms(adjacentRooms);
    }

    public String getPrintableDescription() {
        String res = getPrintableDescription(!detailsTold);
        detailsTold = true;
        return res;
    }

    public String getVerbosePrintableDescription() {
        return getPrintableDescription(true);
    }

    public String getPrintableDescription(boolean verbose) {
        String res = getName() + "\n";

        if (verbose) {
            res = res + this.getDescription() + "\n";

            for (Item item : itemsInRoom) {
                res = res + item.getDescription() + "\n";
            }

            for (Entity entity : entitiesInRoom) {
                res = res + entity.getDescription() + "\n";
            }
        }

        return res;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        setModified(true);
        if (this.getNameChangeListener() != null) {
            this.getNameChangeListener().run();
        }
    }

    /*public StringProperty nameProperty() {
        return name;
    }*/

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        setModified(true);
        this.description = description;
    }

    public boolean isDetailsTold() {
        return detailsTold;
    }

    public void setDetailsTold(boolean detailsTold) {
        setModified(true);
        this.detailsTold = detailsTold;
    }

    public ItemList getItemsInRoom() {
        return itemsInRoom;
    }

    public void setItemsInRoom(ItemList itemsInRoom) {
        setModified(true);
        this.itemsInRoom = itemsInRoom;
        this.itemsInRoom.getChangeListenerList().add(new ItemList.ChangeListener() {
            @Override
            public void removed(Item item) {
                setModified(true);
            }

            @Override
            public void added(int index, Item item) {
                setModified(true);
            }

            @Override
            public void replaced(int index, Item oldValue, Item newValue) {
                setModified(true);
            }
        });
    }

    public EntityList getEntitiesInRoom() {
        return entitiesInRoom;
    }

    public void setEntitiesInRoom(EntityList entitiesInRoom) {
        this.entitiesInRoom = entitiesInRoom;
        this.entitiesInRoom.getChangeListenerList().add(new EntityList.ChangeListener() {
            @Override
            public void removed(Entity item) {
                setModified(true);
            }

            @Override
            public void added(int index, Entity item) {
                setModified(true);
            }

            @Override
            public void replaced(int index, Entity oldValue, Entity newValue) {
                setModified(true);
            }
        });
    }

    public RoomMap getAdjacentRooms() {
        return adjacentRooms;
    }

    public void setAdjacentRooms(RoomMap adjacentRooms) {
        this.adjacentRooms = adjacentRooms;

        this.adjacentRooms.getChangeListenerList().add(new RoomMap.ChangeListener() {
            @Override
            public void removed(WalkDirection key, Room value) {
                setModified(true);
            }

            @Override
            public void added(WalkDirection key, Room value) {
                setModified(true);
            }

            @Override
            public void replaced(WalkDirection key, Room oldValue, Room newValue) {
                setModified(true);
            }
        });
    }

    /**
     * Checks if this game was modified since the last save.
     *
     * @return {@code true} if this game was modified since the last save, {@code false} otherwise
     */
    public boolean isModified() {
        if (this.modified == null) {
            modified = new SimpleBooleanProperty();
        }

        return modified.getValue();
    }

    public void setModified(boolean modified) {
        if (this.modified == null) {
            this.modified = new SimpleBooleanProperty();
        }

        this.modified.set(modified);
    }

    public BooleanProperty modifiedProperty() {
        if (this.modified == null) {
            modified = new SimpleBooleanProperty();
        }

        return modified;
    }

    /**
     * Checks if the specified room is connected to {@code this} room.
     * This is done by exploring the room graph using the so called
     * <a href="https://en.wikipedia.org/wiki/Depth-first_search">Depth first search</a>
     * and checking if the specified room can be found like that.
     *
     * @param room The room to check connectivity from
     * @return {@code true} if {@code this} and the specified room are connected to each other using paths and {@code false} if not
     */
    public boolean isConnectedTo(Room room) {
        visitedRooms = new ArrayList<>();

        boolean res = isConnectedTo(this, room);

        // release resources
        visitedRooms = null;
        return res;
    }

    /**
     * <b>DON'T call this method directly! It is private for important reasons. Please call {@link #isConnectedTo(Room)} instead!</b><br>
     * Anyway, this method checks if {@code room1} and {@code room2} are connected. See {@link #isConnectedTo(Room)} for further information.
     *
     * @param room1 The first room
     * @param room2 The second room
     * @return {@code true} if {@code room1} and {@code room2} are connected to each other using paths and {@code false} if not
     */
    private boolean isConnectedTo(Room room1, Room room2) {
        if (room1 == room2) {
            return true;
        }

        // Check all adjacent rooms
        visitedRooms.add(room1);

        for (Map.Entry<WalkDirection, Room> entry : room1.getAdjacentRooms().entrySet()) {
            if (!visitedRooms.contains(entry.getValue())) {
                boolean tempRes = isConnectedTo(entry.getValue(), room2);
                if (tempRes) {
                    return true;
                }
            }
        }

        // we've searched through all children and no one is connected so we aren't connected too
        return false;
    }

    /**
     * Checks if this and the specified room are direct neighbours
     *
     * @param room The room to be checked
     * @return {@code true} if this and the specified room are direct neighbours, {@code false} otherwise
     */
    public boolean isDirectlyConnectedTo(Room room) {
        return getDirectionTo(room) != null;
    }

    /**
     * Returns the direction that needs to be taken to get from {@code this} room to the specified room
     *
     * @param room The room to get the direction for
     * @return The direction to take to get from {@code this} to {@code room} or {@code null} if {@code this} and {@code room} are not directly connected
     * @see #isDirectlyConnectedTo(Room)
     */
    public WalkDirection getDirectionTo(Room room) {
        for (Map.Entry entry : this.getAdjacentRooms().entrySet()) {
            if (entry.getValue() == room) {
                return (WalkDirection) entry.getKey();
            }
        }

        return null;
    }

    void setIsCurrentRoom(boolean isCurrentRoom) {
        if (this.isCurrentRoom == null) {
            // initialize the variable
            this.isCurrentRoom = new SimpleBooleanProperty();
        }

        this.isCurrentRoom.set(isCurrentRoom);
    }

    public boolean isCurrentRoom() {
        if (this.isCurrentRoom == null) {
            // initialize the variable
            this.isCurrentRoom = new SimpleBooleanProperty();
        }

        return isCurrentRoom.get();
    }

    public BooleanProperty isCurrentRoomProperty() {
        if (this.isCurrentRoom == null) {
            // initialize the variable
            this.isCurrentRoom = new SimpleBooleanProperty();
        }

        return isCurrentRoom;
    }

    public Runnable getNameChangeListener() {
        return nameChangeListener;
    }

    public void setNameChangeListener(Runnable nameChangeListener) {
        this.nameChangeListener = nameChangeListener;
    }
}
