package ui;

import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * A dialog window that allows the user to customize player settings, including
 * player names and colors. Ensures that both players have different names and colors.
 *
 * If playing against the computer, Player 2's name is fixed to "Computer" and cannot be edited.
 *
 * @author Weronika Golden
 * @version 3.0
 */
public class PlayerSettingsDialog {
    private final Stage dialogStage;
    private final PlayerSettings playerSettings;

    /**
     * Constructs a PlayerSettingsDialog with preloaded settings.
     * This dialog intentionally stores a reference to the shared {@code PlayerSettings}
     * so that it can directly apply user changes (names and colors) to the game state.
     * This is safe under the assumption that {@code PlayerSettings} is not accessed
     * concurrently from other threads and is only updated via the UI.
     *
     * @param settings    the player settings object to update
     * @param vsComputer  true if Player 2 should be labeled and locked as "Computer"
     */
    public PlayerSettingsDialog(PlayerSettings settings, boolean vsComputer) {
        this.playerSettings = settings;
        this.dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setTitle("Player Preferences");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(15));
        grid.setHgap(10);
        grid.setVgap(10);

        // Name Inputs
        Label nameOneLabel = new Label("Player 1 Name:");
        TextField nameOneField = new TextField();
        nameOneField.setPromptText("Enter name");
        nameOneField.setText(playerSettings.getPlayerOneName());

        Label nameTwoLabel = new Label("Player 2 Name:");
        TextField nameTwoField = new TextField();
        nameTwoField.setPromptText("Enter name");
        if (vsComputer) {
            nameTwoField.setText("Computer");
            nameTwoField.setDisable(true);
        } else {
            nameTwoField.setText(playerSettings.getPlayerTwoName());
        }

        // Color Pickers
        Label playerOneColorLabel = new Label("Player 1 Color:");
        ColorPicker playerOnePicker = new ColorPicker(playerSettings.getPlayerOneColor());

        Label playerTwoColorLabel = new Label("Player 2 Color:");
        ColorPicker playerTwoPicker = new ColorPicker(playerSettings.getPlayerTwoColor());

        // Buttons
        Button okButton = new Button("OK");
        Button cancelButton = new Button("Cancel");

        // Prevent same color
        okButton.disableProperty().bind(
                Bindings.createBooleanBinding(
                        () -> playerOnePicker.getValue().equals(playerTwoPicker.getValue()),
                        playerOnePicker.valueProperty(), playerTwoPicker.valueProperty()
                )
        );

        okButton.setOnAction(event -> {
            String name1 = nameOneField.getText().trim();
            String name2 = vsComputer ? "Computer" : nameTwoField.getText().trim();

            String error1 = NameValidator.validate(name1, "Player 1");
            String error2 = vsComputer ? null : NameValidator.validate(name2, "Player 2");

            if (!vsComputer && name1.equalsIgnoreCase(name2)) {
                showError("Player names must be different.");
                return;
            }

            if (error1 != null && error2 != null) {
                showError(error1 + "\n" + error2);
                return;
            } else if (error1 != null) {
                showError(error1);
                return;
            } else if (error2 != null) {
                showError(error2);
                return;
            }

            playerSettings.setPlayerOneName(name1);
            playerSettings.setPlayerTwoName(name2);

            playerSettings.setPlayerOneColor(playerOnePicker.getValue());
            playerSettings.setPlayerTwoColor(playerTwoPicker.getValue());
            dialogStage.close();
        });

        cancelButton.setOnAction(event -> dialogStage.close());

        // Layout
        int row = 0;
        grid.add(nameOneLabel, 0, row);
        grid.add(nameOneField, 1, row++);
        grid.add(nameTwoLabel, 0, row);
        grid.add(nameTwoField, 1, row++);
        grid.add(playerOneColorLabel, 0, row);
        grid.add(playerOnePicker, 1, row++);
        grid.add(playerTwoColorLabel, 0, row);
        grid.add(playerTwoPicker, 1, row++);
        grid.add(okButton, 0, row);
        grid.add(cancelButton, 1, row);

        dialogStage.setScene(new Scene(grid));
    }

    /**
     * Displays the dialog and blocks until it is closed.
     */
    public void show() {
        dialogStage.showAndWait();
    }

    /**
     * Displays an error alert with the given message.
     *
     * @param message the error message to show
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
