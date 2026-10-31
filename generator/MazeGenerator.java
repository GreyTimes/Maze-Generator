package generator;

import model.Cell;
import model.MazeGrid;

public interface MazeGenerator {
    void initialize(MazeGrid grid);
    boolean step(); // Returns true while generation is ongoing, false when finished
    Cell getCurrentHead();
}