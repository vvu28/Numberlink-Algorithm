import java.util.*;
public record Cell(int x, int y){
    boolean inBounds(Puzzle puzzle) {
        int rows = puzzle.getRows();
        int cols = puzzle.getCols();
        return x >= 0 && x < cols && y >= 0 && y < rows;
    }

    //map of diagonals of same color as entry
    static Map<Cell, Point> diagonalNBS(Cell cell, Point point, Puzzle puzzle){;
        Map<Cell, Point> allPoints = puzzle.getPoints();
        List<Cell> diags = List.of(cell.NE(),cell.NW(), cell.SE(), cell.SW());
        Map<Cell, Point> diagonalNBS = new HashMap<>();
        for(Cell diag: diags){
            Point newPoint = new Point(point.color(), false);
            boolean containsKey = false;
            char color= ' ';
            if(allPoints.containsKey(diag)) containsKey = true;
            if(containsKey) color = allPoints.get(diag).color();
            if(containsKey && (color==point.color())) diagonalNBS.put(diag, newPoint);
        }
        return diagonalNBS;
    }

    public List<Cell> neighbors(Puzzle puzzle){
        List<Cell> neighbors = new ArrayList<>();
        if(y>0) neighbors.add(this.south());
        if(y<puzzle.getRows()-1) neighbors.add(this.north());
        if(x>0) neighbors.add(this.west());
        if(x<puzzle.getCols()-1) neighbors.add(this.east());
        return neighbors;
    }

    Cell north() { return new Cell(x, y + 1); }
    Cell east()  { return new Cell(x + 1, y); }
    Cell south() { return new Cell(x, y - 1); }
    Cell west()  { return new Cell(x - 1, y); }
    Cell NW()    { return new Cell(x-1, y+1);}
    Cell SW()    { return new Cell(x-1, y-1);}
    Cell NE()    { return new Cell(x+1, y+1);}
    Cell SE()    { return new Cell(x+1, y-1);}
}
