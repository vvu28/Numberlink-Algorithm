import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
public class Solver{
    public static void main(String[] args){
        Puzzle puzzle = readFile(0);
        Printer.printGrid(puzzle);
        System.out.println("\n\n\n");

        long start = System.nanoTime(); // timer

        puzzle = tryPaths(puzzle);

        long end = System.nanoTime();
        long durationNs = end - start;
        Printer.printGrid(puzzle);

        System.out.println("\n\nnanoseconds:" + durationNs);
        System.out.println("seconds:" + durationNs/(Math.pow(10, 9)));
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
        Map<Character, List<Cell>> endpoints = puzzle.endpoints();

        List<Cell> eps = endpoints.get(color);
        if (eps == null || eps.size() != 2) {
            return puzzle;
        }

        Cell to = null;
        for(Cell ep : endpoints.get(color)){
            if(!ep.equals(next)) to = ep;
        }

        List<Cell> moves = orderMoves(to, next, puzzle);
        for(Cell move : moves){
            Puzzle nextPuzz = puzzle.withMove(color, move);
            // Printer.printGrid(nextPuzz);
            if (moves.size() != 1){
                if(hasDeadRegion(nextPuzz) || leavesGap(next, move, color, nextPuzz) || cutsPath(nextPuzz)) continue;
            }

            Puzzle solved = tryPaths(nextPuzz);
            if (solved.isSolved()) return solved;
        }
        return puzzle;
    }

    public static boolean hasDeadRegion(Puzzle puzzle) {
        Map<Cell, Point> points = puzzle.getPoints();
        int rows = puzzle.getRows();
        int cols = puzzle.getCols();
        Set<Cell> visited = new HashSet<>();

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                Cell cell = new Cell(x, y);
                if (points.containsKey(cell) || visited.contains(cell)) continue;

                // BFS to find this empty region
                Set<Cell> region = new HashSet<>();
                Queue<Cell> queue = new LinkedList<>();
                queue.add(cell);
                visited.add(cell);

                while (!queue.isEmpty()) {
                    Cell cur = queue.poll();
                    region.add(cur);
                    for (Cell nb : cur.neighbors(puzzle)) {
                        if (!visited.contains(nb) && !points.containsKey(nb)) {
                            visited.add(nb);
                            queue.add(nb);
                        }
                    }
                }

                // count endpoints bordering this region
                Set<Character> endpointColors = new HashSet<>();
                Map<Character, List<Cell>> endpoints = puzzle.endpoints();
                Set<Cell> epSet = condenseMap(endpoints);
                for (Cell regionCell : region) {
                    for (Cell nb : regionCell.neighbors(puzzle)) {
                        if (epSet.contains(nb)) {
                            endpointColors.add(points.get(nb).color());
                        }
                    }
                    if (epSet.contains(regionCell)) {
                        endpointColors.add(points.get(regionCell).color());
                    }
                }

                if (endpointColors.size() < 2) return true; // dead region
            }
        }
        return false;
    }

    public static boolean leavesGap(Cell from, Cell to, char color, Puzzle puzzle) {
        List<Cell> neighbors = from.emptyNeighbors(puzzle);
        Map<Character, List<Cell>> endpoints = puzzle.endpoints();

        for (Cell nb : neighbors){
            boolean canTravel = false;
            for(Map.Entry<Character, List<Cell>> entry : endpoints.entrySet()){ // go through each color
                if(entry.getKey() == color) continue;                           // same color as "to"
                for (Cell ep : entry.getValue()){                               //iterate through 2 endpoints
                    if (canTravel(ep, nb, puzzle)){
                        canTravel = true;          //if an endpoint can ever travel, move is okay :)
                        break;
                    }
                }
            }
            if(!canTravel) return true;
        }
        return false;
    }

    public static boolean canTravel(Cell from, Cell to, Puzzle puzzle) {
        if (!from.inBounds(puzzle) || !to.inBounds(puzzle)) return false;

        Map<Cell, Point> points = puzzle.getPoints();
        char color = points.containsKey(from) ? points.get(from).color() : 0;

        Set<Cell> visited = new HashSet<>();
        Queue<Cell> queue = new LinkedList<>();
        
        queue.add(from);
        visited.add(from);
        
        while (!queue.isEmpty()) {
            Cell current = queue.poll();
            if (current.equals(to)) return true;
            
            for (Cell neighbor : current.neighbors(puzzle)) {
                if (visited.contains(neighbor)) continue;
                if (!neighbor.equals(to) && points.containsKey(neighbor) && points.get(neighbor).color() != color) continue; // blocked by other colors
                visited.add(neighbor);
                queue.add(neighbor);
            }
        }
        return false;
    }

    //if one endpoint cannot travel to another
    public static boolean cutsPath(Puzzle puzzle){
        Map<Character, List<Cell>> endpoints = puzzle.endpoints();
        for(Map.Entry<Character, List<Cell>> entry : endpoints.entrySet()){
            List<Cell> eps = entry.getValue();
            if(eps.size() != 2) continue;
            if(!canTravel(eps.get(0), eps.get(1), puzzle)) return true;
        }
        return false;
    }

    public static Cell pickNextPoint(Puzzle puzzle){
    Map<Character, List<Cell>> eps = puzzle.endpoints();
    Map<Cell, Point> points = puzzle.getPoints();
    Set<Cell> epSet = condenseMap(eps);
    Cell best = null;
    double bestScore = Double.POSITIVE_INFINITY;

    for (Cell ep : epSet) {
        int legalMoves = legalMoves(puzzle, ep).size(); 
        if (legalMoves == 1) return ep; //automatically return ep if 1 possible move
        int oob = 4 - ep.neighbors(puzzle).size();

        if (legalMoves == 0) return ep; // immediate failure
        double score = 0;
        score += legalMoves * 10;                    // MRV (dominant)
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

        Map<Character, List<Cell>> eps = puzzle.endpoints();
        List<Cell> colorEps = eps.get(color);
        if (colorEps == null || !colorEps.contains(from)) return false; //if path is invalid, no endpoints

        //count same-color neighbors
        int toCount = 1; // from will connect to to
        for (Cell nb : to.neighbors(puzzle)) {
            if (nb.equals(from)) continue;

            Point p = points.get(nb);
            if (p != null && p.color() == color) {
                toCount++;
            }
        }

        int fromCount = 1; // to will connect to from
        for (Cell nb : from.neighbors(puzzle)) {
            Point p = points.get(nb);

            if (p != null && p.color() == color) {
                fromCount++;
            }
        }
        boolean fromIsRoot = points.get(from).isRoot();

        if (!pathIsComplete(color, withMove) && toCount != 1) return false;
        if (!fromIsRoot && fromCount != 2) return false;
        if(fromIsRoot && fromCount != 1) return false;

        return true;
    }

    public static boolean pathIsComplete(char color, Puzzle puzzle){
        List<Cell> eps = puzzle.endpoints().get(color);
        return eps == null || eps.isEmpty();
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

    public static int epDistance(Cell cell, char color, Puzzle puzzle){
        List<Cell> eps = puzzle.endpoints().get(color);
        if (eps.size() != 2) return 0;
        return dist(eps.get(0), cell) + dist(eps.get(1), cell); //return distance between endpoints
    }

   //combines all of the sets in the map
    public static Set<Cell> condenseMap(Map<Character, List<Cell>> map){
        Set<Cell> set = new HashSet<>();
        for(Map.Entry<Character, List<Cell>> entry : map.entrySet()){
            set.addAll(entry.getValue());
        }
        return set;
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

    public static Puzzle readFile(int i) {
        try (Scanner in = new Scanner(new File("TestingGrids.txt"))) {
            while (in.hasNextLine()) {
                String line = in.nextLine().trim();
                // find the right puzzle number
                if (line.equals(String.valueOf(i))) {
                    List<String> gridLines = new ArrayList<>();
                    while (in.hasNextLine()) {
                        String gridLine = in.nextLine();
                        if (gridLine.trim().isEmpty() || gridLine.trim().matches("\\d+")) break;
                        gridLines.add(gridLine);
                    }

                    int rows = gridLines.size();
                    int cols = gridLines.get(0).length();
                    Map<Cell, Point> points = new HashMap<>();

                    for (int y = 0; y < rows; y++) {
                        String row = gridLines.get(y); 
                        for (int x = 0; x < cols; x++) {
                            char c = row.charAt(x);
                            if (c != '-') {
                                points.put(new Cell(x, y), new Point(c, true));
                            }
                        }
                    }

                    return new Puzzle(rows, cols, points);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println("Puzzle " + i + " not found.");
        return null;
    }
}
