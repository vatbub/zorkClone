package model;

import view.GameMessage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the game as a whole, all the rooms, items and the current {@link Player}.
 */
public class Game implements Serializable{
    private Room currentRoom;
    private Player player;
    private int score;
    private int moveCount;
    private static List<GameMessage> messages;

    public Game(){
        this.setMessages(new ArrayList<>());
        this.setMoveCount(0);
        this.setScore(0);
        this.setPlayer(new Player());
    }

    public int getMoveCount() {
        return moveCount;
    }

    public int getScore() {
        return score;
    }

    public Player getPlayer() {
        return player;
    }

    public Room getCurrentRoom() {
        return currentRoom;
    }

    public static List<GameMessage> getMessages() {
        return messages;
    }

    public void setCurrentRoom(Room currentRoom) {
        this.currentRoom = currentRoom;
    }

    public void setMoveCount(int moveCount) {
        this.moveCount = moveCount;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public static void setMessages(List<GameMessage> messages) {
        Game.messages = messages;
    }
}
