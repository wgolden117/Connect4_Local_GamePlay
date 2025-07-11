package ui;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import logic.GameController;

public class GUI extends Application {
    private GameController controller;

    public static void main(String[] args) {
        try {
            launch(args);
        } catch (Throwable t) {
            t.printStackTrace(System.err);
        }
    }

    @Override
    public void start(Stage primaryStage) {
        controller = new GameController(primaryStage);

        // Label
        Label label = new Label("Select Player to play against another player. Select Computer to play against the Computer");
        label.setFont(Font.font("Courier", FontWeight.BOLD, FontPosture.ITALIC, 15));
        label.setWrapText(true);
        label.setAlignment(Pos.CENTER);

        // Buttons
        Button playerButton = new Button("Player");
        Button playerComputer = new Button("Computer");
        HBox buttonBox = new HBox(20, playerButton, playerComputer);
        buttonBox.setAlignment(Pos.CENTER);

        // Combine into VBox
        VBox contentBox = new VBox(20, label, buttonBox);
        contentBox.setAlignment(Pos.CENTER);

        // Use AnchorPane as the root
        AnchorPane anchorRoot = new AnchorPane();

        // Anchor it to top AND bottom to allow vertical centering
        AnchorPane.setTopAnchor(contentBox, 50.0);         // Top margin
        AnchorPane.setBottomAnchor(contentBox, 50.0);      // Bottom margin
        AnchorPane.setLeftAnchor(contentBox, 0.0);
        AnchorPane.setRightAnchor(contentBox, 0.0);
        anchorRoot.getChildren().add(contentBox);

        // Show scene
        Scene scene = new Scene(anchorRoot, 700, 300);
        primaryStage.setTitle("Connect4Game");
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(e -> controller.closeApplication());
        primaryStage.show();

        // Button actions (defer name dialog to avoid IllegalStateException)
        playerButton.setOnAction(e -> {
            controller.setVsComputer(false);
            PlayerNameDialog dialog = new PlayerNameDialog(controller.getPlayerSettings(), false);
            if (dialog.showAndReturnResult()) {
                controller.loadBoard("Player vs. Player");
            }
        });

        playerComputer.setOnAction(e -> {
            controller.setVsComputer(true);
            PlayerNameDialog dialog = new PlayerNameDialog(controller.getPlayerSettings(), true);
            if (dialog.showAndReturnResult()) {
                controller.loadBoard("Player vs. Computer");
            }
        });

    }
}
