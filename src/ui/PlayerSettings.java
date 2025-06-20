package ui;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.paint.Color;

public class PlayerSettings {
    private final ObjectProperty<Color> playerOneColor = new SimpleObjectProperty<>(Color.RED);
    private final ObjectProperty<Color> playerTwoColor = new SimpleObjectProperty<>(Color.FORESTGREEN);

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
}
