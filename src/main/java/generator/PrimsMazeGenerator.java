package generator;

import model.Cell;
import model.MazeGrid;
import java.util.*;

public class PrimsMazeGenerator implements MazeGenerator {
    private MazeGrid grid;
    private List<Cell> frontier;
    private Random rand;
    private Cell currentHead;

    @Override
    public void initialize(MazeGrid grid) {
        this.grid = grid;
        this.frontier = new ArrayList<>();
        this.rand = new Random();
        Cell start = grid.getCell(0, 0);
        start.visited = true;
        addFrontierCells(grid, start, frontier);
        currentHead = start;
    }

    @Override
    public boolean step() {
        if (frontier.isEmpty()) {
            currentHead = null;
            return false;
        }

        Cell current = frontier.remove(rand.nextInt(frontier.size()));
        currentHead = current;

        List<Cell> visitedNeighbors = getVisitedNeighbors(grid, current);
        if (!visitedNeighbors.isEmpty()) {
            Cell neighbor = visitedNeighbors.get(rand.nextInt(visitedNeighbors.size()));
            grid.removeWalls(current, neighbor);
            current.visited = true;
            addFrontierCells(grid, current, frontier);
        }
        return true;
    }

    @Override
    public Cell getCurrentHead() {
        return currentHead;
    }

    private void addFrontierCells(MazeGrid grid, Cell cell, List<Cell> frontier) {
        int[][] dirs = {{0, -1}, {1, 0}, {0, 1}, {-1, 0}};
        for (int[] d : dirs) {
            Cell n = grid.getCell(cell.x + d[0], cell.y + d[1]);
            if (n != null && !n.visited && !frontier.contains(n)) {
                frontier.add(n);
            }
        }
    }

    private List<Cell> getVisitedNeighbors(MazeGrid grid, Cell cell) {
        List<Cell> neighbors = new ArrayList<>();
        int[][] dirs = {{0, -1}, {1, 0}, {0, 1}, {-1, 0}};
        for (int[] d : dirs) {
            Cell n = grid.getCell(cell.x + d[0], cell.y + d[1]);
            if (n != null && n.visited) {
                neighbors.add(n);
            }
        }
        return neighbors;
    }
}