package ui;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class PlayerSettingsDialog {

    private final PlayerSettings playerSettings;

    public PlayerSettingsDialog(PlayerSettings playerSettings) {
        this.playerSettings = playerSettings;
    }

    public void show() {
        Stage dialog = new Stage();
        dialog.setTitle("Change Player Colors");
        dialog.initModality(Modality.APPLICATION_MODAL);

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setVgap(15);
        grid.setHgap(15);

        // Labels and ColorPickers
        Label player1Label = new Label("Player 1 Color:");
        ColorPicker player1ColorPicker = new ColorPicker(playerSettings.getPlayerOneColor());

        Label player2Label = new Label("Player 2 Color:");
        ColorPicker player2ColorPicker = new ColorPicker(playerSettings.getPlayerTwoColor());

        grid.add(player1Label, 0, 0);
        grid.add(player1ColorPicker, 1, 0);
        grid.add(player2Label, 0, 1);
        grid.add(player2ColorPicker, 1, 1);

        // Save Button
        Button saveButton = new Button("Save");
        saveButton.setOnAction(e -> {
            playerSettings.setPlayerOneColor(player1ColorPicker.getValue());
            playerSettings.setPlayerTwoColor(player2ColorPicker.getValue());
            dialog.close();
        });

        grid.add(saveButton, 1, 2);

        Scene scene = new Scene(grid);
        dialog.setScene(scene);
        dialog.showAndWait();
    }
}
