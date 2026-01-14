import java.util.*;
public class Solver{
    public static void main(String[] args){
        //for now just manually place points
        Map<Cell, Point> roots = new HashMap<>();
        int rows = 9;
        int cols = 9;
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

        Map<Character, Path> paths = initPaths(roots);
        Puzzle puzzle = new Puzzle(rows, cols, roots, paths);
        System.out.println("test");

        Printer.printGrid(puzzle);
        System.out.println("\n\n\n");
        puzzle = forcedDirection(puzzle);
        Printer.printGrid(puzzle);
    }

    public static Map<Character, Path> initPaths(Map<Cell, Point> points){
        Map<Character, Path> paths = new HashMap<>();
        Map<Character, Set<Cell>> cellsByColor = cellsByColor(points);
        // Map<Character, Set<Cell>> roots = roots(points);
        for(Map.Entry<Character, Set<Cell>> entry : cellsByColor.entrySet()){
            char color = entry.getKey();
            Path path = new Path(createPath(color, points)); 
            paths.put(color, path);
        }
        return paths;
    }

    public static boolean tryPaths(Puzzle puzzle){
        if(puzzle.isSolved()) return true; //base case
        Map<Cell, Point> points = puzzle.getPoints();
        // Map<Character, Path> paths = puzzle.getPaths();
        // pick next point
        Cell next = pickNextPoint(puzzle);
        if (next == null) return false;
        char color = points.get(next).color();
        // List<Cell> path = paths.get(color).points();
        Set<Cell> moves = legalMoves(puzzle, next);
        for(Cell move : moves){
            Puzzle nextPuzz = puzzle.withMove(color, move);
            if (tryPaths(nextPuzz)) return true;
        }
        return false;
    }

    public static List<Cell> createPath(char color, Map<Cell, Point> points){
        List<Cell> path = new ArrayList<Cell>();
        Map<Character, Set<Cell>> coloredCells = cellsByColor(points);
        Set<Cell> members = coloredCells.get(color);
        for (Cell member : members){
            path.addLast(member);
        }
        return path;
    }

    /* I need some criteria to pick the next point to build from.
        
        Some function of the following: 
        - possible moves
        - distance from point to other root
        - distance from point + other root to the edge
    */
    public static Set<Cell> condenseMap(Map<Character, Set<Cell>> map){
        Set<Cell> set = new HashSet<>();
        for(Map.Entry<Character, Set<Cell>> entry : map.entrySet()){
            set.addAll(entry.getValue());
        }
        return set;
    }

    public static Cell pickNextPoint(Puzzle puzzle){
    Map<Character, Set<Cell>> eps = endpoints(puzzle);
    Set<Cell> epSet = condenseMap(eps);
    Cell best = null;
    double bestScore = Double.POSITIVE_INFINITY;

    for (Cell ep : epSet) {
        int moves = 0;
        int oob = 0;
        for (Cell nb : ep.neighbors(puzzle)) {
            if (nb == null) oob++;
            else if (!puzzle.getPoints().containsKey(nb)) moves++;
        }
        if (moves == 0) return ep; // immediate failure
        double score = 0;
        score += moves * 10;                    // MRV (dominant)
        score += rootDistance(ep, puzzle);    // pull toward target
        score -= oob * 2;                       // edge pressure

        if (score < bestScore) {
            bestScore = score;
            best = ep;
        }
    }
    return best;
    }

    public static double rootDistance(Cell cell, Puzzle puzzle){
        double lowest = Double.POSITIVE_INFINITY;
        Map<Cell, Point> points = puzzle.getPoints();
        char color = points.get(cell).color();
        Set<Cell> coloredCells = cellsByColor(points).get(color);
        for(Cell c : coloredCells){
            if(c == cell) continue;
            double dist = dist(cell, c);
            if(dist < lowest) lowest = dist;
        }
        return lowest;
    }
    
    public static Set<Cell> legalMoves(Puzzle puzzle, Cell cell){
        Set<Cell> legalMoves = new HashSet<>();
        List<Cell> nbs = cell.neighbors(puzzle);
        if(nbs.size() == 0) return null;
        for(Cell nb : nbs){
            if(isValidMove(nb, cell, puzzle)) legalMoves.add(nb);
        }
        return legalMoves;
    }

    public static int movesTotal(Puzzle puzzle, Cell cell){
        Set<Cell> moves = legalMoves(puzzle, cell);
        return moves.size();
    }

    public static Puzzle forcedDirection(Puzzle puzzle){
        //execute for all points that do not belong to continuous colors, and then repeat for added points
        Map<Cell, Point> points = puzzle.getPoints();
        int forced = 0;
        for(Map.Entry<Cell, Point> entry: points.entrySet()){
            Cell cell = entry.getKey();
            int movesTotal = movesTotal(puzzle, cell);
            char color = entry.getValue().color();
            // System.out.println(entry);
            if (movesTotal != 1 || pathIsComplete(color, puzzle)) continue;
            System.out.println(entry + ": " + movesTotal);
            Cell legalMove = legalMoves(puzzle, cell).iterator().next();
            puzzle = puzzle.withMove(color, legalMove);
            Printer.printGrid(puzzle);
            forced++;
        }
        if (forced > 0){
            puzzle = forcedEdges(puzzle);
            return forcedDirection(puzzle);
        }
        return forcedEdges(puzzle);
    }

    //neighbors of same color
    public static Map<Cell, List<Cell>> sameNeighbors(Puzzle puzzle){
        Map<Cell, Point> points = puzzle.getPoints();
        Map<Cell, List<Cell>> neighbors = filledNeighbors(puzzle);
        Map<Cell, List<Cell>> sameNeighbors = new HashMap<>();
        //go through all keys
        for(Map.Entry<Cell, List<Cell>> entry : neighbors.entrySet()){
            List<Cell> same = new ArrayList<>();
            //go through list of neighbors
            for(Cell cell : entry.getValue()){
                if(cell == null || points.get(entry.getKey()) == null || points.get(cell) == null) continue;
                if(points.get(entry.getKey()).color() == points.get(cell).color()) same.add(cell);
            }
            sameNeighbors.put(entry.getKey(), same);
        }
        return sameNeighbors;
    }

    //doesn't include null neighbors
    public static Map<Cell, List<Cell>> filledNeighbors(Puzzle puzzle){
        Map<Cell, List<Cell>> filledNeighbors = new HashMap<>();
        for(Map.Entry<Cell, Point> entry : puzzle.getPoints().entrySet()){
            List<Cell> filled = new ArrayList<>();
            Cell cell = entry.getKey();
            for(Cell nb : cell.neighbors(puzzle)){
                filled.add(nb);
            }
            filledNeighbors.put(cell, filled);
        }
        return filledNeighbors;
    }

    public static Map<Character, Set<Cell>> endpoints(Puzzle puzzle){
        Map<Cell, Point> points = puzzle.getPoints();
        Map<Character, Set<Cell>> endpoints = new HashMap<>();
        for(Map.Entry<Cell, Point> p : points.entrySet()){
            Cell cell = p.getKey();
            Point point = p.getValue();
            int same = sameNeighbors(puzzle).get(cell).size();
            boolean isRoot = point.isRoot();
            if( (same == 1 && !isRoot) || (same == 0 && isRoot)){
                char color = point.color();
                endpoints
                .computeIfAbsent(color, k -> new HashSet<>())
                .add(cell);
            }
        }
        return endpoints;
    }

    public static boolean isValidMove(Cell to, Cell from, Puzzle puzzle){
        Map<Cell, Point> points = new HashMap<>(puzzle.getPoints());
        if(points.containsKey(to)) return false; //point already on grid
        if(!to.inBounds(puzzle)) return false; //OB
        if(!from.neighbors(puzzle).contains(to)) return false; //adjacency
        char color = points.get(from).color();
        //count same-color neighbors
        Map<Cell, List<Cell>> sameNeighbors = sameNeighbors(puzzle.withMove(color, to));
        if (sameNeighbors.get(to).size() != 1) return false;
        if(puzzle.getPoints().get(from).isRoot() && sameNeighbors.get(from).size() != 1) return false;
        return true;
    }

    public static Puzzle forcedEdges(Puzzle puzzle){
        Map<Cell, Point> edgePoints = edgePoints(puzzle);
        int rows = puzzle.getRows();
        int cols = puzzle.getCols();
        List<Cell> corners = new ArrayList<>(List.of(new Cell(0,0), new Cell(0, rows-1), new Cell(cols-1, 0), new Cell(cols-1, rows-1)));
        for(Map.Entry<Cell, Point> entry: edgePoints.entrySet()){
            for(Cell corner: corners){
                if (puzzle.getPoints().containsKey(corner)) continue;
                char color = entry.getValue().color();
                Cell cell = entry.getKey();
                if (dist(corner, cell)==1){
                    System.out.println("yay!" + cell);
                    puzzle = puzzle.withMove(color, corner);
                } 
            }
        }
       return puzzle;
    }

    public static Puzzle distTwo(Puzzle puzzle){
        Map<Character, Set<Cell>> endpoints = endpoints(puzzle);
        for(Map.Entry<Character, Set<Cell>> entry : endpoints.entrySet()){
            //save coordinates of 2 cells
            Iterator<Cell> it = entry.getValue().iterator();
            Cell a = it.next();
            Cell b = it.next();
            if(dist(a, b) != 2.0) continue;

            //save midpoint
            int mX = (a.x()+b.x())/2;
            int mY = (a.y()+b.y())/2;
            Cell midpoint = new Cell(mX, mY);
            puzzle = puzzle.withMove(entry.getKey(), midpoint);
        }
        return puzzle;
    }

    public static boolean pathIsComplete(char color, Puzzle puzzle){
        Path path = puzzle.getPaths().get(color);
        List<Cell> points = path.points();
        Map<Cell, Point> map = puzzle.getPoints();
        Map<Cell, List<Cell>> sameNeighbors = sameNeighbors(puzzle);
        for(Cell cell : points){
            int same = sameNeighbors.get(cell).size();
            boolean isRoot = map.get(cell).isRoot();
            if(isRoot && same != 1) return false;
            if(!isRoot && same != 2) return false;
        }
        return true;
    }

    //A point on the edge with dist 1 to a corner will always travel to the corner
    public static Map<Cell, Point> edgePoints(Puzzle puzzle){
        Map<Cell, Point> allPoints = puzzle.getPoints();
        Map<Cell, Point> edgePoints = new HashMap<>();
        for(Map.Entry<Cell, Point> entry: allPoints.entrySet()){
            List<Cell> nbs = entry.getKey().neighbors(puzzle);
            if(nbs.size() == 3) edgePoints.put(entry.getKey(), entry.getValue());
        }
        return edgePoints;
    }


    //a path cannot come between two roots
    public static boolean cutsPath(Puzzle puzzle, char pathColor){
        Map<Cell, Point> points = puzzle.getPoints();
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
        while(!pathIsComplete(entry.getValue().color(), puzzle)){
            List<Cell> pointsNB = entry.getKey().neighbors(puzzle);
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
    public static Map<Character, Set<Cell>> cellsByColor(Map<Cell, Point> points) {
        Map<Character, Set<Cell>> cellsByColor = new HashMap<>();
        for (Map.Entry<Cell, Point> entry : points.entrySet()) {
            char color = entry.getValue().color();
            cellsByColor
                .computeIfAbsent(color, k -> new HashSet<>())
                .add(entry.getKey());
        }  
        return cellsByColor;
    }

    public static Map<Character, Set<Cell>> roots(Map<Cell, Point> points){
        Map<Character, Set<Cell>> roots = new HashMap<>();
        for(Map.Entry<Cell, Point> entry: points.entrySet()){
            Point point = entry.getValue();
            if (point.isRoot()) {
                char color = point.color();
                roots
                    .computeIfAbsent(color, k -> new HashSet<>())
                    .add(entry.getKey());
            }
        }
        return roots;
    }
    public static List<Cell> children(Map<Cell, Point> points){
        List<Cell> children = new ArrayList<>();
        for(Map.Entry<Cell, Point> point: points.entrySet()){
            if (!point.getValue().isRoot()) {
                children.add(point.getKey());
            }
        }
        return children;
    }

    public static Map<Cell, Point> combinePoints(Map<Cell, Point> i, Map<Cell, Point> ii){
        Map<Cell, Point> points = new HashMap<>(i);
        for(Map.Entry<Cell, Point> entry : ii.entrySet()){
            points.put(entry.getKey(), entry.getValue());
        }
        return points;
    }

    //Manhattan distance formula
    public static double dist(Cell one, Cell two){
        int x1 = one.x();
        int y1 = one.y();
        int x2 = two.x();
        int y2 = two.y();
        return Math.abs(x1-x2) + Math.abs(y1-y2);
    }
}


/**Strategies
 * one path cannot block another
 * roots on the outside "want" to go around the outside - connected to first?
 * Do first the ones where we know the solution exactly
 * 
 * 
 * Heuristics:
 * paths cannot cross
 * all squares must be filled
 * paths must fit within bounds
 * no "zigzagging"
 */