package parser;

import model.Action;

import java.util.List;

/**
 * Created by Frederik on 28/11/2016.
 */
public class Verb extends Word {
    Action action;

    public Verb() {
        super();
    }

    public Verb(String word) {
        super(word);
    }

    public Verb(String word, List<String> synonyms) {
        super(word, synonyms);
    }

    public Action getAction(){
        return action;
    }

    public void setAction(Action action){
        this.action = action;
    }
}
