package ui;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class PlayerNameDialog {
    private final Stage dialogStage;
    private final PlayerSettings playerSettings;
    private boolean okClicked = false;

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

            String error1 = validateName(name1, "Player 1");
            String error2 = vsComputer ? null : validateName(name2, "Player 2");

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

    private String validateName(String name, String playerLabel) {
        if (name.isEmpty()) {
            return playerLabel + " name cannot be blank. Please choose a name!";
        }
        if (!name.matches("[A-Za-z0-9]+")) {
            return playerLabel + " name contains special characters. Only letters and numbers are allowed!";
        }
        if (name.length() > 12) {
            return playerLabel + " name is too long. Please use 12 characters or fewer!";
        }
        return null;
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public boolean showAndReturnResult() {
        dialogStage.showAndWait();
        return okClicked;
    }
}