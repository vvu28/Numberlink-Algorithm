import java.util.*;
final class Puzzle{
    private final int rows;
    private final int cols;
    private final Map<Cell, Point> points; //this includes filled cells only
    private final Map<Character, List<Cell>> endpoints;

    //constructor
    public Puzzle(){
        rows = 0;
        cols = 0;
        points = new HashMap<Cell, Point>();
        endpoints = new HashMap<>();
    }

    public Puzzle(int rows, int cols, Map<Cell, Point> points) {
        this.rows = rows;
        this.cols = cols;
        this.points = Map.copyOf(points);

        Map<Cell, List<Cell>> sameNBs = sameNeighbors();
        Map<Character, List<Cell>> endpoints = new HashMap<>();

        for(Map.Entry<Cell, Point> p : points.entrySet()){
            Cell cell = p.getKey();
            Point point = p.getValue();
            int same = sameNBs.get(cell).size();
            boolean isRoot = point.isRoot();

            char color = point.color();
            if( (same == 1 && !isRoot) || (same == 0 && isRoot)){
                endpoints
                .computeIfAbsent(color, k -> new ArrayList<>())
                .add(cell);
            }
        }
        this.endpoints = endpoints;
    }

    //neighbors of same color
    public Map<Cell, List<Cell>> sameNeighbors(){
        Map<Cell, List<Cell>> neighbors = filledNeighbors();
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
    public Map<Cell, List<Cell>> filledNeighbors(){
        Map<Cell, List<Cell>> filledNeighbors = new HashMap<>();
        for(Map.Entry<Cell, Point> entry : points.entrySet()){
            List<Cell> filled = new ArrayList<>();
            Cell cell = entry.getKey();
            for(Cell nb : cell.neighbors(this)){
               if(points.containsKey(nb)) filled.add(nb);
            }
            filledNeighbors.put(cell, filled);
        }
        return filledNeighbors;
    }

    //getter methods
    public Map<Cell, Point> getPoints(){
        return points;
    }
    public int getRows(){
        return rows;
    }
    public int getCols(){
        return cols;
    }
    public Map<Character, List<Cell>> endpoints(){
        return endpoints;
    }

    //update puzzle after a move by creating a new one
    public Puzzle withMove(char color, Cell cell){
        Map<Cell, Point> newPoints = new HashMap<>(points);
        newPoints.put(cell, new Point(color, false));

        return new Puzzle(rows, cols, newPoints);
    }

    //isSolved
    public boolean isSolved() {
        if (points.size() != rows * cols) return false; //full map
        Map<Character, Integer> rootCounts = new HashMap<>();
        for (Map.Entry<Cell, Point> entry : points.entrySet()) {
            Point p = entry.getValue();
            if (!entry.getKey().inBounds(this)) return false; //OB
            rootCounts.merge(p.color(), p.isRoot() ? 1 : 0, Integer::sum); //count roots per color
        }
        for (int count : rootCounts.values()) {
            if (count != 2) return false; // there aren't 2 roots
        }
        return true;
    }
}