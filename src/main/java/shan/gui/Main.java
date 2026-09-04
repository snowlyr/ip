package shan.gui;

import java.io.IOException;
import java.nio.file.Path;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import shan.Shan;

/**
 * Provides the JavaFX graphical interface for Shan using FXML.
 */
public class Main extends Application {
    private final Shan shan = new Shan(Path.of("data", "shan.txt"));

    /**
     * Constructs the JavaFX application.
     */
    public Main() {
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/view/MainWindow.fxml"));
        AnchorPane mainLayout = fxmlLoader.load();
        fxmlLoader.<MainWindow>getController().setShan(this.shan);

        stage.setTitle("Shan");
        stage.setResizable(false);
        stage.setMinHeight(600.0);
        stage.setMinWidth(400.0);
        stage.setScene(new Scene(mainLayout));
        stage.show();
    }
}
