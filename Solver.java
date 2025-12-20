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

        printGrid(points, rows, cols);
        System.out.println(puzzleSolved(points, rows, cols));
    }

    record Cell(int x, int y){
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

    public static boolean puzzleSolved(Map<Cell, Character> points, int rows, int cols){
        //if the map is full
        if(points.size()!=rows*cols) return false;
        boolean borderRules = false;
        //each path borders itself once for a root, twice otherwise
         Map<Character, Integer> roots = new HashMap<>();
        for(Cell key: points.keySet()){
            if(!key.inBounds(rows, cols)) return false;
            Cell n = key.north();
            Cell e = key.east();
            Cell s = key.south();
            Cell w = key.west();
            Cell ne = key.NE();
            Cell nw = key.NW();
            Cell se = key.SE();
            Cell sw = key.SW();
            char ch = points.get(key);
            int adj=0;
            int rootAdj=0;
            //corners of paths cannot touch
            for (Cell nb: List.of(ne, nw, se, sw)){
                if (points.containsKey(nb) &&
                Character.toLowerCase(points.get(nb)) == Character.toLowerCase(ch)) return false;
            }
            // within each point check all points to see which are adjacent
            for (Cell nb : List.of(n, e, s, w)) {
                if (points.containsKey(nb) &&
                Character.toLowerCase(points.get(nb)) == Character.toLowerCase(ch)) {
                    if(isRoot(ch))rootAdj++;
                    else adj++;
                }
            }
            if(
                (adj==2&&!isRoot(ch))||
                (rootAdj==1&&isRoot(ch))
             ) borderRules=true;
            else{
                System.out.println("fails at " + key);
                return false;
            }
            //check if there are two roots per color
            if (isRoot(ch)) {
                roots.put(ch, roots.getOrDefault(ch, 0) + 1);
            }
        }
        for (int count : roots.values()) {
            if (count != 2) return false;
        }
        return borderRules;
    }

    public static boolean isRoot(char c){
        return Character.isUpperCase(c);
    }

    //print grid to visualize points
    public static void printGrid(Map<Cell, Character> points, int rows, int cols) {
    for (int row = 0; row < rows; row++) {
        // Top border of row
        for (int col = 0; col < cols; col++) {
            System.out.print("+---");
        }
        System.out.println("+");
        // Cell contents
        for (int col = 0; col < cols; col++) {
            char cellChar = ' '; // default empty
            for (Cell key : points.keySet()) {
                if (key.x() == col && key.y() == row) {
                    cellChar = points.get(key); 
                    break;
                }
            }

            System.out.printf("| %c ", cellChar);
        }
        System.out.println("|");
    }

    // Bottom border
    for (int col = 0; col < cols; col++) {
        System.out.print("+---");
    }
    System.out.println("+");
}
}


/**Heuristics:
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