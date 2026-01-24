import java.util.*;

final class Path {
    private final List<Cell> cells;
    private final char color;

    public Path(List<Cell> cells, char color) {
        this.cells = List.copyOf(cells);
        this.color = color;
    }

    public List<Cell> points() {
        return cells;
    }

    public char color(){
        return color;
    }

    public Path extend(Cell next) {
        List<Cell> newCells = new ArrayList<>(cells);
        newCells.add(next);
        return new Path(newCells, this.color());
    }
}
