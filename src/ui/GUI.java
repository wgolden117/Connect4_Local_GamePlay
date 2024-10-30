package ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
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
    private Button[] buttons = new Button[cols];

    public static void main(String[] args) {
        launch(args);
    }

    /**
     *
     * start method that creates the GUI to ask the user
     * if they would like to play vs. another player
     * or the computer
     *
     * @param primaryStage primaryStage
     *
     */
    @Override
    public void start(Stage primaryStage) {

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
     *
     * Creates the GameBoard with options to drop pieces
     *
     * @param primaryStage primaryStage
     * @param labelText label
     *
     */
    public void updateGridPane(Stage primaryStage, String labelText) {
        // create GridPane
        GridPane grid = new GridPane();

        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);

        // create new label
        Label label_new = new Label(" " + labelText + " ");
        label_new.setFont(Font.font("Courier", FontWeight.BOLD,
                FontPosture.ITALIC, 15));

        VBox vbox_two = new VBox();
        vbox_two.setSpacing(20);
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

        if (labelText.equals("Player vs. Player")){
            for (int col = 0; col < cols; col++) {
                Button button = new Button("Drop");
                int column = col;
                button.setOnAction(_ -> dropPiece(column, labelText));
                buttons[col] = button;
                grid.add(button, col, rows); // Re-add the buttons
            }
        } else {
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
     *
     * creates a method getButton to better organize the code
     * @param col column
     * @param labelText labelText
     * @return button
     *
     */
    private Button getButton(int col, String labelText) {
        Button button = new Button("Drop");
        button.setOnAction(_ -> {
            if (!checkFullColumn(col)) {
                if (currentPlayer == 1) { // Ensure it's Player 1's turn
                    dropPiece(col, labelText); // Player makes a move

                    // Create a delay of 1 second before the computer makes a move
                    PauseTransition delay = new PauseTransition(Duration.seconds(0.5)); // 1-second delay
                    delay.setOnFinished(event -> {
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
     *
     * Allows a player to drop a piece on the gameBoard
     * with the Drop button and checks the winState
     *
     * @param col column
     * @param labelText labelText
     *
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
                circles[row][col].setFill(currentPlayer == 1 ? Color.RED : Color.FORESTGREEN);

                // Only switch the player after a valid move
                currentPlayer = currentPlayer == 1 ? 2 : 1;
                break;
            }
        }

        count++;

        // Check for a win or a draw
        if (checkWinState(Color.RED)) {
            gameOver = true; // Set gameOver flag to true
            displayMessage("Red player Wins!", true, labelText, col);
        } else if (checkWinState(Color.FORESTGREEN)) {
            gameOver = true; // Set gameOver flag to true
            displayMessage("Green player Wins!", true, labelText, col);
        } else if (count == 42) {
            gameOver = true; // Set gameOver flag to true
            displayMessage("It's a Draw!", true, labelText, col);
        }
    }
    /**
     *
     * A method to allow the computer to randomly generate a move
     * without choosing a full column
     *
     * @return int
     *
     */
    private int computerMove(){
        Random random = new Random();
        int column;
        do {
            column = random.nextInt(cols);
        } while (checkFullColumn(column));
        return column;
    }
    /**
     *
     * checks to see if column is full
     * @param col column
     * @return boolean
     *
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
     *
     * check the win state of the GUI board
     * @param color color
     * @return boolean
     *
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
     * @param onCloseListener
     */
    public void setOnCloseListener(Runnable onCloseListener) {
        this.onCloseListener = onCloseListener;
    }

    /** Helper method to display a message then close the game
     *
     * @param message message
     * @param closeGame, closes the game if true
     * @param labelText labelText
     * @param col column
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
        // Close game
        Stage stage = (Stage) circles[0][0].getScene().getWindow();
        stage.close();
        // close client
        if (onCloseListener != null)
            onCloseListener.run();
    }
} // closes Connect4GUI class
