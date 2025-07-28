package ui;

import animations.ConfettiAnimator;
import animations.GameAnimator;
import animations.MovingPieceAnimator;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import logic.AIPlayer;
import logic.BoardRenderer;
import logic.GameController;
import logic.GameLogic;
import java.util.Optional;

/**
 * BoardLayout is responsible for constructing and managing the layout of the Connect 4 game board,
 * including the game grid, player labels, animations, and menu bar.
 * It supports both Player vs. Player and Player vs. Computer modes.
 */
public class BoardLayout {
    private final GameLogic gameLogic;
    private final PlayerSettings playerSettings;
    private Label player1Label;
    private Label player2Label;

    /**
     * Constructs a new BoardLayout instance with the provided game logic and player settings.
     * <p>
     * This constructor stores references to the provided {@code GameLogic} and {@code PlayerSettings}
     * objects to support game state access and dynamic UI updates. The {@code playerSettings} instance
     * is shared across UI components to reflect live changes such as player names and colors via property binding.
     * This usage is safe as {@code BoardLayout} does not modify the internal state of {@code PlayerSettings}
     * directly but uses it for reactive bindings and layout refreshes.
     * </p>
     *
     * @param gameLogic       the game logic managing board state and win conditions
     * @param playerSettings  the player settings for colors and names (shared via property bindings)
     */
    public BoardLayout(GameLogic gameLogic, PlayerSettings playerSettings) {
        this.gameLogic = gameLogic;
        this.playerSettings = playerSettings;
    }

    /**
     * Creates the full board layout including UI elements, animations, and interactions.
     *
     * @param labelText  the game mode label (e.g., "Player vs. Player")
     * @param controller the main game controller for coordinating game flow
     * @return an Optional containing the StackPane layout, or empty if the user cancels AI selection
     */
    public Optional<StackPane> createBoardLayout(String labelText, GameController controller) {
        StackPane root = new StackPane();
        root.setPrefSize(850, 850);
        // --- Rolling pieces container ---
        Pane rollingPieceContainer = new Pane();
        rollingPieceContainer.setPrefHeight(200);
        rollingPieceContainer.setStyle("-fx-background-color: transparent;");

        // Set up board renderer
        BoardRenderer boardRenderer = new BoardRenderer(root);
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
            refreshTurnHighlight(controller.getGameStateManager().getCurrentPlayer());
        });

        playerSettings.playerTwoColorProperty().addListener((obs, oldColor, newColor) -> {
            boardRenderer.refreshColors(
                    gameLogic.getBoard(),
                    playerSettings.getPlayerOneColor(),
                    playerSettings.getPlayerTwoColor()
            );
            controller.getMovingPieceAnimator().refreshRollingPieceColors();
            refreshTurnHighlight(controller.getGameStateManager().getCurrentPlayer());
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

        player1Label = new Label();
        player1Label.textProperty().bind(
                Bindings.concat("Player 1: ").concat(playerSettings.playerOneNameProperty())
        );

        player2Label = new Label();
        player2Label.textProperty().bind(
                Bindings.concat("Player 2: ").concat(playerSettings.playerTwoNameProperty())
        );

        player1Label.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        player2Label.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        nameBox.getChildren().addAll(player1Label, player2Label);
        updateTurnHighlight(controller.getGameStateManager().getCurrentPlayer());

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
        movingPieceAnimator.startRollingPieceAnimation();

        // Ensure color refresh happens after rolling pieces are fully added
        Platform.runLater(movingPieceAnimator::refreshRollingPieceColors);

        return Optional.of(root);
    }

    /**
     * Highlights the active player's name label with a colored border.
     *
     * @param currentPlayer the currently active player (1 or 2)
     */
    private void updateTurnHighlight(int currentPlayer) {
        Color borderColor = (currentPlayer == 1)
                ? playerSettings.getPlayerOneColor()
                : playerSettings.getPlayerTwoColor();

        Label activeLabel = (currentPlayer == 1) ? player1Label : player2Label;
        Label inactiveLabel = (currentPlayer == 1) ? player2Label : player1Label;

        activeLabel.setBorder(new Border(new BorderStroke(
                borderColor,
                BorderStrokeStyle.SOLID,
                new CornerRadii(8),
                new BorderWidths(3)
        )));

        inactiveLabel.setBorder(null);
    }

    /**
     * Public method to refresh the turn highlight based on current player.
     *
     * @param currentPlayer the player whose turn is active
     */
    public void refreshTurnHighlight(int currentPlayer) {
        updateTurnHighlight(currentPlayer);
    }

    /**
     * Sets up the column drop buttons and column numbers for Player vs. Player mode.
     *
     * @param grid       the GridPane to which buttons and labels are added
     * @param controller the main game controller
     * @param labelText  the game mode label
     * @param buttons    an array to store button references
     */
    private void setupPlayerVsPlayer(GridPane grid, GameController controller, String labelText, Button[] buttons) {
        for (int col = 0; col < 7; col++) {
            Button button = new Button("Drop");
            int finalCol = col;
            button.setOnAction(e -> controller.dropPiece(finalCol, labelText));
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

    /**
     * Sets up the column drop buttons and column numbers for Player vs. AI mode.
     *
     * @param grid       the GridPane to which buttons and labels are added
     * @param controller the main game controller
     * @param labelText  the game mode label
     * @param buttons    an array to store button references
     */
    private void setupPlayerVsAI(GridPane grid, GameController controller, String labelText, Button[] buttons) {
        for (int col = 0; col < 7; col++) {
            Button button = new Button("Drop");
            int finalCol = col;
            button.setOnAction(e -> controller.dropPiece(finalCol, labelText));
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
