package model;

/**
 * Created by Frederik on 27/11/2016.
 */
public abstract class Action {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    abstract void execute();
}
