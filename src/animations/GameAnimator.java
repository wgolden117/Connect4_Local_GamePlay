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

public class GameAnimator {
    private final StackPane root;
    private final Circle[][] circles;

    public GameAnimator(StackPane root, Circle[][] circles) {
        if (root == null) throw new IllegalArgumentException("Root StackPane cannot be null");
        this.root = root;
        this.circles = circles;
    }

    public void animateDrop(int col, int row, Color color, Runnable onFinish) {
        Circle target = circles[row][col];

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