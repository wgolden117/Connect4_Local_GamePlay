package logic;

import animations.ConfettiAnimator;
import animations.GameAnimator;
import animations.MovingPieceAnimator;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.StackPane;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import ui.BoardLayout;
import ui.PlayerSettings;

import java.net.URL;
import java.util.List;
import java.util.Optional;

/**
 * GameController acts as the mediator between the GUI, game logic, AI, and board rendering.
 */
public class GameController {
    private final GameLogic gameLogic;
    private final GameStateManager gameState;
    private final PlayerSettings playerSettings;
    private AIPlayer aiPlayer;
    private final Stage stage;
    private BoardRenderer boardRenderer;
    private MediaPlayer backgroundPlayer;
    private boolean dropSoundEnabled = true;
    private StackPane rootPane;
    private GameAnimator gameAnimator;
    private ConfettiAnimator confettiAnimator;
    private MovingPieceAnimator movingPieceAnimator;
    private boolean vsComputer;
    private BoardLayout boardLayout;

    public GameController(Stage primaryStage) {
        this.stage = primaryStage;
        stage.setMinWidth(850);
        stage.setMinHeight(850);
        this.gameLogic = new GameLogic();
        this.gameState = new GameStateManager();
        this.playerSettings = new PlayerSettings();
        // Will be instantiated fresh in loadBoard

        setupBackgroundMusic();  // Prepare the music
        playBackgroundMusic();   // Start playing it immediately
    }

    public void setVsComputer(boolean vsComputer) {
        this.vsComputer = vsComputer;
    }

    public boolean isVsComputer() {
        return vsComputer;
    }

    public Stage getStage() {
        return stage;
    }

    public MovingPieceAnimator getMovingPieceAnimator() {
        return movingPieceAnimator;
    }

    public PlayerSettings getPlayerSettings() {
        return playerSettings;
    }

    public GameStateManager getGameStateManager() {
        return gameState;
    }

    public void setDropSoundEnabled(boolean enabled) {
        this.dropSoundEnabled = enabled;
    }

    public void setBoardRenderer(BoardRenderer boardRenderer) {
        this.boardRenderer = boardRenderer;
    }

    public void setGameAnimator(GameAnimator animator) {
        this.gameAnimator = animator;
    }

    public void setAIPlayer(AIPlayer aiPlayer) {
        this.aiPlayer = aiPlayer;
    }

    public void setConfettiHandlers(Runnable triggerConfetti) {
    }

    public void setConfettiAnimator(ConfettiAnimator animator) {
        this.confettiAnimator = animator;
    }

    public void setMovingPieceAnimator(MovingPieceAnimator animator) {
        this.movingPieceAnimator = animator;
    }

    public void playDropSound() {
        if (!dropSoundEnabled) return;

        URL soundURL = getClass().getResource("/sound/drop_piece.wav");
        if (soundURL != null) {
            AudioClip clip = new AudioClip(soundURL.toString());
            clip.play();
        } else {
            System.err.println("Sound file not found: /sound/drop_piece.wav");
        }
    }

    public void loadBoard(String labelText) {
        if (confettiAnimator != null) {
            confettiAnimator.stopConfettiAnimation();
        }

        // Create a brand new BoardLayout each time
        this.boardLayout = new BoardLayout(gameLogic, playerSettings);
        Optional<StackPane> optionalLayout = boardLayout.createBoardLayout(labelText, this);

        if (optionalLayout.isEmpty()) {
            if (labelText.equals("Player vs. Computer")) {
                closeApplication();
            }
            return;
        }

        StackPane layout = optionalLayout.get();
        this.rootPane = layout;

        // New scene with new root
        Scene scene = new Scene(layout);
        stage.setScene(scene);
        stage.setTitle("Connect4");
        stage.setOnCloseRequest(e -> closeApplication());
        stage.show();

        Platform.runLater(() -> {
            layout.applyCss();
            layout.layout();
            stage.sizeToScene();
            stage.getScene().getRoot().requestLayout();
        });
    }

    public void dropPiece(int col, String labelText, StackPane rootPane) {
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

        playDropSound();

        gameAnimator.animateDrop(col, row, currentColor, () -> {
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
                // Explode rolling pieces
                confettiAnimator.explodeRollingPiecesIntoConfetti();
                // Start confetti
                confettiAnimator.startConfettiAnimation();
            } else if (gameState.getMoveCount() == 42 || gameLogic.isBoardFull()) {
                gameState.setGameOver(true);
                displayMessage("It's a Draw!", true, labelText);
            } else {
                gameState.switchPlayer();
                if (boardLayout != null) {
                    boardLayout.refreshTurnHighlight(gameState.getCurrentPlayer());
                }

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
                dropPiece(aiMove, labelText, rootPane); // Let AI drop a piece
            } else {
                displayMessage("AI attempted invalid move.", true, labelText);
            }
        });
        delay.play();
    }

    public void setupBackgroundMusic() {
        try {
            URL musicURL = getClass().getResource("/sound/background_music.wav");
            if (musicURL != null) {
                Media media = new Media(musicURL.toURI().toString());
                backgroundPlayer = new MediaPlayer(media);
                backgroundPlayer.setCycleCount(MediaPlayer.INDEFINITE); // Loop forever
            } else {
                System.err.println("Background music file not found at /sound/background_music.wav");
            }
        } catch (Exception e) {
            System.err.println("Failed to load background music: " + e.getMessage());
        }
    }

    public void playBackgroundMusic() {
        if (backgroundPlayer != null) {
            backgroundPlayer.play();
        }
    }

    public void stopBackgroundMusic() {
        if (backgroundPlayer != null) {
            backgroundPlayer.stop();
        }
    }

    public void playAgain(String labelText) {
        gameState.reset();
        gameLogic.resetBoard();
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
