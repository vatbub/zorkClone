package view;

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


/**
 * Represents a message that was sent from the player to the game or vice versa.
 */
public class GameMessage {
    private String message;
    private boolean messageFromGame;

    @SuppressWarnings("unused")
    public GameMessage() {
        this(null, false);
    }

    public GameMessage(String message, boolean isMessageFromGame) {
        this.setMessage(message);
        this.setIsMessageFromGame(isMessageFromGame);
    }

    public boolean isMessageFromGame() {
        return messageFromGame;
    }

    @SuppressWarnings("unused")
    public boolean isMessageFromPlayer() {
        return !messageFromGame;
    }

    public void setIsMessageFromGame(boolean messageFromGame) {
        this.messageFromGame = messageFromGame;
    }

    @SuppressWarnings("unused")
    public void messageIsFromGame() {
        this.setIsMessageFromGame(true);
    }

    @SuppressWarnings("unused")
    public void messageIsFromPlayer() {
        this.setIsMessageFromGame(false);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
