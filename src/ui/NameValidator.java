package ui;

public class NameValidator {

    /**
     * Validates a player name.
     *
     * @param name         the name to validate
     * @param playerLabel  "Player 1" or "Player 2"
     * @return an error message if invalid, or null if valid
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
