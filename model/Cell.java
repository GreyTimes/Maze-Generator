package model;

public class Cell {
    public int x, y;
    public boolean[] walls = {true, true, true, true}; //top, right, bottom, left
    public boolean visited = false;
    public boolean isEntrance = false;
    public boolean isExit = false;

    public Cell(int x, int y){
        this.x = x;
        this.y = y;
    }
}
