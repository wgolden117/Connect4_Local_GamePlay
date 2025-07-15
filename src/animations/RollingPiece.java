package animations;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

/**
 * Represents a single animated game piece with horizontal and vertical velocity,
 * used for rolling and bouncing effects on the main menu.
 */
public class RollingPiece {
    Circle circle;
    double vx;  // horizontal velocity
    double vy;  // vertical velocity

    /**
     * Constructs a RollingPiece with a visual circle and initial velocities.
     *
     * @param circle The Circle shape representing the piece.
     * @param vx     Initial horizontal velocity.
     * @param vy     Initial vertical velocity.
     */
    RollingPiece(Circle circle, double vx, double vy) {
        this.circle = circle;
        this.vx = vx;
        this.vy = vy;
    }

    /**
     * Sets the fill color of the rolling piece.
     *
     * @param color The new Color to apply.
     */
    public void setFill(Color color) {
        circle.setFill(color);
    }
}
