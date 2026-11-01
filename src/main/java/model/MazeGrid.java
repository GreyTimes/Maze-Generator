package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the topological maze grid structure.
 */
public class MazeGrid {
    public final int cols;
    public final int rows;
    public final Cell[][] cells;
    private final List<Cell> allCells;

    public MazeGrid(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;
        this.cells = new Cell[cols][rows];
        this.allCells = new ArrayList<>();

        initializeRectangularGrid();
    }

    private void initializeRectangularGrid() {
        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {
                cells[x][y] = new Cell(x, y);
                allCells.add(cells[x][y]);
            }
        }

        // Establish standard orthogonal grid adjacencies
        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {
                Cell current = cells[x][y];

                if (x > 0) current.addNeighbor(cells[x - 1][y]);
                if (x < cols - 1) current.addNeighbor(cells[x + 1][y]);
                if (y > 0) current.addNeighbor(cells[x][y - 1]);
                if (y < rows - 1) current.addNeighbor(cells[x][y + 1]);
            }
        }
    }

    /**
     * Safely retrieves a cell at the given coordinates.
     */
    public Cell getCell(int x, int y) {
        if (x < 0 || x >= cols || y < 0 || y >= rows) {
            return null;
        }
        return cells[x][y];
    }

    public List<Cell> getAllCells() {
        return allCells;
    }

    /**
     * Removes the wall between two adjacent cells.
     */
    public void removeWalls(Cell a, Cell b) {
        if (a == null || b == null) return;

        // Handle standard orthogonal grid wall removal
        if (a.x == b.x) {
            if (a.y == b.y + 1) { // b is above a
                a.walls[0] = false;
                b.walls[2] = false;
            } else if (a.y == b.y - 1) { // b is below a
                a.walls[2] = false;
                b.walls[0] = false;
            }
        } else if (a.y == b.y) {
            if (a.x == b.x + 1) { // b is to the left of a
                a.walls[3] = false;
                b.walls[1] = false;
            } else if (a.x == b.x - 1) { // b is to the right of a
                a.walls[1] = false;
                b.walls[3] = false;
            }
        }

        // Also update graph neighbor map references
        a.getNeighbors().put(b, true);
        b.getNeighbors().put(a, true);
    }
}