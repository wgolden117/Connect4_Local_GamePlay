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

    public Optional<StackPane> createBoardLayout(String labelText, GameController controller) {
        StackPane root = new StackPane(); // For animation overlay
        root.setPrefSize(800, 800);

        BoardRenderer boardRenderer = new BoardRenderer(root);

        GridPane grid = boardRenderer.createGrid();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER);
        grid.setMaxSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);

        // Reactive color updates
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

        // Menu bar
        MenuFactory menuFactory = new MenuFactory(controller::closeApplication, controller.getStage(), playerSettings);
        MenuBar menuBar = menuFactory.createMenuBar();

        // Title label + grid in center VBox
        Label label = new Label(" " + labelText + " ");
        label.setFont(Font.font("Courier", FontWeight.BOLD, FontPosture.ITALIC, 15));
        VBox centerBox = new VBox(20, label, grid);
        centerBox.setAlignment(Pos.CENTER);

        // Wrap with BorderPane to pin the menuBar to the top
        BorderPane layout = new BorderPane();
        layout.setTop(menuBar);      // MenuBar stays fixed at top
        layout.setCenter(centerBox); // Game content goes center

        // Add layout to root (for animation layering)
        root.getChildren().add(layout);

        // Setup buttons
        Button[] buttons = new Button[7];
        if (labelText.equals("Player vs. Player")) {
            setupPlayerVsPlayer(grid, controller, labelText, buttons);
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

            setupPlayerVsAI(grid, controller, labelText, buttons);
        }

        boardRenderer.setButtons(buttons);
        controller.setBoardRenderer(boardRenderer);

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
