package logic;

import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import javafx.scene.media.AudioClip;

import java.net.URL;

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
        grid.setHgap(10);
        grid.setVgap(10);

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                Rectangle background = new Rectangle(90, 90);
                background.setFill(Color.BLUE);

                Circle piece = new Circle(40, Color.WHITE);
                piece.setStroke(Color.BLACK);
                circles[row][col] = piece;

                // Wrap in StackPane to center-align
                StackPane cell = new StackPane();
                cell.getChildren().addAll(background, piece);

                grid.add(cell, col, row);
            }
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

            // Position it visually over the correct cell
            StackPane.setAlignment(falling, Pos.TOP_LEFT);

            Bounds targetBounds = target.localToScene(target.getBoundsInLocal());
            Point2D localPoint = root.sceneToLocal(targetBounds.getMinX(), targetBounds.getMinY());

            falling.setTranslateX(localPoint.getX());
            falling.setTranslateY(-500); // Start above the board

            root.getChildren().add(falling);

            // Play the drop sound
            URL soundURL = getClass().getResource("/sound/drop_piece.wav");
            if (soundURL != null) {
                AudioClip clip = new AudioClip(soundURL.toString());
                clip.play();
            } else {
                System.err.println("Sound file not found: /sound/drop_piece.wav");
            }

            // Main drop transition
            TranslateTransition drop = new TranslateTransition(Duration.millis(450), falling);
            drop.setToY(localPoint.getY());

            // Small bounce up
            TranslateTransition bounceUp = new TranslateTransition(Duration.millis(100), falling);
            bounceUp.setToY(localPoint.getY() - 10);

            // Settle back down
            TranslateTransition settle = new TranslateTransition(Duration.millis(100), falling);
            settle.setToY(localPoint.getY());

            // Second smaller bounce
            TranslateTransition bounceUp2 = new TranslateTransition(Duration.millis(80), falling);
            bounceUp2.setToY(localPoint.getY() - 5);

            TranslateTransition settle2 = new TranslateTransition(Duration.millis(80), falling);
            settle2.setToY(localPoint.getY());

            // Chain all together
            SequentialTransition sequence = new SequentialTransition(
                    drop, bounceUp, settle, bounceUp2, settle2
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
}
