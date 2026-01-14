import java.util.*;
// public class Path {
//     private final Set<Cell> roots;

final class Path {
    private final List<Cell> cells;
    // private final char color;

    public Path(List<Cell> cells) {
        this.cells = List.copyOf(cells);
        // this.color = color;
    }

    public List<Cell> points() {
        return cells;
    }

    // public char color(){
    //     return color;
    // }

    public Path extend(Cell next) {
        List<Cell> newCells = new ArrayList<>(cells);
        newCells.add(next);
        return new Path(newCells);
    }
}
