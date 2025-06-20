package logic;

public class GameStateManager {
    private int currentPlayer;
    private boolean gameOver;
    private int moveCount;

    public GameStateManager() {
        reset();
    }

    public int getCurrentPlayer() {
        return currentPlayer;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public int getMoveCount() {
        return moveCount;
    }

    public void incrementMoveCount() {
        moveCount++;
    }

    public void switchPlayer() {
        currentPlayer = (currentPlayer == 1) ? 2 : 1;
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

    public void reset() {
        currentPlayer = 1;
        moveCount = 0;
        gameOver = false;
    }
}
