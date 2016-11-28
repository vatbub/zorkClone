package parser;

import java.util.List;

/**
 * Created by Frederik on 28/11/2016.
 */
public class Noun extends Word{
    public Noun() {
        super();
    }

    public Noun(String word) {
        super(word);
    }

    public Noun(String word, List<String> synonyms) {
        super(word, synonyms);
    }
}
