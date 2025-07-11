package ui;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import logic.GameController;

/**
 * Class to implement a GUI
 */
public class GUI extends Application {
    private GameController controller;

    public static void main(String[] args) {
        try {
            System.out.println("Main() started");
            launch(args);
        } catch (Throwable t) {
            System.err.println("Fatal error during launch:");
            t.printStackTrace(System.err);
        }
    }
    /**
     * start method that creates the GUI to ask the user
     * if they would like to play vs. another player
     * or the computer
     *
     * @param primaryStage primaryStage
     */
    @Override
    public void start(Stage primaryStage) {
        System.out.println("Launching GUI...");

        // Create controller FIRST
        controller = new GameController(primaryStage);

        Label labelSpace = new Label(" ");
        Label label = new Label("   Select Player to play against another player. Select Computer to play against the Computer  ");
        label.setFont(Font.font("Courier", FontWeight.BOLD,
                FontPosture.ITALIC, 15));

        Button playerButton = new Button("Player");
        Button playerComputer = new Button("Computer");

        HBox hbox = new HBox();
        hbox.getChildren().addAll(playerButton, playerComputer);
        hbox.setSpacing(20);
        hbox.setAlignment(Pos.CENTER);

        VBox vbox = new VBox();
        vbox.getChildren().addAll(labelSpace, label, hbox);
        vbox.setSpacing(20);
        vbox.setAlignment(Pos.CENTER);

        Scene scene = new Scene(vbox, 680, 150);
        primaryStage.setTitle("Connect4Game");
        primaryStage.setScene(scene);

        primaryStage.setOnCloseRequest(event -> controller.closeApplication());
        primaryStage.show();

        // Now wire up buttons to controller
        playerButton.setOnAction(event -> {
            controller.setVsComputer(false);
            PlayerNameDialog dialog = new PlayerNameDialog(controller.getPlayerSettings(), false);
            if (dialog.showAndReturnResult()) {
                controller.loadBoard("Player vs. Player");
            }
        });

        playerComputer.setOnAction(event -> {
            controller.setVsComputer(true);
            PlayerNameDialog dialog = new PlayerNameDialog(controller.getPlayerSettings(), true);
            if (dialog.showAndReturnResult()) {
                controller.loadBoard("Player vs. Computer");
            }
        });
    }
}
