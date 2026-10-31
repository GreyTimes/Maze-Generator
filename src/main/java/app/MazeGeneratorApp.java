package app;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.Cell;
import model.MazeGrid;
import view.MazeCanvas;
import generator.*;

import java.util.*;

public class MazeGeneratorApp extends Application {

    private MazeGrid grid;
    private MazeCanvas mazeCanvas;
    private Timeline animTimeline;
    private Label statsLabel;
    private ComboBox<String> algorithmCombo;
    private ToggleButton playModeToggle;
    private ToggleButton showSolutionToggle; // Toggle for showing/hiding solution
    private boolean isPlayMode = false;
    private Cell playerCell = null;
    private int playerSteps = 0;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Customizable JavaFX Maze Generator & Solver");

        mazeCanvas = new MazeCanvas(600, 600);

        Slider widthSlider = new Slider(5, 40, 15);
        Slider heightSlider = new Slider(5, 40, 15);
        Spinner<Integer> entrancesSpinner = new Spinner<>(1, 5, 1);
        Spinner<Integer> exitsSpinner = new Spinner<>(1, 5, 1);
        Slider braidSlider = new Slider(0, 100, 20);

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
        playModeToggle.setStyle("-fx-base: #ffcccc;");

        Button generateBtn = new Button("Generate Labyrinth");
        generateBtn.setMaxWidth(Double.MAX_VALUE);

        // --- Toggleable Show/Hide Solution Button ---
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

        widthSlider.setShowTickLabels(true);
        heightSlider.setShowTickLabels(true);

        statsLabel = new Label("Statistics:\n• Total Cells: -\n• Dead Ends: -\n• Path Length: -");
        statsLabel.setStyle("-fx-font-family: monospace; -fx-padding: 10; -fx-background-color: #f4f4f4; -fx-border-color: #ccc;");

        VBox controls = new VBox(10,
                new Label("Algorithm:"), algorithmCombo,
                playModeToggle,
                new Label("Width (Cols):"), widthSlider,
                new Label("Height (Rows):"), heightSlider,
                new Label("Entrances:"), entrancesSpinner,
                new Label("Exits:"), exitsSpinner,
                new Label("Loop / Braid Factor (%):"), braidSlider,
                generateBtn,
                showSolutionToggle,
                new Separator(),
                new Label("Maze Metrics"),
                statsLabel
        );
        controls.setPadding(new Insets(15));
        controls.setPrefWidth(240);

        BorderPane root = new BorderPane();
        root.setCenter(mazeCanvas);
        root.setRight(controls);

        generateBtn.setOnAction(e -> {
            int cols = (int) widthSlider.getValue();
            int rows = (int) heightSlider.getValue();
            int entrances = entrancesSpinner.getValue();
            int exits = exitsSpinner.getValue();
            double braidFactor = braidSlider.getValue() / 100.0;
            String selectedAlgo = algorithmCombo.getValue();

            startGenerationAnimation(cols, rows, entrances, exits, braidFactor, selectedAlgo);
        });

        playModeToggle.setOnAction(e -> {
            isPlayMode = playModeToggle.isSelected();
            if (isPlayMode) {
                playModeToggle.setText("Playable Mode: ON");
                playModeToggle.setStyle("-fx-base: #ccffcc;");
                spawnPlayer();
            } else {
                playModeToggle.setText("Playable Mode: OFF");
                playModeToggle.setStyle("-fx-base: #ffcccc;");
                playerCell = null;
                playerSteps = 0;
                mazeCanvas.setPlayerCell(null);
            }
        });

        Scene scene = new Scene(root, 870, 750);

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
                        playModeToggle.setStyle("-fx-base: #ffcccc;");
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

    private void startGenerationAnimation(int cols, int rows, int entrances, int exits, double braidFactor, String algorithm) {
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
                createOpenings(grid, entrances, exits);
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
        playerSteps = 1; // Starting on the entrance cell counts as step 1
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

    private void createOpenings(MazeGrid grid, int entrances, int exits) {
        Random rand = new Random();
        for (int i = 0; i < entrances; i++) {
            int y = rand.nextInt(grid.rows);
            Cell c = grid.getCell(0, y);
            c.walls[3] = false;
            c.isEntrance = true;
        }

        for (int i = 0; i < exits; i++) {
            int y = rand.nextInt(grid.rows);
            Cell c = grid.getCell(grid.cols - 1, y);
            c.walls[1] = false;
            c.isExit = true;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}