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

        Puzzle puzzle = new Puzzle(rows, cols, roots, null);
        Set<Path> paths = initPaths(puzzle);
        puzzle.setPaths(paths);
        Printer.printGrid(puzzle);
        System.out.println("\n\n\n");
        // distTwo(puzzle);
        forcedDirection(puzzle);
        Printer.printGrid(puzzle);
        System.out.println(pickNextPoint(puzzle));
    }

    public static Set<Path> initPaths(Puzzle puzzle){
        Set<Path> paths = new HashSet<>();
        Map<Cell, Point> points = puzzle.getPoints();
        Map<Character, Set<Cell>> cellsByColor = cellsByColor(points);
        Map<Character, Set<Cell>> roots = roots(points);
        for(Map.Entry<Character, Set<Cell>> entry : cellsByColor.entrySet()){
            char color = entry.getKey();
            Path path = new Path(color, roots.get(color), createPath(color, puzzle)); 
            paths.add(path);
        }
        return paths;
    }

    public static void tryPaths(Puzzle puzzle){
        Map<Cell, Point> points = puzzle.getPoints();
        Map<Character, Set<Cell>> coloredCells = cellsByColor(points);
        Cell next = pickNextPoint(puzzle);
        Set<Cell> def = coloredCells.get(points.get(next).color());
        Deque<Cell> path = new ArrayDeque<>(setToStack(def));
        if(movesTotal(puzzle, next) == 0){
        }
        Set<Cell> moves = legalMoves(puzzle, next);
        for(Cell move : moves){
            path.addLast(move);
            tryPaths(puzzle);
        }
    }

    public static Deque<Cell> createPath(char color, Puzzle puzzle){
        Deque<Cell> path = new ArrayDeque<Cell>();
        Map<Cell, Point> points = puzzle.getPoints();
        Map<Character, Set<Cell>> coloredCells = cellsByColor(points);
        Set<Cell> members = coloredCells.get(color);
        for (Cell member : members){
            path.addLast(member);
        }
        return path;
    }

    public static Deque<Cell> setToStack(Set<Cell> cells){
        Deque<Cell> stack = new ArrayDeque<>();
        for(Cell cell : cells){
            stack.push(cell);
        }
        return stack;
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
    Map<Cell, List<Cell>> neighbors = neighbors(puzzle);
    Cell best = null;
    double bestScore = Double.POSITIVE_INFINITY;

    for (Cell ep : epSet) {
        int moves = 0;
        int oob = 0;
        for (Cell nb : neighbors.get(ep)) {
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
        Map<Cell, Point> points = puzzle.getPoints();
        Map<Cell, List<Cell>> neighbors = neighbors(puzzle);
        Set<Cell> possibleDirections = new HashSet<>();
        for(Cell nb : neighbors.get(cell)){
            if((!points.containsKey(nb)) && nb != null) possibleDirections.add(nb);
        }
        return possibleDirections;
    }

    public static int movesTotal(Puzzle puzzle, Cell cell){
        Set<Cell> moves = legalMoves(puzzle, cell);
        if(moves == null) return 0;
        return moves.size();
    }

    public static void forcedDirection(Puzzle puzzle){
        //execute for all points that do not belong to continuous colors, and then repeat for added points
        Map<Cell, Point> allPoints = puzzle.getPoints();
        Map<Cell, List<Cell>> neighbors = neighbors(puzzle);
        Map<Cell, Point> forced = new HashMap<>();
        // Map<Cell, List<Cell>> sameNeighbors = sameNeighbors(puzzle);
        for(Map.Entry<Cell, Point> entry: allPoints.entrySet()){
            int movesTotal = movesTotal(puzzle, entry.getKey());
            if (movesTotal != 1 || pathIsComplete(puzzle, entry.getValue().color())) continue;
            // System.out.println(entry + ": " + movesTotal);
            for(Cell nb : neighbors.get(entry.getKey())){
                if((!allPoints.containsKey(nb)) && nb != null){
                    forced.put(nb, new Point(entry.getValue().color(), false));
                    break;
                }
            }

            // to check if any of the diagonals are roots of same color
            // Map<Cell, Point> diags = Cell.diagonalNBS(entry.getKey(), entry.getValue(), puzzle);
            // if(diags == null) continue;
            // int rootDiags = 0;
            // for(Map.Entry<Cell, Point> diag : diags.entrySet()){
            //     if(diag.getValue().isRoot()) rootDiags++;
            // }

            // // for when a point is forced one direction so that it doesn't "snake"
            // if (possibleDirections.size()==2 && diags.size() == 1 && rootDiags == 0){
            //     // System.out.println(entry.getKey() + ", " + Cell.diagonalNBS(entry.getKey(), entry.getValue(), allPoints));
            //     //add neighbor that will be adjacent once, not twice
            //     // for(Cell direction : possibleDirections){
            //     Cell direction1 = possibleDirections.get(0);
            //     Cell direction2 = possibleDirections.get(1);
            //     Cell direction = null;

            //     //test direction1
            //     {
            //         Map<Cell, Point> newPoints = new HashMap<>(allPoints);
            //         newPoints.put(direction1, new Point(color, false));
            //         Map<Cell, List<Cell>> newNeighbors = filledNeighbors(puzzle); //todo make newPuzzle
            //         // System.out.println(entry + ", " + direction1 + ", " + direction2);
            //         List<Cell> dNB = new ArrayList<>(newNeighbors.get(direction1));
            //         if(dNB != null && dNB.size()==1) direction = direction1;
            //     }

            //     //test direction2
            //     if(direction == null){
            //         Map<Cell, Point> newPoints = new HashMap<>(allPoints);
            //         newPoints.put(direction2, new Point(color, false));
            //         Map<Cell, List<Cell>> newNeighbors = filledNeighbors(puzzle);
            //         List<Cell> dNB = new ArrayList<>(newNeighbors.get(direction2));
            //         if(dNB != null && dNB.size()==1) direction = direction2;
            //     }

            //     if (direction == null) continue;
            //     System.out.println(direction + ", " + color);
            //     if(isValidMove(direction, puzzle)){
            //         forced.put(direction, new Point(color, false));
            //     }
        //     }

        }
        // Printer.printGrid(puzzle);
        if (forced.size()!=0){
            // System.out.println(forced);
            puzzle.setPoints(combinePoints(puzzle.getPoints(), forced));
            forcedEdges(puzzle);
            forcedDirection(puzzle);
        }
        forcedEdges(puzzle);
    }

    //neighbors of same color
    public static Map<Cell, List<Cell>> sameNeighbors(Puzzle puzzle){
        Map<Cell, Point> allPoints = puzzle.getPoints();
        Map<Cell, List<Cell>> neighbors = filledNeighbors(puzzle);
        Map<Cell, List<Cell>> sameNeighbors = new HashMap<>();
        //go through all keys
        for(Map.Entry<Cell, List<Cell>> entry : neighbors.entrySet()){
            List<Cell> same = new ArrayList<>();
            //go through list of neighbors
            for(Cell cell : entry.getValue()){
                if(cell == null || allPoints.get(entry.getKey()) == null || allPoints.get(cell) == null) continue;
                if(allPoints.get(entry.getKey()).color() == allPoints.get(cell).color()) same.add(cell);
            }
            sameNeighbors.put(entry.getKey(), same);
        }
        return sameNeighbors;
    }

    //doesn't include null neighbors
    public static Map<Cell, List<Cell>> filledNeighbors(Puzzle puzzle){
        Map<Cell, List<Cell>> neighbors = neighbors(puzzle);
        Map<Cell, List<Cell>> filledNeighbors = new HashMap<>();
        for(Map.Entry<Cell, Point> entry : puzzle.getAllPoints().entrySet()){
            List<Cell> filled = new ArrayList<>();
            for(Cell nb : neighbors.get(entry.getKey())){
                if(puzzle.getPoints().containsKey(nb)) filled.add(nb);
            }
            filledNeighbors.put(entry.getKey(), filled);
        }
        return filledNeighbors;
    }

    public static Map<Cell, List<Cell>> neighbors(Puzzle puzzle){
        Map<Cell, Point> points = puzzle.getAllPoints();
        Map<Cell, List<Cell>> neighbors = new HashMap<>();
        for(Map.Entry<Cell, Point> entry: points.entrySet()){
        Cell p = entry.getKey();
        Cell n = p.north(puzzle);
        Cell e = p.east(puzzle);
        Cell s = p.south(puzzle);
        Cell w = p.west(puzzle);
        List<Cell> nesw = new ArrayList<>(Arrays.asList(n, e, s, w));
        neighbors.put(p, nesw);
        }
        return neighbors;
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

    //fix
    public static boolean isValidMove(Cell moveKey, Puzzle puzzle){
        if(!moveKey.inBounds(puzzle)) return false;
        Map<Cell, Point> allPoints = new HashMap<>(puzzle.getAllPoints());
        Point moveValue = allPoints.get(moveKey);
        allPoints.put(moveKey, moveValue);
        Puzzle newPuzzle = new Puzzle(puzzle.getRows(), puzzle.getCols(), allPoints, null);
        Map<Cell, List<Cell>> sameNeighbors = sameNeighbors(newPuzzle);
        // System.out.println(moveKey);
        for(Cell nb : sameNeighbors.get(moveKey)){
            // if the neighboring cell is a root and the root already has a neighbor, invalid
            Point nbVal = allPoints.get(nb);
            if(nbVal.isRoot() && sameNeighbors.get(nb).size()!=1) return false;
        }
        // System.out.println(moveKey + ", " + sameNeighbors.get(moveKey).size());
        if(sameNeighbors.get(moveKey).size() == 1) return true;
        return false;
    }

    public static void forcedEdges(Puzzle puzzle){
        Map<Cell, Point> edgePoints = edgePoints(puzzle);
        Map<Cell, Point> forcedEdges = new HashMap<>();
        int rows = puzzle.getRows();
        int cols = puzzle.getCols();
        List<Cell> corners = new ArrayList<>(List.of(new Cell(0,0), new Cell(0, rows-1), new Cell(cols-1, 0), new Cell(cols-1, rows-1)));
        for(Map.Entry<Cell, Point> entry: edgePoints.entrySet()){
            for(Cell corner: corners){
                if (puzzle.getPoints().containsKey(corner)) continue;
                if (dist(corner, entry.getKey())==1){
                    // System.out.println(entry);
                    Point newPoint = new Point (entry.getValue().color(), false);
                    forcedEdges.put(corner, newPoint);
                } 
            }
        }
        puzzle.setPoints(combinePoints(puzzle.getPoints(), forcedEdges));
    }

    public static void distTwo(Puzzle puzzle){
        Map<Character, Set<Cell>> endpoints = endpoints(puzzle);
        Map<Cell, Point> givens = new HashMap<>();
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
            givens.put(midpoint, new Point(entry.getKey(), false));
        }
        puzzle.setPoints(combinePoints(givens, puzzle.getPoints()));
    }

    public static boolean pathIsComplete(Puzzle puzzle, char color){
        Map<Cell, Point> points = puzzle.getPoints();
        Map<Cell, List<Cell>> neighbors = neighbors(puzzle);
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

    //A point on the edge with dist 1 to a corner will always travel to the corner
    public static Map<Cell, Point> edgePoints(Puzzle puzzle){
        Map<Cell, Point> allPoints = puzzle.getPoints();
        Map<Cell, Point> edgePoints = new HashMap<>();
        Map<Cell, List<Cell>> neighbors = neighbors(puzzle);
        for(Map.Entry<Cell, Point> entry: allPoints.entrySet()){
            List<Cell> nbs = neighbors.get(entry.getKey());
            int OB = 0;
            for(Cell nb: nbs){
                if(nb==null) OB++;
            }
            if(OB==1) edgePoints.put(entry.getKey(), entry.getValue());
        }
        return edgePoints;
    }


    //a path cannot come between two roots
    public static boolean cutsPath(Puzzle puzzle, char pathColor){
        Map<Cell, Point> points = puzzle.getPoints();
        Map<Cell, List<Cell>> neighbors = neighbors(puzzle);
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
        while(!pathIsComplete(puzzle, entry.getValue().color())){
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

    // public static Map<Cell, Point> findRoots(Map<Cell, Point> points, char color){
    //     Map<Cell, Point> roots = roots(points);
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
        Map<Cell, Point> points = new HashMap<>(i);
        for(Map.Entry<Cell, Point> entry : ii.entrySet()){
            points.put(entry.getKey(), entry.getValue());
        }
        return points;
    }

    // public static Map<Cell, Point> combineThree(Map<Cell, Point> i, Map<Cell, Point> ii, Map<Cell, Point> iii){
    //     return combinePoints(i, combinePoints(ii,iii));
    // }
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