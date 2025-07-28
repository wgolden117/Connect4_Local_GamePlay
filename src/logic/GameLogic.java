package logic;

import java.util.ArrayList;
import java.util.List;

/**
 * GameLogic manages the core Connect 4 board state and win-checking logic.
 * It tracks moves, available positions, and checks for winning conditions.
 * Player values are assumed to be 1 and 2. Empty slots are represented by 0.
 *
 * @author Weronika
 * @version 2.0
 */
public class GameLogic {
    private static final int ROWS = 6;
    private static final int COLS = 7;
    private final int[][] board;
    private final List<int[]> winningPositions = new ArrayList<>();

    /**
     * Constructs a new GameLogic instance with an empty 6x7 board.
     */
    public GameLogic() {
        board = new int[ROWS][COLS];
    }

    /**
     * Returns a copy of the current game board to avoid exposing internal state.
     *
     * @return a deep copy of the board state
     */
    public int[][] getBoard() {
        int[][] copy = new int[ROWS][COLS];
        for (int row = 0; row < ROWS; row++) {
            System.arraycopy(board[row], 0, copy[row], 0, COLS);
        }
        return copy;
    }

    /**
     * Returns a copy of the winning positions list to preserve encapsulation.
     *
     * @return an unmodifiable copy of the winning positions
     */
    public List<int[]> getWinningPositions() {
        return List.copyOf(winningPositions);
    }


    /**
     * Checks if the given column is full (no more available spaces).
     *
     * @param col the column index to check
     * @return true if the column is full, false otherwise
     */
    public boolean isColumnFull(int col) {
        return board[0][col] != 0;
    }

    /**
     * Finds the first available row in the specified column from bottom to top.
     *
     * @param col the column index
     * @return the row index if available, or -1 if the column is full
     */
    public int getAvailableRow(int col) {
        for (int row = ROWS - 1; row >= 0; row--) {
            if (board[row][col] == 0) {
                return row;
            }
        }
        return -1;
    }

    /**
     * Places a player's piece into the specified column if there is room.
     *
     * @param col    the column index
     * @param player the player number (1 or 2)
     * @return true if the move was successful, false if the column is full
     */
    public boolean makeMove(int col, int player) {
        int row = getAvailableRow(col);
        if (row != -1) {
            board[row][col] = player;
            return true;
        }
        return false;
    }

    /**
     * Checks whether the specified player has a winning sequence of four.
     * Updates the winningPositions list if a win is found.
     *
     * @param player the player number (1 or 2)
     * @return true if the player has won, false otherwise
     */
    public boolean checkWinState(int player) {
        winningPositions.clear();
        // Horizontal
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col <= COLS - 4; col++) {
                if (board[row][col] == player &&
                        board[row][col + 1] == player &&
                        board[row][col + 2] == player &&
                        board[row][col + 3] == player) {

                    winningPositions.addAll(List.of(
                            new int[]{row, col},
                            new int[]{row, col + 1},
                            new int[]{row, col + 2},
                            new int[]{row, col + 3}
                    ));
                    return true;
                }
            }
        }
        // Vertical
        for (int col = 0; col < COLS; col++) {
            for (int row = 0; row <= ROWS - 4; row++) {
                if (board[row][col] == player &&
                        board[row + 1][col] == player &&
                        board[row + 2][col] == player &&
                        board[row + 3][col] == player) {

                    winningPositions.addAll(List.of(
                            new int[]{row, col},
                            new int[]{row + 1, col},
                            new int[]{row + 2, col},
                            new int[]{row + 3, col}
                    ));
                    return true;
                }
            }
        }
        // Diagonal /
        for (int row = 3; row < ROWS; row++) {
            for (int col = 0; col <= COLS - 4; col++) {
                if (board[row][col] == player &&
                        board[row - 1][col + 1] == player &&
                        board[row - 2][col + 2] == player &&
                        board[row - 3][col + 3] == player) {

                    winningPositions.addAll(List.of(
                            new int[]{row, col},
                            new int[]{row - 1, col + 1},
                            new int[]{row - 2, col + 2},
                            new int[]{row - 3, col + 3}
                    ));
                    return true;
                }
            }
        }
        // Diagonal \
        for (int row = 0; row <= ROWS - 4; row++) {
            for (int col = 0; col <= COLS - 4; col++) {
                if (board[row][col] == player &&
                        board[row + 1][col + 1] == player &&
                        board[row + 2][col + 2] == player &&
                        board[row + 3][col + 3] == player) {

                    winningPositions.addAll(List.of(
                            new int[]{row, col},
                            new int[]{row + 1, col + 1},
                            new int[]{row + 2, col + 2},
                            new int[]{row + 3, col + 3}
                    ));
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks whether the board is full (no available moves).
     *
     * @return true if the board is full, false otherwise
     */
    public boolean isBoardFull() {
        for (int col = 0; col < COLS; col++) {
            if (!isColumnFull(col)) return false;
        }
        return true;
    }

    /**
     * Resets the game board to its initial empty state.
     */
    public void resetBoard() {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                board[row][col] = 0;
            }
        }
    }
}
