package generator;

import model.Cell;
import model.MazeGrid;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WilsonsMazeGenerator implements MazeGenerator {
    private MazeGrid grid;
    private List<Cell> unvisited = new ArrayList<>();
    private List<Cell> walkPath = new ArrayList<>();
    private final Random rand = new Random();
    private boolean building = false;

    @Override
    public void initialize(MazeGrid grid) {
        this.grid = grid;
        unvisited.clear();
        for (int x = 0; x < grid.cols; x++) {
            for (int y = 0; y < grid.rows; y++) {
                Cell c = grid.cells[x][y];
                c.visited = false;
                unvisited.add(c);
            }
        }

        // Pick random initial cell to start the maze tree
        Cell first = unvisited.get(rand.nextInt(unvisited.size()));
        first.visited = true;
        unvisited.remove(first);
        building = false;
    }

    @Override
    public boolean step() {
        if (unvisited.isEmpty()) return false;

        if (!building) {
            // Start a new loop-erased random walk from a random unvisited cell
            walkPath.clear();
            Cell start = unvisited.get(rand.nextInt(unvisited.size()));
            walkPath.add(start);
            building = true;
        }

        Cell current = walkPath.get(walkPath.size() - 1);

        // If current hits the visited maze, carve the path and merge
        if (current.visited) {
            for (int i = 0; i < walkPath.size() - 1; i++) {
                Cell a = walkPath.get(i);
                Cell b = walkPath.get(i + 1);
                grid.removeWalls(a, b);
                a.visited = true;
                unvisited.remove(a);
            }
            building = false;
            return true;
        }

        // Otherwise, take a random step
        List<Cell> neighbors = new ArrayList<>();
        int[][] dirs = {{0, -1}, {1, 0}, {0, 1}, {-1, 0}};
        for (int[] d : dirs) {
            Cell n = grid.getCell(current.x + d[0], current.y + d[1]);
            if (n != null) neighbors.add(n);
        }

        Cell next = neighbors.get(rand.nextInt(neighbors.size()));

        // Check for loops in walkPath and erase them
        int loopIndex = walkPath.indexOf(next);
        if (loopIndex != -1) {
            subList(walkPath, loopIndex + 1);
        } else {
            walkPath.add(next);
        }

        return true;
    }

    private void subList(List<Cell> list, int fromIndex) {
        while (list.size() > fromIndex) {
            list.remove(list.size() - 1);
        }
    }

    @Override
    public Cell getCurrentHead() {
        return walkPath.isEmpty() ? null : walkPath.get(walkPath.size() - 1);
    }
}