package logic;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import ui.BoardLayout;
import ui.PlayerSettings;

import java.util.List;
import java.util.Optional;

/**
 * GameController acts as the mediator between the GUI, game logic, AI, and board rendering.
 */
public class GameController {
    private final GameLogic gameLogic;
    private final GameStateManager gameState;
    private final PlayerSettings playerSettings;
    private final BoardLayout boardLayout;
    private AIPlayer aiPlayer;
    private final Stage stage;
    private BoardRenderer boardRenderer;


    public GameController(Stage primaryStage) {
        this.stage = primaryStage; // <-- THIS fixes the problem
        this.gameLogic = new GameLogic();
        this.gameState = new GameStateManager();
        this.playerSettings = new PlayerSettings();
        this.boardLayout = new BoardLayout(gameLogic, playerSettings);
    }

    public Stage getStage() {
        return stage;
    }

    public void setBoardRenderer(BoardRenderer boardRenderer) {
        this.boardRenderer = boardRenderer;
    }

    public void setAIPlayer(AIPlayer aiPlayer) {
        this.aiPlayer = aiPlayer;
    }

    public void loadBoard(String labelText) {
        Optional<StackPane> optionalLayout = boardLayout.createBoardLayout(labelText, this);

        if (optionalLayout.isEmpty()) {
            // User clicked "Cancel" on AI difficulty dialog after choosing to play again
            if (labelText.equals("Player vs. Computer")) {
                closeApplication();
            }
            return;
        }

        StackPane layout = optionalLayout.get();
        Scene scene = new Scene(layout);
        stage.setScene(scene);
        stage.setTitle("Connect4");
        stage.setOnCloseRequest(e -> closeApplication());
        stage.show();
    }

    public void dropPiece(int col, String labelText) {
        if (gameState.isGameOver() || gameLogic.isColumnFull(col)) {
            displayMessage("Column is full. Please choose another column!", false, labelText);
            return;
        }

        int row = gameLogic.getAvailableRow(col);
        if (row == -1) {
            displayMessage("No available row in this column.", false, labelText);
            return;
        }

        int currentPlayer = gameState.getCurrentPlayer();
        Color currentColor = (currentPlayer == 1)
                ? playerSettings.getPlayerOneColor()
                : playerSettings.getPlayerTwoColor();

        boardRenderer.setButtonsDisabled(true);

        boardRenderer.animateDrop(col, row, currentColor, () -> {
            if (!gameLogic.makeMove(col, currentPlayer)) {
                displayMessage("Move could not be completed.", false, labelText);
                boardRenderer.setButtonsDisabled(false);
                return;
            }

            boardRenderer.setPiece(row, col, currentColor);
            gameState.incrementMoveCount();

            if (gameLogic.checkWinState(currentPlayer)) {
                gameState.setGameOver(true);
                displayMessage("Player " + currentPlayer + " Wins!", true, labelText);

                // Highlight the winning positions
                List<int[]> winPositions = gameLogic.getWinningPositions();
                boardRenderer.highlightWinningLine(winPositions, currentColor);
            } else if (gameState.getMoveCount() == 42 || gameLogic.isBoardFull()) {
                gameState.setGameOver(true);
                displayMessage("It's a Draw!", true, labelText);
            } else {
                gameState.switchPlayer();

                // Let AI play automatically if it's their turn
                if (labelText.equals("Player vs. Computer") && gameState.getCurrentPlayer() == 2) {
                    triggerAIMove(labelText);  // AI goes automatically
                } else {
                    boardRenderer.setButtonsDisabled(false);
                }
            }
        });
    }

    private void triggerAIMove(String labelText) {
        PauseTransition delay = new PauseTransition(Duration.seconds(0.5));
        delay.setOnFinished(event -> {
            int aiMove = aiPlayer.getMove();

            if (aiMove >= 0 && aiMove < 7) {
                dropPiece(aiMove, labelText); // Let AI drop a piece
            } else {
                displayMessage("AI attempted invalid move.", true, labelText);
            }
        });
        delay.play();
    }

    public void playAgain(String labelText) {
        gameState.reset();
        gameLogic.resetBoard(); // <- This is the missing piece!
        loadBoard(labelText);
    }

    /**
     * Helper method to display a message then close the game
     *
     * @param message    message
     * @param closeGame, closes the game if true
     * @param labelText  labelText
     */
    private void displayMessage(String message, boolean closeGame, String labelText) {
        Platform.runLater(() -> {
            Alert alert;
            if (closeGame) {
                alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Game Over");
                alert.setHeaderText(null);
                alert.setContentText(message + "\nDo you want to play again?");

                ButtonType yesButton = new ButtonType("Yes");
                ButtonType noButton = new ButtonType("No");
                alert.getButtonTypes().setAll(yesButton, noButton);

                alert.setResultConverter(buttonType -> {
                    if (buttonType == yesButton) return ButtonType.YES;
                    if (buttonType == noButton) return ButtonType.NO;
                    return null;
                });
            } else {
                alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Game Over");
                alert.setHeaderText(null);
                alert.setContentText(message);
            }

            Optional<ButtonType> result = alert.showAndWait();

            if (closeGame && result.isPresent() && result.get() == ButtonType.YES) {
                playAgain(labelText);
            } else if (closeGame && (result.isEmpty() || result.get() == ButtonType.NO)) {
                closeApplication();
            }
        });
    }
    /**
     * method to close GUI and client
     */
    public void closeApplication() {
        Optional<Stage> openStage = Stage.getWindows()
                .stream()
                .filter(window -> window instanceof Stage && window.isShowing())
                .map(window -> (Stage) window)
                .findFirst();

        openStage.ifPresent(Stage::close);
    }
}
