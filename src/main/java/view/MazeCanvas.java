package view;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import model.Cell;
import model.MazeGrid;

import java.util.List;

public class MazeCanvas extends Canvas {
    private MazeGrid grid;
    private Cell currentHead;
    private List<Cell> solvedPath;
    private Cell playerCell; // Added for playable mode
    private boolean isDarkMode = true; // Default to dark mode theme

    public MazeCanvas(double width, double height) {
        super(width, height);
    }

    public void setGrid(MazeGrid grid) {
        this.grid = grid;
        this.solvedPath = null;
        this.playerCell = null;
        draw();
    }

    public void setCurrentHead(Cell head) {
        this.currentHead = head;
        draw();
    }

    public void setSolvedPath(List<Cell> solvedPath) {
        this.solvedPath = solvedPath;
        draw();
    }

    public void setPlayerCell(Cell playerCell) {
        this.playerCell = playerCell;
        draw();
    }

    // --- Added method to dynamically switch theme colors ---
    public void setDarkMode(boolean darkMode) {
        this.isDarkMode = darkMode;
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
                    // Dynamic cell background based on theme mode
                    if (isDarkMode) {
                        gc.setFill(Color.web("#1e1e1e")); // Dark background
                    } else {
                        gc.setFill(Color.web("#eaeaea")); // Light grey background
                    }
                    gc.fillRect(px, py, cellWidth, cellHeight);
                } else {
                    gc.setFill(isDarkMode ? Color.DARKSLATEGRAY : Color.web("#d0d0d0"));
                    gc.fillRect(px, py, cellWidth, cellHeight);
                }

                // Draw Walls (Dynamic color based on theme mode)
                if (isDarkMode) {
                    gc.setStroke(Color.web("#555555")); // Soft grey walls for dark mode
                } else {
                    gc.setStroke(Color.BLACK);          // Crisp black walls for light mode
                }
                gc.setLineWidth(2);

                if (c.walls[0]) gc.strokeLine(px, py, px + cellWidth, py);                 // Top
                if (c.walls[1]) gc.strokeLine(px + cellWidth, py, px + cellWidth, py + cellHeight); // Right
                if (c.walls[2]) gc.strokeLine(px, py + cellHeight, px + cellWidth, py + cellHeight); // Bottom
                if (c.walls[3]) gc.strokeLine(px, py, px, py + cellHeight);                // Left
            }
        }

        // Render Solved Path (Gold trail)
        if (solvedPath != null) {
            gc.setFill(Color.GOLD);
            for (Cell c : solvedPath) {
                if (!c.isEntrance && !c.isExit) {
                    double px = c.x * cellWidth;
                    double py = c.y * cellHeight;
                    gc.fillRect(px + cellWidth / 4, py + cellHeight / 4, cellWidth / 2, cellHeight / 2);
                }
            }
        }

        // Highlight generator current head position
        if (currentHead != null) {
            gc.setFill(Color.DODGERBLUE);
            gc.fillRect(currentHead.x * cellWidth + 2, currentHead.y * cellHeight + 2,
                    cellWidth - 4, cellHeight - 4);
        }

        // Render Playable Mode Player Token (Magenta Circle)
        if (playerCell != null) {
            gc.setFill(Color.MAGENTA);
            gc.fillOval(playerCell.x * cellWidth + cellWidth * 0.2,
                    playerCell.y * cellHeight + cellHeight * 0.2,
                    cellWidth * 0.6, cellHeight * 0.6);
        }
    }
}