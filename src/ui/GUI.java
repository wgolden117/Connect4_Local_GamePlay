package ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import java.util.Optional;
import java.util.Random;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import javafx.stage.Window;

/**
 * Class to implement a GUI
 */
public class GUI extends Application {
    private final int rows = 6;
    private final int cols = 7;
    private final Circle[][] circles = new Circle[rows][cols];
    private final int[][] gameBoard = new int[rows][cols]; // Logical game state
    private int currentPlayer = 1; // Player 1: Red, Player 2: Green
    private int count = 0;
    private Runnable onCloseListener;
    private boolean gameOver = false;
    private final PlayerSettings playerSettings = new PlayerSettings();
    private String aiDifficulty = "Easy";


    public static void main(String[] args) {
        try {
            System.out.println("Main() started");
            launch(args);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
    /**
     * start method that creates the GUI to ask the user
     * if they would like to play vs. another player
     * or the computer
     *
     * @param primaryStage primaryStage
     */
    @Override
    public void start(Stage primaryStage) {
        System.out.println("Launching GUI...");

        Label labelSpace = new Label(" ");
        Label label = new Label("   Select Player to play against another player. Select Computer to play against the Computer  ");
        label.setFont(Font.font("Courier", FontWeight.BOLD,
                FontPosture.ITALIC, 15));

        Button playerButton = new Button("Player");
        Button playerComputer = new Button("Computer");

        HBox hbox = new HBox();
        hbox.getChildren().addAll(playerButton, playerComputer);
        hbox.setSpacing(20);

        VBox vbox = new VBox();
        vbox.getChildren().addAll(labelSpace, label, hbox);
        vbox.setSpacing(20);
        hbox.setAlignment(Pos.CENTER);

        // Create a scene and place it in the stage
        Scene scene = new Scene(vbox, 680, 150);
        primaryStage.setTitle("Connect4Game"); // Set the stage title;
        primaryStage.setScene(scene); // Place the scene in the stage

        // Add event handler for window close request
        primaryStage.setOnCloseRequest(event -> {
            // Close the application
            closeApplication();
        });
        primaryStage.show(); // Display the stage

        // click player button
        playerButton.setOnAction(event -> updateGridPane(primaryStage, "Player vs. Player"));

        // click computer button
        playerComputer.setOnAction(event -> updateGridPane(primaryStage, "Player vs. Computer"));

    } // closes start method
    /**
     * Creates the GameBoard with options to drop pieces
     *
     * @param primaryStage primaryStage
     * @param labelText    label
     */
    public void updateGridPane(Stage primaryStage, String labelText) {
        // create GridPane
        GridPane grid = new GridPane();
        playerSettings.playerOneColorProperty().addListener((obs, oldColor, newColor) -> refreshBoardColors());
        playerSettings.playerTwoColorProperty().addListener((obs, oldColor, newColor) -> refreshBoardColors());

        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);

        // create new label
        Label label_new = new Label(" " + labelText + " ");
        label_new.setFont(Font.font("Courier", FontWeight.BOLD,
                FontPosture.ITALIC, 15));

        VBox vbox_two = new VBox();
        vbox_two.setSpacing(20);
        // Add Menu Bar
        MenuFactory menuFactory = new MenuFactory(this::closeApplication, primaryStage, playerSettings);
        MenuBar menuBar = menuFactory.createMenuBar();
        vbox_two.getChildren().add(menuBar);
        vbox_two.getChildren().addAll(label_new, grid);
        vbox_two.setAlignment(Pos.CENTER);

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int tileSize = 90;
                Rectangle rectangle = new Rectangle(tileSize, tileSize);
                rectangle.setFill(Color.BLUE);
                grid.add(rectangle, col, row);

                Circle circle = new Circle((double) tileSize / 2 - 5);
                circle.setFill(Color.WHITE);
                circle.setStroke(Color.BLACK);
                circles[row][col] = circle;

                grid.add(circle, col, row);
            }
        }

        Button[] buttons = new Button[cols];

        if (labelText.equals("Player vs. Player")) {
            for (int col = 0; col < cols; col++) {
                Button button = new Button("Drop");
                int column = col;
                button.setOnAction(event -> dropPiece(column, labelText));
                buttons[col] = button;
                grid.add(button, col, rows); // Re-add the buttons
            }
        } else {
            // Prompt for AI difficulty
            ChoiceDialog<String> difficultyDialog = new ChoiceDialog<>("Easy", "Easy", "Medium", "Hard");
            difficultyDialog.setTitle("AI Difficulty");
            difficultyDialog.setHeaderText("Select AI Difficulty");
            difficultyDialog.setContentText("Difficulty:");

            Optional<String> result = difficultyDialog.showAndWait();
            aiDifficulty = result.orElse("Easy"); // Use default if user cancels

            // play against computer
            for (int col = 0; col < cols; col++) {
                Button button = getButton(col, labelText); // Reinitialize the buttons
                buttons[col] = button;
                grid.add(button, col, rows); // Re-add the buttons
            }
        }
        Scene scene = new Scene(vbox_two);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Connect4");
        primaryStage.show();

        // Add window close listener to handle client shutdown
        primaryStage.setOnCloseRequest(event -> {
            closeApplication();
        });
    }
    /**
     * creates a method getButton to better organize the code
     *
     * @param col       column
     * @param labelText labelText
     * @return button
     */
    private Button getButton(int col, String labelText) {
        Button button = new Button("Drop");
        button.setOnAction(event -> {
            if (!checkFullColumn(col)) {
                if (currentPlayer == 1) { // Ensure it's Player 1's turn
                    dropPiece(col, labelText); // Player makes a move

                    // Create a delay of 1 second before the computer makes a move
                    PauseTransition delay = new PauseTransition(Duration.seconds(0.5)); // 1-second delay
                    delay.setOnFinished(finishedEvent -> { // Rename to avoid conflict
                        if (currentPlayer == 2) { // Check if it's the computer's turn
                            int computerTurn = computerMove(); // computer generates a move
                            dropPiece(computerTurn, labelText); // computer makes a move
                        }
                    });
                    delay.play(); // Start the delay
                }
            } else {
                displayMessage("Column is full. Please choose another column.", false, labelText, col);
            }
        });
        return button;
    }
    /**
     * Allows a player to drop a piece on the gameBoard
     * with the Drop button and checks the winState
     *
     * @param col       column
     * @param labelText labelText
     */
    private void dropPiece(int col, String labelText) {
        // If the game is over, do nothing
        if (gameOver) {
            return; // Exit the method if the game is over
        }
        // Check if the column is full
        if (checkFullColumn(col)) {
            displayMessage("Column is full. Please choose another column.", false, labelText, col);
            return;
        }
        // Find the next available row in the selected column
        for (int row = rows - 1; row >= 0; row--) {
            if (circles[row][col].getFill() == Color.WHITE) {
                gameBoard[row][col] = currentPlayer; // Save player ID
                circles[row][col].setFill(currentPlayer == 1 ? playerSettings.getPlayerOneColor() : playerSettings.getPlayerTwoColor());
                currentPlayer = currentPlayer == 1 ? 2 : 1;
                break;
            }
        }
        count++;
        // Dynamically get the current win colors from settings
        Color playerOneColor = playerSettings.getPlayerOneColor();
        Color playerTwoColor = playerSettings.getPlayerTwoColor();

        if (checkWinState(playerOneColor)) {
            gameOver = true;
            displayMessage("Player 1 Wins!", true, labelText, col);
        } else if (checkWinState(playerTwoColor)) {
            gameOver = true;
            displayMessage("Player 2 Wins!", true, labelText, col);
        } else if (count == 42) {
            gameOver = true;
            displayMessage("It's a Draw!", true, labelText, col);
        }
    }
    private void refreshBoardColors() {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (gameBoard[row][col] == 1) {
                    circles[row][col].setFill(playerSettings.getPlayerOneColor());
                } else if (gameBoard[row][col] == 2) {
                    circles[row][col].setFill(playerSettings.getPlayerTwoColor());
                }
            }
        }
    }
    /**
     * A method to allow the computer to randomly generate a move
     * without choosing a full column
     *
     * @return int
     */
    private int computerMove() {
        switch (aiDifficulty) {
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
    /**
     * Generates a random valid column index that is not full.
     *
     * @return a column index where a move can be made.
     */
    private int getRandomMove() {
        Random random = new Random();
        int column;
        do {
            column = random.nextInt(cols);
        } while (checkFullColumn(column));
        return column;
    }

    /**
     * Attempts to block the human player's winning move.
     * If no block is needed, selects a random valid column.
     *
     * @return a column index to block or move randomly.
     */
    private int getBlockingMoveOrRandom() {
        for (int col = 0; col < cols; col++) {
            if (!checkFullColumn(col)) {
                int row = getAvailableRow(col);
                circles[row][col].setFill(playerSettings.getPlayerOneColor()); // Simulate

                if (checkWinState(playerSettings.getPlayerOneColor())) {
                    circles[row][col].setFill(Color.WHITE); // Undo
                    return col; // Block
                }

                circles[row][col].setFill(Color.WHITE); // Undo
            }
        }
        return getRandomMove();
    }

    /**
     * Determines the best possible move using the minimax algorithm
     * with alpha-beta pruning to simulate and evaluate moves.
     *
     * @return the column index of the best move, or a fallback random move.
     */
    private int getBestMoveMinimax() {
        int bestScore = Integer.MIN_VALUE;
        int bestCol = -1;

        for (int col = 0; col < cols; col++) {
            if (!checkFullColumn(col)) {
                int row = getAvailableRow(col);
                circles[row][col].setFill(playerSettings.getPlayerTwoColor());

                int score = minimax(4, false, Integer.MIN_VALUE, Integer.MAX_VALUE);

                circles[row][col].setFill(Color.WHITE); // Undo

                if (score > bestScore) {
                    bestScore = score;
                    bestCol = col;
                }
            }
        }
        return bestCol != -1 ? bestCol : getRandomMove();
    }

    /**
     * Recursive minimax function to evaluate board states.
     *
     * @param depth remaining search depth
     * @param isMaximizing true if maximizing (AI's turn), false if minimizing (player's turn)
     * @param alpha best value the maximizer can guarantee so far
     * @param beta best value the minimizer can guarantee so far
     * @return evaluation score for the current game state
     */
    private int minimax(int depth, boolean isMaximizing, int alpha, int beta) {
        if (checkWinState(playerSettings.getPlayerTwoColor())) return 1000;
        if (checkWinState(playerSettings.getPlayerOneColor())) return -1000;
        if (depth == 0 || count == 42) return 0;

        if (isMaximizing) {
            int maxEval = Integer.MIN_VALUE;
            for (int col = 0; col < cols; col++) {
                if (!checkFullColumn(col)) {
                    int row = getAvailableRow(col);
                    circles[row][col].setFill(playerSettings.getPlayerTwoColor());

                    int eval = minimax(depth - 1, false, alpha, beta);
                    circles[row][col].setFill(Color.WHITE);
                    maxEval = Math.max(maxEval, eval);
                    alpha = Math.max(alpha, eval);
                    if (beta <= alpha) break;
                }
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (int col = 0; col < cols; col++) {
                if (!checkFullColumn(col)) {
                    int row = getAvailableRow(col);
                    circles[row][col].setFill(playerSettings.getPlayerOneColor());

                    int eval = minimax(depth - 1, true, alpha, beta);
                    circles[row][col].setFill(Color.WHITE);
                    minEval = Math.min(minEval, eval);
                    beta = Math.min(beta, eval);
                    if (beta <= alpha) break;
                }
            }
            return minEval;
        }
    }
    /**
     * Finds the lowest available row in a specified column.
     *
     * @param col the column index
     * @return the row index of the lowest empty cell, or -1 if full
     */
    private int getAvailableRow(int col) {
        for (int row = rows - 1; row >= 0; row--) {
            if (circles[row][col].getFill().equals(Color.WHITE)) {
                return row;
            }
        }
        return -1;
    }
    /**
     * checks to see if column is full
     *
     * @param col column
     * @return boolean
     */
    private boolean checkFullColumn(int col) {
        for (int row = rows - 1; row >= 0; row--) {
            if (circles[row][col].getFill() == Color.WHITE) {
                return false;
            }
        }
        return true;
    }
    /**
     * check the win state of the GUI board
     *
     * @param color color
     * @return boolean
     */
    public boolean checkWinState(Color color) {
        // Check horizontal
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col <= cols - 4; col++) {
                if (circles[row][col].getFill() == color &&
                        circles[row][col + 1].getFill() == color &&
                        circles[row][col + 2].getFill() == color &&
                        circles[row][col + 3].getFill() == color) {
                    return true;
                }
            }
        }

        // Check vertical
        for (int col = 0; col < cols; col++) {
            for (int row = 0; row <= rows - 4; row++) {
                if (circles[row][col].getFill() == color &&
                        circles[row + 1][col].getFill() == color &&
                        circles[row + 2][col].getFill() == color &&
                        circles[row + 3][col].getFill() == color) {
                    return true;
                }
            }
        }

        // Check diagonal (top-left to bottom-right)
        for (int row = 0; row <= rows - 4; row++) {
            for (int col = 0; col <= cols - 4; col++) {
                if (circles[row][col].getFill() == color &&
                        circles[row + 1][col + 1].getFill() == color &&
                        circles[row + 2][col + 2].getFill() == color &&
                        circles[row + 3][col + 3].getFill() == color) {
                    return true;
                }
            }
        }

        // Check diagonal (top-right to bottom-left)
        for (int row = 0; row <= rows - 4; row++) {
            for (int col = 3; col < cols; col++) {
                if (circles[row][col].getFill() == color &&
                        circles[row + 1][col - 1].getFill() == color &&
                        circles[row + 2][col - 2].getFill() == color &&
                        circles[row + 3][col - 3].getFill() == color) {
                    return true;
                }
            }
        }

        return false; // No win state found
    }
    /**
     * Listen for window close
     *
     * @param onCloseListener
     */
    public void setOnCloseListener(Runnable onCloseListener) {
        this.onCloseListener = onCloseListener;
    }

    /**
     * Helper method to display a message then close the game
     *
     * @param message    message
     * @param closeGame, closes the game if true
     * @param labelText  labelText
     * @param col        column
     */
    private void displayMessage(String message, boolean closeGame, String labelText, int col) {
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

                // Set the result converter to handle the user's choice
                alert.setResultConverter(buttonType -> {
                    if (buttonType == yesButton) {
                        return ButtonType.YES;
                    } else if (buttonType == noButton) {
                        return ButtonType.NO;
                    } else {
                        return null;
                    }
                });
            } else {
                alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Game Over");
                alert.setHeaderText(null);
                alert.setContentText(message);
            }

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.YES && !checkFullColumn(col)) {
                // User clicked Yes, restart the game
                playAgain(labelText);
            } else if (result.isPresent() && result.get() == ButtonType.YES) {
                playAgain(labelText);
            } else if (result.isEmpty() || result.get() == ButtonType.NO) {
                // User clicked No or closed the dialog, close the game
                closeApplication();
            }
        });
    }
    /**
     * Method to start the game over
     *
     * @param labelText labelText
     */
    private void playAgain(String labelText) {
        // Reset player turns - Player 1 should always start first
        currentPlayer = 1;

        // Reset move count
        count = 0;

        // Reset the gameOver flag
        gameOver = false;

        // Get the current stage
        Stage stage = (Stage) circles[0][0].getScene().getWindow();

        // Show the game board again
        updateGridPane(stage, labelText); // This will rebuild the game board with new event handlers
    }
    /**
     * method to close GUI and client
     */
    public void closeApplication() {
        // Check if the circles array has been initialized
        Stage stage;
        if (circles[0][0] != null && circles[0][0].getScene() != null) {
            stage = (Stage) circles[0][0].getScene().getWindow();
        } else {
            // If circles is uninitialized, get the primary stage in a safer way
            stage = (Stage) Stage.getWindows().filtered(Window::isShowing).get(0);
        }
        stage.close();

        // Execute any additional onCloseListener tasks
        if (onCloseListener != null) {
            onCloseListener.run();
        }
    }
}// closes GUI class
