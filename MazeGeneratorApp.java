import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.*;

public class MazeGeneratorApp extends Application {

    // --- MODEL CLASSES ---
    public static class Cell {
        public int x, y;
        public boolean[] walls = {true, true, true, true}; // Top, Right, Bottom, Left
        public boolean visited = false;
        public boolean isEntrance = false;
        public boolean isExit = false;

        public Cell(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    public static class MazeGrid {
        public final int cols, rows;
        public final Cell[][] cells;

        public MazeGrid(int cols, int rows) {
            this.cols = cols;
            this.rows = rows;
            this.cells = new Cell[cols][rows];
            for (int x = 0; x < cols; x++) {
                for (int y = 0; y < rows; y++) {
                    cells[x][y] = new Cell(x, y);
                }
            }
        }

        public Cell getCell(int x, int y) {
            if (x < 0 || y < 0 || x >= cols || y >= rows) return null;
            return cells[x][y];
        }

        public void removeWalls(Cell a, Cell b) {
            int dx = a.x - b.x;
            int dy = a.y - b.y;

            if (dx == 1) { a.walls[3] = false; b.walls[1] = false; }       // Left / Right
            else if (dx == -1) { a.walls[1] = false; b.walls[3] = false; } // Right / Left

            if (dy == 1) { a.walls[0] = false; b.walls[2] = false; }       // Top / Bottom
            else if (dy == -1) { a.walls[2] = false; b.walls[0] = false; } // Bottom / Top
        }
    }

    // --- VIEW / CANVAS CLASS ---
    public static class MazeCanvas extends Canvas {
        private MazeGrid grid;
        private Cell currentHead;

        public MazeCanvas(double width, double height) {
            super(width, height);
        }

        public void setGrid(MazeGrid grid) {
            this.grid = grid;
            draw();
        }

        public void setCurrentHead(Cell head) {
            this.currentHead = head;
            draw();
        }

        public void draw() {
            GraphicsContext gc = getGraphicsContext2D();
            gc.clearRect(0, 0, getWidth(), getHeight());

            if (grid == null) return;

            double cellWidth = getWidth() / grid.cols;
            double cellHeight = getHeight() / grid.rows;

            // Render Cells
            for (int x = 0; x < grid.cols; x++) {
                for (int y = 0; y < grid.rows; y++) {
                    Cell c = grid.cells[x][y];
                    double px = x * cellWidth;
                    double py = y * cellHeight;

                    // Background highlight
                    if (c.isEntrance) {
                        gc.setFill(Color.LIGHTGREEN);
                        gc.fillRect(px, py, cellWidth, cellHeight);
                    } else if (c.isExit) {
                        gc.setFill(Color.TOMATO);
                        gc.fillRect(px, py, cellWidth, cellHeight);
                    } else if (c.visited) {
                        gc.setFill(Color.WHITE);
                        gc.fillRect(px, py, cellWidth, cellHeight);
                    } else {
                        gc.setFill(Color.DARKSLATEGRAY);
                        gc.fillRect(px, py, cellWidth, cellHeight);
                    }

                    // Draw Walls
                    gc.setStroke(Color.BLACK);
                    gc.setLineWidth(2);
                    if (c.walls[0]) gc.strokeLine(px, py, px + cellWidth, py);                 // Top
                    if (c.walls[1]) gc.strokeLine(px + cellWidth, py, px + cellWidth, py + cellHeight); // Right
                    if (c.walls[2]) gc.strokeLine(px, py + cellHeight, px + cellWidth, py + cellHeight); // Bottom
                    if (c.walls[3]) gc.strokeLine(px, py, px, py + cellHeight);                // Left
                }
            }

            // Highlight generator current head position
            if (currentHead != null) {
                gc.setFill(Color.DODGERBLUE);
                gc.fillRect(currentHead.x * cellWidth + 2, currentHead.y * cellHeight + 2,
                        cellWidth - 4, cellHeight - 4);
            }
        }
    }

    // --- MAIN JAVAFX APPLICATION ---
    private MazeGrid grid;
    private MazeCanvas mazeCanvas;
    private Timeline animTimeline;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Customizable JavaFX Maze Generator");

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

        VBox controls = new VBox(10,
                new Label("Width (Cols):"), widthSlider,
                new Label("Height (Rows):"), heightSlider,
                new Label("Entrances:"), entrancesSpinner,
                new Label("Exits:"), exitsSpinner,
                new Label("Loop / Braid Factor (%):"), braidSlider,
                generateBtn
        );
        controls.setPadding(new Insets(15));
        controls.setPrefWidth(220);

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

        Scene scene = new Scene(root, 850, 650);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Initial Generation
        generateBtn.fire();
    }

    private void startGenerationAnimation(int cols, int rows, int entrances, int exits, double braidFactor) {
        if (animTimeline != null) animTimeline.stop();

        grid = new MazeGrid(cols, rows);
        mazeCanvas.setGrid(grid);

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

    private void applyBraidFactor(MazeGrid grid, double braidFactor) {
        Random rand = new Random();
        for (int x = 0; x < grid.cols; x++) {
            for (int y = 0; y < grid.rows; y++) {
                Cell cell = grid.cells[x][y];
                // Count wall intactness
                int wallCount = 0;
                for (boolean w : cell.walls) if (w) wallCount++;

                // If it's a dead-end (3 walls) and passes random probability check, open one wall
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
        // Create Entrances along Left Border
        for (int i = 0; i < entrances; i++) {
            int y = rand.nextInt(grid.rows);
            Cell c = grid.getCell(0, y);
            c.walls[3] = false; // Knock down left wall
            c.isEntrance = true;
        }

        // Create Exits along Right Border
        for (int i = 0; i < exits; i++) {
            int y = rand.nextInt(grid.rows);
            Cell c = grid.getCell(grid.cols - 1, y);
            c.walls[1] = false; // Knock down right wall
            c.isExit = true;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}