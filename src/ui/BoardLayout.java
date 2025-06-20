package ui;

import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import logic.*;
import java.util.Optional;

public class BoardLayout {
    private final GameLogic gameLogic;
    private final PlayerSettings playerSettings;

    public BoardLayout(GameLogic gameLogic, PlayerSettings playerSettings) {
        this.gameLogic = gameLogic;
        this.playerSettings = playerSettings;
    }

    public Optional<VBox> createBoardLayout(String labelText, GameController controller) {
        VBox root = new VBox(); // Fresh VBox each time!
        BoardRenderer boardRenderer = new BoardRenderer(root); // Fresh BoardRenderer tied to root

        GridPane grid = boardRenderer.createGrid();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER);

        // Reactively update colors when player preferences change
        playerSettings.playerOneColorProperty().addListener((obs, oldColor, newColor) ->
                boardRenderer.refreshColors(gameLogic.getBoard(),
                        playerSettings.getPlayerOneColor(),
                        playerSettings.getPlayerTwoColor())
        );
        playerSettings.playerTwoColorProperty().addListener((obs, oldColor, newColor) ->
                boardRenderer.refreshColors(gameLogic.getBoard(),
                        playerSettings.getPlayerOneColor(),
                        playerSettings.getPlayerTwoColor())
        );

        Label label = new Label(" " + labelText + " ");
        label.setFont(Font.font("Courier", FontWeight.BOLD, FontPosture.ITALIC, 15));

        MenuFactory menuFactory = new MenuFactory(controller::closeApplication, controller.getStage(), playerSettings);
        MenuBar menuBar = menuFactory.createMenuBar();

        root.setSpacing(20);
        root.setAlignment(Pos.CENTER);
        root.getChildren().addAll(menuBar, label, grid);

        Button[] buttons = new Button[7];

        if (labelText.equals("Player vs. Player")) {
            setupPlayerVsPlayer(grid, controller, labelText, buttons);
        } else {
            // Show difficulty dialog
            ChoiceDialog<String> dialog = new ChoiceDialog<>("Easy", "Easy", "Medium", "Hard");
            dialog.setTitle("AI Difficulty");
            dialog.setHeaderText("Select AI Difficulty");
            dialog.setContentText("Difficulty:");
            Optional<String> result = dialog.showAndWait();

            // If user cancels the dialog, don't continue
            if (result.isEmpty()) {
                return Optional.empty();
            }

            String aiDifficulty = result.get();
            AIPlayer aiPlayer = new AIPlayer(gameLogic, aiDifficulty, 2);
            controller.setAIPlayer(aiPlayer);

            setupPlayerVsAI(grid, controller, labelText, buttons);
        }

        boardRenderer.setButtons(buttons);
        controller.setBoardRenderer(boardRenderer); // Pass it to controller

        return Optional.of(root);
    }

    private void setupPlayerVsPlayer(GridPane grid, GameController controller, String labelText, Button[] buttons) {
        for (int col = 0; col < 7; col++) {
            Button button = new Button("Drop");
            int finalCol = col;
            button.setOnAction(e -> controller.dropPiece(finalCol, labelText));
            buttons[col] = button;
            grid.add(button, col, 6);
        }
    }

    private void setupPlayerVsAI(GridPane grid, GameController controller, String labelText, Button[] buttons) {
        for (int col = 0; col < 7; col++) {
            Button button = new Button("Drop");
            int finalCol = col;
            button.setOnAction(e -> controller.dropPiece(finalCol, labelText));
            buttons[col] = button;
            grid.add(button, col, 6);
        }
    }
}
