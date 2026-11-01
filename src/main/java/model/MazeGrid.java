package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the topological maze grid structure supporting
 * multi-shape geometries via graph nodes.
 */
public class MazeGrid {
    public final int cols;
    public final int rows;
    public final Cell[][] cells; // Kept for backward compatibility with existing rectangular logic
    private final List<Cell> allCells;

    public MazeGrid(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;
        this.cells = new Cell[cols][rows];
        this.allCells = new ArrayList<>();

        initializeRectangularGrid();
    }

    private void initializeRectangularGrid() {
        // Create standard cells
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
     * Safely retrieves a cell at the given Cartesian coordinates.
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
}