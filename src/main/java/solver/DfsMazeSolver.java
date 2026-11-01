package solver;

import model.Cell;
import model.MazeGrid;

import java.util.*;

public class DfsMazeSolver {
    public static List<Cell> solve(MazeGrid grid) {
        Cell start = null, exit = null;
        for (int x = 0; x < grid.cols; x++) {
            for (int y = 0; y < grid.rows; y++) {
                if (grid.cells[x][y].isEntrance) start = grid.cells[x][y];
                if (grid.cells[x][y].isExit) exit = grid.cells[x][y];
            }
        }
        if (start == null || exit == null) return null;

        Stack<Cell> stack = new Stack<>();
        Map<Cell, Cell> parentMap = new HashMap<>();
        Set<Cell> visited = new HashSet<>();

        stack.push(start);
        visited.add(start);

        boolean found = false;
        while (!stack.isEmpty()) {
            Cell curr = stack.pop();
            if (curr == exit) {
                found = true;
                break;
            }

            List<Cell> neighbors = getAccessibleNeighbors(curr, grid);
            for (Cell n : neighbors) {
                if (!visited.contains(n)) {
                    visited.add(n);
                    parentMap.put(n, curr);
                    stack.push(n);
                }
            }
        }

        if (!found) return null;

        List<Cell> path = new ArrayList<>();
        Cell curr = exit;
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