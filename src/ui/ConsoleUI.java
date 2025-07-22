package ui;

import core.AgainstComputerLogic;
import core.LogicForConsole;
import javafx.application.Application;

import java.nio.charset.StandardCharsets;
import java.util.InputMismatchException;
import java.util.Scanner;

/**
 * Console-based entry point for testing the Connect 4 game.
 * Prompts the user to choose between launching the GUI or Console version.
 *
 * @author Weronika Golden
 * @version 3.0
 */
public class ConsoleUI {

    /**
     * Prompts the user to choose between the GUI or Console version of the game.
     * Launches the selected interface.
     *
     * @param args command-line arguments passed to the application
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);

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
     * Prompts the user to select either Player vs. Player or Player vs. Computer mode.
     * Starts the corresponding console-based game session.
     */
    private static void consoleBased_UI(){
        Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);

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
    }
}