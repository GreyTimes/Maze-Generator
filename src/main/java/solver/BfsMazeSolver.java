package solver;

import model.Cell;
import model.MazeGrid;
import java.util.*;

public class BfsMazeSolver {
    public static List<Cell> solve(MazeGrid grid) {
        if (grid == null) return null;

        Cell start = null;
        Cell target = null;

        for (int y = 0; y < grid.rows; y++) {
            if (grid.getCell(0, y).isEntrance) {
                start = grid.getCell(0, y);
                break;
            }
        }
        for (int y = 0; y < grid.rows; y++) {
            if (grid.getCell(grid.cols - 1, y).isExit) {
                target = grid.getCell(grid.cols - 1, y);
                break;
            }
        }

        if (start == null || target == null) return null;

        Queue<Cell> queue = new LinkedList<>();
        Map<Cell, Cell> parentMap = new HashMap<>();
        Set<Cell> visited = new HashSet<>();

        queue.add(start);
        visited.add(start);

        boolean found = false;
        while (!queue.isEmpty()) {
            Cell current = queue.poll();
            if (current.equals(target)) {
                found = true;
                break;
            }

            int[][] dirs = {{0, -1}, {1, 0}, {0, 1}, {-1, 0}};
            for (int i = 0; i < 4; i++) {
                if (!current.walls[i]) {
                    Cell neighbor = grid.getCell(current.x + dirs[i][0], current.y + dirs[i][1]);
                    if (neighbor != null && !visited.contains(neighbor)) {
                        visited.add(neighbor);
                        parentMap.put(neighbor, current);
                        queue.add(neighbor);
                    }
                }
            }
        }

        if (found) {
            List<Cell> path = new ArrayList<>();
            Cell curr = target;
            while (curr != null) {
                path.add(curr);
                curr = parentMap.get(curr);
            }
            Collections.reverse(path);
            return path;
        }
        return null;
    }
}