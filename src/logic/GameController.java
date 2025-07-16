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
 * It manages user and AI moves, updates the board view, handles sound/music, confetti, and restarts.
 * Supports both Player vs. Player and Player vs. Computer game modes.
 *
 * @author Weronika
 * @version 2.0
 */
public class GameController {
    // Core components
    private final GameLogic gameLogic;
    private final GameStateManager gameState;
    private final PlayerSettings playerSettings;
    private AIPlayer aiPlayer;

    // JavaFX stage and rendering
    private final Stage stage;
    private BoardRenderer boardRenderer;
    private BoardLayout boardLayout;

    // Animators and media
    private static MediaPlayer backgroundPlayer;
    private GameAnimator gameAnimator;
    private ConfettiAnimator confettiAnimator;
    private MovingPieceAnimator movingPieceAnimator;
    private boolean dropSoundEnabled = true;
    private boolean vsComputer;

    /**
     * Constructor to initialize the controller with the primary stage and core game components.
     * Also initializes background music.
     *
     * @param primaryStage the main JavaFX stage
     */
    public GameController(Stage primaryStage) {
        this.stage = primaryStage;
        stage.setMinWidth(850);
        stage.setMinHeight(850);
        this.gameLogic = new GameLogic();
        this.gameState = new GameStateManager();
        this.playerSettings = new PlayerSettings();
        // Will be instantiated fresh in loadBoard

        if (backgroundPlayer == null) {  // Prevent overlap
            setupBackgroundMusic();
            playBackgroundMusic();
        }
    }

    /** Sets whether the game mode is versus computer. */
    public void setVsComputer(boolean vsComputer) {
        this.vsComputer = vsComputer;
    }

    /** @return true if game mode is Player vs. Computer. */
    public boolean isVsComputer() {
        return vsComputer;
    }

    /** @return the current JavaFX Stage object. */
    public Stage getStage() {
        return stage;
    }

    /** @return the rolling piece animator for the main menu screen. */
    public MovingPieceAnimator getMovingPieceAnimator() {
        return movingPieceAnimator;
    }

    /** @return the player settings object, containing names and colors. */
    public PlayerSettings getPlayerSettings() {
        return playerSettings;
    }

    /** @return the game state manager which tracks player turns and win state. */
    public GameStateManager getGameStateManager() {
        return gameState;
    }

    /**
     * Enables or disables drop sound effects.
     *
     * @param enabled true to enable sounds, false to mute
     */
    public void setDropSoundEnabled(boolean enabled) {
        this.dropSoundEnabled = enabled;
    }

    /** Sets the board renderer responsible for updating the grid view. */
    public void setBoardRenderer(BoardRenderer boardRenderer) {
        this.boardRenderer = boardRenderer;
    }

    /** Sets the animator that visually drops game pieces. */
    public void setGameAnimator(GameAnimator animator) {
        this.gameAnimator = animator;
    }

    /** Sets the AI player logic for Player vs. Computer mode. */
    public void setAIPlayer(AIPlayer aiPlayer) {
        this.aiPlayer = aiPlayer;
    }

    /** Sets the confetti animator for post-win celebration effects. */
    public void setConfettiAnimator(ConfettiAnimator animator) {
        this.confettiAnimator = animator;
    }

    /** Sets the animator for bouncing pieces on the main menu. */
    public void setMovingPieceAnimator(MovingPieceAnimator animator) {
        this.movingPieceAnimator = animator;
    }

    /** @return true if background music is currently playing. */
    public static boolean isMusicPlaying() {
        return backgroundPlayer != null && backgroundPlayer.getStatus() == MediaPlayer.Status.PLAYING;
    }

    /** Plays the sound for dropping a piece, if sound is enabled. */
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

    /**
     * Loads a new game board scene into the stage with appropriate layout and handlers.
     *
     * @param labelText The mode label ("Player vs. Player" or "Player vs. Computer")
     */
    public void loadBoard(String labelText) {
        if (confettiAnimator != null) {
            confettiAnimator.stopConfettiAnimation();
        }

        // Create a brand new BoardLayout each time
        this.boardLayout = new BoardLayout(gameLogic, playerSettings);
        Optional<StackPane> optionalLayout = boardLayout.createBoardLayout(labelText, this);

        if (optionalLayout.isEmpty()) {
            if (labelText.equals("Player vs. Computer")) {
               playBackgroundMusic();
            }
            return;
        }

        StackPane layout = optionalLayout.get();

        // New scene with new root
        Scene scene = new Scene(layout);
        stage.setScene(scene);
        // stage.setResizable(true); // uncomment to be able to adjust game-board size
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

    /**
     * Handles the logic of dropping a piece into a column. Includes animations,
     * sound, move validation, win/draw detection, and AI turn logic.
     *
     * @param col       the column to drop into
     * @param labelText mode label for display
     */
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

    /**
     * Triggers AI move after a small delay.
     *
     * @param labelText game mode label
     */
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

    /** Loads the background music from a WAV file and prepares it to loop. */
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

    /** Starts playing the background music if not already playing. */
    public void playBackgroundMusic() {
        if (isMusicPlaying()) return;  // Already playing

        stopBackgroundMusic();  // Just in case something's hanging

        try {
            URL musicURL = getClass().getResource("/sound/background_music.wav");
            if (musicURL != null) {
                Media media = new Media(musicURL.toURI().toString());
                backgroundPlayer = new MediaPlayer(media);
                backgroundPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                backgroundPlayer.play();
            } else {
                System.err.println("Background music file not found.");
            }
        } catch (Exception e) {
            System.err.println("Failed to play background music: " + e.getMessage());
        }
    }

    /** Stops the background music if it is currently playing. */
    public void stopBackgroundMusic() {
        if (backgroundPlayer != null) {
            backgroundPlayer.stop();
            backgroundPlayer.dispose();
            backgroundPlayer = null;
        }
    }

    /**
     * Resets the game state and reloads the board to start a new game.
     *
     * @param labelText the mode label to reload
     */
    public void playAgain(String labelText) {
        gameState.reset();
        gameLogic.resetBoard();
        loadBoard(labelText);
    }

    /**
     * Displays an information or confirmation dialog. Offers replay if the game has ended.
     *
     * @param message    the message to show
     * @param closeGame  if true, prompts to restart the game
     * @param labelText  current game mode label
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
            } else {
                if (!isMusicPlaying()) {
                    stopBackgroundMusic();  // Defensive cleanup
                    playBackgroundMusic();  // Restart music only if not already playing
                }
                Platform.runLater(() -> {
                    try {
                        new ui.GUI().start(stage);
                    } catch (Exception e) {
                        System.err.printf("Failed to return to main menu: %s%n", e.getMessage());
                    }
                });
            }
        });
    }

    /** Closes the active game window and exits the application. */
    public void closeApplication() {
        Optional<Stage> openStage = Stage.getWindows()
                .stream()
                .filter(window -> window instanceof Stage && window.isShowing())
                .map(window -> (Stage) window)
                .findFirst();

        openStage.ifPresent(Stage::close);
    }
}
