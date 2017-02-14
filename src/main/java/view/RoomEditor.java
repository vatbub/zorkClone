package view;

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
