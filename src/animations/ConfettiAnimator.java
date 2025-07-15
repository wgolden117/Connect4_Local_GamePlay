package animations;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javafx.animation.Animation;
import javafx.animation.ParallelTransition;
import javafx.animation.PathTransition;
import javafx.animation.RotateTransition;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.animation.Interpolator;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.QuadCurveTo;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class ConfettiAnimator {

    private final StackPane root;
    private final Pane rollingContainer;
    private final List<RollingPiece> rollingPieces;
    private final List<Animation> activeConfettiAnimations = new ArrayList<>();
    private final Random random = new Random();
    private final Pane confettiPane;
    private Timeline confettiTimeline;

    public ConfettiAnimator(StackPane root, Pane rollingContainer, List<RollingPiece> rollingPieces) {
        this.root = root;
        this.confettiPane = rollingContainer;
        this.rollingContainer = rollingContainer;
        this.rollingPieces = rollingPieces;
    }

    public void startConfettiAnimation() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(250), e -> {
            for (int i = 0; i < 5; i++) {
                double width = 4 + random.nextDouble() * 4;
                double height = 8 + random.nextDouble() * 4;
                Rectangle confetti = new Rectangle(width, height, Color.color(random.nextDouble(), random.nextDouble(), random.nextDouble()));
                confetti.setTranslateX(random.nextDouble() * root.getWidth() - root.getWidth() / 2);
                confetti.setTranslateY(-root.getHeight() / 2 - 20 - random.nextDouble() * 30);

                root.getChildren().add(confetti);

                Path path = new Path();
                path.getElements().add(new MoveTo(confetti.getTranslateX(), confetti.getTranslateY()));
                double controlX = confetti.getTranslateX() + (random.nextDouble() - 0.5) * 100;
                double controlY = root.getHeight() / 2 + random.nextDouble() * 100;
                double endX = confetti.getTranslateX() + (random.nextDouble() - 0.5) * 100;
                double endY = root.getHeight();

                path.getElements().add(new QuadCurveTo(controlX, controlY, endX, endY));

                PathTransition fall = new PathTransition(Duration.seconds(3 + random.nextDouble()), path, confetti);
                fall.setInterpolator(Interpolator.EASE_OUT);

                RotateTransition spin = new RotateTransition(Duration.seconds(3), confetti);
                spin.setByAngle(360);
                spin.setCycleCount(Animation.INDEFINITE);

                ParallelTransition drop = new ParallelTransition(confetti, fall, spin);
                drop.setOnFinished(ev -> root.getChildren().remove(confetti));
                drop.play();

                activeConfettiAnimations.add(drop);
            }
        }));

        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
        activeConfettiAnimations.add(timeline);
    }

    public void explodeRollingPiecesIntoConfetti() {
        for (RollingPiece rp : rollingPieces) {
            Circle circle = rp.circle;
            double startX = circle.getLayoutX();
            double startY = circle.getLayoutY();
            rollingContainer.getChildren().remove(circle);

            for (int i = 0; i < 10; i++) {
                Rectangle confetti = new Rectangle(4, 8);
                confetti.setFill(circle.getFill());
                confetti.setTranslateX(startX);
                confetti.setTranslateY(startY);

                root.getChildren().add(confetti);

                Path path = new Path();
                path.getElements().add(new MoveTo(startX, startY));

                double angle = Math.toRadians(random.nextDouble() * 360);
                double radius = 80 + random.nextDouble() * 40;
                double midX = startX + Math.cos(angle) * radius;
                double midY = startY + Math.sin(angle) * radius;

                path.getElements().add(new QuadCurveTo(
                        (startX + midX) / 2 + (random.nextDouble() - 0.5) * 40,
                        startY - 80,
                        midX,
                        midY
                ));

                double fallX = midX + (random.nextDouble() - 0.5) * 60;
                double fallY = root.getHeight() + 100;
                path.getElements().add(new QuadCurveTo(
                        (midX + fallX) / 2,
                        midY + 100,
                        fallX,
                        fallY
                ));

                PathTransition fall = new PathTransition(Duration.seconds(3 + random.nextDouble()), path, confetti);
                fall.setInterpolator(Interpolator.EASE_IN);

                RotateTransition spin = new RotateTransition(Duration.seconds(2), confetti);
                spin.setByAngle(360 * (random.nextBoolean() ? 1 : -1));
                spin.setCycleCount(Animation.INDEFINITE);

                ParallelTransition explosion = new ParallelTransition(confetti, fall, spin);
                explosion.setOnFinished(e -> root.getChildren().remove(confetti));
                explosion.play();
            }
        }

        rollingPieces.clear();
    }

    public void stopConfettiAnimation() {
        if (confettiTimeline != null) {
            confettiTimeline.stop();
        }
        confettiPane.getChildren().clear();
    }
}
