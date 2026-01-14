import java.util.*;
final class Puzzle{
    private final int rows;
    private final int cols;
    // private final Map<Cell, Point> allPoints;
    private final Map<Cell, Point> points; //this includes filled cells only
    private final Map<Character, Path> paths;

    //constructor
    public Puzzle(int rows, int cols, Map<Cell, Point> points, Map<Character, Path> paths) {
        this.rows = rows;
        this.cols = cols;
        this.points = Map.copyOf(points);
        this.paths = Map.copyOf(paths);

        // Map<Cell, Point> grid = new HashMap<>();
        // for (int i = 0; i < rows; i++) {
        //     for (int j = 0; j < cols; j++) {
        //         grid.put(new Cell(j, i), null);
        //     }
        // }
        // // overlay filled points
        // grid.putAll(points);
        // this.allPoints = Map.copyOf(grid);
    }

    //getter methods
    public Map<Cell, Point> getPoints(){
        return points;
    }
    // public Map<Cell, Point> getAllPoints(){
    //     return allPoints;
    // }
    public int getRows(){
        return rows;
    }
    public int getCols(){
        return cols;
    }
    public Map<Character, Path> getPaths(){
        return paths;
    }

    //update puzzle after a move by creating a new one
    public Puzzle withMove(char color, Cell cell){
        Map<Cell, Point> newPoints = new HashMap<>(points);
        newPoints.put(cell, new Point(color, false));
        //new path
        Map<Character, Path> newPaths = new HashMap<>(paths);
        Path oldPath = paths.get(color);
        newPaths.put(color, oldPath.extend(cell));
        return new Puzzle(rows, cols, newPoints, newPaths);
    }

    //isSolved
    public boolean isSolved() {
        if (points.size() != rows * cols) return false; //full map
        Map<Character, Integer> rootCounts = new HashMap<>();
        for (Map.Entry<Cell, Point> entry : points.entrySet()) {
            Point p = entry.getValue();
            if (!entry.getKey().inBounds(this)) return false; //OB
            rootCounts.merge(p.color(), p.isRoot() ? 1 : 0, Integer::sum); //count roots per color
        }
        for (int count : rootCounts.values()) {
            if (count != 2) return false; // there aren't 2 roots
        }
        return true;
    }
}