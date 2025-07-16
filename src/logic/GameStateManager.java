package logic;

/**
 * Manages the current state of the Connect 4 game,
 * including turn tracking, move counting, and game over status.
 * This class acts as a lightweight controller for player turn flow.
 * Player 1 always starts the game.
 *
 * @author Weronika
 * @version 1.0
 */
public class GameStateManager {
    private int currentPlayer;
    private boolean gameOver;
    private int moveCount;

    /**
     * Constructs a new GameStateManager and initializes the game state.
     */
    public GameStateManager() {
        reset();
    }

    /**
     * Returns the player whose turn it is (1 or 2).
     *
     * @return the current player number
     */
    public int getCurrentPlayer() {
        return currentPlayer;
    }

    /**
     * Checks whether the game has ended.
     *
     * @return true if the game is over, false otherwise
     */
    public boolean isGameOver() {
        return gameOver;
    }

    /**
     * Returns the total number of moves made in the game so far.
     *
     * @return the number of moves played
     */
    public int getMoveCount() {
        return moveCount;
    }

    /**
     * Increments the move counter by one.
     */
    public void incrementMoveCount() {
        moveCount++;
    }

    /**
     * Switches the current player from 1 to 2 or 2 to 1.
     */
    public void switchPlayer() {
        currentPlayer = (currentPlayer == 1) ? 2 : 1;
    }

    /**
     * Sets the game-over state.
     *
     * @param gameOver true if the game has ended, false otherwise
     */
    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

    /**
     * Resets the game state to the initial conditions:
     * - Player 1's turn
     * - Move count set to 0
     * - Game not over
     */
    public void reset() {
        currentPlayer = 1;
        moveCount = 0;
        gameOver = false;
    }
}
