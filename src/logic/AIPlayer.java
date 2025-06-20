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
        switch (difficulty) {
            case "Easy":
                return getRandomMove();
            case "Medium":
                return getBlockingMoveOrRandom();
            case "Hard":
                return getBestMoveMinimax();
            default:
                return getRandomMove();
        }
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

                int score = minimax(4, false, Integer.MIN_VALUE, Integer.MAX_VALUE);
                board[row][col] = 0;

                if (score > bestScore) {
                    bestScore = score;
                    bestCol = col;
                }
            }
        }

        return bestCol != -1 ? bestCol : getRandomMove();
    }

    private int minimax(int depth, boolean isMaximizing, int alpha, int beta) {
        int[][] board = gameLogic.getBoard();

        if (gameLogic.checkWinState(aiPlayerId)) return 1000;
        if (gameLogic.checkWinState(humanPlayerId)) return -1000;
        if (depth == 0 || gameLogic.isBoardFull()) return 0;

        if (isMaximizing) {
            int maxEval = Integer.MIN_VALUE;
            for (int col = 0; col < cols; col++) {
                if (!gameLogic.isColumnFull(col)) {
                    int row = gameLogic.getAvailableRow(col);
                    board[row][col] = aiPlayerId;

                    int eval = minimax(depth - 1, false, alpha, beta);
                    board[row][col] = 0;

                    maxEval = Math.max(maxEval, eval);
                    alpha = Math.max(alpha, eval);
                    if (beta <= alpha) break;
                }
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (int col = 0; col < cols; col++) {
                if (!gameLogic.isColumnFull(col)) {
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
}
