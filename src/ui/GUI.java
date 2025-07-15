package ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import logic.GameController;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;

import java.net.URL;


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


        // Create and style buttons
        Button playerButton = new Button("Player");
        Button playerComputer = new Button("Computer");
        Button exitButton = new Button("Exit");
        exitButton.setOnAction(e -> Platform.exit());

        for (Button button : new Button[]{playerButton, playerComputer, exitButton}) {
            button.setPrefSize(150, 50);
            button.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        }

        // VBox for buttons, centered vertically
        VBox buttonBox = new VBox(25, playerButton, playerComputer, exitButton);
        buttonBox.setAlignment(Pos.CENTER);

        // StackPane to center VBox inside right panel
        StackPane rightPane = new StackPane(buttonBox);
        rightPane.setPrefWidth(450); // Adjust this to match your gray area width

        // Spacer to push content to the right
        Region leftSpacer = new Region();
        HBox.setHgrow(leftSpacer, Priority.ALWAYS);

        // HBox to split left (Connect 4 board image) and right (buttons)
        HBox mainLayout = new HBox(leftSpacer, rightPane);
        mainLayout.setPrefSize(1250, 750);

        // Set background image
        URL imageUrl = getClass().getResource("/images/menu_background.png");
        if (imageUrl == null) {
            System.err.println("Error: Background image not found!");
            return; // Or handle gracefully
        }

        Image backgroundImage = new Image(imageUrl.toExternalForm());

        BackgroundImage bgImage = new BackgroundImage(
                backgroundImage,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(100, 100, true, true, false, true)
        );
        mainLayout.setBackground(new Background(bgImage));

        // Show scene
        Scene scene = new Scene(mainLayout, 1250, 750);
        primaryStage.setTitle("Connect4Game");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
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
