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

    public void playAnimation(Runnable onFinished) {
        // 1. Small initial wobble (optional realism)
        RotateTransition wobbleLeft = new RotateTransition(Duration.seconds(0.2), boxImage);
        wobbleLeft.setByAngle(-10);
        RotateTransition wobbleRight = new RotateTransition(Duration.seconds(0.2), boxImage);
        wobbleRight.setByAngle(20);
        RotateTransition wobbleCenter = new RotateTransition(Duration.seconds(0.1), boxImage);
        wobbleCenter.setByAngle(-10);

        // 2. Fall and rotate
        RotateTransition rotateFall = new RotateTransition(Duration.seconds(0.5), boxImage);
        rotateFall.setByAngle(90); // Rotate to side

        TranslateTransition fall = new TranslateTransition(Duration.seconds(0.5), boxImage);
        fall.setByY(80); // Drop down (adjust for realism)

        ParallelTransition fallAndRotate = new ParallelTransition(rotateFall, fall);

        // 3. Small bounce after hitting "floor"
        TranslateTransition bounceUp = new TranslateTransition(Duration.seconds(0.2), boxImage);
        bounceUp.setByY(-10);
        TranslateTransition bounceDown = new TranslateTransition(Duration.seconds(0.2), boxImage);
        bounceDown.setByY(10);

        SequentialTransition bounce = new SequentialTransition(bounceUp, bounceDown);

        // 4. Roll out pieces after tip
        fallAndRotate.setOnFinished(e -> {
            spawnRollingPieces();
            startRollingAnimation();
        });

        // 5. Run user code after animation
        bounce.setOnFinished(e -> {
            PauseTransition pause = new PauseTransition(Duration.seconds(1.0));
            pause.setOnFinished(ev -> {
                boxImage.setVisible(false);
                if (onFinished != null) {
                    onFinished.run();
                }
            });
            pause.play();
        });

        // Full sequence
        SequentialTransition full = new SequentialTransition(
                wobbleLeft, wobbleRight, wobbleCenter,
                fallAndRotate,
                bounce
        );
        full.play();
    }


    private void spawnRollingPieces() {
        rollingPieces.clear();
        int NUM_PIECES = 14;
        for (int i = 0; i < NUM_PIECES; i++) {
            Circle circle = new Circle(10);
            circle.setFill(getRandomColor());

            // Position near the tip of the box (bottom-left)
            circle.setTranslateX(boxImage.getTranslateX() + 80);
            circle.setTranslateY(boxImage.getTranslateY() + boxImage.getFitHeight() - 10);

            double vx = random.nextDouble() * 3 + 1;  // Horizontal burst
            double vy = -random.nextDouble() * 5 - 2; // Initial upward force

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

    public Node getBoxNode() {
        return boxImage;
    }

    private Color getRandomColor() {
        Color[] colors = { Color.RED, Color.YELLOW, Color.BLUE, Color.GREEN, Color.ORANGE };
        return colors[random.nextInt(colors.length)];
    }
}
