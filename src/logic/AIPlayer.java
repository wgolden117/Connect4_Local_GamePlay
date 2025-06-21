package logic;

import java.util.Random;

public class AIPlayer {
    private final GameLogic gameLogic;
    private final int aiPlayerId;
    private final int humanPlayerId;
    private final String difficulty;
    private final int cols = 7;

    public AIPlayer(GameLogic gameLogic, String difficulty, int aiPlayerId) {
        this.gameLogic = gameLogic;
        this.difficulty = difficulty;
        this.aiPlayerId = aiPlayerId;
        this.humanPlayerId = (aiPlayerId == 1) ? 2 : 1;
    }

    /**
     * Main method for the GUI to call — returns AI's chosen move.
     */
    public int getMove() {
        return switch (difficulty) {
            case "Medium" -> getBlockingMoveOrRandom();
            case "Hard" -> getBestMoveMinimax();
            default -> getRandomMove();
        };
    }

    private int getRandomMove() {
        Random random = new Random();
        int col;
        do {
            col = random.nextInt(cols);
        } while (gameLogic.isColumnFull(col));
        return col;
    }

    private int getBlockingMoveOrRandom() {
        int[][] board = gameLogic.getBoard();

        for (int col = 0; col < cols; col++) {
            if (!gameLogic.isColumnFull(col)) {
                int row = gameLogic.getAvailableRow(col);
                board[row][col] = humanPlayerId; // Simulate opponent move

                if (gameLogic.checkWinState(humanPlayerId)) {
                    board[row][col] = 0; // Undo
                    return col; // Block it
                }

                board[row][col] = 0; // Undo
            }
        }

        return getRandomMove();
    }

    private int getBestMoveMinimax() {
        int bestScore = Integer.MIN_VALUE;
        int bestCol = -1;
        int[][] board = gameLogic.getBoard();

        for (int col = 0; col < cols; col++) {
            if (!gameLogic.isColumnFull(col)) {
                int row = gameLogic.getAvailableRow(col);
                board[row][col] = aiPlayerId;

                int score = minimax(6, false, Integer.MIN_VALUE, Integer.MAX_VALUE);  // Depth 6 or higher
                board[row][col] = 0;

                if (score > bestScore) {
                    bestScore = score;
                    bestCol = col;
                } else if (score == bestScore && Math.abs(col - 3) < Math.abs(bestCol - 3)) {
                    bestCol = col; // Prefer closer to center if scores are equal
                }
            }
        }

        return bestCol != -1 ? bestCol : getRandomMove();
    }

    private int minimax(int depth, boolean isMaximizing, int alpha, int beta) {
        int[][] board = gameLogic.getBoard();

        if (hasWon(board, aiPlayerId)) return 100000;
        if (hasWon(board, humanPlayerId)) return -100000;
        if (depth == 0 || gameLogic.isBoardFull()) return evaluateBoard(board);

        if (isMaximizing) {
            int maxEval = Integer.MIN_VALUE;
            for (int col = 0; col < cols; col++) {
                    if (gameLogic.isColumnFull(col)) continue;

                    int row = gameLogic.getAvailableRow(col);
                    board[row][col] = aiPlayerId;

                    int eval = minimax(depth - 1, false, alpha, beta);
                    board[row][col] = 0;

                    maxEval = Math.max(maxEval, eval);
                    alpha = Math.max(alpha, eval);
                    if (beta <= alpha) break;
                }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (int col = 0; col < cols; col++) {
                if (gameLogic.isColumnFull(col)) continue;
                {
                    int row = gameLogic.getAvailableRow(col);
                    board[row][col] = humanPlayerId;

                    int eval = minimax(depth - 1, true, alpha, beta);
                    board[row][col] = 0;

                    minEval = Math.min(minEval, eval);
                    beta = Math.min(beta, eval);
                    if (beta <= alpha) break;
                }
            }
            return minEval;
        }
    }
    private boolean hasWon(int[][] board, int playerId) {
        // Horizontal
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 4; col++) {
                if (board[row][col] == playerId &&
                        board[row][col + 1] == playerId &&
                        board[row][col + 2] == playerId &&
                        board[row][col + 3] == playerId) {
                    return true;
                }
            }
        }

        // Vertical
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 7; col++) {
                if (board[row][col] == playerId &&
                        board[row + 1][col] == playerId &&
                        board[row + 2][col] == playerId &&
                        board[row + 3][col] == playerId) {
                    return true;
                }
            }
        }

        // Positive diagonal
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 4; col++) {
                if (board[row][col] == playerId &&
                        board[row + 1][col + 1] == playerId &&
                        board[row + 2][col + 2] == playerId &&
                        board[row + 3][col + 3] == playerId) {
                    return true;
                }
            }
        }

        // Negative diagonal
        for (int row = 3; row < 6; row++) {
            for (int col = 0; col < 4; col++) {
                if (board[row][col] == playerId &&
                        board[row - 1][col + 1] == playerId &&
                        board[row - 2][col + 2] == playerId &&
                        board[row - 3][col + 3] == playerId) {
                    return true;
                }
            }
        }

        return false;
    }

    private int evaluateBoard(int[][] board) {
        int score = 0;

        // Prioritize center column
        for (int row = 0; row < 6; row++) {
            if (board[row][3] == aiPlayerId) score += 6;
        }

        // Evaluate all 4-piece "windows"
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 7 - 3; col++) {
                int[] window = {board[row][col], board[row][col + 1], board[row][col + 2], board[row][col + 3]};
                score += evaluateWindow(window);
            }
        }

        // Vertical
        for (int row = 0; row < 6 - 3; row++) {
            for (int col = 0; col < 7; col++) {
                int[] window = {board[row][col], board[row + 1][col], board[row + 2][col], board[row + 3][col]};
                score += evaluateWindow(window);
            }
        }

        // Positive diagonal
        for (int row = 0; row < 6 - 3; row++) {
            for (int col = 0; col < 7 - 3; col++) {
                int[] window = {board[row][col], board[row + 1][col + 1], board[row + 2][col + 2], board[row + 3][col + 3]};
                score += evaluateWindow(window);
            }
        }

        // Negative diagonal
        for (int row = 3; row < 6; row++) {
            for (int col = 0; col < 7 - 3; col++) {
                int[] window = {board[row][col], board[row - 1][col + 1], board[row - 2][col + 2], board[row - 3][col + 3]};
                score += evaluateWindow(window);
            }
        }

        return score;
    }

    private int evaluateWindow(int[] window) {
        int score = 0;
        int aiCount = 0;
        int humanCount = 0;
        int emptyCount = 0;

        for (int val : window) {
            if (val == aiPlayerId) aiCount++;
            else if (val == humanPlayerId) humanCount++;
            else emptyCount++;
        }

        if (aiCount == 4) score += 100000;
        else if (aiCount == 3 && emptyCount == 1) score += 100;
        else if (aiCount == 2 && emptyCount == 2) score += 10;

        if (humanCount == 4) score -= 100000;
        else if (humanCount == 3 && emptyCount == 1) score -= 500;
        else if (humanCount == 2 && emptyCount == 2) score -= 200;

        return score;
    }
}
