package model;

import parser.Noun;

import java.util.List;

/**
 * Created by Frederik on 27/11/2016.
 */
public class Item {
    private Noun name;
    private String description;

    private List<Action> actions;

    public Noun getName() {
        return name;
    }

    public void setName(Noun name) {
        this.name = name;
    }

    public List<Action> getActions() {
        return actions;
    }

    public void setActions(List<Action> actions) {
        this.actions = actions;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
