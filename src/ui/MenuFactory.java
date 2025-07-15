package ui;

import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import logic.GameController;

public class MenuFactory {
    private final Runnable onExit;
    private final Stage primaryStage;
    private final PlayerSettings playerSettings;
    private final GameController controller;
    private final boolean vsComputer;

    public MenuFactory(Runnable onExit, Stage primaryStage, PlayerSettings playerSettings, GameController controller, boolean vsComputer) {
        this.onExit = onExit;
        this.primaryStage = primaryStage;
        this.playerSettings = playerSettings;
        this.controller = controller;
        this.vsComputer = vsComputer;
    }

    public MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();

        // File Menu
        Menu fileMenu = new Menu("File");

        MenuItem mainMenuItem = new MenuItem("Main Menu");
        mainMenuItem.setOnAction(e -> {
            controller.stopBackgroundMusic(); // Stop existing music
            new GUI().start(primaryStage);    // Reload initial GUI
        });

        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setOnAction(e -> onExit.run());

        fileMenu.getItems().addAll(mainMenuItem, exitItem);


        // Settings Menu
        Menu settingsMenu = new Menu("Settings");
        MenuItem colorItem = new MenuItem("Player Preferences");
        colorItem.setOnAction(e -> {
            PlayerSettingsDialog dialog = new PlayerSettingsDialog(playerSettings, vsComputer);
            dialog.show();
        });
        settingsMenu.getItems().add(colorItem);

        // Sounds submenu under Settings
        Menu soundsSubMenu = new Menu("Sounds");

        CheckMenuItem toggleMusic = new CheckMenuItem("Background Music");
        toggleMusic.setSelected(true);
        toggleMusic.setOnAction(e -> {
            if (toggleMusic.isSelected()) {
                controller.playBackgroundMusic();
            } else {
                controller.stopBackgroundMusic();
            }
        });

        CheckMenuItem toggleDropSound = new CheckMenuItem("Piece Drop Sound");
        toggleDropSound.setSelected(true);
        toggleDropSound.setOnAction(e -> controller.setDropSoundEnabled(toggleDropSound.isSelected()));

        soundsSubMenu.getItems().addAll(toggleMusic, toggleDropSound);

        // Add sounds submenu to settings menu
        settingsMenu.getItems().add(soundsSubMenu);


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
                """
                        • Players take turns dropping pieces into columns.
                        • First to connect four in a row wins (horizontal, vertical, or diagonal).
                        • If the board fills up with no winner, it's a draw."""
        );
        alert.showAndWait();
    }
}
