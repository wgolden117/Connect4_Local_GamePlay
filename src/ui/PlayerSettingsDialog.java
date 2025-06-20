package ui;

import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class PlayerSettingsDialog {
    private final Stage dialogStage;
    private final PlayerSettings playerSettings;

    public PlayerSettingsDialog(PlayerSettings settings) {
        this.playerSettings = settings;
        this.dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setTitle("Player Preferences");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(15));
        grid.setHgap(10);
        grid.setVgap(10);

        Label playerOneLabel = new Label("Player 1 Color:");
        ColorPicker playerOnePicker = new ColorPicker(playerSettings.getPlayerOneColor());

        Label playerTwoLabel = new Label("Player 2 Color:");
        ColorPicker playerTwoPicker = new ColorPicker(playerSettings.getPlayerTwoColor());

        Button okButton = new Button("OK");
        Button cancelButton = new Button("Cancel");

        // Disable OK button if colors are the same (binding updates automatically)
        okButton.disableProperty().bind(
                Bindings.createBooleanBinding(
                        () -> playerOnePicker.getValue().equals(playerTwoPicker.getValue()),
                        playerOnePicker.valueProperty(), playerTwoPicker.valueProperty()
                )
        );

        okButton.setOnAction(event -> {
            playerSettings.setPlayerOneColor(playerOnePicker.getValue());
            playerSettings.setPlayerTwoColor(playerTwoPicker.getValue());
            dialogStage.close();
        });

        cancelButton.setOnAction(event -> dialogStage.close());

        grid.add(playerOneLabel, 0, 0);
        grid.add(playerOnePicker, 1, 0);
        grid.add(playerTwoLabel, 0, 1);
        grid.add(playerTwoPicker, 1, 1);
        grid.add(okButton, 0, 2);
        grid.add(cancelButton, 1, 2);

        Scene scene = new Scene(grid);
        dialogStage.setScene(scene);
    }

    public void show() {
        dialogStage.showAndWait();
    }
}
