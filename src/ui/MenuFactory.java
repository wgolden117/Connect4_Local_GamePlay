package ui;

import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

public class MenuFactory {
    private final Runnable onExit;
    private final Stage primaryStage;
    private final PlayerSettings playerSettings;

    public MenuFactory(Runnable onExit, Stage primaryStage, PlayerSettings playerSettings) {
        this.onExit = onExit;
        this.primaryStage = primaryStage;
        this.playerSettings = playerSettings;
    }

    public MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();

        // File Menu
        Menu fileMenu = new Menu("File");
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setOnAction(e -> onExit.run());
        fileMenu.getItems().add(exitItem);

        // Settings Menu
        Menu settingsMenu = new Menu("Settings");
        MenuItem colorItem = new MenuItem("Change Player Colors");
        colorItem.setOnAction(e -> {
            PlayerSettingsDialog dialog = new PlayerSettingsDialog(playerSettings);
            dialog.show();
        });

        settingsMenu.getItems().add(colorItem);

        // Help Menu
        Menu helpMenu = new Menu("Help");
        MenuItem howToPlay = new MenuItem("How to Play");
        howToPlay.setOnAction(e -> showHowToPlay());
        helpMenu.getItems().add(howToPlay);

        menuBar.getMenus().addAll(fileMenu, settingsMenu, helpMenu);
        return menuBar;
    }

    private void showHowToPlay() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.initOwner(primaryStage);
        alert.setTitle("How to Play");
        alert.setHeaderText("Connect 4 Rules");
        alert.setContentText(
                "• Players take turns dropping pieces into columns.\n" +
                        "• First to connect four in a row wins (horizontal, vertical, or diagonal).\n" +
                        "• If the board fills up with no winner, it's a draw."
        );
        alert.showAndWait();
    }

    private void showColorPickerDialog() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.initOwner(primaryStage);
        alert.setTitle("Coming Soon");
        alert.setHeaderText(null);
        alert.setContentText("Feature coming soon: customize player colors!");
        alert.showAndWait();
    }
}
