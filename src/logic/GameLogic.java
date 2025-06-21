package logic;

import java.util.ArrayList;
import java.util.List;

public class GameLogic {
    private final int rows = 6;
    private final int cols = 7;
    private final int[][] board;
    private List<int[]> winningPositions = new ArrayList<>();

    public GameLogic() {
        board = new int[rows][cols];
    }

    public int[][] getBoard() {
        return board;
    }

    public List<int[]> getWinningPositions() {
        return winningPositions;
    }

    public boolean isColumnFull(int col) {
        return board[0][col] != 0;
    }

    public int getAvailableRow(int col) {
        for (int row = rows - 1; row >= 0; row--) {
            if (board[row][col] == 0) {
                return row;
            }
        }
        return -1;
    }

    public boolean makeMove(int col, int player) {
        int row = getAvailableRow(col);
        if (row != -1) {
            board[row][col] = player;
            return true;
        }
        return false;
    }

    public boolean checkWinState(int player) {
        winningPositions.clear();

        // Horizontal
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col <= cols - 4; col++) {
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
        for (int col = 0; col < cols; col++) {
            for (int row = 0; row <= rows - 4; row++) {
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
        for (int row = 3; row < rows; row++) {
            for (int col = 0; col <= cols - 4; col++) {
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
        for (int row = 0; row <= rows - 4; row++) {
            for (int col = 0; col <= cols - 4; col++) {
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


    public boolean isBoardFull() {
        for (int col = 0; col < cols; col++) {
            if (!isColumnFull(col)) return false;
        }
        return true;
    }

    public void resetBoard() {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                board[row][col] = 0;
            }
        }
    }
}
