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
        List<Cell> diags = List.of(cell.NE(puzzle),cell.NW(puzzle), cell.SE(puzzle), cell.SW(puzzle));
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

    Cell north(Puzzle puzzle) { Cell n = new Cell(x, y + 1); if(n.inBounds(puzzle))return n; return null; }
    Cell east(Puzzle puzzle)  { Cell e = new Cell(x + 1, y); if(e.inBounds(puzzle)) return e; return null;}
    Cell south(Puzzle puzzle) { Cell s = new Cell(x, y - 1); if(s.inBounds(puzzle)) return s; return null;}
    Cell west(Puzzle puzzle)  { Cell w = new Cell(x - 1, y); if(w.inBounds(puzzle)) return w; return null;}
    Cell NW(Puzzle puzzle)    { Cell nw = new Cell(x-1, y+1); if(nw.inBounds(puzzle)) return nw; return null;}
    Cell SW(Puzzle puzzle)    { Cell sw = new Cell(x-1, y-1); if(sw.inBounds(puzzle)) return sw; return null;}
    Cell NE(Puzzle puzzle)    { Cell ne = new Cell(x+1, y+1); if(ne.inBounds(puzzle)) return ne; return null;}
    Cell SE(Puzzle puzzle)    { Cell se = new Cell(x+1, y-1); if(se.inBounds(puzzle)) return se; return null;}
}
