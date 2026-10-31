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
    private List<Cell> solvedPath; // Added for the pathfinding solver

    public MazeCanvas(double width, double height) {
        super(width, height);
    }

    public void setGrid(MazeGrid grid) {
        this.grid = grid;
        this.solvedPath = null;
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
    }
}