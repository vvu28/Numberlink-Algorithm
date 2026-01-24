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

        Cell to = new Cell(0, 1);
        Cell from = new Cell(8, 4);
        roots.put(to, new Point('f', true));
        roots.put(from, new Point('f', true));

        roots.put(new Cell(2, 2),new Point('g', true));
        roots.put(new Cell(6, 1),new Point('g', true));

        roots.put(new Cell(0, 6),new Point('h', true));
        roots.put(new Cell(6, 8),new Point('h', true));

        roots.put(new Cell(2, 4),new Point('i', true));
        roots.put(new Cell(8, 5),new Point('i', true));

        Map<Character, Path> paths = initPaths(roots);
        Puzzle puzzle = new Puzzle(rows, cols, roots, paths);
        Printer.printGrid(puzzle);
        System.out.println("\n\n\n");
        // System.out.println(canTravel(to, from, puzzle, new HashSet<>()));
        // puzzle = forcedDirection(puzzle);
        puzzle = tryPaths(puzzle);
        Printer.printGrid(puzzle);
    }

    public static Puzzle tryPaths(Puzzle puzzle){
        if(puzzle.isSolved()) return puzzle; //base case
        Map<Cell, Point> points = puzzle.getPoints();
        // pick next point
        Cell next = pickNextPoint(puzzle);
        if (next == null){
            return puzzle;
        }
        
        //find other endpoint
        char color = points.get(next).color();
        Map<Character, List<Cell>> endpoints = endpoints(puzzle);
        Cell to = null;
        for(Cell ep : endpoints.get(color)){
            if(ep != next) to = ep;
        }

        List<Cell> moves = orderMoves(to, next, puzzle);
        for(Cell move : moves){
            Puzzle nextPuzz = puzzle.withMove(color, move);
            Printer.printGrid(nextPuzz);
            if(cutsPath(nextPuzz)) continue;
            nextPuzz = forcedDirection(nextPuzz);
            Printer.printGrid(nextPuzz);

            Puzzle solved = tryPaths(nextPuzz);
            if (solved.isSolved()) return solved;
        }
        return puzzle;
    }

    public static boolean canTravel(Cell from, Cell to, Puzzle puzzle) {
    Set<Cell> visited = new HashSet<>();
    Deque<Cell> stack = new ArrayDeque<>();
    stack.push(from);
    visited.add(from);

    Map<Cell, Point> points = puzzle.getPoints();

    while (!stack.isEmpty()) {
        Cell cur = stack.pop();
        if (cur.equals(to)) return true;

        for (Cell nb : cur.neighbors(puzzle)) {
            if (visited.contains(nb)) continue;

            // can move into empty cells or the target
            if (!points.containsKey(nb) || nb.equals(to)) {
                visited.add(nb);
                stack.push(nb);
            }
        }
    }
    return false;
}

    // public static boolean reachable(Cell move, Puzzle puzzle){
    //     Map<Character, List<Cell>> endpoints = endpoints(puzzle);
    //     List<Cell> nbs = move.neighbors(puzzle);

    //     //every empty neighbor must be reachable by both endpoints of some color
    //     for(Cell nb : nbs){ // every neighbor
    //         for(Map.Entry<Character, List<Cell>> entry : endpoints.entrySet()){ // endpoints of every color
    //             List<Cell> eps = entry.getValue();
    //             if(pathIsComplete(entry.getKey(), puzzle) || eps.size() != 2) continue;
    //             if (canTravel(nb, eps.get(0), puzzle) || canTravel(nb, eps.get(1), puzzle)) return true;
    //         }
    //     }
    //     return false;
    // }

    //if one root cannot travel to another
    public static boolean cutsPath(Puzzle puzzle){
        Map<Character, List<Cell>> endpoints = endpoints(puzzle);
        for(Map.Entry<Character, List<Cell>> entry : endpoints.entrySet()){
            List<Cell> eps = entry.getValue();
            if(eps.size() != 2) continue;
            if(!canTravel(eps.get(0), eps.get(1), puzzle)) return true;
        }
        return false;
    }

    public static Cell pickNextPoint(Puzzle puzzle){
    Map<Character, List<Cell>> eps = endpoints(puzzle);
    Map<Cell, Point> points = puzzle.getPoints();
    Set<Cell> epSet = condenseMap(eps);
    Cell best = null;
    double bestScore = Double.POSITIVE_INFINITY;

    for (Cell ep : epSet) {
        int moves = 4 - legalMoves(puzzle, ep).size();
        int oob = 4 - ep.neighbors(puzzle).size();

        if (moves == 0) return ep; // immediate failure
        double score = 0;
        score += moves * 10;                    // MRV (dominant)
        score += epDistance(ep, points.get(ep).color(), puzzle);    // pull toward target
        score -= oob * 2;                       // edge pressure

        if (score < bestScore) {
            bestScore = score;
            best = ep;
        }
    }
    return best;
    }

    public static boolean isValidMove(Cell to, Cell from, Puzzle puzzle){
        Map<Cell, Point> points = new HashMap<>(puzzle.getPoints());
        char color = points.get(from).color();
        Puzzle withMove = puzzle.withMove(color, to);

        if(points.containsKey(to)) return false; //point already on grid
        if(!to.inBounds(puzzle)) return false; //OB
        if(!from.neighbors(puzzle).contains(to)) return false; //adjacency

        Map<Character, List<Cell>> eps = endpoints(puzzle);
        List<Cell> colorEps = eps.get(color);
        if (colorEps == null || !colorEps.contains(from)) return false; //if path is invalid, no endpoints

        //count same-color neighbors
        Map<Cell, List<Cell>> sameNeighbors = sameNeighbors(withMove);
        boolean fromIsRoot = points.get(from).isRoot();
        int fromNBS = sameNeighbors.get(from).size();

        if (!pathIsComplete(color, withMove) && sameNeighbors.get(to).size() != 1) return false;
        if (!fromIsRoot && fromNBS != 2) return false;
        if(fromIsRoot && fromNBS != 1) return false;

        return true;
    }
    
    public static Set<Cell> legalMoves(Puzzle puzzle, Cell cell){
        Set<Cell> legalMoves = new HashSet<>();
        List<Cell> nbs = cell.neighbors(puzzle);
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
            if (movesTotal != 1 || pathIsComplete(color, puzzle)) continue;
            Cell legalMove = legalMoves(puzzle, cell).iterator().next();
            puzzle = puzzle.withMove(color, legalMove);
            // Printer.printGrid(puzzle);
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
            List<Cell> nbs = entry.getValue();
            for(Cell cell : nbs){
                if(nbs != null && points.get(entry.getKey()).color() == points.get(cell).color()) same.add(cell);
            }
            sameNeighbors.put(entry.getKey(), same);
        }
        return sameNeighbors;
    }

    //doesn't include null neighbors
    public static Map<Cell, List<Cell>> filledNeighbors(Puzzle puzzle){
        Map<Cell, List<Cell>> filledNeighbors = new HashMap<>();
        Map<Cell, Point> points = puzzle.getPoints();
        for(Map.Entry<Cell, Point> entry : puzzle.getPoints().entrySet()){
            List<Cell> filled = new ArrayList<>();
            Cell cell = entry.getKey();
            for(Cell nb : cell.neighbors(puzzle)){
               if(points.containsKey(nb)) filled.add(nb);
            }
            filledNeighbors.put(cell, filled);
        }
        return filledNeighbors;
    }

    public static Map<Character, List<Cell>> endpoints(Puzzle puzzle){
        Map<Cell, Point> points = puzzle.getPoints();
        Map<Cell, List<Cell>> sameNBs = sameNeighbors(puzzle);
        Map<Character, List<Cell>> endpoints = new HashMap<>();

        for(Map.Entry<Cell, Point> p : points.entrySet()){
            Cell cell = p.getKey();
            Point point = p.getValue();
            int same = sameNBs.get(cell).size();
            boolean isRoot = point.isRoot();

            if( (same == 1 && !isRoot) || (same == 0 && isRoot)){
                char color = point.color();
                endpoints
                .computeIfAbsent(color, k -> new ArrayList<>())
                .add(cell);
            }
        }
        return endpoints;
    }

    public static List<Cell> orderMoves(Cell to, Cell from, Puzzle puzzle){ 
        List<Cell> ordered = new ArrayList<>();
        Set<Cell> legalMoves = new HashSet<>(legalMoves(puzzle, from));
        while(legalMoves.size() > 0){
            Cell best = bestMove(to, from, puzzle, legalMoves);
            legalMoves.remove(best);
            ordered.add(best);
        }
        return ordered;
    }

    public static Cell bestMove(Cell to, Cell from, Puzzle puzzle, Set<Cell> legalMoves){
        char color = puzzle.getPoints().get(from).color();
        Cell best = null;
        double highest = Integer.MAX_VALUE;
        for (Cell move : legalMoves){
            //score is a function of possible moves and distance to other endpoint
            double score = dist(move, to) + (4-move.neighbors(puzzle).size()) + (4-legalMoves(puzzle.withMove(color, move), move).size());
            if (score<highest){
                highest = score;
                best = move;
            } 
        }
        return best;
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
                    puzzle = puzzle.withMove(color, corner);
                } 
            }
        }
       return puzzle;
    }

    public static Puzzle distTwo(Puzzle puzzle){
        Map<Character, List<Cell>> endpoints = endpoints(puzzle);
        for(Map.Entry<Character, List<Cell>> entry : endpoints.entrySet()){
            //save coordinates of 2 cells
            List<Cell> eps = entry.getValue();
            Cell a = eps.get(0);
            Cell b = eps.get(1);
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

    public static int epDistance(Cell cell, char color, Puzzle puzzle){
        List<Cell> eps = endpoints(puzzle).get(color);
        if (eps.size() != 2) return 0;
        return dist(eps.get(0), cell) + dist(eps.get(1), cell); //return distance between endpoints
    }

    public static Map<Character, Path> initPaths(Map<Cell, Point> points){
        Map<Character, Path> paths = new HashMap<>();
        Map<Character, Set<Cell>> cellsByColor = cellsByColor(points);
        // Map<Character, Set<Cell>> roots = roots(points);
        for(Map.Entry<Character, Set<Cell>> entry : cellsByColor.entrySet()){
            char color = entry.getKey();
            Path path = createPath(color, points); 
            paths.put(color, path);
        }
        return paths;
    }

    public static Path createPath(char color, Map<Cell, Point> points){
        List<Cell> cells = new ArrayList<Cell>();
        Map<Character, Set<Cell>> coloredCells = cellsByColor(points);
        Set<Cell> members = coloredCells.get(color);
        for (Cell member : members){
            cells.addLast(member);
        }
        return new Path(cells, color);
    }

   //combines all of the sets in the map
    public static Set<Cell> condenseMap(Map<Character, List<Cell>> map){
        Set<Cell> set = new HashSet<>();
        for(Map.Entry<Character, List<Cell>> entry : map.entrySet()){
            set.addAll(entry.getValue());
        }
        return set;
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
    public static int dist(Cell one, Cell two){
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