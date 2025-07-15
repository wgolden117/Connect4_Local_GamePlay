package animations;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class RollingPiece {
    Circle circle;
    double vx;  // horizontal velocity
    double vy;  // vertical velocity

    RollingPiece(Circle circle, double vx, double vy) {
        this.circle = circle;
        this.vx = vx;
        this.vy = vy;
    }

    public void setFill(Color color) {
        circle.setFill(color);
    }
}
