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
        System.out.println("\n\n\n");
        // distTwo(puzzle);
        forcedDirection(puzzle);
        Printer.printGrid(puzzle);
        System.out.println(Puzzle.puzzleSolved(puzzle));
    }

    public static void tryPaths(Puzzle puzzle){
        // pickNextPath(puzzle); //returns path
        Map<Cell, List<Cell>> neighbors = neighbors(puzzle);
        Map<Cell, Point> edgePoints = edgePoints(puzzle);
    }
    
    public static void forcedDirection(Puzzle puzzle){
        //execute for all points that do not belong to continuous colors, and then repeat for added points
        Map<Cell, Point> allPoints = puzzle.getPoints();
        Map<Cell, List<Cell>> neighbors = neighbors(puzzle);
        Map<Cell, Point> forced = new HashMap<>();
        Map<Cell, List<Cell>> sameNeighbors = sameNeighbors(puzzle);
        for(Map.Entry<Cell, Point> entry: allPoints.entrySet()){
            // if(pathIsComplete(allPoints, entry.getValue().color())) continue;
            char color = entry.getValue().color();
            if  (pathIsComplete(puzzle, color) ||
                (sameNeighbors.size() == 2 && !entry.getValue().isRoot()) ||
                (sameNeighbors.size() == 1 && entry.getValue().isRoot())) continue;
            List<Cell> possibleDirections = new ArrayList<>();
            for(Cell nb : neighbors.get(entry.getKey())){
                if((!allPoints.containsKey(nb))&&nb.inBounds(puzzle.getRows(), puzzle.getCols())) possibleDirections.add(nb);
            }
            if(possibleDirections.size()==1){
                forced.put(possibleDirections.get(0), new Point(color, false));
            }

            // to check if any of the diagonals are roots of same color
            Map<Cell, Point> diags = Cell.diagonalNBS(entry.getKey(), entry.getValue(), allPoints);
            if(diags == null) continue;
            int rootDiags = 0;
            for(Map.Entry<Cell, Point> diag : diags.entrySet()){
                if(diag.getValue().isRoot()) rootDiags++;
            }

            // for when a point is forced one direction so that it doesn't "snake"
            if (possibleDirections.size()==2 && diags.size() == 1 && rootDiags == 0){
                // System.out.println(entry.getKey() + ", " + Cell.diagonalNBS(entry.getKey(), entry.getValue(), allPoints));
                //add neighbor that will be adjacent once, not twice
                // for(Cell direction : possibleDirections){
                Cell direction1 = possibleDirections.get(0);
                Cell direction2 = possibleDirections.get(1);
                Cell direction = null;

                //test direction1
                {
                    Map<Cell, Point> newPoints = new HashMap<>(allPoints);
                    newPoints.put(direction1, new Point(color, false));
                    Map<Cell, List<Cell>> newNeighbors = filledNeighbors(puzzle); //todo make newPuzzle
                    // System.out.println(entry + ", " + direction1 + ", " + direction2);
                    if(newNeighbors.get(direction1).size()==1) direction = direction1;
                }

                //test direction2
                if(direction == null){
                    Map<Cell, Point> newPoints = new HashMap<>(allPoints);
                    newPoints.put(direction2, new Point(color, false));
                    Map<Cell, List<Cell>> newNeighbors = filledNeighbors(puzzle);
                    if(newNeighbors.get(direction2).size()==1) direction = direction2;
                }

                if (direction == null) continue;
                System.out.println(direction + ", " + color);
                if(isValidMove(direction, new Point(color, false), puzzle)){
                    forced.put(direction, new Point(color, false));
                }
            }

        }
        Printer.printGrid(puzzle);
        if (forced.size()!=0){
            System.out.println(forced);
            puzzle.setPoints(combinePoints(allPoints, forced));
            forcedEdges(puzzle);
            forcedDirection(puzzle);
        }
        forcedEdges(puzzle);
    }

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

    public static Map<Cell, List<Cell>> filledNeighbors(Puzzle puzzle){
        Map<Cell, List<Cell>> neighbors = neighbors(puzzle);
        Map<Cell, List<Cell>> filledNeighbors = new HashMap<>();
        for(Map.Entry<Cell, Point> entry : puzzle.getPoints().entrySet()){
            List<Cell> filled = new ArrayList<>();
            for(Cell cell : neighbors.get(entry.getKey())){
                filled.add(cell);
            }
            filledNeighbors.put(entry.getKey(), filled);
        }
        return filledNeighbors;
    }

    public static boolean isValidMove(Cell moveKey, Point moveValue, Puzzle puzzle){
        if(!moveKey.inBounds(puzzle.getRows(), puzzle.getCols())) return false;
        Map<Cell, Point> allPoints = puzzle.getPoints();
        Map<Cell, List<Cell>> filledNeighbors = filledNeighbors(puzzle);
        System.out.println(moveKey);
        if (filledNeighbors.get(moveKey) == null) return false; // loner point
        System.out.println("loner " + moveKey);
        int adj = 0;
        for(Cell nb : filledNeighbors.get(moveKey)){
            if(allPoints.get(nb).color()==moveValue.color()){
                adj++;
                // if the neighboring cell is a root and the root already has a neighbor, invalid
                if(allPoints.get(nb).isRoot()){
                    if (filledNeighbors.get(nb) != null) return false;
                    System.out.println("root has neighbor" + moveKey);
                }
            }
        }
        System.out.println(moveKey + ", " + adj);
        if(adj == 1) return true;
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
                    System.out.println(entry);
                    Point newPoint = new Point (entry.getValue().color(), false);
                    forcedEdges.put(corner, newPoint);
                } 
            }
        }
        puzzle.setPoints(combinePoints(puzzle.getPoints(), forcedEdges));
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
                if(!nb.inBounds(puzzle.getRows(), puzzle.getCols())) OB++;
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

    public static Map<Cell, List<Cell>> neighbors(Puzzle puzzle){
        Map<Cell, Point> points = puzzle.getPoints();
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
 * Heuristics:
 * paths cannot cross
 * all squares must be filled
 * paths must fit within bounds
 * no "zigzagging"
 */