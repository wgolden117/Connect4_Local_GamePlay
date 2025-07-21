package ui;

/**
 * Utility class for validating player name input.
 * Ensures names are non-empty, use only valid characters,
 * and are within a specified length limit.
 * This class supports user-friendly error messaging tied to
 * specific player labels (e.g., "Player 1" or "Player 2").
 *
 * @author Weronika Golden
 * @version 3.0
 */
public class NameValidator {

    /**
     * Validates a player name.
     *
     * @param name         the name to validate
     * @param playerLabel  the label identifying the player ("Player 1", "Player 2", etc.)
     * @return an error message if invalid, or {@code null} if the name is valid
     */
    public static String validate(String name, String playerLabel) {
        if (name == null || name.trim().isEmpty()) {
            return playerLabel + " name cannot be blank. Please choose a name!";
        }
        if (!name.matches("[A-Za-z0-9 ]+")) {
            return playerLabel + " name contains invalid characters. Only letters, numbers, and spaces are allowed!";
        }
        if (name.length() > 12) {
            return playerLabel + " name is too long. Please use 12 characters or fewer!";
        }
        return null;
    }
}
