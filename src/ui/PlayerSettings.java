package ui;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.paint.Color;

public class PlayerSettings {
    private final ObjectProperty<Color> playerOneColor = new SimpleObjectProperty<>(Color.RED);
    private final ObjectProperty<Color> playerTwoColor = new SimpleObjectProperty<>(Color.FORESTGREEN);

    // New name properties
    private final SimpleObjectProperty<String> playerOneName = new SimpleObjectProperty<>("Player 1");
    private final SimpleObjectProperty<String> playerTwoName = new SimpleObjectProperty<>("Player 2");

    // === Color Methods ===
    public ObjectProperty<Color> playerOneColorProperty() {
        return playerOneColor;
    }

    public ObjectProperty<Color> playerTwoColorProperty() {
        return playerTwoColor;
    }

    public Color getPlayerOneColor() {
        return playerOneColor.get();
    }

    public Color getPlayerTwoColor() {
        return playerTwoColor.get();
    }

    public void setPlayerOneColor(Color color) {
        playerOneColor.set(color);
    }

    public void setPlayerTwoColor(Color color) {
        playerTwoColor.set(color);
    }

    // === Name Methods ===
    public SimpleObjectProperty<String> playerOneNameProperty() {
        return playerOneName;
    }

    public SimpleObjectProperty<String> playerTwoNameProperty() {
        return playerTwoName;
    }

    public String getPlayerOneName() {
        return playerOneName.get();
    }

    public String getPlayerTwoName() {
        return playerTwoName.get();
    }

    public void setPlayerOneName(String name) {
        playerOneName.set(name);
    }

    public void setPlayerTwoName(String name) {
        playerTwoName.set(name);
    }
}
