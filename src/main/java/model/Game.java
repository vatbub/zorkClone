package model;

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


import common.Common;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import logging.FOKLogger;
import org.jetbrains.annotations.NotNull;
import view.GameMessage;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

/**
 * Represents the game as a whole, all the rooms, items and the current {@link Player}.
 */
public class Game implements Serializable {

    /**
     * The app version that was used when the game was saved.
     */
    @SuppressWarnings({"unused"})
    public final String gameSavedWithAppVersion = Common.getAppVersion();

    private Room currentRoom;
    private Player player;
    private int score;
    private int moveCount;
    private List<GameMessage> messages;
    private transient static FOKLogger log = new FOKLogger(Game.class.getName());

    /**
     * If the game was loaded from a file, this specifies the file it was loaded from. {@code null} if the game was not loaded from a file.
     */
    private transient File fileSource;

    /**
     * {@code true} if this game was modified since the last save, {@code false} otherwise
     */
    private transient BooleanProperty modified;
    // TODO: Listen to modifications of the rooms

    public Game() {
        this(new Room());
    }

    public Game(Room currentRoom) {
        this(currentRoom, new Player());
    }

    public Game(Room currentRoom, Player player) {
        this(currentRoom, player, 0);
    }

    public Game(Room currentRoom, Player player, int score) {
        this(currentRoom, player, score, 0);
    }

    public Game(Room currentRoom, Player player, int score, int moveCount) {
        this(currentRoom, player, score, moveCount, new ArrayList<>());
    }

    public Game(Room currentRoom, Player player, int score, int moveCount, List<GameMessage> messages) {
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

    public List<GameMessage> getMessages() {
        return messages;
    }

    public String getGameSavedWithAppVersion() {
        return gameSavedWithAppVersion;
    }

    /**
     * The {@code File} the game was loaded from or {@code null} if it was not loaded from a file.
     *
     * @return The {@code File} the game was loaded from or {@code null} if it was not loaded from a file.
     */
    public File getFileSource() {
        return fileSource;
    }

    /**
     * Checks if this game was modified since the last save.
     *
     * @return {@code true} if this game was modified since the last save, {@code false} otherwise
     */
    public boolean isModified() {
        if (this.modified==null){
            modified = new SimpleBooleanProperty();
        }

        return modified.getValue();
    }
    
    private void setModified(boolean modified){
        if (this.modified==null){
            this.modified = new SimpleBooleanProperty();
        }
        
        this.modified.set(modified);
    }

    public BooleanProperty modifiedProperty() {
        if (this.modified==null){
            modified = new SimpleBooleanProperty();
        }

        return modified;
    }

    public void setCurrentRoom(Room currentRoom) {
        if (this.currentRoom != null) {
            this.currentRoom.setIsCurrentRoom(false);
        }
        this.currentRoom = currentRoom;
        this.currentRoom.setIsCurrentRoom(true);
        setModified(true);
    }

    public void setMoveCount(int moveCount) {
        this.moveCount = moveCount;
        setModified(true);
    }

    public void setPlayer(Player player) {
        this.player = player;
        setModified(true);
    }

    public void setScore(int score) {
        this.score = score;
        setModified(true);
    }

    public void setMessages(List<GameMessage> messages) {
        this.messages = messages;
        setModified(true);
    }

    public void setFileSource(File fileSource) {
        this.fileSource = fileSource;
    }

    public void save() {
        this.save("");
    }

    /**
     * Saves this game state on the hard disk so that it can be loaded later on using {@link #load(String)} or {@link #load(File)}.<br>
     * The file name is generated automatically using the following scheme:<br>
     * <ul>
     * <li>If {@code customNamePrefix} equals {@code ""} (same as calling {@link #save()}:<br>
     * {@code %appdata%/zork/saves/yyyy-MM-dd-HH-mm-ss.fokGameSave}</li>
     * <li>If {@code customNamePrefix} does not equal {@code ""}:<br>
     * {@code %appdata%/zork/saves/customNamePrefix-yyyy-MM-dd-HH-mm-ss.fokGameSave}</li>
     * </ul>
     * where {@code yyyy-MM-dd-HH-mm-ss} is the current time and day.
     *
     * @param customNamePrefix The prefix of the file name
     * @see #load(File)
     * @see #load(String)
     */
    public void save(@NotNull String customNamePrefix) {
        Objects.requireNonNull(customNamePrefix);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        Date date = new Date();
        String dateString = dateFormat.format(date);

        String fileName = Common.getAndCreateAppDataPath() + "saves" + File.separator;

        if (!customNamePrefix.equals("")) {
            // We have a name given
            fileName = fileName + customNamePrefix + "-" + dateString;
        } else {
            fileName = fileName + dateString;
        }

        fileName = fileName + ".fokGameSave";

        try {
            this.save(new File(fileName));
        } catch (IOException e) {
            log.getLogger().log(Level.SEVERE, "Something magical happened and the user tried to save two games at exactly the same time. Here's the result:", e);
        }
    }

    /**
     * Saves this game state to the specified file so that it can be loaded later on using {@link #load(String)} or {@link #load(File)}.
     *
     * @param fileToSave The file to save the game in. If the file exists already, it is overwritten.
     * @throws IOException If the specified file cannot be written for any reason.
     * @see #load(File)
     * @see #load(String)
     */
    public void save(@NotNull File fileToSave) throws IOException {
        Objects.requireNonNull(fileToSave);

        if (fileToSave.exists()) {
            // delete the file
            fileToSave.delete();
        }

        // Serialize this object at the given file
        FileOutputStream fileOut = new FileOutputStream(fileToSave);
        ObjectOutputStream objOut = new ObjectOutputStream(fileOut);
        objOut.writeObject(this);
        objOut.close();
        fileOut.close();

        this.setFileSource(fileToSave);
        this.setModified(false);
    }

    /**
     * Loads a game from the specified {@code File}.
     *
     * @param saveFileLocation The fully qualified path and name of the file to load the save from.
     * @return The game that was saved in that file
     * @throws IOException            If the specified file dows not exist or cannot be read for some other reason.
     * @throws ClassNotFoundException If the specified file does not contain a {@code Game} but anything else (wrong file format)
     * @see #save()
     * @see #save(String)
     * @see #save(File)
     * @see #gameSavedWithAppVersion
     */
    public static Game load(String saveFileLocation) throws IOException, ClassNotFoundException {
        return Game.load(new File(saveFileLocation));
    }

    /**
     * Loads a game from the specified {@code File}.
     *
     * @param saveFile The file to load the save from
     * @return The game that was saved in that file
     * @throws IOException            If the specified file dows not exist or cannot be read for some other reason.
     * @throws ClassNotFoundException If the specified file does not contain a {@code Game} but anything else (wrong file format)
     */
    public static Game load(@NotNull File saveFile) throws IOException, ClassNotFoundException {
        Objects.requireNonNull(saveFile);

        FileInputStream fileIn = new FileInputStream(saveFile);
        ObjectInputStream objIn = new ObjectInputStream(fileIn);

        Game res = (Game) objIn.readObject();
        res.setFileSource(saveFile);
        res.getCurrentRoom().setIsCurrentRoom(true);
        return res;
    }
}
