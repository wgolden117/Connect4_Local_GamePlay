package ui;

import javafx.animation.*;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.shape.Path;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.QuadCurveTo;
import javafx.animation.PathTransition;
import javafx.animation.RotateTransition;

import javafx.util.Duration;
import logic.*;

import java.util.*;

public class BoardLayout {
    private final GameLogic gameLogic;
    private final PlayerSettings playerSettings;
    private final List<Animation> activeConfettiAnimations = new ArrayList<>();
    private final Random random = new Random();

    public BoardLayout(GameLogic gameLogic, PlayerSettings playerSettings) {
        this.gameLogic = gameLogic;
        this.playerSettings = playerSettings;
    }

    public Optional<StackPane> createBoardLayout(String labelText, GameController controller) {
        StackPane root = new StackPane(); // For animation overlay
        root.setPrefSize(800, 800);

        BoardRenderer boardRenderer = new BoardRenderer(root);

        GridPane grid = boardRenderer.createGrid();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER);
        grid.setMaxSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);

        // Reactive color updates
        playerSettings.playerOneColorProperty().addListener((obs, oldColor, newColor) ->
                boardRenderer.refreshColors(gameLogic.getBoard(),
                        playerSettings.getPlayerOneColor(),
                        playerSettings.getPlayerTwoColor())
        );
        playerSettings.playerTwoColorProperty().addListener((obs, oldColor, newColor) ->
                boardRenderer.refreshColors(gameLogic.getBoard(),
                        playerSettings.getPlayerOneColor(),
                        playerSettings.getPlayerTwoColor())
        );

        // Menu bar
        MenuFactory menuFactory = new MenuFactory(controller::closeApplication, controller.getStage(), playerSettings, controller);
        MenuBar menuBar = menuFactory.createMenuBar();

        // Title label + grid in center VBox
        Label label = new Label(" " + labelText + " ");
        label.setFont(Font.font("Ariel", FontWeight.BOLD, FontPosture.ITALIC, 22));
        VBox centerBox = new VBox(50);
        centerBox.setAlignment(Pos.TOP_CENTER);
        VBox.setMargin(label, new Insets(40, 0, 0, 0)); // top/right/bottom/left padding
        centerBox.getChildren().addAll(label, grid);

        // Wrap with BorderPane to pin the menuBar to the top
        BorderPane layout = new BorderPane();
        layout.setTop(menuBar);      // MenuBar stays fixed at top
        layout.setCenter(centerBox); // Game content goes center

        // Add layout to root (for animation layering)
        root.getChildren().add(layout);

        // Setup buttons
        Button[] buttons = new Button[7];
        if (labelText.equals("Player vs. Player")) {
            setupPlayerVsPlayer(grid, controller, labelText, buttons);
        } else {
            ChoiceDialog<String> dialog = new ChoiceDialog<>("Easy", "Easy", "Medium", "Hard");
            dialog.setTitle("AI Difficulty");
            dialog.setHeaderText("Select AI Difficulty");
            dialog.setContentText("Difficulty:");
            Optional<String> result = dialog.showAndWait();

            if (result.isEmpty()) return Optional.empty();

            String aiDifficulty = result.get();
            AIPlayer aiPlayer = new AIPlayer(gameLogic, aiDifficulty, 2);
            controller.setAIPlayer(aiPlayer);

            setupPlayerVsAI(grid, controller, labelText, buttons);
        }

        boardRenderer.setButtons(buttons);
        controller.setBoardRenderer(boardRenderer);
        controller.setConfettiHandlers(() -> startConfettiAnimation(root), this::stopConfettiAnimation);

        return Optional.of(root);
    }

    private void setupPlayerVsPlayer(GridPane grid, GameController controller, String labelText, Button[] buttons) {
        for (int col = 0; col < 7; col++) {
            Button button = new Button("Drop");
            int finalCol = col;
            button.setOnAction(e -> controller.dropPiece(finalCol, labelText));
            buttons[col] = button;
            grid.add(button, col, 6); // Buttons row
            GridPane.setHalignment(button, HPos.CENTER);

            // Add number label below each button
            Label numberLabel = new Label(String.valueOf(col + 1));
            numberLabel.setStyle("-fx-font-weight: bold;");
            numberLabel.setFont(Font.font("Arial", FontWeight.BOLD, 30));
            grid.add(numberLabel, col, 7); // Row 7 for numbers
            GridPane.setHalignment(numberLabel, HPos.CENTER);
        }
    }

    private void setupPlayerVsAI(GridPane grid, GameController controller, String labelText, Button[] buttons) {
        for (int col = 0; col < 7; col++) {
            Button button = new Button("Drop");
            int finalCol = col;
            button.setOnAction(e -> controller.dropPiece(finalCol, labelText));
            buttons[col] = button;
            grid.add(button, col, 6);
            GridPane.setHalignment(button, HPos.CENTER);

            Label numberLabel = new Label(String.valueOf(col + 1));
            numberLabel.setFont(Font.font("Arial", FontWeight.BOLD, 30));
            numberLabel.setStyle("-fx-font-weight: bold;");
            grid.add(numberLabel, col, 7);
            GridPane.setHalignment(numberLabel, HPos.CENTER);
        }
    }

    public void startConfettiAnimation(StackPane root) {
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(250), e -> {
            for (int i = 0; i < 5; i++) { // Drop 5 confetti at once per tick
                double width = 4 + random.nextDouble() * 4;
                double height = 8 + random.nextDouble() * 4;
                Rectangle confetti = new Rectangle(width, height, Color.color(random.nextDouble(), random.nextDouble(), random.nextDouble()));
                confetti.setTranslateX(random.nextDouble() * root.getWidth() - root.getWidth() / 2);
                confetti.setTranslateY(-root.getHeight() / 2 - 20 - random.nextDouble() * 30); // Start above top

                root.getChildren().add(confetti);

                // Crazy falling path
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


    public void stopConfettiAnimation() {
        activeConfettiAnimations.forEach(Animation::stop);
        activeConfettiAnimations.clear();
    }
}
