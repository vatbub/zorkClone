package parser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Frederik on 27/11/2016.
 */
public class Word {
    private String word;
    private List<String> synonyms;
    private Word thisWordInstance;
    private List<? extends Word> permittedWordClassesThatFollow;
    private List<Word> permittedWordsThatFollow = new ArrayList<Word>() {
        @Override
        public void add(int index, Word word) {
            if (!thisWordInstance.isWordPermittedAsFollowingWord(word))
                throw new IllegalArgumentException("The word " + word.toString() + "is not permitted as a following word of " + thisWordInstance.toString());
            else
                super.add(index, word);
        }
    };

    public Word() {
        this(null);
    }

    public Word(String word) {
        this(word, null);
    }

    public Word(String word, List<String> synonyms) {
        this.word = word;
        this.synonyms = synonyms;
        thisWordInstance = this;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public List<String> getSynonyms() {
        return synonyms;
    }

    public void setSynonyms(List<String> synonyms) {
        this.synonyms = synonyms;
    }

    public List<? extends Word> getPermittedWordClassesThatFollow(){
        return permittedWordClassesThatFollow;
    }

    public void setPermittedWordClassesThatFollow(List<? extends Word> permittedWordClassesThatFollow){
        this.permittedWordClassesThatFollow = permittedWordClassesThatFollow;
    }

    public List<Word> getPermittedWordsThatFollow() {
        return permittedWordsThatFollow;
    }

    public boolean isWordPermittedAsFollowingWord(Word word) {
        if (!this.getPermittedWordClassesThatFollow().contains(word.getClass()))
            return false;
        if (!this.getPermittedWordsThatFollow().contains(word))
            return false;

        return true;
    }

    @Override
    public String toString(){
        return this.getClass().getSimpleName() + "@" +  this.getWord();
    }
}
