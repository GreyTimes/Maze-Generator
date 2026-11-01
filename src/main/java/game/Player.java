package game;

import model.Cell;
import model.MazeGrid;

public class Player {
    private Cell currentCell;
    private int steps = 0;

    public void spawn(MazeGrid grid) {
        steps = 1;
        for (int y = 0; y < grid.rows; y++) {
            Cell c = grid.getCell(0, y);
            if (c.isEntrance) {
                currentCell = c;
                break;
            }
        }
    }

    public boolean move(int dx, int dy, int wallIndex, MazeGrid grid) {
        if (currentCell == null || currentCell.walls[wallIndex]) return false;

        Cell nextCell = grid.getCell(currentCell.x + dx, currentCell.y + dy);
        if (nextCell != null) {
            currentCell = nextCell;
            steps++;
            return true;
        }
        return false;
    }

    // Helper to reveal cells around the player based on fog radius
    public void updateDiscovery(MazeGrid grid, boolean fogActive, int fogRadius) {
        if (grid == null || currentCell == null) return;

        for (int x = 0; x < grid.cols; x++) {
            for (int y = 0; y < grid.rows; y++) {
                Cell c = grid.cells[x][y];
                if (!fogActive) {
                    c.discovered = true;
                } else {
                    int dist = Math.abs(c.x - currentCell.x) + Math.abs(c.y - currentCell.y);
                    if (dist <= fogRadius) {
                        c.discovered = true;
                    }
                }
            }
        }
    }

    public Cell getCurrentCell() { return currentCell; }
    public int getSteps() { return steps; }
    public void reset() {
        currentCell = null;
        steps = 0;
    }
}