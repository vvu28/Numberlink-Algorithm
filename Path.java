import java.util.*;
public class Path {
    private final char color;
    private final Set<Cell> roots;
    private Deque<Cell> points;

    public Path(char color, Set<Cell> roots, Deque<Cell> points){
        this.color = color;
        this.roots = roots;
        this.points = points;
    }

    public char color(){
        return color;
    }
    public Deque<Cell> points(){
        return points;
    }
    public Set<Cell> roots(){
        return roots;
    }

    public static List<Cell> allChildren(List<Cell> def, List<Cell> tent){
        List<Cell> allChildren = new ArrayList<>(def);
        for(Cell cell : tent){
            allChildren.add(cell);
        }
        return allChildren;
    }

    public static List<Cell> allPath(List<Cell> def, List<Cell> tent, Set<Cell> roots){
        List<Cell> allPath = new ArrayList<>(allChildren(def, tent));
        for(Cell cell : roots){
            allPath.add(cell);
        }
        return allPath;
    }
}
