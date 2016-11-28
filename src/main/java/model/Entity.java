package model;

import parser.Word;

/**
 * Created by Frederik on 27/11/2016.
 */
public class Entity {
    private int remeaningHealth = 10;
    private Word name;
    private String description;

    public int getRemeaningHealth() {
        return remeaningHealth;
    }

    public void setRemeaningHealth(int remeaningHealth) {
        this.remeaningHealth = remeaningHealth;
    }

    public Word getName() {
        return name;
    }

    public void setName(Word name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
