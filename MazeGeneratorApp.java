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

import java.util.*;

public class MazeGeneratorApp extends Application {

    private MazeGrid grid;
    private MazeCanvas mazeCanvas;
    private Timeline animTimeline;
    private Label statsLabel; // UI label for live statistics

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Customizable JavaFX Maze Generator & Solver");

        mazeCanvas = new MazeCanvas(600, 600);

        // Control UI Elements
        Slider widthSlider = new Slider(5, 50, 15);
        Slider heightSlider = new Slider(5, 50, 15);
        Spinner<Integer> entrancesSpinner = new Spinner<>(1, 5, 1);
        Spinner<Integer> exitsSpinner = new Spinner<>(1, 5, 1);
        Slider braidSlider = new Slider(0, 100, 20); // Complexity / Loop percentage
        Button generateBtn = new Button("Generate Labyrinth");

        widthSlider.setShowTickLabels(true);
        heightSlider.setShowTickLabels(true);

        // Statistics Panel Setup (Issue #3)
        statsLabel = new Label("Statistics:\n- Total Cells: -\n- Dead Ends: -\n- Path Length: -");
        statsLabel.setStyle("-fx-font-family: monospace; -fx-padding: 10; -fx-background-color: #f4f4f4; -fx-border-color: #ccc;");

        VBox controls = new VBox(10,
                new Label("Width (Cols):"), widthSlider,
                new Label("Height (Rows):"), heightSlider,
                new Label("Entrances:"), entrancesSpinner,
                new Label("Exits:"), exitsSpinner,
                new Label("Loop / Braid Factor (%):"), braidSlider,
                generateBtn,
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

            startGenerationAnimation(cols, rows, entrances, exits, braidFactor);
        });

        Scene scene = new Scene(root, 870, 650);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Initial Generation
        generateBtn.fire();
    }

    private void startGenerationAnimation(int cols, int rows, int entrances, int exits, double braidFactor) {
        if (animTimeline != null) animTimeline.stop();

        grid = new MazeGrid(cols, rows);
        mazeCanvas.setGrid(grid);
        updateStatistics(0); // Reset/init stats

        Stack<Cell> stack = new Stack<>();
        Random rand = new Random();

        Cell startCell = grid.getCell(0, 0);
        startCell.visited = true;
        stack.push(startCell);

        animTimeline = new Timeline();
        KeyFrame keyFrame = new KeyFrame(Duration.millis(15), e -> {
            if (!stack.isEmpty()) {
                Cell current = stack.peek();
                mazeCanvas.setCurrentHead(current);

                List<Cell> unvisitedNeighbors = getUnvisitedNeighbors(grid, current);
                if (!unvisitedNeighbors.isEmpty()) {
                    Cell next = unvisitedNeighbors.get(rand.nextInt(unvisitedNeighbors.size()));
                    grid.removeWalls(current, next);
                    next.visited = true;
                    stack.push(next);
                } else {
                    stack.pop();
                }
            } else {
                // Maze generation finished -> Apply extra parameters
                applyBraidFactor(grid, braidFactor);
                createOpenings(grid, entrances, exits);
                mazeCanvas.setCurrentHead(null);
                animTimeline.stop();

                // Run BFS solver and compute final metrics
                runBfsSolver();
            }
        });

        animTimeline.getKeyFrames().add(keyFrame);
        animTimeline.setCycleCount(Timeline.INDEFINITE);
        animTimeline.play();
    }

    private List<Cell> getUnvisitedNeighbors(MazeGrid grid, Cell cell) {
        List<Cell> neighbors = new ArrayList<>();
        int[][] dirs = {{0, -1}, {1, 0}, {0, 1}, {-1, 0}};

        for (int[] d : dirs) {
            Cell neighbor = grid.getCell(cell.x + d[0], cell.y + d[1]);
            if (neighbor != null && !neighbor.visited) {
                neighbors.add(neighbor);
            }
        }
        return neighbors;
    }

    private void runBfsSolver() {
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

    // --- METRICS CALCULATION (Issue #3) ---
    private void updateStatistics(int pathLength) {
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

        String pathStr = (pathLength > 0) ? pathLength + " steps" : "No path";
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