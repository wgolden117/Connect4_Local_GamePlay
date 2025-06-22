package logic;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.util.Duration;
import ui.PlayerSettings;

import java.util.ArrayList;
import java.util.List;

public class BoardRenderer {
    private final StackPane root;
    private final int rows = 6;
    private final int cols = 7;
    private final Circle[][] circles = new Circle[rows][cols];
    private Button[] buttons;
    private final PlayerSettings playerSettings;
    private final List<Animation> rollingTransitions = new ArrayList<>();
    List<RollingPiece> rollingPieces = new ArrayList<>();

    private final Pane rollingContainer;

    public BoardRenderer(StackPane root, PlayerSettings playerSettings, Pane rollingContainer) {
        this.root = root;
        this.playerSettings = playerSettings;
        this.rollingContainer = rollingContainer;
    }

    public GridPane createGrid() {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(0, 20, 0, 20)); // top, right, bottom, left
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(2);
        grid.setVgap(2);
        grid.setMinSize(0, 0);
        grid.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        // Ensure the GridPane grows dynamically
        grid.prefWidthProperty().bind(root.widthProperty());
        grid.prefHeightProperty().bind(root.heightProperty().multiply(0.75)); // Leave room for controls

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                StackPane cell = new StackPane();
                cell.setMinSize(0, 0);
                cell.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

                Rectangle background = new Rectangle();
                background.setFill(Color.BLUE);
                background.widthProperty().bind(cell.widthProperty());
                background.heightProperty().bind(cell.heightProperty());

                Circle piece = new Circle();
                piece.setFill(Color.WHITE);
                piece.setStroke(Color.BLACK);
                piece.setStrokeWidth(5);
                piece.radiusProperty().bind(
                        Bindings.createDoubleBinding(() ->
                                        Math.min(cell.getWidth(), cell.getHeight()) * 0.4,
                                cell.widthProperty(), cell.heightProperty())
                );

                circles[row][col] = piece;

                cell.getChildren().addAll(background, piece);
                grid.add(cell, col, row);
            }

            RowConstraints rc = new RowConstraints();
            rc.setPercentHeight(100.0 / rows);
            rc.setVgrow(Priority.ALWAYS);
            grid.getRowConstraints().add(rc);
        }

        for (int col = 0; col < cols; col++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(100.0 / cols);
            cc.setHgrow(Priority.ALWAYS);
            grid.getColumnConstraints().add(cc);
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
            falling.setCache(true); // Performance optimization
            falling.setCacheHint(CacheHint.SPEED);

            // Position visually over the cell
            StackPane.setAlignment(falling, Pos.TOP_LEFT);

            Bounds targetBounds = target.localToScene(target.getBoundsInLocal());
            Point2D localPoint = root.sceneToLocal(targetBounds.getMinX(), targetBounds.getMinY());

            // Starting position (500px above)
            double startY = -500;
            double endY = localPoint.getY();
            double distance = endY - startY;
            double speed = 2.5; // pixels per millisecond
            Duration dropDuration = Duration.millis(distance / speed);

            falling.setTranslateX(localPoint.getX());
            falling.setTranslateY(startY);

            root.getChildren().add(falling);

            // Main drop
            TranslateTransition drop = new TranslateTransition(dropDuration, falling);
            drop.setToY(endY);
            drop.setInterpolator(Interpolator.EASE_OUT);

            // Bounce durations scaled down relative to screen
            double bounceOffset1 = 12;
            double bounceOffset2 = 6;
            double bounceOffset3 = 3;

            TranslateTransition bounceUp = new TranslateTransition(Duration.millis(100), falling);
            bounceUp.setToY(endY - bounceOffset1);

            TranslateTransition settle1 = new TranslateTransition(Duration.millis(100), falling);
            settle1.setToY(endY);

            TranslateTransition bounceUp2 = new TranslateTransition(Duration.millis(80), falling);
            bounceUp2.setToY(endY - bounceOffset2);

            TranslateTransition settle2 = new TranslateTransition(Duration.millis(80), falling);
            settle2.setToY(endY);

            TranslateTransition bounceUp3 = new TranslateTransition(Duration.millis(60), falling);
            bounceUp3.setToY(endY - bounceOffset3);

            TranslateTransition settle3 = new TranslateTransition(Duration.millis(60), falling);
            settle3.setToY(endY);

            // Chain all
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

    public void highlightWinningLine(List<int[]> winningCoords, Color playerColor) {
        Color flashColor = getContrastingColor(playerColor);
        List<FillTransition> transitions = new ArrayList<>();

        for (int[] pos : winningCoords) {
            int row = pos[0];
            int col = pos[1];
            Circle circle = circles[row][col];

            FillTransition flash = new FillTransition(Duration.millis(300), circle);
            flash.setFromValue(playerColor);
            flash.setToValue(flashColor);
            flash.setCycleCount(Animation.INDEFINITE);
            flash.setAutoReverse(true);
            flash.play();

            transitions.add(flash);
        }
        // Stop flashing after 5 seconds
        PauseTransition stopFlashing = new PauseTransition(Duration.seconds(6));
        stopFlashing.setOnFinished(e -> transitions.forEach(Animation::stop));
        stopFlashing.play();
    }

    private Color getContrastingColor(Color baseColor) {
        // Convert to luminance
        double luminance = 0.2126 * baseColor.getRed() +
                0.7152 * baseColor.getGreen() +
                0.0722 * baseColor.getBlue();

        // If it's a light color (like yellow), return a dark outline (e.g., black)
        if (luminance > 0.6) {
            return Color.BLACK;
        } else {
            return Color.YELLOW;
        }
    }

    public void startRollingPieceAnimation() {
        rollingTransitions.clear();
        rollingContainer.getChildren().clear();
        rollingPieces.clear();

        int totalPieces = 24;
        double radius = 30;

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
                double y = -50 - Math.random() * 150;

                piece.setLayoutX(x);
                piece.setLayoutY(y);

                piece.setOnMouseClicked(ev -> bounceRollingPiece(piece));

                rollingContainer.getChildren().add(piece);
                // Initial velocity (fall + a bit of x drift)
                RollingPiece rp = new RollingPiece(piece, (Math.random() - 0.5) * 2, 0);
                rollingPieces.add(rp);
            }

            startPhysicsLoop(radius, paneWidth, paneHeight);
        });
    }

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

    public void refreshRollingPieceColors() {
        Platform.runLater(() -> {
            for (int i = 0; i < rollingPieces.size(); i++) {
                boolean isPlayerOne = i < 12;
                Color newColor = isPlayerOne ? playerSettings.getPlayerOneColor() : playerSettings.getPlayerTwoColor();
                rollingPieces.get(i).setFill(newColor);
            }
        });
    }

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

    public void explodeRollingPiecesIntoConfetti(StackPane root) {
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

                // Arc explosion then downward fall
                Path path = new Path();
                path.getElements().add(new MoveTo(startX, startY));

                double angle = Math.toRadians(Math.random() * 360);
                double radius = 80 + Math.random() * 40;
                double midX = startX + Math.cos(angle) * radius;
                double midY = startY + Math.sin(angle) * radius;

                // Create upward burst arc
                path.getElements().add(new QuadCurveTo(
                        (startX + midX) / 2 + (Math.random() - 0.5) * 40,
                        startY - 80,
                        midX,
                        midY
                ));

                // Add falling curve to bottom
                double fallX = midX + (Math.random() - 0.5) * 60;
                double fallY = root.getHeight() + 100; // ensure it goes well below
                path.getElements().add(new QuadCurveTo(
                        (midX + fallX) / 2,
                        midY + 100,
                        fallX,
                        fallY
                ));

                PathTransition fall = new PathTransition(Duration.seconds(3 + Math.random()), path, confetti);
                fall.setInterpolator(Interpolator.EASE_IN);

                RotateTransition spin = new RotateTransition(Duration.seconds(2), confetti);
                spin.setByAngle(360 * (Math.random() < 0.5 ? 1 : -1));
                spin.setCycleCount(Animation.INDEFINITE);

                ParallelTransition explosion = new ParallelTransition(confetti, fall, spin);
                explosion.setOnFinished(e -> root.getChildren().remove(confetti));
                explosion.play();
            }
        }
        rollingPieces.clear();
    }
}
