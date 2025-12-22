import java.util.*;
public class Puzzle{
    private final int rows;
    private final int cols;
    private Map<Cell, Point> points;

    //constructor
    public Puzzle(int rows, int cols, Map<Cell, Point> points) {
        this.rows = rows;
        this.cols = cols;
        this.points = points;
    }

    //getter methods
    public Map<Cell, Point> getPoints(){
        return points;
    }
    public int getRows(){
        return rows;
    }
    public int getCols(){
        return cols;
    }

    //setter methods
    public void setPoints(Map<Cell, Point> points){
        this.points=points;
    }

    public static boolean puzzleSolved(Puzzle puzzle){
        Map<Cell, Point> points = puzzle.getPoints();
        int rows = puzzle.getRows();
        int cols = puzzle.getCols();
        //if the map is full
        if(points.size()!=rows*cols) return false;
        //each path borders itself once for a root, twice otherwise
        //AKA test that each color is continuous
        Map<Point, Integer> roots = new HashMap<>();
        for(Map.Entry<Cell, Point> root: points.entrySet()){
            if(!root.getKey().inBounds(rows, cols)) return false;
            if(!Solver.pathIsComplete(points, root.getValue().color())) return false;
            //check if there are two roots per color
            Point val = root.getValue();
            if (val.isRoot()) {
                roots.put(val, roots.getOrDefault(val, 0) + 1);
            }
        }
        for (int count : roots.values()) {
            if (count != 2) return false;
        }
        return true;
    }
}