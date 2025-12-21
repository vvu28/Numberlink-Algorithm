public record Cell(int x, int y){
    boolean inBounds(int rows, int cols) {
        return x >= 0 && x < cols && y >= 0 && y < rows;
    }
    Cell north() { return new Cell(x, y + 1); }
    Cell east()  { return new Cell(x + 1, y); }
    Cell south() { return new Cell(x, y - 1); }
    Cell west()  { return new Cell(x - 1, y); }
    Cell NW()    { return new Cell(x-1, y+1); }
    Cell SW()    { return new Cell(x-1, y-1); }
    Cell NE()    { return new Cell(x+1, y+1); }
    Cell SE()    { return new Cell(x+1, y-1); }
}
