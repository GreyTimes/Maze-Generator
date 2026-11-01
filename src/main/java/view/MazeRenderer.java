package view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import model.Cell;
import model.MazeGrid;

import java.util.List;

public class MazeRenderer {
    private boolean darkMode = true;

    public void setDarkMode(boolean darkMode) {
        this.darkMode = darkMode;
    }

    public void render(GraphicsContext gc, double width, double height, MazeGrid grid,
                       Cell playerCell, List<Cell> solvedPath, Cell currentHead) {
        // Theme Colors
        Color bgColor = darkMode ? Color.web("#1e1e1e") : Color.web("#f5f5f5");
        Color wallColor = darkMode ? Color.web("#4a4a4a") : Color.web("#333333");
        Color pathColor = Color.web("#00bcd4");
        Color playerColor = Color.web("#e91e63");
        Color headColor = Color.web("#ffeb3b");
        Color entranceColor = Color.web("#4caf50");
        Color exitColor = Color.web("#f44336");

        // Clear and fill background
        gc.setFill(bgColor);
        gc.fillRect(0, 0, width, height);

        if (grid == null) return;

        double cellWidth = width / grid.cols;
        double cellHeight = height / grid.rows;

        // 1. Draw solved path if available
        if (solvedPath != null && !solvedPath.isEmpty()) {
            gc.setFill(pathColor);
            for (Cell c : solvedPath) {
                if (!c.isEntrance && !c.isExit) {
                    gc.fillRect(c.x * cellWidth + cellWidth * 0.2, c.y * cellHeight + cellHeight * 0.2,
                            cellWidth * 0.6, cellHeight * 0.6);
                }
            }
        }

        // 2. Draw cell backgrounds (Entrance & Exit)
        for (int x = 0; x < grid.cols; x++) {
            for (int y = 0; y < grid.rows; y++) {
                Cell c = grid.cells[x][y];
                if (c.isEntrance) {
                    gc.setFill(entranceColor);
                    gc.fillRect(x * cellWidth, y * cellHeight, cellWidth, cellHeight);
                } else if (c.isExit) {
                    gc.setFill(exitColor);
                    gc.fillRect(x * cellWidth, y * cellHeight, cellWidth, cellHeight);
                }
            }
        }

        // 3. Draw generation animation head
        if (currentHead != null) {
            gc.setFill(headColor);
            gc.fillRect(currentHead.x * cellWidth + cellWidth * 0.1, currentHead.y * cellHeight + cellHeight * 0.1,
                    cellWidth * 0.8, cellHeight * 0.8);
        }

        // 4. Draw player in playable mode
        if (playerCell != null) {
            gc.setFill(playerColor);
            gc.fillOval(playerCell.x * cellWidth + cellWidth * 0.15, playerCell.y * cellHeight + cellHeight * 0.15,
                    cellWidth * 0.7, cellHeight * 0.7);
        }

        // 5. Draw walls
        gc.setStroke(wallColor);
        gc.setLineWidth(Math.max(1.5, Math.min(cellWidth, cellHeight) / 10.0));

        for (int x = 0; x < grid.cols; x++) {
            for (int y = 0; y < grid.rows; y++) {
                Cell c = grid.cells[x][y];
                double px = x * cellWidth;
                double py = y * cellHeight;

                if (c.walls[0]) gc.strokeLine(px, py, px + cellWidth, py);           // Top
                if (c.walls[1]) gc.strokeLine(px + cellWidth, py, px + cellWidth, py + cellHeight); // Right
                if (c.walls[2]) gc.strokeLine(px, py + cellHeight, px + cellWidth, py + cellHeight); // Bottom
                if (c.walls[3]) gc.strokeLine(px, py, px, py + cellHeight);         // Left
            }
        }
    }
}