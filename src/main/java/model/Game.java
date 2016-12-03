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
        this(new Room());
    }

    public Game(Room currentRoom){
        this(currentRoom, new Player());
    }

    public Game(Room currentRoom, Player player){
        this(currentRoom, player, 0);
    }

    public Game(Room currentRoom, Player player, int score){
        this(currentRoom, player, score, 0);
    }

    public Game(Room currentRoom, Player player, int score, int moveCount){
        this(currentRoom, player, score, moveCount, new ArrayList<>());
    }

    public Game(Room currentRoom, Player player, int score, int moveCount, List<GameMessage> messages){
        this.setMessages(messages);
        this.setMoveCount(moveCount);
        this.setScore(score);
        this.setPlayer(player);
        this.setCurrentRoom(currentRoom);
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
