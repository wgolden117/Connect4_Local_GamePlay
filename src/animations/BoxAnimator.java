package animations;

import java.util.Objects;
import javafx.animation.*;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BoxAnimator {
    private final ImageView boxImage;
    private final Pane boxPane;
    private final List<RollingPiece> rollingPieces = new ArrayList<>();
    private final Random random = new Random();

    public BoxAnimator(Pane boxPane) {
        this.boxPane = boxPane;
        this.boxImage = new ImageView(new Image(Objects.requireNonNull(getClass().getResource("/images/cardboard_box.png")).toExternalForm()));
        boxImage.setFitWidth(200);
        boxImage.setPreserveRatio(true);
        boxImage.setTranslateX(20);
        boxImage.setTranslateY(-50);
        boxPane.getChildren().add(boxImage);
    }

    public void playAnimation(Runnable onRollingPiecesStart, Runnable onFinished) {
        RotateTransition tipBox = new RotateTransition(Duration.seconds(0.6), boxImage);
        tipBox.setByAngle(180);
        tipBox.setCycleCount(1);

        tipBox.setOnFinished(e -> {
            if (onRollingPiecesStart != null) {
                onRollingPiecesStart.run();  // Start the rolling pieces
            }

            // Slight delay before loading board
            PauseTransition pause = new PauseTransition(Duration.seconds(1.5));
            pause.setOnFinished(ev -> {
                boxImage.setVisible(false);
                if (onFinished != null) {
                    onFinished.run();  // Load game board
                }
            });
            pause.play();
        });

        new SequentialTransition(tipBox).play();
    }


    public Node getBoxNode() {
        return boxImage;
    }

    private void spawnRollingPieces() {
        rollingPieces.clear();
        int NUM_PIECES = 8;
        for (int i = 0; i < NUM_PIECES; i++) {
            Circle circle = new Circle(10);
            circle.setFill(getRandomColor());

            // Start near the box (bottom-left)
            circle.setTranslateX(50);
            circle.setTranslateY(200);

            double vx = random.nextDouble() * 2 + 1;  // 1.0–3.0
            double vy = -random.nextDouble() * 5 - 2; // -2.0 to -7.0 (upward)

            RollingPiece rp = new RollingPiece(circle, vx, vy);
            rollingPieces.add(rp);
            boxPane.getChildren().add(circle);
        }
    }

    private void startRollingAnimation() {
        final double gravity = 0.3;
        final double bounceFactor = 0.6;
        final double floorY = 250;

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                for (RollingPiece rp : rollingPieces) {
                    Circle c = rp.circle;
                    rp.vy += gravity;
                    c.setTranslateX(c.getTranslateX() + rp.vx);
                    c.setTranslateY(c.getTranslateY() + rp.vy);

                    if (c.getTranslateY() >= floorY) {
                        c.setTranslateY(floorY);
                        rp.vy = -rp.vy * bounceFactor;

                        if (Math.abs(rp.vy) < 1) {
                            rp.vy = 0;
                        }
                    }
                }
            }
        };
        timer.start();

        PauseTransition stop = new PauseTransition(Duration.seconds(1.5));
        stop.setOnFinished(e -> timer.stop());
        stop.play();
    }

    private Color getRandomColor() {
        Color[] colors = { Color.RED, Color.YELLOW, Color.BLUE, Color.GREEN, Color.ORANGE };
        return colors[random.nextInt(colors.length)];
    }
}
