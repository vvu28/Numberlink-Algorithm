import java.util.*;
public class Puzzle{
    private final int rows;
    private final int cols;
    private Map<Cell, Point> points; //this includes filled cells only
    private Set<Path> paths;

    //constructor
    public Puzzle(int rows, int cols, Map<Cell, Point> points, Set<Path> paths) {
        this.rows = rows;
        this.cols = cols;
        this.points = points;
        this.paths = paths;
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
    public Set<Path> getPaths(){
        return paths;
    }
    public Map<Cell, Point> getAllPoints(){
        Map<Cell, Point> allPoints = new HashMap<>(points);
        for(int i = 0; i<rows; i++){
            for(int j = 0; j<cols; j++){
                Cell newCell = new Cell(j, i);
                if (!allPoints.containsKey(newCell)) allPoints.put(newCell, null);
            }
        }
        return allPoints;
    }

    //setter methods
    public void setPoints(Map<Cell, Point> points){
        this.points=points;
    }
    public void setPaths(Set<Path> paths){
        this.paths = paths;
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
            if(!root.getKey().inBounds(puzzle)) return false;
            if(!Solver.pathIsComplete(puzzle, root.getValue().color())) return false;
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