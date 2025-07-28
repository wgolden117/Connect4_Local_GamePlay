package ui;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * A custom modal dialog that prompts the user to enter names for Player 1 and Player 2.
 * Validates names using {@link NameValidator} and updates the provided {@link PlayerSettings}.
 * Player 2's field is disabled if playing against the computer.
 *
 * @author Weronika Golden
 * @version 3.0
 */
public class PlayerNameDialog {
    private final Stage dialogStage;
    private final PlayerSettings playerSettings;
    private boolean okClicked = false;

    /**
     * Constructs the player name input dialog.
     * This constructor stores a reference to the {@code PlayerSettings} object so that it can
     * read existing names and apply validated user input directly to the shared settings.
     * This design supports two-way communication between the UI and the game logic and is
     * safe under the assumption that {@code PlayerSettings} is not modified concurrently.
     *
     * @param settings     the player settings object to update
     * @param vsComputer   true if Player 2 should be set as "Computer" and disabled
     */
    public PlayerNameDialog(PlayerSettings settings, boolean vsComputer) {
        this.playerSettings = settings;
        this.dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setTitle("Enter Player Name(s)");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(15));
        grid.setHgap(10);
        grid.setVgap(10);

        Label player1Label = new Label("Player 1 Name:");
        TextField player1Field = new TextField();
        player1Field.setPromptText("Enter name (max 12)");
        player1Field.setText(playerSettings.getPlayerOneName());

        Label player2Label = new Label("Player 2 Name:");
        TextField player2Field = new TextField();
        player2Field.setPromptText("Enter name (max 12)");
        player2Field.setText(vsComputer ? "Computer" : playerSettings.getPlayerTwoName());
        player2Field.setDisable(vsComputer);

        Button okButton = new Button("OK");
        okButton.setOnAction(e -> {
            String name1 = player1Field.getText().trim();
            String name2 = vsComputer ? "Computer" : player2Field.getText().trim();

            String error1 = NameValidator.validate(name1, "Player 1");
            String error2 = vsComputer ? null : NameValidator.validate(name2, "Player 2");

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

            // New check: same name (only if not vsComputer)
            if (!vsComputer && name1.equalsIgnoreCase(name2)) {
                showError("Player 1 and Player 2 cannot have the same name.");
                return;
            }

            playerSettings.setPlayerOneName(name1);
            playerSettings.setPlayerTwoName(name2);
            okClicked = true;
            dialogStage.close();
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> {
            okClicked = false;
            dialogStage.close();
        });

        grid.add(player1Label, 0, 0);
        grid.add(player1Field, 1, 0);
        grid.add(player2Label, 0, 1);
        grid.add(player2Field, 1, 1);
        grid.add(okButton, 0, 2);
        grid.add(cancelButton, 1, 2);

        dialogStage.setScene(new Scene(grid));
    }

    /**
     * Displays the dialog and waits for user interaction.
     *
     * @return true if the OK button was clicked and input was valid; false otherwise
     */
    public boolean showAndReturnResult() {
        dialogStage.showAndWait();
        return okClicked;
    }

    /**
     * Displays an error alert with the provided message.
     *
     * @param message the error message to display
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
