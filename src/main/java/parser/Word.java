package parser;

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
import java.util.ArrayList;
import java.util.List;

/**
 * Any word that can be used by the {@link model.Player} to interact with the game.
 */
public class Word implements Serializable {
    private final Word thisWordInstance;
    private String word;
    private List<String> synonyms;
    private List<? extends Word> permittedWordClassesThatFollow;
    private final List<Word> permittedWordsThatFollow = new ArrayList<Word>() {
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

    @SuppressWarnings({"unused"})
    public void setWord(String word) {
        this.word = word;
    }

    public List<String> getSynonyms() {
        return synonyms;
    }

    @SuppressWarnings({"unused"})
    public void setSynonyms(List<String> synonyms) {
        this.synonyms = synonyms;
    }

    public List<? extends Word> getPermittedWordClassesThatFollow() {
        return permittedWordClassesThatFollow;
    }

    @SuppressWarnings({"unused"})
    public void setPermittedWordClassesThatFollow(List<? extends Word> permittedWordClassesThatFollow) {
        this.permittedWordClassesThatFollow = permittedWordClassesThatFollow;
    }

    public List<Word> getPermittedWordsThatFollow() {
        return permittedWordsThatFollow;
    }

    public boolean isWordPermittedAsFollowingWord(Word word) {
        //noinspection SuspiciousMethodCalls
        return this.getPermittedWordClassesThatFollow().contains(word.getClass()) && this.getPermittedWordsThatFollow().contains(word);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Word) {
            // obj is a word
            Word word = (Word) obj;

            if (!word.getWord().equals(this.getWord())) {
                return false;
            }
            if (!word.getSynonyms().equals(this.getSynonyms())) {
                return false;
            }
            if (!word.getPermittedWordClassesThatFollow().equals(this.getPermittedWordClassesThatFollow())) {
                return false;
            }
            if (!word.getPermittedWordsThatFollow().equals(this.getPermittedWordsThatFollow())) {
                return false;
            }

            // everything ok
            return true;

        } else {
            // Not the same class
            return false;
        }
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "@" + this.getWord();
    }

    /**
     * Checks if {@code input} is equal to {@code this.}{@link #getWord()} or to one of the synonyms
     *
     * @param input The string to compare
     * @return {@code true} if {@code input} is equal to {@code this.}{@link #getWord()} or to one of the synonyms, {@code false} otherwise.
     */
    @SuppressWarnings({"unused"})
    public boolean equals(String input) {
        return (this.getWord().equals(input) || this.getSynonyms().contains(input));
    }
}
