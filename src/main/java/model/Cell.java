package model;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents an individual node/cell in the maze grid, supporting
 * graph-based adjacency for multi-shape topologies.
 */
public class Cell {
    // Generic coordinates (can represent Cartesian x,y or Polar ring,angle)
    public final double x;
    public final double y;

    // Map of neighboring cells and whether the wall between them is open/closed
    // Key: Neighbor Cell, Value: boolean (true = wall open, false = wall closed)
    private final Map<Cell, Boolean> neighbors;

    public Cell(double x, double y) {
        this.x = x;
        this.y = y;
        this.neighbors = new HashMap<>();
    }

    /**
     * Adds a neighbor connection with a default closed wall (false).
     */
    public void addNeighbor(Cell neighbor) {
        neighbors.putIfAbsent(neighbor, false);
    }

    public Map<Cell, Boolean> getNeighbors() {
        return neighbors;
    }

    public boolean hasWallWith(Cell neighbor) {
        return !neighbors.getOrDefault(neighbor, false);
    }

    public void removeWall(Cell neighbor) {
        if (neighbors.containsKey(neighbor)) {
            neighbors.put(neighbor, true);
        }
    }
}