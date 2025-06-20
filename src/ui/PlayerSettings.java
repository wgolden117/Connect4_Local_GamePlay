package ui;

import javafx.scene.paint.Color;

public class PlayerSettings {
    private Color playerOneColor = Color.RED;
    private Color playerTwoColor = Color.FORESTGREEN;

    public Color getPlayerOneColor() {
        return playerOneColor;
    }

    public void setPlayerOneColor(Color color) {
        this.playerOneColor = color;
    }

    public Color getPlayerTwoColor() {
        return playerTwoColor;
    }

    public void setPlayerTwoColor(Color color) {
        this.playerTwoColor = color;
    }
}
