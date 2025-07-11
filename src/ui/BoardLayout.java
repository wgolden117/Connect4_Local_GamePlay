package ui;

import animations.ConfettiAnimator;
import animations.GameAnimator;
import animations.MovingPieceAnimator;
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import logic.*;

import java.util.*;

public class BoardLayout {
    private final GameLogic gameLogic;
    private final PlayerSettings playerSettings;

    public BoardLayout(GameLogic gameLogic, PlayerSettings playerSettings) {
        this.gameLogic = gameLogic;
        this.playerSettings = playerSettings;
    }

    public Optional<StackPane> createBoardLayout(String labelText, GameController controller) {
        StackPane root = new StackPane();
        root.setPrefSize(850, 850);
        // --- Rolling pieces container ---
        Pane rollingPieceContainer = new Pane();
        rollingPieceContainer.setPrefHeight(200);
        rollingPieceContainer.setStyle("-fx-background-color: transparent;");

        // Set up board renderer
        BoardRenderer boardRenderer = new BoardRenderer(root, playerSettings);
        GameAnimator gameAnimator = new GameAnimator(root, boardRenderer.getCircles());
        controller.setGameAnimator(gameAnimator);

        // Setup animations
        MovingPieceAnimator movingPieceAnimator = new MovingPieceAnimator(rollingPieceContainer, playerSettings);
        controller.setMovingPieceAnimator(movingPieceAnimator);
        ConfettiAnimator confettiAnimator = new ConfettiAnimator(root, rollingPieceContainer, movingPieceAnimator.getRollingPieces());
        controller.setConfettiAnimator(confettiAnimator);



        GridPane grid = boardRenderer.createGrid();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER);
        grid.setMaxSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);

        // Reactive color changes
        playerSettings.playerOneColorProperty().addListener((obs, oldColor, newColor) -> {
            boardRenderer.refreshColors(
                    gameLogic.getBoard(),
                    playerSettings.getPlayerOneColor(),
                    playerSettings.getPlayerTwoColor()
            );
            controller.getMovingPieceAnimator().refreshRollingPieceColors();

        });

        playerSettings.playerTwoColorProperty().addListener((obs, oldColor, newColor) -> {
            boardRenderer.refreshColors(
                    gameLogic.getBoard(),
                    playerSettings.getPlayerOneColor(),
                    playerSettings.getPlayerTwoColor()
            );
            controller.getMovingPieceAnimator().refreshRollingPieceColors();

        });

        // UI layout
        MenuFactory menuFactory = new MenuFactory(
                controller::closeApplication,
                controller.getStage(),
                playerSettings,
                controller,
                controller.isVsComputer()
        );

        MenuBar menuBar = menuFactory.createMenuBar();

        Label label = new Label(" " + labelText + " ");
        label.setFont(Font.font("Ariel", FontWeight.BOLD, FontPosture.ITALIC, 22));
        VBox.setMargin(label, new Insets(40, 0, 0, 0));

        HBox nameBox = new HBox(50);
        nameBox.setAlignment(Pos.CENTER);

        Label player1Label = new Label("Player 1: " + playerSettings.getPlayerOneName());
        Label player2Label = new Label("Player 2: " + playerSettings.getPlayerTwoName());

        player1Label.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        player2Label.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        nameBox.getChildren().addAll(player1Label, player2Label);

        VBox centerBox = new VBox(30);
        centerBox.setAlignment(Pos.TOP_CENTER);
        VBox.setVgrow(centerBox, Priority.ALWAYS);
        centerBox.getChildren().addAll(label, nameBox, grid);

        BorderPane layout = new BorderPane();
        layout.setTop(menuBar);
        layout.setCenter(centerBox);
        layout.setBottom(rollingPieceContainer);

        // Add layout to persistent root field
        root.getChildren().clear();
        root.getChildren().add(layout);

        Button[] buttons = new Button[7];
        if (labelText.equals("Player vs. Player")) {
            setupPlayerVsPlayer(grid, controller, labelText, buttons, root);
        } else {
            ChoiceDialog<String> dialog = new ChoiceDialog<>("Easy", "Easy", "Medium", "Hard");
            dialog.setTitle("AI Difficulty");
            dialog.setHeaderText("Select AI Difficulty");
            dialog.setContentText("Difficulty:");
            Optional<String> result = dialog.showAndWait();

            if (result.isEmpty()) return Optional.empty();

            String aiDifficulty = result.get();
            AIPlayer aiPlayer = new AIPlayer(gameLogic, aiDifficulty, 2);
            controller.setAIPlayer(aiPlayer);

            setupPlayerVsAI(grid, controller, labelText, buttons, root);
        }

        boardRenderer.setButtons(buttons);
        controller.setBoardRenderer(boardRenderer);
        movingPieceAnimator.startRollingPieceAnimation();

        // Ensure color refresh happens after rolling pieces are fully added
        Platform.runLater(movingPieceAnimator::refreshRollingPieceColors);

        controller.setConfettiHandlers(confettiAnimator::startConfettiAnimation);


        return Optional.of(root);
    }

    private void setupPlayerVsPlayer(GridPane grid, GameController controller, String labelText, Button[] buttons, StackPane root) {
        for (int col = 0; col < 7; col++) {
            Button button = new Button("Drop");
            int finalCol = col;
            button.setOnAction(e -> controller.dropPiece(finalCol, labelText, root));
            buttons[col] = button;
            grid.add(button, col, 6);
            GridPane.setHalignment(button, HPos.CENTER);

            Label numberLabel = new Label(String.valueOf(col + 1));
            numberLabel.setStyle("-fx-font-weight: bold;");
            numberLabel.setFont(Font.font("Arial", FontWeight.BOLD, 30));
            grid.add(numberLabel, col, 7);
            GridPane.setHalignment(numberLabel, HPos.CENTER);
        }
    }

    private void setupPlayerVsAI(GridPane grid, GameController controller, String labelText, Button[] buttons, StackPane root) {
        for (int col = 0; col < 7; col++) {
            Button button = new Button("Drop");
            int finalCol = col;
            button.setOnAction(e -> controller.dropPiece(finalCol, labelText, root));
            buttons[col] = button;
            grid.add(button, col, 6);
            GridPane.setHalignment(button, HPos.CENTER);

            Label numberLabel = new Label(String.valueOf(col + 1));
            numberLabel.setStyle("-fx-font-weight: bold;");
            numberLabel.setFont(Font.font("Arial", FontWeight.BOLD, 30));
            grid.add(numberLabel, col, 7);
            GridPane.setHalignment(numberLabel, HPos.CENTER);
        }
    }
}
