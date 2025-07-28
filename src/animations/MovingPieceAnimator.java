package animations;

import javafx.animation.Animation;
import javafx.animation.AnimationTimer;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import ui.PlayerSettings;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles the animation and physics simulation of rolling pieces on the main menu screen.
 * Includes bounce interactions on click and dynamic motion.
 */
public class MovingPieceAnimator {
    private final Pane rollingContainer;
    private final List<RollingPiece> rollingPieces = new ArrayList<>();
    private final PlayerSettings playerSettings;

    /**
     * Constructs a MovingPieceAnimator.
     *
     * @param rollingContainer The Pane that contains and renders rolling pieces.
     * @param playerSettings   PlayerSettings used for determining piece colors.
     */
    public MovingPieceAnimator(Pane rollingContainer, PlayerSettings playerSettings) {
        this.rollingContainer = rollingContainer;
        this.playerSettings = playerSettings;
    }

    /**
     * Returns the list of current rolling pieces.
     *
     * @return List of RollingPiece objects.
     */
    public List<RollingPiece> getRollingPieces() {
        return List.copyOf(rollingPieces);
    }

    /**
     * Starts the rolling animation by generating and dropping pieces into the container.
     * Each piece is assigned to a player color and is clickable for bounce effects.
     */
    public void startRollingPieceAnimation() {
        rollingContainer.getChildren().clear();
        rollingPieces.clear();

        int totalPieces = 24;
        double radius = 10
                ;

        Platform.runLater(() -> {
            double paneWidth = rollingContainer.getWidth();
            double paneHeight = rollingContainer.getHeight();

            for (int i = 0; i < totalPieces; i++) {
                boolean isPlayerOne = i < 12;
                Color color = isPlayerOne ? playerSettings.getPlayerOneColor() : playerSettings.getPlayerTwoColor();

                Circle piece = new Circle(radius, color);
                piece.setStroke(Color.BLACK);
                piece.setStrokeWidth(4);

                double x = Math.random() * (paneWidth - 2 * radius);
                double y = -200 - Math.random() * 100; // Spawn well above visible pane

                piece.setLayoutX(x);
                piece.setLayoutY(y);

                piece.setOnMouseClicked(ev -> bounceRollingPiece(piece));

                rollingContainer.getChildren().add(piece);
                RollingPiece rp = new RollingPiece(piece, (Math.random() - 0.5) * 2, 0);
                rollingPieces.add(rp);
            }

            startPhysicsLoop(radius, paneWidth, paneHeight);

            // Refresh colors now that pieces are created
            refreshRollingPieceColors();
        });
    }

    /**
     * Bounces a rolling piece upward and forward in a decreasing bounce sequence.
     *
     * @param piece The Circle to animate.
     */
    private void bounceRollingPiece(Circle piece) {
        double startX = piece.getLayoutX();
        double startY = piece.getLayoutY();

        double bounceHeight = 100;
        double bounceDistance = 60;
        double baseDuration = 150;

        List<Animation> bounceSequence = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            double duration = baseDuration + i * 30;
            double offsetX = (Math.random() - 0.5) * bounceDistance;

            double midX = startX + offsetX;
            double midY = startY - bounceHeight;

            double endY = startY;

            KeyValue kvUpX = new KeyValue(piece.layoutXProperty(), midX, Interpolator.EASE_OUT);
            KeyValue kvUpY = new KeyValue(piece.layoutYProperty(), midY, Interpolator.EASE_OUT);

            KeyValue kvDownX = new KeyValue(piece.layoutXProperty(), midX, Interpolator.EASE_IN);
            KeyValue kvDownY = new KeyValue(piece.layoutYProperty(), endY, Interpolator.EASE_IN);

            KeyFrame kfUp = new KeyFrame(Duration.millis(duration), kvUpX, kvUpY);
            KeyFrame kfDown = new KeyFrame(Duration.millis(duration * 2), kvDownX, kvDownY);

            Timeline bounce = new Timeline(kfUp, kfDown);
            bounceSequence.add(bounce);

            // Update starting point for next bounce
            startX = midX;
            startY = endY;

            bounceHeight *= 0.6;
            bounceDistance *= 0.6;
        }

        SequentialTransition fullBounce = new SequentialTransition();
        fullBounce.getChildren().addAll(bounceSequence);
        fullBounce.play();
    }

    /**
     * Starts a physics loop that simulates gravity, friction, wall collision,
     * and simple 1D elastic collisions between pieces.
     */
    private void startPhysicsLoop(double radius, double paneWidth, double paneHeight) {
        final double gravity = 0.3;       // Slower descent = longer bounces
        final double friction = 0.995;    // Slower sideways decay = longer motion
        final double bounceLoss = 0.85;   // Higher = more energy retained after bounce
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                for (int i = 0; i < rollingPieces.size(); i++) {
                    RollingPiece rp = rollingPieces.get(i);
                    Circle c = rp.circle;

                    rp.vy += gravity;

                    // Update position
                    double x = c.getLayoutX() + rp.vx;
                    double y = c.getLayoutY() + rp.vy;

                    // Wall collision
                    if (x <= radius || x >= paneWidth - radius) {
                        rp.vx *= -1;
                        x = Math.max(radius, Math.min(x, paneWidth - radius));
                    }
                    if (y >= paneHeight - radius) {
                        y = paneHeight - radius;
                        rp.vy *= -bounceLoss;
                        rp.vx *= friction;
                    }

                    // Collision with other pieces
                    for (int j = i + 1; j < rollingPieces.size(); j++) {
                        RollingPiece other = rollingPieces.get(j);
                        Circle oc = other.circle;

                        double dx = x - oc.getLayoutX();
                        double dy = y - oc.getLayoutY();
                        double dist = Math.hypot(dx, dy);

                        if (dist < radius * 2) {
                            // Simple 1D elastic collision
                            double tempVx = rp.vx;
                            double tempVy = rp.vy;
                            rp.vx = other.vx;
                            rp.vy = other.vy;
                            other.vx = tempVx;
                            other.vy = tempVy;
                        }
                    }
                    c.setLayoutX(x);
                    c.setLayoutY(y);
                }
            }
        };
        timer.start();
    }

    /**
     * Refreshes the color of all rolling pieces based on the current player settings.
     * Useful when players update their piece colors in the settings menu.
     */
    public void refreshRollingPieceColors() {
        Platform.runLater(() -> {
            for (int i = 0; i < rollingPieces.size(); i++) {
                boolean isPlayerOne = i < 12;
                Color newColor = isPlayerOne ? playerSettings.getPlayerOneColor() : playerSettings.getPlayerTwoColor();
                rollingPieces.get(i).setFill(newColor);
            }
        });
    }
}
