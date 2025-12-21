import java.util.*;
public class Puzzle{
    private final int rows;
    private final int cols;
    private final Map<Cell, Character> points;

    public Puzzle(int rows, int cols, Map<Cell, Character> points) {
        this.rows = rows;
        this.cols = cols;
        this.points = points;
    }

    public static boolean puzzleSolved(Map<Cell, Character> points, int rows, int cols){
        //if the map is full
        if(points.size()!=rows*cols) return false;
        //each path borders itself once for a root, twice otherwise
        //AKA test that each color is continuous
        Map<Character, Integer> roots = new HashMap<>();
        for(Map.Entry<Cell, Character> root: points.entrySet()){
            if(!Solver.isContinuous(points, root.getKey(), rows, cols)) return false;
            //check if there are two roots per color
            char ch = points.get(root.getKey());
            if (Solver.isRoot(ch)) {
                roots.put(ch, roots.getOrDefault(ch, 0) + 1);
            }
        }
        for (int count : roots.values()) {
            if (count != 2) return false;
        }
        return true;
    }
}