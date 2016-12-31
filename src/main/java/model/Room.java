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
public class Room implements Serializable {
    private String description;
    private String name;
    private transient Runnable nameChangeListener;
    private boolean detailsTold;
    private List<Item> itemsInRoom;
    private List<Entity> entitiesInRoom;
    private RoomMap adjacentRooms;
    private transient BooleanProperty isCurrentRoom;
    /**
     * Used for {@link #isConnectedTo(Room)}
     */
    private List<Room> visitedRooms;

    public Room() {
        this("");
    }

    public Room(String name) {
        this(name, "");
    }

    public Room(String name, String description) {
        this(name, description, new ArrayList<>());
    }

    public Room(String name, String description, List<Item> itemsInRoom) {
        this(name, description, itemsInRoom, new ArrayList<>());
    }

    public Room(String name, String description, List<Item> itemsInRoom, List<Entity> entitiesInRoom) {
        this(name, description, itemsInRoom, entitiesInRoom, new RoomMap(WalkDirection.values().length));
    }

    public Room(String name, String description, List<Item> itemsInRoom, List<Entity> entitiesInRoom, RoomMap adjacentRooms) {
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
        this.name=name;
        if (this.getNameChangeListener()!=null){
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
        this.description = description;
    }

    public boolean isDetailsTold() {
        return detailsTold;
    }

    public void setDetailsTold(boolean detailsTold) {
        this.detailsTold = detailsTold;
    }

    public List<Item> getItemsInRoom() {
        return itemsInRoom;
    }

    public void setItemsInRoom(List<Item> itemsInRoom) {
        this.itemsInRoom = itemsInRoom;
    }

    public List<Entity> getEntitiesInRoom() {
        return entitiesInRoom;
    }

    public void setEntitiesInRoom(List<Entity> entitiesInRoom) {
        this.entitiesInRoom = entitiesInRoom;
    }

    public RoomMap getAdjacentRooms() {
        return adjacentRooms;
    }

    public void setAdjacentRooms(RoomMap adjacentRooms) {
        this.adjacentRooms = adjacentRooms;
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
        visitedRooms = new ArrayList();

        boolean res = isConnectedTo(this,room);

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
     * @param room The room to be checked
     * @return {@code true} if this and the specified room are direct neighbours, {@code false} otherwise
     */
    public boolean isDirectlyConnectedTo(Room room){
        for(Map.Entry entry:this.getAdjacentRooms().entrySet()){
            if (entry.getValue()==room){
                return true;
            }
        }

        return false;
    }

    void setIsCurrentRoom(boolean isCurrentRoom) {
        if (this.isCurrentRoom==null){
            // initialize the variable
            this.isCurrentRoom = new SimpleBooleanProperty();
        }

        this.isCurrentRoom.set(isCurrentRoom);
    }

    public boolean isCurrentRoom() {
        if (this.isCurrentRoom==null){
            // initialize the variable
            this.isCurrentRoom = new SimpleBooleanProperty();
        }

        return isCurrentRoom.get();
    }

    public BooleanProperty isCurrentRoomProperty() {
        if (this.isCurrentRoom==null){
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
