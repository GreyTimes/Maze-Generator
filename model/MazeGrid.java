package model;

public class MazeGrid {
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
