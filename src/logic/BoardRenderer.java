package logic;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.List;

public class BoardRenderer {
    private final StackPane root;
    private final int rows = 6;
    private final int cols = 7;
    private final Circle[][] circles = new Circle[rows][cols];
    private Button[] buttons;

    public BoardRenderer(StackPane root) {
        this.root = root;
    }

    public GridPane createGrid() {
        GridPane grid = new GridPane();
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
                piece.setStrokeWidth(3);
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

    public void setButtons(Button[] buttons) {
        this.buttons = buttons;
    }

    public void setButtonsDisabled(boolean disabled) {
        if (buttons != null) {
            for (Button button : buttons) {
                button.setDisable(disabled);
            }
        }
    }

    public void setPiece(int row, int col, Color color) {
        if (row >= 0 && row < rows && col >= 0 && col < cols) {
            circles[row][col].setFill(color);
        }
    }

    public void animateDrop(int col, int row, Color color, Runnable onFinish) {
        Circle target = circles[row][col];

        Platform.runLater(() -> {
            // Create falling circle with same radius and color
            Circle falling = new Circle(target.getRadius(), color);
            falling.setStroke(Color.BLACK);
            falling.setCache(true); // Performance optimization
            falling.setCacheHint(CacheHint.SPEED);

            // Position visually over the cell
            StackPane.setAlignment(falling, Pos.TOP_LEFT);

            Bounds targetBounds = target.localToScene(target.getBoundsInLocal());
            Point2D localPoint = root.sceneToLocal(targetBounds.getMinX(), targetBounds.getMinY());

            // Starting position (500px above)
            double startY = -500;
            double endY = localPoint.getY();
            double distance = endY - startY;
            double speed = 2.5; // pixels per millisecond
            Duration dropDuration = Duration.millis(distance / speed);

            falling.setTranslateX(localPoint.getX());
            falling.setTranslateY(startY);

            root.getChildren().add(falling);

            // Main drop
            TranslateTransition drop = new TranslateTransition(dropDuration, falling);
            drop.setToY(endY);
            drop.setInterpolator(Interpolator.EASE_OUT);

            // Bounce durations scaled down relative to screen
            double bounceOffset1 = 12;
            double bounceOffset2 = 6;
            double bounceOffset3 = 3;

            TranslateTransition bounceUp = new TranslateTransition(Duration.millis(100), falling);
            bounceUp.setToY(endY - bounceOffset1);

            TranslateTransition settle1 = new TranslateTransition(Duration.millis(100), falling);
            settle1.setToY(endY);

            TranslateTransition bounceUp2 = new TranslateTransition(Duration.millis(80), falling);
            bounceUp2.setToY(endY - bounceOffset2);

            TranslateTransition settle2 = new TranslateTransition(Duration.millis(80), falling);
            settle2.setToY(endY);

            TranslateTransition bounceUp3 = new TranslateTransition(Duration.millis(60), falling);
            bounceUp3.setToY(endY - bounceOffset3);

            TranslateTransition settle3 = new TranslateTransition(Duration.millis(60), falling);
            settle3.setToY(endY);

            // Chain all
            SequentialTransition sequence = new SequentialTransition(
                    drop, bounceUp, settle1, bounceUp2, settle2, bounceUp3, settle3
            );

            sequence.setOnFinished(e -> {
                root.getChildren().remove(falling);
                onFinish.run();
            });

            sequence.play();
        });
    }


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
    public void highlightWinningLine(List<int[]> winningCoords, Color playerColor) {
        Color flashColor = getContrastingColor(playerColor);
        List<FillTransition> transitions = new ArrayList<>();

        for (int[] pos : winningCoords) {
            int row = pos[0];
            int col = pos[1];
            Circle circle = circles[row][col];

            FillTransition flash = new FillTransition(Duration.millis(600), circle);
            flash.setFromValue(playerColor);
            flash.setToValue(flashColor);
            flash.setCycleCount(Animation.INDEFINITE);
            flash.setAutoReverse(true);
            flash.play();

            transitions.add(flash);
        }
        // Stop flashing after 5 seconds
        PauseTransition stopFlashing = new PauseTransition(Duration.seconds(3));
        stopFlashing.setOnFinished(e -> transitions.forEach(Animation::stop));
        stopFlashing.play();
    }

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
}
