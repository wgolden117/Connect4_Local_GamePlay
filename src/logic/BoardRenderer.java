package logic;

import javafx.animation.TranslateTransition;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class BoardRenderer {
    private final VBox root;
    private final int rows = 6;
    private final int cols = 7;
    private final Circle[][] circles = new Circle[rows][cols];
    private Button[] buttons;

    public BoardRenderer(VBox root) {
        this.root = root;
    }

    /**
     * Creates and returns a GridPane representing the game board.
     * Each cell contains a blue rectangle and an empty white circle.
     */
    public GridPane createGrid() {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                Rectangle background = new Rectangle(90, 90);
                background.setFill(Color.BLUE);

                Circle piece = new Circle(40, Color.WHITE);
                piece.setStroke(Color.BLACK);

                circles[row][col] = piece;

                grid.add(background, col, row);
                grid.add(piece, col, row);
            }
        }

        return grid;
    }

    /**
     * Allows the layout manager to provide button references for enabling/disabling.
     */
    public void setButtons(Button[] buttons) {
        this.buttons = buttons;
    }

    /**
     * Enables or disables the drop buttons.
     */
    public void setButtonsDisabled(boolean disabled) {
        if (buttons != null) {
            for (Button button : buttons) {
                button.setDisable(disabled);
            }
        }
    }

    /**
     * Sets the color of a specific board cell.
     */
    public void setPiece(int row, int col, Color color) {
        if (row >= 0 && row < rows && col >= 0 && col < cols) {
            circles[row][col].setFill(color);
        }
    }

    /**
     * Animates a disc falling into place on the board.
     */
    public void animateDrop(int col, int row, Color color, Runnable onFinish) {
        Circle target = circles[row][col];
        Bounds bounds = target.localToScene(target.getBoundsInLocal());

        Circle falling = new Circle(target.getRadius(), color);
        falling.setStroke(Color.BLACK);
        falling.setTranslateX(bounds.getMinX());
        falling.setTranslateY(bounds.getMinY() - (row + 2) * 90);

        root.getChildren().add(falling);

        TranslateTransition transition = new TranslateTransition(Duration.millis(400), falling);
        transition.setToX(bounds.getMinX());
        transition.setToY(bounds.getMinY());

        transition.setOnFinished(e -> {
            root.getChildren().remove(falling);
            onFinish.run();
        });

        transition.play();
    }

    /**
     * Refreshes all the pieces on the board based on the current game state.
     */
    public void refreshColors(int[][] board, Color p1Color, Color p2Color) {
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[0].length; col++) {
                switch (board[row][col]) {
                    case 1 -> circles[row][col].setFill(p1Color);
                    case 2 -> circles[row][col].setFill(p2Color);
                    default -> circles[row][col].setFill(Color.WHITE);
                }
            }
        }
    }
}
