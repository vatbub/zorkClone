package model;

import java.util.List;

/**
 * Created by Frederik on 27/11/2016.
 */
public class Room {
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
