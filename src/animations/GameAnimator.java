package animations;

import javafx.animation.Interpolator;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
/**
 * GameAnimator is responsible for animating Connect 4 game piece drops.
 * It creates a visual effect of a game piece falling from the top of the screen
 * to its destination on the game board.
 */
public final class GameAnimator {
    private final StackPane root;
    private final Circle[][] circles;
    /**
     * Constructs a GameAnimator instance.
     *
     * @param root    The root StackPane that contains all game visuals.
     * @param circles A 2D array representing the visual pieces on the game board.
     * @throws IllegalArgumentException if the root pane is null.
     */
    public GameAnimator(StackPane root, Circle[][] circles) {
        if (root == null) throw new IllegalArgumentException("Root StackPane cannot be null");
        this.root = root;
        this.circles = new Circle[circles.length][];
        for (int i = 0; i < circles.length; i++) {
            this.circles[i] = circles[i].clone(); // shallow copy per row
        }
    }

    /**
     * Animates a falling piece into the specified board location.
     *
     * @param col      The column index of the board where the piece will land.
     * @param row      The row index of the board where the piece will land.
     * @param color    The color of the falling game piece.
     * @param onFinish A callback to execute after the animation completes.
     */
    public void animateDrop(int col, int row, Color color, Runnable onFinish) {
        if (row < 0 || row >= circles.length || col < 0 || col >= circles[0].length) {
            System.err.printf("Invalid drop position: row=%d, col=%d%n", row, col);
            return;
        }

        Circle target = circles[row][col];
        if (target == null) {
            System.err.printf("Target circle is null at row=%d, col=%d%n", row, col);
            return;
        }
        // Defer to next pulse to ensure layout is valid
        Platform.runLater(() -> {
            if (root.getScene() == null) {
                // Wait another pulse if scene not attached yet
                Platform.runLater(() -> animateDrop(col, row, color, onFinish));
                return;
            }

            Circle falling = new Circle(target.getRadius(), color);
            falling.setStroke(Color.BLACK);
            falling.setCache(true);
            falling.setCacheHint(CacheHint.SPEED);
            StackPane.setAlignment(falling, Pos.TOP_LEFT);

            Bounds targetBounds = target.localToScene(target.getBoundsInLocal());
            Point2D localPoint = root.sceneToLocal(targetBounds.getMinX(), targetBounds.getMinY());

            double startY = -500;
            double endY = localPoint.getY();
            double distance = endY - startY;
            double speed = 2.5;
            Duration dropDuration = Duration.millis(distance / speed);

            falling.setTranslateX(localPoint.getX());
            falling.setTranslateY(startY);
            root.getChildren().add(falling);

            // Bounce animation sequence
            TranslateTransition drop = new TranslateTransition(dropDuration, falling);
            drop.setToY(endY);
            drop.setInterpolator(Interpolator.EASE_OUT);

            TranslateTransition bounceUp = new TranslateTransition(Duration.millis(100), falling);
            bounceUp.setToY(endY - 12);

            TranslateTransition settle1 = new TranslateTransition(Duration.millis(100), falling);
            settle1.setToY(endY);

            TranslateTransition bounceUp2 = new TranslateTransition(Duration.millis(80), falling);
            bounceUp2.setToY(endY - 6);

            TranslateTransition settle2 = new TranslateTransition(Duration.millis(80), falling);
            settle2.setToY(endY);

            TranslateTransition bounceUp3 = new TranslateTransition(Duration.millis(60), falling);
            bounceUp3.setToY(endY - 3);

            TranslateTransition settle3 = new TranslateTransition(Duration.millis(60), falling);
            settle3.setToY(endY);

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
}