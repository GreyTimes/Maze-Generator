package generator;

import model.Cell;
import model.MazeGrid;
import java.util.*;

public class DfsMazeGenerator implements MazeGenerator {
    private MazeGrid grid;
    private Stack<Cell> stack;
    private Random rand;
    private Cell currentHead;

    @Override
    public void initialize(MazeGrid grid) {
        this.grid = grid;
        this.stack = new Stack<>();
        this.rand = new Random();
        Cell startCell = grid.getCell(0, 0);
        startCell.visited = true;
        stack.push(startCell);
        currentHead = startCell;
    }

    @Override
    public boolean step() {
        if (stack.isEmpty()) {
            currentHead = null;
            return false;
        }

        Cell current = stack.peek();
        currentHead = current;

        List<Cell> unvisitedNeighbors = getUnvisitedNeighbors(grid, current);
        if (!unvisitedNeighbors.isEmpty()) {
            Cell next = unvisitedNeighbors.get(rand.nextInt(unvisitedNeighbors.size()));
            grid.removeWalls(current, next);
            next.visited = true;
            stack.push(next);
        } else {
            stack.pop();
        }
        return true;
    }

    @Override
    public Cell getCurrentHead() {
        return currentHead;
    }

    private List<Cell> getUnvisitedNeighbors(MazeGrid grid, Cell cell) {
        List<Cell> neighbors = new ArrayList<>();
        int[][] dirs = {{0, -1}, {1, 0}, {0, 1}, {-1, 0}};

        for (int[] d : dirs) {
            Cell neighbor = grid.getCell(cell.x + d[0], cell.y + d[1]);
            if (neighbor != null && !neighbor.visited) {
                neighbors.add(neighbor);
            }
        }
        return neighbors;
    }
}