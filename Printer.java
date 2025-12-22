import java.util.Map;

public class Printer {
    //print grid to visualize points
    public static void printGrid(Puzzle puzzle) {
    Map<Cell, Point> points = puzzle.getPoints();
    int rows = puzzle.getRows();
    int cols = puzzle.getCols();
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
                    cellChar = points.get(key).color();
                    if(points.get(key).isRoot()) cellChar = Character.toUpperCase(cellChar); 
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
