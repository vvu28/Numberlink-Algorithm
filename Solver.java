import java.util.*;
public class Solver{
    public static void main(String[] args){
        int rows = 4;
        int cols = 5;

        //for now just manually place points
        Map<Cell, Character> points = new HashMap<>();
        //keep roots capital for consistency
        points.put(new Cell(0, 0), 'A');
        points.put(new Cell(4, 0),'A');
        points.put(new Cell(1,0),'a');
        points.put(new Cell(3,0),'a');
        points.put(new Cell(2, 0),'a');

        points.put(new Cell(2, 3),'B');
        points.put(new Cell(2,1),'B');
        points.put(new Cell(2,2),'b');

        points.put(new Cell(0, 1),'C');
        points.put(new Cell(0, 3),'C');
        points.put(new Cell(0, 2),'c');

        points.put(new Cell(1, 1),'D');
        points.put(new Cell(1, 3),'D');
        points.put(new Cell(1, 2),'d');

        points.put(new Cell(3, 1),'E');
        points.put(new Cell(3, 3),'E');
        points.put(new Cell(3, 2),'e');

        points.put(new Cell(4, 1),'F');
        points.put(new Cell(4, 3),'F');
        points.put(new Cell(4, 2),'f');

        Printer.printGrid(points, rows, cols);
        System.out.println(puzzleSolved(points, rows, cols));
    }
    public static Map<Cell, Character> givens(Map<Cell, Character> roots, Map<Cell, Character> children, int rows, int cols){
        Map<Cell, Character> givens = new HashMap<>();
        Map<Cell, Character> remainingRoots = new HashMap<>();
        for(Map.Entry<Cell, Character> r1 : roots.entrySet()){
            Cell point1 = r1.getKey();
            Cell point2 = r1.getKey();
            int x1 = point1.x();
            int y1 = point1.y();
            int x2=0;
            int y2=0;
            //list to store root pairs
            for(Map.Entry<Cell, Character> r2 : roots.entrySet()){
                List<Cell> rootPair = new ArrayList<>();
                point2 = r2.getKey();
                if(givens.containsKey(point1)) break;
                if(roots.get(point1).equals(roots.get(point2))&&!point1.equals(point2)){
                    rootPair.add(point1);
                    rootPair.add(point2);
                    x2 = point2.x();
                    y2 = point2.y();
                }
            }
            double dist = Math.sqrt(Math.pow(x1-x2, 2) + Math.pow(y1-y2, 2));
            if(dist == 2.0){
                int mX = (x1+x2)/2;
                int mY = (y1+y2)/2;
                Cell midpoint = new Cell(mX, mY);
                givens.put(midpoint, Character.toLowerCase(roots.get(point1)));
            }
            else{
                remainingRoots.put(point1, roots.get(point1));
                remainingRoots.put(point2, roots.get(point2));
            }
        }
        Map<Cell, Character> allPoints = combineThree(roots, givens, children);
        if(puzzleSolved(allPoints, rows, cols)) return givens;

        //solution when all other paths are complete
        // Map<Cell, Character> possiblePoints = new HashMap<>();
        for(Map.Entry<Cell, Character> root : remainingRoots.entrySet()){
            List<Cell> nesw = NESW(root.getKey());
            for (Cell nb : nesw) {
                if (allPoints.containsKey(nb)) break;
                // else{
                    
                // }
            }
        }
        return givens;
    }

    public static List<Cell> NESW(Cell p){
        Cell n = p.north();
        Cell e = p.east();
        Cell s = p.south();
        Cell w = p.west();
        return List.of(n,e,s,w);
    }

    public static Map<Cell, Character> combinePoints(Map<Cell, Character> i, Map<Cell, Character> ii){
        Map<Cell, Character> points = new HashMap<>();
        for(Map.Entry<Cell, Character> entry : i.entrySet()){
            points.put(entry.getKey(), entry.getValue());
        }
        for(Map.Entry<Cell, Character> entry : ii.entrySet()){
            points.put(entry.getKey(), entry.getValue());
        }
        return points;
    }

    public static Map<Cell, Character> combineThree(Map<Cell, Character> i, Map<Cell, Character> ii, Map<Cell, Character> iii){
        return combinePoints(i, combinePoints(ii,iii));
    }

    

    public static boolean isContinuous(Map<Cell, Character> points, Cell root, int rows, int cols){
        for(Map.Entry<Cell, Character> entry : points.entrySet()){
        //only run for color of root
        if ((points.get(entry.getKey())+"").equalsIgnoreCase(points.get(root)+"")){
        if(!root.inBounds(rows, cols)) return false;
        List<Cell> nesw = NESW(root);
        Cell ne = root.NE();
        Cell nw = root.NW();
        Cell se = root.SE();
        Cell sw = root.SW();
        char ch = points.get(root);
        int adj=0;
        int rootAdj=0;
        //corners of paths cannot touch
        for (Cell nb: List.of(ne, nw, se, sw)){
            if (points.containsKey(nb) &&
            Character.toLowerCase(points.get(nb)) == Character.toLowerCase(ch)) return false;
        }
        // within each point check all points to see which are adjacent
        for (Cell nb : nesw) {
            if (points.containsKey(nb) &&
            Character.toLowerCase(points.get(nb)) == Character.toLowerCase(ch)) {
                if(isRoot(ch))rootAdj++;
                else adj++;
            }
        }
        if(
            !((adj==2&&!isRoot(ch))||
            (rootAdj==1&&isRoot(ch)))
         ) return false;
    }
    }
    return true;
    }

    public static boolean puzzleSolved(Map<Cell, Character> points, int rows, int cols){
        //if the map is full
        if(points.size()!=rows*cols) return false;
        //each path borders itself once for a root, twice otherwise
        //AKA test that each color is continuous
        Map<Character, Integer> roots = new HashMap<>();
        for(Map.Entry<Cell, Character> root: points.entrySet()){
            if(!isContinuous(points, root.getKey(), rows, cols)) return false;
            //check if there are two roots per color
            char ch = points.get(root.getKey());
            if (isRoot(ch)) {
                roots.put(ch, roots.getOrDefault(ch, 0) + 1);
            }
        }
        for (int count : roots.values()) {
            if (count != 2) return false;
        }
        return true;
    }

    public static boolean isRoot(char c){
        return Character.isUpperCase(c);
    }

    public static boolean cutsPath(Map<Cell, Character> points, char pathColor){
        //make hashmap of root colored path
        Map<Cell, Character> colorSet = new HashMap<>();
        for(Map.Entry<Cell, Character> point : points.entrySet()){
            if(points.get(point.getKey())==pathColor){
                colorSet.put(point.getKey(), points.get(point.getKey()));
            }
        }
        //r1 will move towards r2 until they collide
        //then r1 will pick a direction and go around the obstacle until it reaches r2
        //if r1 reaches border it will backtrack in other direction
        //if it reaches border again cutsPath = true
        return false;
    }

}


/**Strategies
 * one path cannot block another
 * roots on the outside "want" to go around the outside - connected to first?
 * Do first the ones where we know the solution exactly
 * 
 * 
 * 
 * Heuristics:
 * paths cannot cross
 * all squares must be filled
 * paths must fit within bounds
 * no "zigzagging"
 * 
 * 
 * First, I should program something that tells whether or not the puzzle:
 * i) has a solution
 * ii) has only one solution
 * iii) if it has a solution, are all boxes filled?
 */