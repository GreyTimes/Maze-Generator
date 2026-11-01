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

    public Cell getCurrentCell() { return currentCell; }
    public int getSteps() { return steps; }
    public void reset() {
        currentCell = null;
        steps = 0;
    }
}