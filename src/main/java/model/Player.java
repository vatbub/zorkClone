package model;

import java.util.List;

/**
 * Created by Frederik on 27/11/2016.
 */
public class Player extends Entity {
    private List<Item> inventory;

    public List<Item> getInventory() {
        return this.inventory;
    }

    public void setInventory(List<Item> inventory) {
        this.inventory = inventory;
    }
}
