import java.util.*;
public class Path {
    private final char color;
    private final List<Cell> roots;
    private final List<Cell> def; //definite children
    private List<Cell> tent; //tentative children

    public Path(char color, List<Cell> roots, List<Cell> def, List<Cell> tent){
        this.color = color;
        this.roots = roots;
        this.def = def;
        this.tent = tent;
    }

    public char getColor(){
        return color;
    }
    public List<Cell> getTent(){
        return tent;
    }
    public List<Cell> getDef(){
        return def;
    }
    public List<Cell> getRoots(){
        return roots;
    }

    public static List<Cell> allChildren(List<Cell> def, List<Cell> tent){
        List<Cell> allChildren = new ArrayList<>(def);
        for(Cell cell : tent){
            allChildren.add(cell);
        }
        return allChildren;
    }

    public static List<Cell> allPath(List<Cell> def, List<Cell> tent, List<Cell> roots){
        List<Cell> allPath = new ArrayList<>(allChildren(def, tent));
        for(Cell cell : roots){
            allPath.add(cell);
        }
        return allPath;
    }
}
