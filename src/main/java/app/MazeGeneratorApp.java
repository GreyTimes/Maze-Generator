package app;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import model.Cell;
import model.MazeGrid;
import view.MazeCanvas;
import generator.*;
import solver.BfsMazeSolver;
import solver.DfsMazeSolver;
import solver.DijkstraMazeSolver;
import game.Player;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

public class MazeGeneratorApp extends Application {

    private MazeGrid grid;
    private MazeCanvas mazeCanvas;
    private Timeline animTimeline;
    private Label statsLabel;
    private ComboBox<String> algorithmCombo;
    private ComboBox<String> solverCombo;
    private ToggleButton playModeToggle;
    private ToggleButton showSolutionToggle;
    private ToggleButton themeToggle;

    private final Player player = new Player();
    private boolean isPlayMode = false;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Customizable JavaFX Maze Generator & Solver");

        mazeCanvas = new MazeCanvas(600, 600);

        // --- Grid Size Slider ---
        Label gridSizeLabel = new Label("Grid Size: 20");
        Slider gridSizeSlider = new Slider(10, 50, 20);
        gridSizeSlider.setShowTickLabels(true);
        gridSizeSlider.setShowTickMarks(true);
        gridSizeSlider.setMajorTickUnit(10);
        gridSizeSlider.setBlockIncrement(1);
        gridSizeSlider.setSnapToTicks(true);
        gridSizeSlider.valueProperty().addListener((obs, oldVal, newVal) ->
                gridSizeLabel.setText("Grid Size: " + newVal.intValue())
        );

        // --- Animation Speed / Delay Slider ---
        Label speedLabel = new Label("Animation Delay (ms): 12");
        Slider speedSlider = new Slider(1, 50, 12);
        speedSlider.setShowTickLabels(true);
        speedSlider.setShowTickMarks(true);
        speedSlider.setMajorTickUnit(10);
        speedSlider.setBlockIncrement(1);
        speedSlider.setSnapToTicks(true);
        speedSlider.valueProperty().addListener((obs, oldVal, newVal) ->
                speedLabel.setText("Animation Delay (ms): " + newVal.intValue())
        );

        // --- Complexity / Dead-End Density Slider ---
        Label complexityLabel = new Label("Complexity / Dead-End Density: 50%");
        Slider complexitySlider = new Slider(0, 100, 50);
        complexitySlider.setShowTickLabels(true);
        complexitySlider.setShowTickMarks(true);
        complexitySlider.setMajorTickUnit(25);
        complexitySlider.setBlockIncrement(5);
        complexitySlider.setSnapToTicks(true);
        complexitySlider.valueProperty().addListener((obs, oldVal, newVal) ->
                complexityLabel.setText("Complexity / Dead-End Density: " + newVal.intValue() + "%")
        );

        algorithmCombo = new ComboBox<>();
        algorithmCombo.getItems().addAll(
                "Recursive Backtracker (DFS)",
                "Randomized Prim's",
                "Randomized Kruskal's",
                "Aldous-Broder",
                "Wilson's"
        );
        algorithmCombo.setValue("Recursive Backtracker (DFS)");
        algorithmCombo.setMaxWidth(Double.MAX_VALUE);

        solverCombo = new ComboBox<>();
        solverCombo.getItems().addAll(
                "Breadth-First Search (BFS)",
                "Depth-First Search (DFS)",
                "Dijkstra's Algorithm"
        );
        solverCombo.setValue("Breadth-First Search (BFS)");
        solverCombo.setMaxWidth(Double.MAX_VALUE);

        // Instantly update solution when solver changes and solution is currently toggled on
        solverCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (showSolutionToggle.isSelected() && grid != null) {
                solveAndDisplayPath();
            }
        });

        playModeToggle = new ToggleButton("Playable Mode: OFF");
        playModeToggle.setMaxWidth(Double.MAX_VALUE);

        themeToggle = new ToggleButton("Theme: Dark");
        themeToggle.setMaxWidth(Double.MAX_VALUE);
        themeToggle.setSelected(true);

        Button generateBtn = new Button("Generate Labyrinth");
        generateBtn.setMaxWidth(Double.MAX_VALUE);

        showSolutionToggle = new ToggleButton("Show Solution");
        showSolutionToggle.setMaxWidth(Double.MAX_VALUE);
        showSolutionToggle.setOnAction(e -> {
            if (grid == null) {
                showSolutionToggle.setSelected(false);
                return;
            }

            if (showSolutionToggle.isSelected()) {
                showSolutionToggle.setText("Hide Solution");
                solveAndDisplayPath();
            } else {
                showSolutionToggle.setText("Show Solution");
                if (mazeCanvas != null) {
                    mazeCanvas.setSolvedPath(null);
                    updateStatistics(-1);
                }
            }
        });

        Button exportBtn = new Button("Export Image");
        exportBtn.setMaxWidth(Double.MAX_VALUE);
        exportBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Maze Image");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Image (*.png)", "*.png"));
            fileChooser.setInitialFileName("maze.png");

            File file = fileChooser.showSaveDialog(primaryStage);
            if (file != null) {
                WritableImage writableImage = new WritableImage((int) mazeCanvas.getWidth(), (int) mazeCanvas.getHeight());
                mazeCanvas.snapshot(null, writableImage);
                try {
                    ImageIO.write(SwingFXUtils.fromFXImage(writableImage, null), "png", file);
                } catch (IOException ex) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to save image: " + ex.getMessage(), ButtonType.OK);
                    alert.showAndWait();
                }
            }
        });

        statsLabel = new Label("Statistics:\nTotal Cells: -\nDead Ends: -\nPath Length: -");
        statsLabel.setStyle("-fx-font-family: monospace; -fx-padding: 10;");

        VBox controls = new VBox(8,
                new Label("Gen Algorithm:"), algorithmCombo,
                new Label("Solver Algorithm:"), solverCombo,
                playModeToggle,
                themeToggle,
                gridSizeLabel, gridSizeSlider,
                speedLabel, speedSlider,
                complexityLabel, complexitySlider,
                generateBtn,
                showSolutionToggle,
                exportBtn,
                new Separator(),
                new Label("Maze Metrics"),
                statsLabel
        );
        controls.setPadding(new Insets(12));
        controls.setPrefWidth(270);

        BorderPane root = new BorderPane();
        root.setCenter(mazeCanvas);
        root.setRight(controls);

        Scene scene = new Scene(root, 900, 780);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        themeToggle.setOnAction(e -> {
            if (themeToggle.isSelected()) {
                themeToggle.setText("Theme: Dark");
                if (!scene.getStylesheets().contains(getClass().getResource("/style.css").toExternalForm())) {
                    scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
                }
                mazeCanvas.setDarkMode(true);
            } else {
                themeToggle.setText("Theme: Light");
                scene.getStylesheets().remove(getClass().getResource("/style.css").toExternalForm());
                mazeCanvas.setDarkMode(false);
            }
        });

        generateBtn.setOnAction(e -> {
            int size = (int) gridSizeSlider.getValue();
            double animDelay = speedSlider.getValue();
            double complexityFactor = complexitySlider.getValue() / 100.0;
            String selectedAlgo = algorithmCombo.getValue();

            startGenerationAnimation(size, animDelay, complexityFactor, selectedAlgo);
        });

        playModeToggle.setOnAction(e -> {
            isPlayMode = playModeToggle.isSelected();
            if (isPlayMode) {
                if (grid == null) {
                    playModeToggle.setSelected(false);
                    isPlayMode = false;
                    return;
                }
                playModeToggle.setText("Playable Mode: ON");
                player.spawn(grid);
                mazeCanvas.setPlayerCell(player.getCurrentCell());
            } else {
                playModeToggle.setText("Playable Mode: OFF");
                player.reset();
                mazeCanvas.setPlayerCell(null);
            }
        });

        scene.setOnKeyPressed(event -> {
            if (!isPlayMode || grid == null || player.getCurrentCell() == null) return;

            int dx = 0, dy = 0;
            int wallIndex = -1;

            switch (event.getCode()) {
                case UP, W -> { dy = -1; wallIndex = 0; }
                case RIGHT, D -> { dx = 1; wallIndex = 1; }
                case DOWN, S -> { dy = 1; wallIndex = 2; }
                case LEFT, A -> { dx = -1; wallIndex = 3; }
                default -> { return; }
            }

            if (player.move(dx, dy, wallIndex, grid)) {
                mazeCanvas.setPlayerCell(player.getCurrentCell());

                if (player.getCurrentCell().isExit) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Victory!");
                    alert.setHeaderText(null);
                    alert.setContentText("🎉 Congratulations! You successfully navigated through the maze in " + player.getSteps() + " steps!");
                    alert.showAndWait();

                    isPlayMode = false;
                    playModeToggle.setSelected(false);
                    playModeToggle.setText("Playable Mode: OFF");
                    player.reset();
                    mazeCanvas.setPlayerCell(null);
                }
            }
        });

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void startGenerationAnimation(int size, double animDelay, double complexityFactor, String algorithm) {
        if (animTimeline != null) animTimeline.stop();

        grid = new MazeGrid(size, size);
        mazeCanvas.setGrid(grid);
        mazeCanvas.setSolvedPath(null);
        updateStatistics(-1);
        player.reset();
        mazeCanvas.setPlayerCell(null);

        if (showSolutionToggle != null) {
            showSolutionToggle.setSelected(false);
            showSolutionToggle.setText("Show Solution");
        }

        MazeGenerator generator = switch (algorithm) {
            case "Randomized Prim's" -> new PrimsMazeGenerator();
            case "Randomized Kruskal's" -> new KruskalsMazeGenerator();
            case "Aldous-Broder" -> new AldousBroderMazeGenerator();
            case "Wilson's" -> new WilsonsMazeGenerator();
            default -> new DfsMazeGenerator();
        };

        generator.initialize(grid);

        animTimeline = new Timeline();
        KeyFrame keyFrame = new KeyFrame(Duration.millis(animDelay), event -> {
            boolean running = generator.step();
            mazeCanvas.setCurrentHead(generator.getCurrentHead());

            if (!running) {
                animTimeline.stop();
                applyComplexityFactor(grid, complexityFactor);
                createOpenings(grid);
                mazeCanvas.setCurrentHead(null);

                // If solution toggle was left active, refresh solution automatically when generation completes
                if (showSolutionToggle.isSelected()) {
                    solveAndDisplayPath();
                } else {
                    updateStatistics(-1);
                }

                if (isPlayMode) {
                    player.spawn(grid);
                    mazeCanvas.setPlayerCell(player.getCurrentCell());
                }
            }
        });

        animTimeline.getKeyFrames().add(keyFrame);
        animTimeline.setCycleCount(Timeline.INDEFINITE);
        animTimeline.play();
    }

    private void solveAndDisplayPath() {
        if (grid == null) return;

        String selectedSolver = solverCombo.getValue();
        List<Cell> path = switch (selectedSolver) {
            case "Depth-First Search (DFS)" -> DfsMazeSolver.solve(grid);
            case "Dijkstra's Algorithm" -> DijkstraMazeSolver.solve(grid);
            default -> BfsMazeSolver.solve(grid);
        };

        if (path != null && !path.isEmpty()) {
            mazeCanvas.setSolvedPath(path);
            updateStatistics(path.size());
        } else {
            mazeCanvas.setSolvedPath(null);
            updateStatistics(-1);
        }
    }

    private void updateStatistics(int pathLength) {
        if (grid == null) return;
        int totalCells = grid.cols * grid.rows;
        int deadEnds = 0;

        for (int x = 0; x < grid.cols; x++) {
            for (int y = 0; y < grid.rows; y++) {
                Cell cell = grid.cells[x][y];
                int wallCount = 0;
                for (boolean w : cell.walls) if (w) wallCount++;
                if (wallCount == 3) deadEnds++;
            }
        }

        String pathStr = (pathLength > 0) ? pathLength + " steps" : "Hidden / Not solved";
        statsLabel.setText(String.format("Statistics:\nTotal Cells: %d\nDead Ends: %d\nPath Length: %s",
                totalCells, deadEnds, pathStr));
    }

    private void applyComplexityFactor(MazeGrid grid, double complexityFactor) {
        Random rand = new Random();
        double removalChance = 1.0 - complexityFactor;

        for (int x = 0; x < grid.cols; x++) {
            for (int y = 0; y < grid.rows; y++) {
                Cell cell = grid.cells[x][y];
                int wallCount = 0;
                for (boolean w : cell.walls) if (w) wallCount++;

                if (wallCount == 3 && rand.nextDouble() < removalChance) {
                    List<Cell> adj = new java.util.ArrayList<>();
                    int[][] dirs = {{0, -1}, {1, 0}, {0, 1}, {-1, 0}};
                    for (int[] d : dirs) {
                        Cell n = grid.getCell(cell.x + d[0], cell.y + d[1]);
                        if (n != null) adj.add(n);
                    }
                    if (!adj.isEmpty()) {
                        grid.removeWalls(cell, adj.get(rand.nextInt(adj.size())));
                    }
                }
            }
        }
    }

    private void createOpenings(MazeGrid grid) {
        Random rand = new Random();

        int entranceY = rand.nextInt(grid.rows);
        Cell entranceCell = grid.getCell(0, entranceY);
        entranceCell.walls[3] = false;
        entranceCell.isEntrance = true;

        int exitY = rand.nextInt(grid.rows);
        Cell exitCell = grid.getCell(grid.cols - 1, exitY);
        exitCell.walls[1] = false;
        exitCell.isExit = true;
    }

    public static void main(String[] args) {
        launch(args);
    }
}