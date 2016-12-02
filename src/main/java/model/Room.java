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


import java.io.Serializable;
import java.util.List;

/**
 * Represents a room in the {@link Game}
 */
public class Room implements Serializable {
    private String description;
    private String name;
    private boolean detailsTold;
    private List<Item> itemsInRoom;
    private List<Entity> entitiesInRoom;

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
    }

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
}
