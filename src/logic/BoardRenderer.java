package logic;

import javafx.animation.Animation;
import javafx.animation.FillTransition;
import javafx.animation.PauseTransition;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Renders the Connect 4 game board in JavaFX.
 * This class handles creation of the grid, updating of piece colors,
 * and animations like win highlighting.
 */
public class BoardRenderer {
    private final StackPane root;
    private final int rows = 6;
    private final int cols = 7;
    private final Circle[][] circles = new Circle[rows][cols];
    private Button[] buttons;

    /**
     * Constructs a BoardRenderer using the given root StackPane.
     * @param root the root pane to bind size constraints against
     */
    public BoardRenderer(StackPane root) {
        this.root = root;
    }

    /**
     * Creates the visual game board as a GridPane of Circles and Rectangles.
     * @return the constructed GridPane
     */
    public GridPane createGrid() {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(0, 20, 0, 20)); // top, right, bottom, left
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(2);
        grid.setVgap(2);
        grid.setMinSize(0, 0);
        grid.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        // Ensure the GridPane grows dynamically
        grid.prefWidthProperty().bind(root.widthProperty());
        grid.prefHeightProperty().bind(root.heightProperty().multiply(0.75)); // Leave room for controls

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                StackPane cell = new StackPane();
                cell.setMinSize(0, 0);
                cell.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

                Rectangle background = new Rectangle();
                background.setFill(Color.BLUE);
                background.widthProperty().bind(cell.widthProperty());
                background.heightProperty().bind(cell.heightProperty());

                Circle piece = new Circle();
                piece.setFill(Color.WHITE);
                piece.setStroke(Color.BLACK);
                piece.setStrokeWidth(5);
                piece.radiusProperty().bind(
                        Bindings.createDoubleBinding(() ->
                                        Math.min(cell.getWidth(), cell.getHeight()) * 0.4,
                                cell.widthProperty(), cell.heightProperty())
                );

                circles[row][col] = piece;

                cell.getChildren().addAll(background, piece);
                grid.add(cell, col, row);
            }

            RowConstraints rc = new RowConstraints();
            rc.setPercentHeight(100.0 / rows);
            rc.setVgrow(Priority.ALWAYS);
            grid.getRowConstraints().add(rc);
        }

        for (int col = 0; col < cols; col++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(100.0 / cols);
            cc.setHgrow(Priority.ALWAYS);
            grid.getColumnConstraints().add(cc);
        }
        return grid;
    }

    /**
     * Assigns the drop buttons used to control the game.
     * @param buttons array of column drop buttons
     */
    public void setButtons(Button[] buttons) {
        this.buttons = buttons;
    }

    /**
     * Enables or disables all column drop buttons.
     * @param disabled true to disable, false to enable
     */
    public void setButtonsDisabled(boolean disabled) {
        if (buttons != null) {
            for (Button button : buttons) {
                button.setDisable(disabled);
            }
        }
    }

    /**
     * Sets the fill color of a specific piece on the board.
     * @param row the row index
     * @param col the column index
     * @param color the new fill color
     */
    public void setPiece(int row, int col, Color color) {
        if (row >= 0 && row < rows && col >= 0 && col < cols) {
            circles[row][col].setFill(color);
        }
    }

    /**
     * Returns the 2D array of Circle nodes for the board.
     * @return the board's circle array
     */
    public Circle[][] getCircles() {
        return circles;
    }

    /**
     * Highlights a winning line on the board with a flashing effect.
     * @param winningCoords list of coordinate pairs representing the winning line
     * @param playerColor the player's original piece color
     */
    public void highlightWinningLine(List<int[]> winningCoords, Color playerColor) {
        Color flashColor = getContrastingColor(playerColor);
        List<FillTransition> transitions = new ArrayList<>();

        for (int[] pos : winningCoords) {
            int row = pos[0];
            int col = pos[1];
            Circle circle = circles[row][col];

            FillTransition flash = new FillTransition(Duration.millis(300), circle);
            flash.setFromValue(playerColor);
            flash.setToValue(flashColor);
            flash.setCycleCount(Animation.INDEFINITE);
            flash.setAutoReverse(true);
            flash.play();

            transitions.add(flash);
        }
        // Stop flashing after 5 seconds
        PauseTransition stopFlashing = new PauseTransition(Duration.seconds(6));
        stopFlashing.setOnFinished(e -> transitions.forEach(Animation::stop));
        stopFlashing.play();
    }

    /**
     * Returns a contrasting color (black or yellow) based on the brightness of the original color.
     * @param baseColor the base color
     * @return a contrasting color for highlight animation
     */
    private Color getContrastingColor(Color baseColor) {
        // Convert to luminance
        double luminance = 0.2126 * baseColor.getRed() +
                0.7152 * baseColor.getGreen() +
                0.0722 * baseColor.getBlue();

        // If it's a light color (like yellow), return a dark outline (e.g., black)
        if (luminance > 0.6) {
            return Color.BLACK;
        } else {
            return Color.YELLOW;
        }
    }

    /**
     * Updates the board's circle colors based on the board state and player colors.
     * @param board the game board matrix
     * @param p1Color color for player 1
     * @param p2Color color for player 2
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
