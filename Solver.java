import java.util.*;
public class Solver{
    public static void main(String[] args){
        //for now just manually place points
        Map<Cell, Point> roots = new HashMap<>();
        int rows = 9;
        int cols = 9;
        //keep roots capital for consistency
        roots.put(new Cell(7, 8), new Point('a', true));
        roots.put(new Cell(8, 7),new Point('a', true));

        roots.put(new Cell(1, 4), new Point('b', true));
        roots.put(new Cell(3,4),new Point('b', true));

        roots.put(new Cell(3, 5),new Point('c', true));
        roots.put(new Cell(7, 1),new Point('c', true));

        roots.put(new Cell(7,3),new Point('d', true));
        roots.put(new Cell(5, 5),new Point('d', true));

        roots.put(new Cell(4, 1),new Point('e', true));
        roots.put(new Cell(8, 6),new Point('e', true));

        roots.put(new Cell(0, 1),new Point('f', true));
        roots.put(new Cell(8, 4),new Point('f', true));

        roots.put(new Cell(2, 2),new Point('g', true));
        roots.put(new Cell(6, 1),new Point('g', true));

        roots.put(new Cell(0, 6),new Point('h', true));
        roots.put(new Cell(6, 8),new Point('h', true));

        roots.put(new Cell(2, 4),new Point('i', true));
        roots.put(new Cell(8, 5),new Point('i', true));

        Puzzle puzzle = new Puzzle(rows, cols, roots);
        Printer.printGrid(puzzle);
        // distTwo(puzzle);
        forcedDirection(puzzle);
        Printer.printGrid(puzzle);
        System.out.println(Puzzle.puzzleSolved(puzzle));
    }
    
    public static void forcedDirection(Puzzle puzzle){
        //execute for all points that do not belong to continuous colors, and then repeat for added points
        Map<Cell, Point> allPoints = puzzle.getPoints();
        Map<Cell, List<Cell>> neighbors = neighbors(allPoints);
        Map<Cell, Point> forced = new HashMap<>();
        for(Map.Entry<Cell, Point> entry: allPoints.entrySet()){
            if(pathIsComplete(allPoints, entry.getValue().color())) continue;
            List<Cell> possibleDirections = new ArrayList<>();
            for(Cell nb : neighbors.get(entry.getKey())){
                if((!allPoints.containsKey(nb))&&nb.inBounds(puzzle.getRows(), puzzle.getCols())) possibleDirections.add(nb);
            }
            // System.out.println(r1 + ", " + possibleDirections.size());
            if(possibleDirections.size()==1){
                Point newPoint = new Point(entry.getValue().color(), false);
                forced.put(possibleDirections.get(0), newPoint);
            }
        }
        if (forced.size()!=0){
            System.out.println(forced);
            puzzle.setPoints(combinePoints(allPoints, forced));
            forcedDirection(puzzle);
        }
    }

    public static void distTwo(Puzzle puzzle){
        Map<Cell, Point> roots = extractRoots(puzzle.getPoints());
        Map<Cell, Point> givens = new HashMap<>();
        for(Map.Entry<Cell, Point> r1 : roots.entrySet()){
            if(givens.containsValue(r1.getValue())) continue;
            Cell point1 = r1.getKey();
            Cell point2 = r1.getKey();
            int x1 = point1.x();
            int y1 = point1.y();
            int x2=0;
            int y2=0;
            //store root pairs
            for(Map.Entry<Cell, Point> r2 : roots.entrySet()){
                if(givens.containsKey(point1)) break;
                if(r1.getValue().equals(r2.getValue())&&!point1.equals(r2.getKey())){
                    point2 = r2.getKey();
                    x2 = point2.x();
                    y2 = point2.y();
                }
            }
            //find distances between root pairs
            double dist = dist(point1, point2);
            if(dist == 2.0){
                int mX = (x1+x2)/2;
                int mY = (y1+y2)/2;
                Cell midpoint = new Cell(mX, mY);
                Point newPoint = new Point(r1.getValue().color(), false);
                givens.put(midpoint, newPoint);
            }
        }
        puzzle.setPoints(combinePoints(givens, puzzle.getPoints()));
    }

    public static boolean pathIsComplete(Map<Cell, Point> points, char color){
        Map<Cell, List<Cell>> neighbors = neighbors(points);
        for(Map.Entry<Cell, Point> entry : points.entrySet()){
        Point point = entry.getValue();
        //only run for color of root
        if(point.color()!=color) continue;
        Cell key = entry.getKey();
        int adj=0;
        int rootAdj=0;
        // within each point check all points to see which are adjacent
        List<Cell> keysNeighbors = neighbors.get(key);
        for (Cell nb : keysNeighbors) {
            if (points.containsKey(nb) &&
            (points.get(nb).color()) == point.color()) {
                if(point.isRoot())rootAdj++;
                else adj++;
            }
        }
        if(
            !((adj==2&&!point.isRoot())||
            (rootAdj==1&&point.isRoot()))
         ) return false;
    }
    return true;
    }

    public static boolean cutsPath(Puzzle puzzle, char pathColor){
        Map<Cell, Point> points = puzzle.getPoints();
        Map<Cell, List<Cell>> neighbors = neighbors(points);
        //make hashmap of root colored path
        Map<Cell, Point> colorSet = new HashMap<>();
        //also set r1 and r2 as roots
        Map.Entry<Cell, Point> r1 = null;
        Map.Entry<Cell, Point> r2 = null;
        for(Map.Entry<Cell, Point> entry : points.entrySet()){
            if(entry.getValue().color()==pathColor){
                colorSet.put(entry.getKey(), entry.getValue());
                if(entry.getValue().isRoot() && r1==null) r1 = entry;
                else r2 = entry;
            }
        //travel shortest path r1-->r2
        //find distance between each nesw and r2 and choose shortest one
        while(!pathIsComplete(points, entry.getValue().color())){
            List<Cell> pointsNB = neighbors.get(entry.getKey());
            Cell shortest = pointsNB.get(0);
            for(Cell d: pointsNB){
                if(colorSet.containsKey(d)) break;
                if( dist(d, r2.getKey()) < dist(shortest, r2.getKey()) ) shortest = d;
            }
            colorSet.put(shortest, points.get(shortest));
        }
    }

        //r1 will move towards r2 until they collide
        //then r1 will pick a direction and go around the obstacle until it reaches r2
        //if r1 reaches border it will backtrack in other direction
        //if it reaches border again cutsPath = true
        return false;
    }

    //to create a map that stores all points belonging to each color
    public static Map<Character, Map<Cell, Point>> cellsByColor(Map<Cell, Point> points){
        Map<Character, Map<Cell, Point>> cellsByColor = new HashMap<>();
        Map<Cell, Point> roots = extractRoots(points);
        for(Map.Entry<Cell, Point> root : roots.entrySet()){
            if(cellsByColor.containsKey(root.getValue().color())) continue;
            Map<Cell, Point> coloredMap = new HashMap<>();
            for(Map.Entry<Cell, Point> child : points.entrySet()){
                if(root.getValue().color() == child.getValue().color()){
                    coloredMap.put(child.getKey(), child.getValue());
                }
            }
            cellsByColor.put(root.getValue().color(), coloredMap);
        }
        return cellsByColor;
    }

    public static Map<Cell, List<Cell>> neighbors(Map<Cell, Point> points){
        Map<Cell, List<Cell>> neighbors = new HashMap<>();
        for(Map.Entry<Cell, Point> entry: points.entrySet()){
        Cell p = entry.getKey();
        Cell n = p.north();
        Cell e = p.east();
        Cell s = p.south();
        Cell w = p.west();
        List<Cell> nesw = new ArrayList<>(Arrays.asList(n, e, s, w));
        neighbors.put(p, nesw);
        }
        return neighbors;
    }

    public static Map<Cell, Point> extractRoots(Map<Cell, Point> points){
        Map<Cell, Point> roots = new HashMap<>();
        for(Map.Entry<Cell, Point> point: points.entrySet()){
            if (point.getValue().isRoot()) {
                roots.put(point.getKey(), point.getValue());
            }
        }
        return roots;
    }

    // public static Map<Cell, Point> findRoots(Map<Cell, Point> points, char color){
    //     Map<Cell, Point> roots = extractRoots(points);
    //     Map<Cell, Point> colored = new HashMap<>();
    //     for(Map.Entry<Cell, Point> root: roots.entrySet()){
    //         if(root.getValue().color()==color){
    //             colored.put(root.getKey(), root.getValue());
    //         }
    //     }
    //     //  System.out.println(roots);
    //     return colored;
    // }

    public static Map<Cell, Point> combinePoints(Map<Cell, Point> i, Map<Cell, Point> ii){
        Map<Cell, Point> points = new HashMap<>();
        for(Map.Entry<Cell, Point> entry : i.entrySet()){
            points.put(entry.getKey(), entry.getValue());
        }
        for(Map.Entry<Cell, Point> entry : ii.entrySet()){
            points.put(entry.getKey(), entry.getValue());
        }
        return points;
    }

    public static Map<Cell, Point> combineThree(Map<Cell, Point> i, Map<Cell, Point> ii, Map<Cell, Point> iii){
        return combinePoints(i, combinePoints(ii,iii));
    }

    public static double dist(Cell one, Cell two){
        int x1 = one.x();
        int y1 = one.y();
        int x2 = two.x();
        int y2 = two.y();
        return Math.sqrt(Math.pow(x1-x2, 2) + Math.pow(y1-y2, 2));
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