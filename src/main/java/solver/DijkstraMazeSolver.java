package solver;

import model.Cell;
import model.MazeGrid;

import java.util.*;

public class DijkstraMazeSolver {
    public static List<Cell> solve(MazeGrid grid) {
        Cell start = null, exit = null;
        for (int x = 0; x < grid.cols; x++) {
            for (int y = 0; y < grid.rows; y++) {
                if (grid.cells[x][y].isEntrance) start = grid.cells[x][y];
                if (grid.cells[x][y].isExit) exit = grid.cells[x][y];
            }
        }
        if (start == null || exit == null) return null;

        Map<Cell, Integer> distances = new HashMap<>();
        Map<Cell, Cell> parentMap = new HashMap<>();
        PriorityQueue<Cell> pq = new PriorityQueue<>(Comparator.comparingInt(distances::get));

        for (int x = 0; x < grid.cols; x++) {
            for (int y = 0; y < grid.rows; y++) {
                distances.put(grid.cells[x][y], Integer.MAX_VALUE);
            }
        }

        distances.put(start, 0);
        pq.add(start);

        while (!pq.isEmpty()) {
            Cell curr = pq.poll();
            if (curr == exit) break;

            List<Cell> neighbors = getAccessibleNeighbors(curr, grid);
            for (Cell n : neighbors) {
                int newDist = distances.get(curr) + 1;
                if (newDist < distances.get(n)) {
                    distances.put(n, newDist);
                    parentMap.put(n, curr);
                    pq.remove(n);
                    pq.add(n);
                }
            }
        }

        List<Cell> path = new ArrayList<>();
        Cell curr = exit;
        if (!parentMap.containsKey(curr) && curr != start) return null;

        while (curr != null) {
            path.add(0, curr);
            curr = parentMap.get(curr);
        }
        return path;
    }

    private static List<Cell> getAccessibleNeighbors(Cell cell, MazeGrid grid) {
        List<Cell> list = new ArrayList<>();
        int[][] dirs = {{0, -1}, {1, 0}, {0, 1}, {-1, 0}};
        for (int i = 0; i < 4; i++) {
            if (!cell.walls[i]) {
                int[] d = dirs[i];
                Cell n = grid.getCell(cell.x + d[0], cell.y + d[1]);
                if (n != null) list.add(n);
            }
        }
        return list;
    }
}