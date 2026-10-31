package generator;

import model.Cell;
import model.MazeGrid;
import java.util.*;

public class KruskalsMazeGenerator implements MazeGenerator {
    private MazeGrid grid;
    private List<Edge> edges;
    private Iterator<Edge> edgeIterator;
    private Map<Cell, Integer> cellSets;
    private Cell currentHead;

    private static class Edge {
        Cell a, b;
        Edge(Cell a, Cell b) { this.a = a; this.b = b; }
    }

    @Override
    public void initialize(MazeGrid grid) {
        this.grid = grid;
        this.cellSets = new HashMap<>();
        this.edges = new ArrayList<>();
        int setId = 0;

        for (int x = 0; x < grid.cols; x++) {
            for (int y = 0; y < grid.rows; y++) {
                Cell c = grid.cells[x][y];
                c.visited = true;
                cellSets.put(c, setId++);

                if (x + 1 < grid.cols) edges.add(new Edge(c, grid.getCell(x + 1, y)));
                if (y + 1 < grid.rows) edges.add(new Edge(c, grid.getCell(x, y + 1)));
            }
        }
        Collections.shuffle(edges);
        this.edgeIterator = edges.iterator();
        currentHead = null;
    }

    @Override
    public boolean step() {
        if (edgeIterator.hasNext()) {
            Edge edge = edgeIterator.next();
            currentHead = edge.a;
            int setA = cellSets.get(edge.a);
            int setB = cellSets.get(edge.b);

            if (setA != setB) {
                grid.removeWalls(edge.a, edge.b);
                for (Map.Entry<Cell, Integer> entry : cellSets.entrySet()) {
                    if (entry.getValue() == setB) {
                        cellSets.put(entry.getKey(), setA);
                    }
                }
            }
            return true;
        }
        currentHead = null;
        return false;
    }

    @Override
    public Cell getCurrentHead() {
        return currentHead;
    }
}