package ui;

import core.AgainstComputerLogic;
import core.LogicForConsole;
import javafx.application.Application;
import java.util.InputMismatchException;
import java.util.Scanner;

/**
 *
 * Console based UI to test the game
 * @author Weronika Golden
 * @version 3.0
 *
 */

public class ConsoleUI {
    /**
     *
     * Asks the user of they would like the console
     * or GUI version of the game.
     * Launches the chosen selection.
     *
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

      GUI guiInterface = new GUI();

        String reset = "\u001B[0m";
        String green = "\u001B[32m";

        char Console = 'C';
        char GUI = 'G';

        boolean validInput = false;
        while (!validInput) {
            try {
                System.out.print(green + "Choose an interface. Select 'G' for GUI or 'C' for Console: " + reset);
                char letter = Character.toUpperCase(scanner.next().charAt(0)); // Convert input to uppercase
                System.out.println();

                if (letter == GUI || letter == Console) {
                    if (letter == GUI) {
                        Application.launch(guiInterface.getClass(), args);
                    } else consoleBased_UI(); {
                    }
                    validInput = true; // exit loop
                } else {
                    throw new InputMismatchException("Invalid input");
                }
            } catch (InputMismatchException e) {
                System.out.println("Please choose a valid input.");
            }
        }
    }

    /**
     *
     * A method that asks the user to select player vs. player
     * or player vs. computer. Then starts the game the user selected.
     *
     */
    private static void consoleBased_UI(){
        Scanner scanner = new Scanner(System.in);

        LogicForConsole game = new LogicForConsole();
        AgainstComputerLogic againstComputer = new AgainstComputerLogic();

        String reset = "\u001B[0m";
        String green = "\u001B[32m";

        char P = 'P';
        char C = 'C';

        boolean validInput = false;
        while (!validInput) {
            try {
                System.out.print(green + "Begin game. Enter 'P' if you want to play against another player; enter 'C' to play against the computer: " + reset);
                char letter = Character.toUpperCase(scanner.next().charAt(0)); // Convert input to uppercase
                System.out.println();

                if (letter == P || letter == C) {
                    if (letter == P) {
                        game.playGame();
                    } else  {
                        againstComputer.playGame_againstComputer();
                    }
                    validInput = true; // exit loop
                } else {
                    throw new InputMismatchException("Invalid input");
                }
            } catch (InputMismatchException e) {
                System.out.println("Please choose a valid input.");
            }
        }
    } // closes consoleBased_UI class

} // closes Connect4TextConsole