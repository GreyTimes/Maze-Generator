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

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class MazeGeneratorApp extends Application {

    private MazeGrid grid;
    private MazeCanvas mazeCanvas;
    private Timeline animTimeline;
    private Label statsLabel;
    private ComboBox<String> algorithmCombo;
    private ToggleButton playModeToggle;
    private ToggleButton showSolutionToggle;
    private ToggleButton themeToggle;
    private boolean isPlayMode = false;
    private Cell playerCell = null;
    private int playerSteps = 0;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Customizable JavaFX Maze Generator & Solver");

        mazeCanvas = new MazeCanvas(600, 600);

        // --- Sliders with Live Value Labels & Integer Snapping ---
        Label widthLabel = new Label("Width (Cols): 15");
        Slider widthSlider = new Slider(5, 40, 15);
        widthSlider.setShowTickLabels(true);
        widthSlider.setShowTickMarks(true);
        widthSlider.setMajorTickUnit(5);
        widthSlider.setBlockIncrement(1);
        widthSlider.setSnapToTicks(true);
        widthSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            widthLabel.setText("Width (Cols): " + newVal.intValue());
        });

        Label heightLabel = new Label("Height (Rows): 15");
        Slider heightSlider = new Slider(5, 40, 15);
        heightSlider.setShowTickLabels(true);
        heightSlider.setShowTickMarks(true);
        heightSlider.setMajorTickUnit(5);
        heightSlider.setBlockIncrement(1);
        heightSlider.setSnapToTicks(true);
        heightSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            heightLabel.setText("Height (Rows): " + newVal.intValue());
        });

        Label braidLabel = new Label("Loop / Braid Factor (%): 20%");
        Slider braidSlider = new Slider(0, 100, 20);
        braidSlider.setShowTickLabels(true);
        braidSlider.setShowTickMarks(true);
        braidSlider.setMajorTickUnit(25);
        braidSlider.setBlockIncrement(5);
        braidSlider.setSnapToTicks(true);
        braidSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            braidLabel.setText("Loop / Braid Factor (%): " + newVal.intValue() + "%");
        });

        algorithmCombo = new ComboBox<>();
        algorithmCombo.getItems().addAll(
                "Recursive Backtracker (DFS)",
                "Randomized Prim's",
                "Randomized Kruskal's"
        );
        algorithmCombo.setValue("Recursive Backtracker (DFS)");
        algorithmCombo.setMaxWidth(Double.MAX_VALUE);

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
            if (showSolutionToggle.isSelected()) {
                showSolutionToggle.setText("Hide Solution");
                runBfsSolver();
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

        statsLabel = new Label("Statistics:\n• Total Cells: -\n• Dead Ends: -\n• Path Length: -");
        statsLabel.setStyle("-fx-font-family: monospace; -fx-padding: 10;");

        // Cleaned control panel layout without spinners
        VBox controls = new VBox(10,
                new Label("Algorithm:"), algorithmCombo,
                playModeToggle,
                themeToggle,
                widthLabel, widthSlider,
                heightLabel, heightSlider,
                braidLabel, braidSlider,
                generateBtn,
                showSolutionToggle,
                exportBtn,
                new Separator(),
                new Label("Maze Metrics"),
                statsLabel
        );
        controls.setPadding(new Insets(15));
        controls.setPrefWidth(250);

        BorderPane root = new BorderPane();
        root.setCenter(mazeCanvas);
        root.setRight(controls);

        Scene scene = new Scene(root, 880, 750);
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
            int cols = (int) widthSlider.getValue();
            int rows = (int) heightSlider.getValue();
            double braidFactor = braidSlider.getValue() / 100.0;
            String selectedAlgo = algorithmCombo.getValue();

            startGenerationAnimation(cols, rows, braidFactor, selectedAlgo);
        });

        playModeToggle.setOnAction(e -> {
            isPlayMode = playModeToggle.isSelected();
            if (isPlayMode) {
                playModeToggle.setText("Playable Mode: ON");
                spawnPlayer();
            } else {
                playModeToggle.setText("Playable Mode: OFF");
                playerCell = null;
                playerSteps = 0;
                mazeCanvas.setPlayerCell(null);
            }
        });

        scene.setOnKeyPressed(event -> {
            if (!isPlayMode || grid == null || playerCell == null) return;

            int dx = 0, dy = 0;
            int wallIndex;

            switch (event.getCode()) {
                case UP, W -> { dy = -1; wallIndex = 0; }
                case RIGHT, D -> { dx = 1; wallIndex = 1; }
                case DOWN, S -> { dy = 1; wallIndex = 2; }
                case LEFT, A -> { dx = -1; wallIndex = 3; }
                default -> { return; }
            }

            if (!playerCell.walls[wallIndex]) {
                Cell nextCell = grid.getCell(playerCell.x + dx, playerCell.y + dy);
                if (nextCell != null) {
                    playerCell = nextCell;
                    playerSteps++;
                    mazeCanvas.setPlayerCell(playerCell);

                    if (playerCell.isExit) {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Victory!");
                        alert.setHeaderText(null);
                        alert.setContentText("🎉 Congratulations! You successfully navigated through the maze in " + playerSteps + " steps!");
                        alert.showAndWait();

                        isPlayMode = false;
                        playModeToggle.setSelected(false);
                        playModeToggle.setText("Playable Mode: OFF");
                        playerCell = null;
                        playerSteps = 0;
                        mazeCanvas.setPlayerCell(null);
                    }
                }
            }
        });

        primaryStage.setScene(scene);
        primaryStage.show();

        generateBtn.fire();
    }

    private void startGenerationAnimation(int cols, int rows, double braidFactor, String algorithm) {
        if (animTimeline != null) animTimeline.stop();

        grid = new MazeGrid(cols, rows);
        mazeCanvas.setGrid(grid);
        mazeCanvas.setSolvedPath(null);
        updateStatistics(-1);
        playerCell = null;
        playerSteps = 0;

        if (showSolutionToggle != null) {
            showSolutionToggle.setSelected(false);
            showSolutionToggle.setText("Show Solution");
        }

        MazeGenerator generator = switch (algorithm) {
            case "Randomized Prim's" -> new PrimsMazeGenerator();
            case "Randomized Kruskal's" -> new KruskalsMazeGenerator();
            default -> new DfsMazeGenerator();
        };

        generator.initialize(grid);

        animTimeline = new Timeline();
        KeyFrame keyFrame = new KeyFrame(Duration.millis(12), event -> {
            boolean running = generator.step();
            mazeCanvas.setCurrentHead(generator.getCurrentHead());

            if (!running) {
                animTimeline.stop();
                applyBraidFactor(grid, braidFactor);
                createOpenings(grid); // Automatically creates exactly 1 entrance and 1 exit
                mazeCanvas.setCurrentHead(null);
                updateStatistics(-1);

                if (isPlayMode) {
                    spawnPlayer();
                }
            }
        });

        animTimeline.getKeyFrames().add(keyFrame);
        animTimeline.setCycleCount(Timeline.INDEFINITE);
        animTimeline.play();
    }

    private void spawnPlayer() {
        if (grid == null) return;
        playerSteps = 1;
        for (int y = 0; y < grid.rows; y++) {
            Cell c = grid.getCell(0, y);
            if (c.isEntrance) {
                playerCell = c;
                mazeCanvas.setPlayerCell(playerCell);
                break;
            }
        }
    }

    private void runBfsSolver() {
        if (grid == null) return;

        Cell start = null;
        Cell target = null;

        for (int y = 0; y < grid.rows; y++) {
            if (grid.getCell(0, y).isEntrance) {
                start = grid.getCell(0, y);
                break;
            }
        }
        for (int y = 0; y < grid.rows; y++) {
            if (grid.getCell(grid.cols - 1, y).isExit) {
                target = grid.getCell(grid.cols - 1, y);
                break;
            }
        }

        if (start == null || target == null) return;

        Queue<Cell> queue = new LinkedList<>();
        Map<Cell, Cell> parentMap = new HashMap<>();
        Set<Cell> visited = new HashSet<>();

        queue.add(start);
        visited.add(start);

        boolean found = false;
        while (!queue.isEmpty()) {
            Cell current = queue.poll();
            if (current.equals(target)) {
                found = true;
                break;
            }

            int[][] dirs = {{0, -1}, {1, 0}, {0, 1}, {-1, 0}};
            for (int i = 0; i < 4; i++) {
                if (!current.walls[i]) {
                    Cell neighbor = grid.getCell(current.x + dirs[i][0], current.y + dirs[i][1]);
                    if (neighbor != null && !visited.contains(neighbor)) {
                        visited.add(neighbor);
                        parentMap.put(neighbor, current);
                        queue.add(neighbor);
                    }
                }
            }
        }

        if (found) {
            List<Cell> path = new ArrayList<>();
            Cell curr = target;
            while (curr != null) {
                path.add(curr);
                curr = parentMap.get(curr);
            }
            Collections.reverse(path);
            mazeCanvas.setSolvedPath(path);
            updateStatistics(path.size());
        } else {
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
        statsLabel.setText(String.format("Statistics:\n• Total Cells: %d\n• Dead Ends: %d\n• Path Length: %s",
                totalCells, deadEnds, pathStr));
    }

    private void applyBraidFactor(MazeGrid grid, double braidFactor) {
        Random rand = new Random();
        for (int x = 0; x < grid.cols; x++) {
            for (int y = 0; y < grid.rows; y++) {
                Cell cell = grid.cells[x][y];
                int wallCount = 0;
                for (boolean w : cell.walls) if (w) wallCount++;

                if (wallCount == 3 && rand.nextDouble() < braidFactor) {
                    List<Cell> adj = new ArrayList<>();
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

    // Always creates exactly 1 entrance and 1 exit
    private void createOpenings(MazeGrid grid) {
        Random rand = new Random();

        // Exactly 1 Entrance on the left column
        int entranceY = rand.nextInt(grid.rows);
        Cell entranceCell = grid.getCell(0, entranceY);
        entranceCell.walls[3] = false;
        entranceCell.isEntrance = true;

        // Exactly 1 Exit on the right column
        int exitY = rand.nextInt(grid.rows);
        Cell exitCell = grid.getCell(grid.cols - 1, exitY);
        exitCell.walls[1] = false;
        exitCell.isExit = true;
    }

    public static void main(String[] args) {
        launch(args);
    }
}