package logic;

public class GameLogic {
    private final int rows = 6;
    private final int cols = 7;
    private final int[][] board;

    public GameLogic() {
        board = new int[rows][cols];
    }

    public int[][] getBoard() {
        return board;
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
        // Horizontal
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col <= cols - 4; col++) {
                if (board[row][col] == player &&
                        board[row][col + 1] == player &&
                        board[row][col + 2] == player &&
                        board[row][col + 3] == player) {
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
