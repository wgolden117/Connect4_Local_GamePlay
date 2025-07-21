package ui;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.paint.Color;

/**
 * Stores and manages settings for the two players,
 * including their names and piece colors.
 * Uses JavaFX properties for reactive UI binding.
 *
 * @author Weronika Golden
 * @version 3.0
 */
public class PlayerSettings {

    /** The color property for Player 1's pieces. */
    private final ObjectProperty<Color> playerOneColor = new SimpleObjectProperty<>(Color.RED);

    /** The color property for Player 2's pieces. */
    private final ObjectProperty<Color> playerTwoColor = new SimpleObjectProperty<>(Color.FORESTGREEN);

    /** The name property for Player 1. */
    private final SimpleObjectProperty<String> playerOneName = new SimpleObjectProperty<>("Player 1");

    /** The name property for Player 2. */
    private final SimpleObjectProperty<String> playerTwoName = new SimpleObjectProperty<>("Player 2");

    // === Color Methods ===

    /**
     * Gets the color property for Player 1.
     * @return the Player 1 color property
     */
    public ObjectProperty<Color> playerOneColorProperty() {
        return playerOneColor;
    }

    /**
     * Gets the color property for Player 2.
     * @return the Player 2 color property
     */
    public ObjectProperty<Color> playerTwoColorProperty() {
        return playerTwoColor;
    }

    /**
     * Returns the current color of Player 1.
     * @return Player 1's color
     */
    public Color getPlayerOneColor() {
        return playerOneColor.get();
    }

    /**
     * Returns the current color of Player 2.
     * @return Player 2's color
     */
    public Color getPlayerTwoColor() {
        return playerTwoColor.get();
    }

    /**
     * Sets the color for Player 1.
     * @param color the new color for Player 1
     */
    public void setPlayerOneColor(Color color) {
        playerOneColor.set(color);
    }

    /**
     * Sets the color for Player 2.
     * @param color the new color for Player 2
     */
    public void setPlayerTwoColor(Color color) {
        playerTwoColor.set(color);
    }

    // === Name Methods ===

    /**
     * Gets the name property for Player 1.
     * @return the Player 1 name property
     */
    public SimpleObjectProperty<String> playerOneNameProperty() {
        return playerOneName;
    }

    /**
     * Gets the name property for Player 2.
     * @return the Player 2 name property
     */
    public SimpleObjectProperty<String> playerTwoNameProperty() {
        return playerTwoName;
    }

    /**
     * Returns the current name of Player 1.
     * @return Player 1's name
     */
    public String getPlayerOneName() {
        return playerOneName.get();
    }

    /**
     * Returns the current name of Player 2.
     * @return Player 2's name
     */
    public String getPlayerTwoName() {
        return playerTwoName.get();
    }

    /**
     * Sets the name for Player 1.
     * @param name the new name for Player 1
     */
    public void setPlayerOneName(String name) {
        playerOneName.set(name);
    }

    /**
     * Sets the name for Player 2.
     * @param name the new name for Player 2
     */
    public void setPlayerTwoName(String name) {
        playerTwoName.set(name);
    }
}

