package shan.gui;

import java.io.IOException;
import java.util.Collections;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

/**
 * Displays a message together with an image representing its sender.
 */
public class DialogBox extends HBox {
    @FXML
    private Label dialog;
    @FXML
    private ImageView displayPicture;

    private DialogBox(String text, Image image) {
        FXMLLoader fxmlLoader = new FXMLLoader(
                DialogBox.class.getResource("/view/DialogBox.fxml"));
        fxmlLoader.setController(this);
        fxmlLoader.setRoot(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load DialogBox.fxml.", exception);
        }

        this.dialog.setText(text);
        this.displayPicture.setImage(image);
    }

    /**
     * Creates a dialog with the user's image on the right.
     *
     * @param text  Message to display.
     * @param image Image representing the user.
     * @return Dialog configured for a user message.
     */
    public static DialogBox getUserDialog(String text, Image image) {
        return new DialogBox(text, image);
    }

    /**
     * Creates a dialog with Shan's image on the left.
     *
     * @param text  Message to display.
     * @param image Image representing Shan.
     * @return Dialog configured for a Shan message.
     */
    public static DialogBox getShanDialog(String text, Image image) {
        DialogBox dialogBox = new DialogBox(text, image);
        dialogBox.flip();
        return dialogBox;
    }

    /**
     * Places the sender image on the left of the message.
     */
    private void flip() {
        ObservableList<Node> dialogElements = FXCollections.observableArrayList(
                this.getChildren());
        Collections.reverse(dialogElements);
        this.getChildren().setAll(dialogElements);
        this.setAlignment(Pos.TOP_LEFT);
    }
}
