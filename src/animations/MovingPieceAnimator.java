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
    // Piece configuration
    private static final double PIECE_RADIUS = 10;
    private static final double PIECE_STROKE_WIDTH = 4;
    private static final int TOTAL_PIECES = 24;
    private static final int PIECES_PER_PLAYER = 12;

    // Spawn position
    private static final double SPAWN_Y_OFFSET = -200;

    // Bounce animation
    private static final double BOUNCE_HEIGHT_INITIAL = 100;
    private static final double BOUNCE_DISTANCE_INITIAL = 60;
    private static final double BOUNCE_DURATION_BASE = 150;
    private static final int BOUNCE_ITERATIONS = 5;
    private static final double RANDOM_OFFSET_SCALE = 0.5;
    private static final double BOUNCE_DECAY = 0.6;

    // Physics simulation
    private static final double GRAVITY = 0.3;
    private static final double BOUNCE_ENERGY_LOSS = -0.85;
    private static final double FRICTION = 0.995;

    private final Pane rollingContainer;
    private final List<RollingPiece> rollingPieces = new ArrayList<>();
    private final PlayerSettings playerSettings;
    private static final double SPAWN_Y_RANDOM_RANGE = 100;
    private static final double BOUNCE_DURATION_INCREMENT = 30;


    /**
     * Constructs a MovingPieceAnimator.
     *
     * @param container The Pane that contains and renders rolling pieces.
     * @param settings   A shared reference to PlayerSettings, used for dynamic color updates.
     *                         This reference is expected to remain valid and shared.
     */
    public MovingPieceAnimator(Pane container, PlayerSettings settings) {
        this.rollingContainer = container;
        this.playerSettings = settings;
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

        Platform.runLater(() -> {
            double paneWidth = rollingContainer.getWidth();
            double paneHeight = rollingContainer.getHeight();

            for (int i = 0; i < TOTAL_PIECES; i++) {
                boolean isPlayerOne = i < PIECES_PER_PLAYER;
                Color color = isPlayerOne ? playerSettings.getPlayerOneColor() : playerSettings.getPlayerTwoColor();

                Circle piece = new Circle(PIECE_RADIUS, color);
                piece.setStroke(Color.BLACK);
                piece.setStrokeWidth(PIECE_STROKE_WIDTH);

                double x = Math.random() * (paneWidth - 2 * PIECE_RADIUS);
                double y = SPAWN_Y_OFFSET - Math.random() * SPAWN_Y_RANDOM_RANGE;

                piece.setLayoutX(x);
                piece.setLayoutY(y);

                piece.setOnMouseClicked(ev -> bounceRollingPiece(piece));

                rollingContainer.getChildren().add(piece);
                RollingPiece rp = new RollingPiece(piece, (Math.random() - RANDOM_OFFSET_SCALE) * 2, 0);
                rollingPieces.add(rp);
            }

            startPhysicsLoop(paneWidth, paneHeight);
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

        double bounceHeight = BOUNCE_HEIGHT_INITIAL;
        double bounceDistance = BOUNCE_DISTANCE_INITIAL;

        List<Animation> bounceSequence = new ArrayList<>();

        for (int i = 0; i < BOUNCE_ITERATIONS; i++) {
            double duration = BOUNCE_DURATION_BASE + i * BOUNCE_DURATION_INCREMENT;
            double offsetX = (Math.random() - RANDOM_OFFSET_SCALE) * bounceDistance;

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

            startX = midX;
            startY = endY;

            bounceHeight *= BOUNCE_DECAY;
            bounceDistance *= BOUNCE_DECAY;
        }

        SequentialTransition fullBounce = new SequentialTransition();
        fullBounce.getChildren().addAll(bounceSequence);
        fullBounce.play();
    }

    /**
     * Starts a physics loop that simulates gravity, friction, wall collision,
     * and simple 1D elastic collisions between pieces.
     *
     * @param paneWidth  The width of the animation container.
     * @param paneHeight The height of the animation container.
     */
    private void startPhysicsLoop(double paneWidth, double paneHeight) {
        new PhysicsLoop(MovingPieceAnimator.PIECE_RADIUS, paneWidth, paneHeight).start();
    }

    /**
     * Refreshes the color of all rolling pieces based on the current player settings.
     * Useful when players update their piece colors in the settings menu.
     */
    public void refreshRollingPieceColors() {
        Platform.runLater(() -> {
            for (int i = 0; i < rollingPieces.size(); i++) {
                boolean isPlayerOne = i < PIECES_PER_PLAYER;
                Color newColor = isPlayerOne ? playerSettings.getPlayerOneColor() : playerSettings.getPlayerTwoColor();
                rollingPieces.get(i).setFill(newColor);
            }
        });
    }

    /**
     * Handles the ongoing physics animation loop for rolling pieces.
     */
    private class PhysicsLoop extends AnimationTimer {
        private final double radius;
        private final double paneWidth;
        private final double paneHeight;

        /**
         * Constructs the PhysicsLoop with required pane bounds and piece radius.
         *
         * @param spawnRadius     Radius of the rolling pieces.
         * @param width  Width of the container pane.
         * @param height Height of the container pane.
         */
        PhysicsLoop(double spawnRadius, double width, double height) {
            this.radius = spawnRadius;
            this.paneWidth = width;
            this.paneHeight = height;
        }

        @Override
        public void handle(long now) {
            for (int i = 0; i < rollingPieces.size(); i++) {
                RollingPiece rp = rollingPieces.get(i);
                Circle c = rp.circle;

                rp.vy += GRAVITY;

                double x = c.getLayoutX() + rp.vx;
                double y = c.getLayoutY() + rp.vy;

                if (x <= radius || x >= paneWidth - radius) {
                    rp.vx *= -1;
                    x = Math.max(radius, Math.min(x, paneWidth - radius));
                }

                if (y >= paneHeight - radius) {
                    y = paneHeight - radius;
                    rp.vy *= BOUNCE_ENERGY_LOSS;
                    rp.vx *= FRICTION;
                }

                for (int j = i + 1; j < rollingPieces.size(); j++) {
                    RollingPiece other = rollingPieces.get(j);
                    Circle oc = other.circle;

                    double dx = x - oc.getLayoutX();
                    double dy = y - oc.getLayoutY();
                    double dist = Math.hypot(dx, dy);

                    if (dist < radius * 2) {
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
    }
}
