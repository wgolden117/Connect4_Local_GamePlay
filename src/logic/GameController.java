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
    private static volatile MediaPlayer backgroundPlayer;
    private GameAnimator gameAnimator;
    private ConfettiAnimator confettiAnimator;
    private MovingPieceAnimator movingPieceAnimator;
    private boolean dropSoundEnabled = true;
    private boolean vsComputer;
    // Constants
    private static final int MIN_STAGE_WIDTH = 850;
    private static final int MIN_STAGE_HEIGHT = 850;
    private static final int MAX_MOVES = 42;
    private static final double AI_MOVE_DELAY_SECONDS = 0.5;
    private static final int NUM_COLUMNS = 7;

    /**
     * Constructor to initialize the controller with the primary stage and core game components.
     * Also initializes background music.
     *
     * @param primaryStage the main JavaFX stage
     */
    public GameController(Stage primaryStage) {
        this.stage = primaryStage;
        stage.setMinWidth(MIN_STAGE_WIDTH);
        stage.setMinHeight(MIN_STAGE_HEIGHT);
        this.gameLogic = new GameLogic();
        this.gameState = new GameStateManager();
        this.playerSettings = new PlayerSettings();
        // Will be instantiated fresh in loadBoard

        if (backgroundPlayer == null) {
            GameController.setupBackgroundMusic();
            GameController.playBackgroundMusic();
        }
    }

    /** Sets whether the game mode is versus computer. */
    public void setVsComputer(boolean isVsComputer) {
        this.vsComputer = isVsComputer;
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

    /**
     * Returns the player settings. Changes to the returned object
     * will affect the internal state of the game controller.
     *
     * @return the shared PlayerSettings instance
     */
    public PlayerSettings getPlayerSettings() {
        return playerSettings;
    }

    /**
     * Returns the game state manager. Changes to the returned object
     * will affect the internal state of the game controller.
     *
     * @return the shared GameStateManager instance
     */
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


    /**
     * Sets the board renderer responsible for updating the grid view.
     * This method intentionally stores a reference to the provided {@code BoardRenderer}
     * instance, as it is created and managed by {@code BoardLayout} during board setup,
     * and is not reused across sessions. The renderer is used exclusively for UI updates
     * during a single game session, and not modified externally.
     *
     * @param renderer the BoardRenderer to use for updating the game board UI
     */
    public void setBoardRenderer(BoardRenderer renderer) {
        this.boardRenderer = renderer;
    }

    /** Sets the animator that visually drops game pieces. */
    public void setGameAnimator(GameAnimator animator) {
        this.gameAnimator = animator;
    }

    /** Sets the AI player logic for Player vs. Computer mode. */
    public void setAIPlayer(AIPlayer ai) {
        this.aiPlayer = ai;
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
     * Handles the logic of dropping a piece into a column. Validates the move,
     * plays animation and sound, updates the board, and checks win/draw state.
     *
     * @param col       the column to drop into
     * @param labelText mode label for display
     */
    public void dropPiece(int col, String labelText) {
        if (!validateMove(col, labelText)) return;

        int row = gameLogic.getAvailableRow(col);
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

            handlePostMove(col, row, currentPlayer, currentColor, labelText);
        });
    }
    /**
     * Handles the result of a move after the piece has been placed.
     * Updates the UI, checks win/draw state, switches players, and triggers AI if needed.
     *
     * @param col           the column the piece was dropped into
     * @param row           the row the piece landed in
     * @param currentPlayer the current player's number (1 or 2)
     * @param currentColor  the color of the current player's piece
     * @param labelText     the mode label used for displaying messages
     */
    private void handlePostMove(int col, int row, int currentPlayer, Color currentColor, String labelText) {
        boardRenderer.setPiece(row, col, currentColor);
        gameState.incrementMoveCount();

        if (gameLogic.checkWinState(currentPlayer)) {
            gameState.setGameOver(true);
            displayMessage("Player " + currentPlayer + " Wins!", true, labelText);
            List<int[]> winPositions = gameLogic.getWinningPositions();
            boardRenderer.highlightWinningLine(winPositions, currentColor);
            confettiAnimator.explodeRollingPiecesIntoConfetti();
            confettiAnimator.startConfettiAnimation();
        } else if (gameState.getMoveCount() == MAX_MOVES || gameLogic.isBoardFull()) {
            gameState.setGameOver(true);
            displayMessage("It's a Draw!", true, labelText);
        } else {
            gameState.switchPlayer();
            if (boardLayout != null) {
                boardLayout.refreshTurnHighlight(gameState.getCurrentPlayer());
            }

            if (labelText.equals("Player vs. Computer") && gameState.getCurrentPlayer() == 2) {
                triggerAIMove(labelText);
            } else {
                boardRenderer.setButtonsDisabled(false);
            }
        }
    }

    /**
     * Validates whether a move can be made in the given column.
     * Displays an error message if the move is invalid.
     *
     * @param col       the column to validate
     * @param labelText the mode label used for displaying messages
     * @return true if the move is valid and can proceed; false otherwise
     */
    private boolean validateMove(int col, String labelText) {
        if (gameState.isGameOver() || gameLogic.isColumnFull(col)) {
            displayMessage("Column is full. Please choose another column!", false, labelText);
            return false;
        }

        int row = gameLogic.getAvailableRow(col);
        if (row == -1) {
            displayMessage("No available row in this column.", false, labelText);
            return false;
        }

        return true;
    }

    /**
     * Triggers AI move after a small delay.
     *
     * @param labelText game mode label
     */
    private void triggerAIMove(String labelText) {
        PauseTransition delay = new PauseTransition(Duration.seconds(AI_MOVE_DELAY_SECONDS));
        delay.setOnFinished(event -> {
            int aiMove = aiPlayer.getMove();

            if (aiMove >= 0 && aiMove < NUM_COLUMNS) {
                dropPiece(aiMove, labelText); // Let AI drop a piece
            } else {
                displayMessage("AI attempted invalid move.", true, labelText);
            }
        });
        delay.play();
    }

    public static synchronized void setupBackgroundMusic() {
        try {
            URL musicURL = GameController.class.getResource("/sound/background_music.wav");
            if (musicURL != null) {
                Media media = new Media(musicURL.toURI().toString());
                backgroundPlayer = new MediaPlayer(media);
                backgroundPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            } else {
                System.err.println("Background music file not found at /sound/background_music.wav");
            }
        } catch (Exception e) {
            System.err.println("Failed to load background music: " + e.getMessage());
        }
    }

    /** Starts playing the background music if not already playing. */
    public static void playBackgroundMusic() {
        if (isMusicPlaying()) return;  // Already playing

        stopBackgroundMusic();  // Just in case something's hanging

        try {
            URL musicURL = GameController.class.getResource("/sound/background_music.wav");
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
    public static void stopBackgroundMusic() {
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
