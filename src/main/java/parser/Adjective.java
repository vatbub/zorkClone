package parser;

import java.util.List;

/**
 * Created by Frederik on 28/11/2016.
 */
public class Adjective extends Word {
    public Adjective() {
        super();
    }

    public Adjective(String word) {
        super(word);
    }

    public Adjective(String word, List<String> synonyms) {
        super(word, synonyms);
    }
}
