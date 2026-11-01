package generator;

import model.Cell;
import model.MazeGrid;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AldousBroderMazeGenerator implements MazeGenerator {
    private MazeGrid grid;
    private Cell current;
    private int unvisitedCount;
    private final Random rand = new Random();

    @Override
    public void initialize(MazeGrid grid) {
        this.grid = grid;
        this.unvisitedCount = grid.cols * grid.rows;
        // Pick a random starting cell
        this.current = grid.getCell(rand.nextInt(grid.cols), rand.nextInt(grid.rows));
        current.visited = true;
        unvisitedCount--;
    }

    @Override
    public boolean step() {
        if (unvisitedCount <= 0) return false;

        // Get neighbors of current cell
        List<Cell> neighbors = new ArrayList<>();
        int[][] dirs = {{0, -1}, {1, 0}, {0, 1}, {-1, 0}};
        for (int[] d : dirs) {
            Cell n = grid.getCell(current.x + d[0], current.y + d[1]);
            if (n != null) neighbors.add(n);
        }

        if (neighbors.isEmpty()) return false;

        Cell next = neighbors.get(rand.nextInt(neighbors.size()));

        if (!next.visited) {
            grid.removeWalls(current, next);
            next.visited = true;
            unvisitedCount--;
        }

        current = next;
        return unvisitedCount > 0;
    }

    @Override
    public Cell getCurrentHead() {
        return current;
    }
}