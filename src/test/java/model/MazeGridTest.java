package model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MazeGridTest {

    @Test
    void testGridInitialization() {
        int cols = 20;
        int rows = 20;
        MazeGrid grid = new MazeGrid(cols, rows);

        // Verify dimensions
        assertEquals(cols, grid.cols, "Grid columns should match requested size");
        assertEquals(rows, grid.rows, "Grid rows should match requested size");

        // Verify cells array is populated and not null
        assertNotNull(grid.cells, "Cells array should be initialized");
        assertEquals(cols, grid.cells.length);
        assertEquals(rows, grid.cells[0].length);

        // Verify individual cell coordinates
        Cell cell = grid.getCell(5, 5);
        assertNotNull(cell, "Cell at (5, 5) should exist");
        assertEquals(5, cell.x);
        assertEquals(5, cell.y);
    }

    @Test
    void testOutOfBoundsCell() {
        MazeGrid grid = new MazeGrid(10, 10);

        // Out of bounds should return null safely
        assertNull(grid.getCell(-1, 0), "Negative coordinate should return null");
        assertNull(grid.getCell(10, 5), "Coordinate equal to cols should return null");
        assertNull(grid.getCell(5, 15), "Coordinate greater than rows should return null");
    }
}