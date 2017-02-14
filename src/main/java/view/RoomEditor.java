package view;

/*-
 * #%L
 * Zork Clone
 * %%
 * Copyright (C) 2016 - 2017 Frederik Kammel
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


import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.Room;
import view.reporting.ReportingDialog;

import java.io.IOException;
import java.util.ResourceBundle;

/**
 * Controller for the RoomEditor.
 */
public class RoomEditor {
    public static ResourceBundle bundle;
    private static Room room;
    private static Stage stage;
    @FXML
    private TextField roomName;

    /**
     * Only for the JavaFX Loader
     */
    @Deprecated
    public RoomEditor() {
    }

    public RoomEditor(Room room) {
        setRoom(room);
    }

    public void show() {
        bundle = ResourceBundle.getBundle("view.strings");
        Parent root;
        stage = new Stage();
        try {
            root = FXMLLoader.load(ReportingDialog.class.getResource("/view/RoomEditor.fxml"), bundle);
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("EditorMain.css").toExternalForm());

            stage.setMinWidth(scene.getRoot().minWidth(0) + 70);
            stage.setMinHeight(scene.getRoot().minHeight(0) + 70);

            stage.setScene(scene);
            stage.setTitle(getRoom().getName());
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void initialize() {
        roomName.setText(getRoom().getName());
        roomName.textProperty().addListener((observable, oldValue, newValue) -> {
            stage.setTitle(newValue);
            getRoom().setName(newValue);
        });
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        RoomEditor.room = room;
    }
}
