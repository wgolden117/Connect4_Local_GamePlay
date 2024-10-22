package core;
import java.util.InputMismatchException;
import java.util.Scanner;

/**
 *
 * Game Logic Module against another Player
 * @author Weronika Golden
 * @version 2.0
 *
 */

public class LogicForConsole {

    String reset = "\u001B[0m";
    String blue = "\u001B[34m";
    String magenta = "\u001B[35m";
    String red = "\u001B[31m";
    String yellow = "\u001B[33m";
    private static int count_playerX = 21;
    private static int count_playerO = 21;
    private static final int rows = 6;
    private static final int columns = 7;
    private static final char emptySpace = ' ';
    private static final char playerX = 'X';
    private static final char playerO = 'O';
    private final char[][] gameBoard;

    /**
     *
     * constructor to initialize the gameBoard
     *
     */
    public LogicForConsole() {

        gameBoard  = new char[rows][columns];

        for (int i = 0; i < rows; i++){
            for (int j = 0; j < columns; j++){
                gameBoard[i][j] = emptySpace;
            }
        }
    }

    /**
     *
     * method to print the gameBoard
     *
     */
    public void printBoard(){
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                System.out.print(gameBoard[i][j] + " " + '|' + " ");
            }
            System.out.println();
        }
    }

    /**
     *
     * checks to see if the move is valid, and if it is
     * places the game piece into the gameBoard
     * otherwise, it tells the player the move is invalid
     * @return boolean
     * @param column column number
     * @param  piece piece - either X or O
     *
     */
    public boolean addPiece(int column, char piece) throws IllegalArgumentException {

        if (column < 0 || column >= columns) {
            throw new IllegalArgumentException(red + "Invalid selection: Choose a column number between 1-7" + reset);
        }
        for (int i = rows - 1; i >= 0; i--) {
            if (gameBoard[i][column] == emptySpace) {
                gameBoard[i][column] = piece;
                return true;
            }
        }
        throw new IllegalArgumentException(red + "No more free spaces: choose another column." + reset);
    }

    /**
     *
     * Checks if there are 4 consecutive X's or O's in a row
     * Horizontally, vertically, and diagonally
     * @return boolean
     * @param piece is the current piece either X or O
     * if any of the conditions are true, returns true otherwise returns false.
     *
     */
    public boolean checkWinState(char piece){

        //check for horizontal win
        for(int i = 0; i < rows; i++){
            for(int j = 0; j <= columns-4;j++){
                if(gameBoard[i][j] == piece && gameBoard[i][j+1] == piece && gameBoard[i][j+2] == piece && gameBoard[i][j+3] == piece) {
                    return true;
                }
            }
        }
        //check for vertical win
        for (int j = 0; j < columns; j++){
            for(int i = 0; i <= rows-4; i++){
                if(gameBoard[i][j] == piece && gameBoard[i+1][j] == piece && gameBoard[i+2][j] == piece && gameBoard[i+3][j] == piece) {
                    return true;
                }
            }
        }
        //check for diagonal win
        for(int i = 0; i <= rows-4; i++){
            for(int j = 0; j <= columns-4; j++){
                // check diagonals from bottom to top right
                if(gameBoard[i][j+3] == piece && gameBoard[i+1][j+2] == piece && gameBoard[i+2][j+1] == piece && gameBoard[i+3][j] == piece){
                    return true;
                }
                // check diagonals from top to bottom right
                if (gameBoard[i][j] == piece && gameBoard[i+1][j+1] == piece && gameBoard[i+2][j+2] == piece && gameBoard[i+3][j+3] == piece){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     *
     * playGame uses a while loop to play Connect4 with 2 players
     * if there are no more moves, the while loop is broken and the game is a draw
     * if there is a winner the while loop is broken and a winner is declared
     *
     */
    public void playGame() {
        Scanner scanner = new Scanner(System.in);
        boolean playAgain = true;

        while (playAgain) {
            LogicForConsole game = new LogicForConsole();

            boolean playerXTurn = true;  // start with playerX
            boolean playConnect4 = true; // begin running the game
            count_playerX = 21; // Reset player X moves
            count_playerO = 21; // Reset player O moves

            while (playConnect4) { // while the game is running...

                game.printBoard();

                if (playerXTurn) {
                    System.out.println();
                    System.out.println(magenta + "PlayerX-your turn. Choose a column number from 1-7. Moves left: " + count_playerX + reset);
                } else {
                    System.out.println();
                    System.out.println(blue + "PlayerO-your turn. Choose a column number from 1-7. Moves left: " + count_playerO + reset);
                }

                try {
                    // players take turns until the game is over
                    if (playerXTurn) {
                        // ask scanner for user input
                        System.out.print("Enter column number (1-" + columns + "): ");
                        int column = scanner.nextInt() - 1;
                        System.out.println();
                        if (game.addPiece(column, playerX)) {
                            count_playerX--;
                            if (game.checkWinState(playerX)) {
                                game.printBoard();
                                System.out.println();
                                System.out.println(magenta + "Player X won the game!" + reset);
                                playConnect4 = false;
                            }
                            playerXTurn = false;
                        }
                    } else {
                        // ask scanner for user input
                        System.out.print("Enter column number (1-" + columns + "): ");
                        int column = scanner.nextInt() - 1;
                        System.out.println();
                        if (game.addPiece(column, playerO)) {
                            count_playerO--;
                            if (game.checkWinState(playerO)) {
                                game.printBoard();
                                System.out.println();
                                System.out.println(blue + "Player O won the game!" + reset);
                                playConnect4 = false;
                            }
                            playerXTurn = true;
                        }
                    }
                    // check if the game is a draw
                    if (playConnect4) {
                        if (count_playerO == 0 && count_playerX == 0) {
                            game.printBoard();
                            System.out.println();
                            System.out.println(yellow + "It's a Draw!" + reset);
                            playConnect4 = false;
                        }
                    }
                } catch (IllegalArgumentException e) {
                    System.out.println(red + "Invalid input: " + e.getMessage() + reset);
                    scanner.nextLine(); // Clear the scanner buffer
                } catch (InputMismatchException e) {
                    System.out.println(red + "Invalid input: Enter a column number between 1-7" + reset);
                    scanner.nextLine(); // Clear the scanner buffer
                }
            }

            // Ask if the player wants to play again
            String answer;
            while (true) {
                System.out.println();
                System.out.print("Do you want to play again? (y/n): ");
                answer = scanner.next().trim().toLowerCase();

                if (answer.equals("y") || answer.equals("n")) {
                    break; // Exit the loop if input is valid
                } else {
                    System.out.println(red + "Invalid input: Please enter 'y' for yes or 'n' for no." + reset);
                }
            }

            if (answer.equals("n")) {
                playAgain = false; // Exit the loop if the answer is 'n'
                System.out.println("Thanks for playing Connect 4!");
            }
        }
        scanner.close();
    } // ends playGame
}// ends Connect4Logic