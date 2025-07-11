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

            if (!isValidName(name1)) {
                showAlert("Invalid Player 1 Name", "Name must be alphanumeric and up to 12 characters.");
                return;
            }

            if (!vsComputer && !isValidName(name2)) {
                showAlert("Invalid Player 2 Name", "Name must be alphanumeric and up to 12 characters.");
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

    public void show() {
        dialogStage.showAndWait();
    }

    private boolean isValidName(String name) {
        return name.matches("[A-Za-z0-9]{1,12}");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.showAndWait();
    }
}
